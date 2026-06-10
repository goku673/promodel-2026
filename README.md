# Promodel-Lite Simulator

Simulador de eventos discretos desarrollado en **Java**, inspirado en la interfaz y funcionamiento de **ProModel**.

El programa permite importar modelos desde archivos `.TXT`, visualizar locaciones, ejecutar la simulación, controlar la velocidad, observar el flujo del proceso y revisar resultados mediante tablas y gráficas.

---

## 📌 Descripción del proyecto

**Promodel-Lite Simulator** es un simulador académico de eventos discretos desarrollado en Java.

El programa permite representar procesos productivos o de servicios mediante:

- Locaciones.
- Entidades.
- Recursos.
- Tiempos de proceso.
- Movimientos.
- Agrupaciones.
- Resultados estadísticos.

El objetivo principal del simulador es facilitar la visualización y análisis de modelos construidos o exportados desde ProModel mediante archivos `.TXT`.

El sistema puede trabajar con distintos modelos siempre que el archivo importado tenga una estructura compatible.

---

## 🖼️ Vista general del programa

![Captura de Pantalla](https://i.ibb.co/TxsdHw8z/portada.jpg)

---

## 🚀 Características principales

- Interfaz gráfica inspirada en ProModel.
- Carga de modelos desde archivos `.TXT`.
- Visualización de locaciones del modelo.
- Representación gráfica del flujo del proceso.
- Animación del movimiento de entidades.
- Control de simulación: iniciar, pausar, reanudar, detener y resetear.
- Control de velocidad de ejecución.
- Opción para deshabilitar animación.
- Visualización del reloj de simulación.
- Tabla de resultados por locación.
- Estadísticas generales del sistema.
- Gráficas de utilización y resultados.
- Soporte para distintos modelos exportados desde ProModel.
- Ejecución desde archivos `.java`, `.bat` o `.jar`.

---

## 🛠️ Requisitos

Para ejecutar el programa se necesita tener instalado **Java JDK**.

### Versión recomendada

```text
JDK 26.0.1 o superior
```

### Descargar Java

Puedes descargar Java desde la página oficial de Oracle:

```text
https://www.oracle.com/java/technologies/downloads/
```

### Ruta sugerida de instalación en Windows

```text
C:\Program Files\Java\jdk-26.0.1
```

---

## ✅ Verificar instalación de Java

Antes de ejecutar el programa, abre **PowerShell** o **CMD** y escribe:

```powershell
java -version
```

Luego verifica el compilador:

```powershell
javac -version
```

Si ambos comandos muestran una versión de Java, el entorno está configurado correctamente.

Ejemplo esperado:

```text
java version "26.0.1"
javac 26.0.1
```

Si aparece un mensaje como:

```text
java no se reconoce como un comando interno o externo
```

significa que Java no está instalado o no está agregado al PATH del sistema.

---

## 📥 Descargar el proyecto

Puedes obtener el proyecto desde GitHub:

```text
https://github.com/goku673/promodel-2026
```

### Opción 1: Descargar como ZIP

1. Entra al repositorio:  
   `https://github.com/goku673/promodel-2026`
2. Presiona el botón **Code**.
3. Selecciona **Download ZIP**.
4. Descomprime el archivo.
5. Abre la carpeta del proyecto.

### Opción 2: Clonar con Git

Abre PowerShell o CMD y ejecuta:

```powershell
git clone https://github.com/goku673/promodel-2026
```

Luego entra a la carpeta del proyecto:

```powershell
cd promodel-2026
```

---

## ▶️ Ejecutar el programa

Existen dos formas de ejecutar el programa:

1. Ejecutarlo manualmente con comandos.
2. Ejecutarlo automáticamente con el archivo `ejecutar.bat`.

---

## Opción 1: Ejecutar con comandos

### Paso 1: Abrir la terminal en la carpeta del proyecto

Abre PowerShell o CMD en la carpeta donde están los archivos `.java`.

También puedes entrar manualmente con:

```powershell
cd "C:\Users\WIN\Desktop\promodel-2026"
```

Si tu proyecto está en otra ubicación, cambia la ruta.

---

### Paso 2: Compilar el proyecto

Ejecuta el siguiente comando:

```powershell
javac -encoding UTF-8 *.java
```

Este comando compila todos los archivos `.java` y genera archivos `.class`.

---

### Paso 3: Ejecutar el simulador

Después de compilar, ejecuta:

```powershell
java MultiEngraneSimulator
```

Después de ejecutar este comando, se abrirá la interfaz principal del simulador.

---

## Opción 2: Ejecutar con archivo BAT

El proyecto incluye el archivo:

```text
ejecutar.bat
```

Este archivo permite compilar y ejecutar el programa automáticamente.

### Pasos

1. Abre la carpeta del proyecto.
2. Haz doble clic en `ejecutar.bat`.
3. Espera a que compile.
4. Se abrirá la interfaz principal del simulador.

Si el archivo `.bat` no funciona, utiliza la ejecución manual con los comandos anteriores.

---

## 📄 Cargar un modelo TXT

El programa permite cargar archivos `.TXT` exportados desde ProModel.

### Pasos generales

1. Ejecuta el programa.
2. Busca la opción para cargar o importar modelo.
3. Selecciona el archivo `.TXT`.
4. Espera a que se carguen las locaciones, entidades, recursos y procesos.
5. Verifica que el modelo aparezca correctamente en la interfaz.
6. Presiona **Iniciar** para ejecutar la simulación.

---

## 🎮 Controles del programa

| Control | Función |
|---|---|
| Iniciar | Comienza la simulación |
| Pausar | Detiene temporalmente la simulación |
| Reanudar | Continúa la simulación pausada |
| Detener | Finaliza la simulación |
| Resetear | Reinicia el modelo desde cero |
| Velocidad | Permite acelerar o reducir la simulación |
| Deshabilitar animación | Ejecuta la simulación más rápido sin mostrar todos los movimientos |
| Ver resultados | Muestra tablas de resultados |
| Ver gráficas | Muestra gráficos de la simulación |
| SimRunner | Permite ejecutar pruebas o escenarios de simulación |

---

## 📊 Resultados que muestra el programa

El simulador muestra resultados generales y resultados por locación.

Entre los datos principales se encuentran:

- Entidades creadas.
- Entidades salientes.
- Entidades en sistema.
- Total de entidades procesadas.
- Capacidad de cada locación.
- Entidades dentro de cada locación.
- Entidades en cola.
- Cantidad procesada por locación.
- Porcentaje de utilización.
- Tiempo promedio por locación.
- Contenido promedio.
- Gráficas de comparación.

---

## 🧩 Instrucciones soportadas

El programa puede interpretar varias instrucciones comunes de modelos de simulación, entre ellas:

| Instrucción | Descripción |
|---|---|
| `WAIT` | Representa un tiempo de proceso o espera |
| `USE` | Utiliza un recurso durante un tiempo determinado |
| `MOVE FOR` | Movimiento con tiempo fijo |
| `MOVE WITH` | Movimiento utilizando un recurso |
| `THEN FREE` | Libera un recurso después de usarlo |
| `GROUP` | Agrupa entidades |
| `ACCUM` | Acumula entidades o lotes antes de continuar |
| `COMBINE` | Combina entidades para formar una nueva entidad |
| `UNGROUP` | Desagrupa entidades |
| `ER()` | Distribución Erlang |
| `U()` | Distribución uniforme |
| `T()` | Distribución triangular |

---

## 📦 Crear archivo JAR

Si deseas generar un archivo ejecutable `.jar`, sigue estos pasos.

### Paso 1: Compilar el proyecto

```powershell
javac -encoding UTF-8 *.java
```

---

### Paso 2: Crear el archivo manifest.txt

Crea un archivo llamado:

```text
manifest.txt
```

Dentro del archivo escribe:

```text
Main-Class: MultiEngraneSimulator
```

Importante: deja una línea vacía al final del archivo.

---

### Paso 3: Crear el archivo JAR

```powershell
jar cfm MultiEngraneSimulator.jar manifest.txt *.class
```

---

### Paso 4: Ejecutar el archivo JAR

```powershell
java -jar MultiEngraneSimulator.jar
```

También puedes ejecutar el `.jar` con doble clic si Java está configurado correctamente.

---

## 🧹 Limpiar archivos compilados

Cuando se compila el proyecto, Java genera archivos `.class`.

Estos archivos no son necesarios para subir el proyecto a GitHub, porque se generan nuevamente al compilar.

### Eliminar archivos `.class`

En PowerShell ejecuta:

```powershell
Get-ChildItem -Recurse -Filter *.class | Remove-Item -Force
```

### Verificar que no existan archivos `.class`

```powershell
Get-ChildItem -Recurse -Filter *.class
```

Si no aparece ningún resultado, significa que los archivos `.class` fueron eliminados correctamente.

---

## ☁️ Subir cambios a GitHub

Para subir cambios al repositorio, usa:

```powershell
git status
git add .
git commit -m "Actualizar simulador"
git push origin main
```

Si tu rama se llama `master`, usa:

```powershell
git push origin master
```

Si tu rama se llama `dev`, usa:

```powershell
git push origin dev
```

Para saber en qué rama estás:

```powershell
git branch
```

---

## 📁 Estructura del proyecto

| Archivo / Carpeta | Descripción |
|---|---|
| `MultiEngraneSimulator.java` | Archivo principal del programa |
| `MainFrame.java` | Ventana principal de la aplicación |
| `FactoryPanel.java` | Panel gráfico donde se visualiza el modelo |
| `ControlPanel.java` | Panel de botones y controles |
| `SimEngine.java` | Motor de simulación |
| `SimState.java` | Estado actual de la simulación |
| `SimWorker.java` | Control de ejecución de la simulación |
| `ProModelParser.java` | Lectura de archivos exportados desde ProModel |
| `ProModelData.java` | Estructura de datos del modelo importado |
| `ResultsDialog.java` | Ventana de resultados |
| `ChartsDialog.java` | Ventana de gráficas |
| `ParamsDialog.java` | Configuración de parámetros |
| `GraphicsDialog.java` | Configuración visual del modelo |
| `BuildDialog.java` | Construcción o edición de elementos del modelo |
| `StatsPanel.java` | Panel de estadísticas |
| `Renderers.java` | Renderizado visual de tablas y componentes |
| `SimConstants.java` | Constantes utilizadas por el simulador |
| `SimParams.java` | Parámetros generales de simulación |
| `ProjectIO.java` | Lectura y guardado de proyectos |
| `public/` | Carpeta de imágenes y recursos visuales |
| `README.md` | Documentación del proyecto |
| `ejecutar.bat` | Script para compilar y ejecutar automáticamente |
| `manifest.txt` | Archivo usado para generar el `.jar` |

---

## ✅ Archivos recomendados para subir a GitHub

Para una entrega limpia, se recomienda subir:

- Archivos `.java`.
- Carpeta `public`.
- Archivo `README.md`.
- Archivo `ejecutar.bat`.
- Archivo `manifest.txt`.
- Archivos `.txt` de prueba, si corresponde.

---

## ❌ Archivos que no son necesarios subir

No es necesario subir:

- Archivos `.class`.
- Archivos temporales.
- Archivos generados automáticamente.
- Carpetas de configuración del editor, si no son necesarias.

Los archivos `.class` se generan automáticamente al compilar con:

```powershell
javac -encoding UTF-8 *.java
```

---

## ⚠️ Errores comunes

### Error: java no se reconoce como comando

Solución:

Instala Java JDK y agrega Java al PATH del sistema.

---

### Error: javac no se reconoce como comando

Solución:

Instala el JDK completo, no solo el JRE, y revisa las variables de entorno.

---

### El programa no abre

Solución:

Primero compila el proyecto:

```powershell
javac -encoding UTF-8 *.java
```

Luego ejecuta:

```powershell
java MultiEngraneSimulator
```

---

### El archivo TXT no carga correctamente

Solución:

Verifica que el archivo haya sido exportado correctamente desde ProModel y que contenga secciones como:

- Locaciones.
- Entidades.
- Recursos.
- Procesamiento.
- Arribos.

---

### No aparecen imágenes

Solución:

Verifica que la carpeta `public` esté dentro del proyecto y que no haya sido eliminada.

---

### El programa compila, pero no ejecuta

Solución:

Revisa que el archivo principal sea:

```text
MultiEngraneSimulator.java
```

Y ejecuta:

```powershell
java MultiEngraneSimulator
```

---

### El archivo JAR no abre

Solución:

Verifica que el archivo `manifest.txt` tenga este contenido:

```text
Main-Class: MultiEngraneSimulator
```

También asegúrate de dejar una línea vacía al final del archivo `manifest.txt`.

---

## 🧪 Flujo recomendado para probar el programa

Para probar el programa desde cero:

1. Descargar o clonar el repositorio.
2. Verificar que Java esté instalado.
3. Abrir PowerShell o CMD en la carpeta del proyecto.
4. Compilar con:

```powershell
javac -encoding UTF-8 *.java
```

5. Ejecutar con:

```powershell
java MultiEngraneSimulator
```

6. Cargar un archivo `.TXT`.
7. Verificar que aparezcan locaciones y entidades.
8. Presionar **Iniciar**.
9. Observar el flujo del proceso.
10. Revisar resultados y gráficas.

---

## 📌 Recomendación para entrega

Para entregar el proyecto se recomienda incluir únicamente los archivos necesarios para compilar y ejecutar el programa.

Contenido recomendado:

```text
promodel-2026/
│
├── public/
├── BuildDialog.java
├── ChartsDialog.java
├── ControlPanel.java
├── FactoryPanel.java
├── GraphicsDialog.java
├── LType.java
├── MainFrame.java
├── MultiEngraneSimulator.java
├── ParamsDialog.java
├── ProjectIO.java
├── ProModelData.java
├── ProModelParser.java
├── Renderers.java
├── ResultsDialog.java
├── SimConstants.java
├── SimEngine.java
├── SimParams.java
├── SimState.java
├── SimWorker.java
├── StatsPanel.java
├── ejecutar.bat
├── manifest.txt
├── README.md
└── test_modelo.txt
```

No es necesario incluir archivos `.class`, ya que estos se generan automáticamente al compilar.

---

## 📚 Datos académicos

| Dato | Información |
|---|---|
| Materia | Taller de Sistemas Operativos |
| Gestión | 1/2026 |
| Docente | Ing. JOSE RICHARD AYOROA CARDOZO |
| Tipo de proyecto | Simulador académico desarrollado en Java |

---

## 👤 Autor

Proyecto desarrollado como simulador académico de eventos discretos en Java, con interfaz inspirada en ProModel para el análisis de procesos productivos.

---

## 📝 Nota final

Este simulador fue desarrollado con fines académicos.

Su propósito principal es apoyar el aprendizaje de simulación de sistemas, permitiendo visualizar procesos, ejecutar modelos y analizar resultados de forma gráfica.
