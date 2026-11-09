# Rutinas por cuenta

Las rutinas pertenecen al ID de cuenta IAM autenticada, el mismo que utiliza
Android al consultar `/api/v1/daily-routines/patient/{patientId}`. Este ID no es
el ID del perfil de Profiles. El servidor comprueba la pertenencia en lectura,
edicion, recordatorios, activacion, finalizacion y eliminacion.

Crear y editar conservan titulo, descripcion, hora HH:mm, duracion de 1 a 60
minutos, dias de la semana, tipo de ejercicio y preferencia de recordatorio.
Los clientes anteriores pueden omitir los nuevos campos: las actualizaciones
parciales conservan los detalles que ya estaban guardados. Las filas antiguas
usan valores predeterminados para datos que no existian en el modelo.

- `DELETE /api/v1/daily-routines/{id}` elimina una rutina propia.
- `POST /api/v1/daily-routines/{id}/completion` registra la fecha actual de Lima
  en `completedOn`. Repetirlo el mismo dia es idempotente y no desactiva la rutina.
- Completar una rutina no equivale a cambiar `isActive`.

Android ya no introduce rutinas de ejemplo como si pertenecieran a cada cuenta.
Una respuesta vacia muestra el estado vacio; un fallo muestra un error. Crear,
editar o eliminar se refleja como guardado solo despues de confirmar el servidor.
La base local se actualiza con las columnas nuevas al iniciar el backend mediante
la configuracion Hibernate existente (`ddl-auto=update`).

Validacion: `RoutineAccountIsolationIntegrationTest`, pruebas de los servicios
de rutinas, `RoutineManagementTest` y `PatientUiRegressionTest`.

El selector de animo de Inicio sigue siendo un estado temporal de pantalla;
este cambio no crea un historial emocional ni lo comparte con especialistas.
