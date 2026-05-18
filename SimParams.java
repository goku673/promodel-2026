/**
 * SimParams - Todos los parámetros configurables de la simulación.
 * Esta clase es un POJO con valores por defecto que el usuario puede
 * modificar desde la interfaz gráfica antes de iniciar la simulación.
 */
public class SimParams implements Cloneable {

    // ── General ───────────────────────────────────────────────────────────
    /** Duracion total de la simulacion en MINUTOS */
    public double duracion = 0.0;
    public String tiempoSimul = "";
    
    /** Semilla del generador aleatorio para reproducibilidad */
    public long   semilla  = 42L;

    // Nuevos campos solicitados
    public String precisionReloj = "Minuto";
    public String replicas = "";

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
}
