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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * BuildDialog - Diálogo "Construir" estilo ProModel.
 * Muestra dinámicamente los datos importados del archivo .txt.
 */
public class BuildDialog extends JDialog {

    public SimParams params;
    public ProModelData currentData;
    public boolean saved = false;

    // Tablas editables
    private DefaultTableModel tmLoc, tmEnt, tmRes, tmProc, tmArribo;
    private JTabbedPane tabsPane;

    public BuildDialog(JFrame owner, SimParams p, ProModelData data) {
        super(owner, "Construir — Modelo Promodel-Lite", true);
        this.params = p.copy();
        this.currentData = data;
        setSize(820, 580);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(SimConstants.BG_PANEL);
        build();
    }

    private void build() {
        setLayout(new BorderLayout());

        // Header
        JLabel hdr = lbl("  Construir — Modelo Promodel-Lite", SimConstants.FONT_TITLE, SimConstants.C_TEXT);
        hdr.setBackground(SimConstants.BG_HEADER); hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0,SimConstants.C_ACCENT),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        this.tabsPane = tabs;
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

    public void setSelectedTab(int index) {
        if (tabsPane != null && index >= 0 && index < tabsPane.getTabCount()) {
            tabsPane.setSelectedIndex(index);
        }
    }

    // ── Tab Locaciones ────────────────────────────────────────────────────
    private JPanel buildLocTab() {
        String[] cols = {"Nombre","Capacidad","Unidades","Estadist.","Reglas","Costos"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.locations.size()][6];
            for (int i = 0; i < currentData.locations.size(); i++) {
                ProModelData.LocDef l = currentData.locations.get(i);
                data[i] = new Object[]{l.name, l.cap, l.units, l.stats, l.rules, l.costs};
            }
        }
        
        tmLoc = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c == 1; } // solo Cap editable
        };
        return tablePanel(tmLoc,
            "Capacidad: edita la columna 'Capacidad'. INFINITE = sin limite.",
            new int[]{160,80,80,120,150,80});
    }

    // ── Tab Entidades ─────────────────────────────────────────────────────
    private JPanel buildEntTab() {
        String[] cols = {"Icono", "Nombre","Velocidad (ppm)","Estadisticas", "Costos"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.entities.size()][5];
            for (int i = 0; i < currentData.entities.size(); i++) {
                ProModelData.EntDef e = currentData.entities.get(i);
                data[i] = new Object[]{e.iconPath, e.name, e.speed, e.stats, e.costs};
            }
        }
        
        tmEnt = new DefaultTableModel(data, cols) {
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
        return tablePanel(tmRes, "Recursos disponibles en el modelo.", new int[]{130,70,110,120,200});
    }

    // ── Tab Procesamiento ─────────────────────────────────────────────────
    private JPanel buildProcTab() {
        String[] cols = {"Entidad","Locacion","Operacion","Blk","Salida","Destino","Regla","Movimiento"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.processing.size()][8];
            for (int i = 0; i < currentData.processing.size(); i++) {
                ProModelData.ProcDef p = currentData.processing.get(i);
                data[i] = new Object[]{p.entity, p.location, p.operation.replace("\n", " | "), p.blk, p.output, p.destination, p.rule, p.moveLogic.replace("\n", " | ")};
            }
        }
        
        tmProc = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tmProc, "Procesamiento del modelo (solo lectura).",
            new int[]{100,120,180,40,100,120,70,180});
    }

    // ── Tab Arribos ───────────────────────────────────────────────────────
    private JPanel buildArriboTab() {
        String[] cols = {"Entidad","Locacion","Cant./Arribo","1ra vez","Ocurrencias","Frecuencia (min)", "Lógica"};
        Object[][] data = new Object[0][0];
        
        if (currentData != null) {
            data = new Object[currentData.arrivals.size()][7];
            for (int i = 0; i < currentData.arrivals.size(); i++) {
                ProModelData.ArrDef a = currentData.arrivals.get(i);
                data[i] = new Object[]{a.entity, a.location, a.qty, a.firstTime, a.occurrences, a.frequency, a.logic};
            }
        }
        
        tmArribo = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        return tablePanel(tmArribo,
            "Lista de arribos del modelo.",
            new int[]{120,120,90,70,100,130,100});
    }

    // ── Botones ───────────────────────────────────────────────────────────
    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        p.setBackground(SimConstants.BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(2,0,0,0,SimConstants.C_BORDER));

        JButton btnCancel = btn("Cancelar", new Color(100,40,40), e -> dispose());
        JButton btnSave   = btn("Cerrar", new Color(40,120,60), e -> saveAndClose());
        p.add(btnCancel); p.add(btnSave);
        return p;
    }

    private void saveAndClose() {
        // En esta versión dinámica, guardamos las capacidades modificadas de vuelta al modelo
        if (currentData != null) {
            for (int i = 0; i < tmLoc.getRowCount(); i++) {
                Object val = tmLoc.getValueAt(i, 1);
                if (val != null) {
                    currentData.locations.get(i).cap = val.toString().trim();
                }
            }
        }
        saved = true;
        dispose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JPanel tablePanel(DefaultTableModel tm, String hint, int[] widths) {
        JTable table = new JTable(tm);
        table.setBackground(SimConstants.BG_PANEL);
        table.setForeground(SimConstants.C_TEXT);
        table.setFont(SimConstants.FONT_SMALL);
        table.setRowHeight(22);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionBackground(SimConstants.BG_CARD);
        table.setSelectionForeground(SimConstants.C_TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SimConstants.BG_CARD);
        header.setForeground(SimConstants.C_ACCENT2);
        header.setFont(SimConstants.FONT_LABEL);
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t,v,sel,f,r,c);
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

        for (int i=0;i<widths.length&&i<table.getColumnCount();i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane sp = new JScrollPane(table);
        sp.setBackground(SimConstants.BG_PANEL);
        sp.getViewport().setBackground(SimConstants.BG_PANEL);
        sp.setBorder(BorderFactory.createEmptyBorder());

        JLabel hintLbl = new JLabel("  " + hint);
        hintLbl.setFont(new Font("Arial",Font.ITALIC,10));
        hintLbl.setForeground(SimConstants.C_MUTED);
        hintLbl.setBackground(SimConstants.BG_HEADER);
        hintLbl.setOpaque(true);
        hintLbl.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_PANEL);
        p.add(sp, BorderLayout.CENTER);
        p.add(hintLbl, BorderLayout.SOUTH);
        return p;
    }

    private JLabel lbl(String text, Font f, Color c) {
        JLabel l = new JLabel(text); l.setFont(f); l.setForeground(c); return l;
    }

    private JButton btn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(SimConstants.FONT_LABEL); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        b.addActionListener(al); return b;
    }
}
