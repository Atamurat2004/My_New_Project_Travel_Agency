@echo off
echo ========================================
echo    Travel Agency Support System
echo ========================================
echo.

echo Starting Spring Boot application...
echo.

cd /d "%~dp0"
mvn spring-boot:run

echo.
echo Application started successfully!
echo Open http://localhost:8080 in your browser
echo For support system: http://localhost:8080/support.html
echo.
pause 