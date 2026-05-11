import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/** ChartsDialog - Ventana de gráficas estadísticas con Graphics2D puro. */
public class ChartsDialog extends JDialog {
    private final SimState state;

    public ChartsDialog(JFrame owner, SimState state) {
        super(owner, "Graficas de Simulacion - Multi-Engrane", false);
        this.state = state;
        setSize(820, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(SimConstants.BG_DARK);
        build();
    }

    private void build() {
        setLayout(new BorderLayout());
        JLabel hdr = new JLabel("  Graficas de Simulacion en Tiempo Real");
        hdr.setFont(SimConstants.FONT_TITLE);
        hdr.setForeground(SimConstants.C_TEXT);
        hdr.setBackground(SimConstants.BG_HEADER);
        hdr.setOpaque(true);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0,0,2,0, SimConstants.C_ACCENT),
            BorderFactory.createEmptyBorder(8,10,8,10)));
        add(hdr, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(SimConstants.BG_PANEL);
        tabs.setForeground(SimConstants.C_TEXT);
        tabs.setFont(SimConstants.FONT_LABEL);
        tabs.addTab("Throughput",  new ThroughputChart(state));
        tabs.addTab("Utilizacion", new UtilizationChart(state));
        tabs.addTab("Colas",       new QueueChart(state));
        add(tabs, BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Actualizar Graficas");
        btnRefresh.setBackground(SimConstants.BG_CARD);
        btnRefresh.setForeground(SimConstants.C_ACCENT2);
        btnRefresh.setFont(SimConstants.FONT_LABEL);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        btnRefresh.addActionListener(e -> {
            for (int i=0; i<tabs.getTabCount(); i++) tabs.getComponentAt(i).repaint();
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setBackground(SimConstants.BG_HEADER);
        south.setBorder(BorderFactory.createMatteBorder(2,0,0,0, SimConstants.C_BORDER));
        south.add(btnRefresh);
        add(south, BorderLayout.SOUTH);
    }

    // ── Gráfica 1: Throughput ─────────────────────────────────────────────
    static class ThroughputChart extends JPanel {
        private final SimState state;
        ThroughputChart(SimState s) { this.state=s; setBackground(SimConstants.BG_DARK); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int W=getWidth(), H=getHeight(), pad=60;
            drawBg(g2,W,H,pad,"Throughput Acumulado - Piezas Finales vs. Tiempo");

            List<double[]> hist = new ArrayList<>(state.histThroughput);
            if (hist.size()<2) { noData(g2,W,H); return; }
            double maxT = hist.get(hist.size()-1)[0];
            double maxP = hist.get(hist.size()-1)[1];
            if (maxP==0||maxT==0) { noData(g2,W,H); return; }

            drawAxes(g2,pad,H-pad,W-2*pad,H-2*pad,maxT,maxP,"Tiempo (min)","Piezas");

            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setPaint(new GradientPaint(pad,H-pad,SimConstants.COL_PIEZA_CORTADA,
                                          pad+(W-2*pad),pad,SimConstants.COL_FINAL));
            Path2D path = new Path2D.Double();
            boolean first=true;
            for (double[] pt : hist) {
                int px = pad+(int)(pt[0]/maxT*(W-2*pad));
                int py = (H-pad)-(int)(pt[1]/maxP*(H-2*pad));
                if (first) { path.moveTo(px,py); first=false; } else path.lineTo(px,py);
            }
            g2.draw(path);
            path.lineTo(pad+(W-2*pad), H-pad); path.lineTo(pad,H-pad); path.closePath();
            g2.setColor(new Color(79,195,247,40)); g2.fill(path);

            double[] last = hist.get(hist.size()-1);
            int ex=pad+(int)(last[0]/maxT*(W-2*pad));
            int ey=(H-pad)-(int)(last[1]/maxP*(H-2*pad));
            g2.setColor(SimConstants.COL_FINAL);
            g2.fillOval(ex-5,ey-5,10,10);
            g2.setFont(SimConstants.FONT_LABEL);
            g2.drawString(String.format("%.0f piezas",last[1]), ex+8, ey+4);

            double rate=maxP/maxT;
            g2.setColor(new Color(255,180,50,150));
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10f,new float[]{6f,4f},0f));
            g2.drawLine(pad,H-pad, pad+(W-2*pad),(H-pad)-(int)(rate*(W-2*pad)/maxP*maxT));
            g2.setFont(new Font("Arial",Font.ITALIC,10));
            g2.drawString(String.format("Tendencia: %.2f piezas/min",rate), pad+8, pad+20);
        }
    }

    // ── Gráfica 2: Utilización ────────────────────────────────────────────
    static class UtilizationChart extends JPanel {
        private final SimState state;
        UtilizationChart(SimState s) { this.state=s; setBackground(SimConstants.BG_DARK); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int W=getWidth(), H=getHeight();
            drawBg(g2,W,H,50,"Utilizacion por Locacion (%)");

            Collection<Loc> locs = state.locs.values();
            int n=locs.size(); if (n==0) { noData(g2,W,H); return; }
            int padL=130, padR=60, padT=60, padB=40;
            int chartW=W-padL-padR;
            int barH=Math.min(28,(H-padT-padB)/n-4);
            int gap=(H-padT-padB-n*barH)/(n+1);

            g2.setFont(SimConstants.FONT_SMALL);
            DecimalFormat df=new DecimalFormat("0.0");
            int i=0;
            for (Loc loc : locs) {
                double util=loc.utilLive(state.clk,state.clk);
                int by=padT+gap+i*(barH+gap);
                int bw=(int)(util/100.0*chartW);
                g2.setColor(new Color(30,30,55)); g2.fillRoundRect(padL,by,chartW,barH,6,6);
                if (bw>0) {
                    Color c=util<50?new Color(40,160,60):util<80?new Color(200,150,0):new Color(200,50,50);
                    g2.setPaint(new GradientPaint(padL,by,c.darker(),padL+bw,by,c));
                    g2.fillRoundRect(padL,by,bw,barH,6,6);
                }
                g2.setColor(SimConstants.C_TEXT); g2.setFont(SimConstants.FONT_SMALL);
                FontMetrics fm=g2.getFontMetrics();
                String nm=loc.name.replace("INSPECCION","INSP.");
                g2.drawString(nm, padL-fm.stringWidth(nm)-6, by+barH/2+4);
                g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,10));
                g2.drawString(df.format(util)+"%", padL+bw+5, by+barH/2+4);
                i++;
            }
            int x80=padL+(int)(0.8*chartW);
            g2.setColor(new Color(255,80,80,150));
            g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,10f,new float[]{4f,3f},0f));
            g2.drawLine(x80,padT,x80,H-padB);
            g2.setFont(new Font("Arial",Font.ITALIC,9));
            g2.setColor(new Color(255,80,80,200)); g2.drawString("80%",x80-10,padT-5);
        }
    }

    // ── Gráfica 3: Nivel de cola ──────────────────────────────────────────
    static class QueueChart extends JPanel {
        private final SimState state;
        QueueChart(SimState s) { this.state=s; setBackground(SimConstants.BG_DARK); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int W=getWidth(), H=getHeight();
            drawBg(g2,W,H,50,"Entidades en Locacion (estado actual)");

            List<Loc> locs=new ArrayList<>(state.locs.values());
            int n=locs.size(); if(n==0){noData(g2,W,H);return;}
            int padL=55,padR=20,padT=60,padB=60;
            int chartW=W-padL-padR, chartH=H-padT-padB;
            int maxCnt=locs.stream().mapToInt(l->l.cap==Integer.MAX_VALUE?l.cnt:l.cap).max().orElse(1);
            maxCnt=Math.max(maxCnt,1);
            int barW=chartW/n-4;

            for (int i=0;i<n;i++) {
                Loc loc=locs.get(i);
                int bx=padL+i*(barW+4);
                int cnt=loc.cnt;
                int bh=(int)((double)cnt/maxCnt*chartH);
                g2.setColor(new Color(30,30,55)); g2.fillRect(bx,padT,barW,chartH);
                if (bh>0) {
                    Color c=loc.type.color();
                    g2.setPaint(new GradientPaint(bx,padT+chartH-bh,c.darker(),bx,padT+chartH,c));
                    g2.fillRect(bx,padT+chartH-bh,barW,bh);
                }
                g2.setColor(SimConstants.C_BORDER);
                g2.setStroke(new BasicStroke(1f)); g2.drawRect(bx,padT,barW,chartH);
                Graphics2D gc=(Graphics2D)g2.create();
                gc.translate(bx+barW/2, padT+chartH+10); gc.rotate(Math.PI/4);
                gc.setFont(new Font("Arial",Font.PLAIN,9));
                gc.setColor(SimConstants.C_TEXT);
                gc.drawString(loc.name.substring(0,Math.min(6,loc.name.length())),0,0);
                gc.dispose();
                g2.setColor(Color.WHITE); g2.setFont(new Font("Arial",Font.BOLD,10));
                String num=String.valueOf(cnt); FontMetrics fm=g2.getFontMetrics();
                if(bh>12) g2.drawString(num,bx+(barW-fm.stringWidth(num))/2,padT+chartH-bh-3);
            }
            g2.setFont(new Font("Arial",Font.PLAIN,9)); g2.setColor(SimConstants.C_MUTED);
            for (int v=0;v<=maxCnt;v+=Math.max(1,maxCnt/5)) {
                int py=padT+chartH-(int)((double)v/maxCnt*chartH);
                g2.drawLine(padL-4,py,padL,py); g2.drawString(String.valueOf(v),padL-22,py+4);
            }
        }
    }

    // ── Utilidades estáticas de dibujo ────────────────────────────────────
    static void drawBg(Graphics2D g2, int W, int H, int pad, String title) {
        g2.setColor(SimConstants.BG_DARK); g2.fillRect(0,0,W,H);
        g2.setColor(new Color(30,30,55)); g2.setStroke(new BasicStroke(0.5f));
        for (int x=pad;x<W-pad;x+=60) g2.drawLine(x,pad,x,H-pad);
        for (int y=pad;y<H-pad;y+=40) g2.drawLine(pad,y,W-pad,y);
        g2.setFont(new Font("Arial",Font.BOLD,13)); g2.setColor(SimConstants.C_TEXT);
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(title,(W-fm.stringWidth(title))/2, pad-10);
    }

    static void drawAxes(Graphics2D g2,int ox,int oy,int W,int H,
                          double maxX,double maxY,String xL,String yL) {
        g2.setColor(SimConstants.C_MUTED); g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(ox,oy,ox+W,oy); g2.drawLine(ox,oy,ox,oy-H);
        DecimalFormat df=new DecimalFormat("0");
        g2.setFont(new Font("Arial",Font.PLAIN,9));
        for (int i=0;i<=5;i++) {
            int px=ox+i*W/5; g2.drawLine(px,oy,px,oy+4); g2.drawString(df.format(maxX*i/5),px-8,oy+14);
            int py=oy-i*H/5; g2.drawLine(ox-4,py,ox,py); g2.drawString(df.format(maxY*i/5),ox-30,py+4);
        }
        g2.setFont(SimConstants.FONT_SMALL); g2.setColor(SimConstants.C_TEXT);
        g2.drawString(xL, ox+W/2-20, oy+28);
    }

    static void noData(Graphics2D g2, int W, int H) {
        g2.setFont(new Font("Arial",Font.BOLD,14)); g2.setColor(SimConstants.C_MUTED);
        String msg="Sin datos - ejecuta la simulacion mas tiempo";
        FontMetrics fm=g2.getFontMetrics(); g2.drawString(msg,(W-fm.stringWidth(msg))/2,H/2);
    }
}
