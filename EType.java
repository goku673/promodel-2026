/** Tipos de entidad que fluyen por el sistema */
public enum EType {
    BARRA, PIEZA_CORTADA, PIEZA_TORNEADA, PIEZA_FRESADA, PIEZA_PINTADA, PIEZA_FINAL;

    public java.awt.Color color() {
        switch (this) {
            case BARRA:          return new java.awt.Color(160,160,160);
            case PIEZA_CORTADA:  return new java.awt.Color(79,195,247);
            case PIEZA_TORNEADA: return new java.awt.Color(41,182,246);
            case PIEZA_FRESADA:  return new java.awt.Color(255,112,67);
            case PIEZA_PINTADA:  return new java.awt.Color(171,71,188);
            case PIEZA_FINAL:    return new java.awt.Color(102,187,106);
            default:             return java.awt.Color.WHITE;
        }
    }

    public String label() {
        switch (this) {
            case BARRA:          return "BARRA";
            case PIEZA_CORTADA:  return "CORTADA";
            case PIEZA_TORNEADA: return "TORNEADA";
            case PIEZA_FRESADA:  return "FRESADA";
            case PIEZA_PINTADA:  return "PINTADA";
            case PIEZA_FINAL:    return "FINAL";
            default:             return "PIEZA";
        }
    }
}
