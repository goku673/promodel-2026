import java.awt.Color;
import java.awt.Font;


/**
 * SimConstants - Constantes globales, colores y enumeraciones del simulador.
 * Todas las clases del proyecto comparten estas definiciones.
 */
public class SimConstants {

    // ── Colores del tema claro/monocromo ────────────────────────────────────────────
    public static final Color BG_DARK    = new Color(255, 255, 255); // Fondo blanco
    public static final Color BG_PANEL   = new Color(240, 240, 240); // Fondo panel plomo claro
    public static final Color BG_CARD    = new Color(220, 220, 220); // Plomo
    public static final Color BG_HEADER  = new Color(200, 200, 200); // Plomo
    public static final Color C_ACCENT   = new Color(0, 0, 0); // Negro
    public static final Color C_ACCENT2  = new Color(50, 50, 50); // Negro suave
    public static final Color C_TEXT     = new Color(0, 0, 0); // Texto negro
    public static final Color C_MUTED    = new Color(100, 100, 100); // Plomo oscuro
    public static final Color C_SUCCESS  = new Color(150, 150, 150); // Plomo
    public static final Color C_WARNING  = new Color(100, 100, 100); // Plomo oscuro
    public static final Color C_BORDER   = new Color(150, 150, 150); // Plomo medio

    // ── Colores por tipo de entidad ───────────────────────────────────────
    public static final Color COL_BARRA         = new Color(200, 200, 200);
    public static final Color COL_PIEZA_CORTADA = new Color(170, 170, 170);
    public static final Color COL_TORNEADA      = new Color(140, 140, 140);
    public static final Color COL_FRESADA       = new Color(110, 110, 110);
    public static final Color COL_PINTADA       = new Color(80,  80,  80);
    public static final Color COL_FINAL         = new Color(0,   0,   0);

    // ── Colores por tipo de locación ──────────────────────────────────────
    public static final Color COL_CONVEYOR   = new Color(220, 220, 220);
    public static final Color COL_ALMACEN    = new Color(200, 200, 200);
    public static final Color COL_MAQUINA    = new Color(150, 150, 150);
    public static final Color COL_INSPECCION = new Color(100, 100, 100);
    public static final Color COL_EMPAQUE    = new Color(50,  50,  50);
    public static final Color COL_EMBARQUE   = new Color(0,   0,   0);

    // ── Fuentes ───────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Arial", Font.BOLD,  15);
    public static final Font FONT_LABEL  = new Font("Arial", Font.BOLD,  11);
    public static final Font FONT_SMALL  = new Font("Arial", Font.PLAIN, 10);
    public static final Font FONT_COUNT  = new Font("Arial", Font.BOLD,  13);
    public static final Font FONT_MONO   = new Font("Monospaced", Font.BOLD, 12);
}
