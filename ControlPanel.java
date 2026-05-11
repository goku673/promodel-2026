import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ControlPanel - Panel lateral de controles de simulación.
 */
public class ControlPanel extends JPanel {

    private final MainFrame frame;
    private JButton btnStart, btnPause, btnStop, btnReset;
    private JButton btnParams, btnCharts;
    private JSlider speedSlider;
    private JLabel  lblSpeed;
    private JLabel  lblBarras, lblPiezas, lblEnSistema;
    private JLabel  lblClockBig;

    private static final double[] SPEEDS = {0.25, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0};
    private static final String[] SPEED_LABELS = {"x0.25","x0.5","x1","x2","x5","x10","x20","x50"};

    public ControlPanel(MainFrame frame) {
        this.frame = frame;
        setBackground(SimConstants.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, SimConstants.C_BORDER));
        setPreferredSize(new Dimension(195, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        build();
    }

    private void build() {
        add(Box.createVerticalStrut(10));
        add(section("CONTROL"));
        add(Box.createVerticalStrut(6));
        add(makeSimButtons());
        add(Box.createVerticalStrut(12));
        add(section("VELOCIDAD"));
        add(Box.createVerticalStrut(6));
        add(makeSpeedPanel());
        add(Box.createVerticalStrut(12));
        add(section("ESTADÍSTICAS"));
        add(Box.createVerticalStrut(6));
        add(makeCounters());
        add(Box.createVerticalStrut(12));
        add(section("HERRAMIENTAS"));
        add(Box.createVerticalStrut(6));
        add(makeToolButtons());
        add(Box.createVerticalGlue());
        add(makeClockPanel());
        add(Box.createVerticalStrut(10));
    }

    private JPanel makeSimButtons() {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btnStart = simBtn("▶  Iniciar",  new Color(40,140,60));
        btnPause = simBtn("⏸  Pausar",   new Color(150,100,10));
        btnStop  = simBtn("⏹  Detener",  new Color(140,30,30));
        btnReset = simBtn("↺  Resetear", new Color(50,50,100));
        btnStart.addActionListener(e -> frame.startSimulation());
        btnPause.addActionListener(e -> frame.togglePause());
        btnStop .addActionListener(e -> frame.stopSimulation());
        btnReset.addActionListener(e -> frame.resetSimulation());
        btnPause.setEnabled(false);
        btnStop .setEnabled(false);
        p.add(btnStart); p.add(btnPause); p.add(btnStop); p.add(btnReset);
        return p;
    }

    private JPanel makeSpeedPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        lblSpeed = new JLabel("Velocidad: x1", SwingConstants.CENTER);
        lblSpeed.setFont(SimConstants.FONT_LABEL);
        lblSpeed.setForeground(SimConstants.C_ACCENT2);

        speedSlider = new JSlider(0, SPEEDS.length - 1, 2);
        speedSlider.setBackground(SimConstants.BG_PANEL);
        speedSlider.setForeground(SimConstants.C_TEXT);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setOpaque(false);

        java.util.Hashtable<Integer,JLabel> labels = new java.util.Hashtable<>();
        for (int i = 0; i < SPEED_LABELS.length; i += 2) {
            JLabel lbl = new JLabel(SPEED_LABELS[i]);
            lbl.setFont(new Font("Arial", Font.PLAIN, 8));
            lbl.setForeground(SimConstants.C_MUTED);
            labels.put(i, lbl);
        }
        speedSlider.setLabelTable(labels);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            int idx = speedSlider.getValue();
            double mult = SPEEDS[Math.min(idx, SPEEDS.length-1)];
            lblSpeed.setText("Velocidad: " + SPEED_LABELS[idx]);
            frame.setSpeedMult(mult);
        });

        JPanel quick = new JPanel(new GridLayout(1, 3, 3, 0));
        quick.setOpaque(false);
        quick.add(quickBtn("x1",  () -> speedSlider.setValue(2)));
        quick.add(quickBtn("x5",  () -> speedSlider.setValue(4)));
        quick.add(quickBtn("x20", () -> speedSlider.setValue(6)));

        p.add(lblSpeed,    BorderLayout.NORTH);
        p.add(speedSlider, BorderLayout.CENTER);
        p.add(quick,       BorderLayout.SOUTH);
        return p;
    }

    private JPanel makeCounters() {
        JPanel p = new JPanel(new GridLayout(3, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        lblBarras    = counterLabel("Barras llegadas: 0");
        lblPiezas    = counterLabel("Piezas finales:  0");
        lblEnSistema = counterLabel("En sistema:      0");
        p.add(lblBarras); p.add(lblPiezas); p.add(lblEnSistema);
        return p;
    }

    private JPanel makeToolButtons() {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btnParams = toolBtn("⚙  Parámetros...");
        btnCharts = toolBtn("📊  Gráficas...");
        btnParams.addActionListener(e -> frame.showParamsDialog());
        btnCharts.addActionListener(e -> frame.showCharts());
        p.add(btnParams); p.add(btnCharts);
        return p;
    }

    private JPanel makeClockPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(SimConstants.BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 8, 0, 8, SimConstants.C_BORDER),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        JLabel lbl = new JLabel("RELOJ SIM.", SwingConstants.CENTER);
        lbl.setFont(new Font("Arial", Font.PLAIN, 9));
        lbl.setForeground(SimConstants.C_MUTED);
        lblClockBig = new JLabel("0:00", SwingConstants.CENTER);
        lblClockBig.setFont(new Font("Monospaced", Font.BOLD, 26));
        lblClockBig.setForeground(SimConstants.C_ACCENT2);
        JLabel lblUnit = new JLabel("horas : minutos", SwingConstants.CENTER);
        lblUnit.setFont(new Font("Arial", Font.PLAIN, 8));
        lblUnit.setForeground(SimConstants.C_MUTED);
        p.add(lbl, BorderLayout.NORTH);
        p.add(lblClockBig, BorderLayout.CENTER);
        p.add(lblUnit, BorderLayout.SOUTH);
        return p;
    }

    // ── Callbacks del MainFrame ───────────────────────────────────────────
    public void onStarted()                        { btnStart.setEnabled(false); btnPause.setEnabled(true); btnStop.setEnabled(true); btnParams.setEnabled(false); }
    public void onPauseToggled(boolean paused)     { btnPause.setText(paused ? "▶  Reanudar" : "⏸  Pausar"); }
    public void onStopped()                        { btnStart.setEnabled(true); btnPause.setEnabled(false); btnStop.setEnabled(false); btnPause.setText("⏸  Pausar"); btnParams.setEnabled(true); }

    public void refreshCounters() {
        if (frame.state == null) return;
        lblBarras   .setText("Barras llegadas: " + frame.state.barrasLlegadas.get());
        lblPiezas   .setText("Piezas finales:  " + frame.state.piezasFinales.get());
        lblEnSistema.setText("En sistema:      " + frame.state.enSistema);
        double clk = frame.state.clk;
        lblClockBig.setText(String.format("%d:%02d", (int)(clk/60), (int)(clk%60)));
    }

    // ── Builder helpers ───────────────────────────────────────────────────
    private JPanel section(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Arial", Font.BOLD, 9));
        lbl.setForeground(SimConstants.C_ACCENT);
        JSeparator sep = new JSeparator();
        sep.setForeground(SimConstants.C_ACCENT);
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.add(lbl, BorderLayout.WEST);
        row.add(sep, BorderLayout.CENTER);
        p.add(row, BorderLayout.CENTER);
        return p;
    }

    private JButton simBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(SimConstants.FONT_LABEL); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7, 6, 7, 6));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited (MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private JButton toolBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(SimConstants.BG_CARD); b.setForeground(SimConstants.C_ACCENT2);
        b.setFont(SimConstants.FONT_SMALL); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SimConstants.C_BORDER),
            BorderFactory.createEmptyBorder(5, 4, 5, 4)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton quickBtn(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40,40,70)); b.setForeground(SimConstants.C_TEXT);
        b.setFont(new Font("Arial", Font.BOLD, 9)); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JLabel counterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SimConstants.FONT_SMALL); l.setForeground(SimConstants.C_TEXT);
        l.setBackground(SimConstants.BG_CARD); l.setOpaque(true);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SimConstants.C_BORDER),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)));
        return l;
    }
}
