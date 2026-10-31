# Payments & Payouts

Implementación del Bounded Context `payments` en SafeDiary Platform basada en Arquitectura Hexagonal y Domain-Driven Design (DDD). Gestiona el ciclo de vida de suscripciones para pacientes (`FREE`, `TERRA`, `ASTRUM`), la integración con Stripe Checkout y el marketplace de consultorías con psicólogos mediante cuentas conectadas Stripe Express (cobros divididos / destination charges y retiros bancarios).

## Modelo

| Elemento | Responsabilidad |
|---|---|
| `Subscription` (aggregate) | Membresía del paciente (`FREE`, `TERRA`, `ASTRUM`), estado (`ACTIVE`, `PENDING_PAYMENT`, `CANCELLED`, `EXPIRED`), periodo de vigencia `activeUntil` e identificadores de Stripe (`sub_...`, `cus_...`). |
| `ConnectedAccount` (aggregate) | Cuenta conectada Stripe Express del psicólogo (`acct_...`), registrando capacidades de cobro (`chargesEnabled`), retiros (`payoutsEnabled`) y datos completados (`detailsSubmitted`). |
| `PaymentIntent` (aggregate) | Operación de cobro idempotente para consultorías o citas (`CONSULTATION`), asociando monto, comisión retenida, moneda, clave de idempotencia, referencia de pasarela y estado (`CREATED`, `APPROVED`, `FAILED`). |
| `ProcessedWebhookEvent` (entity) | Registro de eventos Stripe procesados para garantizar estricta idempotencia y evitar cobros o renovaciones duplicadas. |
| `SubscriptionPlan` (value object) | Tarifas y periodicidad: `FREE` ($0), `TERRA` ($19.99/mes) y `ASTRUM` ($100.00/año). |

Reglas principales:

- **Activación Free Directa:** La suscripción `FREE` se activa inmediatamente en la base de datos sin invocar a Stripe ni requerir tarjeta bancaria.
- **Doble Propagación de Metadatos:** En sesiones de Checkout de suscripción, `userId`, `patientAccountId` y `plan` se inyectan en los metadatos de la sesión y en `SessionCreateParams.SubscriptionData` para asegurar que renovaciones y eventos periódicos de Stripe no pierdan el contexto del paciente.
- **Degradación Automática a Free:** Cuando se cancela una suscripción o Stripe emite `customer.subscription.deleted`, la membresía del paciente se degrada a `FREE` y se invalidan las fechas de vigencia previas.
- **Onboarding de Psicólogos:** Se crean cuentas conectadas tipo Express (`AccountCreateParams.Type.EXPRESS`) con capacidades `card_payments` y `transfers`, generando enlaces de onboarding de un solo uso (`AccountLink`).
- **Cobro Dividido (Split Payment / Destination Charge):** El pago de citas (`Mode.PAYMENT`) deduce la comisión de la plataforma (`application_fee_amount`) y transfiere el neto directamente al `acct_...` del psicólogo (`transfer_data.destination`).
- **Verificación de Firma Dual:** El webhook valida criptográficamente con `stripe.webhook-secret` (eventos de suscripción de plataforma) y realiza fallback automático a `stripe.connect-webhook-secret` para eventos de cuentas conectadas.
- **Deserialización Segura:** Si `event.getDataObjectDeserializer().getObject()` viene vacío por diferencias de versión entre la API de Stripe y el SDK, se activa `deserializeUnsafe()` como fallback seguro.
- **Confirmación Automática de Citas:** Al confirmarse el pago de una consultoría (`checkout.session.completed` con `type=CONSULTATION`), se publica internamente `AppointmentPaymentResultDto`, notificando a `CareScheduling` para confirmar la cita de forma desacoplada.

## Integraciones (context map)

| Contexto | Patrón | Implementación |
|---|---|---|
| Payments ↔ Stripe API Gateway | Anti-Corruption Layer (ACL) | `StripeGatewayClient` implementado por `StripeGatewayAclAdapter`, aislando la lógica de dominio del SDK de Stripe. |
| Payments ↔ Care Scheduling | Published Language + Customer/Supplier | `AppointmentChargeEventConsumer` escucha `AppointmentChargeRequestedDto`. Al confirmarse el pago en el webhook, Payments publica `AppointmentPaymentResultDto` consumido por `PaymentResultEventConsumer`. |
| Payments → Otros Bounded Contexts | Open Host Service (OHS) + ACL | `PaymentsContextFacade` expone `fetchPatientPlanStatus()` para validar límites en AssistantAI, Rutines y Clinician Directory. |
| IAM ↔ Payments | Seguridad Stateless (JWT) | `WebSecurityConfiguration` exime de validación JWT a `/api/v1/payments/webhooks/**` y a las URLs de retorno del navegador tras Checkout u onboarding. |

## Configuración y Entornos

Las propiedades usan placeholders con valores por defecto para funcionar sin configuración en desarrollo local (generando sesiones simuladas) y tomar variables de entorno en producción (Render):

| Variable de Entorno | Propiedad | Propósito / Valor por Defecto |
|---|---|---|
| `STRIPE_API_KEY` | `stripe.api-key` | Clave secreta (`sk_test_...` o `sk_live_...`). Si está vacía, opera en modo simulación. |
| `STRIPE_PUBLIC_KEY` | `stripe.public-key` | Clave publicable para frontend móvil o web (`pk_test_...`). |
| `STRIPE_WEBHOOK_SECRET` | `stripe.webhook-secret` | Secreto del endpoint webhook de plataforma (`whsec_...`). |
| `STRIPE_CONNECT_WEBHOOK_SECRET` | `stripe.connect-webhook-secret` | Secreto del endpoint webhook de Stripe Connect. |
| `STRIPE_TERRA_PRICE_ID` | `stripe.subscription.terra.price-id` | ID del precio recurrente mensual de TERRA en Stripe. |
| `STRIPE_ASTRUM_PRICE_ID` | `stripe.subscription.astrum.price-id` | ID del precio recurrente anual de ASTRUM en Stripe. |
| `STRIPE_SUCCESS_URL` | `stripe.success-url` | Redirección de éxito (`http://localhost:8080/api/v1/payments/subscriptions/success`). |
| `STRIPE_CANCEL_URL` | `stripe.cancel-url` | Redirección de cancelación (`http://localhost:8080/api/v1/payments/subscriptions/cancel`). |
| `STRIPE_CONNECT_REFRESH_URL` | `stripe.connect.refresh-url` | Redirección si expira el link de onboarding de Connect. |
| `STRIPE_CONNECT_RETURN_URL` | `stripe.connect.return-url` | Redirección al completar el onboarding de Connect. |
| `STRIPE_MARKETPLACE_FEE_PERCENT` | `stripe.marketplace.fee-percent` | Porcentaje de comisión retenido por SafeDiary (por defecto: `15.0`%). |

## Endpoints

Prefijo `/api/v1/payments`.

| Método | Ruta | Acceso | Propósito |
|---|---|---|---|
| POST | `/subscriptions/checkout` | Token (paciente) | Iniciar sesión de Stripe Checkout para TERRA/ASTRUM o activar FREE de inmediato. |
| GET | `/subscriptions/current` | Token (paciente) | Consultar el plan actual y vigencia (por defecto FREE). |
| POST | `/subscriptions/cancel` | Token (paciente) | Cancelar la suscripción recurrente en Stripe y degradar a FREE. |
| GET | `/subscriptions/success` | Público | Landing de retorno tras pago exitoso en Checkout. |
| GET | `/subscriptions/cancel` | Público | Landing de retorno si el usuario cancela el Checkout. |
| POST | `/connect/accounts` | Token (psicólogo) | Crear cuenta Express y generar `AccountLink` para onboarding en Stripe Connect. |
| GET | `/connect/accounts/{id}` | Token (psicólogo) | Consultar si la cuenta conectada tiene pagos y retiros habilitados (`ready`). |
| GET | `/connect/refresh` | Público | Landing si expira el enlace de onboarding de Connect. |
| GET | `/connect/return` | Público | Landing al finalizar el formulario de onboarding de Connect. |
| POST | `/consultations/checkout` | Token (paciente) | Iniciar Checkout para cita médica con split payment y comisión de plataforma. |
| POST | `/webhooks/stripe` | Público (Firma Stripe) | Receptor único de webhooks con verificación criptográfica y procesamiento idempotente. |

## Recorrido de Prueba con Stripe CLI

### 1. Iniciar reenvío local de webhooks
```bash
stripe listen --forward-to localhost:8080/api/v1/payments/webhooks/stripe
```
Copia el `whsec_...` obtenido y configúralo en `STRIPE_WEBHOOK_SECRET`.

### 2. Suscripción y Activación (Flujo 1)
1. **Crear checkout para plan TERRA:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/payments/subscriptions/checkout \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TOKEN_PACIENTE>" \
     -d '{"patientAccountId": 1, "plan": "TERRA"}'
   ```
2. **Simular evento de confirmación:**
   ```bash
   stripe trigger checkout.session.completed
   ```
3. **Verificar degradación al cancelar en Stripe:**
   ```bash
   stripe trigger customer.subscription.deleted
   ```

### 3. Onboarding y Pago de Consultoría Connect (Flujo 2)
1. **Generar onboarding Express para psicólogo:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/payments/connect/accounts \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TOKEN_PSICOLOGO>" \
     -d '{"psychologistAccountId": 20, "email": "dra.garcia@safediary.app"}'
   ```
2. **Consultar estado de la cuenta Connect:**
   ```bash
   curl -X GET http://localhost:8080/api/v1/payments/connect/accounts/20 \
     -H "Authorization: Bearer <TOKEN_PSICOLOGO>"
   ```
3. **Pago de consulta con split payment hacia el psicólogo:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/payments/consultations/checkout \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <TOKEN_PACIENTE>" \
     -d '{"patientAccountId": 1, "psychologistAccountId": 20, "appointmentId": 105, "totalAmount": 70.00, "platformFee": 10.50}'
   ```
