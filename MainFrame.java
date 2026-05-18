import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;

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
 * MainFrame - Ventana principal del simulador Promodel-Lite.
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
    ProModelData currentData;

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
        super("⚙ Promodel-Lite — Simulador de Fabricación de Engranes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(SimConstants.BG_DARK);
        buildUI();
        buildMenu();
    }

    // ── Construcción de la interfaz ───────────────────────────────────────

    JPanel welcomePanel;
    JPanel mainWrapper;

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),       BorderLayout.NORTH);

        factoryPanel = new FactoryPanel();
        statsPanel = new StatsPanel();
        controlPanel = new ControlPanel(this);
        
        // Vista principal de simulacion (oculta al inicio)
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
        
        javax.swing.JButton btnImport = new javax.swing.JButton("Importar Modelo (.txt)");
        btnImport.setFont(SimConstants.FONT_TITLE);
        btnImport.setFocusPainted(false);
        btnImport.addActionListener(e -> importModel());
        
        JPanel pCenter = new JPanel(new java.awt.GridBagLayout());
        pCenter.setBackground(SimConstants.BG_DARK);
        pCenter.add(btnImport);
        
        w.add(lblWelcome, BorderLayout.CENTER);
        w.add(pCenter, BorderLayout.SOUTH);
        return w;
    }

    private void importModel() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
        if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
            try {
                ProModelData data = ProModelParser.parse(fc.getSelectedFile().getAbsolutePath());
                
                // Guardamos para pasárselo a las demás ventanas (Construir, etc)
                this.currentData = data;
                
                // Inicializar estado dinamico
                state = new SimState(params.copy());
                state.loadFromData(data);
                
                // Cambiar a vista de simulacion
                remove(welcomePanel);
                add(mainWrapper, BorderLayout.CENTER);
                revalidate();
                repaint();
                
                factoryPanel.setState(state);
                statsPanel.setState(state);
                
                JOptionPane.showMessageDialog(this, "Modelo importado con éxito. Haz clic derecho en las locaciones para asignar imágenes.");
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al importar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(SimConstants.BG_HEADER);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, SimConstants.C_ACCENT));
        h.setPreferredSize(new Dimension(0, 58));

        // Título izquierda
        JLabel title = new JLabel("  ⚙  PROMODEL-LITE  —  Simulador de Fabricación de Engranes");
        title.setFont(SimConstants.FONT_TITLE);
        title.setForeground(SimConstants.C_TEXT);
        h.add(title, BorderLayout.WEST);

        // Reloj y estado derecha
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        right.setOpaque(false);

        lblStatus = new JLabel("DETENIDO");
        lblStatus.setFont(SimConstants.FONT_LABEL);
        lblStatus.setForeground(SimConstants.C_MUTED);

        lblClock = new JLabel("HR:00  MIN:00");
        lblClock.setFont(SimConstants.FONT_MONO);
        lblClock.setForeground(SimConstants.C_ACCENT2);

        right.add(lblStatus);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(lblClock);
        h.add(right, BorderLayout.EAST);

        return h;
    }



    private void buildMenu() {
        JMenuBar mb = new JMenuBar();
        mb.setBackground(SimConstants.BG_CARD);
        mb.setBorder(BorderFactory.createEmptyBorder());

        // Menú Archivo
        JMenu mFile = darkMenu("Archivo");
        JMenuItem miImport = darkItem("Importar Modelo (.txt)...");
        miImport.addActionListener(e -> importModel());
        mFile.add(miImport);
        mb.add(mFile);

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

        // ── Menú Construir (estilo ProModel) ──────────────────────────────
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
        
        JMenuItem miTutorial = darkItem("Tutorial...");
        miTutorial.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "TUTORIAL RÁPIDO DE USO:\n\n" +
            "1. Importar Modelo:\n" +
            "   Ve a 'Archivo > Importar Modelo' y selecciona tu archivo .txt o .mod generado por ProModel.\n\n" +
            "2. Personalizar Gráficos:\n" +
            "   - Ve a 'Construir > Gráficas' (Ctrl+G) para subir tus propios iconos PNG/JPG.\n" +
            "   - En el lienzo, haz clic sobre una locación para seleccionarla, y luego en la ventana\n" +
            "     de Gráficas selecciona el icono que desees aplicarle.\n\n" +
            "3. Configurar Layout:\n" +
            "   Arrastra las locaciones libremente por el lienzo. El simulador recordará automáticamente\n" +
            "   las posiciones para tu próxima corrida.\n\n" +
            "4. Configurar Parámetros:\n" +
            "   Ve a 'Simulación > Parámetros...' para cambiar la duración (ej. '90 DAY') y la semilla.\n\n" +
            "5. Atajos de Teclado (Menú Construir):\n" +
            "   - Ctrl+G : Gráficas\n" +
            "   - Ctrl+L : Locaciones\n" +
            "   - Ctrl+E : Entidades\n" +
            "   - Ctrl+N : Redes de Ruta\n" +
            "   - Ctrl+R : Recursos\n" +
            "   - Ctrl+P : Procesamiento\n" +
            "   - Ctrl+I : Arribos\n\n" +
            "6. Simulación y Resultados:\n" +
            "   Usa el botón ▶ Iniciar. Puedes pausar o detener en cualquier momento.\n" +
            "   Al terminar, ve a 'Simulación > Ver Gráficas...' para analizar la utilización y el flujo.",
            "Tutorial de Promodel-Lite",
            JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(miTutorial);
        mHelp.addSeparator();
        
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

    // ── Control de simulación ─────────────────────────────────────────────

    public void startSimulation() {
        if (currentData == null) {
            JOptionPane.showMessageDialog(this, "Importa un modelo .txt primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (worker != null && !worker.isDone()) worker.cancel(true);

        state  = new SimState(params.copy());
        
        // ¡MUY IMPORTANTE! Volver a cargar los datos importados en el nuevo estado
        state.loadFromData(currentData); 

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
        // Actualizar reloj en formato HR:MM estilo ProModel
        int hr  = (int)(state.clk / 60);
        int min = (int)(state.clk % 60);
        lblClock.setText(String.format("HR:%02d  MIN:%02d", hr, min));
        // Repintar fábrica y tabla
        factoryPanel.repaint();
        statsPanel.refresh();
        controlPanel.refreshCounters();
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

    // ── Diálogos ─────────────────────────────────────────────────────────

    public void showParamsDialog() {
        ParamsDialog dlg = new ParamsDialog(this, params);
        dlg.setVisible(true);
        if (dlg.accepted) params = dlg.result;
    }

    public void showCharts() {
        if (state == null) {
            JOptionPane.showMessageDialog(this,
                "Inicia la simulacion primero.",
                "Sin datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ChartsDialog(this, state).setVisible(true);
    }

    public void showBuildDialog(int tab) {
        if (state != null && state.running && !state.paused) {
            JOptionPane.showMessageDialog(this,
                "Detiene o pausa la simulacion antes de editar el modelo.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BuildDialog dlg = new BuildDialog(this, params, currentData);
        // Abrir en la pestaña seleccionada
        dlg.setSelectedTab(tab);
        dlg.setVisible(true);
        if (dlg.saved) params = dlg.params;
    }

    public void showResults() {
        if (state == null) {
            JOptionPane.showMessageDialog(this, "Sin datos de simulacion.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        new ResultsDialog(this, state).setVisible(true);
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
