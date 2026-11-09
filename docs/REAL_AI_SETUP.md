# Activar respuestas reales de Diarito

El perfil `dev` utiliza `mock` de forma predeterminada. Ese proveedor devuelve una frase fija y no utiliza un modelo de IA.

1. Crea una clave desde tu cuenta en [Google AI Studio](https://aistudio.google.com/apikey) o [Groq](https://console.groq.com/keys).
2. En PowerShell, desde la carpeta `safediary-platform`, ejecuta:

```powershell
.\scripts\Enable-RealAi.ps1
```

Para Groq:

```powershell
.\scripts\Enable-RealAi.ps1 -Provider Groq
```

El script solicita la clave con entrada oculta, consulta los modelos disponibles y comprueba una respuesta breve. Solo después guarda la configuración en `config/application.properties`, excluido de Git. Conserva las otras propiedades y copia una configuración anterior a un respaldo dentro de la misma carpeta ignorada.

Al terminar reinicia únicamente el proceso Java de SafeDiary que escucha en el puerto 8080. Si ese puerto pertenece a otro programa, no lo detiene. Los logs del arranque quedan en `target/real-ai-backend.log` y `target/real-ai-backend-error.log`.

La prueba envía al proveedor únicamente «Responde solo con la palabra LISTO.». Las llamadas posteriores de Diarito usan el proveedor seleccionado y su cuota.

Para guardar la configuración sin reiniciar:

```powershell
.\scripts\Enable-RealAi.ps1 -ConfigureOnly
```

Para comprobar la preparación sin introducir una clave ni hacer cambios:

```powershell
.\scripts\Enable-RealAi.ps1 -Check
```

La activación necesita una clave válida con cuota disponible. El script no crea cuentas, claves ni planes de facturación. Si el backend usa una clave JWT temporal, reiniciarlo invalida las sesiones anteriores y es necesario volver a iniciar sesión en la app.
