# Clinician Directory

Implementación basada en la sección 2.6.6 de `SafeDiary_Doc/docs/20-chap-2.md`.
Sigue las capas de los otros bounded contexts: dominio sin JPA, commands/queries,
servicios de aplicación con `Result<T, ApplicationError>`, repositorios de dominio,
adaptadores JPA, entidades de persistencia, assemblers, resources REST y facade ACL.

## Arrancar en Windows

Requisitos: Java 21 y PostgreSQL local en ejecución. El Maven Wrapper viene en el repo;
no necesitas instalar Maven.

Desde PowerShell:

```powershell
cd C:\Users\Kamil\Desktop\SafeDiary\safediary-platform

# Solo la primera vez, si safediary_db todavía no existe:
& "C:\Program Files\PostgreSQL\18\bin\createdb.exe" -h localhost -U postgres safediary_db

# Usa la contraseña de TU PostgreSQL:
$env:DB_PASSWORD = "TU_PASSWORD"
$env:ASSISTANTAI_LLM_PROVIDER = "mock"
.\mvnw.cmd spring-boot:run
```

El perfil `dev` es el predeterminado. Usa `localhost:5432`, base `safediary_db`,
usuario `postgres`. También puedes cambiar `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USER` y `PORT`. En desarrollo Hibernate crea los esquemas y actualiza las
tablas; no ejecutes todo el SQL de referencia sobre una base existente.
El proveedor `mock` permite arrancar sin keys de IA.

- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Detener el servidor: Ctrl+C en la terminal.
- Pruebas: `.\mvnw.cmd test`.

## Identidad de desarrollo

IAM todavía no está implementado en la plataforma. Los endpoints privados usan el
puerto `IamRoleClient`. Su adaptador provisional acepta los siguientes headers
**solo en dev**, con `clinician-directory.demo-identity.enabled=true`:

| Header | Ejemplo |
|---|---|
| `X-Account-Id` | `1` |
| `X-Role` | `PSYCHOLOGIST`, `PATIENT`, `ADMIN` o `MODERATOR` |

En Swagger, pulsa **Try it out**, completa ambos headers y ejecuta la operación.
Sin identidad responde 401; con un rol o propietario incorrecto responde 403.
En `prod` se rechazan estos headers incluso si se activa también `dev`.
Antes de habilitar escrituras en producción hay que sustituir ese adaptador por
identidad y roles autenticados de IAM.

## Endpoints

Prefijo: `/api/v1`. Los IDs de `clinicians` pertenecen a este contexto; no son
los IDs de los antiguos `psychologists` de Profiles.

| Método | Ruta | Acceso / propósito |
|---|---|---|
| POST | `/clinicians` | Psicólogo: crear una única ficha propia en DRAFT |
| GET | `/clinicians/me` | Psicólogo: consultar su ficha, incluso oculta |
| PUT | `/clinicians/{id}` | Propietario: editar ficha, especialidades, banner y tarifa |
| POST | `/clinicians/{id}/publication` | Propietario: publicar con verificación APPROVED |
| DELETE | `/clinicians/{id}/publication` | Propietario: ocultar ficha |
| POST | `/clinicians/{id}/verification-requests` | Propietario: solicitar verificación |
| GET | `/clinicians/{id}/verification` | Propietario o administrador: estado y referencias privadas |
| GET | `/verification-requests` | Administrador: solicitudes pendientes |
| PATCH | `/verification-requests/{id}/decision` | Administrador independiente: aprobar o rechazar |
| GET | `/clinicians` | Público: búsqueda y filtros |
| GET | `/clinicians/{id}` | Público: ficha aprobada y publicada |
| GET | `/clinicians/{id}/rating` | Público: promedio y cantidad de reseñas |
| GET | `/clinicians/{id}/reviews` | Público: reseñas anónimas y contador de utilidad |
| POST | `/clinicians/{id}/reviews` | Paciente: reseñar una cita propia completada |
| PUT | `/reviews/{id}/helpful-vote` | Usuario: fijar utilidad en true/false; reintentos idempotentes |
| POST | `/reviews/{id}/reports` | Usuario: denunciar sin retirar automáticamente la reseña |
| DELETE | `/reviews/{id}?confirmed=true` | Autor: eliminar con confirmación explícita |
| GET | `/review-reports?status=PENDING` | Administrador o moderador: consultar denuncias |
| PATCH | `/review-reports/{id}/resolution` | Moderador independiente: desestimar o retirar la reseña |
| GET | `/clinicians/{id}/trust-score` | Propietario: puntaje, factores y estado de datos insuficientes |

Búsqueda: `text` (nombre o especialidad, sin distinguir mayúsculas),
`specialty` (coincidencia exacta sin distinguir mayúsculas),
`maxAmount` y `currency`, `page=0`, `size=20` (máximo 100).
Para comparar importes es obligatorio indicar moneda al usar `maxAmount`.
Las búsquedas sin coincidencias devuelven `[]`; una ficha no elegible devuelve 404.

## Recorrido en Swagger

1. Con `X-Account-Id: 1`, `X-Role: PSYCHOLOGIST`, crear una ficha:

```json
{
  "displayName": "Ana Torres",
  "professionalTitle": "Psicóloga clínica",
  "bio": "Acompañamiento para ansiedad y estrés.",
  "bannerRef": "https://example.org/banner.png",
  "specialties": ["Ansiedad", "Estrés"],
  "amount": 80.00,
  "currency": "PEN",
  "durationMinutes": 50
}
```

2. Con el `id` de la ficha, solicitar verificación:

```json
{
  "licenseNumber": "CPP-1234",
  "specialty": "Ansiedad",
  "documentRef": "private://credentials/license.pdf"
}
```

`documentRef` es una referencia opaca a almacenamiento privado; el endpoint no
sube archivos ni descarga esa URI. `bannerRef` es una URL HTTPS pública.
El almacenamiento real y su autorización deben integrarse antes de producción.

3. Con otra cuenta, por ejemplo `900`, y rol `ADMIN`, decidir la solicitud:

```json
{"decision": "APPROVED"}
```

Para rechazar: `{"decision":"REJECTED","reason":"Documento ilegible"}`.
No se puede revisar la propia solicitud ni decidir dos veces la misma solicitud.
Después de un rechazo se puede enviar una nueva solicitud.

4. Volver a la cuenta `1`, rol `PSYCHOLOGIST`, y ejecutar
   `POST /clinicians/{id}/publication`. Ahora aparece en la búsqueda pública.
   Una nueva solicitud de verificación oculta la ficha hasta aprobarla y publicarla.

5. Actualizar con `PUT /clinicians/{id}` usando el mismo cuerpo de creación.
   Si cambia el importe, moneda o duración, se conserva la tarifa anterior y se
   crea una versión nueva. Las reservas deben conservar su propia copia del precio.

## Reseñas e integración con Care Scheduling

Care Scheduling publica este evento al cerrar una atención como `COMPLETED`
(ver [CARE_SCHEDULING.md](CARE_SCHEDULING.md)). Las reseñas necesitan una
sesión completada recibida por la facade ACL o el consumidor de eventos internos:

```java
directoryFacade.recordCompletedSession(new CompletedCareSessionDto(
    appointmentId, clinicianId, patientAccountId, durationMinutes, completedAt));
```

También se puede publicar `CompletedCareSessionDto` con
`ApplicationEventPublisher`; `CareSessionEventConsumer` registra la elegibilidad.
Los reintentos del mismo evento son idempotentes; un evento contradictorio falla.
No existe un endpoint público para inventar sesiones completadas. Las pruebas de
integración ejercitan este recorrido completo.

Tras ese evento, el paciente puede enviar:

```json
{"appointmentId": 10, "rating": 5, "text": "Me ayudó la sesión."}
```

Se admite una reseña por cita, incluso después de eliminarla. La eliminación
retira texto y puntuación, desactiva votos y recalcula los agregados. Las respuestas
públicas no incluyen cuenta del paciente, cita ni identidades de votantes.

Utilidad: `{"helpful":true}` o `{"helpful":false}`.
Denuncia: `{"reason":"SPAM","comment":"Comentario opcional"}`.
Motivos: `OFFENSIVE_CONTENT`, `FALSE_INFORMATION`, `SPAM`, `OTHER`.
Resolución: `{"removeReview":true,"resolution":"Motivo documentado"}`.
La denuncia permanece pendiente hasta la decisión; denunciante, autor y profesional
reseñado no pueden resolver su propio caso.

## Valoración y confianza

Solo cuentan reseñas publicadas. Sin reseñas, el promedio es `null`.
La política inicial `v1` del puntaje usa `promedio / 5 × 100` con un mínimo de
tres reseñas; por debajo devuelve `score: null`, `sufficientData: false`.
El desglose muestra sesiones completadas y minutos, pero el volumen y los votos
de utilidad no incrementan el puntaje. Esta política es explícita y provisional:
retención y otros factores requieren los futuros contratos de Care Scheduling.

## Límites del contexto

- Su esquema es `clinician_directory`. No modifica citas, pagos ni contenido clínico.
- `ClinicianDirectoryContextFacade.fetchPublishedClinicianRate` es el contrato para
  futuros consumidores de fichas y tarifas elegibles.
- Los endpoints heredados de Profiles siguen presentes por compatibilidad; no se
  migran ni se asumen equivalencias entre sus IDs y los IDs del directorio.
- Los cambios de estado emiten eventos internos sin documentos ni contenido de reseñas.
- Los comandos sobre un profesional toman un bloqueo en su ficha para serializar
  verificación, tarifas, reseñas, votos, denuncias y recálculos. La base también
  impone unicidad de cuenta, cita, versión de tarifa y usuario/reseña.
- El SQL de referencia es diseño para una base nueva, no una migración de producción.
