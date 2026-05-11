import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/** ParamsDialog - Diálogo modal para editar todos los parámetros de simulación. */
public class ParamsDialog extends JDialog {

    public boolean  accepted = false;
    public SimParams result;

    private final SimParams working;

    private JTextField fConv1, fAlm1M, fAlm1S, fAlm1C;
    private JTextField fCortM, fCortC, fTornM, fTornS, fTornC;
    private JTextField fConv2, fFresM, fFresC;
    private JTextField fAlm2M, fAlm2S, fAlm2C;
    private JTextField fPintM, fPintC;
    private JTextField fIns1M, fIns1S, fIns1C, fIns2M, fIns2C;
    private JTextField fEmpM, fEmpS, fEmpC, fEmbM, fEmbC;
    private JTextField fT1, fT2, fT3, fMk1, fMk2;
    private JTextField fArribo, fDur, fSeed, fRech;

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

        JLabel hdr = new JLabel("  Parametros de Simulacion - Multi-Engrane");
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
        tabs.addTab("Tiempos de Proceso", buildProcessTab());
        tabs.addTab("Recursos",           buildResourceTab());
        tabs.addTab("General",            buildGeneralTab());
        add(tabs, BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
    }

    private JPanel buildProcessTab() {
        JPanel p = form();
        GridBagConstraints c = gbc();
        addHeader(p,c,"CONVEYOR_1 - Tiempo fijo");
        fConv1 = row(p,c,"Tiempo (min):",       sv(working.conv1Tiempo));
        addHeader(p,c,"ALMACEN_1 - Normal N(mu, sigma)");
        fAlm1M = row(p,c,"Media (min):",         sv(working.alm1Media));
        fAlm1S = row(p,c,"Desv. sigma (min):",   sv(working.alm1Sigma));
        fAlm1C = row(p,c,"Capacidad:",           sv(working.alm1Cap));
        addHeader(p,c,"CORTADORA - Exponencial E(mu)");
        fCortM = row(p,c,"Media (min):",         sv(working.cortMedia));
        fCortC = row(p,c,"Capacidad:",           sv(working.cortCap));
        addHeader(p,c,"TORNO - Normal N(mu, sigma)");
        fTornM = row(p,c,"Media (min):",         sv(working.tornMedia));
        fTornS = row(p,c,"Desv. sigma (min):",   sv(working.tornSigma));
        fTornC = row(p,c,"Capacidad:",           sv(working.tornCap));
        addHeader(p,c,"CONVEYOR_2 - Tiempo fijo");
        fConv2 = row(p,c,"Tiempo (min):",       sv(working.conv2Tiempo));
        addHeader(p,c,"FRESADORA - Exponencial E(mu)");
        fFresM = row(p,c,"Media (min):",         sv(working.fresMedia));
        fFresC = row(p,c,"Capacidad:",           sv(working.fresCap));
        addHeader(p,c,"ALMACEN_2 - Normal N(mu, sigma)");
        fAlm2M = row(p,c,"Media (min):",         sv(working.alm2Media));
        fAlm2S = row(p,c,"Desv. sigma (min):",   sv(working.alm2Sigma));
        fAlm2C = row(p,c,"Capacidad:",           sv(working.alm2Cap));
        addHeader(p,c,"PINTURA - Exponencial E(mu)");
        fPintM = row(p,c,"Media (min):",         sv(working.pintMedia));
        fPintC = row(p,c,"Capacidad:",           sv(working.pintCap));
        addHeader(p,c,"INSPECCION_1 - Normal N(mu, sigma)");
        fIns1M = row(p,c,"Media (min):",         sv(working.ins1Media));
        fIns1S = row(p,c,"Desv. sigma (min):",   sv(working.ins1Sigma));
        fIns1C = row(p,c,"Capacidad:",           sv(working.ins1Cap));
        addHeader(p,c,"INSPECCION_2 - Exponencial E(mu)");
        fIns2M = row(p,c,"Media (min):",         sv(working.ins2Media));
        fIns2C = row(p,c,"Capacidad:",           sv(working.ins2Cap));
        addHeader(p,c,"EMPAQUE - Normal N(mu, sigma)");
        fEmpM  = row(p,c,"Media (min):",         sv(working.empMedia));
        fEmpS  = row(p,c,"Desv. sigma (min):",   sv(working.empSigma));
        fEmpC  = row(p,c,"Capacidad:",           sv(working.empCap));
        addHeader(p,c,"EMBARQUE - Exponencial E(mu)");
        fEmbM  = row(p,c,"Media (min):",         sv(working.embMedia));
        fEmbC  = row(p,c,"Capacidad:",           sv(working.embCap));
        return scroll(p);
    }

    private JPanel buildResourceTab() {
        JPanel p = form();
        GridBagConstraints c = gbc();
        addHeader(p,c,"TRABAJADOR_1 - Cortadora a Torno");
        fT1  = row(p,c,"Tiempo traslado (min):", sv(working.t1Traslado));
        addHeader(p,c,"TRABAJADOR_2 - Fresadora a Almacen 2");
        fT2  = row(p,c,"Tiempo traslado (min):", sv(working.t2Traslado));
        addHeader(p,c,"TRABAJADOR_3 - Empaque a Embarque");
        fT3  = row(p,c,"Tiempo traslado (min):", sv(working.t3Traslado));
        addHeader(p,c,"MONTACARGAS - Almacen 2 a Pintura");
        fMk1 = row(p,c,"Tiempo traslado (min):", sv(working.mkTraslado1));
        addHeader(p,c,"MONTACARGAS - Pintura a Inspeccion 1");
        fMk2 = row(p,c,"Tiempo traslado (min):", sv(working.mkTraslado2));
        return scroll(p);
    }

    private JPanel buildGeneralTab() {
        JPanel p = form();
        GridBagConstraints c = gbc();
        addHeader(p,c,"LLEGADAS DE BARRAS");
        fArribo = row(p,c,"Frecuencia llegada (min):", sv(working.arriboFrecuencia));
        addHeader(p,c,"DURACION Y REPRODUCIBILIDAD");
        fDur  = row(p,c,"Duracion simulacion (min):", sv(working.duracion));
        fSeed = row(p,c,"Semilla aleatoria:",          sv(working.semilla));
        addHeader(p,c,"INSPECCION 1 - Probabilidad de rechazo");
        fRech = row(p,c,"Prob. envio a Insp. 2 (0-1):", sv(working.probRechazo));
        note(p,c,"  20% de piezas van a Inspeccion 2 por defecto");
        note(p,c,"  80% va directamente a Empaque");
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
            working.conv1Tiempo  = dbl(fConv1);
            working.alm1Media    = dbl(fAlm1M); working.alm1Sigma  = dbl(fAlm1S); working.alm1Cap  = itg(fAlm1C);
            working.cortMedia    = dbl(fCortM); working.cortCap    = itg(fCortC);
            working.tornMedia    = dbl(fTornM); working.tornSigma  = dbl(fTornS); working.tornCap  = itg(fTornC);
            working.conv2Tiempo  = dbl(fConv2);
            working.fresMedia    = dbl(fFresM); working.fresCap    = itg(fFresC);
            working.alm2Media    = dbl(fAlm2M); working.alm2Sigma  = dbl(fAlm2S); working.alm2Cap  = itg(fAlm2C);
            working.pintMedia    = dbl(fPintM); working.pintCap    = itg(fPintC);
            working.ins1Media    = dbl(fIns1M); working.ins1Sigma  = dbl(fIns1S); working.ins1Cap  = itg(fIns1C);
            working.ins2Media    = dbl(fIns2M); working.ins2Cap    = itg(fIns2C);
            working.empMedia     = dbl(fEmpM);  working.empSigma   = dbl(fEmpS);  working.empCap   = itg(fEmpC);
            working.embMedia     = dbl(fEmbM);  working.embCap     = itg(fEmbC);
            working.t1Traslado   = dbl(fT1);    working.t2Traslado = dbl(fT2);
            working.t3Traslado   = dbl(fT3);    working.mkTraslado1= dbl(fMk1);   working.mkTraslado2= dbl(fMk2);
            working.arriboFrecuencia = dbl(fArribo);
            working.duracion     = dbl(fDur);
            working.semilla      = Long.parseLong(fSeed.getText().trim());
            working.probRechazo  = dbl(fRech);
            if (working.probRechazo < 0 || working.probRechazo > 1)
                throw new NumberFormatException("Probabilidad debe ser 0-1");
            result = working;
            return true;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor invalido: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void loadDefaults() {
        SimParams d = new SimParams();
        fConv1.setText(sv(d.conv1Tiempo)); fAlm1M.setText(sv(d.alm1Media)); fAlm1S.setText(sv(d.alm1Sigma)); fAlm1C.setText(sv(d.alm1Cap));
        fCortM.setText(sv(d.cortMedia));   fCortC.setText(sv(d.cortCap));
        fTornM.setText(sv(d.tornMedia));   fTornS.setText(sv(d.tornSigma)); fTornC.setText(sv(d.tornCap));
        fConv2.setText(sv(d.conv2Tiempo)); fFresM.setText(sv(d.fresMedia)); fFresC.setText(sv(d.fresCap));
        fAlm2M.setText(sv(d.alm2Media));  fAlm2S.setText(sv(d.alm2Sigma)); fAlm2C.setText(sv(d.alm2Cap));
        fPintM.setText(sv(d.pintMedia));   fPintC.setText(sv(d.pintCap));
        fIns1M.setText(sv(d.ins1Media));  fIns1S.setText(sv(d.ins1Sigma)); fIns1C.setText(sv(d.ins1Cap));
        fIns2M.setText(sv(d.ins2Media));  fIns2C.setText(sv(d.ins2Cap));
        fEmpM .setText(sv(d.empMedia));   fEmpS .setText(sv(d.empSigma));  fEmpC .setText(sv(d.empCap));
        fEmbM .setText(sv(d.embMedia));   fEmbC .setText(sv(d.embCap));
        fT1   .setText(sv(d.t1Traslado)); fT2   .setText(sv(d.t2Traslado));
        fT3   .setText(sv(d.t3Traslado)); fMk1  .setText(sv(d.mkTraslado1)); fMk2.setText(sv(d.mkTraslado2));
        fArribo.setText(sv(d.arriboFrecuencia)); fDur.setText(sv(d.duracion));
        fSeed  .setText(sv(d.semilla));   fRech.setText(sv(d.probRechazo));
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
