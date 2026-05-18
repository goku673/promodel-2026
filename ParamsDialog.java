import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/** ParamsDialog - Diálogo modal para editar todos los parámetros de simulación. */
public class ParamsDialog extends JDialog {

    public boolean  accepted = false;
    public SimParams result;

    private final SimParams working;

    private JTextField fTiempoSimul, fReplicas, fSeed;
    private JRadioButton rbSeg, rbMin, rbHora, rbDia;

    public ParamsDialog(JFrame owner, SimParams params) {
        super(owner, "Parametros de Simulacion", true);
        this.working = params.copy();
        setSize(560, 490);
        setLocationRelativeTo(owner);
        setResizable(false);
        getContentPane().setBackground(SimConstants.BG_PANEL);
        build();
    }

    private void build() {
        setLayout(new BorderLayout());

        JLabel hdr = new JLabel("  Parametros de Simulacion - Promodel-Lite");
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
        tabs.addTab("General",            buildGeneralTab());
        add(tabs, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }



    private JPanel buildGeneralTab() {
        JPanel p = form();
        GridBagConstraints c = gbc();
        
        addHeader(p,c,"TIEMPO DE CORRIDA");
        fTiempoSimul = row(p,c,"Tiempo Simul.*:", working.tiempoSimul);
        c.gridy++; c.gridx=0; c.gridwidth=2;
        JLabel lNote = new JLabel("*A menos que se especifique lo contrario, la unidad de tiempo por defecto HORA.");
        lNote.setFont(new Font("Arial", Font.ITALIC, 10));
        lNote.setForeground(SimConstants.C_MUTED);
        p.add(lNote, c);
        c.gridwidth=1;
        
        addHeader(p,c,"PRECISIÓN DEL RELOJ");
        c.gridy++; c.gridx=0; c.gridwidth=2;
        JPanel pPrec = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pPrec.setBackground(SimConstants.BG_PANEL);
        rbSeg = new JRadioButton("Segundo");
        rbMin = new JRadioButton("Minuto");
        rbHora = new JRadioButton("Hora");
        rbDia = new JRadioButton("Día");
        rbSeg.setBackground(SimConstants.BG_PANEL); rbSeg.setForeground(SimConstants.C_TEXT);
        rbMin.setBackground(SimConstants.BG_PANEL); rbMin.setForeground(SimConstants.C_TEXT);
        rbHora.setBackground(SimConstants.BG_PANEL); rbHora.setForeground(SimConstants.C_TEXT);
        rbDia.setBackground(SimConstants.BG_PANEL); rbDia.setForeground(SimConstants.C_TEXT);
        
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSeg); bg.add(rbMin); bg.add(rbHora); bg.add(rbDia);
        pPrec.add(rbSeg); pPrec.add(rbMin); pPrec.add(rbHora); pPrec.add(rbDia);
        
        if (working.precisionReloj.equals("Segundo")) rbSeg.setSelected(true);
        else if (working.precisionReloj.equals("Hora")) rbHora.setSelected(true);
        else if (working.precisionReloj.equals("Día")) rbDia.setSelected(true);
        else rbMin.setSelected(true); // Default
        
        p.add(pPrec, c);
        c.gridwidth=1;
        
        addHeader(p,c,"REPORTE DE RESULTADOS");
        fReplicas = row(p,c,"Número de Réplicas:", working.replicas);
        
        addHeader(p,c,"GENERAL");
        fSeed = row(p,c,"Semilla aleatoria:", sv(working.semilla));
        
        return scroll(p);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        p.setBackground(SimConstants.BG_HEADER);
        p.setBorder(BorderFactory.createMatteBorder(2,0,0,0,SimConstants.C_BORDER));
        p.add(abtn("Defaults", new Color(50,50,100),  e -> loadDefaults()));
        p.add(abtn("Cancelar", new Color(100,40,40),  e -> dispose()));
        p.add(abtn("Aplicar",  new Color(40,120,60),  e -> { if (apply()) { accepted=true; dispose(); }}));
        return p;
    }

    private boolean apply() {
        try {
            String ts = fTiempoSimul.getText().trim().toUpperCase();
            working.tiempoSimul = fTiempoSimul.getText().trim();
            if (ts.isEmpty()) {
                working.duracion = 0;
            } else {
                double val = 0;
                String[] parts = ts.split(" ");
                val = Double.parseDouble(parts[0]);
                if (parts.length > 1) {
                    String unit = parts[1];
                    if (unit.startsWith("DAY") || unit.startsWith("DIA")) val *= (24 * 60);
                    else if (unit.startsWith("HR") || unit.startsWith("HORA")) val *= 60;
                    else if (unit.startsWith("SEC") || unit.startsWith("SEG")) val /= 60.0;
                } else {
                    val *= 60; // default HORA
                }
                working.duracion = val;
            }
            
            working.semilla = Long.parseLong(fSeed.getText().trim());
            working.replicas = fReplicas.getText().trim();
            
            if (rbSeg.isSelected()) working.precisionReloj = "Segundo";
            else if (rbHora.isSelected()) working.precisionReloj = "Hora";
            else if (rbDia.isSelected()) working.precisionReloj = "Día";
            else working.precisionReloj = "Minuto";
            
            result = working;
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Verifica los números.",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void loadDefaults() {
        SimParams d = new SimParams();
        fTiempoSimul.setText(d.tiempoSimul);
        fReplicas.setText(d.replicas);
        fSeed.setText(sv(d.semilla));
        rbMin.setSelected(true);
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────
    private JPanel form() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(SimConstants.BG_PANEL);
        return p;
    }

    private JPanel scroll(JPanel inner) {
        JScrollPane sp = new JScrollPane(inner,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBackground(SimConstants.BG_PANEL);
        sp.getViewport().setBackground(SimConstants.BG_PANEL);
        sp.setBorder(BorderFactory.createEmptyBorder());
        JPanel w = new JPanel(new BorderLayout());
        w.setBackground(SimConstants.BG_PANEL);
        w.add(sp, BorderLayout.CENTER);
        return w;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2,8,2,8);
        c.fill   = GridBagConstraints.HORIZONTAL;
        c.weightx= 1; c.gridy = 0;
        return c;
    }

    private void addHeader(JPanel p, GridBagConstraints c, String title) {
        c.gridx = 0; c.gridwidth = 2; c.gridy++;
        JLabel lbl = new JLabel("  " + title);
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        lbl.setForeground(SimConstants.C_ACCENT2);
        lbl.setBackground(SimConstants.BG_CARD);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createEmptyBorder(3,4,3,4));
        p.add(lbl, c);
        c.gridwidth = 1;
    }

    private JTextField row(JPanel p, GridBagConstraints c, String label, String val) {
        c.gridy++; c.gridx = 0; c.weightx = 0.5;
        JLabel lbl = new JLabel(label);
        lbl.setFont(SimConstants.FONT_SMALL);
        lbl.setForeground(SimConstants.C_TEXT);
        p.add(lbl, c);
        c.gridx = 1; c.weightx = 0.5;
        JTextField tf = new JTextField(val, 8);
        tf.setBackground(SimConstants.BG_CARD);
        tf.setForeground(SimConstants.C_TEXT);
        tf.setCaretColor(SimConstants.C_TEXT);
        tf.setFont(SimConstants.FONT_SMALL);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SimConstants.C_BORDER),
            BorderFactory.createEmptyBorder(2,4,2,4)));
        p.add(tf, c);
        return tf;
    }

    private void note(JPanel p, GridBagConstraints c, String text) {
        c.gridy++; c.gridx = 0; c.gridwidth = 2;
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", Font.ITALIC, 9));
        lbl.setForeground(SimConstants.C_MUTED);
        p.add(lbl, c);
        c.gridwidth = 1;
    }

    private JButton abtn(String text, Color bg, ActionListener al) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(SimConstants.FONT_LABEL); b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(7,14,7,14));
        b.addActionListener(al);
        return b;
    }

    private double dbl(JTextField tf) { return Double.parseDouble(tf.getText().trim()); }
    private int    itg(JTextField tf) { return Integer.parseInt(tf.getText().trim()); }
    private String sv(double v)       { return String.valueOf(v); }
    private String sv(int v)          { return String.valueOf(v); }
    private String sv(long v)         { return String.valueOf(v); }
}
