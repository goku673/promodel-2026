import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

/**
 * FactoryPanel - Layout animado de la fábrica con partículas en movimiento.
 *
 * FIXES:
 *  - FlowParticle almacena sx/sy correctamente (no calculados on-the-fly)
 *  - Partículas se generan en TODAS las conexiones activas
 *  - Barras de capacidad visibles en TODOS los almacenes
 *  - Engranes giran cuando locación está activa
 *  - Reloj muestra HR:MM en el panel
 */
public class FactoryPanel extends JPanel {

    private SimState state;
    private int animPhase = 0;  // 0-359 para animaciones

    // Lista de partículas activas en tránsito
    private final List<FlowParticle> particles = new ArrayList<>();
    private long lastGenTime = 0;

    // ── Conexiones del modelo ─────────────────────────────────────────────
    private static final String[][] CONN = {
        // {desde, hasta, etiqueta, tipo-entidad}
        {"CONVEYOR_1",   "ALMACEN_1",    "directo", "BARRA"},
        {"ALMACEN_1",    "CORTADORA",    "3 min",   "BARRA"},
        {"CORTADORA",    "TORNO",        "T1",      "CORTADA"},
        {"TORNO",        "CONVEYOR_2",   "3 min",   "TORNEADA"},
        {"CONVEYOR_2",   "FRESADORA",    "directo", "TORNEADA"},
        {"FRESADORA",    "ALMACEN_2",    "T2",      "FRESADA"},
        {"ALMACEN_2",    "PINTURA",      "MK",      "FRESADA"},
        {"PINTURA",      "INSPECCION_1", "MK",      "PINTADA"},
        {"INSPECCION_1", "EMPAQUE",      "80%",     "PINTADA"},
        {"INSPECCION_1", "INSPECCION_2", "20%",     "PINTADA"},
        {"INSPECCION_2", "EMPAQUE",      "3 min",   "PINTADA"},
        {"EMPAQUE",      "EMBARQUE",     "T3",      "FINAL"},
    };

    // ── Partícula visual que viaja entre dos locaciones ───────────────────
    static class FlowParticle {
        // CORRECCIÓN: guardar posición inicial explícitamente
        final float sx, sy, ex, ey;
        float x, y;
        float progress = 0f;
        final float speed;
        final Color color;

        FlowParticle(float sx, float sy, float ex, float ey, Color c) {
            this.sx = sx; this.sy = sy;
            this.ex = ex; this.ey = ey;
            this.x  = sx; this.y  = sy;
            this.color = c;
            this.speed = 0.006f + (float)(Math.random() * 0.010f);
        }

        void update() {
            progress = Math.min(1f, progress + speed);
            x = sx + progress * (ex - sx);   // interpolación lineal correcta
            y = sy + progress * (ey - sy);
        }

        boolean done() { return progress >= 1f; }
    }

    public FactoryPanel() {
        setBackground(SimConstants.BG_DARK);
        setPreferredSize(new Dimension(1080, 490));
        // Timer de animacion ~30 fps
        new javax.swing.Timer(33, e -> {
            // Solo animar si la simulacion esta corriendo
            if (state != null && state.running) {
                animPhase = (animPhase + 3) % 360;
                updateParticles();
                updateAgents();    // mover icono de trabajadores
            } else if (state != null && state.finished) {
                // Despues de terminar, mantener el ultimo estado visible pero estatico
                animPhase = (animPhase + 1) % 360; // animacion muy lenta
            }
            repaint();
        }).start();
    }

    public void setState(SimState s) {
        this.state = s;
        particles.clear();
        repaint();
    }

    // ── Animar agentes (workers/montacargas) hacia su target ──────────────
    private void updateAgents() {
        if (state == null) return;
        for (float[] a : state.resAgents.values()) {
            // curX/Y (indices 2,3) se interpolan hacia tgtX/Y (indices 4,5)
            float dx = a[4] - a[2];
            float dy = a[5] - a[3];
            float speed = 4.0f;   // pixeles por frame
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            if (dist > speed) {
                a[2] += dx / dist * speed;
                a[3] += dy / dist * speed;
            } else {
                a[2] = a[4];
                a[3] = a[5];
            }
        }
    }

    // ── Actualiza partículas y genera nuevas ──────────────────────────────
    private void updateParticles() {
        if (state == null) return;

        // Avanzar posición de partículas existentes
        Iterator<FlowParticle> it = particles.iterator();
        while (it.hasNext()) {
            FlowParticle p = it.next();
            p.update();
            if (p.done()) it.remove();
        }

        // Limitar total de partículas para no saturar
        if (particles.size() > 120) return;

        long now = System.currentTimeMillis();
        if (now - lastGenTime < 350) return;   // generar cada 350ms
        lastGenTime = now;

        // Generar partículas en TODAS las conexiones activas
        for (String[] c : CONN) {
            Loc from = state.loc(c[0]);
            Loc to   = state.loc(c[1]);
            if (from == null || to == null) continue;

            // Generar si la locación de origen tiene o ha tenido actividad
            boolean active = (from.cnt > 0 || from.processed > 0);
            if (!active) continue;

            // Probabilidad proporcional a la actividad
            double prob = from.cnt > 0 ? 0.6 : 0.25;
            if (Math.random() > prob) continue;

            // Centro de cada locación como punto origen/destino
            float sx = from.x + from.w / 2f;
            float sy = from.y + from.h / 2f;
            float ex = to.x   + to.w   / 2f;
            float ey = to.y   + to.h   / 2f;

            // Ajustar a borde del rectángulo
            float[] fp = edgePt(from, (int)ex, (int)ey);
            float[] tp = edgePt(to,   (int)sx, (int)sy);

            Color col = entityColor(c[3]);
            particles.add(new FlowParticle(fp[0], fp[1], tp[0], tp[1], col));
        }
    }

    private Color entityColor(String hint) {
        switch (hint) {
            case "BARRA":    return SimConstants.COL_BARRA;
            case "CORTADA":  return SimConstants.COL_PIEZA_CORTADA;
            case "TORNEADA": return SimConstants.COL_TORNEADA;
            case "FRESADA":  return SimConstants.COL_FRESADA;
            case "PINTADA":  return SimConstants.COL_PINTADA;
            case "FINAL":    return SimConstants.COL_FINAL;
            default:         return Color.WHITE;
        }
    }

    // ── Pintado principal ─────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,     RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // Fondo degradado oscuro
        g2.setPaint(new GradientPaint(0, 0, new Color(10,10,28), W, H, new Color(18,18,42)));
        g2.fillRect(0, 0, W, H);
        drawGrid(g2, W, H);

        // Usar estado actual o estado vacío para mostrar layout estático
        SimState st = (state != null) ? state : new SimState(new SimParams());

        // 1. Conexiones/flechas
        drawAllConnections(g2, st);

        // 2. Partículas en tránsito (solo si corriendo)
        if (state != null && (state.running || state.finished)) {
            for (FlowParticle p : new ArrayList<>(particles)) {
                drawParticle(g2, p);
            }
        }

        // 3. Locaciones
        for (Loc loc : st.locs.values()) {
            drawLocation(g2, loc);
        }

        // 4. Agentes (trabajadores / montacargas)
        if (state != null && (state.running || state.finished)) {
            drawAllAgents(g2, state);
        }

        // 5. Leyendas
        drawResourceLegend(g2, W, H, st);
        drawEntityLegend(g2, W, H);

        // 6. Mensaje inicial si no hay simulacion activa
        if (state == null || (!state.running && !state.finished)) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(SimConstants.C_MUTED);
            String msg = "Configura parametros y presiona Iniciar para simular";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H - 20);
        }
    }

    private void drawParticle(Graphics2D g2, FlowParticle p) {
        float pulse = (float)(0.75 + 0.25 * Math.sin(Math.toRadians(animPhase * 4)));
        int r = (int)(6 * pulse);
        // Brillo (glow)
        g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 60));
        g2.fillOval((int)p.x - r - 3, (int)p.y - r - 3, (r+3)*2, (r+3)*2);
        // Cuerpo
        g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 230));
        g2.fillOval((int)p.x - r, (int)p.y - r, r*2, r*2);
        // Borde blanco
        g2.setColor(new Color(255, 255, 255, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval((int)p.x - r, (int)p.y - r, r*2, r*2);
    }

    // ── Cuadrícula ────────────────────────────────────────────────────────
    private void drawGrid(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(28, 28, 52));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);
    }

    // ── Conexiones / flechas ──────────────────────────────────────────────
    private void drawAllConnections(Graphics2D g2, SimState st) {
        for (String[] c : CONN) {
            Loc from = st.loc(c[0]); Loc to = st.loc(c[1]);
            if (from == null || to == null) continue;
            drawArrow(g2, from, to, c[2]);
        }
    }

    private void drawArrow(Graphics2D g2, Loc from, Loc to, String label) {
        float[] fp = edgePt(from, to.x + to.w/2,   to.y + to.h/2);
        float[] tp = edgePt(to,   from.x + from.w/2, from.y + from.h/2);

        boolean isPct = label.contains("%");
        Color ac = isPct ? new Color(255, 180, 50, 190) : new Color(80, 130, 200, 180);

        g2.setColor(ac);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            10f, isPct ? new float[]{5f, 3f} : null, 0f));
        g2.drawLine((int)fp[0], (int)fp[1], (int)tp[0], (int)tp[1]);
        drawArrowHead(g2, fp[0], fp[1], tp[0], tp[1], ac);

        if (!label.equals("directo")) {
            g2.setFont(SimConstants.FONT_SMALL);
            g2.setColor(new Color(190, 190, 230));
            int mx = (int)(fp[0] + tp[0]) / 2;
            int my = (int)(fp[1] + tp[1]) / 2 - 6;
            g2.drawString(label, mx - 10, my);
        }
    }

    private void drawArrowHead(Graphics2D g2, float x1, float y1, float x2, float y2, Color c) {
        double a = Math.atan2(y2 - y1, x2 - x1);
        int s = 8;
        int[] xp = {(int)x2, (int)(x2 - s*Math.cos(a-0.4)), (int)(x2 - s*Math.cos(a+0.4))};
        int[] yp = {(int)y2, (int)(y2 - s*Math.sin(a-0.4)), (int)(y2 - s*Math.sin(a+0.4))};
        g2.setColor(c);
        g2.fillPolygon(xp, yp, 3);
    }

    /** Calcula el punto de borde del rectángulo de la locación hacia (tx,ty) */
    private float[] edgePt(Loc loc, int tx, int ty) {
        int cx = loc.x + loc.w / 2, cy = loc.y + loc.h / 2;
        double dx = tx - cx, dy = ty - cy;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return new float[]{cx, cy};
        dx /= len; dy /= len;
        double t;
        if (dx == 0)       t = (loc.h / 2.0) / Math.abs(dy);
        else if (dy == 0)  t = (loc.w / 2.0) / Math.abs(dx);
        else               t = Math.min(Math.abs(loc.w/2.0/dx), Math.abs(loc.h/2.0/dy));
        return new float[]{(float)(cx + dx*t), (float)(cy + dy*t)};
    }

    // ── Dibujado de locaciones ────────────────────────────────────────────
    private void drawLocation(Graphics2D g2, Loc loc) {
        int x=loc.x, y=loc.y, w=loc.w, h=loc.h;
        Color base = loc.type.color();
        boolean active = loc.cnt > 0;

        // Glow pulsante si está activa
        if (active) {
            float glow = (float)(0.4 + 0.4 * Math.abs(Math.sin(Math.toRadians(animPhase*2))));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)(70*glow)));
            g2.fillRoundRect(x-6, y-6, w+12, h+12, 18, 18);
        }

        // Sombra
        g2.setColor(new Color(0,0,0,90));
        g2.fillRoundRect(x+4, y+4, w, h, 12, 12);

        // Fondo card
        g2.setPaint(new GradientPaint(x,y,base.darker().darker(),x,y+h,base.darker()));
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Borde
        g2.setColor(active ? base.brighter() : base);
        g2.setStroke(new BasicStroke(active ? 2.5f : 1.5f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Decoración interna por tipo
        switch (loc.type) {
            case CONVEYOR:   drawConveyorDecor(g2, loc); break;
            case ALMACEN:    drawAlmacenDecor(g2, loc);  break;
            case MAQUINA:    drawGear(g2, x+w/2, y+h/2-2, 18, 10, active); break;
            case INSPECCION: drawInspDecor(g2, loc);     break;
            default:         drawGenericDecor(g2, loc);  break;
        }

        // Nombre debajo
        g2.setFont(SimConstants.FONT_SMALL);
        g2.setColor(SimConstants.C_TEXT);
        String nm = loc.name.replace("_"," ").replace("INSPECCION","INSP.");
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(nm, x + (w - fm.stringWidth(nm))/2, y + h + 14);

        // Badge contador (arriba-derecha)
        drawCountBadge(g2, loc);

        // CONTADOR ESPECIAL EN EMBARQUE: total acumulado (1,2,3,4...)
        if (loc.name.equals("EMBARQUE") && state != null && (state.running || state.finished)) {
            drawEmbarqueCounter(g2, loc);
        }

        // BARRA DE CAPACIDAD (en todos los almacenes y empaque/embarque)
        if ((loc.type == LType.ALMACEN || loc.type == LType.EMPAQUE || loc.type == LType.EMBARQUE)
                && loc.cap < Integer.MAX_VALUE) {
            drawCapacityBar(g2, loc);
        }

        // Dots de entidades dentro del box
        drawEntityDots(g2, loc);
    }

    private void drawConveyorDecor(Graphics2D g2, Loc loc) {
        int x=loc.x,y=loc.y,w=loc.w,h=loc.h;
        int beltY = y + h/2 - 4;
        g2.setColor(new Color(100,120,120,130));
        g2.fillRoundRect(x+5, beltY, w-10, 8, 4, 4);
        // Ruedas
        for (int rx : new int[]{x+8, x+w/2-4, x+w-16}) {
            g2.setColor(new Color(70,80,80)); g2.fillOval(rx, beltY-4, 12, 16);
            g2.setColor(new Color(140,150,150)); g2.drawOval(rx, beltY-4, 12, 16);
        }
        // Punto animado moviéndose por la correa
        float pos = (animPhase / 360f);
        int dotX = x + 10 + (int)((w-24) * pos);
        g2.setColor(new Color(220, 230, 230, 200));
        g2.fillOval(dotX, beltY, 8, 8);
        g2.setFont(new Font("Arial",Font.BOLD,9));
        g2.setColor(new Color(180,200,200,160));
        g2.drawString(">>", x+w/2-8, y+h/2+16);
    }

    private void drawAlmacenDecor(Graphics2D g2, Loc loc) {
        int bx=loc.x+loc.w/2-12, by=loc.y+8;
        g2.setColor(new Color(200,150,0,80));
        g2.fillRect(bx, by+8, 24, 18);
        g2.fillPolygon(new int[]{bx-3,bx+12,bx+27}, new int[]{by+8,by,by+8}, 3);
        g2.setColor(new Color(255,200,50,150)); g2.drawRect(bx,by+8,24,18);
    }

    private void drawGear(Graphics2D g2, int cx, int cy, int r, int teeth, boolean spin) {
        // Solo girar si la simulacion esta corriendo
        boolean doSpin = spin && state != null && state.running;
        double angle = doSpin ? (System.currentTimeMillis() / 80.0 % (Math.PI*2)) : 0;
        Path2D gear = new Path2D.Double();
        int r2 = r-5, r3 = r+5;
        for (int i=0;i<teeth;i++) {
            double a1=angle+i*2*Math.PI/teeth, a2=a1+Math.PI/teeth*0.6;
            double a3=a2+Math.PI/teeth*0.4,   a4=a3+Math.PI/teeth*0.6;
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

    private void drawInspDecor(Graphics2D g2, Loc loc) {
        int cx=loc.x+loc.w/2, cy=loc.y+loc.h/2-2;
        g2.setColor(new Color(0,180,200,180));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx-11,cy-11,20,20);
        g2.setStroke(new BasicStroke(3f)); g2.drawLine(cx+7,cy+7,cx+14,cy+14);
    }

    private void drawGenericDecor(Graphics2D g2, Loc loc) {
        int cx=loc.x+loc.w/2, cy=loc.y+loc.h/2;
        g2.setFont(new Font("Arial",Font.BOLD,18));
        g2.setColor(loc.type.color().brighter());
        String icon=loc.type.icon(); FontMetrics fm=g2.getFontMetrics();
        g2.drawString(icon, cx-fm.stringWidth(icon)/2, cy+8);
    }

    /** Badge circular en la esquina superior derecha con el conteo */
    private void drawCountBadge(Graphics2D g2, Loc loc) {
        int total = loc.cnt + loc.waitingCount();
        int bx=loc.x+loc.w-18, by=loc.y-12;
        Color bg = total == 0       ? new Color(40,40,60)
                 : loc.cap < Integer.MAX_VALUE && total >= (int)(loc.cap*0.85)
                                    ? new Color(180,40,40)
                                    : new Color(35,100,40);
        g2.setColor(bg);        g2.fillOval(bx-2,by-2,24,24);
        g2.setColor(bg.brighter()); g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(bx-2,by-2,24,24);
        g2.setFont(SimConstants.FONT_COUNT); g2.setColor(Color.WHITE);
        String num = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(num, bx+(20-fm.stringWidth(num))/2, by+16);
    }

    /**
     * Contador acumulado en EMBARQUE: muestra "Total: 1, 2, 3, 4..." directamente
     * en el box del Embarque con animacion de pulso.
     */
    private void drawEmbarqueCounter(Graphics2D g2, Loc loc) {
        int total = state.embarqueTotales.get();

        // Panel de fondo dentro del box
        int px = loc.x + 4, py = loc.y + 20, pw = loc.w - 8, ph = 28;
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(px, py, pw, ph, 6, 6);

        // Etiqueta "TOTAL"
        g2.setFont(new Font("Arial", Font.BOLD, 7));
        g2.setColor(new Color(180, 210, 255, 200));
        g2.drawString("TOTAL LLEGADOS", px + 4, py + 9);

        // Numero grande con pulso si cambio recientemente
        float pulse = (float)(0.9 + 0.1 * Math.abs(Math.sin(Math.toRadians(animPhase * 3))));
        int fontSize = (int)(18 * pulse);
        g2.setFont(new Font("Arial", Font.BOLD, fontSize));

        // Color: verde si < 100, dorado si >= 100, naranja si >= 500
        Color numColor = total < 100  ? new Color(50, 255, 100)
                       : total < 500  ? new Color(255, 215, 50)
                       :                new Color(255, 130, 50);
        g2.setColor(numColor);

        String numStr = String.valueOf(total);
        FontMetrics fm = g2.getFontMetrics();
        int nx = px + (pw - fm.stringWidth(numStr)) / 2;
        int ny = py + ph - 4;
        g2.drawString(numStr, nx, ny);

        // Borde del panel
        g2.setColor(numColor.darker());
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(px, py, pw, ph, 6, 6);
    }

    /**
     * MEDIDOR DE CAPACIDAD Y ESCALA — barra vertical a la izquierda de la locación,
     * igual que ProModel. Verde-Amarillo-Rojo según porcentaje de llenado.
     */
    private void drawCapacityBar(Graphics2D g2, Loc loc) {
        if (loc.cap <= 0 || loc.cap == Integer.MAX_VALUE) return;
        float pct = Math.min(1f, (float) loc.cnt / loc.cap);
        int bx = loc.x - 18;       // barra a la IZQUIERDA del box
        int by = loc.y;
        int bw = 10, bh = loc.h;

        // Fondo
        g2.setColor(new Color(25,25,45));
        g2.fillRoundRect(bx, by, bw, bh, 4, 4);

        // Relleno (de abajo hacia arriba)
        int fillH = (int)(bh * pct);
        Color fillC = pct < 0.5f ? SimConstants.C_SUCCESS
                    : pct < 0.85f ? SimConstants.C_WARNING
                    : SimConstants.C_ACCENT;
        g2.setColor(fillC);
        g2.fillRoundRect(bx, by + bh - fillH, bw, fillH, 4, 4);

        // Borde
        g2.setColor(SimConstants.C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, bw, bh, 4, 4);

        // Porcentaje debajo
        g2.setFont(new Font("Arial", Font.PLAIN, 8));
        g2.setColor(SimConstants.C_MUTED);
        String pctStr = (int)(pct*100)+"%";
        g2.drawString(pctStr, bx - 2, by + bh + 11);
    }

    /** Puntos de colores representando entidades dentro de la locación */
    private void drawEntityDots(Graphics2D g2, Loc loc) {
        int cnt = Math.min(loc.cnt, 8);
        if (cnt <= 0) return;
        int dotR = 4;
        int startX = loc.x + 6, startY = loc.y + loc.h - 14;
        for (int i = 0; i < cnt; i++) {
            float pulse = (float)(0.8 + 0.2*Math.sin(Math.toRadians(animPhase*2 + i*40)));
            int r = (int)(dotR * pulse);
            g2.setColor(new Color(220, 180, 50, 200));
            g2.fillOval(startX + i*(dotR*2+2) - r/2, startY - r/2, r*2, r*2);
        }
    }

    // ── AGENTES: dibujar personita (workers) y montacargas ──────────────────
    private static final Color[] AGENT_COLORS = {
        new Color(255, 215,  50),  // T1 - amarillo
        new Color( 50, 220, 120),  // T2 - verde
        new Color(100, 180, 255),  // T3 - celeste
        new Color(255, 130,  50),  // MK - naranja
    };
    private static final String[] AGENT_KEYS  = {"T1","T2","T3","MK"};
    private static final boolean[] AGENT_IS_MK = {false, false, false, true};

    private void drawAllAgents(Graphics2D g2, SimState st) {
        for (int i = 0; i < AGENT_KEYS.length; i++) {
            float[] a = st.resAgents.get(AGENT_KEYS[i]);
            if (a == null) continue;
            Res res = st.res(AGENT_KEYS[i]);
            boolean busy = (res != null && res.busy);
            Color col = AGENT_COLORS[i];
            int ax = (int) a[2], ay = (int) a[3];
            if (AGENT_IS_MK[i]) {
                drawForklift(g2, ax, ay, col, busy);
            } else {
                drawPerson(g2, ax, ay, col, busy);
            }
        }
    }

    /** Dibuja una personita estilo sticktfigure */
    private void drawPerson(Graphics2D g2, int cx, int cy, Color col, boolean busy) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Glow si esta ocupado
        if (busy) {
            float glow = (float)(0.4 + 0.4 * Math.abs(Math.sin(Math.toRadians(animPhase * 4))));
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(100 * glow)));
            g2.fillOval(cx - 14, cy - 26, 28, 28);
        }

        // Cabeza
        g2.setColor(col);
        g2.fillOval(cx - 6, cy - 22, 12, 12);
        g2.setColor(col.darker());
        g2.drawOval(cx - 6, cy - 22, 12, 12);

        // Cuerpo
        g2.setColor(col);
        g2.drawLine(cx, cy - 10, cx, cy + 4);

        // Brazos (animados si busy)
        double armAngle = busy ? Math.sin(Math.toRadians(animPhase * 5)) * 0.5 : 0.3;
        int ax1 = cx + (int)(10 * Math.cos(Math.PI/2 + armAngle));
        int ay1 = cy - 6 + (int)(6 * Math.sin(Math.PI/2 + armAngle));
        int ax2 = cx - (int)(10 * Math.cos(Math.PI/2 + armAngle));
        int ay2 = cy - 6 + (int)(6 * Math.sin(Math.PI/2 + armAngle));
        g2.drawLine(cx, cy - 6, ax1, ay1);
        g2.drawLine(cx, cy - 6, ax2, ay2);

        // Piernas (animadas si busy)
        double legAngle = busy ? Math.sin(Math.toRadians(animPhase * 5)) * 0.4 : 0.2;
        g2.drawLine(cx, cy + 4, cx + (int)(8 * Math.sin(legAngle)),  cy + 14);
        g2.drawLine(cx, cy + 4, cx - (int)(8 * Math.sin(legAngle)),  cy + 14);

        // Etiqueta pequeña
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.setColor(col);
        g2.drawString(busy ? "OCUP." : "LIBRE", cx - 10, cy + 24);
    }

    /** Dibuja un montacargas simplificado */
    private void drawForklift(Graphics2D g2, int cx, int cy, Color col, boolean busy) {
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Glow si ocupado
        if (busy) {
            float glow = (float)(0.4 + 0.4 * Math.abs(Math.sin(Math.toRadians(animPhase * 4))));
            g2.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(80 * glow)));
            g2.fillOval(cx - 16, cy - 20, 32, 26);
        }

        // Cuerpo del montacargas
        g2.setColor(col.darker());
        g2.fillRoundRect(cx - 12, cy - 16, 20, 14, 4, 4);
        g2.setColor(col);
        g2.drawRoundRect(cx - 12, cy - 16, 20, 14, 4, 4);

        // Cabina (parte superior)
        g2.setColor(col.darker().darker());
        g2.fillRect(cx - 8, cy - 22, 12, 8);
        g2.setColor(col);
        g2.drawRect(cx - 8, cy - 22, 12, 8);

        // Horquilla (fork) — mueve arriba/abajo si busy
        int forkY = busy ? cy - 16 - (int)(4 * Math.abs(Math.sin(Math.toRadians(animPhase * 3)))) : cy - 10;
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawLine(cx + 8, forkY, cx + 18, forkY);       // brazo superior
        g2.drawLine(cx + 8, forkY + 4, cx + 18, forkY + 4); // brazo inferior
        g2.drawLine(cx + 8, forkY - 4, cx + 8, forkY + 8);  // mástil

        // Ruedas
        g2.setColor(new Color(60, 60, 60));
        g2.fillOval(cx - 12, cy - 3, 8, 8);
        g2.fillOval(cx + 5,  cy - 3, 8, 8);
        g2.setColor(new Color(120, 120, 120));
        g2.drawOval(cx - 12, cy - 3, 8, 8);
        g2.drawOval(cx + 5,  cy - 3, 8, 8);

        // Etiqueta
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        g2.setColor(col);
        g2.drawString(busy ? "OCUP." : "LIBRE", cx - 12, cy + 14);
    }

    // ── Leyendas ──────────────────────────────────────────────────────────
    private void drawResourceLegend(Graphics2D g2, int W, int H, SimState st) {
        int lx=10, ly=H-100;
        g2.setColor(new Color(18,18,42,220)); g2.fillRoundRect(lx-5,ly-18,220,96,8,8);
        g2.setColor(SimConstants.C_BORDER); g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx-5,ly-18,220,96,8,8);
        g2.setFont(SimConstants.FONT_LABEL); g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("RECURSOS", lx, ly);
        String[][] ri = {{"T1","TRABAJADOR_1"},{"T2","TRABAJADOR_2"},
                         {"T3","TRABAJADOR_3"},{"MK","MONTACARGAS"}};
        int ry = ly + 17;
        for (String[] r : ri) {
            Res res = st.res(r[0]); if (res==null) continue;
            g2.setColor(res.busy ? SimConstants.C_ACCENT : SimConstants.C_SUCCESS);
            g2.fillOval(lx, ry-8, 10, 10);
            g2.setColor(SimConstants.C_TEXT); g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(r[1]+" — "+(res.busy?"OCUPADO":"LIBRE"), lx+14, ry);
            ry += 17;
        }
    }

    private void drawEntityLegend(Graphics2D g2, int W, int H) {
        int lx=W-195, ly=H-100;
        g2.setColor(new Color(18,18,42,220)); g2.fillRoundRect(lx-5,ly-18,190,96,8,8);
        g2.setColor(SimConstants.C_BORDER); g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx-5,ly-18,190,96,8,8);
        g2.setFont(SimConstants.FONT_LABEL); g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("ENTIDADES", lx, ly);
        int ey = ly + 17;
        for (EType t : EType.values()) {
            g2.setColor(t.color()); g2.fillOval(lx, ey-8, 10, 10);
            g2.setColor(SimConstants.C_TEXT); g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(t.label(), lx+14, ey);
            ey += 14;
        }
    }
}
