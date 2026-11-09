# Psicologos ficticios para probar el flujo local

Backend: `http://localhost:8080`, perfil `dev`. Android debug consulta ese backend
mediante `http://10.0.2.2:8080` desde el emulador.

Todos los perfiles llevan `Demo` en el nombre y una biografia que indica que son
ficticios. Su aprobacion simula la verificacion solo para pruebas locales; no
representa una acreditacion profesional real. No tienen resenas inventadas.

| Nombre | Especialidades | Correo | Precio por 50 minutos |
| --- | --- | --- | --- |
| Demo - Ana Torres | Ansiedad, Estres | ana.torres.demo@example.test | S/ 80 |
| Demo - Diego Ruiz | Pareja, Comunicacion | diego.ruiz.demo@example.test | S/ 100 |
| Demo - Lucia Mendoza | Adolescentes, Familia | lucia.mendoza.demo@example.test | S/ 120 |
| Demo - Carlos Vega | Autoestima, Duelo | carlos.vega.demo@example.test | S/ 90 |

Clave de las cuatro cuentas ficticias: `SafeDiaryDemo2026!`. Rol IAM:
`PSYCHOLOGIST`. Estas credenciales son publicas para desarrollo y no se deben
usar para cuentas reales ni desplegar en produccion.

Disponibilidad de lunes a sabado, hora de Lima:

- Ana: 09:00-12:00.
- Diego: 14:00-17:00.
- Lucia: 10:00-13:00.
- Carlos: 16:00-19:00.

Para comenzar, entrar con una cuenta de paciente, abrir Psychologist, buscar
`Demo`, filtrar por especialidad o precio y abrir un perfil para solicitar
contacto. El backend permite al psicologo responder la solicitud, coordinar por
chat y proponer un horario. La aceptacion del paciente reserva el horario y el
entorno de desarrollo dispone de simulacion de pago.

La creacion de estos datos no completa pantallas que aun falten en el flujo
profesional. Tampoco envia mensajes, crea citas ni realiza pagos por el usuario.

Para recrear datos o reutilizarlos sin duplicar cuentas y perfiles:

```powershell
.\scripts\Seed-LocalClinicians.ps1
```

El script utiliza los endpoints actuales, admite solo una URL local, simula la
revision con el mecanismo de identidad administrativa ya existente en `dev` y
comprueba que cada perfil quede publicado con horarios. Al repetirlo reemplaza
unicamente la disponibilidad semanal de estas cuatro cuentas ficticias.
