# Entornos: development y production

El backend usa **perfiles de Spring**:

| Perfil | Dónde | Cómo se activa | Archivo |
|---|---|---|---|
| `dev` | tu PC | automático (`spring.profiles.default=dev`) | `src/main/resources/application-dev.properties` |
| `prod` | Render | `SPRING_PROFILES_ACTIVE=prod` (ya está en `render.yaml`) | `src/main/resources/application-prod.properties` |

La configuración común está en `application.properties`. Lo que cambia según el entorno va en el archivo de cada perfil.

## Development (tu PC)

No hace falta configurar nada para la base de datos: usa PostgreSQL local (`localhost:5432/safediary_db`, usuario `postgres`).

Las **keys de IA** van en `config/application.properties`. Ese archivo está en `.gitignore` y nunca se sube. Créalo así:

```properties
spring.ai.google.genai.api-key=TU_KEY_DE_GEMINI
assistantai.llm.backup.api-key=TU_KEY_DE_GROQ
```

Basta con una sola de las dos keys. Si no tienes ninguna, arranca con IA simulada usando la variable `ASSISTANTAI_LLM_PROVIDER=mock`.

## Production (Render)

Todo sale de variables de entorno. El perfil `prod` **no tiene valores por defecto**: si falta una variable, la app no arranca (por ejemplo, sin `DB_PASSWORD` falla la conexión a la base). Revisa que estén todas las de la tabla.

| Variable | Obligatoria | Origen | Ejemplo / valor |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | sí | `render.yaml` | `prod` |
| `PORT` | sí | Render la pone sola | — |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | sí | Render te las pide (datos de filess.io) | — |
| `DB_POOL_SIZE` | no | `render.yaml` | `3` (conexiones máximas a la base) |
| `GEMINI_API_KEY` | una de las dos keys | Render te la pide (secreta) | — |
| `LLM_BACKUP_API_KEY` | una de las dos keys | Render te la pide (secreta) | key de Groq |
| `GEMINI_MODEL` | no | `render.yaml` | `gemini-3.5-flash-lite` |
| `LLM_FALLBACK_MODEL` | no | `render.yaml` | vacío (no repite el mismo modelo) |
| `LLM_BACKUP_MODEL` | no | por defecto | `qwen/qwen3.8-27b` |
| `ASSISTANTAI_LLM_PROVIDER` | no | por defecto | `spring-ai` (`mock` para pruebas) |

## Variables de ambos entornos

Estas variables funcionan igual en `dev` y en `prod`. Sirven para probar cosas sin tocar archivos:

- `GEMINI_MODEL`, `LLM_FALLBACK_MODEL`: el modelo de Gemini y su respaldo.
- `LLM_BACKUP_BASE_URL`, `LLM_BACKUP_MODEL`: otro proveedor compatible con OpenAI. La URL debe terminar en `/v1`.
- `SPRING_AI_CHAT_MODEL`: `google-genai`, `openai` o `none`.

## Probar el perfil prod en tu PC

```bash
SPRING_PROFILES_ACTIVE=prod DB_HOST=localhost DB_PORT=5432 DB_NAME=safediary_db DB_USER=postgres DB_PASSWORD=... ./mvnw spring-boot:run
```

## App móvil

La app ya separa entornos con sus *build types*:

| Build | Backend |
|---|---|
| `debug` | `http://10.0.2.2:8080/` (emulador → tu PC). En un celular físico: `-Psafediary.apiBaseUrl=http://<ip>:8080/` |
| `release` | `https://safediary-platform.onrender.com/`. Para otra URL: `-Psafediary.releaseApiBaseUrl=https://...` |
