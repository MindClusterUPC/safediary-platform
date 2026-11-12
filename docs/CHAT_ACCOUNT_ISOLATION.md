# Aislamiento del chat por cuenta

El servidor obtiene la cuenta del JWT autenticado. Los endpoints de chat y de
sesiones comprueban la pertenencia antes de consultar o modificar conversaciones.
Un ID de cuenta enviado por el cliente no permite acceder al historial de otra cuenta.

Las conversaciones anteriores al cambio usaban una cuenta demo compartida. La
columna `owner_verified` se agrega con valor `false` para conservar esas filas sin
exponerlas. Las conversaciones nuevas se guardan con `true`. No se debe marcar
historial antiguo como verificado ni reasignarlo sin comprobar su propietario.
Con la configuración actual, Hibernate agrega la columna al iniciar el backend.

Android usa una base Room y preferencias de chat por cuenta. Los archivos antiguos
`safediary.db` y `diarito_prefs` se conservan, sin importarlos a una cuenta nueva
porque no contienen una atribución confiable. El diario también usa la base de la
cuenta. Cerrar sesión conserva el historial aislado para el próximo acceso de su
propietario.

Los módulos de chat y diario se reconstruyen al cambiar la sesión. Sus peticiones
quedan vinculadas al token con el que se iniciaron: se rechazan las peticiones en
espera y las respuestas tardías si la sesión cambia.

Validación: `ChatAccountIsolationIntegrationTest` usa cuentas ficticias y H2;
`AccountBoundaryTest` comprueba almacenamiento y límites de peticiones;
`AccountStorageIsolationTest` comprueba Room y preferencias en Android.

Estos cambios requieren actualizar tanto el backend como Android. Una instalación
que use un backend remoto necesita desplegar allí la actualización por separado.
