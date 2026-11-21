# Interactive Online Compiler backend

The browser connects only to `ws://localhost:8080/api/v1/execute`. For each browser socket, the backend creates an isolated outbound Piston socket and translates Piston's interactive protocol into the browser protocol.

## Run

1. Run Piston privately (do not publish port 2004 on a public interface). Configure its hard execution limits to match this application, for example `PISTON_COMPILE_TIMEOUT=10000`, `PISTON_RUN_TIMEOUT=600000`, `PISTON_COMPILE_CPU_TIME=10000`, and `PISTON_RUN_CPU_TIME=10000`. Piston enforces CPU limits at its container/job boundary; the browser and Spring timers cannot do that.
2. From `backend`, run `mvn spring-boot:run` (or use the Maven wrapper). On this Windows workspace use `-Dmaven.repo.local=C:\temp\compiler-m2` if Maven cannot handle the space in the workspace path.
3. Open `http://localhost:8080`. The bundled frontend is unchanged visually and uses the backend WebSocket endpoint.

## Browser protocol

`START` requires `language`, `version`, `filename`, and `code`. `INPUT` and `CANCEL` optionally include the execution id; when supplied it must belong to the current socket. Responses are `STATUS`, `OUTPUT`, `ERROR`, `EXIT`, and `TIMEOUT`. `OUTPUT.stream` is `stdout` or `stderr`.

The Piston side uses its documented `/api/v2/connect` messages: `init`, `data` with `stdin`, and `signal`; it consumes `runtime`, `stage`, `data`, and `exit`. There is no Piston event that reliably means “the program is waiting for stdin.” The backend enables input at the `run` stage and its five-minute timer measures absent meaningful input/output activity, including silent computation, rather than claiming to detect a stdin wait.

## Verification

`mvn test` runs the application context plus WebSocket integration tests against an in-process Piston-protocol mock. Coverage includes normal execution, compile and runtime errors, single and sequential stdin, cancellation, inactivity timeout, frontend disconnect, concurrent sessions, and cleanup. It does not prove a local Docker Piston installation; after starting Piston, manually run the Java scanner example in the UI, send two lines, try cancellation, and repeat from two browser windows.

## Compiler module (JDoodle WebSocket)

The compiler module is integrated into this existing Spring Boot application. It uses the same application port (`server.port`, currently 8080) and exposes the WebSocket endpoint configured by `compiler.websocket-path` (default `/ws/execute`). It is not a second Spring Boot application.

### Required environment variables

Set these in your IDE Run Configuration or operating-system environment before starting the backend:

- `DB_HOST`, `DB_PORT`, `DB_NAME`, and `DB_USERNAME` — MySQL connection details
- `DB_PASSWORD` — MySQL password
- `JWT_SECRET` — JWT signing secret
- `JDOODLE_ACCOUNT_1_CLIENT_ID` and `JDOODLE_ACCOUNT_1_CLIENT_SECRET` — JDoodle API credentials
- `COMPILER_ALLOWED_ORIGINS` — comma-separated frontend origins allowed by REST and WebSocket endpoints
- `GEMINI_API_KEY`, `GROQ_API_KEY`, `OPENROUTER_API_KEY`, `SAMBANOVA_API_KEY` — AI provider API keys
- `CLOUDFLARE_API_KEY` and `CLOUDFLARE_ACCOUNT_ID` — Cloudflare Workers AI credentials

For local development, these values can be kept in `backend/.env`. The file is
gitignored and Spring loads it automatically when the backend is started from
the repository root or the `backend` directory. Do not add real secrets to
`ENVIRONMENT-VARIABLES.example.txt`.

Optional account rotation variables:

- `JDOODLE_ACCOUNT_2_CLIENT_ID`, `JDOODLE_ACCOUNT_2_CLIENT_SECRET`, `JDOODLE_ACCOUNT_2_ENABLED`
- `JDOODLE_ACCOUNT_3_CLIENT_ID`, `JDOODLE_ACCOUNT_3_CLIENT_SECRET`, `JDOODLE_ACCOUNT_3_ENABLED`
- `JDOODLE_ACCOUNT_4_CLIENT_ID`, `JDOODLE_ACCOUNT_4_CLIENT_SECRET`, `JDOODLE_ACCOUNT_4_ENABLED`

### Deploy to Render

The repository-root `render.yaml` defines only the backend Web Service and builds it
from `backend/Dockerfile`. The service needs a reachable external MySQL database;
this Blueprint does not create one. Set the database variables, `JWT_SECRET`,
JDoodle account 1 credentials, and `COMPILER_ALLOWED_ORIGINS` when Render prompts
for Blueprint environment values. Set `COMPILER_ALLOWED_ORIGINS` to the deployed
frontend's origin (scheme and hostname, without a trailing slash). Render supplies
`PORT` automatically, and the backend uses it.

After deployment, check `https://<your-service>.onrender.com/health`. Update the
frontend's API and WebSocket URLs to the deployed backend; use `https://` for HTTP
requests and `wss://` for WebSocket connections.

Optional language version overrides: `JDOODLE_JAVA_VERSION_INDEX`, `JDOODLE_PYTHON_VERSION_INDEX`, `JDOODLE_C_VERSION_INDEX`, `JDOODLE_CPP_VERSION_INDEX`. The default configured index is `0`; check JDoodle's language/version reference if you want a different runtime.

### Run on Windows PowerShell

```powershell
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "your_long_random_jwt_secret"
$env:JDOODLE_ACCOUNT_1_CLIENT_ID = "your_jdoodle_client_id"
$env:JDOODLE_ACCOUNT_1_CLIENT_SECRET = "your_jdoodle_client_secret"
.\mvnw.cmd spring-boot:run
```

The compiler socket is `ws://localhost:8080/ws/execute`. Keep the JDoodle client secret only on the backend. Do not commit `.env`, credentials, or real secrets.

The compiler module's local account-credit counter is an in-memory safety estimate, not JDoodle's authoritative usage meter; it resets when the application restarts. JDoodle may reject a request if the provider-side daily allowance is exhausted.
