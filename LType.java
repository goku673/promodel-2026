/** Tipos de locación en la fábrica */
public enum LType {
    CONVEYOR, ALMACEN, MAQUINA, INSPECCION, EMPAQUE, EMBARQUE;

    public java.awt.Color color() {
        switch (this) {
            case CONVEYOR:   return new java.awt.Color(55,71,79);
            case ALMACEN:    return new java.awt.Color(230,160,0);
            case MAQUINA:    return new java.awt.Color(40,100,180);
            case INSPECCION: return new java.awt.Color(0,150,170);
            case EMPAQUE:    return new java.awt.Color(200,80,50);
            case EMBARQUE:   return new java.awt.Color(70,160,80);
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
