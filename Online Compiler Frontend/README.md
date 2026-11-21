# TAVQEN Online Compiler Frontend

This archive is packaged so that `package.json` is in the extracted project root.

## Windows quick start

1. Extract `TAVQEN-Frontend-Final.zip`.
2. Open PowerShell or CMD **in the folder that directly contains `package.json`**.
3. Check that `package.json` is visible with `dir package.json`.
4. Run:

```powershell
npm install
npm run dev
```

5. Open the Local URL printed by Vite (normally `http://localhost:5173`).

You can also double-click `start.bat` after extracting; it checks Node/npm, installs dependencies if needed, and starts Vite.

## Required software

- Node.js 20.19+ or 22.12+
- npm (included with Node.js)
- TAVQEN Spring Boot backend running on `http://localhost:8080`

## Scripts

- `npm run dev` — start Vite development server
- `npm run build` — production build
- `npm run preview` — preview production build
- `npm test` — run frontend unit tests

## Backend integration

The Vite development server proxies `/api` and `/ws/execute` to the Spring Boot backend on port 8080. The browser uses the same-origin Vite proxy during local development.

The language catalog is loaded from `GET /api/v1/languages`. Code execution uses the `/ws/execute` WebSocket endpoint.
