import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * FactoryPanel - Layout animado de la fábrica con partículas en movimiento.
 * Usa javax.swing.Timer a 30fps para animar engranes girando y partículas
 * moviéndose entre locaciones (simulando el flujo de piezas como ProModel).
 */
public class FactoryPanel extends JPanel {

    private SimState state;
    private int animPhase = 0;  // 0-359, usado para rotación y pulso

    // Partículas que viajan entre locaciones
    private final List<FlowParticle> particles = new ArrayList<>();
    private long lastParticleTime = 0;

    // Conexiones del proceso
    private static final String[][] CONNECTIONS = {
        {"CONVEYOR_1","ALMACEN_1",  "directo", "BARRA"},
        {"ALMACEN_1", "CORTADORA",  "3 min",   "BARRA"},
        {"CORTADORA", "TORNO",      "T1",      "CORTADA"},
        {"TORNO",     "CONVEYOR_2", "3 min",   "TORNEADA"},
        {"CONVEYOR_2","FRESADORA",  "directo", "TORNEADA"},
        {"FRESADORA", "ALMACEN_2",  "T2",      "FRESADA"},
        {"ALMACEN_2", "PINTURA",    "MK",      "FRESADA"},
        {"PINTURA",   "INSPECCION_1","MK",     "PINTADA"},
        {"INSPECCION_1","EMPAQUE",  "80%",     "PINTADA"},
        {"INSPECCION_1","INSPECCION_2","20%",  "PINTADA"},
        {"INSPECCION_2","EMPAQUE",  "3 min",   "PINTADA"},
        {"EMPAQUE",   "EMBARQUE",   "T3",      "FINAL"},
    };

    /** Partícula visual que viaja de una locación a otra */
    static class FlowParticle {
        float x, y, tx, ty;  // posición actual y objetivo
        float progress = 0;  // 0.0 → 1.0
        float speed;         // progreso por tick
        Color color;
        String fromLoc, toLoc;

        FlowParticle(float sx, float sy, float ex, float ey, Color c, String from, String to) {
            x=sx; y=sy; tx=ex; ty=ey; color=c; fromLoc=from; toLoc=to;
            speed = 0.012f + (float)(Math.random()*0.008);
        }
        void update() {
            progress = Math.min(1f, progress + speed);
            x = sx() + progress*(tx - sx());
            y = sy() + progress*(ty - sy());
        }
        private float sx() { return tx - (tx-x)/(1-progress+0.001f); }
        private float sy() { return ty - (ty-y)/(1-progress+0.001f); }
        boolean done() { return progress >= 1f; }
    }

    public FactoryPanel() {
        setBackground(SimConstants.BG_DARK);
        setPreferredSize(new Dimension(1080, 480));
        // Timer de animación a ~30fps
        new javax.swing.Timer(33, e -> {
            animPhase = (animPhase + 3) % 360;
            updateParticles();
            repaint();
        }).start();
    }

    public void setState(SimState s) { this.state = s; particles.clear(); repaint(); }

    /** Actualiza partículas existentes y genera nuevas según el estado */
    private void updateParticles() {
        if (state == null) return;
        // Avanzar partículas
        particles.removeIf(FlowParticle::done);
        for (FlowParticle p : particles) p.update();

        // Generar nuevas partículas según throughput
        long now = System.currentTimeMillis();
        if (now - lastParticleTime > 400) {
            lastParticleTime = now;
            for (String[] conn : CONNECTIONS) {
                Loc from = state.loc(conn[0]);
                Loc to   = state.loc(conn[1]);
                if (from == null || to == null) continue;
                // Solo generar si hay flujo activo
                if (from.processed > 0 || from.cnt > 0) {
                    if (Math.random() < 0.35) {
                        Color c = particleColor(conn[3]);
                        float sx = from.x + from.w/2f;
                        float sy = from.y + from.h/2f;
                        float ex = to.x   + to.w/2f;
                        float ey = to.y   + to.h/2f;
                        particles.add(new FlowParticle(sx,sy,ex,ey,c,conn[0],conn[1]));
                    }
                }
            }
        }
    }

    private Color particleColor(String entityHint) {
        switch (entityHint) {
            case "BARRA":    return SimConstants.COL_BARRA;
            case "CORTADA":  return SimConstants.COL_PIEZA_CORTADA;
            case "TORNEADA": return SimConstants.COL_TORNEADA;
            case "FRESADA":  return SimConstants.COL_FRESADA;
            case "PINTADA":  return SimConstants.COL_PINTADA;
            case "FINAL":    return SimConstants.COL_FINAL;
            default:         return Color.WHITE;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();
        g2.setPaint(new GradientPaint(0,0,new Color(10,10,28),W,H,new Color(18,18,42)));
        g2.fillRect(0,0,W,H);
        drawGrid(g2,W,H);

        SimState st = (state != null) ? state : new SimState(new SimParams());
        drawConnectionsFor(g2, st);

        // Partículas animadas (piezas en tránsito)
        for (FlowParticle p : new ArrayList<>(particles)) {
            float pulse = (float)(0.7 + 0.3*Math.sin(Math.toRadians(animPhase*3)));
            int r = (int)(7*pulse);
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 220));
            g2.fillOval((int)p.x - r, (int)p.y - r, r*2, r*2);
            g2.setColor(p.color.brighter());
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval((int)p.x - r, (int)p.y - r, r*2, r*2);
        }

        for (Loc loc : st.locs.values()) drawLocation(g2, loc);
        drawResourceLegend(g2, W, H);
        drawEntityLegend(g2, W, H);

        if (state == null) {
            g2.setFont(new Font("Arial",Font.BOLD,15));
            g2.setColor(SimConstants.C_MUTED);
            String msg = "Configura parametros y presiona Iniciar";
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(msg,(W-fm.stringWidth(msg))/2, H-30);
        }
    }

    private void drawGrid(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(30,30,55));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x=0;x<W;x+=40) g2.drawLine(x,0,x,H);
        for (int y=0;y<H;y+=40) g2.drawLine(0,y,W,y);
    }

    private void drawConnectionsFor(Graphics2D g2, SimState st) {
        for (String[] conn : CONNECTIONS) {
            Loc from=st.loc(conn[0]); Loc to=st.loc(conn[1]);
            if (from==null||to==null) continue;
            drawArrow(g2,from,to,conn[2]);
        }
    }

    private void drawArrow(Graphics2D g2, Loc from, Loc to, String label) {
        int[] fp=edgePt(from,to.x+to.w/2,to.y+to.h/2);
        int[] tp=edgePt(to,from.x+from.w/2,from.y+from.h/2);
        boolean isPct=label.contains("%");
        Color ac=isPct?new Color(255,180,50,180):new Color(80,120,180,180);
        g2.setColor(ac);
        g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
            10f,isPct?new float[]{5f,3f}:null,0f));
        g2.drawLine(fp[0],fp[1],tp[0],tp[1]);
        drawHead(g2,fp[0],fp[1],tp[0],tp[1],ac);
        if (!label.equals("directo")) {
            g2.setFont(SimConstants.FONT_SMALL);
            g2.setColor(new Color(180,180,220));
            int mx=(fp[0]+tp[0])/2, my=(fp[1]+tp[1])/2-5;
            g2.drawString(label,mx-10,my);
        }
    }

    private void drawHead(Graphics2D g2,int x1,int y1,int x2,int y2,Color c) {
        double a=Math.atan2(y2-y1,x2-x1); int s=8;
        int[] xp={x2,x2-(int)(s*Math.cos(a-0.4)),x2-(int)(s*Math.cos(a+0.4))};
        int[] yp={y2,y2-(int)(s*Math.sin(a-0.4)),y2-(int)(s*Math.sin(a+0.4))};
        g2.setColor(c); g2.fillPolygon(xp,yp,3);
    }

    private int[] edgePt(Loc loc,int tx,int ty) {
        int cx=loc.x+loc.w/2, cy=loc.y+loc.h/2;
        double dx=tx-cx, dy=ty-cy, len=Math.sqrt(dx*dx+dy*dy);
        if (len==0) return new int[]{cx,cy};
        dx/=len; dy/=len;
        double t=(dx==0)?(loc.h/2.0)/Math.abs(dy)
                :(dy==0)?(loc.w/2.0)/Math.abs(dx)
                :Math.min(Math.abs(loc.w/2.0/dx),Math.abs(loc.h/2.0/dy));
        return new int[]{(int)(cx+dx*t),(int)(cy+dy*t)};
    }

    private void drawLocation(Graphics2D g2, Loc loc) {
        int x=loc.x,y=loc.y,w=loc.w,h=loc.h;
        Color base=loc.type.color();

        // Pulso si hay entidades activas
        boolean active = loc.cnt > 0;
        if (active) {
            float glow=(float)(0.5+0.5*Math.sin(Math.toRadians(animPhase*2)));
            Color glowC=new Color(base.getRed(),base.getGreen(),base.getBlue(),(int)(60*glow));
            g2.setColor(glowC);
            g2.fillRoundRect(x-4,y-4,w+8,h+8,16,16);
        }

        g2.setColor(new Color(0,0,0,80));
        g2.fillRoundRect(x+3,y+3,w,h,12,12);
        g2.setPaint(new GradientPaint(x,y,base.darker().darker(),x,y+h,base.darker()));
        g2.fillRoundRect(x,y,w,h,12,12);
        g2.setColor(active ? base.brighter() : base);
        g2.setStroke(new BasicStroke(active?2.5f:1.5f));
        g2.drawRoundRect(x,y,w,h,12,12);

        // Decoración por tipo
        switch (loc.type) {
            case CONVEYOR:   drawConveyor(g2,loc); break;
            case ALMACEN:    drawAlmacen(g2,loc);  break;
            case MAQUINA:    drawGear(g2,loc.x+loc.w/2,loc.y+loc.h/2-2,18,10,active); break;
            case INSPECCION: drawInsp(g2,loc); break;
            default:         drawGeneric(g2,loc); break;
        }

        // Nombre
        g2.setFont(SimConstants.FONT_SMALL); g2.setColor(SimConstants.C_TEXT);
        String nm=loc.name.replace("_"," ").replace("INSPECCION","INSP.");
        FontMetrics fm=g2.getFontMetrics();
        g2.drawString(nm,x+(w-fm.stringWidth(nm))/2,y+h+14);

        drawBadge(g2,loc);
        if (loc.type==LType.ALMACEN && loc.cap<Integer.MAX_VALUE) drawFillBar(g2,loc);

        // Dots de entidades dentro de la locación
        drawEntityDots(g2, loc);
    }

    /** Dibuja puntos de colores representando entidades dentro de la locación */
    private void drawEntityDots(Graphics2D g2, Loc loc) {
        int cnt = Math.min(loc.cnt, 8);
        if (cnt <= 0) return;
        int dotR = 4;
        int startX = loc.x + 8;
        int startY = loc.y + loc.h - 16;
        for (int i = 0; i < cnt; i++) {
            float pulse = (float)(0.8+0.2*Math.sin(Math.toRadians(animPhase*2 + i*45)));
            int r = (int)(dotR * pulse);
            Color dc = new Color(200+i*5, 180, 50, 200);
            g2.setColor(dc);
            g2.fillOval(startX + i*(dotR*2+2) - r/2, startY - r/2, r*2, r*2);
        }
    }

    private void drawConveyor(Graphics2D g2, Loc loc) {
        int x=loc.x,y=loc.y,w=loc.w,h=loc.h;
        int beltY=y+h/2-4;
        g2.setColor(new Color(100,120,120,130));
        g2.fillRoundRect(x+5,beltY,w-10,8,4,4);
        int[] rx={x+8,x+w/2-4,x+w-16};
        for (int rx2:rx) {
            g2.setColor(new Color(70,80,80)); g2.fillOval(rx2,beltY-4,12,16);
            g2.setColor(new Color(140,150,150)); g2.drawOval(rx2,beltY-4,12,16);
        }
        // Animación: punto deslizándose por la correa
        float pos=(animPhase/360f);
        int dotX=x+8+(int)((w-20)*pos);
        g2.setColor(new Color(200,220,220,180));
        g2.fillOval(dotX,beltY,8,8);
        g2.setColor(new Color(180,200,200)); g2.setFont(new Font("Arial",Font.BOLD,10));
        g2.drawString(">>",x+w/2-10,y+h/2+16);
    }

    private void drawAlmacen(Graphics2D g2, Loc loc) {
        int bx=loc.x+loc.w/2-14, by=loc.y+10;
        g2.setColor(new Color(200,150,0,80));
        g2.fillRect(bx,by+8,28,22);
        g2.fillPolygon(new int[]{bx-3,bx+14,bx+31},new int[]{by+8,by,by+8},3);
        g2.setColor(new Color(255,200,50,160)); g2.drawRect(bx,by+8,28,22);
    }

    private void drawGear(Graphics2D g2,int cx,int cy,int r,int teeth,boolean spinning) {
        double angle=spinning?(System.currentTimeMillis()/80.0%(Math.PI*2)):0;
        Path2D gear=new Path2D.Double();
        int r2=r-5,r3=r+5;
        for (int i=0;i<teeth;i++) {
            double a1=angle+i*2*Math.PI/teeth;
            double a2=a1+Math.PI/teeth*0.6;
            double a3=a2+Math.PI/teeth*0.4;
            double a4=a3+Math.PI/teeth*0.6;
            if (i==0) gear.moveTo(cx+r2*Math.cos(a1),cy+r2*Math.sin(a1));
            else       gear.lineTo(cx+r2*Math.cos(a1),cy+r2*Math.sin(a1));
            gear.lineTo(cx+r3*Math.cos(a2),cy+r3*Math.sin(a2));
            gear.lineTo(cx+r3*Math.cos(a3),cy+r3*Math.sin(a3));
            gear.lineTo(cx+r2*Math.cos(a4),cy+r2*Math.sin(a4));
        }
        gear.closePath();
        g2.setColor(new Color(60,100,160,200)); g2.fill(gear);
        g2.setColor(new Color(100,160,220)); g2.setStroke(new BasicStroke(1f)); g2.draw(gear);
        g2.setColor(new Color(40,60,100)); g2.fillOval(cx-5,cy-5,10,10);
    }

    private void drawInsp(Graphics2D g2, Loc loc) {
        int cx=loc.x+loc.w/2, cy=loc.y+loc.h/2-2;
        g2.setColor(new Color(0,180,200,180));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx-11,cy-11,20,20);
        g2.setStroke(new BasicStroke(3f)); g2.drawLine(cx+7,cy+7,cx+14,cy+14);
    }

    private void drawGeneric(Graphics2D g2, Loc loc) {
        int cx=loc.x+loc.w/2, cy=loc.y+loc.h/2-2;
        g2.setFont(new Font("Arial",Font.BOLD,20));
        g2.setColor(loc.type.color().brighter());
        String icon=loc.type.icon(); FontMetrics fm=g2.getFontMetrics();
        g2.drawString(icon,cx-fm.stringWidth(icon)/2,cy+8);
    }

    private void drawBadge(Graphics2D g2, Loc loc) {
        int cnt=loc.cnt+loc.waitingCount();
        int bx=loc.x+loc.w-20, by=loc.y-12;
        Color bg=cnt==0?new Color(40,40,60):cnt>=(int)(loc.cap*0.8)?new Color(180,40,40):new Color(40,100,40);
        g2.setColor(bg); g2.fillOval(bx-2,by-2,24,24);
        g2.setColor(bg.brighter()); g2.setStroke(new BasicStroke(1.5f)); g2.drawOval(bx-2,by-2,24,24);
        g2.setFont(SimConstants.FONT_COUNT); g2.setColor(Color.WHITE);
        String num=String.valueOf(cnt); FontMetrics fm=g2.getFontMetrics();
        g2.drawString(num,bx+(20-fm.stringWidth(num))/2,by+16);
    }

    private void drawFillBar(Graphics2D g2, Loc loc) {
        if (loc.cap<=0||loc.cap==Integer.MAX_VALUE) return;
        int bx=loc.x+4,by=loc.y+loc.h-10,bw=loc.w-8,bh=5;
        float pct=(float)loc.cnt/loc.cap;
        g2.setColor(new Color(30,30,50)); g2.fillRoundRect(bx,by,bw,bh,3,3);
        Color fc=pct<0.5f?SimConstants.C_SUCCESS:pct<0.85f?SimConstants.C_WARNING:SimConstants.C_ACCENT;
        g2.setColor(fc); g2.fillRoundRect(bx,by,(int)(bw*pct),bh,3,3);
    }

    private void drawResourceLegend(Graphics2D g2, int W, int H) {
        SimState st=state; if(st==null) return;
        int lx=10,ly=H-100;
        g2.setColor(new Color(20,20,45,210)); g2.fillRoundRect(lx-5,ly-15,215,92,8,8);
        g2.setColor(SimConstants.C_BORDER); g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx-5,ly-15,215,92,8,8);
        g2.setFont(SimConstants.FONT_LABEL); g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("RECURSOS",lx,ly);
        String[][] ri={{"T1","TRABAJADOR_1"},{"T2","TRABAJADOR_2"},{"T3","TRABAJADOR_3"},{"MK","MONTACARGAS"}};
        int ry=ly+16;
        for (String[] r:ri) {
            Res res=st.res(r[0]); if(res==null) continue;
            g2.setColor(res.busy?SimConstants.C_ACCENT:SimConstants.C_SUCCESS);
            g2.fillOval(lx,ry-8,10,10);
            g2.setColor(SimConstants.C_TEXT); g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(r[1]+" - "+(res.busy?"OCUPADO":"LIBRE"),lx+14,ry);
            ry+=16;
        }
    }

    private void drawEntityLegend(Graphics2D g2, int W, int H) {
        int lx=W-190,ly=H-100;
        g2.setColor(new Color(20,20,45,210)); g2.fillRoundRect(lx-5,ly-15,185,92,8,8);
        g2.setColor(SimConstants.C_BORDER); g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx-5,ly-15,185,92,8,8);
        g2.setFont(SimConstants.FONT_LABEL); g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("ENTIDADES",lx,ly);
        int ey=ly+16;
        for (EType t:EType.values()) {
            g2.setColor(t.color()); g2.fillOval(lx,ey-8,10,10);
            g2.setColor(SimConstants.C_TEXT); g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(t.label(),lx+14,ey);
            ey+=14;
        }
    }
}
