import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;

public class ProModelParser {
    
    enum Section {
        NONE, LOCATIONS, ENTITIES, RESOURCES, PROCESSING, ARRIVALS, VARIABLES
    }
    
    /**
     * Parsea un archivo de texto de ProModel y devuelve una estructura de datos poblada.
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
            
            // Ignorar líneas vacías o comentarios que inician con #
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            
            // Detectar cabeceras de sección (rodeadas de asteriscos)
            if (trimmed.startsWith("*") && i + 2 < lines.size() && lines.get(i+1).trim().startsWith("*")) {
                String header = lines.get(i+1).replaceAll("\\*", "").trim().toLowerCase();
                
                // Limpiar posibles acentos/tildes en la comparación
                if (header.contains("locacion") || header.contains("locaci")) currentSection = Section.LOCATIONS;
                else if (header.contains("entidad")) currentSection = Section.ENTITIES;
                else if (header.contains("recurso")) currentSection = Section.RESOURCES;
                else if (header.contains("procesamiento")) currentSection = Section.PROCESSING;
                else if (header.contains("arribo")) currentSection = Section.ARRIVALS;
                else if (header.contains("variable")) currentSection = Section.VARIABLES;
                
                currentBounds = null;
                i += 2; // Saltar la última línea de asteriscos
                continue;
            }
            
            // Detectar los límites de columna basados en la línea de guiones (----)
            if (trimmed.startsWith("--") && currentSection != Section.NONE && currentBounds == null) {
                currentBounds = parseDashes(line);
                continue;
            }
            
            // Si ya estamos en una sección y tenemos los límites de columna, procesamos los datos
            if (currentBounds != null && currentSection != Section.NONE) {
                List<String> columns = extractColumns(line, currentBounds);
                
                switch (currentSection) {
                    case LOCATIONS:
                        if (columns.size() >= 5 && !columns.get(0).isEmpty()) {
                            ProModelData.LocDef loc = new ProModelData.LocDef();
                            loc.name = columns.get(0);
                            loc.cap = columns.get(1);
                            loc.units = columns.get(2);
                            loc.stats = columns.get(3);
                            loc.rules = columns.get(4);
                            if (columns.size() >= 6) loc.costs = columns.get(5);
                            
                            // Si tiene más de 1 unidad, ProModel generará filas individuales (.1, .2)
                            // Ignoramos la fila resumen para no duplicar gráficas
                            try {
                                if (Integer.parseInt(loc.units) > 1) {
                                    continue;
                                }
                            } catch (Exception e) {}
                            
                            data.locations.add(loc);
                        }
                        break;
                        
                    case ENTITIES:
                        if (columns.size() >= 3 && !columns.get(0).isEmpty()) {
                            ProModelData.EntDef ent = new ProModelData.EntDef();
                            ent.name = columns.get(0);
                            ent.speed = columns.get(1);
                            ent.stats = columns.get(2);
                            if (columns.size() >= 4) ent.costs = columns.get(3);
                            data.entities.add(ent);
                        }
                        break;
                        
                    case RESOURCES:
                        if (columns.size() >= 3 && !columns.get(0).isEmpty() && !columns.get(0).equals("Nombre")) {
                            ProModelData.ResDef res = new ProModelData.ResDef();
                            res.name = columns.get(0);
                            res.units = columns.get(1);
                            res.stats = columns.get(2);
                            if (columns.size() >= 4) res.searchPath = columns.get(3);
                            if (columns.size() >= 5) res.workSearch = columns.get(4);
                            if (columns.size() >= 6) res.pathNetwork = columns.get(5);
                            if (columns.size() >= 7) res.moveLogic = columns.get(6);
                            data.resources.add(res);
                        } else if (!data.resources.isEmpty() && columns.size() >= 7 && !columns.get(6).isEmpty()) {
                            // Línea de continuación para recursos (ej: Lleno: 150 Ppm)
                            ProModelData.ResDef last = data.resources.get(data.resources.size() - 1);
                            last.moveLogic += "\n" + columns.get(6);
                        }
                        break;
                        
                    case PROCESSING:
                        if (columns.size() >= 7 && !columns.get(0).isEmpty() && !columns.get(0).equals("Entidad")) { 
                            // Nuevo bloque de proceso
                            currentProc = new ProModelData.ProcDef();
                            currentProc.entity = columns.get(0);
                            currentProc.location = columns.get(1);
                            currentProc.operation = columns.get(2);
                            currentProc.blk = columns.get(3);
                            currentProc.output = columns.get(4);
                            currentProc.destination = columns.get(5);
                            currentProc.rule = columns.get(6);
                            if (columns.size() >= 8) currentProc.moveLogic = columns.get(7);
                            data.processing.add(currentProc);
                        } else if (currentProc != null && columns.size() >= 3) {
                            // Continuación de operación o nueva ruta
                            if (!columns.get(2).isEmpty() && !columns.get(2).startsWith("--")) {
                                currentProc.operation += (currentProc.operation.isEmpty() ? "" : "\n") + columns.get(2);
                            }
                            if (columns.size() >= 8 && !columns.get(7).isEmpty() && !columns.get(7).startsWith("--")) {
                                currentProc.moveLogic += (currentProc.moveLogic.isEmpty() ? "" : "\n") + columns.get(7);
                            }
                            // Múltiples rutas para un mismo bloque de proceso
                            if (columns.size() >= 6 && !columns.get(4).isEmpty() && !columns.get(5).isEmpty() && columns.get(0).isEmpty()) {
                                ProModelData.ProcDef routing = new ProModelData.ProcDef();
                                routing.entity = currentProc.entity;
                                routing.location = currentProc.location;
                                routing.operation = ""; // La operación pertenece al bloque principal
                                routing.blk = columns.get(3);
                                routing.output = columns.get(4);
                                routing.destination = columns.get(5);
                                routing.rule = columns.get(6);
                                if (columns.size() >= 8) routing.moveLogic = columns.get(7);
                                data.processing.add(routing);
                            }
                        }
                        break;
                        
                    case ARRIVALS:
                        if (columns.size() >= 6 && !columns.get(0).isEmpty() && !columns.get(0).equals("Entidad")) {
                            currentArr = new ProModelData.ArrDef();
                            currentArr.entity = columns.get(0);
                            currentArr.location = columns.get(1);
                            currentArr.qty = columns.get(2);
                            currentArr.firstTime = columns.get(3);
                            currentArr.occurrences = columns.get(4);
                            currentArr.frequency = columns.get(5);
                            if (columns.size() >= 7) currentArr.logic = columns.get(6);
                            data.arrivals.add(currentArr);
                        } else if (currentArr != null && columns.size() >= 7 && !columns.get(6).isEmpty()) {
                            currentArr.logic += (currentArr.logic.isEmpty() ? "" : "\n") + columns.get(6);
                        }
                        break;
                        
                    case VARIABLES:
                        if (columns.size() >= 3 && !columns.get(0).isEmpty() && !columns.get(0).equals("ID")) {
                            ProModelData.VarDef var = new ProModelData.VarDef();
                            var.id = columns.get(0);
                            var.type = columns.get(1);
                            var.initialValue = columns.get(2);
                            if (columns.size() >= 4) var.stats = columns.get(3);
                            data.variables.add(var);
                        }
                        break;
                }
            }
        }
        
        return data;
    }
    
    /** Extrae las posiciones de las columnas leyendo la línea de guiones "------- ---- ----" */
    private static List<int[]> parseDashes(String line) {
        List<int[]> bounds = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '-') {
                if (start == -1) start = i;
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
        
        // Expandir límites para abarcar los espacios en blanco
        if (!bounds.isEmpty()) bounds.get(0)[0] = 0;
        
        for (int i = 0; i < bounds.size() - 1; i++) {
            bounds.get(i)[1] = bounds.get(i+1)[0]; 
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
                int e = Math.min(end, line.length());
                cols.add(line.substring(start, e).trim());
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
