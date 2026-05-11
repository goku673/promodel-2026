import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;


/**
 * MainFrame - Ventana principal del simulador Multi-Engrane.
 *
 * Layout:
 *   NORTH  → Header con título y reloj
 *   CENTER → FactoryPanel (animación de la fábrica)
 *   SOUTH  → StatsPanel (tabla de estadísticas)
 *   EAST   → ControlPanel (controles de simulación)
 */
public class MainFrame extends JFrame {

    // Parámetros editables por el usuario
    SimParams params = new SimParams();

    // Componentes de simulación
    SimState   state;
    SimEngine  engine;
    SimWorker  worker;

    // Paneles de la GUI
    FactoryPanel  factoryPanel;
    StatsPanel    statsPanel;
    ControlPanel  controlPanel;
    JLabel        lblClock;
    JLabel        lblStatus;

    public MainFrame() {
        super("⚙ Multi-Engrane — Simulador de Fabricación de Engranes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(SimConstants.BG_DARK);
        buildUI();
        buildMenu();
    }

    // ── Construcción de la interfaz ───────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(),   BorderLayout.SOUTH);

        controlPanel = new ControlPanel(this);
        add(controlPanel,        BorderLayout.EAST);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SimConstants.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SimConstants.C_ACCENT));
        h.setPreferredSize(new Dimension(0, 58));

        // Título izquierda
        JLabel title = new JLabel("  ⚙  MULTI-ENGRANE  —  Simulador de Fabricación de Engranes");
        title.setFont(SimConstants.FONT_TITLE);
        title.setForeground(SimConstants.C_TEXT);
        h.add(title, BorderLayout.WEST);

        // Reloj y estado derecha
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        right.setOpaque(false);

        lblStatus = new JLabel("DETENIDO");
        lblStatus.setFont(SimConstants.FONT_LABEL);
        lblStatus.setForeground(SimConstants.C_MUTED);

        lblClock = new JLabel("T = 0.00 min");
        lblClock.setFont(SimConstants.FONT_MONO);
        lblClock.setForeground(SimConstants.C_ACCENT2);

        right.add(lblStatus);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(lblClock);
        h.add(right, BorderLayout.EAST);

        return h;
    }

    private JPanel buildCenterPanel() {
        factoryPanel = new FactoryPanel();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SimConstants.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 4));
        wrapper.add(factoryPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildSouthPanel() {
        statsPanel = new StatsPanel();
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SimConstants.BG_DARK);
        wrapper.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 4));
        wrapper.add(statsPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private void buildMenu() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(SimConstants.BG_CARD);
        mb.setBorder(BorderFactory.createEmptyBorder());

        // Menú Simulación
        JMenu mSim = darkMenu("Simulación");
        JMenuItem miParams   = darkItem("⚙  Parámetros...");
        JMenuItem miStart    = darkItem("▶  Iniciar");
        JMenuItem miPause    = darkItem("⏸  Pausar / Reanudar");
        JMenuItem miStop     = darkItem("⏹  Detener");
        JMenuItem miReset    = darkItem("↺  Resetear");
        JMenuItem miCharts   = darkItem("📊  Ver Gráficas...");

        miParams.addActionListener(e -> showParamsDialog());
        miStart .addActionListener(e -> startSimulation());
        miPause .addActionListener(e -> togglePause());
        miStop  .addActionListener(e -> stopSimulation());
        miReset .addActionListener(e -> resetSimulation());
        miCharts.addActionListener(e -> showCharts());

        mSim.add(miParams); mSim.addSeparator();
        mSim.add(miStart); mSim.add(miPause); mSim.add(miStop);
        mSim.addSeparator();
        mSim.add(miReset); mSim.addSeparator(); mSim.add(miCharts);
        mb.add(mSim);

        // Menú Ayuda
        JMenu mHelp = darkMenu("Ayuda");
        JMenuItem miAbout = darkItem("Acerca de...");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Multi-Engrane Simulator v1.0\n" +
            "Simulación de Eventos Discretos (DES)\n" +
            "Java Swing — Sin dependencias externas\n\n" +
            "Proceso: BARRA → CONVEYOR_1 → ALMACEN_1 → CORTADORA\n" +
            "         → TORNO → CONVEYOR_2 → FRESADORA → ALMACEN_2\n" +
            "         → PINTURA → INSPECCION_1 → EMPAQUE → EMBARQUE",
            "Acerca de Multi-Engrane Simulator",
            JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(miAbout);
        mb.add(mHelp);

        setJMenuBar(mb);
    }

    // ── Control de simulación ─────────────────────────────────────────────

    public void startSimulation() {
        if (worker != null && !worker.isDone()) worker.cancel(true);

        state  = new SimState(params.copy());
        engine = new SimEngine(state);
        worker = new SimWorker(state, engine, this::onTick, this::onFinished);

        factoryPanel.setState(state);
        statsPanel.setState(state);

        setStatus("CORRIENDO", SimConstants.C_SUCCESS);
        controlPanel.onStarted();
        worker.execute();
    }

    public void togglePause() {
        if (state == null) return;
        state.paused = !state.paused;
        setStatus(state.paused ? "PAUSADO" : "CORRIENDO",
                  state.paused ? SimConstants.C_WARNING : SimConstants.C_SUCCESS);
        controlPanel.onPauseToggled(state.paused);
    }

    public void stopSimulation() {
        if (worker != null) worker.cancel(true);
        if (state  != null) { state.running = false; state.finished = true; }
        setStatus("DETENIDO", SimConstants.C_MUTED);
        controlPanel.onStopped();
    }

    public void resetSimulation() {
        stopSimulation();
        factoryPanel.setState(null);
        statsPanel.setState(null);
        lblClock.setText("T = 0.00 min");
    }

    public void setSpeedMult(double mult) {
        if (state != null) state.speedMult = mult;
    }

    // ── Callbacks del SimWorker ───────────────────────────────────────────

    private void onTick() {
        if (state == null) return;
        // Actualizar reloj
        lblClock.setText(String.format("T = %.1f min  (%.1f h)",
                state.clk, state.clk / 60.0));
        // Repintar fábrica y tabla
        factoryPanel.repaint();
        statsPanel.refresh();
        controlPanel.refreshCounters();
    }

    private void onFinished() {
        setStatus("FINALIZADO", SimConstants.C_ACCENT2);
        controlPanel.onStopped();
        if (state != null) {
            JOptionPane.showMessageDialog(this,
                String.format("✅ Simulación finalizada\n\n" +
                    "Tiempo simulado:     %.1f min (%.1f h)\n" +
                    "Barras llegadas:     %d\n" +
                    "Piezas finalizadas:  %d\n" +
                    "Piezas en sistema:   %d",
                    state.clk, state.clk / 60.0,
                    state.barrasLlegadas.get(),
                    state.piezasFinales.get(),
                    state.enSistema),
                "Simulación Completada",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ── Diálogos ─────────────────────────────────────────────────────────

    public void showParamsDialog() {
        ParamsDialog dlg = new ParamsDialog(this, params);
        dlg.setVisible(true);
        if (dlg.accepted) params = dlg.result;
    }

    public void showCharts() {
        if (state == null) {
            JOptionPane.showMessageDialog(this,
                "Inicia la simulación primero.",
                "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ChartsDialog(this, state).setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void setStatus(String text, Color color) {
        lblStatus.setText(text);
        lblStatus.setForeground(color);
    }

    private JMenu darkMenu(String text) {
        JMenu m = new JMenu(text);
        m.setForeground(SimConstants.C_TEXT);
        m.setFont(SimConstants.FONT_LABEL);
        m.setOpaque(false);
        return m;
    }

    private JMenuItem darkItem(String text) {
        JMenuItem i = new JMenuItem(text);
        i.setBackground(SimConstants.BG_CARD);
        i.setForeground(SimConstants.C_TEXT);
        i.setFont(SimConstants.FONT_SMALL);
        i.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return i;
    }
}
