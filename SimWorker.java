import javax.swing.SwingWorker;
import java.util.List;

/**
 * SimWorker - SwingWorker que ejecuta el motor DES en un hilo de fondo.
 *
 * CORRECCIÓN CRÍTICA: 'simUntil' avanza siempre de forma independiente.
 * Antes: "until = state.clk + SIM_STEP" causaba un loop infinito cuando
 * no había eventos en el rango (state.clk no avanzaba y until era siempre el mismo).
 */
public class SimWorker extends SwingWorker<Void, Double> {

    private final SimState  state;
    private final SimEngine engine;
    private final Runnable  onTick;
    private final Runnable  onFinished;

    /** Minutos de simulacion que se procesan por cada frame visual */
    private static final double SIM_STEP = 2.0;

    /** Millisegundos reales entre frames a velocidad x1 */
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
        try {
            engine.init();

            // CLAVE: simUntil avanza siempre, independiente de state.clk
            // state.clk solo salta al tiempo del proximo evento procesado
            double simUntil = 0.0;

            while (state.running && !isCancelled()) {

                // Esperar si está en pausa
                while (state.paused && state.running) {
                    Thread.sleep(80);
                }
                if (!state.running || isCancelled()) break;

                // Avanzar la ventana de tiempo simulado
                simUntil += SIM_STEP;

                // Procesar todos los eventos hasta simUntil
                boolean ok = engine.stepUntil(simUntil);

                // Notificar al EDT para actualizar la GUI
                publish(state.clk);

                if (!ok) break;

                // Dormir según velocidad elegida por el usuario
                long sleepMs = (long)(BASE_SLEEP_MS / Math.max(0.05, state.speedMult));
                if (sleepMs > 0) Thread.sleep(sleepMs);
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            System.err.println("[SimWorker ERROR] " + ex.getClass().getSimpleName()
                               + ": " + ex.getMessage());
            ex.printStackTrace();
        }

        state.running  = false;
        state.finished = true;
        return null;
    }

    @Override
    protected void process(List<Double> chunks) {
        // Este metodo se ejecuta en el EDT (hilo de la GUI)
        if (onTick != null) onTick.run();
    }

    @Override
    protected void done() {
        // Llamado en el EDT cuando el worker termina o es cancelado
        if (onFinished != null) onFinished.run();
    }
}
