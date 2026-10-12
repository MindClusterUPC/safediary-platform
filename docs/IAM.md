# IAM (Identity & Access Management)

Implementación basada en la sección 2.6.1 de `SafeDiary_Doc/docs/20-chap-2.md` y en las pantallas de
autenticación del prototipo. Cubre US-001, US-003, US-009, US-015, US-016, US-023, US-025, US-033, US-045,
US-046, TS-002 y TS-007, con las mismas capas que los demás bounded contexts.

## Cómo autentica al resto de la plataforma

1. El usuario se registra o inicia sesión y recibe un `accessToken` (JWT, 24 h por defecto).
2. La app (o Swagger con el botón **Authorize**) lo envía como `Authorization: Bearer <token>`.
3. `BearerAuthorizationRequestFilter` valida el token y que la cuenta siga activa, y publica la identidad en
   los headers que los demás contextos ya leen: `X-Account-Id`, `X-Role` y, para pacientes,
   `X-Patient-Account-Id`. Los valores que envíe el cliente en esos headers se reemplazan, así que no se
   puede suplantar a otra cuenta cuando hay token.
4. Un token inválido, vencido o de una cuenta eliminada responde 401.

Los endpoints privados de IAM (`/accounts`, `/consents`, `/consent-scopes`, `/vault`, `/audit-events`)
exigen token. El resto de la API conserva sus reglas actuales. Otros contextos pueden consumir
`IamContextFacade` para obtener la cuenta autenticada o validar un consentimiento.

**Importante:** Clinician Directory y Care Scheduling todavía usan su adaptador de identidad de desarrollo,
que solo funciona en el perfil `dev`. Con IAM ya pueden usar el token en `dev`; para que funcionen en `prod`,
sus responsables deben reemplazar ese adaptador por `IamContextFacade.fetchAuthenticatedAccount()`.

## Endpoints

Prefijo `/api/v1`.

| Método | Ruta | Acceso | Para qué | Historia |
|---|---|---|---|---|
| POST | `/authentication/sign-up` | Público | Crear cuenta `PATIENT` o `PSYCHOLOGIST` e iniciar sesión | US-001 |
| POST | `/authentication/sign-in` | Público | Iniciar sesión; 5 intentos fallidos bloquean 15 min | US-001 |
| POST | `/authentication/federated-sign-in` | Público | Registro o ingreso con el ID token de Google o Apple | US-003 |
| POST | `/authentication/biometric-sign-in` | Público | Ingreso rápido con el token que libera FaceID/huella | US-015 |
| POST | `/authentication/password-reset-requests` | Público | Enviar código de 6 dígitos (no revela si el correo existe) | US-016 |
| POST | `/authentication/password-reset-requests/verification` | Público | Validar el código (vence en 10 min o tras 5 intentos) | US-016 |
| POST | `/authentication/password-resets` | Público | Definir la nueva contraseña, distinta de la anterior | US-016 |
| GET | `/accounts/me` | Token | Datos de la cuenta autenticada | — |
| POST | `/accounts/me/biometric-unlock` | Token | Habilitar biometría; devuelve una sola vez el token del dispositivo | US-015 |
| POST | `/accounts/me/deletion-request` | Token | Solicitar eliminación: cierra sesiones, revoca accesos e informa la fecha | US-033 |
| POST | `/consents` | Token (paciente) | Compartir alcance y periodo con un psicólogo | US-009 |
| GET | `/consents` | Token (paciente) | Accesos vigentes con especialista, alcance y fecha | US-023 |
| POST | `/consents/{id}/revocation` | Token (paciente) | Revocar; el acceso se retira de inmediato | US-023 |
| GET | `/consent-scopes` | Token (psicólogo) | Qué puede consultar de cada paciente y hasta cuándo | US-046 |
| GET | `/audit-events` | Token (paciente) | Quién accedió o cambió el acceso a su información | TS-007 |
| POST | `/vault` | Token | Crear la bóveda privada con un PIN de 4 a 8 dígitos | US-025 |
| POST | `/vault/entries` | Token | Mover una entrada del diario a la bóveda (pide el PIN) | US-025 |
| POST | `/vault/access` | Token | Abrir la bóveda; 5 PIN incorrectos la bloquean 15 min | US-025 |

US-045 (resumen emocional autorizado) se cubre con `IamContextFacade.authorizeConsentedAccess`, que valida
el consentimiento vigente y registra en la auditoría tanto el acceso concedido como el denegado.

## Prueba rápida en Swagger

1. `POST /authentication/sign-up` con `{"email":"maria@safeplace.com","password":"SafeSanctuary2025#","role":"PATIENT"}`.
2. Copia el `accessToken`, pulsa **Authorize** y pégalo.
3. Llama a cualquier endpoint: `GET /accounts/me`, o endpoints de otros contextos sin escribir `X-Account-Id`.

En `dev`, el código de recuperación aparece en el log si no hay SMTP configurado.

## Configuración

| Variable | Uso |
|---|---|
| `IAM_JWT_SECRET` | Firma de los tokens (mínimo 32 caracteres). Obligatoria en `prod`; en `dev` se usa una clave temporal. |
| `IAM_JWT_EXPIRATION_MINUTES` | Vigencia del token (1440 por defecto). |
| `GOOGLE_CLIENT_IDS`, `APPLE_CLIENT_IDS` | Client IDs de la app; sin ellos el ingreso federado responde 422. |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD` | SMTP para enviar el código de recuperación. |

Las cuentas `ADMIN` y `MODERATOR` no se registran desde la app; se asignan directamente en la base de datos.
