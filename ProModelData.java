import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ProModelData
 *
 * Estructuras de datos para almacenar el modelo leído desde un archivo .txt
 * de ProModel y también para guardar el proyecto configurado como .pmsim.
 *
 * Guarda:
 * - Locaciones
 * - Entidades
 * - Redes de ruta
 * - Interfaces
 * - Recursos
 * - Procesamiento
 * - Arribos
 * - Variables
 * - Posiciones visuales
 * - Imágenes asignadas
 * - Opciones visuales de contador/medidor
 */
public class ProModelData implements Serializable {

    private static final long serialVersionUID = 1L;

    public final List<LocDef> locations = new ArrayList<>();
    public final List<EntDef> entities = new ArrayList<>();
    public final List<RouteSegDef> routeSegments = new ArrayList<>();
    public final List<InterfaceDef> interfaces = new ArrayList<>();
    public final List<ResDef> resources = new ArrayList<>();
    public final List<ProcDef> processing = new ArrayList<>();
    public final List<ArrDef> arrivals = new ArrayList<>();
    public final List<VarDef> variables = new ArrayList<>();
    public final List<MacroDef> macros = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────
    // LOCACIONES
    // ─────────────────────────────────────────────────────────────────────

    public static class LocDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String name = "";
        public String cap = "";
        public String units = "";
        public String stats = "";
        public String rules = "";
        public String costs = "";

        // Datos visuales del layout
        public int x = -1;
        public int y = -1;
        public int w = 100;
        public int h = 60;

        // Imagen asignada desde GraphicsDialog / FactoryPanel
        public String iconPath = null;

        // Opciones visuales
        public boolean showCounter = false;
        public String counterType = "Contenido Actual";
        public boolean showGauge = false;

        @Override
        public String toString() {
            return "Loc: " + name + " (Cap:" + cap + ", Units:" + units + ")";
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENTIDADES
    // ─────────────────────────────────────────────────────────────────────

    public static class EntDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String name = "";
        public String speed = "";
        public String stats = "";
        public String costs = "";

        /**
         * Permite guardar varios gráficos por entidad.
         *
         * Graphic 1 = imagen base.
         * Graphic 2, 3, 4... = imágenes alternativas.
         */
        public final Map<Integer, String> graphicPaths = new LinkedHashMap<>();

        public String getIcon(int graphicId) {
            String p = graphicPaths.get(graphicId);

            if (p != null && !p.trim().isEmpty()) {
                return p;
            }

            p = graphicPaths.get(1);

            if (p != null && !p.trim().isEmpty()) {
                return p;
            }

            return null;
        }

        public String getIconPath() {
            String p = graphicPaths.get(1);
            return p == null ? "" : p;
        }

        public void setIconPath(String path) {
            if (path == null || path.trim().isEmpty()) {
                graphicPaths.remove(1);
            } else {
                graphicPaths.put(1, path);
            }
        }

        @Override
        public String toString() {
            return "Ent: " + name + " (Vel:" + speed + ")";
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // REDES DE RUTA
    // ─────────────────────────────────────────────────────────────────────

    public static class RouteSegDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String network = "";
        public String type = "";
        public String tv = "";
        public String fromNode = "";
        public String toNode = "";
        public String bi = "";
        public double distance = 0;
        public double speedFactor = 1;

        @Override
        public String toString() {
            return "Route: " + network + " " + fromNode + " -> " + toNode + " dist=" + distance;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // INTERFACES
    // ─────────────────────────────────────────────────────────────────────

    public static class InterfaceDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String network = "";
        public String node = "";
        public String location = "";

        @Override
        public String toString() {
            return "Interface: " + network + " " + node + " -> " + location;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RECURSOS
    // ─────────────────────────────────────────────────────────────────────

    public static class ResDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String name = "";
        public String units = "";
        public String stats = "";

        public String searchPath = "";
        public String workSearch = "";
        public String pathNetwork = "";
        public String moveLogic = "";

        /**
         * Imagen del recurso.
         * Ejemplo: operador, trabajador, montacarga, camioneta.
         */
        public String iconPath = "";

        /**
         * Nodo Home leído desde:
         * Home: N1
         */
        public String homeNode = "";

        /**
         * Locación Home resuelta usando:
         * pathNetwork + homeNode -> interfaces
         */
        public String homeLocation = "";

        public String getIconPath() {
            return iconPath == null ? "" : iconPath;
        }

        public void setIconPath(String path) {
            iconPath = path == null ? "" : path;
        }

        @Override
        public String toString() {
            return "Res: " + name + " (Units:" + units + ", Home:" + homeLocation + ")";
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PROCESAMIENTO
    // ─────────────────────────────────────────────────────────────────────

    public static class ProcDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String entity = "";
        public String location = "";
        public String operation = "";
        public String blk = "";
        public String output = "";
        public String destination = "";
        public String rule = "";
        public String moveLogic = "";

        @Override
        public String toString() {
            return "Proc: " + entity + " @ " + location + " -> " + destination;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ARRIBOS
    // ─────────────────────────────────────────────────────────────────────

    public static class ArrDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String entity = "";
        public String location = "";
        public String qty = "";
        public String firstTime = "";
        public String occurrences = "";
        public String frequency = "";
        public String logic = "";

        @Override
        public String toString() {
            return "Arr: " + entity + " -> " + location + " cada " + frequency;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // VARIABLES
    // ─────────────────────────────────────────────────────────────────────

    public static class VarDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String id = "";
        public String type = "";
        public String initialValue = "";
        public String stats = "";

        @Override
        public String toString() {
            return "Var: " + id + " = " + initialValue;
        }
    }


    // ─────────────────────────────────────────────────────────────────────
    // MACROS
    // ─────────────────────────────────────────────────────────────────────

    public static class MacroDef implements Serializable {

        private static final long serialVersionUID = 1L;

        public String id = "";
        public String text = "";

        @Override
        public String toString() {
            return "Macro: " + id + " = " + text;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RESOLVER HOME DE RECURSOS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Calcula homeNode y homeLocation para cada recurso.
     *
     * Primero intenta leer:
     * Home: N1
     *
     * Luego busca en interfaces:
     * Red + Nodo -> Locación
     */
    public void resolveResourceHomes() {
        for (ResDef r : resources) {
            if (r == null) {
                continue;
            }

            if (r.homeNode == null || r.homeNode.trim().isEmpty()) {
                r.homeNode = extractValueAfterKey(r.moveLogic, "Home:");
            }

            if (r.homeNode == null || r.homeNode.trim().isEmpty()) {
                r.homeNode = "N1";
            }

            r.homeNode = r.homeNode.trim();

            String resolved = resolveInterfaceLocation(r.pathNetwork, r.homeNode);

            if (resolved != null && !resolved.trim().isEmpty()) {
                r.homeLocation = resolved.trim();
            } else if (r.homeLocation == null) {
                r.homeLocation = "";
            }
        }
    }

    /**
     * Busca una locación asociada a una red y nodo.
     */
    public String resolveInterfaceLocation(String network, String node) {
        if (network == null || node == null) {
            return "";
        }

        String net = network.trim();
        String nd = node.trim();

        if (net.isEmpty() || nd.isEmpty()) {
            return "";
        }

        for (InterfaceDef inf : interfaces) {
            if (inf == null) {
                continue;
            }

            if (inf.network == null || inf.node == null || inf.location == null) {
                continue;
            }

            if (inf.network.trim().equalsIgnoreCase(net)
                    && inf.node.trim().equalsIgnoreCase(nd)) {
                return inf.location;
            }
        }

        return "";
    }

    private static String extractValueAfterKey(String text, String key) {
        if (text == null || key == null) {
            return "";
        }

        String upper = text.toUpperCase();
        String upperKey = key.toUpperCase();

        int idx = upper.indexOf(upperKey);

        if (idx < 0) {
            return "";
        }

        String after = text.substring(idx + key.length()).trim();

        if (after.isEmpty()) {
            return "";
        }

        String[] parts = after.split("\\s+");

        if (parts.length == 0) {
            return "";
        }

        return parts[0].trim();
    }


    public String resolveMacroValue(String value) {
        if (value == null) {
            return "";
        }

        String v = value.trim();
        if (v.isEmpty()) {
            return v;
        }

        for (MacroDef m : macros) {
            if (m != null && m.id != null && m.id.trim().equalsIgnoreCase(v)) {
                return m.text == null ? "" : m.text.trim();
            }
        }

        return v;
    }

    public int parseMacroInteger(String value, int defaultValue) {
        String v = resolveMacroValue(value);

        if (v.equalsIgnoreCase("INF")
                || v.equalsIgnoreCase("INFINITE")
                || v.equalsIgnoreCase("INFINITO")
                || v.equalsIgnoreCase("INFINITA")) {
            return Integer.MAX_VALUE;
        }

        try {
            return Integer.parseInt(v.replace(",", ".").trim().split("\\.")[0]);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────────────

    public LocDef findLocation(String name) {
        if (name == null) {
            return null;
        }

        for (LocDef l : locations) {
            if (l != null && l.name != null && l.name.equalsIgnoreCase(name)) {
                return l;
            }
        }

        return null;
    }

    public EntDef findEntity(String name) {
        if (name == null) {
            return null;
        }

        for (EntDef e : entities) {
            if (e != null && e.name != null && e.name.equalsIgnoreCase(name)) {
                return e;
            }
        }

        return null;
    }

    public ResDef findResource(String name) {
        if (name == null) {
            return null;
        }

        for (ResDef r : resources) {
            if (r != null && r.name != null && r.name.equalsIgnoreCase(name)) {
                return r;
            }
        }

        return null;
    }

    public InterfaceDef findInterface(String network, String node) {
        if (network == null || node == null) {
            return null;
        }

        for (InterfaceDef inf : interfaces) {
            if (inf == null) {
                continue;
            }

            if (inf.network != null
                    && inf.node != null
                    && inf.network.equalsIgnoreCase(network)
                    && inf.node.equalsIgnoreCase(node)) {
                return inf;
            }
        }

        return null;
    }

    /**
     * Muestra un resumen en consola.
     * Lo usa ProModelParser.main para probar el parseo.
     */
    public void printSummary() {
        System.out.println("========== RESUMEN DEL MODELO ==========");
        System.out.println("Locaciones:       " + locations.size());
        System.out.println("Entidades:        " + entities.size());
        System.out.println("Redes de ruta:    " + routeSegments.size());
        System.out.println("Interfaces:       " + interfaces.size());
        System.out.println("Recursos:         " + resources.size());
        System.out.println("Procesamiento:    " + processing.size());
        System.out.println("Arribos:          " + arrivals.size());
        System.out.println("Variables:        " + variables.size());
        System.out.println("Macros:           " + macros.size());
        System.out.println();

        if (!locations.isEmpty()) {
            System.out.println("--- Locaciones ---");
            for (LocDef l : locations) {
                System.out.println(l);
            }
            System.out.println();
        }

        if (!entities.isEmpty()) {
            System.out.println("--- Entidades ---");
            for (EntDef e : entities) {
                System.out.println(e);
            }
            System.out.println();
        }

        if (!routeSegments.isEmpty()) {
            System.out.println("--- Redes de Ruta ---");
            for (RouteSegDef r : routeSegments) {
                System.out.println(r);
            }
            System.out.println();
        }

        if (!interfaces.isEmpty()) {
            System.out.println("--- Interfaces ---");
            for (InterfaceDef inf : interfaces) {
                System.out.println(inf);
            }
            System.out.println();
        }

        if (!resources.isEmpty()) {
            System.out.println("--- Recursos ---");
            for (ResDef r : resources) {
                System.out.println(r);
                System.out.println("    Red: " + r.pathNetwork);
                System.out.println("    Home Nodo: " + r.homeNode);
                System.out.println("    Home Locación: " + r.homeLocation);
                System.out.println("    Imagen: " + r.iconPath);
            }
            System.out.println();
        }

        if (!processing.isEmpty()) {
            System.out.println("--- Procesamiento ---");
            for (ProcDef p : processing) {
                System.out.println(p);
            }
            System.out.println();
        }

        if (!arrivals.isEmpty()) {
            System.out.println("--- Arribos ---");
            for (ArrDef a : arrivals) {
                System.out.println(a);
            }
            System.out.println();
        }

        if (!variables.isEmpty()) {
            System.out.println("--- Variables ---");
            for (VarDef v : variables) {
                System.out.println(v);
            }
            System.out.println();
        }

        if (!macros.isEmpty()) {
            System.out.println("--- Macros ---");
            for (MacroDef m : macros) {
                System.out.println(m);
            }
            System.out.println();
        }

        System.out.println("========================================");
    }
}