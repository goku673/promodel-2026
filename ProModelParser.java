import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ProModelParser {

    enum Section {
        NONE,
        LOCATIONS,
        ENTITIES,
        ROUTES,
        INTERFACES,
        RESOURCES,
        PROCESSING,
        ARRIVALS,
        VARIABLES,
        MACROS
    }

    /**
     * Parsea un archivo de texto exportado/listado desde ProModel
     * y devuelve una estructura ProModelData cargada.
     */
    public static ProModelData parse(String filePath) throws IOException {
        ProModelData data = new ProModelData();

        List<String> lines;

        try {
            lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (java.nio.charset.MalformedInputException e) {
            // Archivos antiguos de Windows o ProModel suelen usar ISO-8859-1
            lines = Files.readAllLines(Paths.get(filePath), Charset.forName("ISO-8859-1"));
        }

        Section currentSection = Section.NONE;
        List<int[]> currentBounds = null;

        ProModelData.ProcDef currentProc = null;
        ProModelData.ArrDef currentArr = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            // Ignorar líneas vacías o comentarios
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            // Detectar cabeceras de sección rodeadas de asteriscos
            if (trimmed.startsWith("*")
                    && i + 2 < lines.size()
                    && lines.get(i + 1).trim().startsWith("*")) {

                String header = lines.get(i + 1)
                        .replaceAll("\\*", "")
                        .trim()
                        .toLowerCase();

                if (header.contains("locacion") || header.contains("locaci")) {
                    currentSection = Section.LOCATIONS;
                } else if (header.contains("entidad")) {
                    currentSection = Section.ENTITIES;
                } else if (header.contains("redes de ruta") || header.equals("ruta") || header.contains("ruta")) {
                    currentSection = Section.ROUTES;
                } else if (header.contains("interfaces")) {
                    currentSection = Section.INTERFACES;
                } else if (header.contains("recurso")) {
                    currentSection = Section.RESOURCES;
                } else if (header.contains("procesamiento")) {
                    currentSection = Section.PROCESSING;
                } else if (header.contains("arribo")) {
                    currentSection = Section.ARRIVALS;
                } else if (header.contains("variable")) {
                    currentSection = Section.VARIABLES;
                } else if (header.contains("macro")) {
                    currentSection = Section.MACROS;
                } else {
                    currentSection = Section.NONE;
                }

                currentBounds = null;
                currentProc = null;
                currentArr = null;

                // Saltar la línea del título y la línea final de asteriscos
                i += 2;
                continue;
            }

            // Detectar límites de columnas con la línea:
            // -------- ---- --------
            if (trimmed.startsWith("--")
                    && currentSection != Section.NONE
                    && currentBounds == null) {
                currentBounds = parseDashes(line);
                continue;
            }

            if (currentBounds == null || currentSection == Section.NONE) {
                continue;
            }

            List<String> columns = extractColumns(line, currentBounds);

            switch (currentSection) {
                case LOCATIONS:
                    parseLocation(data, lines, columns);
                    break;

                case ENTITIES:
                    parseEntity(data, columns);
                    break;

                case ROUTES:
                    parseRoute(data, columns);
                    break;

                case INTERFACES:
                    parseInterface(data, columns);
                    break;

                case RESOURCES:
                    parseResource(data, line, columns);
                    break;

                case PROCESSING:
                    currentProc = parseProcessing(data, currentProc, line, currentBounds, columns);
                    break;

                case ARRIVALS:
                    currentArr = parseArrival(data, currentArr, columns);
                    break;

                case VARIABLES:
                    parseVariable(data, columns);
                    break;

                case MACROS:
                    parseMacro(data, columns);
                    break;

                case NONE:
                default:
                    break;
            }
        }

        // Resolver macros antes de crear el simulador.
        // Ejemplo: Mac_Montacargas -> 2, Mac_Mecanicos -> 5.
        resolveMacros(data);

        // Resolver Home de cada recurso usando:
        // Recurso.pathNetwork + Recurso.homeNode -> Interface.location
        resolveResourceHomes(data);

        return data;
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE LOCACIONES
    // ─────────────────────────────────────────────────────────────

    private static void parseLocation(ProModelData data, List<String> lines, List<String> columns) {
        if (columns.size() < 5) return;
        if (columns.get(0).isEmpty()) return;
        if (columns.get(0).equalsIgnoreCase("Nombre")) return;

        ProModelData.LocDef loc = new ProModelData.LocDef();

        loc.name = columns.get(0);
        loc.cap = columns.get(1);
        loc.units = columns.get(2);
        loc.stats = columns.get(3);
        loc.rules = columns.get(4);

        if (columns.size() >= 6) {
            loc.costs = columns.get(5);
        }

        /*
         * Si la locación tiene unidades > 1:
         * - Si el TXT ya trae LOC.1, LOC.2, etc., no generamos nada.
         * - Si no trae subunidades explícitas, las generamos.
         */
        boolean isInstance = loc.name.matches(".*\\.\\d+$");

        if (!isInstance) {
            int units = 1;

            try {
                units = Integer.parseInt(loc.units.trim());
            } catch (Exception ignored) {
                units = 1;
            }

            if (units > 1) {
                boolean hasExplicitSubunits = false;
                String subunitPrefix = loc.name + ".1";

                for (String l : lines) {
                    if (l.trim().toLowerCase().startsWith(subunitPrefix.toLowerCase())) {
                        hasExplicitSubunits = true;
                        break;
                    }
                }

                if (hasExplicitSubunits) {
                    // Se omite el padre porque las subunidades vendrán en el TXT
                    return;
                }

                for (int k = 1; k <= units; k++) {
                    ProModelData.LocDef subLoc = new ProModelData.LocDef();

                    subLoc.name = loc.name + "." + k;
                    subLoc.cap = loc.cap;
                    subLoc.units = "1";
                    subLoc.stats = loc.stats;
                    subLoc.rules = loc.rules;
                    subLoc.costs = loc.costs;

                    data.locations.add(subLoc);
                }

                return;
            }
        }

        data.locations.add(loc);
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE ENTIDADES
    // ─────────────────────────────────────────────────────────────

    private static void parseEntity(ProModelData data, List<String> columns) {
        if (columns.size() < 3) return;
        if (columns.get(0).isEmpty()) return;
        if (columns.get(0).equalsIgnoreCase("Nombre")) return;

        ProModelData.EntDef ent = new ProModelData.EntDef();

        ent.name = columns.get(0);
        ent.speed = columns.get(1);
        ent.stats = columns.get(2);

        if (columns.size() >= 4) {
            ent.costs = columns.get(3);
        }

        data.entities.add(ent);
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE REDES DE RUTA
    // ─────────────────────────────────────────────────────────────

    private static void parseRoute(ProModelData data, List<String> columns) {
        if (columns.size() < 8) return;
        if (columns.get(0).equalsIgnoreCase("Nombre")) return;

        ProModelData.RouteSegDef r = new ProModelData.RouteSegDef();

        ProModelData.RouteSegDef last = null;
        if (!data.routeSegments.isEmpty()) {
            last = data.routeSegments.get(data.routeSegments.size() - 1);
        }

        r.network = columns.get(0).isEmpty() && last != null
                ? last.network
                : columns.get(0);

        r.type = columns.get(1).isEmpty() && last != null
                ? last.type
                : columns.get(1);

        r.tv = columns.get(2).isEmpty() && last != null
                ? last.tv
                : columns.get(2);

        r.fromNode = columns.get(3);
        r.toNode = columns.get(4);
        r.bi = columns.get(5);

        try {
            r.distance = Double.parseDouble(columns.get(6).replace(",", "."));
        } catch (Exception ex) {
            r.distance = 0;
        }

        try {
            r.speedFactor = Double.parseDouble(columns.get(7).replace(",", "."));
        } catch (Exception ex) {
            r.speedFactor = 1;
        }

        if (!r.network.isEmpty()
                && !r.fromNode.isEmpty()
                && !r.toNode.isEmpty()) {
            data.routeSegments.add(r);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE INTERFACES
    // ─────────────────────────────────────────────────────────────

    private static void parseInterface(ProModelData data, List<String> columns) {
        if (columns.size() < 3) return;
        if (columns.get(0).equalsIgnoreCase("Red")) return;

        ProModelData.InterfaceDef inf = new ProModelData.InterfaceDef();

        inf.network = columns.get(0).isEmpty() && !data.interfaces.isEmpty()
                ? data.interfaces.get(data.interfaces.size() - 1).network
                : columns.get(0);

        inf.node = columns.get(1);
        inf.location = columns.get(2);

        if (!inf.network.isEmpty()
                && !inf.node.isEmpty()
                && !inf.location.isEmpty()) {
            data.interfaces.add(inf);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE RECURSOS
    // ─────────────────────────────────────────────────────────────

    private static void parseResource(ProModelData data, String rawLine, List<String> columns) {
        if (columns.size() < 3) return;

        boolean isHeader = columns.get(0).equalsIgnoreCase("Nombre");
        if (isHeader) return;

        boolean isNewResource = !columns.get(0).isEmpty();

        if (isNewResource) {
            ProModelData.ResDef res = new ProModelData.ResDef();

            res.name = columns.get(0);
            res.units = columns.get(1);
            res.stats = columns.get(2);

            if (columns.size() >= 4) {
                res.searchPath = columns.get(3);
            }

            if (columns.size() >= 5) {
                res.workSearch = columns.get(4);
            }

            if (columns.size() >= 6) {
                res.pathNetwork = columns.get(5);
            }

            if (columns.size() >= 7) {
                res.moveLogic = columns.get(6);
            }

            // Buscar datos extra como Home, Vacío, Lleno
            readResourceExtra(res, rawLine, columns);

            data.resources.add(res);
            return;
        }

        // Línea de continuación del recurso anterior:
        // Ejemplo:
        // Home: N1     Lleno: 90 Ppm
        if (!data.resources.isEmpty()) {
            ProModelData.ResDef last = data.resources.get(data.resources.size() - 1);

            readResourceExtra(last, rawLine, columns);

            String continuation = "";

            if (columns.size() >= 7 && !columns.get(6).isEmpty()) {
                continuation = columns.get(6);
            } else {
                continuation = rawLine == null ? "" : rawLine.trim();
            }

            if (!continuation.isEmpty()
                    && !continuation.startsWith("*")
                    && !continuation.startsWith("-")) {
                last.moveLogic += (last.moveLogic == null || last.moveLogic.isEmpty() ? "" : "\n")
                        + continuation;
            }
        }
    }

    private static void readResourceExtra(
            ProModelData.ResDef res,
            String rawLine,
            List<String> columns
    ) {
        if (res == null) return;

        String line = rawLine == null ? "" : rawLine.trim();

        String home = extractValueAfterKey(line, "Home:");
        if (!home.isEmpty()) {
            res.homeNode = home;
        }

        // Revisar también columna por columna
        if (columns != null) {
            for (String c : columns) {
                String h = extractValueAfterKey(c, "Home:");
                if (!h.isEmpty()) {
                    res.homeNode = h;
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE PROCESAMIENTO
    // ─────────────────────────────────────────────────────────────

    private static ProModelData.ProcDef parseProcessing(
            ProModelData data,
            ProModelData.ProcDef currentProc,
            String rawLine,
            List<int[]> bounds,
            List<String> columns
    ) {
        if (columns.size() < 7) return currentProc;

        if (!columns.get(0).isEmpty()
                && !columns.get(0).equalsIgnoreCase("Entidad")) {

            ProModelData.ProcDef proc = new ProModelData.ProcDef();

            proc.entity = columns.get(0);
            proc.location = columns.get(1);
            proc.operation = columns.get(2);
            proc.blk = columns.get(3);
            proc.output = columns.get(4);
            proc.destination = columns.get(5);
            proc.rule = columns.get(6);

            if (columns.size() >= 8) {
                proc.moveLogic = columns.get(7);
            }

            data.processing.add(proc);
            return proc;
        }

        if (currentProc == null) return null;

        boolean continuationHasRealBlock = columns.size() >= 4
                && columns.get(3) != null
                && columns.get(3).trim().matches("\\d+");

        // Continuación de operación.
        // Si la línea NO es una línea real de ruta, tomamos el texto desde la columna
        // Operación hasta el final de la línea para no cortar expresiones largas como
        // CONT_ESTIRAJE = Entries(ESTIRAJE_MAQUINAS).
        if (columns.size() >= 3) {
            String opText = columns.get(2);

            if (!continuationHasRealBlock && bounds != null && bounds.size() >= 3 && rawLine != null) {
                int opStart = bounds.get(2)[0];
                if (opStart < rawLine.length()) {
                    opText = rawLine.substring(opStart).trim();
                }
            }

            if (opText != null && !opText.isEmpty()) {
                currentProc.operation += (currentProc.operation == null || currentProc.operation.isEmpty() ? "" : "\n")
                        + opText;
            }
        }

        boolean routingConsumed = false;

        // Continuación de enrutamiento en el mismo proceso.
        // IMPORTANTE:
        // En los listados de ProModel las operaciones largas, por ejemplo:
        // CONT_ESTIRAJE = Entries(ESTIRAJE_MAQUINAS)
        // pueden desbordarse visualmente hacia las columnas de Salida/Destino.
        // Si interpretamos ese desborde como ruta, se crean rutas falsas como:
        // salida=TIRAJE_MA, destino=QUINAS), y la simulación deja de mover entidades.
        // Por eso solo aceptamos una ruta cuando la columna Blk realmente trae un número.
        if (columns.size() >= 7) {
            boolean hasRealBlock = continuationHasRealBlock;

            boolean hasRouting = hasRealBlock
                    && (
                            !columns.get(4).isEmpty()
                                    || !columns.get(5).isEmpty()
                                    || !columns.get(6).isEmpty()
                                    || (columns.size() >= 8 && !columns.get(7).isEmpty())
                    );

            if (hasRouting) {
                routingConsumed = true;
                // Si el currentProc todavía no tenía salida, completar la misma fila
                if ((currentProc.output == null || currentProc.output.isEmpty())
                        && !columns.get(4).isEmpty()) {

                    currentProc.blk = columns.get(3);
                    currentProc.output = columns.get(4);
                    currentProc.destination = columns.get(5);
                    currentProc.rule = columns.get(6);

                    if (columns.size() >= 8) {
                        currentProc.moveLogic = columns.get(7);
                    }

                } else if (!columns.get(4).isEmpty()) {
                    /*
                     * En ProModel puede haber varias salidas desde una misma operación.
                     * Creamos otro ProcDef copiando entidad/locación/operación,
                     * pero con otra salida/destino/regla.
                     */
                    ProModelData.ProcDef extra = new ProModelData.ProcDef();

                    extra.entity = currentProc.entity;
                    extra.location = currentProc.location;
                    extra.operation = currentProc.operation;
                    extra.blk = columns.get(3);
                    extra.output = columns.get(4);
                    extra.destination = columns.get(5);
                    extra.rule = columns.get(6);

                    if (columns.size() >= 8) {
                        extra.moveLogic = columns.get(7);
                    }

                    data.processing.add(extra);
                    return extra;
                }
            }
        }

        // Continuación de lógica de movimiento
        if (!routingConsumed && columns.size() >= 8 && !columns.get(7).isEmpty()) {
            currentProc.moveLogic += (currentProc.moveLogic == null || currentProc.moveLogic.isEmpty() ? "" : "\n")
                    + columns.get(7);
        }

        return currentProc;
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE ARRIBOS
    // ─────────────────────────────────────────────────────────────

    private static ProModelData.ArrDef parseArrival(
            ProModelData data,
            ProModelData.ArrDef currentArr,
            List<String> columns
    ) {
        if (columns.size() < 6) return currentArr;

        if (!columns.get(0).isEmpty()
                && !columns.get(0).equalsIgnoreCase("Entidad")) {

            ProModelData.ArrDef arr = new ProModelData.ArrDef();

            arr.entity = columns.get(0);
            arr.location = columns.get(1);
            arr.qty = columns.get(2);
            arr.firstTime = columns.get(3);
            arr.occurrences = columns.get(4);
            arr.frequency = columns.get(5);

            if (columns.size() >= 7) {
                arr.logic = columns.get(6);
            }

            data.arrivals.add(arr);
            return arr;
        }

        if (currentArr != null && columns.size() >= 7 && !columns.get(6).isEmpty()) {
            currentArr.logic += (currentArr.logic == null || currentArr.logic.isEmpty() ? "" : "\n")
                    + columns.get(6);
        }

        return currentArr;
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE VARIABLES
    // ─────────────────────────────────────────────────────────────

    private static void parseVariable(ProModelData data, List<String> columns) {
        if (columns.size() < 3) return;
        if (columns.get(0).isEmpty()) return;
        if (columns.get(0).equalsIgnoreCase("ID")) return;

        ProModelData.VarDef var = new ProModelData.VarDef();

        var.id = columns.get(0);
        var.type = columns.get(1);
        var.initialValue = columns.get(2);

        if (columns.size() >= 4) {
            var.stats = columns.get(3);
        }

        data.variables.add(var);
    }

    // ─────────────────────────────────────────────────────────────
    // PARSEO DE MACROS
    // ─────────────────────────────────────────────────────────────

    private static void parseMacro(ProModelData data, List<String> columns) {
        if (columns.size() < 2) return;
        if (columns.get(0).isEmpty()) return;
        if (columns.get(0).equalsIgnoreCase("ID")) return;

        ProModelData.MacroDef macro = new ProModelData.MacroDef();
        macro.id = columns.get(0).trim();
        macro.text = columns.get(1).trim();

        if (!macro.id.isEmpty()) {
            data.macros.add(macro);
        }
    }

    /**
     * Reemplaza macros en campos numéricos usados por el simulador.
     * Conserva el texto original solo si no existe una macro con ese ID.
     */
    private static void resolveMacros(ProModelData data) {
        if (data == null || data.macros == null || data.macros.isEmpty()) return;

        for (ProModelData.LocDef l : data.locations) {
            l.cap = resolveMacroToken(data, l.cap);
            l.units = resolveMacroToken(data, l.units);
        }

        for (ProModelData.ResDef r : data.resources) {
            r.units = resolveMacroToken(data, r.units);
        }

        for (ProModelData.ArrDef a : data.arrivals) {
            a.qty = resolveMacroToken(data, a.qty);
            a.firstTime = resolveMacroToken(data, a.firstTime);
            a.occurrences = resolveMacroToken(data, a.occurrences);
            a.frequency = resolveMacroToken(data, a.frequency);
        }
    }

    private static String resolveMacroToken(ProModelData data, String token) {
        if (token == null) return "";

        String t = token.trim();
        if (t.isEmpty()) return t;

        for (ProModelData.MacroDef m : data.macros) {
            if (m != null && m.id != null && m.id.trim().equalsIgnoreCase(t)) {
                return m.text == null ? "" : m.text.trim();
            }
        }

        return t;
    }

    // ─────────────────────────────────────────────────────────────
    // RESOLVER HOME DE RECURSOS
    // ─────────────────────────────────────────────────────────────

    private static void resolveResourceHomes(ProModelData data) {
        if (data == null) return;

        for (ProModelData.ResDef r : data.resources) {
            if (r.homeNode == null || r.homeNode.trim().isEmpty()) {
                r.homeNode = extractValueAfterKey(r.moveLogic, "Home:");
            }

            if (r.homeLocation == null || r.homeLocation.trim().isEmpty()) {
                r.homeLocation = resolveInterfaceLocation(data, r.pathNetwork, r.homeNode);
            }
        }
    }

    private static String resolveInterfaceLocation(
            ProModelData data,
            String network,
            String node
    ) {
        if (data == null) return "";
        if (network == null || node == null) return "";

        String net = network.trim();
        String nd = node.trim();

        if (net.isEmpty() || nd.isEmpty()) return "";

        for (ProModelData.InterfaceDef inf : data.interfaces) {
            if (inf.network.equalsIgnoreCase(net)
                    && inf.node.equalsIgnoreCase(nd)) {
                return inf.location;
            }
        }

        return "";
    }

    // ─────────────────────────────────────────────────────────────
    // UTILIDADES
    // ─────────────────────────────────────────────────────────────

    private static String extractValueAfterKey(String text, String key) {
        if (text == null || key == null) return "";

        String upper = text.toUpperCase();
        String upperKey = key.toUpperCase();

        int idx = upper.indexOf(upperKey);
        if (idx < 0) return "";

        String after = text.substring(idx + key.length()).trim();
        if (after.isEmpty()) return "";

        String[] parts = after.split("\\s+");
        return parts.length > 0 ? parts[0].trim() : "";
    }

    /**
     * Extrae las posiciones de columnas leyendo la línea de guiones.
     * Ejemplo:
     * Nombre        Cap Unidades
     * ------------- --- --------
     */
    private static List<int[]> parseDashes(String line) {
        List<int[]> bounds = new ArrayList<>();

        int start = -1;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '-') {
                if (start == -1) {
                    start = i;
                }
            } else {
                if (start != -1) {
                    bounds.add(new int[]{start, i});
                    start = -1;
                }
            }
        }

        if (start != -1) {
            bounds.add(new int[]{start, line.length()});
        }

        // Expandir el primer límite para capturar columnas que arrancan antes
        if (!bounds.isEmpty()) {
            bounds.get(0)[0] = 0;
        }

        // Hacer que cada columna llegue hasta el inicio de la siguiente
        for (int i = 0; i < bounds.size() - 1; i++) {
            bounds.get(i)[1] = bounds.get(i + 1)[0];
        }

        if (!bounds.isEmpty()) {
            bounds.get(bounds.size() - 1)[1] = Integer.MAX_VALUE;
        }

        return bounds;
    }

    private static List<String> extractColumns(String line, List<int[]> bounds) {
        List<String> cols = new ArrayList<>();

        for (int[] b : bounds) {
            int start = b[0];
            int end = b[1];

            if (start >= line.length()) {
                cols.add("");
            } else {
                int realEnd = Math.min(end, line.length());
                cols.add(line.substring(start, realEnd).trim());
            }
        }

        return cols;
    }

    // MAIN DE PRUEBA
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                ProModelData data = parse(args[0]);
                data.printSummary();
            } else {
                System.out.println("Por favor, pasa la ruta de un archivo .txt como argumento.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}