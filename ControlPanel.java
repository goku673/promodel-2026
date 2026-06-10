import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ControlPanel - Panel lateral de controles de simulación.
 */
public class ControlPanel extends JPanel {

    private final MainFrame frame;
    private JButton btnStart, btnPause, btnStop, btnReset, btnSimRunner;
    private JSlider speedSlider;
    private JLabel  lblSpeed;
    private JCheckBox chkDisableAnim;
    private JLabel  lblBarras, lblPiezas, lblEnSistema, lblEmbarque;
    private JLabel  lblClockBig;

    private static final double[] SPEEDS = {0.25, 0.5, 1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0, 500.0};
    private static final String[] SPEED_LABELS = {"x0.25","x0.5","x1","x2","x5","x10","x20","x50","x100","MAX"};

    public ControlPanel(MainFrame frame) {
        this.frame = frame;
        setBackground(SimConstants.BG_PANEL);
        setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, SimConstants.C_BORDER));
        setPreferredSize(new Dimension(250, 0));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        build();
    }

    private void build() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JButton btnClose = new JButton("✖");
        btnClose.setForeground(SimConstants.C_MUTED);
        btnClose.setFont(new Font("Arial", Font.BOLD, 14));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(new Color(220, 80, 80)); }
            public void mouseExited(MouseEvent e)  { btnClose.setForeground(SimConstants.C_MUTED); }
        });
        btnClose.addActionListener(e -> {
            this.setVisible(false);
            if(this.getParent() != null) this.getParent().revalidate();
        });
        
        headerPanel.add(btnClose, BorderLayout.EAST);
        add(headerPanel);

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
        add(Box.createVerticalGlue());
        add(makeClockPanel());
        add(Box.createVerticalStrut(10));
    }

    private JPanel makeSimButtons() {
        JPanel p = new JPanel(new GridLayout(5, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btnStart = simBtn("▶  Iniciar",  new Color(40,140,60));
        btnPause = simBtn("⏸  Pausar",   new Color(150,100,10));
        btnStop  = simBtn("⏹  Detener",  new Color(140,30,30));
        btnReset = simBtn("↺  Resetear", new Color(50,50,100));
        btnSimRunner = simBtn("▣  SimRunner", new Color(90,60,150));
        btnStart.addActionListener(e -> frame.startSimulation());
        btnPause.addActionListener(e -> frame.togglePause());
        btnStop .addActionListener(e -> frame.stopSimulation());
        btnReset.addActionListener(e -> frame.resetSimulation());
        btnSimRunner.addActionListener(e -> frame.showSimRunner());
        btnPause.setEnabled(false);
        btnStop .setEnabled(false);
        p.add(btnStart); p.add(btnPause); p.add(btnStop); p.add(btnReset); p.add(btnSimRunner);
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
        for (int i = 0; i < SPEEDS.length; i++) {
            if (SPEEDS[i] == 1.0 || SPEEDS[i] == 5.0 || SPEEDS[i] == 20.0 || SPEEDS[i] == 100.0 || SPEEDS[i] == 500.0) {
                JLabel lbl = new JLabel(SPEEDS[i] == 500.0 ? "MAX" : "x" + (int)SPEEDS[i]);
                lbl.setFont(new Font("Arial", Font.PLAIN, 8));
                lbl.setForeground(SimConstants.C_MUTED);
                labels.put(i, lbl);
            }
        }
        speedSlider.setLabelTable(labels);
        speedSlider.setPaintLabels(true);
        speedSlider.addChangeListener(e -> {
            int idx = speedSlider.getValue();
            double mult = SPEEDS[Math.min(idx, SPEEDS.length-1)];
            lblSpeed.setText("Velocidad: " + SPEED_LABELS[idx]);
            frame.setSpeedMult(mult);
        });

        JPanel quick = new JPanel(new GridLayout(1, 4, 3, 0));
        quick.setOpaque(false);
        quick.add(quickBtn("x1",  () -> speedSlider.setValue(2)));
        quick.add(quickBtn("x5",  () -> speedSlider.setValue(4)));
        quick.add(quickBtn("x100",() -> speedSlider.setValue(8)));
        quick.add(quickBtn("MAX", () -> speedSlider.setValue(9)));

        chkDisableAnim = new JCheckBox("Deshabilitar Animación");
        chkDisableAnim.setOpaque(false);
        chkDisableAnim.setFocusPainted(false);
        chkDisableAnim.setForeground(SimConstants.C_TEXT);
        chkDisableAnim.setFont(SimConstants.FONT_SMALL);
        chkDisableAnim.setToolTipText("Ejecuta la simulación a máxima velocidad sin actualizar gráficos");
        chkDisableAnim.addActionListener(e -> {
            boolean dis = chkDisableAnim.isSelected();
            frame.params.disableAnimation = dis;
            if (frame.state != null) {
                frame.state.params.disableAnimation = dis;
            }
            speedSlider.setEnabled(!dis);
        });

        JPanel pSouth = new JPanel(new BorderLayout(0, 5));
        pSouth.setOpaque(false);
        pSouth.add(quick, BorderLayout.NORTH);
        pSouth.add(chkDisableAnim, BorderLayout.SOUTH);

        p.add(lblSpeed,    BorderLayout.NORTH);
        p.add(speedSlider, BorderLayout.CENTER);
        p.add(pSouth,      BorderLayout.SOUTH);
        return p;
    }

    private JPanel makeCounters() {
        JPanel p = new JPanel(new GridLayout(5, 1, 0, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        lblBarras    = counterLabel("Barras llegadas: 0");
        lblPiezas    = counterLabel("Piezas finales:  0");
        lblEnSistema = counterLabel("En sistema:      0");
        lblEmbarque  = counterLabel("En Embarque:     0");
        p.add(lblBarras); p.add(lblPiezas); p.add(lblEnSistema); p.add(lblEmbarque);
        return p;
    }



    private JLabel  lblReplicas;

    private JPanel makeClockPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 5, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        // Panel de Replicas
        JPanel pLeft = new JPanel(new BorderLayout());
        pLeft.setBackground(SimConstants.BG_CARD);
        pLeft.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SimConstants.C_BORDER, 2),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        
        lblReplicas = new JLabel("Correr 1 de 1", SwingConstants.CENTER);
        lblReplicas.setFont(new Font("Arial", Font.BOLD, 11));
        lblReplicas.setForeground(SimConstants.C_TEXT);
        pLeft.add(lblReplicas, BorderLayout.CENTER);

        // Panel de Reloj
        JPanel pRight = new JPanel(new BorderLayout());
        pRight.setBackground(SimConstants.BG_CARD);
        pRight.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SimConstants.C_BORDER, 2),
            BorderFactory.createEmptyBorder(6, 4, 6, 4)));
        
        lblClockBig = new JLabel("<html><font color='red'>HR:</font>00 <font color='red'>MIN:</font>00</html>", SwingConstants.CENTER);
        lblClockBig.setFont(new Font("Arial", Font.BOLD, 12));
        lblClockBig.setForeground(SimConstants.C_TEXT);
        pRight.add(lblClockBig, BorderLayout.CENTER);

        p.add(pLeft);
        p.add(pRight);
        
        return p;
    }

    // ── Callbacks del MainFrame ───────────────────────────────────────────
    public void onStarted()                        { btnStart.setEnabled(false); btnPause.setEnabled(true); btnStop.setEnabled(true); }
    public void onPauseToggled(boolean paused)     { btnPause.setText(paused ? "▶  Reanudar" : "⏸  Pausar"); }
    public void onStopped()                        { btnStart.setEnabled(true); btnPause.setEnabled(false); btnStop.setEnabled(false); btnPause.setText("⏸  Pausar"); }

    public void refreshCounters() {
        if (frame.state == null) return;
<<<<<<< Updated upstream
        lblBarras   .setText("Barras llegadas: " + frame.state.barrasLlegadas.get());
        lblPiezas   .setText("Piezas finales:  " + frame.state.piezasFinales.get());
        lblEnSistema.setText("En sistema:      " + frame.state.enSistema);
        lblEmbarque .setText("En Embarque:     " + frame.state.embarqueTotales.get());
=======
        lblBarras   .setText("Entidades Creadas: " + frame.state.entidadesCreadas.get());
        lblPiezas   .setText("Ent. Salientes:    " + frame.state.entidadesSalientes.get());
        lblEnSistema.setText("En sistema:        " + frame.state.enSistema);
        lblEmbarque .setText("Total Procesadas:  " + frame.state.totalProcesadas.get());
        
        String rep = frame.params.replicas;
        if (rep == null || rep.trim().isEmpty()) rep = "1";
        lblReplicas.setText("Correr 1 de " + rep);
        
>>>>>>> Stashed changes
        double clk = frame.state.clk;
        lblClockBig.setText(formatClockHtml(clk));
        frame.updateHeaderClock(clk);
    }


    public void refreshClockOnly(double clk) {
        if (lblClockBig != null) {
            lblClockBig.setText(formatClockHtml(clk));
        }
    }

    private String formatClockHtml(double clk) {
        int totalMinutes = Math.max(0, (int)Math.floor(clk));
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("<html><font color='red'>HR:</font>%02d <font color='red'>MIN:</font>%02d</html>", hours, minutes);
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
