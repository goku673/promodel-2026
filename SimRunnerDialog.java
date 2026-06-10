import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * SimRunnerDialog
 *
 * Ventana tipo SimRunner/Optimizer inspirada en ProModel.
 * No depende de ProModel externo: ejecuta experimentos usando el motor interno
 * del proyecto y las Macros importadas del TXT.
 */
public class SimRunnerDialog extends JDialog {
    private final MainFrame owner;
    private final ProModelData baseData;
    private final SimParams baseParams;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRun;
    private JButton btnPlot;
    private JButton btnReport;
    private JProgressBar progress1;
    private JProgressBar progress2;
    private JLabel lblPhase1;
    private JLabel lblPhase2;
    private JLabel lblStatus;
    private JTextArea txtOptions;

    private final java.util.List<ExperimentResult> results = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("0.000");

    public SimRunnerDialog(MainFrame owner, ProModelData data, SimParams params) {
        super(owner, "SimRunner — Untitled", false);
        this.owner = owner;
        this.baseData = data;
        this.baseParams = params == null ? new SimParams() : params.copy();
        setSize(1160, 650);
        setLocationRelativeTo(owner);
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(235, 235, 235));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildLeftNav(), BorderLayout.WEST);
        add(buildMain(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new GridLayout(1, 3));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                new EmptyBorder(8, 12, 8, 12)
        ));
        p.setBackground(new Color(238, 238, 238));

        p.add(stepLabel("⚒", "Setup Project"));
        p.add(stepLabel("♙", "Analyze Model"));
        p.add(stepLabel("◢", "Optimize Model"));
        return p;
    }

    private JLabel stepLabel(String icon, String text) {
        JLabel l = new JLabel(icon + "   " + text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        return l;
    }

    private JPanel buildLeftNav() {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 0));
        p.setPreferredSize(new Dimension(135, 0));
        p.setBorder(new EmptyBorder(10, 8, 10, 8));
        p.setBackground(new Color(230, 230, 230));

        p.add(navLabel("Set options"));
        p.add(navLabel("Seek optimum"));
        p.add(navLabel("Response plot"));
        p.add(navLabel("Final report"));
        return p;
    }

    private JLabel navLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setOpaque(true);
        l.setBackground(new Color(242, 242, 242));
        l.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190)));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JPanel buildMain() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.setBackground(new Color(235, 235, 235));

        JLabel instruction = new JLabel("Click the run button to start the optimization");
        instruction.setForeground(new Color(20, 45, 130));
        instruction.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        root.add(instruction, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(8, 8));
        body.setOpaque(false);
        body.add(buildButtonsAndProgress(), BorderLayout.NORTH);
        body.add(buildTable(), BorderLayout.CENTER);
        body.add(buildOptionsPanel(), BorderLayout.SOUTH);
        root.add(body, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildButtonsAndProgress() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        btnRun = new JButton("Run");
        btnPlot = new JButton("Performance Plot");
        btnReport = new JButton("Final Report");
        btnPlot.setEnabled(false);
        btnReport.setEnabled(false);

        btnRun.addActionListener(this::runExperiments);
        btnPlot.addActionListener(e -> new PerformancePlotDialog(this, results).setVisible(true));
        btnReport.addActionListener(e -> showFinalReport());

        buttons.add(btnRun);
        buttons.add(btnPlot);
        buttons.add(btnReport);
        p.add(buttons, BorderLayout.NORTH);

        JPanel conv = new JPanel(new BorderLayout());
        conv.setBorder(BorderFactory.createTitledBorder("Convergence Status"));
        conv.setBackground(new Color(235, 235, 235));

        JPanel left = new JPanel(new GridLayout(2, 2, 8, 2));
        left.setOpaque(false);
        lblPhase1 = new JLabel("Phase 1:");
        lblPhase2 = new JLabel("Phase 2:");
        progress1 = new JProgressBar();
        progress2 = new JProgressBar();
        left.add(lblPhase1); left.add(progress1);
        left.add(lblPhase2); left.add(progress2);
        conv.add(left, BorderLayout.CENTER);

        lblStatus = new JLabel("Ready", SwingConstants.CENTER);
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        p.add(conv, BorderLayout.CENTER);
        p.add(lblStatus, BorderLayout.SOUTH);
        return p;
    }

    private JScrollPane buildTable() {
        java.util.List<String> cols = new ArrayList<>();
        cols.add("Experiment");
        cols.add("Objective Function");
        cols.add(getObjectiveName());
        for (ProModelData.MacroDef m : baseData.macros) {
            if (isNumeric(m.text)) cols.add(m.id);
        }

        model = new DefaultTableModel(cols.toArray(new String[0]), 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return new JScrollPane(table);
    }

    private JPanel buildOptionsPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Opciones detectadas"));
        p.setBackground(new Color(235, 235, 235));
        txtOptions = new JTextArea(4, 20);
        txtOptions.setEditable(false);
        txtOptions.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtOptions.setText(buildOptionsText());
        p.add(new JScrollPane(txtOptions), BorderLayout.CENTER);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setBackground(new Color(235, 235, 235));
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        p.add(close);
        return p;
    }

    private String buildOptionsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Macros variables para optimización:\n");
        for (ProModelData.MacroDef m : baseData.macros) {
            if (isNumeric(m.text)) {
                int v = parseInt(m.text, 1);
                sb.append("  ").append(m.id).append(" = ").append(v)
                  .append("  rango sugerido: ").append(Math.max(1, v - 2)).append("..").append(v + 2).append('\n');
            }
        }
        sb.append("\nObjetivo: maximizar ").append(getObjectiveName()).append('\n');
        sb.append("Duración usada: ").append(getRunDuration()).append(" minutos");
        return sb.toString();
    }

    private void runExperiments(ActionEvent event) {
        if (baseData == null || baseData.macros.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay macros numéricas para optimizar.", "SimRunner", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnRun.setEnabled(false);
        btnPlot.setEnabled(false);
        btnReport.setEnabled(false);
        results.clear();
        model.setRowCount(0);
        lblStatus.setText("Optimization running...");

        java.util.List<Map<String, Integer>> combinations = buildCombinations(24);
        progress1.setMaximum(Math.max(1, combinations.size()));
        progress2.setMaximum(Math.max(1, combinations.size()));
        progress1.setValue(0);
        progress2.setValue(0);

        SwingWorker<Void, ExperimentResult> worker = new SwingWorker<Void, ExperimentResult>() {
            @Override
            protected Void doInBackground() {
                int i = 1;
                for (Map<String, Integer> combo : combinations) {
                    ExperimentResult r = runOneExperiment(i, combo);
                    publish(r);
                    setProgress((int)((i * 100.0) / combinations.size()));
                    i++;
                }
                return null;
            }

            @Override
            protected void process(java.util.List<ExperimentResult> chunks) {
                for (ExperimentResult r : chunks) {
                    results.add(r);
                    addResultRow(r);
                    progress1.setValue(results.size());
                    progress2.setValue(results.size());
                    lblPhase1.setText("Phase 1: Generation:" + Math.max(1, results.size() / 6));
                    lblPhase2.setText("Phase 2: Experiment:" + results.size());
                }
            }

            @Override
            protected void done() {
                results.sort((a, b) -> Double.compare(b.objective, a.objective));
                reloadSortedTable();
                lblStatus.setText("Optimization Converged");
                btnRun.setEnabled(true);
                btnPlot.setEnabled(!results.isEmpty());
                btnReport.setEnabled(!results.isEmpty());
            }
        };
        worker.execute();
    }

    private java.util.List<Map<String, Integer>> buildCombinations(int max) {
        java.util.List<ProModelData.MacroDef> numeric = new ArrayList<>();
        for (ProModelData.MacroDef m : baseData.macros) if (isNumeric(m.text)) numeric.add(m);

        java.util.List<Map<String, Integer>> combos = new ArrayList<>();
        buildCombinationRec(numeric, 0, new LinkedHashMap<>(), combos, max);
        return combos;
    }

    private void buildCombinationRec(java.util.List<ProModelData.MacroDef> macros, int idx,
                                     Map<String, Integer> current,
                                     java.util.List<Map<String, Integer>> out, int max) {
        if (out.size() >= max) return;
        if (idx >= macros.size()) {
            out.add(new LinkedHashMap<>(current));
            return;
        }
        ProModelData.MacroDef m = macros.get(idx);
        int base = parseInt(m.text, 1);
        int min = Math.max(1, base - 2);
        int maxVal = base + 2;
        for (int v = min; v <= maxVal && out.size() < max; v++) {
            current.put(m.id, v);
            buildCombinationRec(macros, idx + 1, current, out, max);
        }
    }

    private ExperimentResult runOneExperiment(int number, Map<String, Integer> combo) {
        ProModelData data = deepCopy(baseData);
        for (ProModelData.MacroDef m : data.macros) {
            Integer v = combo.get(m.id);
            if (v != null) m.text = String.valueOf(v);
        }
        data.resolveResourceHomes();

        SimParams params = baseParams.copy();
        params.disableAnimation = true;
        if (params.duracion <= 0) params.duracion = getRunDuration();
        params.semilla = baseParams.semilla + number;

        SimState st = new SimState(params);
        st.loadFromData(data);
        SimEngine engine = new SimEngine(st);
        engine.init();

        double until = 0.0;
        while (st.running && until <= params.duracion + 1) {
            until += 1.0;
            engine.stepUntil(until);
        }

        String objectiveLoc = getObjectiveLocationName(data);
        Loc loc = st.loc(objectiveLoc);
        double util = loc == null ? 0 : loc.utilLive(st.clk, st.clk);
        ExperimentResult r = new ExperimentResult();
        r.number = number;
        r.objective = util;
        r.locationUtilization = util;
        r.macros.putAll(combo);
        return r;
    }

    private void addResultRow(ExperimentResult r) {
        java.util.List<Object> row = new ArrayList<>();
        row.add(r.number);
        row.add(df.format(r.objective));
        row.add(df.format(r.locationUtilization));
        for (ProModelData.MacroDef m : baseData.macros) {
            if (isNumeric(m.text)) row.add(r.macros.get(m.id));
        }
        model.addRow(row.toArray());
    }

    private void reloadSortedTable() {
        model.setRowCount(0);
        for (ExperimentResult r : results) addResultRow(r);
    }

    private void showFinalReport() {
        if (results.isEmpty()) return;
        ExperimentResult best = results.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("FINAL REPORT\n\n");
        sb.append("Mejor experimento: ").append(best.number).append('\n');
        sb.append("Objective Function: ").append(df.format(best.objective)).append('\n');
        sb.append(getObjectiveName()).append(": ").append(df.format(best.locationUtilization)).append("%\n\n");
        sb.append("Macros recomendadas:\n");
        for (Map.Entry<String, Integer> e : best.macros.entrySet()) {
            sb.append("  ").append(e.getKey()).append(" = ").append(e.getValue()).append('\n');
        }
        JTextArea area = new JTextArea(sb.toString(), 16, 45);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Final Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getObjectiveName() {
        String loc = getObjectiveLocationName(baseData);
        return loc + ": % Utilization";
    }

    private String getObjectiveLocationName(ProModelData data) {
        if (data == null || data.locations.isEmpty()) return "Location";
        for (ProModelData.LocDef l : data.locations) {
            if (l.cap != null && !l.cap.equalsIgnoreCase("INF")) return l.name;
        }
        return data.locations.get(0).name;
    }

    private double getRunDuration() {
        if (baseParams != null && baseParams.duracion > 0) return baseParams.duracion;
        return 60.0 * 60.0; // 60 horas, parecido al ejemplo de ProModel/SimRunner
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        try { Integer.parseInt(s.trim()); return true; } catch (Exception e) { return false; }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    @SuppressWarnings("unchecked")
    private <T extends Serializable> T deepCopy(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            return (T) ois.readObject();
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo copiar el modelo para SimRunner", ex);
        }
    }

    static class ExperimentResult {
        int number;
        double objective;
        double locationUtilization;
        Map<String, Integer> macros = new LinkedHashMap<>();
    }
}

class PerformancePlotDialog extends JDialog {
    private final java.util.List<SimRunnerDialog.ExperimentResult> results;

    PerformancePlotDialog(Dialog owner, java.util.List<SimRunnerDialog.ExperimentResult> results) {
        super(owner, "Performance Measures Plot", false);
        this.results = new ArrayList<>(results);
        setSize(500, 420);
        setLocationRelativeTo(owner);
        add(new PlotPanel(), BorderLayout.CENTER);
    }

    class PlotPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int left = 55, top = 25, right = 25, bottom = 55;
            int cw = w - left - right, ch = h - top - bottom;
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setColor(new Color(230, 230, 230));
            for (int i = 0; i <= 10; i++) {
                int x = left + i * cw / 10;
                int y = top + i * ch / 10;
                g2.drawLine(x, top, x, top + ch);
                g2.drawLine(left, y, left + cw, y);
            }
            g2.setColor(Color.BLACK);
            g2.drawRect(left, top, cw, ch);
            g2.drawString("Objective Value", 8, top + 20);
            g2.drawString("Experiments", left + cw / 2 - 30, h - 18);

            if (results.isEmpty()) {
                g2.dispose();
                return;
            }
            double max = 1;
            for (SimRunnerDialog.ExperimentResult r : results) max = Math.max(max, r.objective);
            double upper = Math.max(10, Math.ceil(max / 10.0) * 10.0);

            int prevX = -1, prevY = -1;
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0, 170, 50));
            for (int i = 0; i < results.size(); i++) {
                SimRunnerDialog.ExperimentResult r = results.get(i);
                int x = left + (results.size() == 1 ? 0 : i * cw / (results.size() - 1));
                int y = top + ch - (int) ((r.objective / upper) * ch);
                if (prevX >= 0) g2.drawLine(prevX, prevY, x, y);
                g2.fillOval(x - 3, y - 3, 6, 6);
                prevX = x; prevY = y;
            }
            g2.setColor(Color.RED);
            int maxY = top + ch - (int)((max / upper) * ch);
            g2.drawLine(left, maxY, left + cw, maxY);
            g2.setColor(Color.BLACK);
            g2.drawString("Obj.Funct.", left + cw - 110, top + ch + 32);
            g2.setColor(Color.RED);
            g2.drawString("Maximum", left + cw - 40, top + ch + 32);
            g2.dispose();
        }
    }
}
