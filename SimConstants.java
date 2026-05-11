import java.awt.Color;
import java.awt.Font;


/**
 * SimConstants - Constantes globales, colores y enumeraciones del simulador.
 * Todas las clases del proyecto comparten estas definiciones.
 */
public class SimConstants {

    // ── Colores del tema oscuro ────────────────────────────────────────────
    public static final Color BG_DARK    = new Color(10,  10,  30);
    public static final Color BG_PANEL   = new Color(20,  20,  45);
    public static final Color BG_CARD    = new Color(22,  33,  62);
    public static final Color BG_HEADER  = new Color(15,  15,  40);
    public static final Color C_ACCENT   = new Color(233, 69,  96);
    public static final Color C_ACCENT2  = new Color(79,  195, 247);
    public static final Color C_TEXT     = new Color(224, 224, 224);
    public static final Color C_MUTED    = new Color(130, 130, 150);
    public static final Color C_SUCCESS  = new Color(102, 187, 106);
    public static final Color C_WARNING  = new Color(255, 179, 0);
    public static final Color C_BORDER   = new Color(50,  50,  80);

    // ── Colores por tipo de entidad ───────────────────────────────────────
    public static final Color COL_BARRA         = new Color(160, 160, 160);
    public static final Color COL_PIEZA_CORTADA = new Color(79,  195, 247);
    public static final Color COL_TORNEADA      = new Color(41,  182, 246);
    public static final Color COL_FRESADA       = new Color(255, 112, 67);
    public static final Color COL_PINTADA       = new Color(171, 71,  188);
    public static final Color COL_FINAL         = new Color(102, 187, 106);

    // ── Colores por tipo de locación ──────────────────────────────────────
    public static final Color COL_CONVEYOR   = new Color(55,  71,  79);
    public static final Color COL_ALMACEN    = new Color(230, 160, 0);
    public static final Color COL_MAQUINA    = new Color(40,  100, 180);
    public static final Color COL_INSPECCION = new Color(0,   150, 170);
    public static final Color COL_EMPAQUE    = new Color(200, 80,  50);
    public static final Color COL_EMBARQUE   = new Color(70,  160, 80);

    // ── Fuentes ───────────────────────────────────────────────────────────
    public static final Font FONT_TITLE  = new Font("Arial", Font.BOLD,  15);
    public static final Font FONT_LABEL  = new Font("Arial", Font.BOLD,  11);
    public static final Font FONT_SMALL  = new Font("Arial", Font.PLAIN, 10);
    public static final Font FONT_COUNT  = new Font("Arial", Font.BOLD,  13);
    public static final Font FONT_MONO   = new Font("Monospaced", Font.BOLD, 12);
}
