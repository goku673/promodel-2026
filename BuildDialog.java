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
<<<<<<< Updated upstream
 * BuildDialog - Diálogo "Construir" estilo ProModel.
 * Tabs: Locaciones | Entidades | Redes de Ruta | Recursos | Procesamiento | Arribos
 * Los cambios se aplican a SimParams al presionar "Guardar y Cerrar".
=======
 * BuildDialog
 *
 * Diálogo de construcción/configuración del modelo.
 *
 * Permite revisar:
 * - Locaciones
 * - Entidades
 * - Redes de ruta
 * - Interfaces
 * - Recursos
 * - Procesamiento
 * - Arribos
 *
 * Corrección importante:
 * La tabla de locaciones permite activar contador, elegir tipo de contador
 * y activar medidor. Además guarda correctamente el último cambio aunque
 * el usuario cierre mientras una celda está en edición.
>>>>>>> Stashed changes
 */
public class BuildDialog extends JDialog {

    public SimParams params;
    public ProModelData currentData;
    public boolean saved = false;

<<<<<<< Updated upstream
    // Tablas editables
    private DefaultTableModel tmLoc, tmRes, tmArribo;
=======
    // Modelos de tabla
    private DefaultTableModel tmLoc;
    private DefaultTableModel tmEnt;
    private DefaultTableModel tmRes;
    private DefaultTableModel tmProc;
    private DefaultTableModel tmArribo;
    private DefaultTableModel tmMacro;

    // Tabla de locaciones para cerrar edición correctamente
    private JTable tblLoc;

    private JTabbedPane tabsPane;
>>>>>>> Stashed changes

    public BuildDialog(JFrame owner, SimParams p) {
        super(owner, "Construir — Modelo Promodel-Lite", true);

        this.params = p.copy();
<<<<<<< Updated upstream
        setSize(820, 580);
=======
        this.currentData = data;

        if (this.currentData != null) {
            this.currentData.resolveResourceHomes();
        }

        setSize(940, 640);
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
        this.tabsPane = tabs;

>>>>>>> Stashed changes
        tabs.setBackground(SimConstants.BG_PANEL);
        tabs.setForeground(SimConstants.C_TEXT);
        tabs.setFont(SimConstants.FONT_LABEL);

<<<<<<< Updated upstream
        tabs.addTab("Locaciones",       buildLocTab());
        tabs.addTab("Entidades",        buildEntTab());
        tabs.addTab("Redes de Ruta",    buildRutTab());
        tabs.addTab("Recursos",         buildResTab());
        tabs.addTab("Procesamiento",    buildProcTab());
        tabs.addTab("Arribos",          buildArriboTab());
=======
        tabs.addTab("Locaciones", buildLocTab());
        tabs.addTab("Entidades", buildEntTab());
        tabs.addTab("Redes de Ruta", buildRutTab());
        tabs.addTab("Recursos", buildResTab());
        tabs.addTab("Procesamiento", buildProcTab());
        tabs.addTab("Arribos", buildArriboTab());
        tabs.addTab("Macros", buildMacroTab());
>>>>>>> Stashed changes

        add(tabs, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

<<<<<<< Updated upstream
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
=======
    public void setSelectedTab(int index) {
        if (tabsPane != null && index >= 0 && index < tabsPane.getTabCount()) {
            tabsPane.setSelectedIndex(index);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB LOCACIONES
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildLocTab() {
        String[] cols = {
                "Nombre",
                "Capacidad",
                "Unidades",
                "Mostrar Contador",
                "Tipo Contador",
                "Mostrar Medidor",
                "Estadist.",
                "Reglas"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.locations.size()][8];

            for (int i = 0; i < currentData.locations.size(); i++) {
                ProModelData.LocDef l = currentData.locations.get(i);

                data[i] = new Object[]{
                        l.name,
                        l.cap,
                        l.units,
                        l.showCounter,
                        l.counterType,
                        l.showGauge,
                        l.stats,
                        l.rules
                };
            }
        }

        tmLoc = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int row, int col) {
                /*
                 * Editable:
                 * - Capacidad
                 * - Mostrar Contador
                 * - Tipo Contador
                 * - Mostrar Medidor
                 */
                return col == 1 || col == 3 || col == 4 || col == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 5) {
                    return Boolean.class;
                }

                return String.class;
            }
        };

        JPanel p = tablePanel(
                tmLoc,
                "Configura capacidad, contador y medidor visual. El contador puede mostrar contenido actual, entradas o salidas.",
                new int[]{160, 80, 70, 120, 150, 120, 120, 120}
        );

        JScrollPane sp = (JScrollPane) p.getComponent(0);
        JTable table = (JTable) sp.getViewport().getView();

        this.tblLoc = table;

        /*
         * Corrección importante:
         * Hace que el combo/checkbox guarde su último valor al perder foco.
         */
        table.setSurrendersFocusOnKeystroke(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JComboBox<String> cbType = new JComboBox<>(
                new String[]{
                        "Contenido Actual",
                        "Entradas Totales",
                        "Salidas"
                }
        );

        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(cbType));

        return p;
>>>>>>> Stashed changes
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB ENTIDADES
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildEntTab() {
<<<<<<< Updated upstream
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
=======
        String[] cols = {
                "Icono",
                "Nombre",
                "Velocidad (ppm)",
                "Estadisticas",
                "Costos"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.entities.size()][5];

            for (int i = 0; i < currentData.entities.size(); i++) {
                ProModelData.EntDef e = currentData.entities.get(i);

                data[i] = new Object[]{
                        e.getIconPath(),
                        e.name,
                        e.speed,
                        e.stats,
                        e.costs
                };
            }
        }

        tmEnt = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JPanel tableWrapper = tablePanel(
                tmEnt,
                "Doble clic en 'Icono' para asignar Graphic 1. Selecciona una fila y usa 'Editar Gráficos...' para más imágenes.",
                new int[]{70, 170, 120, 200, 100}
        );

        JScrollPane sp = (JScrollPane) tableWrapper.getComponent(0);
        JTable table = (JTable) sp.getViewport().getView();

        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    int c = table.columnAtPoint(e.getPoint());

                    if (c == 0 && currentData != null && r >= 0) {
                        assignEntityGraphic(r, 1, table);
                    }
                }
            }
        });

        JButton btnEditGraphics = new JButton("Editar Gráficos...");
        styleButton(btnEditGraphics, new Color(50, 80, 140));

        btnEditGraphics.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row < 0 || currentData == null) {
                JOptionPane.showMessageDialog(
                        BuildDialog.this,
                        "Selecciona una entidad primero.",
                        "Sin selección",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            ProModelData.EntDef entDef = currentData.entities.get(row);
            showEntityGraphicsEditor(entDef, row, table);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(SimConstants.BG_HEADER);
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SimConstants.C_BORDER));
        toolbar.add(btnEditGraphics);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SimConstants.BG_PANEL);
        outer.add(tableWrapper, BorderLayout.CENTER);
        outer.add(toolbar, BorderLayout.SOUTH);

        return outer;
    }

    private void assignEntityGraphic(int tableRow, int graphicId, JTable table) {
        if (currentData == null || tableRow < 0 || tableRow >= currentData.entities.size()) {
            return;
        }

        File selected = chooseAndCopyImage("Seleccionar imagen — Graphic " + graphicId);

        if (selected == null) {
            return;
        }

        currentData.entities.get(tableRow).graphicPaths.put(graphicId, selected.getAbsolutePath());

        if (graphicId == 1 && table != null) {
            table.setValueAt(selected.getAbsolutePath(), tableRow, 0);
            table.repaint();
        }
    }

    private void showEntityGraphicsEditor(ProModelData.EntDef entDef, int tableRow, JTable mainTable) {
        JDialog dlg = new JDialog(
                BuildDialog.this,
                "Gráficos de Entidad — " + entDef.name,
                true
        );

        dlg.setSize(560, 410);
        dlg.setLocationRelativeTo(BuildDialog.this);
        dlg.getContentPane().setBackground(SimConstants.BG_PANEL);
        dlg.setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  Gráficos de: " + entDef.name);
        hdr.setFont(SimConstants.FONT_TITLE);
        hdr.setForeground(SimConstants.C_TEXT);
        hdr.setBackground(SimConstants.BG_HEADER);
        hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, SimConstants.C_ACCENT),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        dlg.add(hdr, BorderLayout.NORTH);

        String[] gCols = {"Graphic ID", "Ruta del Icono"};

        DefaultTableModel gModel = new DefaultTableModel(gCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (java.util.Map.Entry<Integer, String> entry : entDef.graphicPaths.entrySet()) {
            gModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable gTable = new JTable(gModel);
        styleTable(gTable);
        gTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        gTable.getColumnModel().getColumn(1).setPreferredWidth(420);

        JScrollPane gScroll = new JScrollPane(gTable);
        gScroll.setBorder(BorderFactory.createEmptyBorder());
        gScroll.getViewport().setBackground(SimConstants.BG_PANEL);

        dlg.add(gScroll, BorderLayout.CENTER);

        JButton btnAdd = new JButton("+ Agregar / Reemplazar");
        styleButton(btnAdd, new Color(40, 120, 60));

        btnAdd.addActionListener(ev -> {
            String idStr = JOptionPane.showInputDialog(
                    dlg,
                    "Ingresa el ID del gráfico. Ejemplo: 1, 2, 3...",
                    "Nuevo Gráfico",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (idStr == null || idStr.trim().isEmpty()) {
                return;
            }

            int gid;

            try {
                gid = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        dlg,
                        "ID inválido. Debe ser un número entero.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (gid < 1) {
                JOptionPane.showMessageDialog(
                        dlg,
                        "El ID debe ser mayor o igual a 1.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            File selected = chooseAndCopyImage("Seleccionar imagen — Graphic " + gid);

            if (selected == null) {
                return;
            }

            entDef.graphicPaths.put(gid, selected.getAbsolutePath());

            boolean found = false;

            for (int i = 0; i < gModel.getRowCount(); i++) {
                Object val = gModel.getValueAt(i, 0);

                if (val instanceof Integer && ((Integer) val) == gid) {
                    gModel.setValueAt(selected.getAbsolutePath(), i, 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                gModel.addRow(new Object[]{gid, selected.getAbsolutePath()});
            }

            if (gid == 1 && mainTable != null) {
                mainTable.setValueAt(selected.getAbsolutePath(), tableRow, 0);
                mainTable.repaint();
            }
        });

        JButton btnDelete = new JButton("Eliminar");
        styleButton(btnDelete, new Color(140, 40, 40));

        btnDelete.addActionListener(ev -> {
            int sel = gTable.getSelectedRow();

            if (sel < 0) {
                JOptionPane.showMessageDialog(
                        dlg,
                        "Selecciona un gráfico para eliminar.",
                        "Sin selección",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            int gid = (Integer) gModel.getValueAt(sel, 0);

            if (gid == 1) {
                JOptionPane.showMessageDialog(
                        dlg,
                        "Graphic 1 es el icono base. Puedes reemplazarlo, pero no eliminarlo.",
                        "No permitido",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            entDef.graphicPaths.remove(gid);
            gModel.removeRow(sel);
        });

        JButton btnClose = new JButton("Cerrar");
        styleButton(btnClose, new Color(60, 60, 60));
        btnClose.addActionListener(ev -> dlg.dispose());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.setBackground(SimConstants.BG_HEADER);
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SimConstants.C_BORDER));
        btnBar.add(btnAdd);
        btnBar.add(btnDelete);
        btnBar.add(btnClose);

        dlg.add(btnBar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB REDES DE RUTA
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildRutTab() {
        JPanel outer = new JPanel(new BorderLayout(0, 8));
        outer.setBackground(SimConstants.BG_PANEL);

        String[] colsSeg = {
                "Nombre",
                "Tipo",
                "T/V",
                "Desde",
                "Hasta",
                "BI",
                "Distancia/Tiempo",
                "Factor Velocidad"
        };

        Object[][] dataSeg = new Object[0][0];

        if (currentData != null) {
            dataSeg = new Object[currentData.routeSegments.size()][8];

            for (int i = 0; i < currentData.routeSegments.size(); i++) {
                ProModelData.RouteSegDef r = currentData.routeSegments.get(i);

                dataSeg[i] = new Object[]{
                        r.network,
                        r.type,
                        r.tv,
                        r.fromNode,
                        r.toNode,
                        r.bi,
                        r.distance,
                        r.speedFactor
                };
            }
        }

        DefaultTableModel tmRut = new DefaultTableModel(dataSeg, colsSeg) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JPanel segPanel = tablePanel(
                tmRut,
                "Segmentos de ruta importados desde el TXT de ProModel.",
                new int[]{150, 100, 160, 70, 70, 50, 130, 130}
        );

        String[] colsInt = {"Red", "Nodo", "Locación"};

        Object[][] dataInt = new Object[0][0];

        if (currentData != null) {
            dataInt = new Object[currentData.interfaces.size()][3];

            for (int i = 0; i < currentData.interfaces.size(); i++) {
                ProModelData.InterfaceDef inf = currentData.interfaces.get(i);

                dataInt[i] = new Object[]{
                        inf.network,
                        inf.node,
                        inf.location
                };
            }
        }

        DefaultTableModel tmInt = new DefaultTableModel(dataInt, colsInt) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JPanel intPanel = tablePanel(
                tmInt,
                "Interfaces: indican en qué locación física está cada nodo de la red.",
                new int[]{180, 90, 180}
        );

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(SimConstants.BG_PANEL);
        top.add(sectionTitle("Segmentos de Red"), BorderLayout.NORTH);
        top.add(segPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(SimConstants.BG_PANEL);
        bottom.add(sectionTitle("Interfaces: Nodo → Locación"), BorderLayout.NORTH);
        bottom.add(intPanel, BorderLayout.CENTER);

        javax.swing.JSplitPane split = new javax.swing.JSplitPane(
                javax.swing.JSplitPane.VERTICAL_SPLIT,
                top,
                bottom
        );

        split.setResizeWeight(0.55);
        split.setDividerSize(6);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(SimConstants.BG_PANEL);

        outer.add(split, BorderLayout.CENTER);

        return outer;
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB RECURSOS
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildResTab() {
        if (currentData != null) {
            currentData.resolveResourceHomes();
        }

        String[] cols = {
                "Icono",
                "Nombre",
                "Unidades",
                "Red/Ruta",
                "Home Nodo",
                "Home Locación",
                "Movimiento"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.resources.size()][7];

            for (int i = 0; i < currentData.resources.size(); i++) {
                ProModelData.ResDef r = currentData.resources.get(i);

                data[i] = new Object[]{
                        r.getIconPath(),
                        r.name,
                        r.units,
                        r.pathNetwork,
                        r.homeNode,
                        r.homeLocation,
                        r.moveLogic == null ? "" : r.moveLogic.replace("\n", " | ")
                };
            }
        }

        tmRes = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JPanel tableWrapper = tablePanel(
                tmRes,
                "Doble clic en Icono para asignar imagen al recurso. Home se obtiene desde Interfaces.",
                new int[]{70, 170, 70, 130, 90, 140, 280}
        );

        JScrollPane sp = (JScrollPane) tableWrapper.getComponent(0);
        JTable table = (JTable) sp.getViewport().getView();

        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    int col = table.columnAtPoint(e.getPoint());

                    if (row >= 0 && col == 0 && currentData != null) {
                        assignResourceGraphic(row, table);
                    }
                }
            }
        });

        JButton btnAsignar = new JButton("Asignar imagen al recurso");
        styleButton(btnAsignar, new Color(50, 80, 140));

        btnAsignar.addActionListener(e -> {
            int row = table.getSelectedRow();

            if (row < 0) {
                JOptionPane.showMessageDialog(
                        BuildDialog.this,
                        "Selecciona primero un recurso.",
                        "Sin selección",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            assignResourceGraphic(row, table);
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(SimConstants.BG_HEADER);
        toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, SimConstants.C_BORDER));
        toolbar.add(btnAsignar);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(SimConstants.BG_PANEL);
        outer.add(tableWrapper, BorderLayout.CENTER);
        outer.add(toolbar, BorderLayout.SOUTH);

        return outer;
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
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
=======
        String[] cols = {
                "Entidad",
                "Locacion",
                "Operacion",
                "Blk",
                "Salida",
                "Destino",
                "Regla",
                "Movimiento"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.processing.size()][8];

            for (int i = 0; i < currentData.processing.size(); i++) {
                ProModelData.ProcDef p = currentData.processing.get(i);

                data[i] = new Object[]{
                        p.entity,
                        p.location,
                        p.operation == null ? "" : p.operation.replace("\n", " | "),
                        p.blk,
                        p.output,
                        p.destination,
                        p.rule,
                        p.moveLogic == null ? "" : p.moveLogic.replace("\n", " | ")
                };
            }
        }

        tmProc = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        return tablePanel(
                tmProc,
                "Procesamiento del modelo. Solo lectura.",
                new int[]{100, 120, 180, 40, 100, 120, 70, 180}
        );
>>>>>>> Stashed changes
    }

    // ─────────────────────────────────────────────────────────────────────
    // TAB ARRIBOS
    // ─────────────────────────────────────────────────────────────────────

    private JPanel buildArriboTab() {
<<<<<<< Updated upstream
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
=======
        String[] cols = {
                "Entidad",
                "Locacion",
                "Cant./Arribo",
                "1ra vez",
                "Ocurrencias",
                "Frecuencia (min)",
                "Lógica"
        };

        Object[][] data = new Object[0][0];

        if (currentData != null) {
            data = new Object[currentData.arrivals.size()][7];

            for (int i = 0; i < currentData.arrivals.size(); i++) {
                ProModelData.ArrDef a = currentData.arrivals.get(i);

                data[i] = new Object[]{
                        a.entity,
                        a.location,
                        a.qty,
                        a.firstTime,
                        a.occurrences,
                        a.frequency,
                        a.logic
                };
            }
        }

        tmArribo = new DefaultTableModel(data, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        return tablePanel(
                tmArribo,
                "Lista de arribos del modelo.",
                new int[]{120, 120, 90, 70, 100, 130, 100}
        );
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
        JButton btnCancel = btn("Cancelar", new Color(100,40,40), e -> dispose());
        JButton btnSave   = btn("Cerrar", new Color(40,120,60), e -> saveAndClose());
        p.add(btnCancel); p.add(btnSave);
=======
>>>>>>> Stashed changes
        return p;
    }

    private void saveAndClose() {
<<<<<<< Updated upstream
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
=======
        /*
         * Corrección importante:
         * Si el usuario estaba editando un checkbox o combo,
         * forzamos a guardar el valor antes de leer la tabla.
         */
        if (tblLoc != null && tblLoc.isEditing()) {
            tblLoc.getCellEditor().stopCellEditing();
        }

        if (currentData != null && tmLoc != null) {
            for (int i = 0; i < tmLoc.getRowCount() && i < currentData.locations.size(); i++) {
                ProModelData.LocDef l = currentData.locations.get(i);

                Object capVal = tmLoc.getValueAt(i, 1);
                if (capVal != null) {
                    l.cap = capVal.toString().trim();
                }

                Object showC = tmLoc.getValueAt(i, 3);
                l.showCounter = Boolean.TRUE.equals(showC);

                Object cType = tmLoc.getValueAt(i, 4);
                if (cType != null && !cType.toString().trim().isEmpty()) {
                    l.counterType = cType.toString();
                } else {
                    l.counterType = "Contenido Actual";
                }

                Object showG = tmLoc.getValueAt(i, 5);
                l.showGauge = Boolean.TRUE.equals(showG);
            }

            currentData.resolveResourceHomes();
        }

        saved = true;
        dispose();
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                boolean editable = t.isCellEditable(r,c);
                if (sel)         setBackground(SimConstants.BG_CARD);
                else if (editable) setBackground(new Color(230,230,230));
                else               setBackground(r%2==0?Color.WHITE:new Color(245,245,245));
                setBorder(BorderFactory.createEmptyBorder(0,6,0,4));
=======

                boolean editable = t.isCellEditable(r, c);

                if (sel) {
                    setBackground(SimConstants.BG_CARD);
                } else if (editable) {
                    setBackground(new Color(230, 230, 230));
                } else {
                    setBackground(r % 2 == 0 ? Color.WHITE : new Color(245, 245, 245));
                }

                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 4));

>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
    private double dbl(Object v) {
        return Double.parseDouble(v == null ? "0" : v.toString().trim());
    }
}
=======
    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(SimConstants.FONT_LABEL);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    // ─────────────────────────────────────────────────────────────────────
    // RENDERER DE ICONOS PARA ENTIDADES Y RECURSOS
    // ─────────────────────────────────────────────────────────────────────

    private static class IconCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t,
                Object v,
                boolean sel,
                boolean f,
                int r,
                int c
        ) {
            super.getTableCellRendererComponent(t, "", sel, f, r, c);

            setOpaque(true);
            setBackground(sel ? SimConstants.BG_CARD : (r % 2 == 0 ? Color.WHITE : new Color(245, 245, 245)));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 4));
            setHorizontalAlignment(JLabel.CENTER);

            if (v != null && !v.toString().trim().isEmpty()) {
                try {
                    ImageIcon icon = new ImageIcon(
                            new ImageIcon(v.toString())
                                    .getImage()
                                    .getScaledInstance(36, 36, Image.SCALE_SMOOTH)
                    );

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
    }
}
>>>>>>> Stashed changes
