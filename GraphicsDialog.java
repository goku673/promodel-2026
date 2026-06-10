import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GraphicsDialog extends JDialog {

    private MainFrame mainFrame;
    public static GraphicsDialog instance = null;
    
    private Loc currentLoc = null;
    private boolean updating = false;
    
    private JCheckBox chkCounter;
    private JComboBox<String> cbType;
    private JCheckBox chkGauge;
    public static class ImageItem {
        public String name;
        public Image img;
        public String path;
        public ImageItem(String n, Image i, String p) { name = n; img = i; path = p; }
    }
    
    // Lista compartida de imágenes
    public static final List<ImageItem> palette = new ArrayList<>();
    public static ImageItem selectedItem = null;
    
    // Directorio donde se guardarán las imágenes
    private static final String PUBLIC_DIR = "public";

    // Bloque estático para cargar imágenes guardadas al iniciar la aplicación
    static {
        loadSavedImages();
    }
    
    private static void loadSavedImages() {
        File dir = new File(PUBLIC_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
            if (files != null) {
                for (File f : files) {
                    try {
                        Image img = ImageIO.read(f);
                        if (img != null) {
                            String name = f.getName().substring(0, f.getName().lastIndexOf('.'));
                            palette.add(new ImageItem(name, img, f.getAbsolutePath()));
                        }
                    } catch (Exception e) {
                        System.err.println("No se pudo cargar " + f.getName());
                    }
                }
            }
        }
    }
    
    private JPanel gridPanel;

    public GraphicsDialog(MainFrame owner) {
        super(owner, "Gráficas", false); // false = modal desactivado, puede interactuar con el lienzo
        this.mainFrame = owner;
        instance = this;
        setSize(300, 750);
        setLocationRelativeTo(owner);
        // Desplazar a la izquierda de la ventana principal para que parezca una barra lateral
        Point loc = owner.getLocation();
        setLocation(Math.max(0, loc.x - 310), loc.y);
        
        getContentPane().setBackground(SimConstants.BG_PANEL);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                selectedItem = null;
                instance = null;
            }
        });
        
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // Header
        JLabel lblTop = new JLabel("  Gráficas Disponibles");
        lblTop.setFont(SimConstants.FONT_TITLE);
        lblTop.setForeground(SimConstants.C_TEXT);
        lblTop.setPreferredSize(new Dimension(0, 40));
        lblTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SimConstants.C_BORDER));
        add(lblTop, BorderLayout.NORTH);

        // Grid
        gridPanel = new JPanel(new GridLayout(0, 3, 5, 5));
        gridPanel.setBackground(SimConstants.BG_DARK);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Sidebar con botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
        pnlBotones.setBackground(SimConstants.BG_PANEL);
        pnlBotones.setPreferredSize(new Dimension(80, 0));
        
        JButton btnNuevo = btn("Nuevo", e -> uploadNewImage());
        JButton btnVer   = btn("Deseleccionar", e -> deselect());
        JButton btnClose = btn("Cerrar", e -> {
            selectedItem = null;
            instance = null;
            refreshGrid();
            setVisible(false);
            dispose();
        });
        
        pnlBotones.add(btnNuevo);
        pnlBotones.add(btnVer);
        pnlBotones.add(btnClose);
        
        add(pnlBotones, BorderLayout.EAST);
        
        // --- FOOTER PARA PROPIEDADES DE LOCACIÓN ---
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(SimConstants.BG_PANEL);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, SimConstants.C_BORDER),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblProp = new JLabel("Propiedades de Locación Seleccionada:");
        lblProp.setFont(SimConstants.FONT_LABEL);
        lblProp.setForeground(SimConstants.C_ACCENT2);
        footer.add(lblProp);
        footer.add(Box.createVerticalStrut(5));
        
        chkCounter = new JCheckBox("Mostrar Contador");
        chkCounter.setOpaque(false); chkCounter.setForeground(SimConstants.C_TEXT);
        
        cbType = new JComboBox<>(new String[]{"Contenido Actual", "Entradas Totales", "Salidas"});
        cbType.setMaximumSize(new Dimension(250, 25));
        
        chkGauge = new JCheckBox("Mostrar Medidor");
        chkGauge.setOpaque(false); chkGauge.setForeground(SimConstants.C_TEXT);
        
        footer.add(chkCounter);
        footer.add(Box.createVerticalStrut(2));
        footer.add(cbType);
        footer.add(Box.createVerticalStrut(2));
        footer.add(chkGauge);
        
        add(footer, BorderLayout.SOUTH);
        
        // Listeners del footer
        ActionListener updateProp = e -> {
            if (updating || currentLoc == null) return;
            
            boolean sc = chkCounter.isSelected();
            String ct = (String) cbType.getSelectedItem();
            boolean sg = chkGauge.isSelected();
            
            currentLoc.showCounter = sc;
            currentLoc.counterType = ct;
            currentLoc.showGauge = sg;
            
            // Actualizar en el modelo persistente
            if (mainFrame.currentData != null) {
                for (ProModelData.LocDef d : mainFrame.currentData.locations) {
                    if (d.name.equals(currentLoc.name) || currentLoc.name.startsWith(d.name + ".")) {
                        d.showCounter = sc;
                        d.counterType = ct;
                        d.showGauge = sg;
                    }
                }
            }
            
            // Sincronizar en vivo si hay una simulación activa
            if (mainFrame.state != null) {
                Loc activeLoc = mainFrame.state.loc(currentLoc.name);
                if (activeLoc != null && activeLoc != currentLoc) {
                    activeLoc.showCounter = sc;
                    activeLoc.counterType = ct;
                    activeLoc.showGauge = sg;
                }
            }
            
            mainFrame.repaint();
        };
        
        chkCounter.addActionListener(updateProp);
        cbType.addActionListener(updateProp);
        chkGauge.addActionListener(updateProp);
        
        setSelectedLoc(null); // Desactivar al inicio
        
        refreshGrid();
    }
    
    public void setSelectedLoc(Loc l) {
        this.currentLoc = l;
        updating = true;
        if (l == null) {
            chkCounter.setEnabled(false);
            chkGauge.setEnabled(false);
            cbType.setEnabled(false);
            chkCounter.setSelected(false);
            chkGauge.setSelected(false);
        } else {
            chkCounter.setEnabled(true);
            chkGauge.setEnabled(true);
            cbType.setEnabled(true);
            chkCounter.setSelected(l.showCounter);
            chkGauge.setSelected(l.showGauge);
            cbType.setSelectedItem(l.counterType);
        }
        updating = false;
    }

    private void uploadNewImage() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar nueva imagen / icono");
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                Image img = ImageIO.read(fc.getSelectedFile());
                if (img != null) {
                    String name = JOptionPane.showInputDialog(this, "Nombre para el icono:", fc.getSelectedFile().getName());
                    if (name == null || name.trim().isEmpty()) name = "Imagen_" + System.currentTimeMillis();
                    
                    // Asegurar que el nombre sea seguro para archivo y guardarlo
                    name = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
                    File dest = new File(PUBLIC_DIR, name + ".png");
                    Files.copy(fc.getSelectedFile().toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    
                    palette.add(new ImageItem(name, img, dest.getAbsolutePath()));
                    refreshGrid();
                } else {
                    JOptionPane.showMessageDialog(this, "Formato de imagen inválido.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
    
    private void deselect() {
        selectedItem = null;
        refreshGrid();
    }

    private void refreshGrid() {
        gridPanel.removeAll();
        for (ImageItem item : palette) {
            JPanel p = new JPanel(new BorderLayout());
            p.setPreferredSize(new Dimension(60, 60));
            boolean isSelected = (item == selectedItem);
            
            p.setBackground(isSelected ? SimConstants.BG_CARD : SimConstants.BG_PANEL);
            p.setBorder(BorderFactory.createLineBorder(isSelected ? SimConstants.C_ACCENT : SimConstants.C_BORDER, isSelected ? 2 : 1));
            
            // Scaled Icon
            ImageIcon icon = new ImageIcon(item.img.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            JLabel lblIcon = new JLabel(icon);
            p.add(lblIcon, BorderLayout.CENTER);
            
            p.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectedItem = item;
                    refreshGrid();
                }
            });
            gridPanel.add(p);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton btn(String text, ActionListener al) {
        JButton b = new JButton(text);
        b.setFont(SimConstants.FONT_SMALL);
        b.setFocusPainted(false);
        b.setBackground(SimConstants.BG_CARD);
        b.setForeground(SimConstants.C_TEXT);
        b.setPreferredSize(new Dimension(70, 30));
        b.addActionListener(al);
        return b;
    }
}
