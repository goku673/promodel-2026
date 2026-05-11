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
    Rng(long seed) { r = new Random(seed); }

    /** Exponencial: -media * ln(U), U ~ Uniform(0,1) */
    public double exp(double mean) {
        double u = r.nextDouble();
        return u == 0 ? mean : -mean * Math.log(u);
    }

    /** Normal (Box-Muller) */
    public double norm(double mean, double sigma) {
        double u1, u2;
        do { u1 = r.nextDouble(); u2 = r.nextDouble(); } while (u1 == 0);
        double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return Math.max(0.001, mean + sigma * z);
    }

    /** Bernoulli: true con probabilidad p */
    public boolean prob(double p) { return r.nextDouble() < p; }
}

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD
// ─────────────────────────────────────────────────────────────────────────────
class Entity {
    private static final AtomicInteger SEQ = new AtomicInteger(1);
    public static void resetIds() { SEQ.set(1); }

    public final int id;
    public EType type;
    public double sysEntryTime;
    public String curLoc;

    public Entity(EType type, double now) {
        this.id           = SEQ.getAndIncrement();
        this.type         = type;
        this.sysEntryTime = now;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LOCACIÓN
// ─────────────────────────────────────────────────────────────────────────────
class Loc {
    public final String name;
    public final int    cap;
    public final LType  type;

    public int    cnt          = 0;
    public int    processed    = 0;   // total salidas
    public int    totalEntries = 0;   // total entradas
    public int    maxCnt       = 0;   // maximo simultaneo
    public double busyTime     = 0;
    public double busyStart    = 0;

    // Para promedio de contenido: sum(cnt * dt)
    public double sumContentTime   = 0;
    public double lastContentUpd   = 0;

    /** Cola de lambdas pendientes de entrar a esta locacion */
    public final Deque<Runnable> waiting = new ArrayDeque<>();

    public int x, y, w, h;

    public Loc(String name, int cap, LType type, int x, int y, int w, int h) {
        this.name = name; this.cap = cap; this.type = type;
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    public boolean full() { return cnt >= cap; }

    private void updateAvg(double clk) {
        sumContentTime += cnt * (clk - lastContentUpd);
        lastContentUpd = clk;
    }

    public void enter(double clk) {
        updateAvg(clk);
        if (cnt == 0) busyStart = clk;
        cnt++;
        totalEntries++;
        if (cnt > maxCnt) maxCnt = cnt;
    }

    public void exit(double clk) {
        updateAvg(clk);
        cnt--;
        processed++;
        if (cnt == 0) busyTime += clk - busyStart;
    }

    /** Intenta dejar entrar entidades en espera */
    public void drain() {
        while (!waiting.isEmpty() && !full()) {
            waiting.poll().run();
        }
    }

    /** Utilizacion en tiempo real */
    public double utilLive(double clk, double total) {
        if (total <= 0) return 0;
        double busy = busyTime + (cnt > 0 ? clk - busyStart : 0);
        return Math.min(100.0, busy / total * 100.0);
    }

    /** Contenido promedio ponderado en tiempo */
    public double avgContents(double totalTime) {
        return totalTime > 0 ? sumContentTime / totalTime : 0;
    }

    /** Tiempo promedio por entrada (min) */
    public double avgTimePerEntry(double clk) {
        if (totalEntries == 0) return 0;
        double bt = busyTime + (cnt > 0 ? clk - busyStart : 0);
        return bt / totalEntries;
    }

    public int waitingCount() { return waiting.size(); }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECURSO
// ─────────────────────────────────────────────────────────────────────────────
class Res {
    public final String name;
    public boolean busy      = false;
    public int    timesUsed  = 0;     // veces que fue asignado
    public double workTime   = 0;     // tiempo total de trabajo (min)
    public double travelTime = 0;     // tiempo total de viaje (min)
    public double workStart  = 0;     // cuando empezo la tarea actual
    private final Deque<Runnable> queue = new ArrayDeque<>();

    public Res(String name) { this.name = name; }

    public void reset() {
        busy = false; timesUsed = 0; workTime = 0;
        travelTime = 0; workStart = 0; queue.clear();
    }

    /** Solicita el recurso con registro del tiempo de inicio. */
    public void request(double clk, double travel, Runnable action) {
        if (!busy) {
            busy = true;
            workStart = clk;
            travelTime += travel;
            timesUsed++;
            action.run();
        } else {
            final double trvl = travel;
            queue.offer(() -> {
                workStart = clk;
                travelTime += trvl;
                timesUsed++;
                action.run();
            });
        }
    }

    /** Libera el recurso y acumula tiempo de trabajo. */
    public void release(double clk) {
        workTime += clk - workStart;
        Runnable next = queue.poll();
        if (next != null) next.run();
        else              busy = false;
    }

    /** % Utilizacion sobre el tiempo total de simulacion */
    public double utilPct(double totalTime) {
        return totalTime > 0 ? Math.min(100.0, workTime / totalTime * 100.0) : 0;
    }

    /** Tiempo promedio de trabajo por uso (min) */
    public double avgTimePerUse() {
        return timesUsed > 0 ? workTime / timesUsed : 0;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EVENTO (Future Event List)
// ─────────────────────────────────────────────────────────────────────────────
class Ev implements Comparable<Ev> {
    private static final AtomicInteger SEQ = new AtomicInteger(0);
    public static void reset() { SEQ.set(0); }

    public final double   time;
    public final int      seq;
    public final Runnable action;

    public Ev(double time, Runnable action) {
        this.time   = time;
        this.seq    = SEQ.getAndIncrement();
        this.action = action;
    }

    @Override
    public int compareTo(Ev o) {
        int c = Double.compare(time, o.time);
        return c != 0 ? c : Integer.compare(seq, o.seq);
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ESTADO GLOBAL DE SIMULACIÓN
// ─────────────────────────────────────────────────────────────────────────────
class SimState {
    public final SimParams params;
    public final Rng rng;

    public final Map<String, Loc> locs = new LinkedHashMap<>();
    public final Map<String, Res> res  = new LinkedHashMap<>();

    public volatile double clk = 0;

    public final AtomicInteger barrasLlegadas = new AtomicInteger(0);
    public final AtomicInteger piezasFinales  = new AtomicInteger(0);
    public volatile int        enSistema      = 0;

    /** Historial [tiempo, throughput] para gráficas */
    public final List<double[]> histThroughput =
        Collections.synchronizedList(new ArrayList<>());

    public volatile boolean running   = false;
    public volatile boolean paused    = false;
    public volatile boolean finished  = false;
    public volatile double  speedMult = 1.0;

    public final PriorityQueue<Ev> fel = new PriorityQueue<>();

    public SimState(SimParams p) {
        this.params = p;
        this.rng    = new Rng(p.semilla);
        initLocs();
        initRes();
    }

    private void initLocs() {
        // Layout: panel ~1060 x 490 px
        // Fila 1 (y=50):  flujo principal de izquierda a derecha
        // Fila 2 (y=240): ramificación inspección + empaque
        // Fila 3 (y=380): embarque final
        int W = 90, H = 65;
        addL("CONVEYOR_1",   Integer.MAX_VALUE, LType.CONVEYOR,    20, 50, 130, H);
        addL("ALMACEN_1",    params.alm1Cap,    LType.ALMACEN,    160, 50,   W, H);
        addL("CORTADORA",    params.cortCap,    LType.MAQUINA,    260, 50,   W, H);
        addL("TORNO",        params.tornCap,    LType.MAQUINA,    360, 50,   W, H);
        addL("CONVEYOR_2",   Integer.MAX_VALUE, LType.CONVEYOR,   460, 50, 110, H);
        addL("FRESADORA",    params.fresCap,    LType.MAQUINA,    580, 50,   W, H);
        addL("ALMACEN_2",    params.alm2Cap,    LType.ALMACEN,    680, 50,   W, H);
        addL("PINTURA",      params.pintCap,    LType.MAQUINA,    780, 50,   W, H);
        addL("INSPECCION_1", params.ins1Cap,    LType.INSPECCION, 880, 50,   W, H);

        addL("INSPECCION_2", params.ins2Cap,    LType.INSPECCION, 880,240,   W, H);
        addL("EMPAQUE",      params.empCap,     LType.EMPAQUE,    680,240,   W, H);
        addL("EMBARQUE",     params.embCap,     LType.EMBARQUE,   780,380,   W, H);
    }

    private void addL(String n, int c, LType t, int x, int y, int w, int h) {
        locs.put(n, new Loc(n, c, t, x, y, w, h));
    }

    private void initRes() {
        res.put("T1", new Res("TRABAJADOR_1"));
        res.put("T2", new Res("TRABAJADOR_2"));
        res.put("T3", new Res("TRABAJADOR_3"));
        res.put("MK", new Res("MONTACARGAS"));
    }

    public Loc loc(String n) { return locs.get(n); }
    public Res res(String n) { return res.get(n);  }

    public void schedule(double time, Runnable action) {
        fel.offer(new Ev(time, action));
    }

    /** Resetea todo el estado para una nueva ejecución */
    public void reset() {
        clk = 0;
        barrasLlegadas.set(0);
        piezasFinales.set(0);
        enSistema  = 0;
        running    = false;
        paused     = false;
        finished   = false;
        histThroughput.clear();
        fel.clear();
        Ev.reset();
        Entity.resetIds();
        locs.values().forEach(l -> {
            l.cnt = 0; l.processed = 0; l.totalEntries = 0; l.maxCnt = 0;
            l.busyTime = 0; l.busyStart = 0;
            l.sumContentTime = 0; l.lastContentUpd = 0;
            l.waiting.clear();
        });
        res.values().forEach(Res::reset);
    }
}
