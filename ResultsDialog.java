import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * ResultsDialog - Ventana completa de resultados de simulación.
 * Aparece al finalizar la simulación si el usuario acepta verla.
 * Muestra: resumen global, estadísticas por locación, estadísticas de recursos.
 */
public class ResultsDialog extends JDialog {

    private final SimState state;
    private final DecimalFormat df2 = new DecimalFormat("0.00");
    private final DecimalFormat df0 = new DecimalFormat("0");

    public ResultsDialog(JFrame owner, SimState state) {
        super(owner, "Resultados de Simulacion — Multi-Engrane", false);
        this.state = state;
        setSize(900, 640);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(SimConstants.BG_PANEL);
        build();
    }

    private void build() {
        setLayout(new BorderLayout());

        // Header
        JLabel hdr = new JLabel("  Resultados Finales de Simulacion — Multi-Engrane");
        hdr.setFont(SimConstants.FONT_TITLE);
        hdr.setForeground(SimConstants.C_TEXT);
        hdr.setBackground(SimConstants.BG_HEADER);
        hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0,SimConstants.C_ACCENT),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SimConstants.BG_PANEL);
        tabs.setForeground(SimConstants.C_TEXT);
        tabs.setFont(SimConstants.FONT_LABEL);

        tabs.addTab("Resumen General",      buildSummaryTab());
        tabs.addTab("Estadisticas por Loc.", buildLocStatsTab());
        tabs.addTab("Recursos",             buildResTab());
        tabs.addTab("Grafica Throughput",   buildChartTab());

        add(tabs, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT,10,8));
        south.setBackground(SimConstants.BG_HEADER);
        south.setBorder(BorderFactory.createMatteBorder(2,0,0,0,SimConstants.C_BORDER));

        JButton btnExport = btn("Exportar CSV", new Color(50,80,150));
        btnExport.addActionListener(e -> exportCSV());
        JButton btnClose  = btn("Cerrar", new Color(100,40,40));
        btnClose.addActionListener(e -> dispose());
        south.add(btnExport); south.add(btnClose);
        add(south, BorderLayout.SOUTH);
    }

    // ── Tab 1: Resumen ────────────────────────────────────────────────────
    private JPanel buildSummaryTab() {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(SimConstants.BG_PANEL);
        p.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        double totalTime = state.clk;
        int barras    = state.barrasLlegadas.get();
        int piezasFin = state.piezasFinales.get();
        double throughput = totalTime > 0 ? piezasFin / totalTime : 0;

        // Cards de KPIs
        JPanel cards = new JPanel(new GridLayout(2,3,10,10));
        cards.setOpaque(false);
        cards.add(kpiCard("Tiempo Simulado",    df2.format(totalTime)+" min", SimConstants.C_ACCENT2));
        cards.add(kpiCard("Barras Llegadas",    df0.format(barras),           SimConstants.COL_BARRA));
        cards.add(kpiCard("Piezas Terminadas",  df0.format(piezasFin),        SimConstants.COL_FINAL));
        cards.add(kpiCard("Throughput Prom.",   df2.format(throughput)+" pzs/min", new Color(79,195,247)));
        cards.add(kpiCard("Piezas Generadas",   df0.format(barras*2),         SimConstants.COL_PIEZA_CORTADA));
        cards.add(kpiCard("En Sistema al Final",df0.format(state.enSistema),  SimConstants.C_WARNING));

        // Tabla resumen de tiempos del modelo
        String[] cols = {"Parametro","Valor"};
        Object[][] data = {
            {"Duracion simulacion",      df2.format(state.params.duracion) + " min"},
            {"Frecuencia de arribo",     df2.format(state.params.arriboFrecuencia) + " min"},
            {"Semilla aleatoria",        String.valueOf(state.params.semilla)},
            {"% Rechazo en Inspeccion 1",df2.format(state.params.probRechazo*100) + "%"},
            {"Piezas por barra (Create)",    "2"},
            {"Tasa produccion real",     df2.format(throughput*60) + " piezas/hora"},
            {"Tiempo ciclo promedio",    piezasFin>0?df2.format(totalTime/piezasFin)+" min/pieza":"—"},
        };
        JTable t = darkTable(data,cols,new int[]{280,300});

        p.add(cards, BorderLayout.NORTH);
        p.add(scroll(t), BorderLayout.CENTER);
        return p;
    }

    // ── Tab 2: Estadísticas por Locación ──────────────────────────────────
    private JPanel buildLocStatsTab() {
        String[] cols = {"Locacion","Capacidad","Procesadas","En Loc.","En Cola","Util.%","Tiempo Util.(min)"};
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        for (Loc loc : state.locs.values()) {
            int cap = loc.cap == Integer.MAX_VALUE ? 999 : loc.cap;
            double util = loc.utilLive(state.clk, state.clk);
            double busyMin = loc.busyTime + (loc.cnt > 0 ? state.clk - loc.busyStart : 0);
            rows.add(new Object[]{
                loc.name, cap, loc.processed, loc.cnt, loc.waitingCount(),
                df2.format(util)+"%", df2.format(busyMin)
            });
        }
        Object[][] data = rows.toArray(new Object[0][]);
        JTable t = darkTable(data, cols, new int[]{140,70,90,70,70,70,120});

        // Colorear por utilización
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean f,int r,int c) {
                super.getTableCellRendererComponent(tbl,v,sel,f,r,c);
                setOpaque(true); setForeground(SimConstants.C_TEXT);
                setBorder(BorderFactory.createEmptyBorder(0,6,0,4));
                if (sel) { setBackground(SimConstants.BG_CARD); return this; }
                setBackground(r%2==0?SimConstants.BG_PANEL:new Color(25,25,50));
                // Resaltar alta utilización
                if (c==5) {
                    try {
                        double u=Double.parseDouble(v.toString().replace("%",""));
                        if (u>=80) setBackground(new Color(80,20,20));
                        else if (u>=50) setBackground(new Color(60,50,10));
                    } catch (Exception ignored){}
                }
                return this;
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_PANEL);
        p.add(scroll(t), BorderLayout.CENTER);

        JLabel note = new JLabel("  Rojo: utilizacion >= 80% (posible cuello de botella)");
        note.setFont(new Font("Arial",Font.ITALIC,10));
        note.setForeground(SimConstants.C_MUTED);
        note.setBackground(SimConstants.BG_HEADER); note.setOpaque(true);
        note.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        p.add(note, BorderLayout.SOUTH);
        return p;
    }

    // ── Tab 3: Recursos ───────────────────────────────────────────────────
    private JPanel buildResTab() {
        String[] cols = {"Recurso","Estado Final","Cola pendiente"};
        Object[][] data = {
            {"TRABAJADOR_1", state.res("T1")!=null&&state.res("T1").busy?"OCUPADO":"LIBRE", "—"},
            {"TRABAJADOR_2", state.res("T2")!=null&&state.res("T2").busy?"OCUPADO":"LIBRE", "—"},
            {"TRABAJADOR_3", state.res("T3")!=null&&state.res("T3").busy?"OCUPADO":"LIBRE", "—"},
            {"MONTACARGAS",  state.res("MK")!=null&&state.res("MK").busy?"OCUPADO":"LIBRE", "—"},
        };
        JTable t = darkTable(data, cols, new int[]{200,200,200});
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_PANEL);
        p.add(scroll(t), BorderLayout.NORTH);

        // Panel de parámetros usados
        String[] cols2 = {"Parametro de Recurso","Valor Usado"};
        Object[][] data2 = {
            {"T1 - Traslado Cortadora→Torno",    df2.format(state.params.t1Traslado)+" min"},
            {"T2 - Traslado Fresadora→Almacen2", df2.format(state.params.t2Traslado)+" min"},
            {"T3 - Traslado Empaque→Embarque",   df2.format(state.params.t3Traslado)+" min"},
            {"MK - Traslado Almacen2→Pintura",   df2.format(state.params.mkTraslado1)+" min"},
            {"MK - Traslado Pintura→Inspeccion1",df2.format(state.params.mkTraslado2)+" min"},
        };
        JTable t2 = darkTable(data2, cols2, new int[]{300,200});
        p.add(scroll(t2), BorderLayout.CENTER);
        return p;
    }

    // ── Tab 4: Gráfica de throughput (simple) ─────────────────────────────
    private JPanel buildChartTab() {
        return new ChartsDialog.ThroughputChart(state);
    }

    // ── Exportar CSV ──────────────────────────────────────────────────────
    private void exportCSV() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("resultados_multiengrane.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(fc.getSelectedFile())) {
            pw.println("RESULTADOS SIMULACION MULTI-ENGRANE");
            pw.println("Tiempo simulado," + df2.format(state.clk) + " min");
            pw.println("Barras llegadas," + state.barrasLlegadas.get());
            pw.println("Piezas finalizadas," + state.piezasFinales.get());
            pw.println();
            pw.println("LOCACION,CAP,PROCESADAS,EN_LOC,UTILIZ_%");
            for (Loc loc : state.locs.values()) {
                int cap = loc.cap==Integer.MAX_VALUE?999:loc.cap;
                pw.println(loc.name+","+cap+","+loc.processed+","+loc.cnt+","+
                           df2.format(loc.utilLive(state.clk,state.clk)));
            }
            JOptionPane.showMessageDialog(this, "CSV exportado correctamente.", "Exito",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JPanel kpiCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 2),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        JLabel ttl = new JLabel(title, SwingConstants.CENTER);
        ttl.setFont(new Font("Arial",Font.PLAIN,10));
        ttl.setForeground(SimConstants.C_MUTED);
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Monospaced",Font.BOLD,18));
        val.setForeground(accent);
        p.add(ttl, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private JTable darkTable(Object[][] data, String[] cols, int[] widths) {
        DefaultTableModel tm = new DefaultTableModel(data,cols) {
            public boolean isCellEditable(int r,int c){return false;}
        };
        JTable t = new JTable(tm);
        t.setBackground(SimConstants.BG_PANEL); t.setForeground(SimConstants.C_TEXT);
        t.setFont(SimConstants.FONT_SMALL); t.setRowHeight(22);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,1));
        t.setSelectionBackground(SimConstants.BG_CARD);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            public Component getTableCellRendererComponent(JTable tbl,Object v,boolean sel,boolean f,int r,int c){
                super.getTableCellRendererComponent(tbl,v,sel,f,r,c);
                setOpaque(true); setForeground(SimConstants.C_TEXT);
                setBackground(sel?SimConstants.BG_CARD:r%2==0?SimConstants.BG_PANEL:new Color(25,25,50));
                setBorder(BorderFactory.createEmptyBorder(0,6,0,4)); return this;
            }
        });
        JTableHeader h=t.getTableHeader();
        h.setBackground(SimConstants.BG_CARD); h.setForeground(SimConstants.C_ACCENT2);
        h.setFont(SimConstants.FONT_LABEL); h.setReorderingAllowed(false);
        for(int i=0;i<widths.length&&i<t.getColumnCount();i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        return t;
    }

    private JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBackground(SimConstants.BG_PANEL);
        sp.getViewport().setBackground(SimConstants.BG_PANEL);
        sp.setBorder(BorderFactory.createEmptyBorder());
        return sp;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(SimConstants.FONT_LABEL); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        return b;
    }
}
