import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * SimModel - Clases del modelo de datos.
 * Incluye: Rng, Entity, Loc, Res, Ev, SimState
 */

// ─────────────────────────────────────────────────────────────────────────────
// GENERADOR DE NÚMEROS ALEATORIOS
// ─────────────────────────────────────────────────────────────────────────────
class Rng {
    private final Random r;

    Rng(long seed) {
        r = new Random(seed);
    }

    /** Exponencial: -media * ln(U), U ~ Uniform(0,1) */
    public double exp(double mean) {
        double u = r.nextDouble();
        return u == 0 ? mean : -mean * Math.log(u);
    }

    /** Normal usando Box-Muller */
    public double norm(double mean, double sigma) {
        double u1;
        double u2;

        do {
            u1 = r.nextDouble();
            u2 = r.nextDouble();
        } while (u1 == 0);

        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);

        return Math.max(0.001, mean + sigma * z);
    }

    /** Bernoulli: true con probabilidad p */
    public boolean prob(double p) {
        return r.nextDouble() < p;
    }

    public double uniform(double mean, double halfRange) {
        return mean + (r.nextDouble() * 2.0 - 1.0) * halfRange;
    }

    public double triangular(double min, double mode, double max) {
        double u = r.nextDouble();

        if (max <= min) {
            return min;
        }

        double c = (mode - min) / (max - min);

        if (u <= c) {
            return min + Math.sqrt(u * (max - min) * (mode - min));
        } else {
            return max - Math.sqrt((1.0 - u) * (max - min) * (max - mode));
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD
// ─────────────────────────────────────────────────────────────────────────────
class Entity {
    private static final AtomicInteger SEQ = new AtomicInteger(1);

    public static void resetIds() {
        SEQ.set(1);
    }

    public final int id;
    public String typeName = "";
    public double sysEntryTime;
    public String curLoc;

    /**
     * ID del gráfico activo.
     * 1 = gráfico por defecto.
     */
    public volatile int currentGraphicId = 1;

    // Animación visual
    public float curX;
    public float curY;
    public float targetX;
    public float targetY;

    public double moveStartTime = -1;
    public double moveEndTime = -1;

    public boolean moving = false;

    /**
     * Cantidad de piezas representadas por esta entidad.
     * Ejemplo:
     * - MOLDE normal = 1
     * - LOTE_MOLDES formado por GROUP 5 = 5
     * - Carga del horno por ACCUM 4 lotes = 20
     */
    public int batchSize = 1;

    public Entity(String typeName, double now) {
        this.id = SEQ.getAndIncrement();
        this.typeName = typeName;
        this.sysEntryTime = now;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOCACIÓN
// ─────────────────────────────────────────────────────────────────────────────
class Loc {
    public final String name;
    public final int cap;
    public final LType type;

    public int cnt = 0;
    public int processed = 0;
    public int totalEntries = 0;
    public int maxCnt = 0;

    public double busyTime = 0;
    public double busyStart = 0;

    /**
     * Para promedio de contenido:
     * sumContentTime = sumatoria de cnt * dt.
     */
    public double sumContentTime = 0;
    public double lastContentUpd = 0;

    /**
     * Cola de acciones pendientes para entrar a esta locación.
     */
    public final Deque<Runnable> waiting = new ArrayDeque<>();

    // Datos visuales
    public int x;
    public int y;
    public int w;
    public int h;

    public String iconPath = null;

    public boolean showCounter = false;
    public String counterType = "Contenido Actual";
    public boolean showGauge = false;

    public Loc(String name, int cap, LType type, int x, int y, int w, int h) {
        this.name = name;
        this.cap = cap;
        this.type = type;

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean full() {
        return cnt >= cap;
    }

    private void updateAvg(double clk) {
        sumContentTime += cnt * (clk - lastContentUpd);
        lastContentUpd = clk;
    }

    public void enter(double clk) {
        updateAvg(clk);

        if (cnt == 0) {
            busyStart = clk;
        }

        cnt++;
        totalEntries++;

        if (cnt > maxCnt) {
            maxCnt = cnt;
        }
    }

    public void exit(double clk) {
        updateAvg(clk);

        if (cnt > 0) {
            cnt--;
        }

        processed++;

        if (cnt == 0) {
            busyTime += clk - busyStart;
        }
    }

    /**
     * Intenta dejar entrar entidades en espera.
     */
    public void drain() {
        while (!waiting.isEmpty() && !full()) {
            waiting.poll().run();
        }
    }

    /**
     * Utilización en tiempo real:
     * contenido promedio / capacidad.
     */
    public double utilLive(double clk, double total) {
        if (total <= 0 || cap <= 0 || cap == Integer.MAX_VALUE) {
            return 0;
        }

        double currentSum = sumContentTime + cnt * (clk - lastContentUpd);
        double avgCont = currentSum / total;

        return Math.min(100.0, (avgCont / cap) * 100.0);
    }

    /**
     * Contenido promedio ponderado en el tiempo.
     */
    public double avgContents(double clk, double totalTime) {
        if (totalTime <= 0) {
            return 0;
        }

        double currentSum = sumContentTime + cnt * (clk - lastContentUpd);

        return currentSum / totalTime;
    }

    /**
     * Tiempo promedio por entrada usando Ley de Little:
     * W = sum(L*dt) / Entradas.
     */
    public double avgTimePerEntry(double clk) {
        if (totalEntries == 0) {
            return 0;
        }

        double currentSum = sumContentTime + cnt * (clk - lastContentUpd);

        return currentSum / totalEntries;
    }

    public int waitingCount() {
        return waiting.size();
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECURSO
// ─────────────────────────────────────────────────────────────────────────────
class Res {
    public final String name;

    /**
     * Capacidad total del recurso.
     * Ejemplo:
     * OPERADOR 2 -> capacity = 2.
     */
    public int capacity = 1;

    /**
     * Unidades disponibles actualmente.
     */
    public int available = 1;

    public int timesUsed = 0;

    public double workTime = 0;
    public double travelTime = 0;
    public double workStart = 0;

    private final Deque<Runnable> queue = new ArrayDeque<>();

    public double sumBusyTime = 0;
    public double lastBusyUpd = 0;

    public Res(String name) {
        this.name = name;
    }

    public Res(String name, int cap) {
        this.name = name;
        this.capacity = Math.max(1, cap);
        this.available = this.capacity;
    }

    public boolean busy() {
        return available <= 0;
    }

    public void reset() {
        available = capacity;
        timesUsed = 0;

        workTime = 0;
        travelTime = 0;
        workStart = 0;

        queue.clear();

        sumBusyTime = 0;
        lastBusyUpd = 0;
    }

    private void updateAvg(double clk) {
        int inUse = capacity - available;
        sumBusyTime += inUse * (clk - lastBusyUpd);
        lastBusyUpd = clk;
    }

    /**
     * Solicita una unidad del recurso.
     */
    public void request(double clk, double travel, Runnable action) {
        if (available > 0) {
            updateAvg(clk);

            available--;
            travelTime += travel;
            timesUsed++;

            action.run();
        } else {
            final double trvl = travel;

            queue.offer(() -> {
                updateAvg(clk);

                available--;
                travelTime += trvl;
                timesUsed++;

                action.run();
            });
        }
    }

    /**
     * Libera una unidad del recurso.
     */
    public void release(double clk) {
        updateAvg(clk);

        Runnable next = queue.poll();

        if (next != null) {
            next.run();
        } else {
            available++;
        }

        if (available > capacity) {
            available = capacity;
        }
    }

    /**
     * Utilización:
     * promedio de unidades en uso / capacidad.
     */
    public double utilPct(double clk, double totalTime) {
        if (totalTime <= 0 || capacity <= 0) {
            return 0;
        }

        double currentSum = sumBusyTime + (capacity - available) * (clk - lastBusyUpd);
        double avgInUse = currentSum / totalTime;

        return Math.min(100.0, (avgInUse / capacity) * 100.0);
    }

    /**
     * Tiempo promedio por uso.
     */
    public double avgTimePerUse(double clk) {
        if (timesUsed == 0) {
            return 0;
        }

        double currentSum = sumBusyTime + (capacity - available) * (clk - lastBusyUpd);

        return currentSum / timesUsed;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EVENTO
// ─────────────────────────────────────────────────────────────────────────────
class Ev implements Comparable<Ev> {
    private static final AtomicInteger SEQ = new AtomicInteger(0);

    public static void reset() {
        SEQ.set(0);
    }

    public final double time;
    public final int seq;
    public final Runnable action;

    public Ev(double time, Runnable action) {
        this.time = time;
        this.seq = SEQ.getAndIncrement();
        this.action = action;
    }

    @Override
    public int compareTo(Ev o) {
        int c = Double.compare(time, o.time);

        if (c != 0) {
            return c;
        }

        return Integer.compare(seq, o.seq);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADO GLOBAL DE SIMULACIÓN
// ─────────────────────────────────────────────────────────────────────────────
public class SimState {

    // ─────────────────────────────────────────────────────────────────────
    // Parámetros y aleatoriedad
    // ─────────────────────────────────────────────────────────────────────

    public final SimParams params;
    public final Rng rng;

    // ─────────────────────────────────────────────────────────────────────
    // Datos importados desde ProModel
    // ─────────────────────────────────────────────────────────────────────

    public ProModelData currentData;

    /**
     * Lista de procesos/rutas importadas desde la sección Procesamiento.
     */
    public final List<ProModelData.ProcDef> routes = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────
    // Reloj y estados de ejecución
    // ─────────────────────────────────────────────────────────────────────

    public double clk = 0.0;

    public boolean running = false;
    public boolean paused = false;
    public boolean finished = false;

    /**
     * Multiplicador de velocidad visual.
     */
    public double speedMult = 1.0;

    // ─────────────────────────────────────────────────────────────────────
    // Estructuras principales
    // ─────────────────────────────────────────────────────────────────────

    public final Map<String, Loc> locs = new LinkedHashMap<>();

    public final Map<String, Res> res = new LinkedHashMap<>();

    public final List<Entity> activeEntities =
            Collections.synchronizedList(new ArrayList<>());

    public final PriorityQueue<Ev> fel = new PriorityQueue<>();

    public final Map<String, Double> globalVariables = new LinkedHashMap<>();

    // ─────────────────────────────────────────────────────────────────────
    // Contadores generales
    // ─────────────────────────────────────────────────────────────────────

    public final AtomicInteger entidadesCreadas = new AtomicInteger(0);
    public final AtomicInteger entidadesSalientes = new AtomicInteger(0);
    public final AtomicInteger totalProcesadas = new AtomicInteger(0);

    public int enSistema = 0;

    /**
     * Historial de throughput.
     * Cada posición guarda:
     * [0] = tiempo
     * [1] = salidas acumuladas.
     */
    public final List<double[]> histThroughput =
            Collections.synchronizedList(new ArrayList<>());

    // ─────────────────────────────────────────────────────────────────────
    // Agentes visuales de recursos
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Mapa de agentes visuales.
     *
     * Llave:
     * nombre del recurso.
     *
     * Arreglo:
     * [0] = homeX
     * [1] = homeY
     * [2] = fromX / currentX base
     * [3] = fromY / currentY base
     * [4] = targetX
     * [5] = targetY
     * [6] = moving 0/1
     * [7] = startTime
     * [8] = endTime
     */
    public final Map<String, float[]> resAgents = new LinkedHashMap<>();

    // ─────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────

    public SimState(SimParams p) {
        this.params = p;
        this.rng = new Rng(p.semilla);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Cargar datos desde ProModelData
    // ─────────────────────────────────────────────────────────────────────

    public void loadFromData(ProModelData data) {
        this.currentData = data;

        locs.clear();
        res.clear();
        resAgents.clear();
        routes.clear();
        activeEntities.clear();
        fel.clear();
        globalVariables.clear();
        histThroughput.clear();

        clk = 0.0;
        running = false;
        paused = false;
        finished = false;
        enSistema = 0;

        entidadesCreadas.set(0);
        entidadesSalientes.set(0);
        totalProcesadas.set(0);

        Ev.reset();
        Entity.resetIds();

        if (data == null) {
            return;
        }

        data.resolveResourceHomes();

        if (data.processing != null) {
            routes.addAll(data.processing);
        }

        loadLocations(data);
        loadVariables(data);
        loadResources(data);
    }

    /**
     * Carga locaciones importadas.
     */
    private void loadLocations(ProModelData data) {
        int defaultX = 50;
        int defaultY = 50;

        if (data.locations == null) {
            return;
        }

        for (ProModelData.LocDef d : data.locations) {
            int cap = data.parseMacroInteger(d.cap, 1);
            LType type = inferLocationType(d.name);

            if (d.x == -1) {
                d.x = defaultX;
                d.y = defaultY;

                defaultX += 150;

                if (defaultX > 900) {
                    defaultX = 50;
                    defaultY += 110;
                }
            }

            int units = parseUnits(d.units);

            if (units <= 1 || d.name.matches(".*\\.\\d+$")) {
                addL(d.name, cap, type, d.x, d.y, d.w, d.h);

                Loc loc = locs.get(d.name);

                if (loc != null) {
                    copyVisualConfigToLoc(d, loc);
                }
            } else {
                for (int i = 1; i <= units; i++) {
                    String unitName = d.name + "." + i;

                    int offsetX = d.x + ((i - 1) * 25);
                    int offsetY = d.y + ((i - 1) * 25);

                    addL(unitName, cap, type, offsetX, offsetY, d.w, d.h);

                    Loc loc = locs.get(unitName);

                    if (loc != null) {
                        copyVisualConfigToLoc(d, loc);
                    }
                }
            }
        }
    }

    /**
     * Copia configuración visual desde LocDef a Loc.
     */
    private void copyVisualConfigToLoc(ProModelData.LocDef d, Loc loc) {
        loc.iconPath = d.iconPath;
        loc.showCounter = d.showCounter;
        loc.counterType = d.counterType;
        loc.showGauge = d.showGauge;
    }

    /**
     * Carga variables globales.
     */
    private void loadVariables(ProModelData data) {
        globalVariables.clear();

        if (data.variables == null) {
            return;
        }

        for (ProModelData.VarDef v : data.variables) {
            try {
                globalVariables.put(
                        v.id,
                        Double.parseDouble(v.initialValue.replace(",", ".").trim())
                );
            } catch (Exception e) {
                globalVariables.put(v.id, 0.0);
            }
        }
    }

    /**
     * Carga recursos y crea su agente visual en su Home.
     */
    private void loadResources(ProModelData data) {
        if (data.resources == null) {
            return;
        }

        for (ProModelData.ResDef r : data.resources) {
            if (r == null || r.name == null || r.name.trim().isEmpty()) {
                continue;
            }

            int cap = data.parseMacroInteger(r.units, 1);

            res.put(r.name, new Res(r.name, cap));

            float hx = 50;
            float hy = 50;

            String homeLocName = findHomeLocationForResource(data, r);

            if (homeLocName != null && !homeLocName.trim().isEmpty()) {
                Loc homeLoc = loc(homeLocName);

                if (homeLoc != null) {
                    hx = homeLoc.x + homeLoc.w / 2f;
                    hy = homeLoc.y + homeLoc.h / 2f;
                }
            } else if (!locs.isEmpty()) {
                Loc first = locs.values().iterator().next();
                hx = first.x + first.w / 2f;
                hy = first.y + first.h / 2f;
            }

            addAgent(r.name, hx, hy);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Parseos e inferencias
    // ─────────────────────────────────────────────────────────────────────

    private int parseCapacity(String capText) {
        if (capText == null) {
            return 1;
        }

        String c = capText.trim();

        if (c.isEmpty()) {
            return 1;
        }

        if (c.equalsIgnoreCase("INF")
                || c.equalsIgnoreCase("INFINITE")
                || c.equalsIgnoreCase("INFINITA")
                || c.equalsIgnoreCase("INFINITO")) {
            return Integer.MAX_VALUE;
        }

        try {
            return Math.max(1, Integer.parseInt(c));
        } catch (Exception e) {
            return 1;
        }
    }

    private int parseUnits(String unitsText) {
        if (unitsText == null || unitsText.trim().isEmpty()) {
            return 1;
        }

        try {
            return Math.max(1, Integer.parseInt(unitsText.trim()));
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Determina tipo visual de locación según el nombre.
     */
    private LType inferLocationType(String name) {
        if (name == null) {
            return LType.MAQUINA;
        }

        String n = name.toUpperCase();

        if (n.contains("CONVEYOR")) {
            return LType.CONVEYOR;
        }

        if (n.contains("SILO")
                || n.contains("ALMACEN")
                || n.contains("ALMACENAJE")
                || n.contains("ALAMCEN")
                || n.contains("FILA")) {
            return LType.ALMACEN;
        }

        if (n.contains("INSPECCION")
                || n.contains("CONTROL")
                || n.contains("MESA")) {
            return LType.INSPECCION;
        }

        if (n.contains("EMPACADO")
                || n.contains("EMBOTELLADO")
                || n.contains("ETIQUETADO")) {
            return LType.EMPAQUE;
        }

        if (n.contains("MERCADO")
                || n.contains("SALIDA")
                || n.contains("EMBARQUE")) {
            return LType.EMBARQUE;
        }

        return LType.MAQUINA;
    }


    /**
     * Busca la clave real de un recurso sin importar mayúsculas/minúsculas.
     * Ejemplo: "Montacargas" -> "MONTACARGAS".
     */
    public String canonicalResourceKey(String key) {
        if (key == null) {
            return null;
        }

        if (resAgents.containsKey(key) || res.containsKey(key)) {
            return key;
        }

        for (String k : resAgents.keySet()) {
            if (k.equalsIgnoreCase(key.trim())) {
                return k;
            }
        }

        for (String k : res.keySet()) {
            if (k.equalsIgnoreCase(key.trim())) {
                return k;
            }
        }

        return key;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Recursos visuales
    // ─────────────────────────────────────────────────────────────────────

    private void addAgent(String key, float hx, float hy) {
        if (key == null || key.trim().isEmpty()) {
            return;
        }

        resAgents.put(key, new float[]{
                hx, hy,     // home
                hx, hy,     // from/current base
                hx, hy,     // target
                0f,         // moving
                0f, 0f      // startTime, endTime
        });
    }

    /**
     * Mueve un agente desde un punto hacia otro.
     * Compatible con SimEngine anterior.
     */
    public void agentGoFrom(String key, float fx, float fy, float tx, float ty) {
        agentGoFromTimed(key, fx, fy, tx, ty, clk, clk + 1.0);
    }

    /**
     * Mueve un agente desde un punto hacia otro con duración real.
     *
     * Este método es importante para que los recursos no salten de golpe.
     */
    public void agentGoFromTimed(
            String key,
            float fx,
            float fy,
            float tx,
            float ty,
            double startTime,
            double endTime
    ) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return;
        }

        if (endTime <= startTime) {
            endTime = startTime + 0.8;
        }

        a[2] = fx;
        a[3] = fy;
        a[4] = tx;
        a[5] = ty;
        a[6] = 1f;
        a[7] = (float) startTime;
        a[8] = (float) endTime;
    }

    /**
     * Mueve el agente hacia un destino desde su posición actual.
     */
    public void agentGoTo(String key, float tx, float ty) {
        agentGoToTimed(key, tx, ty, clk, clk + 1.0);
    }

    /**
     * Mueve el agente hacia un destino desde su posición actual,
     * usando tiempos reales de simulación.
     */
    public void agentGoToTimed(String key, float tx, float ty, double startTime, double endTime) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return;
        }

        if (endTime <= startTime) {
            endTime = startTime + 0.8;
        }

        float cx = getAgentCurrentX(key);
        float cy = getAgentCurrentY(key);

        a[2] = cx;
        a[3] = cy;
        a[4] = tx;
        a[5] = ty;
        a[6] = 1f;
        a[7] = (float) startTime;
        a[8] = (float) endTime;
    }

    /**
     * Mueve el recurso hacia el centro de una locación.
     */
    public void agentGoToLocation(String key, String locName) {
        Loc l = loc(locName);

        if (l == null) {
            return;
        }

        agentGoTo(key, l.x + l.w / 2f, l.y + l.h / 2f);
    }

    /**
     * Mueve el recurso desde una locación hasta otra con duración.
     */
    public void agentGoFromLocationTimed(
            String key,
            String fromLocName,
            String toLocName,
            double startTime,
            double endTime
    ) {
        Loc from = loc(fromLocName);
        Loc to = loc(toLocName);

        if (from == null || to == null) {
            return;
        }

        agentGoFromTimed(
                key,
                from.x + from.w / 2f,
                from.y + from.h / 2f,
                to.x + to.w / 2f,
                to.y + to.h / 2f,
                startTime,
                endTime
        );
    }

    /**
     * Regresa el agente a su Home con duración por defecto.
     */
    public void agentReturnHome(String key) {
        agentReturnHomeTimed(key, clk, clk + 1.0);
    }

    /**
     * Regresa el recurso a su Home usando tiempos reales.
     *
     * Este método lo usa SimEngine cuando encuentra "Then Free".
     */
    public void agentReturnHomeTimed(String key, double startTime, double endTime) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return;
        }

        if (endTime <= startTime) {
            endTime = startTime + 0.8;
        }

        float cx = getAgentCurrentX(key);
        float cy = getAgentCurrentY(key);

        a[2] = cx;
        a[3] = cy;
        a[4] = a[0];
        a[5] = a[1];
        a[6] = 1f;
        a[7] = (float) startTime;
        a[8] = (float) endTime;
    }

    /**
     * Detiene el recurso en su posición actual.
     */
    public void agentStopAtTarget(String key) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return;
        }

        float cx = getAgentCurrentX(key);
        float cy = getAgentCurrentY(key);

        a[2] = cx;
        a[3] = cy;
        a[4] = cx;
        a[5] = cy;
        a[6] = 0f;
        a[7] = (float) clk;
        a[8] = (float) clk;
    }

    public float getAgentCurrentX(String key) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return 0;
        }

        return interpolateAgent(a, true);
    }

    public float getAgentCurrentY(String key) {
        key = canonicalResourceKey(key);
        float[] a = resAgents.get(key);

        if (a == null) {
            return 0;
        }

        return interpolateAgent(a, false);
    }

    private float interpolateAgent(float[] a, boolean xAxis) {
        if (a == null || a.length < 9) {
            return 0;
        }

        if (a[6] != 1f) {
            return xAxis ? a[2] : a[3];
        }

        float start = a[7];
        float end = a[8];

        if (end <= start) {
            return xAxis ? a[4] : a[5];
        }

        float progress = (float) ((clk - start) / (end - start));

        if (progress < 0f) {
            progress = 0f;
        }

        if (progress >= 1f) {
            progress = 1f;

            // Cuando termina, dejamos el agente fijo en el target.
            a[2] = a[4];
            a[3] = a[5];
            a[6] = 0f;
        }

        if (xAxis) {
            return a[2] + progress * (a[4] - a[2]);
        }

        return a[3] + progress * (a[5] - a[3]);
    }

    /**
     * Busca la locación Home de un recurso usando:
     * 1. r.homeLocation si ya viene resuelto.
     * 2. r.pathNetwork + r.homeNode.
     * 3. r.pathNetwork + Home: N1 leído de moveLogic.
     * 4. Por defecto, N1.
     */
    private String findHomeLocationForResource(ProModelData data, ProModelData.ResDef resDef) {
        if (data == null || resDef == null) {
            return null;
        }

        if (resDef.homeLocation != null && !resDef.homeLocation.trim().isEmpty()) {
            return resDef.homeLocation.trim();
        }

        String network = resDef.pathNetwork;

        if (network == null || network.trim().isEmpty()) {
            return null;
        }

        network = network.trim();

        String homeNode = resDef.homeNode;

        if (homeNode == null || homeNode.trim().isEmpty()) {
            homeNode = extractHomeNodeFromText(resDef.moveLogic);
        }

        if (homeNode == null || homeNode.trim().isEmpty()) {
            homeNode = "N1";
        }

        homeNode = homeNode.trim();

        if (data.interfaces == null) {
            return null;
        }

        for (ProModelData.InterfaceDef inf : data.interfaces) {
            if (inf == null) {
                continue;
            }

            if (inf.network != null
                    && inf.node != null
                    && inf.location != null
                    && inf.network.trim().equalsIgnoreCase(network)
                    && inf.node.trim().equalsIgnoreCase(homeNode)) {
                return inf.location;
            }
        }

        return null;
    }

    private String extractHomeNodeFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] lines = text.split("\\n");

        for (String line : lines) {
            String clean = line.trim();
            String upper = clean.toUpperCase();

            int idx = upper.indexOf("HOME:");

            if (idx >= 0) {
                String after = clean.substring(idx + "HOME:".length()).trim();

                if (after.isEmpty()) {
                    return "";
                }

                String[] parts = after.split("\\s+");

                if (parts.length > 0) {
                    return parts[0].trim();
                }
            }
        }

        return "";
    }

    // ─────────────────────────────────────────────────────────────────────
    // Métodos de acceso
    // ─────────────────────────────────────────────────────────────────────

    private void addL(String name, int cap, LType type, int x, int y, int w, int h) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        locs.put(name, new Loc(name, cap, type, x, y, w, h));
    }

    public Loc loc(String name) {
        if (name == null) {
            return null;
        }

        Loc direct = locs.get(name);

        if (direct != null) {
            return direct;
        }

        for (String k : locs.keySet()) {
            if (k.startsWith(name + ".")) {
                return locs.get(k);
            }
        }

        return null;
    }

    public Res res(String name) {
        String key = canonicalResourceKey(name);
        return res.get(key);
    }

    public void schedule(double time, Runnable action) {
        fel.offer(new Ev(time, action));
    }

    // ─────────────────────────────────────────────────────────────────────
    // Reset de simulación
    // ─────────────────────────────────────────────────────────────────────

    public void reset() {
        clk = 0.0;

        entidadesCreadas.set(0);
        entidadesSalientes.set(0);
        totalProcesadas.set(0);

        enSistema = 0;

        running = false;
        paused = false;
        finished = false;

        histThroughput.clear();
        fel.clear();

        Ev.reset();
        Entity.resetIds();

        for (Loc l : locs.values()) {
            resetLoc(l);
        }

        for (Res r : res.values()) {
            r.reset();
        }

        for (float[] a : resAgents.values()) {
            a[2] = a[0];
            a[3] = a[1];
            a[4] = a[0];
            a[5] = a[1];
            a[6] = 0f;
            a[7] = 0f;
            a[8] = 0f;
        }

        activeEntities.clear();

        reloadGlobalVariables();
    }

    private void resetLoc(Loc l) {
        l.cnt = 0;
        l.processed = 0;
        l.totalEntries = 0;
        l.maxCnt = 0;

        l.busyTime = 0;
        l.busyStart = 0;

        l.sumContentTime = 0;
        l.lastContentUpd = 0;

        l.waiting.clear();
    }

    private void reloadGlobalVariables() {
        globalVariables.clear();

        if (currentData == null || currentData.variables == null) {
            return;
        }

        for (ProModelData.VarDef v : currentData.variables) {
            try {
                globalVariables.put(
                        v.id,
                        Double.parseDouble(v.initialValue.replace(",", ".").trim())
                );
            } catch (Exception e) {
                globalVariables.put(v.id, 0.0);
            }
        }
    }
}