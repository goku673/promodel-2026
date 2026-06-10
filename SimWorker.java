import javax.swing.SwingWorker;
import java.util.List;

/**
 * SimWorker
 *
 * Ejecuta el motor de simulación en segundo plano.
 *
 * Este worker se encarga de:
 * - Inicializar el motor.
 * - Avanzar el reloj de simulación poco a poco.
 * - Refrescar la interfaz visual.
 * - Respetar pausa, detener y velocidad.
 *
 * Corrección importante:
 * SIM_STEP es pequeño para que las entidades y recursos se vean deslizándose.
 */
public class SimWorker extends SwingWorker<Void, Double> {

    private final SimState state;
    private final SimEngine engine;
    private final Runnable onTick;
    private final Runnable onFinished;

    /**
     * Minutos simulados que se procesan por frame visual.
     *
     * Antes estaba en 0.2.
     * Eso hacía que los movimientos cortos parecieran saltos.
     *
     * Con 0.03 se generan más pasos visuales y se nota el desplazamiento.
     */
    private static final double SIM_STEP = 0.03;

    /**
     * Milisegundos reales entre frames cuando la velocidad es x1.
     */
    private static final long BASE_SLEEP_MS = 50L;

    public SimWorker(
            SimState state,
            SimEngine engine,
            Runnable onTick,
            Runnable onFinished
    ) {
        this.state = state;
        this.engine = engine;
        this.onTick = onTick;
        this.onFinished = onFinished;
    }

    @Override
    protected Void doInBackground() {
        try {
            engine.init();

            /*
             * simUntil avanza de manera independiente a state.clk.
             *
             * Esto es importante porque state.clk puede saltar al tiempo exacto
             * de un evento, pero entre eventos también necesitamos avanzar el
             * reloj visual para que Renderers pueda interpolar posiciones.
             */
            double simUntil = 0.0;

            while (state.running && !isCancelled()) {

                /*
                 * Pausa.
                 */
                while (state.paused && state.running && !isCancelled()) {
                    Thread.sleep(80);
                }

                if (!state.running || isCancelled()) {
                    break;
                }

                /*
                 * Si el usuario desactiva animación, se procesa todo rápido.
                 */
                if (state.params.disableAnimation) {
                    boolean ok = true;

                    while (state.running && !isCancelled()) {
                        simUntil += SIM_STEP;
                        ok = engine.stepUntil(simUntil);

                        if (!ok) {
                            break;
                        }
                    }

                    publish(state.clk);
                    break;
                }

                /*
                 * Cantidad de pasos por refresco visual.
                 *
                 * Para velocidades normales, conviene 1 paso por frame.
                 * Para velocidades altas, procesamos más pasos para no saturar Swing.
                 */
                int stepsToRun = calculateStepsToRun(state.speedMult);

                boolean ok = true;

                for (int i = 0; i < stepsToRun; i++) {
                    simUntil += SIM_STEP;
                    ok = engine.stepUntil(simUntil);

                    if (!ok || !state.running || isCancelled()) {
                        break;
                    }
                }

                /*
                 * Refrescar la interfaz.
                 * process() se ejecutará en el hilo de Swing.
                 */
                publish(state.clk);

                if (!ok || !state.running || isCancelled()) {
                    break;
                }

                /*
                 * Dormir según velocidad.
                 * A mayor velocidad, menor espera real entre frames.
                 */
                long sleepMs = calculateSleepMs(state.speedMult);

                if (sleepMs > 0) {
                    Thread.sleep(sleepMs);
                }
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();

        } catch (Exception ex) {
            System.err.println(
                    "[SimWorker ERROR] "
                            + ex.getClass().getSimpleName()
                            + ": "
                            + ex.getMessage()
            );
            ex.printStackTrace();
        }

        state.running = false;
        state.finished = true;

        return null;
    }

    /**
     * Define cuántos pasos de simulación se ejecutan antes de refrescar la UI.
     *
     * En velocidades bajas se usa 1 para que se vea suave.
     * En velocidades altas se usan más pasos para que no se vuelva lento.
     */
    private int calculateStepsToRun(double speedMult) {
        if (speedMult >= 500.0) {
            return 5000;
        }

        if (speedMult >= 100.0) {
            return 1000;
        }

        if (speedMult >= 50.0) {
            return 200;
        }

        if (speedMult >= 20.0) {
            return 50;
        }

        if (speedMult >= 10.0) {
            return 10;
        }

        return 1;
    }

    /**
     * Calcula cuánto debe dormir el hilo según la velocidad.
     */
    private long calculateSleepMs(double speedMult) {
        double safeSpeed = Math.max(0.05, speedMult);

        long sleepMs = (long) (BASE_SLEEP_MS / safeSpeed);

        /*
         * En velocidades normales no dejamos que baje demasiado,
         * porque si no la CPU trabaja innecesariamente.
         */
        if (speedMult <= 10.0) {
            sleepMs = Math.max(5L, sleepMs);
        }

        return sleepMs;
    }

    @Override
    protected void process(List<Double> chunks) {
        /*
         * Este método corre en el EDT, o sea, en el hilo de Swing.
         * Aquí se actualiza la interfaz.
         */
        if (onTick != null) {
            onTick.run();
        }
    }

    @Override
    protected void done() {
        /*
         * Se ejecuta cuando termina o se cancela el worker.
         */
        if (onFinished != null) {
            onFinished.run();
        }
    }
}