/**
 * SimEngine - Motor DES completo de fabricacion de engranes.
 * ACTUALIZADO: usa request(clk, travel, action) y release(clk)
 * para registrar correctamente tiempos de trabajo por recurso.
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

    public void init() {
        s.reset();
        s.running = true;
        schedule(0, this::arrival);
    }

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

    // ── Llegada de BARRA_ACERO ────────────────────────────────────────────
    private void arrival() {
        s.barrasLlegadas.incrementAndGet();
        s.enSistema++;
        Entity e = new Entity(EType.BARRA, s.clk);
        e.curLoc = "CONVEYOR_1";
        loc("CONVEYOR_1").enter(s.clk);
        schedule(s.clk + p.conv1Tiempo,      () -> doneConveyor1(e));
        schedule(s.clk + p.arriboFrecuencia, this::arrival);
    }

    // ── CONVEYOR_1 → ALMACEN_1 ───────────────────────────────────────────
    private void doneConveyor1(Entity e) {
        loc("CONVEYOR_1").exit(s.clk);
        e.curLoc = "ALMACEN_1";
        tryEnter(e, "ALMACEN_1");
    }

    // ── ALMACEN_1 → CORTADORA (traslado fijo 3 min) ───────────────────────
    private void doneAlmacen1(Entity e) {
        loc("ALMACEN_1").exit(s.clk);
        loc("ALMACEN_1").drain();
        e.curLoc = "transit→CORTADORA";
        schedule(s.clk + p.t1Traslado, () -> arrivedAt(e, "CORTADORA"));
    }

    // ── CORTADORA → TORNO (crea 2 PIEZA_CORTADA, usa TRABAJADOR_1) ───────
    private void doneCortadora(Entity barra) {
        loc("CORTADORA").exit(s.clk);
        loc("CORTADORA").drain();
        s.enSistema--;   // barra sale

        for (int i = 0; i < 2; i++) {
            Entity pieza = new Entity(EType.PIEZA_CORTADA, s.clk);
            s.enSistema++;
            final Entity fp = pieza;
            res("T1").request(s.clk, p.t1Traslado, () -> {
                fp.curLoc = "transit→TORNO";
                schedule(s.clk + p.t1Traslado, () -> {
                    res("T1").release(s.clk);
                    arrivedAt(fp, "TORNO");
                });
            });
        }
    }

    // ── TORNO → CONVEYOR_2 ────────────────────────────────────────────────
    private void doneTorno(Entity e) {
        e.type = EType.PIEZA_TORNEADA;
        loc("TORNO").exit(s.clk);
        loc("TORNO").drain();
        e.curLoc = "transit→CONVEYOR_2";
        schedule(s.clk + p.t1Traslado, () -> arrivedAt(e, "CONVEYOR_2"));
    }

    // ── CONVEYOR_2 → FRESADORA ────────────────────────────────────────────
    private void doneConveyor2(Entity e) {
        loc("CONVEYOR_2").exit(s.clk);
        e.curLoc = "transit→FRESADORA";
        schedule(s.clk + p.conv2Tiempo, () -> arrivedAt(e, "FRESADORA"));
    }

    // ── FRESADORA → ALMACEN_2 (usa TRABAJADOR_2) ─────────────────────────
    private void doneFresadora(Entity e) {
        e.type = EType.PIEZA_FRESADA;
        loc("FRESADORA").exit(s.clk);
        loc("FRESADORA").drain();
        res("T2").request(s.clk, p.t2Traslado, () -> {
            e.curLoc = "transit→ALMACEN_2";
            schedule(s.clk + p.t2Traslado, () -> {
                res("T2").release(s.clk);
                arrivedAt(e, "ALMACEN_2");
            });
        });
    }

    // ── ALMACEN_2 → PINTURA (usa MONTACARGAS) ────────────────────────────
    private void doneAlmacen2(Entity e) {
        loc("ALMACEN_2").exit(s.clk);
        loc("ALMACEN_2").drain();
        res("MK").request(s.clk, p.mkTraslado1, () -> {
            e.curLoc = "transit→PINTURA";
            schedule(s.clk + p.mkTraslado1, () -> {
                res("MK").release(s.clk);
                arrivedAt(e, "PINTURA");
            });
        });
    }

    // ── PINTURA → INSPECCION_1 (usa MONTACARGAS) ─────────────────────────
    private void donePintura(Entity e) {
        e.type = EType.PIEZA_PINTADA;
        loc("PINTURA").exit(s.clk);
        loc("PINTURA").drain();
        res("MK").request(s.clk, p.mkTraslado2, () -> {
            e.curLoc = "transit→INSPECCION_1";
            schedule(s.clk + p.mkTraslado2, () -> {
                res("MK").release(s.clk);
                arrivedAt(e, "INSPECCION_1");
            });
        });
    }

    // ── INSPECCION_1 → 80% EMPAQUE | 20% INSPECCION_2 ───────────────────
    private void doneInspeccion1(Entity e) {
        loc("INSPECCION_1").exit(s.clk);
        loc("INSPECCION_1").drain();
        if (rng.prob(p.probRechazo)) {
            e.curLoc = "transit→INSPECCION_2";
            schedule(s.clk + 4.0, () -> arrivedAt(e, "INSPECCION_2"));
        } else {
            tryEnter(e, "EMPAQUE");
        }
    }

    // ── INSPECCION_2 → EMPAQUE ────────────────────────────────────────────
    private void doneInspeccion2(Entity e) {
        loc("INSPECCION_2").exit(s.clk);
        loc("INSPECCION_2").drain();
        e.curLoc = "transit→EMPAQUE";
        schedule(s.clk + 3.0, () -> arrivedAt(e, "EMPAQUE"));
    }

    // ── EMPAQUE → EMBARQUE (usa TRABAJADOR_3) ────────────────────────────
    private void doneEmpaque(Entity e) {
        e.type = EType.PIEZA_FINAL;
        loc("EMPAQUE").exit(s.clk);
        loc("EMPAQUE").drain();
        res("T3").request(s.clk, p.t3Traslado, () -> {
            e.curLoc = "transit→EMBARQUE";
            schedule(s.clk + p.t3Traslado, () -> {
                res("T3").release(s.clk);
                arrivedAt(e, "EMBARQUE");
            });
        });
    }

    // ── EMBARQUE → EXIT ───────────────────────────────────────────────────
    private void doneEmbarque(Entity e) {
        loc("EMBARQUE").exit(s.clk);
        loc("EMBARQUE").drain();
        s.piezasFinales.incrementAndGet();
        s.enSistema--;
        s.histThroughput.add(new double[]{s.clk, s.piezasFinales.get()});
    }

    // ── Ruteo genérico ─────────────────────────────────────────────────────
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

    private void arrivedAt(Entity e, String destName) { tryEnter(e, destName); }

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

    private void  schedule(double t, Runnable a) { s.schedule(t, a); }
    private Loc   loc(String n) { return s.loc(n); }
    private Res   res(String n) { return s.res(n); }
}
