import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.DefaultCellEditor;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * BuildDialog - Diálogo "Construir" estilo ProModel.
 * Tabs: Locaciones | Entidades | Redes de Ruta | Recursos | Procesamiento | Arribos
 * Los cambios se aplican a SimParams al presionar "Guardar y Cerrar".
 */
public class BuildDialog extends JDialog {

    public SimParams params;
    public ProModelData currentData;
    public boolean saved = false;

    // Tablas editables
    private DefaultTableModel tmLoc, tmRes, tmArribo;

    public BuildDialog(JFrame owner, SimParams p) {
        super(owner, "Construir — Modelo Promodel-Lite", true);

        this.params = p.copy();
        setSize(820, 580);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(SimConstants.BG_PANEL);

        build();
    }

    private void build() {
        setLayout(new BorderLayout());

        JLabel hdr = lbl(
                "  Construir — Modelo Promodel-Lite",
                SimConstants.FONT_TITLE,
                SimConstants.C_TEXT
        );

        hdr.setBackground(SimConstants.BG_HEADER);
        hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, SimConstants.C_ACCENT),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SimConstants.BG_PANEL);
        tabs.setForeground(SimConstants.C_TEXT);
        tabs.setFont(SimConstants.FONT_LABEL);

        tabs.addTab("Locaciones",       buildLocTab());
        tabs.addTab("Entidades",        buildEntTab());
        tabs.addTab("Redes de Ruta",    buildRutTab());
        tabs.addTab("Recursos",         buildResTab());
        tabs.addTab("Procesamiento",    buildProcTab());
        tabs.addTab("Arribos",          buildArriboTab());

        add(tabs, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    // ── Tab Locaciones ────────────────────────────────────────────────────
    private JPanel buildLocTab() {
        String[] cols = {"Nombre","Capacidad","Unidades","Mostrar Contador","Tipo Contador","Mostrar Medidor","Estadist.","Reglas"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.locations.size()][8];
            for (int i = 0; i < currentData.locations.size(); i++) {
                ProModelData.LocDef l = currentData.locations.get(i);
                data[i] = new Object[]{l.name, l.cap, l.units, l.showCounter, l.counterType, l.showGauge, l.stats, l.rules};
            }
        }
        
        tmLoc = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c == 1 || c == 3 || c == 4 || c == 5; }
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 5) return Boolean.class;
                return String.class;
            }
        };
        return tablePanel(tmLoc,
            "Capacidad: edita la columna 'Capacidad'. INFINITE = sin limite.",
            new int[]{160,80,90,70,100,120});
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB ENTIDADES
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildEntTab() {
        String[] cols = {"Nombre","Velocidad (ppm)","Estadisticas"};
        Object[][] data = {
            {"BARRA_ACERO",   "150","Series de tiempo"},
            {"PIEZA_CORTADA", "150","Series de tiempo"},
            {"PIEZA_TORNEADA","150","Series de tiempo"},
            {"PIEZA_FRESADA", "150","Series de tiempo"},
            {"PIEZA_PINTADA", "150","Series de tiempo"},
            {"PIEZA_FINAL",   "150","Series de tiempo"},
        };
        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JPanel p = tablePanel(tmEnt, "Doble clic en la celda 'Icono' para cargar o asignar una imagen (se guardará en la carpeta public).", new int[]{60, 160,120,200, 100});
        
        // El tablePanel devuelve un JPanel con el JScrollPane en el centro
        JScrollPane sp = (JScrollPane) p.getComponent(0);
        JTable table = (JTable) sp.getViewport().getView();
        table.setRowHeight(40); // Más altura para mostrar el icono
        
        // Custom Renderer para la columna del icono
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t, "", sel, f, r, c);
                setOpaque(true);
                setBackground(sel ? SimConstants.BG_CARD : (r % 2 == 0 ? Color.WHITE : new Color(245, 245, 245)));
                setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));
                setHorizontalAlignment(JLabel.CENTER);
                
                if (v != null && !v.toString().isEmpty()) {
                    try {
                        ImageIcon icon = new ImageIcon(new ImageIcon(v.toString()).getImage().getScaledInstance(36, 36, Image.SCALE_SMOOTH));
                        setIcon(icon);
                        setText("");
                    } catch (Exception e) { 
                        setIcon(null); 
                        setText("?"); 
                    }
                } else {
                    setIcon(null);
                    setText("Doble clic");
                    setFont(SimConstants.FONT_SMALL);
                }
                return this;
            }
        });
        
        // Click Listener para abrir el selector de archivos
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    int c = table.columnAtPoint(e.getPoint());
                    if (c == 0 && currentData != null) {
                        File publicDir = new File("public");
                        if (!publicDir.exists()) publicDir.mkdirs();
                        
                        JFileChooser fc = new JFileChooser(publicDir);
                        fc.setDialogTitle("Seleccionar imagen para la entidad");
                        if (fc.showOpenDialog(BuildDialog.this) == JFileChooser.APPROVE_OPTION) {
                            try {
                                File src = fc.getSelectedFile();
                                String name = src.getName();
                                name = name.replaceAll("[^a-zA-Z0-9_\\.-]", "_");
                                File dest = new File(publicDir, name);
                                
                                if (!src.getAbsolutePath().equals(dest.getAbsolutePath())) {
                                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                }
                                
                                currentData.entities.get(r).iconPath = dest.getAbsolutePath();
                                table.setValueAt(dest.getAbsolutePath(), r, 0);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(BuildDialog.this, "Error al cargar la imagen: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        });
        
        return p;
    }

    // ── Tab Redes de Ruta ─────────────────────────────────────────────────
    private JPanel buildRutTab() {
        String[] cols = {"Nombre", "Tipo", "T/V", "Desde", "Hasta", "BI", "Distancia/Tiempo", "Factor Velocidad"};
        Object[][] data = new Object[0][0]; // Vacío por ahora
        
        DefaultTableModel tmRut = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tmRut, "Configuración visual de rutas (solo lectura).", new int[]{150, 100, 130, 60, 60, 40, 110, 110});
    }

    // ── Tab Recursos ──────────────────────────────────────────────────────
    private JPanel buildResTab() {
        String[] cols = {"Nombre","Unidades","Estadisticas","Ruta Búsqueda", "Lógica de Movimiento"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.resources.size()][5];
            for (int i = 0; i < currentData.resources.size(); i++) {
                ProModelData.ResDef r = currentData.resources.get(i);
                data[i] = new Object[]{r.name, r.units, r.stats, r.searchPath, r.moveLogic.replace("\n", " | ")};
            }
        }
        
        tmRes = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tmRes, "Edita la columna 'Traslado (min)' para cambiar tiempos de transporte.",
            new int[]{130,70,160,60,110,110});
    }

    private void assignResourceGraphic(int tableRow, JTable table) {
        if (currentData == null || tableRow < 0 || tableRow >= currentData.resources.size()) {
            return;
        }

        File selected = chooseAndCopyImage("Seleccionar imagen para recurso");

        if (selected == null) {
            return;
        }

        ProModelData.ResDef r = currentData.resources.get(tableRow);
        r.setIconPath(selected.getAbsolutePath());

        table.setValueAt(selected.getAbsolutePath(), tableRow, 0);
        table.repaint();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB PROCESAMIENTO
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildProcTab() {
        String[] cols = {"Entidad","Locacion","Operacion","Salida","Destino","Regla","Movimiento"};
        Object[][] data = {
            {"BARRA_ACERO","CONVEYOR_1","Wait 4 min","BARRA_ACERO","ALMACEN_1","FIRST 1","—"},
            {"BARRA_ACERO","ALMACEN_1","Wait N(5,0.5) min","BARRA_ACERO","CORTADORA","FIRST 1","Move For 3 min"},
            {"BARRA_ACERO","CORTADORA","Wait E(3) min  Create 2","PIEZA_CORTADA","TORNO","FIRST 1","Move With TRABAJADOR_1"},
            {"PIEZA_CORTADA","TORNO","Wait N(5,0.5) min","PIEZA_TORNEADA","CONVEYOR_2","FIRST 1","Move For 3 min"},
            {"PIEZA_TORNEADA","CONVEYOR_2","Wait 4 min","PIEZA_TORNEADA","FRESADORA","FIRST 1","Move For 4 min"},
            {"PIEZA_TORNEADA","FRESADORA","Wait E(3) min","PIEZA_FRESADA","ALMACEN_2","FIRST 1","Move With TRABAJADOR_2"},
            {"PIEZA_FRESADA","ALMACEN_2","Wait N(5,0.5) min","PIEZA_FRESADA","PINTURA","FIRST 1","Move With MONTACARGAS"},
            {"PIEZA_FRESADA","PINTURA","Wait E(3) min","PIEZA_PINTADA","INSPECCION_1","FIRST 1","Move With MONTACARGAS"},
            {"PIEZA_PINTADA","INSPECCION_1","Wait N(5,0.5) min","PIEZA_PINTADA","INSPECCION_2","20%","Move For 4 min"},
            {"PIEZA_PINTADA","INSPECCION_1","—","PIEZA_PINTADA","EMPAQUE","80%","—"},
            {"PIEZA_PINTADA","INSPECCION_2","Wait E(3) min","PIEZA_PINTADA","EMPAQUE","FIRST 1","Move For 3 min"},
            {"PIEZA_PINTADA","EMPAQUE","Wait N(5,0.5) min","PIEZA_FINAL","EMBARQUE","FIRST 1","Move With TRABAJADOR_3"},
            {"PIEZA_FINAL","EMBARQUE","Wait E(3) min","PIEZA_FINAL","EXIT","FIRST 1","Move For 3 min"},
        };
        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tm, "Procesamiento del modelo (solo lectura). Modifica tiempos en Parametros.",
            new int[]{110,110,170,110,110,70,160});
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB ARRIBOS
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildArriboTab() {
        String[] cols = {"Entidad","Locacion","Cant./Arribo","1ra vez","Ocurrencias","Frecuencia (min)"};
        Object[][] data = {
            {"BARRA_ACERO","CONVEYOR_1","1","0","INF", params.arriboFrecuencia},
        };
        tmArribo = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tmArribo,
            "Edita 'Frecuencia (min)' para cambiar cada cuanto llegan las barras de acero.",
            new int[]{120,120,90,70,100,130});
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB MACROS
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildMacroTab() {
        String[] cols = {
                "ID Macro",
                "Valor",
                "Uso"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.macros.size()][3];

            for (int i = 0; i < currentData.macros.size(); i++) {
                ProModelData.MacroDef m = currentData.macros.get(i);

                data[i] = new Object[]{
                        m.id,
                        m.text,
                        describeMacroUse(m.id)
                };
            }
        }

        tmMacro = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        return tablePanel(
                tmMacro,
                "Macros importadas desde el TXT. Se usan para cantidades como recursos o arribos.",
                new int[]{180, 120, 420}
        );
    }

    private String describeMacroUse(String id) {
        if (currentData == null || id == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (ProModelData.ResDef r : currentData.resources) {
            if (r != null && id.equalsIgnoreCase(r.units)) {
                appendUse(sb, "Recurso " + r.name);
            }
        }

        for (ProModelData.ArrDef a : currentData.arrivals) {
            if (a != null && id.equalsIgnoreCase(a.qty)) {
                appendUse(sb, "Arribo " + a.entity + " en " + a.location);
            }
        }

        return sb.length() == 0 ? "Valor aplicado al importar" : sb.toString();
    }

    private void appendUse(StringBuilder sb, String text) {
        if (sb.length() > 0) {
            sb.append(" | ");
        }
        sb.append(text);
    }

    // ─────────────────────────────────────────────────────────────────────
    // BOTONES INFERIORES
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(SimConstants.BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, SimConstants.C_BORDER));

        JButton btnCancel = btn("Cancelar", new Color(100, 40, 40), e -> dispose());
        JButton btnSave = btn("Cerrar / Guardar Cambios", new Color(40, 120, 60), e -> saveAndClose());

        p.add(btnCancel);
        p.add(btnSave);

        JButton btnCancel = btn("Cancelar", new Color(100,40,40), e -> dispose());
        JButton btnSave   = btn("Cerrar", new Color(40,120,60), e -> saveAndClose());
        p.add(btnCancel); p.add(btnSave);
        return p;
    }

    private void saveAndClose() {
        try {
            // Leer cambios de Locaciones (capacidades)
            String[] locKeys = {"alm1Cap","cortCap","tornCap","fresCap","alm2Cap","pintCap","ins1Cap","ins2Cap","empCap","embCap"};
            int[] locRows    = {1,2,3,5,6,7,8,9,10,11};
            for (int i=0;i<locRows.length;i++) {
                Object val = tmLoc.getValueAt(locRows[i],1);
                if (val==null||"INFINITE".equals(val.toString())) continue;
                int cap = Integer.parseInt(val.toString().trim());
                switch (locKeys[i]) {
                    case "alm1Cap": params.alm1Cap=cap; break;
                    case "cortCap": params.cortCap=cap; break;
                    case "tornCap": params.tornCap=cap; break;
                    case "fresCap": params.fresCap=cap; break;
                    case "alm2Cap": params.alm2Cap=cap; break;
                    case "pintCap": params.pintCap=cap; break;
                    case "ins1Cap": params.ins1Cap=cap; break;
                    case "ins2Cap": params.ins2Cap=cap; break;
                    case "empCap":  params.empCap=cap;  break;
                    case "embCap":  params.embCap=cap;  break;
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────

    private File chooseAndCopyImage(String title) {
        try {
            File publicDir = new File("public");

            if (!publicDir.exists()) {
                publicDir.mkdirs();
            }

            JFileChooser fc = new JFileChooser(publicDir);
            fc.setDialogTitle(title);

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Imágenes (PNG, JPG, JPEG, GIF)",
                    "png",
                    "jpg",
                    "jpeg",
                    "gif"
            );

            fc.setFileFilter(filter);

            if (fc.showOpenDialog(BuildDialog.this) != JFileChooser.APPROVE_OPTION) {
                return null;
            }

            File src = fc.getSelectedFile();

            String name = src.getName().replaceAll("[^a-zA-Z0-9_.\\-]", "_");
            File dest = new File(publicDir, name);

            if (!src.getAbsolutePath().equals(dest.getAbsolutePath())) {
                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            return dest;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    BuildDialog.this,
                    "Error al cargar la imagen:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return null;
        }
    }

    private JPanel tablePanel(DefaultTableModel tm, String hint, int[] widths) {
        JTable table = new JTable(tm);

        styleTable(table);

        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(SimConstants.BG_PANEL);
        sp.getViewport().setBackground(SimConstants.BG_PANEL);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JLabel hintLbl = new JLabel("  " + hint);
        hintLbl.setFont(new Font("Arial", Font.ITALIC, 10));
        hintLbl.setForeground(SimConstants.C_MUTED);
        hintLbl.setBackground(SimConstants.BG_HEADER);
        hintLbl.setOpaque(true);
        hintLbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_PANEL);
        p.add(sp, BorderLayout.CENTER);
        p.add(hintLbl, BorderLayout.SOUTH);

        return p;
    }

    private void styleTable(JTable table) {
        table.setBackground(SimConstants.BG_PANEL);
        table.setForeground(SimConstants.C_TEXT);
        table.setFont(SimConstants.FONT_SMALL);
        table.setRowHeight(24);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SimConstants.BG_CARD);
        table.setSelectionForeground(SimConstants.C_TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SimConstants.BG_CARD);
        header.setForeground(SimConstants.C_ACCENT2);
        header.setFont(SimConstants.FONT_LABEL);
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t,
                    Object v,
                    boolean sel,
                    boolean f,
                    int r,
                    int c
            ) {
                super.getTableCellRendererComponent(t, v, sel, f, r, c);

                setOpaque(true);
                setForeground(SimConstants.C_TEXT);
                boolean editable = t.isCellEditable(r,c);
                if (sel)         setBackground(SimConstants.BG_CARD);
                else if (editable) setBackground(new Color(230,230,230));
                else               setBackground(r%2==0?Color.WHITE:new Color(245,245,245));
                setBorder(BorderFactory.createEmptyBorder(0,6,0,4));
                return this;
            }
        });
    }

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(SimConstants.FONT_LABEL);
        lbl.setForeground(SimConstants.C_ACCENT2);
        lbl.setBackground(SimConstants.BG_HEADER);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        return lbl;
    }

    private JLabel lbl(String text, Font f, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private JButton btn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        styleButton(b, bg);
        b.addActionListener(al);
        return b;
    }

    private double dbl(Object v) {
        return Double.parseDouble(v == null ? "0" : v.toString().trim());
    }
}
