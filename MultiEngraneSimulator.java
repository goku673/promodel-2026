import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║         PROMODEL-LITE SIMULATOR — Simulación de Fabricación             ║
 * ║                                                                          ║
 * ║  Motor: Simulación de Eventos Discretos (DES) con FEL                   ║
 * ║  GUI:   Java Swing con tema oscuro y animación en tiempo real            ║
 * ║                                                                          ║
 * ║  COMPILAR:                                                               ║
 * ║    javac *.java                                                          ║
 * ║                                                                          ║
 * ║  EJECUTAR:                                                               ║
 * ║    java MultiEngraneSimulator                                            ║
 * ║                                                                          ║
 * ║  Proceso simulado:                                                       ║
 * ║    BARRA(15min) → CONVEYOR_1(4) → ALMACEN_1(N5,0.5) → CORTADORA(E3)   ║
 * ║    → TORNO(N5,0.5) ×2 piezas → CONVEYOR_2(4) → FRESADORA(E3)          ║
 * ║    → ALMACEN_2(N5,0.5) → PINTURA(E3) → INSPECCION_1(N5,0.5)           ║
 * ║    → 80% EMPAQUE(N5,0.5) / 20% INSPECCION_2(E3)→EMPAQUE               ║
 * ║    → EMBARQUE(E3) → EXIT                                                ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public class MultiEngraneSimulator {

    public static void main(String[] args) {
        // Aplicar tema oscuro base del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            // Personalizar colores globales de Swing
            UIManager.put("Panel.background",          new java.awt.Color(255, 255, 255));
            UIManager.put("Label.foreground",          new java.awt.Color(0, 0, 0));
            UIManager.put("TabbedPane.background",     new java.awt.Color(240, 240, 240));
            UIManager.put("TabbedPane.foreground",     new java.awt.Color(0, 0, 0));
            UIManager.put("TabbedPane.selected",       new java.awt.Color(200, 200, 200));
            UIManager.put("TabbedPane.contentAreaColor",new java.awt.Color(255, 255, 255));
            UIManager.put("ScrollPane.background",     new java.awt.Color(255, 255, 255));
            UIManager.put("ScrollBar.background",      new java.awt.Color(240, 240, 240));
            UIManager.put("ScrollBar.thumb",           new java.awt.Color(150, 150, 150));
            UIManager.put("OptionPane.background",     new java.awt.Color(255, 255, 255));
            UIManager.put("OptionPane.messageForeground", new java.awt.Color(0, 0, 0));
        } catch (Exception ignored) { /* continuar con L&F por defecto */ }

        // Lanzar GUI en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
