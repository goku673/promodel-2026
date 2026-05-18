/** Tipos de locación en la fábrica */
public enum LType {
    CONVEYOR, ALMACEN, MAQUINA, INSPECCION, EMPAQUE, EMBARQUE;

    public java.awt.Color color() {
        switch (this) {
            case CONVEYOR:   return new java.awt.Color(220, 220, 220);
            case ALMACEN:    return new java.awt.Color(200, 200, 200);
            case MAQUINA:    return new java.awt.Color(150, 150, 150);
            case INSPECCION: return new java.awt.Color(100, 100, 100);
            case EMPAQUE:    return new java.awt.Color(50,  50,  50);
            case EMBARQUE:   return new java.awt.Color(0,   0,   0);
            default:         return java.awt.Color.GRAY;
        }
    }

    public String icon() {
        switch (this) {
            case CONVEYOR:   return "═══";
            case ALMACEN:    return "▣";
            case MAQUINA:    return "⚙";
            case INSPECCION: return "🔍";
            case EMPAQUE:    return "📦";
            case EMBARQUE:   return "🚚";
            default:         return "■";
        }
    }
}
