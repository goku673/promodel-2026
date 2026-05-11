import javax.swing.SwingWorker;
import java.util.List;

/**
 * SimWorker - SwingWorker que ejecuta el motor DES en un hilo de fondo.
 *
 * Publica snapshots del reloj de simulación al EDT (Event Dispatch Thread)
 * para que la GUI se actualice sin bloquearse.
 * El intervalo de pausa entre pasos controla la velocidad visual.
 */
public class SimWorker extends SwingWorker<Void, Double> {

    private final SimState  state;
    private final SimEngine engine;
    private final Runnable  onTick;     // llamado en EDT cada tick
    private final Runnable  onFinished; // llamado en EDT al terminar

    /** Paso de simulación por iteración (minutos simulados) */
    private static final double SIM_STEP = 0.5;

    /** Tiempo real base entre pasos a velocidad x1 (milisegundos) */
    private static final long BASE_SLEEP_MS = 50L;

    public SimWorker(SimState state, SimEngine engine,
                     Runnable onTick, Runnable onFinished) {
        this.state      = state;
        this.engine     = engine;
        this.onTick     = onTick;
        this.onFinished = onFinished;
    }

    @Override
    protected Void doInBackground() throws Exception {
        engine.init();

        while (state.running && !isCancelled()) {
            // ── Pausa ─────────────────────────────────────────────────────
            while (state.paused && state.running) {
                Thread.sleep(100);
            }
            if (!state.running || isCancelled()) break;

            // ── Avanzar reloj un paso ──────────────────────────────────
            double until = state.clk + SIM_STEP;
            boolean ok = engine.stepUntil(until);

            // ── Publicar snapshot al EDT ───────────────────────────────
            publish(state.clk);

            if (!ok) break;

            // ── Dormir según velocidad seleccionada ────────────────────
            long sleepMs = (long)(BASE_SLEEP_MS / Math.max(0.1, state.speedMult));
            if (sleepMs > 0) Thread.sleep(sleepMs);
        }

        state.running  = false;
        state.finished = true;
        return null;
    }

    @Override
    protected void process(List<Double> chunks) {
        // Llamado en el EDT con los snapshots publicados
        if (onTick != null) onTick.run();
    }

    @Override
    protected void done() {
        // Llamado en el EDT cuando el worker termina
        if (onFinished != null) onFinished.run();
    }
}
