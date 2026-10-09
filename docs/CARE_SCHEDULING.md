# Care Scheduling

Implementación basada en la sección 2.6.7 de `SafeDiary_Doc/docs/20-chap-2.md` (EP-06:
US-014, US-038, US-043, US-044, US-048, US-049, US-050 y TS-006) y en el prototipo móvil
del módulo *Scheduling*. Sigue las mismas capas que Clinician Directory: dominio sin JPA,
commands/queries, un servicio de comandos y uno de consultas con `Result<T, ApplicationError>`,
repositorios de dominio, adaptadores JPA, resources REST y facade ACL.

## Modelo

| Elemento | Responsabilidad |
|---|---|
| `ContactRequest` (aggregate) | Chat privado paciente ↔ psicólogo con sus mensajes. `PENDING` → `ACCEPTED` / `REJECTED`. No es una cita ni da acceso al diario. |
| `Appointment` (aggregate) | Propuesta (`REQUESTED`), reserva temporal (`HELD`), cita (`CONFIRMED`), `CANCELLED`, `EXPIRED`, `COMPLETED`. Guarda el importe acordado. |
| `SlotHold` (entity de `Appointment`) | Retención de 1 hora creada al aceptar la propuesta. |
| `ClinicalSession` (aggregate) | Videollamada de una cita confirmada: inicio, fin y resultado operativo. |
| `AvailabilitySlot` | Ventanas semanales reservables del psicólogo (zona `America/Lima`). |
| `SummaryAccessAudit` | Auditoría de cada lectura del resumen emocional autorizado. |

Reglas principales:

- Solo se contacta o reserva a psicólogos **verificados y publicados** en Clinician Directory.
- Una propuesta debe estar dentro de la disponibilidad, empezar después de la hora de retención y no
  chocar con otra reserva activa. Proponer otro horario cancela la propuesta anterior.
- Solo una reserva activa (`HELD` vigente o `CONFIRMED`) ocupa un horario. Las aceptaciones de la misma
  agenda se serializan con un bloqueo pesimista.
- Al aceptar, la reserva temporal pide el cobro a Payments (flujo 4 del reporte). La retención vence a la
  hora; un job (`care-scheduling.hold-expiration.*`) la expira y libera el horario.
- Solo un pago aprobado, por el importe acordado y para una retención vigente, confirma la cita. Las
  notificaciones duplicadas no confirman otra cita; un pago tardío se devuelve a Payments para reembolso.
  Un pago fallido mantiene la retención para reintentar (pantalla *Pago no completado*).
- Solo los dos participantes entran a la reunión, desde 10 minutos antes hasta el fin de la cita.
- Al cerrar como `COMPLETED` se publica `CompletedCareSessionDto`, que habilita una reseña en Clinician
  Directory. `NO_SHOW` y `NOT_HELD` no completan la cita y piden a Payments aplicar su política.

## Integraciones (context map)

| Contexto | Patrón | Implementación |
|---|---|---|
| Clinician Directory → Care Scheduling | Customer/Supplier + ACL | `ClinicianDirectoryAclClient` usa `ClinicianDirectoryContextFacade` (ficha publicada, tarifa vigente y ficha del psicólogo autenticado). |
| Care Scheduling → Clinician Directory | Published Language | Evento `CompletedCareSessionDto`, consumido por `CareSessionEventConsumer`. |
| Care Scheduling ↔ Payments & Payouts | Customer/Supplier + Published Language | Care Scheduling publica `AppointmentChargeRequestedDto` y `AppointmentRefundRequestedDto`; Payments responde con `AppointmentPaymentResultDto` (lo consume `PaymentResultEventConsumer`) o llama a `CareSchedulingContextFacade`. |
| AssistantAI → Care Scheduling | Customer/Supplier + ACL | `AssistantAiEmotionalSummaryClient` lee solo el último resumen semanal. |
| IAM → Care Scheduling | OHS + ACL | Puerto `IamClient` (identidad y consentimiento), con un adaptador de desarrollo hasta que exista IAM. |

**Pendiente en Payments & Payouts:** hoy solo cobra suscripciones. Para cobrar citas debe consumir
`AppointmentChargeRequestedDto` (la `idempotencyKey` es estable por reserva), crear el cobro y publicar
`AppointmentPaymentResultDto` tras verificar la firma de la pasarela. Mientras tanto, en dev existe
un endpoint que simula ese resultado.

## Arrancar en desarrollo

Igual que en [CLINICIAN_DIRECTORY.md](CLINICIAN_DIRECTORY.md): PostgreSQL local y `.\mvnw.cmd spring-boot:run`.
En el perfil `dev` quedan activos estos adaptadores temporales, que en `prod` se rechazan:

| Propiedad | Uso |
|---|---|
| `care-scheduling.demo-identity.enabled` | Headers `X-Account-Id` y `X-Role` (`PATIENT` o `PSYCHOLOGIST`); `X-Consent-Ref` como consentimiento vigente para el resumen. |
| `care-scheduling.payment-simulation.enabled` | `POST /api/v1/dev/care-scheduling/appointments/{id}/payment-result`. |

El psicólogo usa su `accountId`; Care Scheduling resuelve su ficha en Clinician Directory.

## Endpoints

Prefijo `/api/v1`. Las horas viajan como instantes ISO-8601 en UTC (`2026-10-09T20:00:00Z` = 15:00 en Lima).

| Método | Ruta | Acceso / propósito | Pantalla |
|---|---|---|---|
| POST | `/contact-requests` | Paciente: solicitar contacto con mensaje; reutiliza el chat abierto | Solicitar contacto |
| GET | `/contact-requests` | Participante: conversaciones con su último mensaje | Mis citas, Mi agenda |
| PATCH | `/contact-requests/{id}/decision` | Psicólogo: `{"accept": true}` abre el chat, `false` lo rechaza | Mi agenda |
| GET / POST | `/contact-requests/{id}/messages` | Participante: leer o enviar mensajes (máx. 500) | Chat de coordinación |
| PUT | `/schedule/availability` | Psicólogo: ventanas semanales | — |
| GET | `/schedule/clinicians/{id}/slots?date=2026-10-09` | Horarios del día; el dueño ve `HELD`/`CONFIRMED`, los demás `UNAVAILABLE` | Mi agenda |
| GET | `/schedule/agenda?from=&to=` | Psicólogo: citas del rango (7 días por defecto) | Mi agenda |
| POST | `/schedule/proposals` | Psicólogo: proponer horario; no reserva ni cobra | Proponer horario |
| POST | `/schedule/proposals/{appointmentId}/acceptance` | Paciente: aceptar, retener 1 hora y pedir el cobro | Aceptar horario, Reserva temporal |
| GET | `/appointments` | Paciente: propuestas, reservas y citas; la app agrupa por `status` en Próximas, Pendientes e Historial | Mis citas |
| POST | `/appointments/{id}/cancellation` | Participante: cancelar y liberar el horario | Cancelar reserva |
| GET | `/appointments/{id}/session` | Participante: estado de la sesión | Sala de espera |
| POST | `/appointments/{id}/session/access` | Participante: entrar a la videollamada (devuelve `joinUrl`) | Sesión iniciada |
| POST | `/appointments/{id}/session/closure` | Psicólogo: `COMPLETED`, `NO_SHOW` o `NOT_HELD` | Cerrar la atención |
| GET | `/appointments/{id}/authorized-summary` | Psicólogo: resumen con consentimiento vigente (auditado) | — |

## Recorrido en Swagger

1. Publicar una ficha en Clinician Directory con la cuenta `1` (`PSYCHOLOGIST`), según su guía.
2. Cuenta `1`, `PSYCHOLOGIST`: `PUT /schedule/availability`
   ```json
   [{"dayOfWeek":"THURSDAY","startTime":"09:00","endTime":"21:00"}]
   ```
3. Cuenta `2`, `PATIENT`: `POST /contact-requests`
   ```json
   {"clinicianId": 1, "message": "Hola, quisiera agendar una sesión sobre ansiedad en exámenes."}
   ```
4. Cuenta `1`: `PATCH /contact-requests/{id}/decision` con `{"accept": true}` y luego
   `POST /schedule/proposals` con `{"contactRequestId": 1, "startsAt": "2026-10-15T20:00:00Z"}`.
5. Cuenta `2`: `POST /schedule/proposals/{appointmentId}/acceptance` → `HELD`, `holdRemainingSeconds` y `paymentReference`.
6. Sin headers: `POST /dev/care-scheduling/appointments/{id}/payment-result` con
   `{"paymentReference":"care-appointment-1","approved":true,"amount":120.00,"currency":"PEN"}` → `CONFIRMED`.
   Con `"approved": false` la reserva sigue en `HELD` para reintentar.
7. Dentro de la ventana de la cita: `POST /appointments/{id}/session/access` (ambas cuentas) y,
   con la cuenta `1`, `POST /appointments/{id}/session/closure` con `{"outcome":"COMPLETED"}`.
8. Cuenta `2`: `POST /clinicians/1/reviews` con `{"appointmentId": ..., "rating": 5}`.

`CareSchedulingApiIntegrationTest` ejecuta este recorrido completo, la expiración con pago tardío y
las cancelaciones con y sin reembolso.

## Antes de producción

- Reemplazar `DevelopmentIamClient` por IAM.
- Implementar en Payments & Payouts el cobro de citas (ver arriba) y retirar el simulador.
- `ConfigurableVideoProviderAdapter` usa nombres de sala imposibles de adivinar bajo
  `care-scheduling.video.base-url`; conviene un proveedor con tokens por participante.
