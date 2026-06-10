# Promodel-Lite Simulator (ProModel Edition)

Un simulador de eventos discretos (DES) avanzado escrito en Java, diseñado para replicar la estética y funcionalidad de **ProModel** para un sistema de manufactura de engranes.

![Captura de Pantalla](https://i.ibb.co/ksqg1cmz/promodel-plus.jpg)

## 🚀 Características Principales

- **Interfaz ProModel Style:** Diseño industrial moderno con colores corporativos.
- **Animación en Tiempo Real:** Partículas, trabajadores (T1-T3) y montacargas (MK).
- **Reportes Exhaustivos:** Tablas resumen y gráficos de barras (Estilo ProModel).
- **Control de Velocidad:** Ajuste dinámico de hasta 50x.
- **Contador en Embarque:** Visualización acumulativa de piezas que llegan al destino final.

## 🛠️ Requisitos e Instalación

Este proyecto ha sido desarrollado utilizando **JDK 26**.

- **Versión recomendada:** JDK 26.0.1 o superior.
- **Descarga:** [Oracle Java JDK Downloads](https://www.oracle.com/java/technologies/downloads/#jdk26-windows)
- **Ruta de instalación sugerida:** `C:\Program Files\Java\jdk-26.0.1`

### Configuración del Entorno

Asegúrate de que la carpeta `bin` de tu JDK esté en las variables de entorno (PATH) de tu sistema para ejecutar los comandos directamente.

## 💻 Comandos de Consola

Abre una terminal (PowerShell o CMD) en la carpeta del proyecto y utiliza los siguientes comandos:

### 1. Compilar los archivos (.class)

Genera los archivos ejecutables a partir del código fuente:

```powershell
javac -encoding UTF-8 *.java
```

### 2. Ejecutar la aplicación

Inicia el simulador desde la consola (una vez compilado):

```powershell
java MultiEngraneSimulator
```

### 3. Generar un archivo ejecutable (.JAR)

Si deseas crear un archivo único que se pueda distribuir y ejecutar con doble clic:

**Paso A: Crear el archivo MANIFEST**
Crea un archivo llamado `manifest.txt` con el siguiente contenido:

```text
Main-Class: MultiEngraneSimulator
```

**Paso B: Empaquetar el JAR**

```powershell
jar cfm MultiEngraneSimulator.jar manifest.txt *.class
```

**Paso C: Ejecutar el JAR**

```powershell
java -jar MultiEngraneSimulator.jar
```

## ⚙️ Uso de Scripts Automáticos

Si prefieres no usar la consola manualmente, utiliza el archivo incluido:

- `ejecutar.bat`: Compila automáticamente y lanza la aplicación en un solo paso.

## 🧹 Limpieza y Git

Si deseas limpiar los archivos compilados o subir cambios a GitHub, usa estos comandos:

### Borrar archivos compilados (.class)

```powershell
Remove-Item *.class
```

### Subir cambios a GitHub

```powershell
git add .
git commit -m "Mejoras en la simulación y documentación"
git push origin dev
```

## 📁 Estructura del Proyecto

- `MultiEngraneSimulator.java`: Punto de entrada (Main).
- `MainFrame.java`: Gestión de la ventana principal.
- `FactoryPanel.java`: Motor gráfico y animaciones.
- `SimEngine.java`: Motor de eventos de simulación.
- `SimModel.java`: Datos y lógica de los recursos/locaciones.
- `ResultsDialog.java`: Generación de reportes y gráficas.

---

_Desarrollado para la optimización de procesos industriales._

```
promodel-2026
├─ BuildDialog.java
├─ ChartsDialog.java
├─ ControlPanel.java
├─ ejecutar.bat
├─ FactoryPanel.java
├─ GraphicsDialog.java
├─ LType.java
├─ MainFrame.java
├─ manifest.txt
├─ MultiEngraneSimulator.jar
├─ MultiEngraneSimulator.java
├─ MultiEngraneSimulator.zip
├─ ParamsDialog.java
├─ patch_split.ps1
├─ ProModelData.java
├─ ProModelParser.java
├─ public
│  ├─ Almacen3_png.png
│  ├─ Almacen4_png.png
│  ├─ Almacen_png.png
│  ├─ Auto_png.png
│  ├─ Barrametal_png.png
│  ├─ Caja_png.png
│  ├─ Camioneta_png.png
│  ├─ CentroTuning_png.png
│  ├─ Cortadora_png.png
│  ├─ Edificio_2_png.png
│  ├─ Edificio_png.png
│  ├─ Fila2_png.png
│  ├─ Fila_png.png
│  ├─ Horno2_png.png
│  ├─ Horno_png.png
│  ├─ InspeccionMesa_png.png
│  ├─ Inspeccion_png.png
│  ├─ Lavadora_png.png
│  ├─ Montacarga_png.png
│  ├─ Operador_png.png
│  ├─ PiezaHorneada_png.png
│  ├─ PiezaLimpia_png.png
│  ├─ PiezaPintada_png.png
│  ├─ Pieza_png.png
│  ├─ Pintadora2_png.png
│  ├─ Pintadora_png.png
│  ├─ Tornillo_png.png
│  ├─ Torno_png.png
│  ├─ trabajadora_png.png
│  └─ trabajador_png.png
├─ README.md
├─ Renderers.java
├─ ResultsDialog.java
├─ SimConstants.java
├─ SimEngine.java
├─ SimModel.java
├─ SimParams.java
├─ SimWorker.java
├─ StatsPanel.java
└─ test_modelo.txt

```
```
promodel-2026
├─ BuildDialog.java
├─ ChartsDialog.java
├─ ControlPanel.java
├─ ejecutar.bat
├─ FactoryPanel.java
├─ GraphicsDialog.java
├─ LType.java
├─ MainFrame.java
├─ manifest.txt
├─ MultiEngraneSimulator.jar
├─ MultiEngraneSimulator.java
├─ MultiEngraneSimulator.zip
├─ ParamsDialog.java
├─ patch_split.ps1
├─ ProModelData.java
├─ ProModelParser.java
├─ public
│  ├─ Almacen3_png.png
│  ├─ Almacen4_png.png
│  ├─ Almacen_png.png
│  ├─ Auto_png.png
│  ├─ Barrametal_png.png
│  ├─ Caja_png.png
│  ├─ Camioneta_png.png
│  ├─ CentroTuning_png.png
│  ├─ Cortadora_png.png
│  ├─ Edificio_2_png.png
│  ├─ Edificio_png.png
│  ├─ Fila2_png.png
│  ├─ Fila_png.png
│  ├─ Horno2_png.png
│  ├─ Horno_png.png
│  ├─ InspeccionMesa_png.png
│  ├─ Inspeccion_png.png
│  ├─ Lavadora_png.png
│  ├─ Montacarga_png.png
│  ├─ Operador_png.png
│  ├─ PiezaHorneada_png.png
│  ├─ PiezaLimpia_png.png
│  ├─ PiezaPintada_png.png
│  ├─ Pieza_png.png
│  ├─ Pintadora2_png.png
│  ├─ Pintadora_png.png
│  ├─ Tornillo_png.png
│  ├─ Torno_png.png
│  ├─ trabajadora_png.png
│  └─ trabajador_png.png
├─ README.md
├─ Renderers.java
├─ ResultsDialog.java
├─ SimConstants.java
├─ SimEngine.java
├─ SimParams.java
├─ SimState.java
├─ SimWorker.java
├─ StatsPanel.java
└─ test_modelo.txt

```