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

    public double uniform(double mean, double halfRange) {
        return mean + (r.nextDouble() * 2.0 - 1.0) * halfRange;
    }

    public double triangular(double min, double mode, double max) {
        double u = r.nextDouble();
        double range = max - min;
        double mid = mode - min;
        if (u <= mid / range) {
            return min + Math.sqrt(u * range * mid);
        } else {
            return max - Math.sqrt((1.0 - u) * range * (max - mode));
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ENTIDAD
// ─────────────────────────────────────────────────────────────────────────────
class Entity {
    private static final AtomicInteger SEQ = new AtomicInteger(1);
    public static void resetIds() { SEQ.set(1); }

    public final int id;
    public String typeName = "";
    public String iconPath = null;
    public double sysEntryTime;
    public String curLoc;
    
    // Animación
    public float curX, curY;
    public float targetX, targetY;
    public double moveStartTime = -1;
    public double moveEndTime = -1;
    public boolean moving = false;

    public Entity(String typeName, double now) {
        this.id           = SEQ.getAndIncrement();
        this.typeName     = typeName;
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
    public String iconPath = null;
    public boolean showCounter = false;
    public String counterType = "Contenido Actual";
    public boolean showGauge = false;

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
    public final List<ProModelData.ProcDef> routes = new ArrayList<>();
    public final List<Entity> activeEntities = Collections.synchronizedList(new ArrayList<>());
    public ProModelData currentData = null;

    public volatile double clk = 0;

    public final AtomicInteger entidadesCreadas  = new AtomicInteger(0);
    public final AtomicInteger entidadesSalientes = new AtomicInteger(0);
    public final AtomicInteger totalProcesadas = new AtomicInteger(0);
    public volatile int        enSistema       = 0;

    /** Historial [tiempo, throughput] para graficas */
    public final List<double[]> histThroughput =
        Collections.synchronizedList(new ArrayList<>());

    public volatile boolean running   = false;
    public volatile boolean paused    = false;
    public volatile boolean finished  = false;
    public volatile double  speedMult = 1.0;

    public final PriorityQueue<Ev> fel = new PriorityQueue<>();

    // ── Agentes visuales de recursos (animacion de trabajadores) ──────────
    /** [0]=homeX [1]=homeY [2]=curX [3]=curY [4]=tgtX [5]=tgtY [6]=moving(0/1) */
    public final Map<String, float[]> resAgents = new LinkedHashMap<>();

    public SimState(SimParams p) {
        this.params = p;
        this.rng    = new Rng(p.semilla);
        // Se inicializa vacío. Los datos vendrán de ProModelData
    }

    public void loadFromData(ProModelData data) {
        this.currentData = data;
        locs.clear();
        res.clear();
        resAgents.clear();
        routes.clear();
        if (data.processing != null) {
            routes.addAll(data.processing);
        }
        
        int defaultX = 50, defaultY = 50;
        for (ProModelData.LocDef d : data.locations) {
            int cap = 1;
            try {
                if (d.cap.equalsIgnoreCase("INFINITE")) cap = Integer.MAX_VALUE;
                else if (!d.cap.isEmpty()) cap = Integer.parseInt(d.cap);
            } catch (Exception e) {}
            
            if (d.x == -1) {
                d.x = defaultX;
                d.y = defaultY;
                defaultX += 150;
                if (defaultX > 800) { defaultX = 50; defaultY += 100; }
            }
            
            addL(d.name, cap, LType.MAQUINA, d.x, d.y, d.w, d.h);
            Loc newLoc = locs.get(d.name);
            if (newLoc != null) {
                newLoc.iconPath = d.iconPath;
                newLoc.showCounter = d.showCounter;
                newLoc.counterType = d.counterType;
                newLoc.showGauge = d.showGauge;
            }
        }
        
        for (ProModelData.ResDef r : data.resources) {
            res.put(r.name, new Res(r.name));
            addAgent(r.name, 50, 50); 
        }
    }

    private void addL(String n, int c, LType t, int x, int y, int w, int h) {
        locs.put(n, new Loc(n, c, t, x, y, w, h));
    }

    private void addAgent(String key, float hx, float hy) {
        resAgents.put(key, new float[]{hx, hy, hx, hy, hx, hy, 0f});
    }

    /** Mueve el agente desde un punto especifico hacia el destino (muestra ruta completa) */
    public void agentGoFrom(String key, float fx, float fy, float tx, float ty) {
        float[] a = resAgents.get(key);
        if (a == null) return;
        a[2] = fx; a[3] = fy;  // posicion actual = origen real
        a[4] = tx; a[5] = ty;  // target = destino real
        a[6] = 1f;             // moving
    }

    /** Mueve el agente hacia el destino cuando el recurso es asignado */
    public void agentGoTo(String key, float tx, float ty) {
        float[] a = resAgents.get(key);
        if (a == null) return;
        a[4] = tx; a[5] = ty; a[6] = 1f;
    }

    /** Regresa el agente a home cuando el recurso es liberado */
    public void agentReturnHome(String key) {
        float[] a = resAgents.get(key);
        if (a == null) return;
        a[4] = a[0]; a[5] = a[1]; a[6] = 0f;
    }

    public Loc loc(String n) { return locs.get(n); }
    public Res res(String n) { return res.get(n);  }

    public void schedule(double time, Runnable action) {
        fel.offer(new Ev(time, action));
    }

    /** Resetea todo el estado para una nueva ejecución */
    public void reset() {
        clk = 0;
        entidadesCreadas.set(0);
        entidadesSalientes.set(0);
        totalProcesadas.set(0);
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
        // Resetear posiciones de agentes
        resAgents.values().forEach(a -> {
            a[2] = a[0]; a[3] = a[1]; // curX/Y = homeX/Y
            a[4] = a[0]; a[5] = a[1]; // tgtX/Y = homeX/Y
            a[6] = 0f;                 // no moving
        });
        activeEntities.clear();
    }
}
