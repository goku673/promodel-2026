import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
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
 * Tabs: Locaciones | Entidades | Redes de Ruta | Recursos | Procesamiento | Arribos
 * Los cambios se aplican a SimParams al presionar "Guardar y Cerrar".
 */
public class BuildDialog extends JDialog {

    public SimParams params;
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

        // Header
        JLabel hdr = lbl("  Construir — Modelo Promodel-Lite", SimConstants.FONT_TITLE, SimConstants.C_TEXT);
        hdr.setBackground(SimConstants.BG_HEADER); hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0,SimConstants.C_ACCENT),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SimConstants.BG_PANEL);
        tabs.setForeground(SimConstants.C_TEXT);
        tabs.setFont(SimConstants.FONT_LABEL);

        tabs.addTab("Locaciones",       buildLocTab());
        tabs.addTab("Entidades",        buildEntTab());
        tabs.addTab("Redes de Ruta",    buildRutasTab());
        tabs.addTab("Recursos",         buildResTab());
        tabs.addTab("Procesamiento",    buildProcTab());
        tabs.addTab("Arribos",          buildArriboTab());

        add(tabs, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    // ── Tab Locaciones ────────────────────────────────────────────────────
    private JPanel buildLocTab() {
        String[] cols = {"Nombre","Capacidad","Tipo","Unidades","Regla","Estadist."};
        Object[][] data = {
            {"CONVEYOR_1",  "INFINITE", "CONVEYOR",   "1","FIFO","Series de tiempo"},
            {"ALMACEN_1",   params.alm1Cap, "ALMACEN","1","Mas Tiempo","Series de tiempo"},
            {"CORTADORA",   params.cortCap, "MAQUINA", "1","Mas Tiempo","Series de tiempo"},
            {"TORNO",       params.tornCap, "MAQUINA", "1","Mas Tiempo","Series de tiempo"},
            {"CONVEYOR_2",  "INFINITE", "CONVEYOR",   "1","FIFO","Series de tiempo"},
            {"FRESADORA",   params.fresCap, "MAQUINA", "1","Mas Tiempo","Series de tiempo"},
            {"ALMACEN_2",   params.alm2Cap, "ALMACEN", "1","Mas Tiempo","Series de tiempo"},
            {"PINTURA",     params.pintCap, "MAQUINA", "1","Mas Tiempo","Series de tiempo"},
            {"INSPECCION_1",params.ins1Cap, "INSPECCION","1","Mas Tiempo","Series de tiempo"},
            {"INSPECCION_2",params.ins2Cap, "INSPECCION","1","Mas Tiempo","Series de tiempo"},
            {"EMPAQUE",     params.empCap,  "EMPAQUE",  "1","Mas Tiempo","Series de tiempo"},
            {"EMBARQUE",    params.embCap,  "EMBARQUE", "1","Mas Tiempo","Series de tiempo"},
        };
        tmLoc = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c == 1; } // solo Cap editable
        };
        return tablePanel(tmLoc,
            "Capacidad: edita la columna 'Capacidad'. INFINITE = sin limite.",
            new int[]{160,80,90,70,100,120});
    }

    // ── Tab Entidades ─────────────────────────────────────────────────────
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
        return tablePanel(tm, "Entidades del sistema (solo lectura — definidas por el proceso).",
            new int[]{160,120,200});
    }

    // ── Tab Redes de Ruta ─────────────────────────────────────────────────
    private JPanel buildRutasTab() {
        String[] cols = {"Red","Tipo","Desde","Hasta","Bi","Distancia/Tiempo","Vel."};
        Object[][] data = {
            {"RUTA_TRABAJADOR_1","Sobrepasar","N1","N2","Si","28.28","1"},
            {"RUTA_TRABAJADOR_2","Sobrepasar","N1","N2","Si","29.31","1"},
            {"RUTA_TRABAJADOR_3","Sobrepasar","N1","N2","Si","37.60","1"},
            {"RUTA_MONTACARGAS", "Sobrepasar","N1","N2","Si","49.51","1"},
            {"RUTA_MONTACARGAS", "Sobrepasar","N2","N3","Si","43.35","1"},
        };
        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c==5||c==6; }
        };
        return tablePanel(tm, "Redes de ruta para recursos. Edita Distancia y Factor de velocidad.",
            new int[]{150,100,60,60,40,120,40});
    }

    // ── Tab Recursos ──────────────────────────────────────────────────────
    private JPanel buildResTab() {
        String[] cols = {"Nombre","Unidades","Ruta","Home","Traslado (min)","Buscar"};
        Object[][] data = {
            {"TRABAJADOR_1","1","RUTA_TRABAJADOR_1","N1", params.t1Traslado, "Mas Cercano"},
            {"TRABAJADOR_2","1","RUTA_TRABAJADOR_2","N1", params.t2Traslado, "Mas Cercano"},
            {"TRABAJADOR_3","1","RUTA_TRABAJADOR_3","N1", params.t3Traslado, "Mas Cercano"},
            {"MONTACARGAS", "1","RUTA_MONTACARGAS", "N1", params.mkTraslado1,"Mas Cercano"},
        };
        tmRes = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c==4; }
        };
        return tablePanel(tmRes, "Edita la columna 'Traslado (min)' para cambiar tiempos de transporte.",
            new int[]{130,70,160,60,110,110});
    }

    // ── Tab Procesamiento ─────────────────────────────────────────────────
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

    // ── Tab Arribos ───────────────────────────────────────────────────────
    private JPanel buildArriboTab() {
        String[] cols = {"Entidad","Locacion","Cant./Arribo","1ra vez","Ocurrencias","Frecuencia (min)"};
        Object[][] data = {
            {"BARRA_ACERO","CONVEYOR_1","1","0","INF", params.arriboFrecuencia},
        };
        tmArribo = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return c==5; }
        };
        return tablePanel(tmArribo,
            "Edita 'Frecuencia (min)' para cambiar cada cuanto llegan las barras de acero.",
            new int[]{120,120,90,70,100,130});
    }

    // ── Botones ───────────────────────────────────────────────────────────
    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        p.setBackground(SimConstants.BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(2,0,0,0,SimConstants.C_BORDER));

        JButton btnCancel = btn("Cancelar", new Color(100,40,40), e -> dispose());
        JButton btnSave   = btn("Guardar y Cerrar", new Color(40,120,60), e -> saveAndClose());
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
            // Leer cambios de Recursos (traslados)
            params.t1Traslado  = dbl(tmRes.getValueAt(0,4));
            params.t2Traslado  = dbl(tmRes.getValueAt(1,4));
            params.t3Traslado  = dbl(tmRes.getValueAt(2,4));
            params.mkTraslado1 = dbl(tmRes.getValueAt(3,4));
            // Leer cambios de Arribos
            params.arriboFrecuencia = dbl(tmArribo.getValueAt(0,5));

            saved = true;
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Valor invalido: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

        // Colores alternos y resaltado de columnas editables
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean f, int r, int c) {
                super.getTableCellRendererComponent(t,v,sel,f,r,c);
                setOpaque(true);
                setForeground(SimConstants.C_TEXT);
                boolean editable = t.isCellEditable(r,c);
                if (sel)         setBackground(SimConstants.BG_CARD);
                else if (editable) setBackground(new Color(30,50,30));
                else               setBackground(r%2==0?SimConstants.BG_PANEL:new Color(25,25,50));
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

    private double dbl(Object v) {
        return Double.parseDouble(v == null ? "0" : v.toString().trim());
    }
}
