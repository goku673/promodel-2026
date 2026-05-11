@echo off
cd /d "c:\Users\WIN\Desktop\promodel proyect"
echo Compilando Multi-Engrane Simulator...
"C:\Program Files\BlueJ\jdk\bin\javac.exe" -encoding UTF-8 -Xlint:none *.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo la compilacion. Revisa los errores arriba.
    pause
    exit /b 1
)
echo Compilacion exitosa. Iniciando simulador...
"C:\Program Files\BlueJ\jdk\bin\java.exe" MultiEngraneSimulator
pause
