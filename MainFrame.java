import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * MainFrame - Ventana principal del simulador Promodel-Lite.
 *
 * Ahora soporta:
 * - Importar modelo .txt
 * - Guardar proyecto configurado .pmsim
 * - Abrir proyecto configurado .pmsim
 */
public class MainFrame extends JFrame {

    // Parámetros editables por el usuario
    SimParams params = new SimParams();
    ProModelData currentData;

    // Componentes de simulación
    SimState state;
    SimEngine engine;
    SimWorker worker;

    // Paneles de la GUI
    FactoryPanel factoryPanel;
    StatsPanel statsPanel;
    ControlPanel controlPanel;

    JLabel lblStatus;
    JLabel lblHeaderClock;

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(),   BorderLayout.SOUTH);

        factoryPanel = new FactoryPanel();
        statsPanel = new StatsPanel();
        controlPanel = new ControlPanel(this);
        add(controlPanel,        BorderLayout.EAST);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SimConstants.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SimConstants.C_ACCENT));
        h.setPreferredSize(new Dimension(0, 58));

        JLabel title = new JLabel("  ⚙  PROMODEL-LITE  —  Simulador de Fabricación de Engranes");
        title.setFont(SimConstants.FONT_TITLE);
        title.setForeground(SimConstants.C_TEXT);
        h.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        right.setOpaque(false);

        lblHeaderClock = new JLabel("HR:00 MIN:00");
        lblHeaderClock.setFont(new Font("Arial", Font.BOLD, 13));
        lblHeaderClock.setForeground(new Color(220, 30, 30));
        lblHeaderClock.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SimConstants.C_BORDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        lblHeaderClock.setToolTipText("Reloj de simulación: horas y minutos simulados");
        right.add(lblHeaderClock);

        lblStatus = new JLabel("DETENIDO");
        lblStatus.setFont(SimConstants.FONT_LABEL);
        lblStatus.setForeground(SimConstants.C_MUTED);
        right.add(lblStatus);

        JButton btnToggle = new JButton("🎛 Panel");
        btnToggle.setFont(SimConstants.FONT_SMALL);
        btnToggle.setForeground(SimConstants.C_TEXT);
        btnToggle.setBackground(SimConstants.BG_CARD);
        btnToggle.setFocusPainted(false);
        btnToggle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnToggle.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        btnToggle.addActionListener(e -> {
            controlPanel.setVisible(!controlPanel.isVisible());
            mainWrapper.revalidate();
        });

        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(btnToggle);

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

        JMenuItem miParams = darkItem("⚙  Parámetros.");
        JMenuItem miStart = darkItem("▶  Iniciar");
        JMenuItem miPause = darkItem("⏸  Pausar / Reanudar");
        JMenuItem miStop = darkItem("⏹  Detener");
        JMenuItem miReset = darkItem("↺  Resetear");
        JMenuItem miCharts = darkItem("📊  Ver Gráficas.");
        JMenuItem miResults = darkItem("📄  Ver Resultados.");
        JMenuItem miSimRunner = darkItem("▣  SimRunner / Optimizer.");

        miParams.addActionListener(e -> showParamsDialog());
        miStart.addActionListener(e -> startSimulation());
        miPause.addActionListener(e -> togglePause());
        miStop.addActionListener(e -> stopSimulation());
        miReset.addActionListener(e -> resetSimulation());
        miCharts.addActionListener(e -> showCharts());
        miResults.addActionListener(e -> showResults());
        miSimRunner.addActionListener(e -> showSimRunner());

        mSim.add(miParams);
        mSim.addSeparator();
        mSim.add(miStart);
        mSim.add(miPause);
        mSim.add(miStop);
        mSim.addSeparator();
        mSim.add(miReset);
        mSim.addSeparator();
        mSim.add(miCharts);
        mSim.add(miResults);
        mSim.addSeparator();
        mSim.add(miSimRunner);

        mb.add(mSim);

        // Menú Construir
        JMenu mBuild = darkMenu("Construir");
        JMenuItem miBuildMain = darkItem("Abrir editor del modelo...");
        JMenuItem miGraphEdit = darkItem("Gráficas");
        miGraphEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miLocEdit   = darkItem("Locaciones");
        miLocEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miEntEdit   = darkItem("Entidades");
        miEntEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miRutEdit   = darkItem("Redes de Ruta");
        miRutEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miResEdit   = darkItem("Recursos");
        miResEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miProcEdit  = darkItem("Procesamiento");
        miProcEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        JMenuItem miArrEdit   = darkItem("Arribos");
        miArrEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));

        miBuildMain.addActionListener(e -> showBuildDialog(0));
        miGraphEdit.addActionListener(e -> new GraphicsDialog(this).setVisible(true));
        miLocEdit  .addActionListener(e -> showBuildDialog(0));
        miEntEdit  .addActionListener(e -> showBuildDialog(1));
        miRutEdit  .addActionListener(e -> showBuildDialog(2));
        miResEdit  .addActionListener(e -> showBuildDialog(3));
        miProcEdit .addActionListener(e -> showBuildDialog(4));
        miArrEdit  .addActionListener(e -> showBuildDialog(5));

        mBuild.add(miBuildMain); mBuild.addSeparator();
        mBuild.add(miGraphEdit); mBuild.addSeparator();
        mBuild.add(miLocEdit); mBuild.add(miEntEdit); mBuild.add(miRutEdit);
        mBuild.add(miResEdit); mBuild.add(miProcEdit); mBuild.add(miArrEdit);
        mb.add(mBuild);

        // Menú Ayuda
        JMenu mHelp = darkMenu("Ayuda");
        JMenuItem miAbout = darkItem("Acerca de...");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Promodel-Lite Simulator v1.0\n" +
            "Simulador Genérico Dinámico por Modelos ProModel (.txt)\n" +
            "Java Swing — Sin dependencias externas",
            "Acerca de Promodel-Lite",
            JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(miAbout);

        mb.add(mHelp);

        setJMenuBar(mb);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Control de simulación
    // ─────────────────────────────────────────────────────────────────────

    public void startSimulation() {
        if (worker != null && !worker.isDone()) worker.cancel(true);

        state  = new SimState(params.copy());
        engine = new SimEngine(state);
        worker = new SimWorker(state, engine, this::onTick, this::onFinished);

        mostrarVistaSimulacion();

        factoryPanel.setState(state);
        statsPanel.setState(state);

        setStatus("CORRIENDO", SimConstants.C_SUCCESS);
        controlPanel.onStarted();

        worker.execute();
    }

    public void togglePause() {
        if (state == null) {
            return;
        }

        state.paused = !state.paused;

        setStatus(
                state.paused ? "PAUSADO" : "CORRIENDO",
                state.paused ? SimConstants.C_WARNING : SimConstants.C_SUCCESS
        );

        controlPanel.onPauseToggled(state.paused);
    }

    public void stopSimulation() {
        if (worker != null) {
            worker.cancel(true);
        }

        if (state != null) {
            state.running = false;
            state.finished = true;
        }

        setStatus("DETENIDO", SimConstants.C_MUTED);

        if (controlPanel != null) {
            controlPanel.onStopped();
        }
    }

    public void resetSimulation() {
        stopSimulation();

        if (currentData != null) {
            state = new SimState(params.copy());
            state.loadFromData(currentData);

            updateHeaderClock(0);
            if (controlPanel != null) controlPanel.refreshClockOnly(0);

            factoryPanel.setState(state);
            statsPanel.setState(state);

            setStatus("DETENIDO", SimConstants.C_MUTED);
        } else {
            state = null;
            factoryPanel.setState(null);
            statsPanel.setState(null);
        }
    }

    public void setSpeedMult(double mult) {
        if (state != null) {
            state.speedMult = mult;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Callbacks del SimWorker
    // ─────────────────────────────────────────────────────────────────────

    private void onTick() {
        if (state == null) {
            return;
        }

        factoryPanel.repaint();
        statsPanel.refresh();
        controlPanel.refreshCounters();
        updateHeaderClock(state.clk);
    }

    private void onFinished() {
        setStatus("FINALIZADO", SimConstants.C_ACCENT2);
        controlPanel.onStopped();
        if (state == null) return;
        // Preguntar si desea ver resultados
        int opt = JOptionPane.showConfirmDialog(this,
            String.format(
                "Simulacion finalizada.%n%n" +
                "  Tiempo simulado:    %.1f min (%.1f h)%n" +
                "  Entidades Creadas:    %d%n" +
                "  Entidades Salientes: %d%n%n" +
                "Deseas ver el reporte completo de resultados?",
                state.clk, state.clk/60.0,
                state.entidadesCreadas.get(),
                state.entidadesSalientes.get()),
            "Simulacion Completada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) {
            new ResultsDialog(this, state).setVisible(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Diálogos
    // ─────────────────────────────────────────────────────────────────────

    public void showParamsDialog() {
        ParamsDialog dlg = new ParamsDialog(this, params);
        dlg.setVisible(true);

        if (dlg.accepted) {
            params = dlg.result;
        }
    }

    public void showCharts() {
        if (state == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero inicia la simulación para generar datos.",
                    "Sin datos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        new ChartsDialog(this, state).setVisible(true);
    }

    public void showBuildDialog(int tab) {
        if (currentData == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero importa un modelo .txt o abre un proyecto .pmsim.",
                    "Sin modelo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        BuildDialog dlg = new BuildDialog(this, params);
        // Abrir en la pestaña seleccionada
        if (dlg.getContentPane().getComponent(1) instanceof javax.swing.JTabbedPane) {
            ((javax.swing.JTabbedPane)dlg.getContentPane().getComponent(1)).setSelectedIndex(tab);
        }
        dlg.setVisible(true);

        if (dlg.saved) {
            params = dlg.params;
            currentData = dlg.currentData;

            if (currentData != null) {
                currentData.resolveResourceHomes();
            }

            state = new SimState(params.copy());
            state.loadFromData(currentData);

            factoryPanel.setState(state);
            statsPanel.setState(state);

            repaint();
        }
    }

    public void showSimRunner() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero importa un modelo .txt o abre un proyecto .pmsim.",
                    "Sin modelo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (state != null && state.running && !state.paused) {
            JOptionPane.showMessageDialog(
                    this,
                    "Detén o pausa la simulación antes de ejecutar SimRunner.",
                    "SimRunner",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        new SimRunnerDialog(this, currentData, params).setVisible(true);
    }

    public void showResults() {
        if (state == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Sin datos de simulación.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        new ResultsDialog(this, state).setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers visuales
    // ─────────────────────────────────────────────────────────────────────


    public void updateHeaderClock(double clk) {
        if (lblHeaderClock == null) return;
        int totalMinutes = Math.max(0, (int)Math.floor(clk));
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        lblHeaderClock.setText(String.format("HR:%02d MIN:%02d", hours, minutes));
    }

    private void setStatus(String text, Color color) {
        if (lblStatus != null) {
            lblStatus.setText(text);
            lblStatus.setForeground(color);
        }
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