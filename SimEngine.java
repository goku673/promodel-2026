/**

 * SimEngine - Motor de Simulación de Eventos Discretos (DES).
 *
 * Implementa toda la lógica del proceso de fabricación de engranes.
 * Cada locación tiene un método "done_X" que se ejecuta cuando una entidad
 * termina su servicio allí, y decide a dónde va la entidad a continuación.
 *
 * El motor usa lambdas (Runnable) en la FEL para máxima claridad.
 */
public class SimEngine {

    private final SimState s;
    private final SimParams p;
    private final Rng rng;

    public SimEngine(SimState state) {
        this.s   = state;
        this.p   = state.params;
        this.rng = state.rng;
    }

    // ── Inicialización ────────────────────────────────────────────────────

    /** Prepara la simulación y programa la primera llegada de barra. */
    public void init() {
        s.reset();
        s.running = true;
        schedule(0, this::arrival);
    }

    // ── Avance del reloj ──────────────────────────────────────────────────

    /**
     * Procesa todos los eventos de la FEL hasta el tiempo 'until'.
     * @return true si la simulación continúa; false si terminó.
     */
    public boolean stepUntil(double until) {
        while (!s.fel.isEmpty() && s.fel.peek().time <= until) {
            if (s.paused || !s.running) return s.running;
            Ev ev = s.fel.poll();
            s.clk = ev.time;
            if (s.clk > p.duracion) {
                s.running  = false;
                s.finished = true;
                return false;
            }
            ev.action.run();
        }
        return s.running;
    }

    // ── Eventos de llegada ────────────────────────────────────────────────

    /**
     * ARRIVAL: Crea una nueva BARRA_ACERO y la envía a CONVEYOR_1.
     * Programa la siguiente llegada (frecuencia fija).
     */
    private void arrival() {
        s.barrasLlegadas.incrementAndGet();
        s.enSistema++;
        Entity e = new Entity(EType.BARRA, s.clk);
        e.curLoc = "CONVEYOR_1";
        Loc c1 = loc("CONVEYOR_1");
        // CONVEYOR_1 tiene capacidad infinita: siempre entra
        c1.enter(s.clk);
        schedule(s.clk + p.conv1Tiempo, () -> doneConveyor1(e));
        // Siguiente barra
        schedule(s.clk + p.arriboFrecuencia, this::arrival);
    }

    // ── Lógica de cada locación ───────────────────────────────────────────

    /** CONVEYOR_1 → ALMACEN_1 (directo, sin recurso) */
    private void doneConveyor1(Entity e) {
        loc("CONVEYOR_1").exit(s.clk);
        e.curLoc = "ALMACEN_1";
        tryEnter(e, "ALMACEN_1");
    }

    /** ALMACEN_1 → CORTADORA (traslado fijo de 3 min, sin recurso) */
    private void doneAlmacen1(Entity e) {
        loc("ALMACEN_1").exit(s.clk);
        loc("ALMACEN_1").drain();
        e.curLoc = "transit→CORTADORA";
        schedule(s.clk + p.t1Traslado, () -> arrivedAt(e, "CORTADORA"));
    }

    /**
     * CORTADORA → TORNO (x2 piezas).
     * La BARRA_ACERO se destruye y se crean 2 PIEZA_CORTADA.
     * Cada pieza requiere TRABAJADOR_1 para el traslado.
     */
    private void doneCortadora(Entity barra) {
        loc("CORTADORA").exit(s.clk);
        loc("CORTADORA").drain();
        s.enSistema--; // barra sale del sistema

        // Crear 2 piezas cortadas
        for (int i = 0; i < 2; i++) {
            Entity pieza = new Entity(EType.PIEZA_CORTADA, s.clk);
            s.enSistema++;
            final Entity fp = pieza;
            // Solicitar TRABAJADOR_1 para transportar al TORNO
            res("T1").request(() -> {
                fp.curLoc = "transit→TORNO (T1)";
                schedule(s.clk + p.t1Traslado, () -> {
                    res("T1").release(); // liberar trabajador al llegar
                    arrivedAt(fp, "TORNO");
                });
            });
        }
    }

    /** TORNO → CONVEYOR_2 (traslado fijo 3 min) */
    private void doneTorno(Entity e) {
        e.type = EType.PIEZA_TORNEADA;
        loc("TORNO").exit(s.clk);
        loc("TORNO").drain();
        e.curLoc = "transit→CONVEYOR_2";
        schedule(s.clk + p.t1Traslado, () -> arrivedAt(e, "CONVEYOR_2"));
    }

    /** CONVEYOR_2 → FRESADORA (traslado = conv2Tiempo = 4 min) */
    private void doneConveyor2(Entity e) {
        loc("CONVEYOR_2").exit(s.clk);
        e.curLoc = "transit→FRESADORA";
        schedule(s.clk + p.conv2Tiempo, () -> arrivedAt(e, "FRESADORA"));
    }

    /** FRESADORA → ALMACEN_2 con TRABAJADOR_2 */
    private void doneFresadora(Entity e) {
        e.type = EType.PIEZA_FRESADA;
        loc("FRESADORA").exit(s.clk);
        loc("FRESADORA").drain();
        res("T2").request(() -> {
            e.curLoc = "transit→ALMACEN_2 (T2)";
            schedule(s.clk + p.t2Traslado, () -> {
                res("T2").release();
                arrivedAt(e, "ALMACEN_2");
            });
        });
    }

    /** ALMACEN_2 → PINTURA con MONTACARGAS */
    private void doneAlmacen2(Entity e) {
        loc("ALMACEN_2").exit(s.clk);
        loc("ALMACEN_2").drain();
        res("MK").request(() -> {
            e.curLoc = "transit→PINTURA (MK)";
            schedule(s.clk + p.mkTraslado1, () -> {
                res("MK").release();
                arrivedAt(e, "PINTURA");
            });
        });
    }

    /** PINTURA → INSPECCION_1 con MONTACARGAS */
    private void donePintura(Entity e) {
        e.type = EType.PIEZA_PINTADA;
        loc("PINTURA").exit(s.clk);
        loc("PINTURA").drain();
        res("MK").request(() -> {
            e.curLoc = "transit→INSPECCION_1 (MK)";
            schedule(s.clk + p.mkTraslado2, () -> {
                res("MK").release();
                arrivedAt(e, "INSPECCION_1");
            });
        });
    }

    /**
     * INSPECCION_1 → 80% EMPAQUE | 20% INSPECCION_2.
     * 80%: va directo a EMPAQUE
     * 20%: se mueve 4 min a INSPECCION_2
     */
    private void doneInspeccion1(Entity e) {
        loc("INSPECCION_1").exit(s.clk);
        loc("INSPECCION_1").drain();
        if (rng.prob(p.probRechazo)) {
            // 20% → INSPECCION_2 (Move for 4 min)
            e.curLoc = "transit→INSPECCION_2";
            schedule(s.clk + 4.0, () -> arrivedAt(e, "INSPECCION_2"));
        } else {
            // 80% → EMPAQUE directo
            tryEnter(e, "EMPAQUE");
        }
    }

    /** INSPECCION_2 → EMPAQUE (Move for 3 min) */
    private void doneInspeccion2(Entity e) {
        loc("INSPECCION_2").exit(s.clk);
        loc("INSPECCION_2").drain();
        e.curLoc = "transit→EMPAQUE";
        schedule(s.clk + 3.0, () -> arrivedAt(e, "EMPAQUE"));
    }

    /** EMPAQUE → EMBARQUE con TRABAJADOR_3 */
    private void doneEmpaque(Entity e) {
        e.type = EType.PIEZA_FINAL;
        loc("EMPAQUE").exit(s.clk);
        loc("EMPAQUE").drain();
        res("T3").request(() -> {
            e.curLoc = "transit→EMBARQUE (T3)";
            schedule(s.clk + p.t3Traslado, () -> {
                res("T3").release();
                arrivedAt(e, "EMBARQUE");
            });
        });
    }

    /** EMBARQUE → EXIT (Move for 3 min, pieza sale del sistema) */
    private void doneEmbarque(Entity e) {
        loc("EMBARQUE").exit(s.clk);
        loc("EMBARQUE").drain();
        s.piezasFinales.incrementAndGet();
        s.enSistema--;
        // Registrar punto en historial de throughput
        s.histThroughput.add(new double[]{s.clk, s.piezasFinales.get()});
    }

    // ── Métodos de ruteo ──────────────────────────────────────────────────

    /**
     * Intenta hacer entrar la entidad en la locación destino.
     * Si está llena, la encola en loc.waiting.
     */
    private void tryEnter(Entity e, String destName) {
        Loc D = loc(destName);
        if (!D.full()) {
            D.enter(s.clk);
            e.curLoc = destName;
            schedule(s.clk + p.serviceTime(destName, rng), () -> done(e, destName));
        } else {
            D.waiting.offer(() -> {
                D.enter(s.clk);
                e.curLoc = destName;
                schedule(s.clk + p.serviceTime(destName, rng), () -> done(e, destName));
            });
        }
    }

    /**
     * La entidad llega a destino tras un traslado en tránsito.
     * Equivale a tryEnter pero llamado al terminar el transit.
     */
    private void arrivedAt(Entity e, String destName) {
        tryEnter(e, destName);
    }

    /**
     * Despacha el evento "terminó servicio en locación loc".
     */
    private void done(Entity e, String locName) {
        switch (locName) {
            case "CONVEYOR_1":   doneConveyor1(e);   break;
            case "ALMACEN_1":    doneAlmacen1(e);    break;
            case "CORTADORA":    doneCortadora(e);   break;
            case "TORNO":        doneTorno(e);        break;
            case "CONVEYOR_2":   doneConveyor2(e);   break;
            case "FRESADORA":    doneFresadora(e);   break;
            case "ALMACEN_2":    doneAlmacen2(e);    break;
            case "PINTURA":      donePintura(e);     break;
            case "INSPECCION_1": doneInspeccion1(e); break;
            case "INSPECCION_2": doneInspeccion2(e); break;
            case "EMPAQUE":      doneEmpaque(e);     break;
            case "EMBARQUE":     doneEmbarque(e);    break;
        }
    }

    // ── Atajos internos ───────────────────────────────────────────────────
    private void  schedule(double t, Runnable a) { s.schedule(t, a); }
    private Loc   loc(String n) { return s.loc(n); }
    private Res   res(String n) { return s.res(n); }
}
