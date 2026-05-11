@echo off
setlocal
echo ===========================================
echo   Promodel-Lite Simulator (ProModel Style)
echo ===========================================
echo.
echo Compilando archivos fuente...
javac -encoding UTF-8 -Xlint:none *.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Fallo la compilacion. 
    echo Asegurate de tener el JDK de Java instalado y configurado en el PATH.
    pause
    exit /b 1
)
echo.
echo Compilacion exitosa. Iniciando simulador...
start "" java MultiEngraneSimulator
echo.
echo [INFO] Simulador iniciado en una nueva ventana.
exit /b 0
