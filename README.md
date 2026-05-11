# Multi-Engrane Simulator (ProModel Edition)

Un simulador de eventos discretos (DES) avanzado escrito en Java, diseñado para replicar la estética y funcionalidad de **ProModel** para un sistema de manufactura de engranes.

![Captura de Pantalla](https://via.placeholder.com/1000x500/1e1e2e/ffffff?text=Multi-Engrane+Simulator+ProModel+Style)

## 🚀 Características Principales

- **Interfaz ProModel Style:** Diseño industrial moderno con colores corporativos y tipografía optimizada.
- **Animación en Tiempo Real:** 
    - Movimiento de partículas entre locaciones.
    - Personitas animadas para trabajadores (T1, T2, T3).
    - Montacargas animado (MK) con rutas dinámicas.
    - Engranes rotatorios funcionales.
    - Barras de capacidad dinámicas (Verde/Amarillo/Rojo).
- **Reportes Exhaustivos:** 
    - 3 Tablas resumen (Entidad, Locación, Recurso).
    - 3 Gráficos de barras con gradientes (Total Exits, % Utilización).
- **Control Total:** Velocidad ajustable (hasta 50x), pausa, detención y reinicio.
- **Contadores en Pantalla:** Monitorización de llegadas, piezas finales, entidades en sistema y contador acumulado en Embarque.

## 🛠️ Requisitos

- **Java JDK 17 o superior** (Se recomienda JDK 21+).
- **Variables de entorno:** Asegúrate de tener `java` y `javac` configurados en tu PATH.

## ⚙️ Instalación y Uso

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/goku673/promodel-2026.git
   cd promodel-proyect
   ```

2. **Ejecutar mediante script (Recomendado):**
   Haz doble clic en `ejecutar.bat` (solo Windows).

3. **Compilar y Ejecutar manualmente:**
   ```powershell
   javac -encoding UTF-8 *.java
   java MultiEngraneSimulator
   ```

## 📁 Estructura del Proyecto

- `MultiEngraneSimulator.java`: Punto de entrada de la aplicación.
- `MainFrame.java`: Ventana principal y gestión de paneles.
- `FactoryPanel.java`: Motor de renderizado visual y animaciones.
- `SimEngine.java`: Lógica de simulación y despacho de eventos.
- `SimModel.java`: Definición de estructuras de datos (Loc, Res, Entity).
- `ResultsDialog.java`: Ventana de reportes y gráficas finales.
- `ParamsDialog.java`: Configuración de parámetros de simulación.
- `SimConstants.java`: Paleta de colores y estilos globales.

## 📈 Configuración de la Simulación

Puedes ajustar los siguientes parámetros en el menú **Parámetros**:
- Duración total de la simulación.
- Frecuencia de arribos (Exponencial).
- Tiempos de servicio por máquina.
- Tiempos de traslado para trabajadores y montacargas.
- Probabilidades de rechazo en inspección.

## ✒️ Créditos
Desarrollado para la optimización de procesos industriales utilizando técnicas de simulación avanzada.

---
*Nota: Este software es una herramienta educativa inspirada en el software ProModel.*