import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
<<<<<<< Updated upstream
=======
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),       BorderLayout.NORTH);
        add(buildCenterPanel(),  BorderLayout.CENTER);
        add(buildSouthPanel(),   BorderLayout.SOUTH);
=======
    JPanel welcomePanel;
    JPanel mainWrapper;

    public MainFrame() {
        super("⚙ Promodel-Lite — Simulador de Fabricación de Engranes");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        getContentPane().setBackground(SimConstants.BG_DARK);

        buildUI();
        buildMenu();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Construcción de la interfaz
    // ─────────────────────────────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);
>>>>>>> Stashed changes

        controlPanel = new ControlPanel(this);
<<<<<<< Updated upstream
        add(controlPanel,        BorderLayout.EAST);
=======

        // Vista principal de simulación
        mainWrapper = new JPanel(new BorderLayout());
        mainWrapper.setBackground(SimConstants.BG_DARK);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 4));
        centerWrapper.setBackground(SimConstants.BG_DARK);
        centerWrapper.add(factoryPanel, BorderLayout.CENTER);

        JPanel southWrapper = new JPanel(new BorderLayout());
        southWrapper.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 4));
        southWrapper.setBackground(SimConstants.BG_DARK);
        southWrapper.add(statsPanel, BorderLayout.CENTER);

        mainWrapper.add(centerWrapper, BorderLayout.CENTER);
        mainWrapper.add(southWrapper, BorderLayout.SOUTH);
        mainWrapper.add(controlPanel, BorderLayout.EAST);

        // Vista de bienvenida
        welcomePanel = buildWelcomePanel();
        add(welcomePanel, BorderLayout.CENTER);
    }

    private JPanel buildWelcomePanel() {
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(SimConstants.BG_DARK);

        JLabel lblWelcome = new JLabel("Bienvenido a Promodel-Lite", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 30));
        lblWelcome.setForeground(SimConstants.C_TEXT);

        JLabel lblSub = new JLabel(
                "Importa un modelo TXT o abre un proyecto guardado PMSIM",
                SwingConstants.CENTER
        );
        lblSub.setFont(SimConstants.FONT_LABEL);
        lblSub.setForeground(SimConstants.C_MUTED);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.add(lblWelcome, BorderLayout.CENTER);
        titlePanel.add(lblSub, BorderLayout.SOUTH);

        JButton btnImport = new JButton("Importar Modelo (.txt)");
        btnImport.setFont(SimConstants.FONT_TITLE);
        btnImport.setFocusPainted(false);
        btnImport.addActionListener(e -> importModel());

        JButton btnOpenProject = new JButton("Abrir Proyecto (.pmsim)");
        btnOpenProject.setFont(SimConstants.FONT_TITLE);
        btnOpenProject.setFocusPainted(false);
        btnOpenProject.addActionListener(e -> abrirProyecto());

        JPanel buttons = new JPanel(new GridLayout(2, 1, 0, 12));
        buttons.setOpaque(false);
        buttons.add(btnImport);
        buttons.add(btnOpenProject);

        JPanel pCenter = new JPanel(new java.awt.GridBagLayout());
        pCenter.setBackground(SimConstants.BG_DARK);
        pCenter.add(buttons);

        w.add(titlePanel, BorderLayout.CENTER);
        w.add(pCenter, BorderLayout.SOUTH);

        return w;
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
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
=======
    // ─────────────────────────────────────────────────────────────────────
    // Importar TXT / Guardar PMSIM / Abrir PMSIM
    // ─────────────────────────────────────────────────────────────────────

    private void importModel() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Importar modelo ProModel (.txt)");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Modelo ProModel exportado (*.txt)",
                "txt"
        ));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ProModelData data = ProModelParser.parse(fc.getSelectedFile().getAbsolutePath());

                cargarModeloEnInterfaz(data);

                JOptionPane.showMessageDialog(
                        this,
                        "Modelo importado con éxito.\n\nAhora puedes configurarlo y guardarlo como proyecto .pmsim.",
                        "Modelo importado",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Error al importar modelo:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                ex.printStackTrace();
            }
        }
    }

    private void guardarProyecto() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Primero importa un modelo .txt o abre un proyecto .pmsim antes de guardar.",
                    "Sin modelo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            if (currentData != null) {
                currentData.resolveResourceHomes();
            }

            JFileChooser fc = ProjectIO.createProjectChooser();
            fc.setDialogTitle("Guardar proyecto Promodel-Lite");

            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                if (!file.getName().toLowerCase().endsWith(".pmsim")) {
                    file = new File(file.getAbsolutePath() + ".pmsim");
                }

                if (file.exists()) {
                    int opt = JOptionPane.showConfirmDialog(
                            this,
                            "El archivo ya existe.\n¿Deseas reemplazarlo?",
                            "Confirmar guardado",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE
                    );

                    if (opt != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                ProjectIO.saveProject(file, currentData);

                JOptionPane.showMessageDialog(
                        this,
                        "Proyecto guardado correctamente:\n" + file.getAbsolutePath(),
                        "Proyecto guardado",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al guardar proyecto:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void abrirProyecto() {
        try {
            JFileChooser fc = ProjectIO.createProjectChooser();
            fc.setDialogTitle("Abrir proyecto Promodel-Lite");

            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                ProModelData data = ProjectIO.loadProject(fc.getSelectedFile());

                cargarModeloEnInterfaz(data);

                JOptionPane.showMessageDialog(
                        this,
                        "Proyecto abierto correctamente.",
                        "Proyecto abierto",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error al abrir proyecto:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    /**
     * Carga un ProModelData en la interfaz.
     * Sirve tanto para importar TXT como para abrir PMSIM.
     */
    private void cargarModeloEnInterfaz(ProModelData data) {
        if (data == null) {
            return;
        }

        stopSimulation();

        this.currentData = data;

        if (this.currentData != null) {
            this.currentData.resolveResourceHomes();
        }

        state = new SimState(params.copy());
        state.loadFromData(currentData);

        updateHeaderClock(0);
        if (controlPanel != null) controlPanel.refreshClockOnly(0);

        mostrarVistaSimulacion();

        factoryPanel.setState(state);
        statsPanel.setState(state);

        setStatus("DETENIDO", SimConstants.C_MUTED);
        controlPanel.onStopped();

        revalidate();
        repaint();
    }

    /**
     * Cambia desde pantalla de bienvenida hacia pantalla de simulación.
     */
    private void mostrarVistaSimulacion() {
        if (welcomePanel != null && welcomePanel.getParent() != null) {
            remove(welcomePanel);
        }

        if (mainWrapper != null && mainWrapper.getParent() == null) {
            add(mainWrapper, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────
    // Menú principal
    // ─────────────────────────────────────────────────────────────────────
>>>>>>> Stashed changes

    private void buildMenu() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(SimConstants.BG_CARD);
        mb.setBorder(BorderFactory.createEmptyBorder());

<<<<<<< Updated upstream
=======
        // Menú Archivo
        JMenu mFile = darkMenu("Archivo");

        JMenuItem miImport = darkItem("Importar Modelo (.txt)");
        miImport.addActionListener(e -> importModel());
        mFile.add(miImport);

        JMenuItem miOpenProject = darkItem("Abrir Proyecto (.pmsim)");
        miOpenProject.addActionListener(e -> abrirProyecto());
        mFile.add(miOpenProject);

        JMenuItem miSaveProject = darkItem("Guardar Proyecto (.pmsim)");
        miSaveProject.addActionListener(e -> guardarProyecto());
        mFile.add(miSaveProject);

        mFile.addSeparator();

        JMenuItem miExit = darkItem("Salir");
        miExit.addActionListener(e -> dispose());
        mFile.add(miExit);

        mb.add(mFile);

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        JMenuItem miBuildMain = darkItem("Abrir editor del modelo...");
        JMenuItem miLocEdit   = darkItem("Locaciones        Ctrl+L");
        JMenuItem miEntEdit   = darkItem("Entidades         Ctrl+E");
        JMenuItem miRutEdit   = darkItem("Redes de Ruta     Ctrl+N");
        JMenuItem miResEdit   = darkItem("Recursos          Ctrl+R");
        JMenuItem miProcEdit  = darkItem("Procesamiento     Ctrl+P");
        JMenuItem miArrEdit   = darkItem("Arribos           Ctrl+I");

        miBuildMain.addActionListener(e -> showBuildDialog(0));
        miLocEdit  .addActionListener(e -> showBuildDialog(0));
        miEntEdit  .addActionListener(e -> showBuildDialog(1));
        miRutEdit  .addActionListener(e -> showBuildDialog(2));
        miResEdit  .addActionListener(e -> showBuildDialog(3));
        miProcEdit .addActionListener(e -> showBuildDialog(4));
        miArrEdit  .addActionListener(e -> showBuildDialog(5));

        mBuild.add(miBuildMain); mBuild.addSeparator();
        mBuild.add(miLocEdit); mBuild.add(miEntEdit); mBuild.add(miRutEdit);
        mBuild.add(miResEdit); mBuild.add(miProcEdit); mBuild.add(miArrEdit);
=======

        JMenuItem miBuildMain = darkItem("Abrir editor del modelo.");
        JMenuItem miGraphEdit = darkItem("Gráficas");
        JMenuItem miLocEdit = darkItem("Locaciones");
        JMenuItem miEntEdit = darkItem("Entidades");
        JMenuItem miRutEdit = darkItem("Redes de Ruta");
        JMenuItem miResEdit = darkItem("Recursos");
        JMenuItem miProcEdit = darkItem("Procesamiento");
        JMenuItem miArrEdit = darkItem("Arribos");
        JMenuItem miMacroEdit = darkItem("Macros");

        miGraphEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        miLocEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        miEntEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        miRutEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        miResEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        miProcEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        miArrEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        miMacroEdit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));

        miBuildMain.addActionListener(e -> showBuildDialog(0));
        miGraphEdit.addActionListener(e -> new GraphicsDialog(this).setVisible(true));
        miLocEdit.addActionListener(e -> showBuildDialog(0));
        miEntEdit.addActionListener(e -> showBuildDialog(1));
        miRutEdit.addActionListener(e -> showBuildDialog(2));
        miResEdit.addActionListener(e -> showBuildDialog(3));
        miProcEdit.addActionListener(e -> showBuildDialog(4));
        miArrEdit.addActionListener(e -> showBuildDialog(5));
        miMacroEdit.addActionListener(e -> showBuildDialog(6));

        mBuild.add(miBuildMain);
        mBuild.addSeparator();
        mBuild.add(miGraphEdit);
        mBuild.addSeparator();
        mBuild.add(miLocEdit);
        mBuild.add(miEntEdit);
        mBuild.add(miRutEdit);
        mBuild.add(miResEdit);
        mBuild.add(miProcEdit);
        mBuild.add(miArrEdit);
        mBuild.add(miMacroEdit);

>>>>>>> Stashed changes
        mb.add(mBuild);

        // Menú Ayuda
        JMenu mHelp = darkMenu("Ayuda");
<<<<<<< Updated upstream
        JMenuItem miAbout = darkItem("Acerca de...");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Promodel-Lite Simulator v1.0\n" +
            "Simulación de Eventos Discretos (DES)\n" +
            "Java Swing — Sin dependencias externas\n\n" +
            "Proceso: BARRA → CONVEYOR_1 → ALMACEN_1 → CORTADORA\n" +
            "         → TORNO → CONVEYOR_2 → FRESADORA → ALMACEN_2\n" +
            "         → PINTURA → INSPECCION_1 → EMPAQUE → EMBARQUE",
            "Acerca de Promodel-Lite Simulator",
            JOptionPane.INFORMATION_MESSAGE));
=======

        JMenuItem miTutorial = darkItem("Tutorial.");
        miTutorial.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "TUTORIAL RÁPIDO DE USO:\n\n" +
                        "1. Importar Modelo:\n" +
                        "   Ve a Archivo > Importar Modelo y selecciona tu archivo .txt generado por ProModel.\n\n" +
                        "2. Personalizar Gráficos:\n" +
                        "   - Ve a Construir > Gráficas para subir iconos PNG/JPG.\n" +
                        "   - En el lienzo, selecciona una locación y luego elige una imagen.\n" +
                        "   - En Construir > Entidades puedes asignar imágenes a entidades.\n" +
                        "   - En Construir > Recursos puedes asignar imágenes a operadores o vehículos.\n\n" +
                        "3. Configurar Layout:\n" +
                        "   Arrastra las locaciones y redimensiónalas en el lienzo.\n\n" +
                        "4. Guardar Proyecto:\n" +
                        "   Ve a Archivo > Guardar Proyecto (.pmsim).\n" +
                        "   Esto guarda posiciones, imágenes, recursos, entidades, rutas y configuración.\n\n" +
                        "5. Abrir Proyecto:\n" +
                        "   Ve a Archivo > Abrir Proyecto (.pmsim).\n" +
                        "   Así recuperas todo sin configurar desde cero.\n\n" +
                        "6. Simulación:\n" +
                        "   Usa ▶ Iniciar, Pausar, Detener y Ver Gráficas.",
                "Tutorial de Promodel-Lite",
                JOptionPane.INFORMATION_MESSAGE
        ));

        JMenuItem miAbout = darkItem("Acerca de.");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(
                this,
                "Promodel-Lite Simulator v1.0\n" +
                        "Simulador Genérico Dinámico por Modelos ProModel (.txt)\n" +
                        "Java Swing — Sin dependencias externas\n\n" +
                        "Extensión de proyecto: .pmsim",
                "Acerca de Promodel-Lite",
                JOptionPane.INFORMATION_MESSAGE
        ));

        mHelp.add(miTutorial);
        mHelp.addSeparator();
>>>>>>> Stashed changes
        mHelp.add(miAbout);

        mb.add(mHelp);

        setJMenuBar(mb);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Control de simulación
    // ─────────────────────────────────────────────────────────────────────

    public void startSimulation() {
<<<<<<< Updated upstream
        if (worker != null && !worker.isDone()) worker.cancel(true);

        state  = new SimState(params.copy());
=======
        if (currentData == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Importa un modelo .txt o abre un proyecto .pmsim primero.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (worker != null && !worker.isDone()) {
            worker.cancel(true);
        }

        if (currentData != null) {
            currentData.resolveResourceHomes();
        }

        state = new SimState(params.copy());
        state.loadFromData(currentData);

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        controlPanel.onStopped();
        if (state == null) return;
        // Preguntar si desea ver resultados
        int opt = JOptionPane.showConfirmDialog(this,
            String.format(
                "Simulacion finalizada.%n%n" +
                "  Tiempo simulado:    %.1f min (%.1f h)%n" +
                "  Barras llegadas:    %d%n" +
                "  Piezas finalizadas: %d%n%n" +
                "Deseas ver el reporte completo de resultados?",
                state.clk, state.clk/60.0,
                state.barrasLlegadas.get(),
                state.piezasFinales.get()),
            "Simulacion Completada",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
=======

        if (controlPanel != null) {
            controlPanel.onStopped();
        }

        if (state == null) {
            return;
        }

        int opt = JOptionPane.showConfirmDialog(
                this,
                String.format(
                        "Simulación finalizada.%n%n" +
                                "Tiempo simulado: %.1f min (%.1f h)%n" +
                                "Entidades creadas: %d%n" +
                                "Entidades salientes: %d%n%n" +
                                "¿Deseas ver el reporte completo de resultados?",
                        state.clk,
                        state.clk / 60.0,
                        state.entidadesCreadas.get(),
                        state.entidadesSalientes.get()
                ),
                "Simulación completada",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        BuildDialog dlg = new BuildDialog(this, params);
        // Abrir en la pestaña seleccionada
        if (dlg.getContentPane().getComponent(1) instanceof javax.swing.JTabbedPane) {
            ((javax.swing.JTabbedPane)dlg.getContentPane().getComponent(1)).setSelectedIndex(tab);
        }
=======

        if (state != null && state.running && !state.paused) {
            JOptionPane.showMessageDialog(
                    this,
                    "Detén o pausa la simulación antes de editar el modelo.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        BuildDialog dlg = new BuildDialog(this, params, currentData);
        dlg.setSelectedTab(tab);
>>>>>>> Stashed changes
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