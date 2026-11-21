@echo off
setlocal
cd /d "%~dp0"
where node >nul 2>nul || (echo Node.js is not installed. Install Node.js 20.19+ or 22.12+ first.& pause & exit /b 1)
where npm >nul 2>nul || (echo npm was not found. Reinstall Node.js with npm included.& pause & exit /b 1)
if not exist package.json (echo package.json is missing from this folder.& pause & exit /b 1)
if not exist node_modules (echo Installing frontend dependencies...& call npm install || (echo Dependency installation failed.& pause & exit /b 1))
call npm run dev
pause
