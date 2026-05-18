import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * ResultsDialog - Ventana de resultados estilo ProModel.
 * Pestañas:
 *   1. Entidad Resumen Table
 *   2. Entity Summary (gráfica barras - Total Exits)
 *   3. Locación Resumen Table
 *   4. Location Summary (gráfica barras - % Utilización)
 *   5. Resource Resumen Table
 *   6. Resource Summary (gráfica barras - % Utilización)
 */
public class ResultsDialog extends JDialog {

    private final SimState     state;
    private final DecimalFormat df  = new DecimalFormat("#,##0.00");
    private final DecimalFormat df0 = new DecimalFormat("#,##0");

    // Colores ProModel
    private static final Color BAR_TOP    = new Color(100, 181, 246);
    private static final Color BAR_BOT    = new Color( 21, 101, 192);
    private static final Color CHART_BG   = new Color(245, 248, 252);
    private static final Color GRID_COLOR = new Color(210, 220, 230);
    private static final Color AXIS_COLOR = new Color(100, 100, 100);

    public ResultsDialog(JFrame owner, SimState state) {
        super(owner, "Resultados de Simulación", true);
        this.state = state;
        setSize(1100, 640);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(Color.WHITE);
        build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    private void build() {
        setLayout(new BorderLayout());

        // Header
        JLabel hdr = new JLabel("  Reporte de Resultados — Escenario: Baseline",
                                SwingConstants.LEFT);
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 14));
        hdr.setForeground(Color.WHITE);
        hdr.setBackground(new Color(41, 98, 155));
        hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        add(hdr, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabs.setBackground(CHART_BG);

        tabs.addTab("Entidad Resumen Table",   buildEntityTableTab());
        tabs.addTab("Entity Summary",          buildEntityChartTab());
        tabs.addTab("Locación Resumen Table",  buildLocationTableTab());
        tabs.addTab("Location Summary",        buildLocationChartTab());
        tabs.addTab("Resource Resumen Table",  buildResourceTableTab());
        tabs.addTab("Resource Summary",        buildResourceChartTab());

        add(tabs, BorderLayout.CENTER);

        // Barra inferior
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bot.setBackground(new Color(235, 240, 248));
        bot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(180, 200, 220)));
        JButton btnClose = styledBtn("Cerrar", new Color(41, 98, 155));
        btnClose.addActionListener(e -> dispose());
        bot.add(btnClose);
        add(bot, BorderLayout.SOUTH);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 1 — ENTIDAD RESUMEN (tabla)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildEntityTableTab() {
        String[] cols = {
            "Nombre", "Total Salidas", "Cantidad actual En Sistema",
            "Tiempo En Sistema Promedio (Min)",
            "Tiempo En lógica de movimiento Promedio (Min)",
            "Tiempo Esperando Promedio (Min)",
            "Tiempo En Operación Promedio (Min)",
            "Tiempo de Bloqueo Promedio (Min)"
        };

        java.util.List<Object[]> rows = new ArrayList<>();
        if (state.currentData != null) {
            for (ProModelData.EntDef e : state.currentData.entities) {
                rows.add(row(e.name, "0.00", "0.00", "0.00", "0.00", "0.00", "0.00", "0.00"));
            }
        }

        int pf = state.entidadesSalientes.get();
        double tSist = pf > 0 ? state.clk / pf * 6 : 0;
        rows.add(new Object[]{
            "TOTALES",
            df.format(pf),
            df.format(state.enSistema),
            df.format(tSist),
            "0.00", "0.00", "0.00", "0.00"
        });

        return tablePanel("Entidad Resumen", cols, rows);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 2 — ENTITY SUMMARY (gráfica barras - Total Exits)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildEntityChartTab() {
        java.util.List<String> lbl = new ArrayList<>();
        java.util.List<Double> val = new ArrayList<>();
        if (state.currentData != null) {
            for (ProModelData.EntDef e : state.currentData.entities) {
                lbl.add(e.name);
                val.add(0.0); // Stats per entity can be implemented later
            }
        }
        lbl.add("TOTALES");
        val.add((double)state.entidadesSalientes.get());
        
        String[] labels = lbl.toArray(new String[0]);
        double[] values = val.stream().mapToDouble(Double::doubleValue).toArray();
        return barChartPanel("Entity Summary — Total Exits", "Total Exits", labels, values);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 3 — LOCACIÓN RESUMEN (tabla)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildLocationTableTab() {
        String[] cols = {
            "Name", "Scheduled Time (Hr)", "Capacity", "Total Entries",
            "Average Time Per Entry (Min)", "Average Contents",
            "Maximum Contents", "Current Contents", "% Utilization"
        };

        java.util.List<Object[]> rows = new ArrayList<>();
        double hrs = state.clk / 60.0;

        for (Loc loc : state.locs.values()) {
            String cap = loc.cap == Integer.MAX_VALUE ? "999,999.00" : df.format(loc.cap);
            rows.add(new Object[]{
                loc.name.replace("_", " "),
                df.format(hrs),
                cap,
                df.format(loc.totalEntries),
                df.format(loc.avgTimePerEntry(state.clk)),
                df.format(loc.avgContents(state.clk)),
                df.format(loc.maxCnt),
                df.format(loc.cnt),
                df.format(loc.utilLive(state.clk, state.clk))
            });
        }

        return tablePanel("Location Summary", cols, rows);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 4 — LOCATION SUMMARY (gráfica barras - % Utilización)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildLocationChartTab() {
        java.util.List<String> lbl = new ArrayList<>();
        java.util.List<Double> val = new ArrayList<>();
        for (Loc loc : state.locs.values()) {
            lbl.add(loc.name.replace("_", "\n"));
            val.add(loc.utilLive(state.clk, state.clk));
        }
        String[] labels = lbl.toArray(new String[0]);
        double[] values = val.stream().mapToDouble(Double::doubleValue).toArray();
        return barChartPanel("Location Summary — % Utilization", "% Utilization", labels, values);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 5 — RESOURCE RESUMEN (tabla)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildResourceTableTab() {
        String[] cols = {
            "Nombre", "Unidades", "Tiempo Programado (Hr)",
            "Tiempo de Trabajo (Min)", "Número de Veces Utilizado",
            "Tiempo Por Uso Promedio (Min)",
            "Tiempo Viaje Para Utilizar Promedio (Min)",
            "Tiempo Viaje a Estacionar Promedio (Min)",
            "% Bloqueado En Viaje", "% Utilización"
        };

        java.util.List<Object[]> rows = new ArrayList<>();
        double hrs = state.clk / 60.0;

        for (Res res : state.res.values()) {
            rows.add(new Object[]{
                res.name.replace("_", " "),
                "1.00",
                df.format(hrs),
                df.format(res.workTime),
                df.format(res.timesUsed),
                df.format(res.avgTimePerUse()),
                df.format(res.timesUsed > 0 ? res.travelTime / res.timesUsed : 0),
                "0.00", "0.00",
                df.format(res.utilPct(state.clk))
            });
        }

        return tablePanel("Resource Resumen", cols, rows);
    }

    // ═══════════════════════════════════════════════════════════
    //  TAB 6 — RESOURCE SUMMARY (gráfica barras - % Utilización)
    // ═══════════════════════════════════════════════════════════
    private JPanel buildResourceChartTab() {
        java.util.List<String> lbl = new ArrayList<>();
        java.util.List<Double> val = new ArrayList<>();
        for (Res res : state.res.values()) {
            lbl.add(res.name.replace("_", " "));
            val.add(res.utilPct(state.clk));
        }
        String[] labels = lbl.toArray(new String[0]);
        double[] values = val.stream().mapToDouble(Double::doubleValue).toArray();
        return barChartPanel("Resource Summary — % Utilization", "% Utilization", labels, values);
    }

    // ═══════════════════════════════════════════════════════════
    //  COMPONENTE GENÉRICO: Gráfica de barras estilo ProModel
    // ═══════════════════════════════════════════════════════════
    private JPanel barChartPanel(String title, String yLabel,
                                  String[] labels, double[] values) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(CHART_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Título
        JLabel ttl = new JLabel(title, SwingConstants.CENTER);
        ttl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ttl.setForeground(new Color(40, 80, 130));
        ttl.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        wrapper.add(ttl, BorderLayout.NORTH);

        // Leyenda
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        legend.setOpaque(false);
        JLabel leg = new JLabel("■ Baseline");
        leg.setForeground(BAR_BOT);
        leg.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legend.add(leg);
        wrapper.add(legend, BorderLayout.SOUTH);

        // Canvas de la gráfica
        JPanel canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBarChart((Graphics2D) g, getWidth(), getHeight(),
                             yLabel, labels, values);
            }
        };
        canvas.setBackground(CHART_BG);
        wrapper.add(canvas, BorderLayout.CENTER);

        return wrapper;
    }

    private void drawBarChart(Graphics2D g2, int W, int H,
                              String yLabel, String[] labels, double[] values) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Márgenes
        int left = 68, right = 20, top = 20, bottom = 60;
        int chartW = W - left - right;
        int chartH = H - top - bottom;

        // Fondo
        g2.setColor(Color.WHITE);
        g2.fillRect(left, top, chartW, chartH);
        g2.setColor(new Color(180, 200, 220));
        g2.drawRect(left, top, chartW, chartH);

        if (values.length == 0) return;

        // Escala Y
        double maxVal = 0;
        for (double v : values) maxVal = Math.max(maxVal, v);
        if (maxVal == 0) maxVal = 10;
        // Redondear máximo a un valor "limpio"
        double step = niceStep(maxVal, 6);
        double maxY = Math.ceil(maxVal / step) * step;

        // Líneas de cuadrícula y etiquetas Y
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        int gridLines = (int)(maxY / step);
        for (int i = 0; i <= gridLines; i++) {
            double yVal = i * step;
            int yPx = top + chartH - (int)(yVal / maxY * chartH);
            // línea punteada
            g2.setColor(GRID_COLOR);
            float[] dash = {4f, 3f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT,
                         BasicStroke.JOIN_MITER, 1f, dash, 0f));
            g2.drawLine(left, yPx, left + chartW, yPx);
            g2.setStroke(new BasicStroke(1f));
            // etiqueta
            g2.setColor(AXIS_COLOR);
            String lbl = df.format(yVal);
            int sw = g2.getFontMetrics().stringWidth(lbl);
            g2.drawString(lbl, left - sw - 4, yPx + 4);
        }

        // Etiqueta eje Y (rotada)
        g2.setColor(AXIS_COLOR);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(-Math.PI / 2, 14, top + chartH / 2);
        int sw = g2r.getFontMetrics().stringWidth(yLabel);
        g2r.drawString(yLabel, 14 - sw / 2, top + chartH / 2 + 4);
        g2r.dispose();

        // Barras
        int n = values.length;
        int gap = Math.max(6, chartW / (n * 5));
        int barW = Math.max(10, (chartW - gap * (n + 1)) / n);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < n; i++) {
            int bx = left + gap + i * (barW + gap);
            int barH = (int)(values[i] / maxY * chartH);
            int by = top + chartH - barH;

            // Gradiente azul estilo ProModel
            GradientPaint gp = new GradientPaint(
                bx, by, BAR_TOP,
                bx, by + barH, BAR_BOT);
            g2.setPaint(gp);
            g2.fillRect(bx, by, barW, barH);

            // Borde barra
            g2.setColor(new Color(21, 101, 192));
            g2.setStroke(new BasicStroke(0.5f));
            g2.drawRect(bx, by, barW, barH);

            // Valor encima
            if (barH > 0) {
                g2.setColor(new Color(40, 80, 130));
                String vStr = df.format(values[i]);
                int vsw = fm.stringWidth(vStr);
                g2.drawString(vStr, bx + barW / 2 - vsw / 2, by - 3);
            }

            // Etiqueta X (multilinea si tiene \n)
            g2.setColor(AXIS_COLOR);
            String[] parts = labels[i].split("\n");
            int ly = top + chartH + 14;
            for (String part : parts) {
                int psw = fm.stringWidth(part);
                g2.drawString(part, bx + barW / 2 - psw / 2, ly);
                ly += 12;
            }
        }

        // Ejes
        g2.setColor(new Color(140, 160, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(left, top, left, top + chartH);
        g2.drawLine(left, top + chartH, left + chartW, top + chartH);
    }

    /** Calcula un "paso limpio" para la escala Y */
    private double niceStep(double maxVal, int targetLines) {
        double rawStep = maxVal / targetLines;
        double magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
        double[] niceSteps = {1, 2, 5, 10};
        double step = niceSteps[niceSteps.length - 1] * magnitude;
        for (double ns : niceSteps) {
            if (ns * magnitude >= rawStep) { step = ns * magnitude; break; }
        }
        return step;
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER: crear panel con tabla estilo ProModel
    // ═══════════════════════════════════════════════════════════
    private JPanel tablePanel(String title, String[] cols,
                               java.util.List<Object[]> rows) {
        Object[][] data = new Object[rows.size()][cols.length];
        for (int i = 0; i < rows.size(); i++) data[i] = rows.get(i);

        DefaultTableModel tm = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tm);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setGridColor(new Color(215, 225, 235));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionBackground(new Color(210, 230, 250));

        // Alternar colores de fila
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(242, 247, 253));
                    comp.setForeground(Color.BLACK);
                }
                if (c > 0) setHorizontalAlignment(JLabel.RIGHT);
                else        setHorizontalAlignment(JLabel.LEFT);
                return comp;
            }
        });

        // Header ProModel
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(220, 232, 248));
        header.setForeground(new Color(40, 70, 120));
        header.setFont(new Font("Segoe UI", Font.BOLD, 11));
        header.setPreferredSize(new Dimension(0, 36));
        ((DefaultTableCellRenderer) header.getDefaultRenderer())
            .setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i)
                 .setPreferredWidth(Math.max(130, cols[i].length() * 8));
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(Color.WHITE);

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(196, 218, 240));
        lbl.setForeground(new Color(20, 60, 110));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));

        p.add(lbl, BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ═══════════════════════════════════════════════════════════
    //  UTILIDADES
    // ═══════════════════════════════════════════════════════════
    private Object[] row(String name, String... vals) {
        Object[] r = new Object[1 + vals.length];
        r[0] = name;
        System.arraycopy(vals, 0, r, 1, vals.length);
        return r;
    }

    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 18, 7, 18));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
