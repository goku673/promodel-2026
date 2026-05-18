/**
 * SimEngine - Motor DES de fabricacion de engranes.
 * Actualizado: usa request(clk,travel,action) y release(clk) para tracking,
 * ademas mueve agentes visuales (trabajadores/montacargas) y cuenta EMBARQUE.
 */
public class SimEngine {

    private final SimState s;
    private final SimParams p;
    private final Rng rng;

    public SimEngine(SimState state) {
        this.s = state;
        this.p = state.params;
        this.rng = state.rng;
    }

    public void init() {
        s.reset();
        s.running = true;
        if (s.currentData != null) {
            for (ProModelData.ArrDef arr : s.currentData.arrivals) {
                double firstTime = parseTime(arr.firstTime);
                schedule(firstTime, () -> doArrival(arr, 1));
            }
        }
    }

    public boolean stepUntil(double until) {
        while (!s.fel.isEmpty() && s.fel.peek().time <= until) {
            if (s.paused || !s.running)
                return s.running;
            Ev ev = s.fel.poll();
            s.clk = ev.time;
            if (s.clk > p.duracion) {
                s.running = false;
                s.finished = true;
                return false;
            }
            ev.action.run();
        }
        return s.running;
    }

    private double parseTime(String expr) {
        if (expr == null || expr.isEmpty())
            return 0;
        expr = expr.toUpperCase().replace("MIN", "").trim();
        expr = expr.replace("WAIT", "").replace("MOVE FOR", "").trim();

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

    private void doOperation(Entity e) {
        ProModelData.ProcDef proc = findOperation(e.typeName, e.curLoc);
        double waitTime = 0;
        if (proc != null && proc.operation != null) {
            String[] lines = proc.operation.split("\n");
            for (String line : lines) {
                line = line.trim().toUpperCase();
                if (line.startsWith("WAIT")) {
                    waitTime += parseTime(line);
                } else if (line.matches("^[0-9]+.*") || line.startsWith("N(") || line.startsWith("E(") || line.startsWith("U(") || line.startsWith("T(")) {
                    waitTime += parseTime(line);
                }
            }
        }

        if (waitTime > 0) {
            schedule(s.clk + waitTime, () -> doRouting(e));
        } else {
            doRouting(e);
        }
    }

    private void doRouting(Entity e) {
        String oldLoc = e.curLoc;
        ProModelData.ProcDef route = findRoute(e.typeName, e.curLoc);

        if (route == null || route.destination == null || route.destination.equalsIgnoreCase("EXIT")) {
            freeLoc(oldLoc);
            finishEntity(e);
            return;
        }

        if (route.output != null && !route.output.isEmpty()) {
            e.typeName = route.output;
            for (ProModelData.EntDef ed : s.currentData.entities) {
                if (ed.name.equalsIgnoreCase(e.typeName)) {
                    e.iconPath = ed.iconPath;
                    break;
                }
            }
        }

        e.curLoc = resolveDest(route.destination);
        double travelTime = parseTime(route.moveLogic);

        if (travelTime > 0) {
            Loc destLoc = s.locs.get(e.curLoc);
            if (destLoc != null) {
                e.targetX = destLoc.x + destLoc.w / 2;
                e.targetY = destLoc.y + destLoc.h / 2;
                e.moveStartTime = s.clk;
                e.moveEndTime = s.clk + travelTime;
                e.moving = true;
            }
            freeLoc(oldLoc); // Libera mientras viaja

            schedule(s.clk + travelTime, () -> {
                e.moving = false;
                e.curX = e.targetX;
                e.curY = e.targetY;
                tryEnter(e);
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
