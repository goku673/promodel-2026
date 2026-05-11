/**
 * SimParams - Todos los parámetros configurables de la simulación.
 * Esta clase es un POJO con valores por defecto que el usuario puede
 * modificar desde la interfaz gráfica antes de iniciar la simulación.
 */
public class SimParams implements Cloneable {

    // ── Llegadas ──────────────────────────────────────────────────────────
    /** Frecuencia de llegada de barras (minutos entre llegadas) */
    public double arriboFrecuencia = 15.0;

    // ── CONVEYOR_1 (fijo) ─────────────────────────────────────────────────
    public double conv1Tiempo = 4.0;

    // ── ALMACEN_1 (Normal) ────────────────────────────────────────────────
    public double alm1Media  = 5.0;
    public double alm1Sigma  = 0.5;
    public int    alm1Cap    = 10;

    // ── CORTADORA (Exponencial) ───────────────────────────────────────────
    public double cortMedia  = 3.0;
    public int    cortCap    = 1;

    // ── TORNO (Normal) ────────────────────────────────────────────────────
    public double tornMedia  = 5.0;
    public double tornSigma  = 0.5;
    public int    tornCap    = 2;

    // ── CONVEYOR_2 (fijo) ─────────────────────────────────────────────────
    public double conv2Tiempo = 4.0;

    // ── FRESADORA (Exponencial) ───────────────────────────────────────────
    public double fresMedia  = 3.0;
    public int    fresCap    = 2;

    // ── ALMACEN_2 (Normal) ────────────────────────────────────────────────
    public double alm2Media  = 5.0;
    public double alm2Sigma  = 0.5;
    public int    alm2Cap    = 10;

    // ── PINTURA (Exponencial) ─────────────────────────────────────────────
    public double pintMedia  = 3.0;
    public int    pintCap    = 4;

    // ── INSPECCION_1 (Normal) ─────────────────────────────────────────────
    public double ins1Media  = 5.0;
    public double ins1Sigma  = 0.5;
    public int    ins1Cap    = 2;

    // ── INSPECCION_2 (Exponencial) ────────────────────────────────────────
    public double ins2Media  = 3.0;
    public int    ins2Cap    = 1;

    // ── EMPAQUE (Normal) ──────────────────────────────────────────────────
    public double empMedia   = 5.0;
    public double empSigma   = 0.5;
    public int    empCap     = 1;

    // ── EMBARQUE (Exponencial) ────────────────────────────────────────────
    public double embMedia   = 3.0;
    public int    embCap     = 3;

    // ── Tiempos de traslado de recursos ───────────────────────────────────
    /** TRABAJADOR_1: CORTADORA → TORNO */
    public double t1Traslado = 3.0;
    /** TRABAJADOR_2: FRESADORA → ALMACEN_2 */
    public double t2Traslado = 3.0;
    /** TRABAJADOR_3: EMPAQUE → EMBARQUE */
    public double t3Traslado = 3.0;
    /** MONTACARGAS: ALMACEN_2 → PINTURA */
    public double mkTraslado1 = 3.0;
    /** MONTACARGAS: PINTURA → INSPECCION_1 */
    public double mkTraslado2 = 3.0;

    // ── Probabilidades ────────────────────────────────────────────────────
    /** Probabilidad de que INSPECCION_1 envíe a INSPECCION_2 (defecto) */
    public double probRechazo = 0.20;

    // ── General ───────────────────────────────────────────────────────────
    /** Duración total de la simulación en minutos */
    public double duracion = 480.0;
    /** Semilla del generador aleatorio para reproducibilidad */
    public long   semilla  = 42L;

    /**
     * Crea una copia profunda de los parámetros para uso en simulación.
     */
    public SimParams copy() {
        try {
            return (SimParams) super.clone();
        } catch (CloneNotSupportedException e) {
            return new SimParams(); // fallback con valores por defecto
        }
    }

    /**
     * Retorna tiempo de servicio para la locación dada, usando el generador.
     */
    public double serviceTime(String loc, Rng rng) {
        switch (loc) {
            case "CONVEYOR_1":   return conv1Tiempo;
            case "ALMACEN_1":    return rng.norm(alm1Media, alm1Sigma);
            case "CORTADORA":    return rng.exp(cortMedia);
            case "TORNO":        return rng.norm(tornMedia, tornSigma);
            case "CONVEYOR_2":   return conv2Tiempo;
            case "FRESADORA":    return rng.exp(fresMedia);
            case "ALMACEN_2":    return rng.norm(alm2Media, alm2Sigma);
            case "PINTURA":      return rng.exp(pintMedia);
            case "INSPECCION_1": return rng.norm(ins1Media, ins1Sigma);
            case "INSPECCION_2": return rng.exp(ins2Media);
            case "EMPAQUE":      return rng.norm(empMedia, empSigma);
            case "EMBARQUE":     return rng.exp(embMedia);
            default:             return 1.0;
        }
    }
}
