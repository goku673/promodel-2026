
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
        this.s   = state;
        this.p   = state.params;
        this.rng = state.rng;
    }

    /**
     * Inicializa la simulación.
     */
    public void init() {
        s.reset();
        s.running = true;
<<<<<<< Updated upstream
        schedule(0, this::arrival);
=======
        accumCounters.clear();

        if (s.currentData != null) {
            for (ProModelData.ArrDef arr : s.currentData.arrivals) {
                double firstTime = parseTime(arr.firstTime);
                schedule(firstTime, () -> doArrival(arr, 1));
            }
        }
>>>>>>> Stashed changes
    }

    /**
     * Ejecuta eventos hasta el tiempo indicado.
     */
    public boolean stepUntil(double until) {
        while (!s.fel.isEmpty() && s.fel.peek().time <= until) {
<<<<<<< Updated upstream
            if (s.paused || !s.running) return s.running;
=======
            if (s.paused || !s.running) {
                return s.running;
            }

>>>>>>> Stashed changes
            Ev ev = s.fel.poll();

            /*
             * Importante: primero colocamos el reloj exactamente
             * en el tiempo del evento.
             */
            s.clk = ev.time;

            if (s.clk > p.duracion) {
                s.running  = false;
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

<<<<<<< Updated upstream
    // ── Helper: pixel-center de una locacion ──────────────────────────────
    private float[] locCenter(String name) {
        Loc l = loc(name);
        if (l == null) return new float[]{0, 0};
        return new float[]{l.x + l.w / 2f, l.y + l.h / 2f};
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

    // ── ALMACEN_1 → CORTADORA (T1 lleva la pieza) ────────────────────────
    private void doneAlmacen1(Entity e) {
        loc("ALMACEN_1").exit(s.clk);
        loc("ALMACEN_1").drain();
        float[] tgt = locCenter("CORTADORA");
        s.agentGoTo("T1", tgt[0], tgt[1]);
        e.curLoc = "transit→CORTADORA";
        schedule(s.clk + p.t1Traslado, () -> {
            s.agentReturnHome("T1");
            arrivedAt(e, "CORTADORA");
        });
    }

    // ── CORTADORA → TORNO (T1 lleva las 2 piezas) ────────────────────────
    private void doneCortadora(Entity barra) {
        loc("CORTADORA").exit(s.clk);
        loc("CORTADORA").drain();
        s.enSistema--;

        for (int i = 0; i < 2; i++) {
            Entity pieza = new Entity(EType.PIEZA_CORTADA, s.clk);
            s.enSistema++;
            final Entity fp = pieza;
            float[] tgt = locCenter("TORNO");
            res("T1").request(s.clk, p.t1Traslado, () -> {
                s.agentGoTo("T1", tgt[0], tgt[1]);
                fp.curLoc = "transit→TORNO";
                schedule(s.clk + p.t1Traslado, () -> {
                    res("T1").release(s.clk);
                    s.agentReturnHome("T1");
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

    // ── FRESADORA → ALMACEN_2 (T2 lleva la pieza) ─────────────────────────
    private void doneFresadora(Entity e) {
        e.type = EType.PIEZA_FRESADA;
        loc("FRESADORA").exit(s.clk);
        loc("FRESADORA").drain();
        float[] tgt = locCenter("ALMACEN_2");
        res("T2").request(s.clk, p.t2Traslado, () -> {
            s.agentGoTo("T2", tgt[0], tgt[1]);
            e.curLoc = "transit→ALMACEN_2";
            schedule(s.clk + p.t2Traslado, () -> {
                res("T2").release(s.clk);
                s.agentReturnHome("T2");
                arrivedAt(e, "ALMACEN_2");
            });
        });
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
=======
    /**
     * Interpreta tiempos de ProModel:
     * WAIT 10 min, 10, E(5), N(10,2), U(5,1), T(1,2,3), etc.
     */
    private double parseTime(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return 0;
        }

        String raw = expr.toUpperCase().trim();
        double unitFactor = 1.0; // todo se convierte a minutos

        if (raw.contains("HOURS") || raw.contains("HOUR")
                || raw.contains("HORAS") || raw.contains("HORA")
                || raw.matches(".*\\bHR\\b.*") || raw.matches(".*\\bHRS\\b.*")) {
            unitFactor = 60.0;
        } else if (raw.contains("SECONDS") || raw.contains("SECOND")
                || raw.contains("SEGUNDOS") || raw.contains("SEGUNDO")
                || raw.matches(".*\\bSEC\\b.*") || raw.matches(".*\\bSEG\\b.*")) {
            unitFactor = 1.0 / 60.0;
        }

        raw = raw
                .replace("MINUTES", "")
                .replace("MINUTE", "")
                .replace("MINS", "")
                .replace("MIN", "")
                .replace("HOURS", "")
                .replace("HOUR", "")
                .replace("HORAS", "")
                .replace("HORA", "")
                .replace("HRS", "")
                .replace("HR", "")
                .replace("SECONDS", "")
                .replace("SECOND", "")
                .replace("SEGUNDOS", "")
                .replace("SEGUNDO", "")
                .replace("SEC", "")
                .replace("SEG", "")
                .replace("WAIT", "")
                .replace("MOVE FOR", "")
                .trim();

        if (raw.isEmpty()) {
            return 0;
        }

        // Soporta expresiones tipo: 38 + E(151) hr
        List<String> sumParts = splitTopLevel(raw, '+');
        if (sumParts.size() > 1) {
            double total = 0;
            for (String part : sumParts) {
                total += parseTime(part);
            }
            return total * unitFactor;
        }

        double value = parseTimeWithoutUnit(raw);
        return value * unitFactor;
    }

    private List<String> splitTopLevel(String expr, char op) {
        List<String> parts = new ArrayList<>();
        int level = 0;
        int start = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (c == '(') level++;
            if (c == ')') level = Math.max(0, level - 1);

            if (c == op && level == 0) {
                parts.add(expr.substring(start, i).trim());
                start = i + 1;
            }
        }

        parts.add(expr.substring(start).trim());
        return parts;
    }

    private double parseTimeWithoutUnit(String expr) {
        if (expr == null || expr.trim().isEmpty()) {
            return 0;
        }

        expr = expr.trim();

        if (expr.startsWith("E(") || expr.startsWith("EXP(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                double mean = Double.parseDouble(expr.substring(start + 1, end).trim().replace(",", "."));
                return rng.exp(mean);
            } catch (Exception e) {
                return 0;
            }
        }

        if (expr.startsWith("N(") || expr.startsWith("NORM(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double mean = Double.parseDouble(parts[0].trim().replace(",", "."));
                double std = Double.parseDouble(parts[1].trim().replace(",", "."));
                return rng.norm(mean, std);
            } catch (Exception e) {
                return 0;
            }
        }

        if (expr.startsWith("U(") || expr.startsWith("UNIFORM(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double mean = Double.parseDouble(parts[0].trim().replace(",", "."));
                double halfRange = Double.parseDouble(parts[1].trim().replace(",", "."));
                return rng.uniform(mean, halfRange);
            } catch (Exception e) {
                return 0;
            }
        }

        if (expr.startsWith("T(") || expr.startsWith("TRI(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double min = Double.parseDouble(parts[0].trim().replace(",", "."));
                double mode = Double.parseDouble(parts[1].trim().replace(",", "."));
                double max = Double.parseDouble(parts[2].trim().replace(",", "."));
                return rng.triangular(min, mode, max);
            } catch (Exception e) {
                return 0;
            }
        }

        // Erlang de ProModel: ER(media, fases).
        // Ejemplo del ejercicio: ER(18,3) = 3 fases con media total 18 minutos.
        if (expr.startsWith("ER(") || expr.startsWith("ERLANG(")) {
            try {
                int start = expr.indexOf('(');
                int end = expr.indexOf(')');
                String[] parts = expr.substring(start + 1, end).split(",");
                double mean = Double.parseDouble(parts[0].trim().replace(",", "."));
                int phases = (int) Math.max(1, Math.round(Double.parseDouble(parts[1].trim().replace(",", "."))));
                double phaseMean = mean / phases;
                double total = 0.0;
                for (int i = 0; i < phases; i++) {
                    total += rng.exp(phaseMean);
                }
                return total;
            } catch (Exception e) {
                return 0;
            }
        }

        try {
            return Double.parseDouble(expr.replace(",", ".").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Genera arribos según el TXT de ProModel.
     */
    private void doArrival(ProModelData.ArrDef arr, int occurrence) {
        if (!s.running) {
            return;
        }

        int qty = 1;
        if (s.currentData != null) {
            qty = s.currentData.parseMacroInteger(arr.qty, 1);
        } else {
            qty = (int) Math.max(1, parseTime(arr.qty));
        }

        int maxOcc = 1;
        try {
            if (arr.occurrences != null && arr.occurrences.toUpperCase().contains("INF")) {
                maxOcc = Integer.MAX_VALUE;
            } else {
                maxOcc = Integer.parseInt(arr.occurrences.trim());
            }
        } catch (Exception e) {
            maxOcc = Integer.MAX_VALUE;
        }

        String resolvedLoc = resolveDest(arr.location);

        for (int i = 0; i < qty; i++) {
            Entity e = new Entity(arr.entity, s.clk);
            e.curLoc = resolvedLoc;

            Loc loc = s.locs.get(e.curLoc);
            if (loc != null) {
                e.curX = loc.x + loc.w / 2f;
                e.curY = loc.y + loc.h / 2f;
                e.targetX = e.curX;
                e.targetY = e.curY;
            }

            s.activeEntities.add(e);
            s.enSistema++;
            s.entidadesCreadas.incrementAndGet();

            tryEnter(e);
        }

        if (occurrence < maxOcc) {
            double frequency = parseTime(arr.frequency);
            if (frequency <= 0) {
                frequency = 1;
            }

            double nextArrival = s.clk + frequency;
            schedule(nextArrival, () -> doArrival(arr, occurrence + 1));
        }
    }

    /**
     * Intenta ingresar una entidad a una locación.
     * Si la locación está llena, la entidad espera.
     */
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

    /**
     * Ejecuta la operación de la entidad en su locación actual.
     */
    private void doOperation(Entity e) {
        ProModelData.ProcDef proc = findOperation(e.typeName, e.curLoc);

        if (proc == null || proc.operation == null || proc.operation.trim().isEmpty()) {
            doRouting(e);
            return;
        }

        String[] lines = proc.operation.split("[\\n|]");

        String getResName = null;

        for (String line : lines) {
            String clean = line.trim();
            String upper = clean.toUpperCase();

            if (upper.startsWith("GET ")) {
                getResName = clean.substring(4).trim();
                break;
            }
        }

        if (getResName != null) {
            final String resName = getResName;
            final String[] opLines = lines;

            Res r = s.res(resName);

            if (r == null) {
                executeOperation(e, opLines, null);
            } else {
                Loc curLoc = s.locs.get(e.curLoc);

                float tx = curLoc != null ? curLoc.x + curLoc.w / 2f : 50;
                float ty = curLoc != null ? curLoc.y + curLoc.h / 2f : 50;

                s.agentGoTo(resName, tx, ty);

                r.request(s.clk, 0, () -> executeOperation(e, opLines, resName));
            }
        } else {
            executeOperation(e, lines, null);
        }
    }

    /**
     * Inicia la ejecución secuencial de las líneas de operación.
     */
    private void executeOperation(Entity e, String[] lines, String resName) {
        processOperationStep(e, lines, 0, resName);
    }

    /**
     * Procesa paso por paso una operación.
     */
    private void processOperationStep(Entity e, String[] lines, int stepIndex, String resName) {
        if (stepIndex >= lines.length) {
            if (resName != null) {
                Res r = s.res(resName);
                if (r != null) {
                    r.release(s.clk);
                    s.agentReturnHome(resName);
                }
            }

            doRouting(e);
            return;
        }

        String rawLine = lines[stepIndex];
        String cleanLine = rawLine.trim();
        String line = cleanLine.toUpperCase();
        int nextStep = stepIndex + 1;

        if (line.isEmpty()) {
            processOperationStep(e, lines, nextStep, resName);
            return;
        }

        /**
         * GRAPHIC N
         */
        if (line.startsWith("GRAPHIC ")) {
            try {
                int gid = Integer.parseInt(line.substring(8).trim());
                e.currentGraphicId = gid;
            } catch (Exception ex) {
                // Ignorar si no es número.
            }

            processOperationStep(e, lines, nextStep, resName);
            return;
        }

        /**
         * WAIT
         */
        if (line.startsWith("WAIT ")
                || line.matches("^[0-9].*")
                || line.matches("^[ENUT]\\(.*")) {

            double dt = parseTime(line);

            if (dt > 0) {
                schedule(s.clk + dt, () -> processOperationStep(e, lines, nextStep, resName));
            } else {
                processOperationStep(e, lines, nextStep, resName);
            }

            return;
        }

        /**
         * USE RECURSO FOR TIEMPO
         * Ejemplo: USE INSPECTOR FOR ER(18,3) min
         */
        if (line.startsWith("USE ")) {
            processUseResource(e, cleanLine, lines, nextStep, resName);
            return;
        }

        /**
         * GROUP / COMBINE
         * Ejemplos:
         * GROUP 5 AS LOTE_MOLDES
         * COMBINE 25 AS CAJA_MOLDES
         */
        if (line.startsWith("GROUP ") || line.startsWith("COMBINE ")) {
            boolean ok = processBatchCommand(e, cleanLine);

            if (ok) {
                processOperationStep(e, lines, nextStep, resName);
            } else {
                // Espera hasta que haya suficientes entidades para formar el grupo.
                return;
            }

            return;
        }

        /**
         * UNGROUP
         * Convierte una entidad agrupada nuevamente en unidades individuales.
         */
        if (line.startsWith("UNGROUP")) {
            processUngroupCommand(e);
            processOperationStep(e, lines, nextStep, resName);
            return;
        }

        /**
         * JOIN N ENTIDAD
         */
        if (line.startsWith("JOIN ")) {
            boolean ok = processJoin(e, cleanLine);

            if (ok) {
                processOperationStep(e, lines, nextStep, resName);
            } else {
                // Si todavía no hay entidades suficientes para unir,
                // se reintenta después de 1 minuto simulado.
                schedule(s.clk + 1, () -> processOperationStep(e, lines, stepIndex, resName));
            }

            return;
        }

        /**
         * ACCUM N
         */
        if (line.startsWith("ACCUM ")) {
            boolean ok = processAccum(e, cleanLine);

            if (ok) {
                processOperationStep(e, lines, nextStep, resName);
            } else {
                // La entidad queda esperando hasta que se acumule el grupo.
                return;
            }

            return;
        }

        /**
         * GET recurso
         * Ya se gestionó antes en doOperation.
         */
        if (line.startsWith("GET ")) {
            processOperationStep(e, lines, nextStep, resName);
            return;
        }

        /**
         * FREE recurso
         */
        if (line.startsWith("FREE ")) {
            String freeRes = cleanLine.substring(5).trim();

            Res r = s.res(freeRes);
            if (r != null) {
                r.release(s.clk);
                s.agentReturnHome(freeRes);
            }

            String nextResName = freeRes.equalsIgnoreCase(resName) ? null : resName;

            processOperationStep(e, lines, nextStep, nextResName);
            return;
        }

        /**
         * Asignación de variable.
         */
        if (cleanLine.contains("=")) {
            evaluateVariableAssignment(cleanLine);
            processOperationStep(e, lines, nextStep, resName);
            return;
        }

        /**
         * Línea desconocida: se ignora para no detener la simulación.
         */
        processOperationStep(e, lines, nextStep, resName);
    }


    /**
     * Procesa USE RECURSO FOR TIEMPO.
     * Reserva el recurso, espera el tiempo indicado y luego libera el recurso.
     */
    private void processUseResource(
            Entity e,
            String useLine,
            String[] lines,
            int nextStep,
            String currentResName
    ) {
        try {
            String upper = useLine.toUpperCase();
            int useIdx = upper.indexOf("USE ");
            int forIdx = upper.indexOf(" FOR ");

            if (useIdx < 0 || forIdx < 0) {
                processOperationStep(e, lines, nextStep, currentResName);
                return;
            }

            String resName = useLine.substring(useIdx + 4, forIdx).trim();
            String timeExpr = useLine.substring(forIdx + 5).trim();

            Res r = s.res(resName);

            if (r == null) {
                double dtNoRes = parseTime(timeExpr);
                if (dtNoRes > 0) {
                    schedule(s.clk + dtNoRes, () -> processOperationStep(e, lines, nextStep, currentResName));
                } else {
                    processOperationStep(e, lines, nextStep, currentResName);
                }
                return;
            }

            r.request(s.clk, 0, () -> {
                double dt = parseTime(timeExpr);

                if (dt <= 0) {
                    r.release(s.clk);
                    processOperationStep(e, lines, nextStep, currentResName);
                } else {
                    schedule(s.clk + dt, () -> {
                        r.release(s.clk);
                        processOperationStep(e, lines, nextStep, currentResName);
                    });
                }
            });

        } catch (Exception ex) {
            processOperationStep(e, lines, nextStep, currentResName);
        }
    }

    /**
     * Procesa GROUP / COMBINE.
     * Consume N entidades del mismo tipo en la misma locación y deja una sola
     * entidad representando al grupo. El tipo de salida se toma preferentemente
     * del enrutamiento, porque el TXT de ProModel puede cortar operaciones largas.
     */
    private boolean processBatchCommand(Entity mainEntity, String batchLine) {
        String[] parts = batchLine.trim().split("\\s+");

        if (parts.length < 2) {
            return true;
        }

        int required;

        try {
            required = Integer.parseInt(parts[1].trim());
        } catch (Exception ex) {
            return true;
        }

        String outputType = null;

        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equalsIgnoreCase("AS")) {
                outputType = parts[i + 1].trim();
                break;
            }
        }

        ProModelData.ProcDef route = findRoute(mainEntity.typeName, mainEntity.curLoc);
        if (route != null
                && route.output != null
                && !route.output.trim().isEmpty()
                && !route.output.equalsIgnoreCase(mainEntity.typeName)) {
            outputType = route.output.trim();
        }

        if (outputType == null || outputType.trim().isEmpty()) {
            outputType = mainEntity.typeName;
        }

        outputType = canonicalEntityName(outputType);

        java.util.List<Entity> candidates = new java.util.ArrayList<>();

        for (Entity other : s.activeEntities) {
            if (other == mainEntity) {
                continue;
            }

            if (other.typeName.equalsIgnoreCase(mainEntity.typeName)
                    && other.curLoc != null
                    && sameBaseLoc(other.curLoc, mainEntity.curLoc)
                    && !other.moving) {

                candidates.add(other);

                if (candidates.size() >= required - 1) {
                    break;
                }
            }
        }

        if (candidates.size() < required - 1) {
            return false;
        }

        int totalBatchSize = Math.max(1, mainEntity.batchSize);

        for (Entity other : candidates) {
            totalBatchSize += Math.max(1, other.batchSize);
            removeJoinedEntity(other);
        }

        mainEntity.typeName = outputType;
        mainEntity.currentGraphicId = 1;
        mainEntity.batchSize = totalBatchSize;

        return true;
    }

    /**
     * Corrige nombres truncados por el TXT de ProModel.
     * Ejemplo: CAJA_M -> CAJA_MOLDES, CONTENED -> CONTENEDOR.
     */
    private String canonicalEntityName(String maybeName) {
        if (maybeName == null) {
            return "";
        }

        String clean = maybeName.trim();

        if (s.currentData != null && s.currentData.entities != null) {
            for (ProModelData.EntDef ent : s.currentData.entities) {
                if (ent.name.equalsIgnoreCase(clean)) {
                    return ent.name;
                }
            }

            for (ProModelData.EntDef ent : s.currentData.entities) {
                if (ent.name.toUpperCase().startsWith(clean.toUpperCase())
                        || clean.toUpperCase().startsWith(ent.name.toUpperCase())) {
                    return ent.name;
                }
            }
        }

        return clean;
    }

    /**
     * Procesa UNGROUP.
     * Si la entidad representa un grupo de N piezas, genera N entidades
     * individuales para que el flujo continúe correctamente.
     */
    private void processUngroupCommand(Entity e) {
        int qty = Math.max(1, e.batchSize);

        ProModelData.ProcDef route = findRoute(e.typeName, e.curLoc);
        String outputType = e.typeName;

        if (route != null && route.output != null && !route.output.trim().isEmpty()) {
            outputType = route.output.trim();
        }

        outputType = canonicalEntityName(outputType);

        e.typeName = outputType;
        e.currentGraphicId = 1;
        e.batchSize = 1;

        if (qty <= 1) {
            return;
        }

        Loc loc = s.loc(e.curLoc);
        float x = loc != null ? loc.x + loc.w / 2f : e.curX;
        float y = loc != null ? loc.y + loc.h / 2f : e.curY;

        for (int i = 1; i < qty; i++) {
            Entity extra = new Entity(outputType, e.sysEntryTime);
            extra.curLoc = e.curLoc;
            extra.curX = x;
            extra.curY = y;
            extra.targetX = x;
            extra.targetY = y;
            extra.currentGraphicId = 1;
            extra.batchSize = 1;

            s.activeEntities.add(extra);
            s.enSistema++;
            s.entidadesCreadas.incrementAndGet();

            schedule(s.clk, () -> doRouting(extra));
        }
    }

    /**
     * Procesa JOIN N ENTIDAD.
     * Consume entidades auxiliares que estén en la misma locación.
     */
    private boolean processJoin(Entity mainEntity, String joinLine) {
        String[] parts = joinLine.trim().split("\\s+");

        if (parts.length < 3) {
            return true;
        }

        int qty;
        String joinEntity;

        try {
            qty = Integer.parseInt(parts[1].trim());
            joinEntity = parts[2].trim();
        } catch (Exception ex) {
            return true;
        }

        List<Entity> candidates = new ArrayList<>();

        for (Entity other : s.activeEntities) {
            if (other == mainEntity) {
                continue;
            }

            if (other.typeName.equalsIgnoreCase(joinEntity)
                    && other.curLoc != null
                    && sameBaseLoc(other.curLoc, mainEntity.curLoc)) {
                candidates.add(other);
            }

            if (candidates.size() >= qty) {
                break;
            }
        }

        if (candidates.size() < qty) {
            return false;
        }

        for (Entity joined : candidates) {
            removeJoinedEntity(joined);
        }

        return true;
    }

    /**
     * Elimina una entidad consumida por JOIN.
     */
    private void removeJoinedEntity(Entity e) {
        s.activeEntities.remove(e);
        s.enSistema = Math.max(0, s.enSistema - 1);

        Loc l = s.loc(e.curLoc);
        if (l != null && l.cnt > 0) {
            l.exit(s.clk);
            l.drain();
        }
    }

    /**
     * Procesa ACCUM N.
     * Cada entidad que llega aumenta el contador.
     * Cuando se llega a N, una entidad continúa.
     */
    private boolean processAccum(Entity e, String accumLine) {
        String[] parts = accumLine.trim().split("\\s+");

        if (parts.length < 2) {
            return true;
        }

        int required;

        try {
            required = Integer.parseInt(parts[1].trim());
        } catch (Exception ex) {
            return true;
        }

        String key = e.typeName.toUpperCase() + "@" + baseLoc(e.curLoc).toUpperCase();

        int current = accumCounters.getOrDefault(key, 0) + 1;
        accumCounters.put(key, current);

        if (current < required) {
            return false;
        }

        accumCounters.put(key, 0);

        int toRemove = required - 1;
        int totalBatchSize = Math.max(1, e.batchSize);

        Iterator<Entity> it = s.activeEntities.iterator();

        while (it.hasNext() && toRemove > 0) {
            Entity other = it.next();

            if (other == e) {
                continue;
            }

            if (other.typeName.equalsIgnoreCase(e.typeName)
                    && sameBaseLoc(other.curLoc, e.curLoc)) {

                totalBatchSize += Math.max(1, other.batchSize);

                it.remove();
                s.enSistema = Math.max(0, s.enSistema - 1);

                Loc l = s.loc(other.curLoc);
                if (l != null && l.cnt > 0) {
                    l.exit(s.clk);
                    l.drain();
                }

                toRemove--;
            }
        }

        e.batchSize = totalBatchSize;

        return true;
    }

    /**
     * Evalúa asignaciones simples de variables.
     * Ejemplo:
     * VAR1 = 10
     */
    private void evaluateVariableAssignment(String rawLine) {
        try {
            String[] parts = rawLine.split("=");

            if (parts.length != 2) {
                return;
            }

            String varId = parts[0].trim();
            String valueExpr = parts[1].trim();

            double result;

            String upperValue = valueExpr.toUpperCase().trim();

            if (valueExpr.equalsIgnoreCase("INC")) {
                result = s.globalVariables.getOrDefault(varId, 0.0) + 1.0;
            } else if (upperValue.startsWith("ENTRIES(") && upperValue.endsWith(")")) {
                String locName = valueExpr.substring(valueExpr.indexOf('(') + 1, valueExpr.lastIndexOf(')')).trim();
                Loc loc = s.loc(locName);
                result = loc == null ? 0.0 : loc.totalEntries;
            } else {
                result = Double.parseDouble(valueExpr.replace(",", "."));
            }

            s.globalVariables.put(varId, result);
        } catch (Exception ex) {
            // Ignorar asignaciones no soportadas.
        }
    }

    /**
     * Enruta una entidad hacia su siguiente locación.
     * Esta es la parte corregida para rutas y recursos.
     */
    private void doRouting(Entity e) {
        String oldLoc = e.curLoc;
        ProModelData.ProcDef route = findRoute(e.typeName, e.curLoc);

        if (route == null
                || route.destination == null
                || route.destination.trim().isEmpty()
                || route.destination.equalsIgnoreCase("EXIT")) {

            freeLoc(oldLoc);
            finishEntity(e);
            return;
        }

        /**
         * Cambio de entidad.
         * Ejemplo: GRANOS_CEBADA + LUPULO -> MOSTO.
         */
        if (route.output != null && !route.output.trim().isEmpty()) {
            if (!route.output.equalsIgnoreCase(e.typeName)) {
                e.typeName = route.output.trim();
                e.currentGraphicId = 1;
            }
        }

        String newLoc = resolveDest(route.destination.trim());

        String resourceName = extractResourceName(route.moveLogic);
        if (resourceName != null) {
            resourceName = s.canonicalResourceKey(resourceName);
        }

        final String movingResourceName = resourceName;

        /*
         * Si el enrutamiento usa MOVE WITH, primero se reserva el recurso.
         * Antes, 50 entidades podían mandar el mismo montacargas al mismo tiempo
         * y la posición visual del recurso se sobrescribía. Por eso parecía que
         * el montacargas no llevaba nada o saltaba hacia otros lados.
         */
        Res routeResource = movingResourceName == null ? null : s.res(movingResourceName);

        if (routeResource != null) {
            routeResource.request(s.clk, 0, () -> startRoutingMovement(e, route, oldLoc, newLoc, movingResourceName, true));
        } else {
            startRoutingMovement(e, route, oldLoc, newLoc, movingResourceName, false);
        }
    }

    /**
     * Ejecuta el movimiento visual de una entidad y, si corresponde, del recurso.
     */
    private void startRoutingMovement(
            Entity e,
            ProModelData.ProcDef route,
            String oldLoc,
            String newLoc,
            String movingResourceName,
            boolean releaseResourceAtArrival
    ) {
        Loc originLoc = s.locs.get(oldLoc);
        Loc destLoc = s.locs.get(newLoc);

        e.curLoc = newLoc;

        double travelTime = calculateTravelTime(route.moveLogic, oldLoc, newLoc);

        if (travelTime <= 0) {
            travelTime = calculateEntityTravelTime(e.typeName, oldLoc, newLoc);
        }

        if (travelTime <= 0) {
            travelTime = 1.0;
        }

        travelTime = Math.max(0.8, travelTime);

        if (originLoc != null && destLoc != null) {
            e.curX = originLoc.x + originLoc.w / 2f;
            e.curY = originLoc.y + originLoc.h / 2f;

            e.targetX = destLoc.x + destLoc.w / 2f;
            e.targetY = destLoc.y + destLoc.h / 2f;

            e.moveStartTime = s.clk;
            e.moveEndTime = s.clk + travelTime;
            e.moving = true;
        }

        if (movingResourceName != null && originLoc != null && destLoc != null) {
            float ox = originLoc.x + originLoc.w / 2f;
            float oy = originLoc.y + originLoc.h / 2f;

            float dx = destLoc.x + destLoc.w / 2f;
            float dy = destLoc.y + destLoc.h / 2f;

            s.agentGoFromTimed(
                    movingResourceName,
                    ox,
                    oy,
                    dx,
                    dy,
                    s.clk,
                    s.clk + travelTime
            );
        }

        freeLoc(oldLoc);

        schedule(s.clk + travelTime, () -> {
            e.moving = false;
            e.curX = e.targetX;
            e.curY = e.targetY;

            if (movingResourceName != null) {
                if (route.moveLogic != null
                        && route.moveLogic.toUpperCase().contains("THEN FREE")) {
                    if (releaseResourceAtArrival) {
                        Res rr = s.res(movingResourceName);
                        if (rr != null) {
                            rr.release(s.clk);
                        }
                    }
                    s.agentReturnHome(movingResourceName);
                } else {
                    Loc arrived = s.locs.get(e.curLoc);

                    if (arrived != null) {
                        float ax = arrived.x + arrived.w / 2f;
                        float ay = arrived.y + arrived.h / 2f;

                        s.agentGoFrom(movingResourceName, ax, ay, ax, ay);
                    }
                }
            }

            tryEnter(e);
        });
    }

    /**
     * Extrae el nombre del recurso desde:
     * MOVE WITH OPERADOR_CEBADA Then Free
     */
    private String extractResourceName(String moveLogic) {
        if (moveLogic == null) {
            return null;
        }

        String upper = moveLogic.toUpperCase();
        int idx = upper.indexOf("MOVE WITH");

        if (idx < 0) {
            return null;
        }

        String rest = moveLogic.substring(idx + "MOVE WITH".length()).trim();

        if (rest.isEmpty()) {
            return null;
        }

        String[] parts = rest.split("\\s+");

        if (parts.length == 0) {
            return null;
        }

        return parts[0].trim();
    }

    /**
     * Busca la definición del recurso importado desde ProModel.
     */
    private ProModelData.ResDef findResourceDef(String resourceName) {
        if (resourceName == null || s.currentData == null) {
            return null;
        }

        for (ProModelData.ResDef r : s.currentData.resources) {
            if (r.name.equalsIgnoreCase(resourceName)) {
                return r;
            }
        }

        return null;
    }

    /**
     * Calcula tiempo usando:
     * tiempo = distancia / velocidad
     */
    private double calculateTravelTime(String moveLogic, String fromLoc, String toLoc) {
        String resourceName = extractResourceName(moveLogic);

        if (resourceName == null) {
            return 0;
        }

        ProModelData.ResDef resDef = findResourceDef(resourceName);

        if (resDef == null) {
            return 0;
        }

        double speed = getResourceSpeed(resDef, true);

        if (speed <= 0) {
            speed = 100.0;
        }

        double distance = getRouteDistance(resDef, fromLoc, toLoc);

        /*
         * Fallback crítico:
         * si la red/interfaz no encuentra distancia, usamos la distancia visual
         * entre locaciones. Esto evita recursos quietos o movimientos casi nulos.
         */
        if (distance <= 0) {
            distance = getGraphicDistanceAsPromodelDistance(fromLoc, toLoc);
        }

        if (distance > 0 && speed > 0) {
            return distance / speed;
        }

        return 0;
    }

    /**
     * Movimiento por defecto cuando no hay MOVE WITH.
     * Usa velocidad de entidad y distancia gráfica aproximada.
     */
    private double calculateEntityTravelTime(String entityName, String fromLoc, String toLoc) {
        if (s.currentData == null) {
            return 0;
        }

        ProModelData.EntDef entDef = null;

        for (ProModelData.EntDef e : s.currentData.entities) {
            if (e.name.equalsIgnoreCase(entityName)) {
                entDef = e;
                break;
            }
        }

        if (entDef == null || entDef.speed == null || entDef.speed.trim().isEmpty()) {
            return 0;
        }

        double speed;

        try {
            speed = Double.parseDouble(entDef.speed.replace(",", ".").trim());
        } catch (Exception ex) {
            return 0;
        }

        if (speed <= 0) {
            return 0;
        }

        Loc a = s.locs.get(fromLoc);
        Loc b = s.locs.get(toLoc);

        if (a == null || b == null) {
            return 0;
        }

        double ax = a.x + a.w / 2.0;
        double ay = a.y + a.h / 2.0;

        double bx = b.x + b.w / 2.0;
        double by = b.y + b.h / 2.0;

        double pixelDistance = Math.sqrt(Math.pow(bx - ax, 2) + Math.pow(by - ay, 2));

        /**
         * Conversión aproximada para que no sea instantáneo.
         */
        double approximatePromodelDistance = pixelDistance / 10.0;
        double t = approximatePromodelDistance / speed;

        return Math.max(0.8, t);
    }

    /**
     * Distancia visual aproximada entre locaciones.
     * Se usa cuando no se puede calcular la distancia real por red/interfaz.
     */
    private double getGraphicDistanceAsPromodelDistance(String fromLoc, String toLoc) {
        Loc a = s.locs.get(fromLoc);
        Loc b = s.locs.get(toLoc);

        if (a == null || b == null) {
            return 0;
        }

        double ax = a.x + a.w / 2.0;
        double ay = a.y + a.h / 2.0;

        double bx = b.x + b.w / 2.0;
        double by = b.y + b.h / 2.0;

        double pixelDistance = Math.sqrt(
                Math.pow(bx - ax, 2)
                        + Math.pow(by - ay, 2)
        );

        return pixelDistance / 10.0;
    }

    /**
     * Obtiene velocidad del recurso desde:
     * Vacío: 100 Ppm
     * Lleno: 100 Ppm
     */
    private double getResourceSpeed(ProModelData.ResDef resDef, boolean loaded) {
        if (resDef == null || resDef.moveLogic == null) {
            return 100.0;
        }

        String key = loaded ? "LLENO:" : "VACÍO:";
        String key2 = loaded ? "LLENO:" : "VACIO:";

        String[] lines = resDef.moveLogic.split("\\n");

        for (String line : lines) {
            String upper = line.toUpperCase();

            if (upper.contains(key) || upper.contains(key2)) {
                String clean = upper
                        .replace(key, "")
                        .replace(key2, "")
                        .replace("PPM", "")
                        .trim();

                try {
                    return Double.parseDouble(clean.replace(",", "."));
                } catch (Exception ex) {
                    return 100.0;
                }
            }
        }

        return 100.0;
    }

    /**
     * Obtiene distancia entre dos locaciones usando:
     * recurso -> red -> interfaces -> segmentos.
     */
    private double getRouteDistance(ProModelData.ResDef resDef, String fromLoc, String toLoc) {
        if (resDef == null || resDef.pathNetwork == null || s.currentData == null) {
            return 0;
        }

        String network = resDef.pathNetwork.trim();

        if (network.isEmpty()) {
            return 0;
        }

        String fromNode = null;
        String toNode = null;

        for (ProModelData.InterfaceDef inf : s.currentData.interfaces) {
            if (inf.network.equalsIgnoreCase(network)) {
                if (sameBaseLoc(inf.location, fromLoc)) {
                    fromNode = inf.node;
                }

                if (sameBaseLoc(inf.location, toLoc)) {
                    toNode = inf.node;
                }
            }
        }

        if (fromNode == null || toNode == null) {
            return 0;
        }

        /**
         * Buscar conexión directa o inversa.
         */
        for (ProModelData.RouteSegDef seg : s.currentData.routeSegments) {
            if (seg.network.equalsIgnoreCase(network)) {
                boolean direct = seg.fromNode.equalsIgnoreCase(fromNode)
                        && seg.toNode.equalsIgnoreCase(toNode);

                boolean reverse = seg.fromNode.equalsIgnoreCase(toNode)
                        && seg.toNode.equalsIgnoreCase(fromNode);

                if (direct || reverse) {
                    return seg.distance;
                }
            }
        }

        /**
         * Si no hay conexión directa, busca distancia por camino simple.
         * Esto sirve para RED_CEBADA: N1 -> N2 -> N3.
         */
        return getPathDistance(network, fromNode, toNode);
    }

    /**
     * Calcula distancia por una red lineal simple.
     */
    private double getPathDistance(String network, String fromNode, String toNode) {
        if (s.currentData == null) {
            return 0;
        }

        List<ProModelData.RouteSegDef> segments = new ArrayList<>();

        for (ProModelData.RouteSegDef seg : s.currentData.routeSegments) {
            if (seg.network.equalsIgnoreCase(network)) {
                segments.add(seg);
            }
        }

        if (segments.isEmpty()) {
            return 0;
        }

        double direct = dfsDistance(segments, fromNode, toNode, new ArrayList<>());

        if (direct > 0) {
            return direct;
        }

        return 0;
    }

    /**
     * DFS pequeño para redes simples.
     */
    private double dfsDistance(
            List<ProModelData.RouteSegDef> segments,
            String currentNode,
            String targetNode,
            List<String> visited) {

        if (currentNode.equalsIgnoreCase(targetNode)) {
            return 0;
        }

        visited.add(currentNode.toUpperCase());

        for (ProModelData.RouteSegDef seg : segments) {
            String next = null;

            if (seg.fromNode.equalsIgnoreCase(currentNode)) {
                next = seg.toNode;
            } else if (seg.toNode.equalsIgnoreCase(currentNode)) {
                next = seg.fromNode;
            }

            if (next == null) {
                continue;
            }

            if (visited.contains(next.toUpperCase())) {
                continue;
            }

            double d = dfsDistance(segments, next, targetNode, visited);

            if (d >= 0 && (next.equalsIgnoreCase(targetNode) || d > 0)) {
                return seg.distance + d;
            }
        }

        return -1;
    }

    /**
     * Libera una locación.
     */
    private void freeLoc(String locName) {
        Loc l = s.loc(locName);

        if (l != null) {
            l.exit(s.clk);
            l.drain();
        }
    }

    /**
     * Termina una entidad que sale del sistema.
     */
    private void finishEntity(Entity e) {
        s.activeEntities.remove(e);
        s.enSistema = Math.max(0, s.enSistema - 1);
        s.entidadesSalientes.incrementAndGet();
        s.histThroughput.add(new double[] {s.clk, s.entidadesSalientes.get()});
    }

    /**
     * Busca operación para entidad + locación.
     */
    private ProModelData.ProcDef findOperation(String entityName, String locName) {
        String baseLocName = baseLoc(locName);

        for (ProModelData.ProcDef p : s.routes) {
            if (p.entity.equalsIgnoreCase(entityName)
                    && p.location.equalsIgnoreCase(baseLocName)
                    && p.operation != null
                    && !p.operation.trim().isEmpty()) {
                return p;
            }
        }

        return null;
    }

    /**
     * Busca ruta para entidad + locación.
     */
    private ProModelData.ProcDef findRoute(String entityName, String locName) {
        String baseLocName = baseLoc(locName);

        /**
         * Primero busca coincidencia exacta entidad + locación.
         */
        for (ProModelData.ProcDef p : s.routes) {
            if (p.entity.equalsIgnoreCase(entityName)
                    && p.location.equalsIgnoreCase(baseLocName)
                    && p.destination != null
                    && !p.destination.trim().isEmpty()) {

                return chooseByRuleOrProbability(entityName, baseLocName);
            }
        }

        /**
         * Fallback por locación.
         */
        for (ProModelData.ProcDef p : s.routes) {
            if (p.location.equalsIgnoreCase(baseLocName)
                    && p.destination != null
                    && !p.destination.trim().isEmpty()) {
                return p;
            }
        }

        return null;
    }

    /**
     * Maneja rutas con probabilidad.
     * Ejemplo:
     * CERVEZA INSPECCION -> EMBOTELLADO 0.90
     * CERVEZA INSPECCION -> DESCARTE 0.10
     */
    private ProModelData.ProcDef chooseByRuleOrProbability(String entityName, String locName) {
        List<ProModelData.ProcDef> candidates = new ArrayList<>();

        for (ProModelData.ProcDef p : s.routes) {
            if (p.entity.equalsIgnoreCase(entityName)
                    && p.location.equalsIgnoreCase(locName)
                    && p.destination != null
                    && !p.destination.trim().isEmpty()) {
                candidates.add(p);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        double r = Math.random();
        double acc = 0;

        for (ProModelData.ProcDef p : candidates) {
            double prob = parseProbability(p.rule);

            if (prob > 0 && prob <= 1) {
                acc += prob;

                if (r <= acc) {
                    return p;
                }
            }
        }

        return candidates.get(0);
    }

    /**
     * Interpreta probabilidad desde regla tipo:
     * 0.90 1
     * 0.10
     * FIRST 1
     */
    private double parseProbability(String rule) {
        if (rule == null) {
            return 0;
        }

        String[] parts = rule.trim().split("\\s+");

        if (parts.length == 0) {
            return 0;
        }

        try {
            return Double.parseDouble(parts[0].replace(",", "."));
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Resuelve locaciones con unidades múltiples.
     */
    private String resolveDest(String destName) {
        if (destName == null) {
            return "";
        }

        destName = destName.trim();

        if (s.locs.containsKey(destName)) {
            return destName;
        }

        List<String> units = new ArrayList<>();

        for (String k : s.locs.keySet()) {
            if (k.startsWith(destName + ".")) {
                units.add(k);
            }
        }

        if (units.isEmpty()) {
            return destName;
        }

        String best = units.get(0);
        int minLoad = Integer.MAX_VALUE;

        for (String u : units) {
            Loc l = s.locs.get(u);
            int load = l.cnt + l.waiting.size();

            if (load < minLoad) {
                minLoad = load;
                best = u;
            }
        }

        List<String> tied = new ArrayList<>();

        for (String u : units) {
            Loc l = s.locs.get(u);
            int load = l.cnt + l.waiting.size();

            if (load == minLoad) {
                tied.add(u);
            }
        }

        if (tied.size() > 1) {
            return tied.get((int) (Math.random() * tied.size()));
        }

        return best;
    }

    /**
     * Quita sufijos de unidades:
     * LOC.1 -> LOC
     */
    private String baseLoc(String locName) {
        if (locName == null) {
            return "";
        }

        if (locName.matches(".*\\.\\d+$")) {
            return locName.substring(0, locName.lastIndexOf('.'));
        }

        return locName;
    }

    /**
     * Compara locaciones ignorando unidad.
     */
    private boolean sameBaseLoc(String a, String b) {
        return baseLoc(a).equalsIgnoreCase(baseLoc(b));
    }

    /**
     * Agenda evento.
     */
    private void schedule(double t, Runnable a) {
        s.schedule(t, a);
>>>>>>> Stashed changes
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
        });
    }

    // ── INSPECCION_1 → 80% EMPAQUE | 20% INSPECCION_2 ────────────────────
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

    // ── EMPAQUE → EMBARQUE (T3 lleva la pieza) ────────────────────────────
    private void doneEmpaque(Entity e) {
        e.type = EType.PIEZA_FINAL;
        loc("EMPAQUE").exit(s.clk);
        loc("EMPAQUE").drain();
        float[] tgt = locCenter("EMBARQUE");
        res("T3").request(s.clk, p.t3Traslado, () -> {
            s.agentGoTo("T3", tgt[0], tgt[1]);
            e.curLoc = "transit→EMBARQUE";
            schedule(s.clk + p.t3Traslado, () -> {
                res("T3").release(s.clk);
                s.agentReturnHome("T3");
                arrivedAt(e, "EMBARQUE");
            });
        });
    }

    // ── EMBARQUE → EXIT ───────────────────────────────────────────────────
    private void doneEmbarque(Entity e) {
        loc("EMBARQUE").exit(s.clk);
        loc("EMBARQUE").drain();
        s.piezasFinales.incrementAndGet();
        s.embarqueTotales.incrementAndGet();   // ← NUEVO: contador llegados a EMBARQUE
        s.enSistema--;
        s.histThroughput.add(new double[]{s.clk, s.piezasFinales.get()});
    }

    // ── Ruteo generico ─────────────────────────────────────────────────────
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
