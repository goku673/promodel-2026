
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * SimEngine - Motor de simulación DES.
 *
 * Versión corregida para:
 * - Importar rutas desde ProModel.
 * - Mover entidades visualmente entre todas las locaciones.
 * - Usar MOVE WITH recurso.
 * - Calcular tiempo de viaje con distancia / velocidad.
 * - Mostrar operadores y vehículo moviéndose.
 * - Soportar WAIT, GRAPHIC, GET, FREE, JOIN y ACCUM de forma básica.
 */
public class SimEngine {

    private final SimState s;
    private final SimParams p;
    private final Rng rng;

    /**
     * Control simple para ACCUM.
     * Llave: entidad + locación.
     * Valor: cantidad acumulada.
     */
    private final Map<String, Integer> accumCounters = new HashMap<>();

    public SimEngine(SimState state) {
        this.s = state;
        this.p = state.params;
        this.rng = state.rng;
    }

    /**
     * Inicializa la simulación.
     */
    public void init() {
        s.reset();
        s.running = true;
        schedule(0, this::arrival);
    }

    /**
     * Ejecuta eventos hasta el tiempo indicado.
     */
    public boolean stepUntil(double until) {
        while (!s.fel.isEmpty() && s.fel.peek().time <= until) {
            if (s.paused || !s.running) return s.running;
            Ev ev = s.fel.poll();

            /*
             * Importante: primero colocamos el reloj exactamente
             * en el tiempo del evento.
             */
            s.clk = ev.time;

            if (s.clk > p.duracion) {
                s.running = false;
                s.finished = true;
                return false;
            }

            ev.action.run();
        }

        /*
         * Corrección visual importante:
         * aunque no haya eventos entre un tiempo y otro, el reloj debe avanzar.
         * Si el reloj no avanza, las entidades y recursos parecen aparecer
         * directamente en la siguiente locación.
         */
        if (s.running && !s.paused) {
            s.clk = Math.min(until, p.duracion);
        }

        if (s.clk >= p.duracion) {
            s.running = false;
            s.finished = true;
            return false;
        }

        return s.running;
    }

    // ── Helper: pixel-center de una locacion ──────────────────────────────
    private float[] locCenter(String name) {
        Loc l = loc(name);
        if (l == null) return new float[]{0, 0};
        return new float[]{l.x + l.w / 2f, l.y + l.h / 2f};
    }

        if (expr.startsWith("E(") || expr.startsWith("EXP(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                double mean = Double.parseDouble(expr.substring(start + 1, end).trim());
                return rng.exp(mean);
            } catch (Exception e) {
            }
        } else if (expr.startsWith("N(") || expr.startsWith("NORM(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double mean = Double.parseDouble(parts[0].trim());
                double std = Double.parseDouble(parts[1].trim());
                return rng.norm(mean, std);
            } catch (Exception e) {
            }
        } else if (expr.startsWith("U(") || expr.startsWith("UNIFORM(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double mean = Double.parseDouble(parts[0].trim());
                double halfRange = Double.parseDouble(parts[1].trim());
                return rng.uniform(mean, halfRange);
            } catch (Exception e) {
            }
        } else if (expr.startsWith("T(") || expr.startsWith("TRI(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double min = Double.parseDouble(parts[0].trim());
                double mode = Double.parseDouble(parts[1].trim());
                double max = Double.parseDouble(parts[2].trim());
                return rng.triangular(min, mode, max);
            } catch (Exception e) {
            }
        } else {
            try {
                return Double.parseDouble(expr);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private void doArrival(ProModelData.ArrDef arr, int occurrence) {
        if (!s.running)
            return;

        int qty = (int) Math.max(1, parseTime(arr.qty));
        int maxOcc = 1;
        try {
            if (!arr.occurrences.toUpperCase().contains("INF"))
                maxOcc = Integer.parseInt(arr.occurrences);
            else
                maxOcc = Integer.MAX_VALUE;
        } catch (Exception e) {
        }

        String resolvedLoc = resolveDest(arr.location);

        for (int i = 0; i < qty; i++) {
            Entity e = new Entity(arr.entity, s.clk);
            for (ProModelData.EntDef ed : s.currentData.entities) {
                if (ed.name.equalsIgnoreCase(e.typeName)) {
                    e.iconPath = ed.iconPath;
                    break;
                }
            }
            e.curLoc = resolvedLoc;

            Loc loc = s.locs.get(e.curLoc);
            if (loc != null) {
                e.curX = loc.x + loc.w / 2;
                e.curY = loc.y + loc.h / 2;
            }
            s.activeEntities.add(e);
            s.enSistema++;

            tryEnter(e);
        }

        if (occurrence < maxOcc) {
            double nextArrival = s.clk + parseTime(arr.frequency);
            schedule(nextArrival, () -> doArrival(arr, occurrence + 1));
        }
    }

    private void tryEnter(Entity e) {
        Loc l = s.loc(e.curLoc);
        if (l == null) {
            finishEntity(e);
            return;
        }

        if (!l.full()) {
            l.enter(s.clk);
            doOperation(e);
        } else {
            l.waiting.offer(() -> {
                l.enter(s.clk);
                doOperation(e);
            });
        }
    }

    // ── ALMACEN_2 → PINTURA (MONTACARGAS: va DESDE almacen HASTA pintura) ──────
    private void doneAlmacen2(Entity e) {
        loc("ALMACEN_2").exit(s.clk);
        loc("ALMACEN_2").drain();
        float[] from = locCenter("ALMACEN_2");
        float[] tgt  = locCenter("PINTURA");
        res("MK").request(s.clk, p.mkTraslado1, () -> {
            // MK parte desde ALMACEN_2 hasta PINTURA
            s.agentGoFrom("MK", from[0], from[1], tgt[0], tgt[1]);
            e.curLoc = "transit→PINTURA";
            schedule(s.clk + p.mkTraslado1, () -> {
                res("MK").release(s.clk);
                s.agentReturnHome("MK");
                arrivedAt(e, "PINTURA");
            });
        });
    }

    // ── PINTURA → INSPECCION_1 (MONTACARGAS: va DESDE pintura HASTA inspeccion) ──
    private void donePintura(Entity e) {
        e.type = EType.PIEZA_PINTADA;
        loc("PINTURA").exit(s.clk);
        loc("PINTURA").drain();
        float[] from = locCenter("PINTURA");
        float[] tgt  = locCenter("INSPECCION_1");
        res("MK").request(s.clk, p.mkTraslado2, () -> {
            // MK parte desde PINTURA hasta INSPECCION_1
            s.agentGoFrom("MK", from[0], from[1], tgt[0], tgt[1]);
            e.curLoc = "transit→INSPECCION_1";
            schedule(s.clk + p.mkTraslado2, () -> {
                res("MK").release(s.clk);
                s.agentReturnHome("MK");
                arrivedAt(e, "INSPECCION_1");
            });
        } else {
            Loc destLoc = s.locs.get(e.curLoc);
            if (destLoc != null) {
                e.curX = destLoc.x + destLoc.w / 2;
                e.curY = destLoc.y + destLoc.h / 2;
            }
            freeLoc(oldLoc);
            tryEnter(e);
        }
    }

    private void freeLoc(String locName) {
        Loc l = s.loc(locName);
        if (l != null) {
            l.exit(s.clk);
            l.drain();
        }
    }

    private void finishEntity(Entity e) {
        s.activeEntities.remove(e);
        s.enSistema--;
        s.entidadesSalientes.incrementAndGet();
        s.histThroughput.add(new double[] { s.clk, s.entidadesSalientes.get() });
    }

    private ProModelData.ProcDef findOperation(String entityName, String locName) {
        String baseLocName = locName;
        if (baseLocName.matches(".*\\.\\d+$"))
            baseLocName = baseLocName.substring(0, baseLocName.lastIndexOf('.'));

        for (ProModelData.ProcDef p : s.routes) {
            if (p.entity.equalsIgnoreCase(entityName) && p.location.equalsIgnoreCase(baseLocName) && p.operation != null
                    && !p.operation.trim().isEmpty()) {
                return p;
            }
        }
        return null;
    }

    private ProModelData.ProcDef findRoute(String entityName, String locName) {
        String baseLocName = locName;
        if (baseLocName.matches(".*\\.\\d+$"))
            baseLocName = baseLocName.substring(0, baseLocName.lastIndexOf('.'));

        for (ProModelData.ProcDef p : s.routes) {
            if (p.entity.equalsIgnoreCase(entityName) && p.location.equalsIgnoreCase(baseLocName)
                    && p.destination != null && !p.destination.trim().isEmpty()) {
                return p;
            }
        }
        for (ProModelData.ProcDef p : s.routes) {
            if (p.location.equalsIgnoreCase(baseLocName) && p.destination != null && !p.destination.trim().isEmpty())
                return p;
        }
        return null;
    }

    private String resolveDest(String destName) {
        if (s.locs.containsKey(destName))
            return destName;
        java.util.List<String> units = new java.util.ArrayList<>();
        for (String k : s.locs.keySet()) {
            if (k.startsWith(destName + "."))
                units.add(k);
        }
        if (units.isEmpty())
            return destName;

        String best = units.get(0);
        int minL = Integer.MAX_VALUE;
        for (String u : units) {
            Loc l = s.locs.get(u);
            if (l.cnt + l.waiting.size() < minL) {
                minL = l.cnt + l.waiting.size();
                best = u;
            }
        }
        
        java.util.List<String> tied = new java.util.ArrayList<>();
        for (String u : units) {
            Loc l = s.locs.get(u);
            if (l.cnt + l.waiting.size() == minL) {
                tied.add(u);
            }
        }
        
        if (tied.size() > 1) {
            // Escoger aleatoriamente entre las empatadas
            return tied.get((int)(Math.random() * tied.size()));
        }
        
        return best;
    }

    private void schedule(double t, Runnable a) {
        s.schedule(t, a);
    }
}
