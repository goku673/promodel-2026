import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * ProjectIO
 * Guarda y abre proyectos configurados de Promodel-Lite.
 *
 * Extensión recomendada:
 * .pmsim
 *
 * Guarda:
 * - Locaciones
 * - Posiciones X/Y
 * - Tamaños W/H
 * - Imágenes asignadas
 * - Entidades
 * - Recursos
 * - Rutas
 * - Interfaces
 * - Procesamiento
 * - Arribos
 * - Variables
 */
public class ProjectIO {

    public static final String EXTENSION = "pmsim";

    /**
     * Guarda el proyecto completo en archivo .pmsim.
     */
    public static void saveProject(File file, ProModelData data) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("Archivo no válido.");
        }

        if (data == null) {
            throw new IllegalArgumentException("No hay modelo cargado para guardar.");
        }

        File target = ensureExtension(file);

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(target))) {
            out.writeObject(data);
        }
    }

    /**
     * Abre un proyecto .pmsim y devuelve el ProModelData guardado.
     */
    public static ProModelData loadProject(File file) throws Exception {
        if (file == null) {
            throw new IllegalArgumentException("Archivo no válido.");
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();

            if (!(obj instanceof ProModelData)) {
                throw new IllegalArgumentException("El archivo no es un proyecto Promodel-Lite válido.");
            }

            ProModelData data = (ProModelData) obj;

            if (data != null) {
                data.resolveResourceHomes();
            }

            return data;
        }
    }

    /**
     * Crea JFileChooser configurado para archivos .pmsim.
     */
    public static JFileChooser createProjectChooser() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(
                "Proyecto Promodel-Lite (*." + EXTENSION + ")",
                EXTENSION
        ));
        return fc;
    }

    /**
     * Si el usuario guarda sin extensión, se agrega .pmsim automáticamente.
     */
    private static File ensureExtension(File file) {
        String path = file.getAbsolutePath();

        if (!path.toLowerCase().endsWith("." + EXTENSION)) {
            return new File(path + "." + EXTENSION);
        }

        return file;
    }
}