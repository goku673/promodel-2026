import java.util.ArrayList;
import java.util.List;

/**
 * Estructuras de datos para almacenar el modelo leído desde un archivo .txt de ProModel.
 */
public class ProModelData {

    public static class LocDef {
        public String name = "";
        public String cap = "";
        public String units = "";
        public String stats = "";
        public String rules = "";
        public String costs = "";
        
        public int x = -1;
        public int y = -1;
        public int w = 100;
        public int h = 60;
        public String iconPath = null;
        
        @Override
        public String toString() { return "Loc: " + name + " (Cap:" + cap + ")"; }
    }

    public static class EntDef {
        public String name = "";
        public String speed = "";
        public String stats = "";
        public String costs = "";
        public String iconPath = "";

        @Override
        public String toString() { return "Ent: " + name + " (Vel:" + speed + ")"; }
    }

    public static class ResDef {
        public String name = "";
        public String units = "";
        public String stats = "";
        public String searchPath = "";
        public String workSearch = "";
        public String pathNetwork = "";
        public String moveLogic = "";
        public String costs = "";

        @Override
        public String toString() { return "Res: " + name + " (Units:" + units + ")"; }
    }

    public static class ProcDef {
        public String entity = "";
        public String location = "";
        public String operation = "";
        public String blk = "";
        public String output = "";
        public String destination = "";
        public String rule = "";
        public String moveLogic = "";

        @Override
        public String toString() { return "Proc: " + entity + " @ " + location + " -> " + destination; }
    }

    public static class ArrDef {
        public String entity = "";
        public String location = "";
        public String qty = "";
        public String firstTime = "";
        public String occurrences = "";
        public String frequency = "";
        public String logic = "";

        @Override
        public String toString() { return "Arr: " + entity + " at " + location + " freq: " + frequency; }
    }

    public static class VarDef {
        public String id = "";
        public String type = "";
        public String initialValue = "";
        public String stats = "";

        @Override
        public String toString() { return "Var: " + id + " = " + initialValue; }
    }

    public final List<LocDef> locations = new ArrayList<>();
    public final List<EntDef> entities = new ArrayList<>();
    public final List<ResDef> resources = new ArrayList<>();
    public final List<ProcDef> processing = new ArrayList<>();
    public final List<ArrDef> arrivals = new ArrayList<>();
    public final List<VarDef> variables = new ArrayList<>();

    public void printSummary() {
        System.out.println("--- ProModel Model Summary ---");
        System.out.println("Locations: " + locations.size());
        locations.forEach(System.out::println);
        System.out.println("\nEntities: " + entities.size());
        entities.forEach(System.out::println);
        System.out.println("\nResources: " + resources.size());
        resources.forEach(System.out::println);
        System.out.println("\nProcessing: " + processing.size());
        processing.forEach(System.out::println);
        System.out.println("\nArrivals: " + arrivals.size());
        arrivals.forEach(System.out::println);
        System.out.println("\nVariables: " + variables.size());
        variables.forEach(System.out::println);
        System.out.println("------------------------------");
    }
}
