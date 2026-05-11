import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

import javax.swing.JPanel;

/**
 * FactoryPanel - Panel gráfico animado que muestra el layout de la fábrica.
 *
 * Dibuja con Graphics2D:
 *  - Fondo oscuro con grid sutil
 *  - Conexiones (flechas) entre locaciones
 *  - Cada locación con su ícono, color, nombre y contador dinámico
 *  - Barra de nivel de llenado para almacenes
 *  - Indicador de estado para recursos (libre/ocupado)
 *  - Entidades como puntos de colores en tránsito (animación)
 */
public class FactoryPanel extends JPanel {

    private SimState state;

    // Definición de conexiones (flechas entre locaciones)
    private static final String[][] CONNECTIONS = {
        {"CONVEYOR_1", "ALMACEN_1",    "directo"},
        {"ALMACEN_1",  "CORTADORA",    "T=3min"},
        {"CORTADORA",  "TORNO",        "T1"},
        {"TORNO",      "CONVEYOR_2",   "3min"},
        {"CONVEYOR_2", "FRESADORA",    "directo"},
        {"FRESADORA",  "ALMACEN_2",    "T2"},
        {"ALMACEN_2",  "PINTURA",      "MK"},
        {"PINTURA",    "INSPECCION_1", "MK"},
        {"INSPECCION_1","EMPAQUE",     "80%"},
        {"INSPECCION_1","INSPECCION_2","20%"},
        {"INSPECCION_2","EMPAQUE",     "3min"},
        {"EMPAQUE",    "EMBARQUE",     "T3"},
    };

    public FactoryPanel() {
        setBackground(SimConstants.BG_DARK);
        setPreferredSize(new Dimension(1080, 480));
    }

    public void setState(SimState s) {
        this.state = s;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();
        // Fondo con gradiente
        GradientPaint gp = new GradientPaint(0,0, new Color(10,10,28), W,H, new Color(18,18,40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, W, H);

        // Grid sutil
        drawGrid(g2, W, H);

        if (state == null) {
            drawPlaceholder(g2, W, H);
            return;
        }

        // Flechas de conexión
        drawConnections(g2);

        // Locaciones
        for (Loc loc : state.locs.values()) {
            drawLocation(g2, loc);
        }

        // Leyenda de recursos
        drawResourceLegend(g2, W, H);

        // Leyenda de entidades
        drawEntityLegend(g2, W, H);
    }

    // ── Cuadrícula de fondo ───────────────────────────────────────────────

    private void drawGrid(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(30, 30, 55));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);
    }

    private void drawPlaceholder(Graphics2D g2, int W, int H) {
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(SimConstants.C_MUTED);
        String msg = "Configura parámetros y presiona  ▶ Iniciar  para comenzar la simulación";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);

        // Dibujar layout estático de referencia
        if (state == null) drawStaticLayout(g2);
    }

    private void drawStaticLayout(Graphics2D g2) {
        SimParams p = new SimParams();
        SimState dummy = new SimState(p);
        for (Loc loc : dummy.locs.values()) {
            drawLocation(g2, loc);
        }
        drawConnectionsFor(g2, dummy);
    }

    // ── Conexiones (flechas) ──────────────────────────────────────────────

    private void drawConnections(Graphics2D g2) {
        drawConnectionsFor(g2, state);
    }

    private void drawConnectionsFor(Graphics2D g2, SimState st) {
        for (String[] conn : CONNECTIONS) {
            Loc from = st.loc(conn[0]);
            Loc to   = st.loc(conn[1]);
            if (from == null || to == null) continue;
            String label = conn[2];
            drawArrow(g2, from, to, label);
        }
    }

    private void drawArrow(Graphics2D g2, Loc from, Loc to, String label) {
        int fx = from.x + from.w / 2;
        int fy = from.y + from.h / 2;
        int tx = to.x   + to.w   / 2;
        int ty = to.y   + to.h   / 2;

        // Ajustar puntos a los bordes de los rectángulos
        int[] fp = edgePoint(from, tx, ty);
        int[] tp = edgePoint(to,   fx, fy);

        // Color de la flecha
        boolean isProbability = label.contains("%");
        Color arrowColor = isProbability
            ? new Color(255, 180, 50, 200)
            : new Color(80, 120, 180, 200);

        g2.setColor(arrowColor);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                10f, isProbability ? new float[]{5f, 3f} : null, 0f));
        g2.drawLine(fp[0], fp[1], tp[0], tp[1]);

        // Punta de flecha
        drawArrowHead(g2, fp[0], fp[1], tp[0], tp[1], arrowColor);

        // Etiqueta
        if (!label.equals("directo")) {
            int mx = (fp[0] + tp[0]) / 2;
            int my = (fp[1] + tp[1]) / 2 - 5;
            g2.setFont(SimConstants.FONT_SMALL);
            g2.setColor(new Color(180, 180, 220));
            g2.drawString(label, mx - 10, my);
        }
    }

    private void drawArrowHead(Graphics2D g2, int x1, int y1, int x2, int y2, Color c) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int size = 8;
        int[] xp = {x2,
            x2 - (int)(size * Math.cos(angle - 0.4)),
            x2 - (int)(size * Math.cos(angle + 0.4))};
        int[] yp = {y2,
            y2 - (int)(size * Math.sin(angle - 0.4)),
            y2 - (int)(size * Math.sin(angle + 0.4))};
        g2.setColor(c);
        g2.fillPolygon(xp, yp, 3);
    }

    private int[] edgePoint(Loc loc, int tx, int ty) {
        int cx = loc.x + loc.w / 2;
        int cy = loc.y + loc.h / 2;
        double dx = tx - cx, dy = ty - cy;
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len == 0) return new int[]{cx, cy};
        dx /= len; dy /= len;
        // Intersección con borde del rectángulo
        double t = Math.min(Math.abs((loc.w/2.0)/dx), Math.abs((loc.h/2.0)/dy));
        if (dx == 0) t = (loc.h/2.0)/Math.abs(dy);
        if (dy == 0) t = (loc.w/2.0)/Math.abs(dx);
        return new int[]{(int)(cx + dx*t), (int)(cy + dy*t)};
    }

    // ── Dibujado de locaciones ────────────────────────────────────────────

    private void drawLocation(Graphics2D g2, Loc loc) {
        int x = loc.x, y = loc.y, w = loc.w, h = loc.h;
        Color baseColor = loc.type.color();

        // ── Sombra ──────────────────────────────────────────────────────
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x + 3, y + 3, w, h, 12, 12);

        // ── Fondo del card ───────────────────────────────────────────────
        GradientPaint bg = new GradientPaint(
            x, y, baseColor.darker().darker(),
            x, y+h, baseColor.darker());
        g2.setPaint(bg);
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // ── Borde con color del tipo ──────────────────────────────────
        g2.setColor(baseColor);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // ── Dibujo específico por tipo ────────────────────────────────
        switch (loc.type) {
            case CONVEYOR:   drawConveyorDecor(g2, loc); break;
            case ALMACEN:    drawAlmacenDecor(g2, loc);  break;
            case MAQUINA:    drawGearDecor(g2, loc);     break;
            case INSPECCION: drawInspDecor(g2, loc);     break;
            default:         drawGenericDecor(g2, loc);  break;
        }

        // ── Nombre ────────────────────────────────────────────────────
        g2.setFont(SimConstants.FONT_SMALL);
        g2.setColor(SimConstants.C_TEXT);
        String name = loc.name.replace("_", " ").replace("INSPECCION", "INSP.");
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, x + (w - fm.stringWidth(name))/2, y + h + 14);

        // ── Badge contador ────────────────────────────────────────────
        drawCountBadge(g2, loc);

        // ── Barra de ocupación (almacenes) ────────────────────────────
        if (loc.type == LType.ALMACEN && loc.cap < Integer.MAX_VALUE) {
            drawFillBar(g2, loc);
        }
    }

    private void drawConveyorDecor(Graphics2D g2, Loc loc) {
        int x = loc.x, y = loc.y, w = loc.w, h = loc.h;
        g2.setColor(new Color(120, 140, 140, 120));
        int beltY = y + h/2 - 4;
        g2.fillRoundRect(x+5, beltY, w-10, 8, 4, 4);
        // ruedas
        int[] rx = {x+8, x+w/2-4, x+w-16};
        for (int rx2 : rx) {
            g2.setColor(new Color(80, 90, 90));
            g2.fillOval(rx2, beltY-4, 12, 16);
            g2.setColor(new Color(150, 160, 160));
            g2.drawOval(rx2, beltY-4, 12, 16);
        }
        // flechas indicadoras
        g2.setColor(new Color(200, 220, 220, 180));
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("→→→", x + w/2 - 14, y + h/2 + 16);
    }

    private void drawAlmacenDecor(Graphics2D g2, Loc loc) {
        int x = loc.x, y = loc.y, w = loc.w, h = loc.h;
        // Silueta de almacén (edificio)
        g2.setColor(new Color(200, 150, 0, 80));
        int bx = x + w/2 - 14, by = y + 10;
        g2.fillRect(bx, by+8, 28, 22);
        g2.fillPolygon(new int[]{bx-3,bx+14,bx+31}, new int[]{by+8,by,by+8}, 3);
        g2.setColor(new Color(255, 200, 50, 160));
        g2.drawRect(bx, by+8, 28, 22);
    }

    private void drawGearDecor(Graphics2D g2, Loc loc) {
        int cx = loc.x + loc.w/2;
        int cy = loc.y + loc.h/2 - 2;
        drawGear(g2, cx, cy, 18, 10, loc.cnt > 0);
    }

    private void drawGear(Graphics2D g2, int cx, int cy, int r, int teeth, boolean spinning) {
        double angle = spinning ? (System.currentTimeMillis() / 100.0 % (Math.PI*2)) : 0;
        Path2D gear = new Path2D.Double();
        int r2 = r - 5, r3 = r + 5;
        for (int i = 0; i < teeth; i++) {
            double a1 = angle + i * 2*Math.PI/teeth;
            double a2 = a1 + Math.PI/teeth * 0.6;
            double a3 = a2 + Math.PI/teeth * 0.4;
            double a4 = a3 + Math.PI/teeth * 0.6;
            if (i == 0) gear.moveTo(cx + r2*Math.cos(a1), cy + r2*Math.sin(a1));
            else        gear.lineTo(cx + r2*Math.cos(a1), cy + r2*Math.sin(a1));
            gear.lineTo(cx + r3*Math.cos(a2), cy + r3*Math.sin(a2));
            gear.lineTo(cx + r3*Math.cos(a3), cy + r3*Math.sin(a3));
            gear.lineTo(cx + r2*Math.cos(a4), cy + r2*Math.sin(a4));
        }
        gear.closePath();
        g2.setColor(new Color(60, 100, 160, 200));
        g2.fill(gear);
        g2.setColor(new Color(100, 160, 220));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(gear);
        g2.setColor(new Color(40, 60, 100));
        g2.fillOval(cx-5, cy-5, 10, 10);
    }

    private void drawInspDecor(Graphics2D g2, Loc loc) {
        int cx = loc.x + loc.w/2;
        int cy = loc.y + loc.h/2 - 2;
        g2.setColor(new Color(0, 180, 200, 160));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx-12, cy-12, 20, 20); // lupa
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(cx+6, cy+6, cx+14, cy+14); // mango
    }

    private void drawGenericDecor(Graphics2D g2, Loc loc) {
        int cx = loc.x + loc.w/2;
        int cy = loc.y + loc.h/2 - 2;
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(loc.type.color().brighter());
        String icon = loc.type.icon();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(icon, cx - fm.stringWidth(icon)/2, cy + 8);
    }

    // ── Badge de contador ─────────────────────────────────────────────────

    private void drawCountBadge(Graphics2D g2, Loc loc) {
        int cnt = loc.cnt + loc.waitingCount();
        int bx = loc.x + loc.w - 20, by = loc.y - 12;
        // Fondo del badge
        Color bg = cnt == 0 ? new Color(40, 40, 60)
                 : cnt >= loc.cap * 0.8 ? new Color(180, 40, 40)
                 : new Color(40, 100, 40);
        g2.setColor(bg);
        g2.fillOval(bx - 2, by - 2, 24, 24);
        g2.setColor(bg.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(bx - 2, by - 2, 24, 24);
        // Número
        g2.setFont(SimConstants.FONT_COUNT);
        g2.setColor(Color.WHITE);
        String num = String.valueOf(cnt);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(num, bx + (20 - fm.stringWidth(num))/2, by + 16);
    }

    // ── Barra de nivel para almacenes ─────────────────────────────────────

    private void drawFillBar(Graphics2D g2, Loc loc) {
        if (loc.cap <= 0 || loc.cap == Integer.MAX_VALUE) return;
        int bx = loc.x + 4, by = loc.y + loc.h - 10, bw = loc.w - 8, bh = 6;
        float pct = (float) loc.cnt / loc.cap;
        g2.setColor(new Color(30, 30, 50));
        g2.fillRoundRect(bx, by, bw, bh, 3, 3);
        Color fillC = pct < 0.5 ? SimConstants.C_SUCCESS : pct < 0.85 ? SimConstants.C_WARNING : SimConstants.C_ACCENT;
        g2.setColor(fillC);
        g2.fillRoundRect(bx, by, (int)(bw * pct), bh, 3, 3);
    }

    // ── Leyendas ──────────────────────────────────────────────────────────

    private void drawResourceLegend(Graphics2D g2, int W, int H) {
        if (state == null) return;
        int lx = 10, ly = H - 95;
        g2.setColor(new Color(20, 20, 45, 200));
        g2.fillRoundRect(lx - 5, ly - 15, 210, 90, 8, 8);
        g2.setColor(SimConstants.C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx - 5, ly - 15, 210, 90, 8, 8);

        g2.setFont(SimConstants.FONT_LABEL);
        g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("RECURSOS", lx, ly);

        String[][] resInfo = {
            {"T1", "TRABAJADOR_1"}, {"T2", "TRABAJADOR_2"},
            {"T3", "TRABAJADOR_3"}, {"MK", "MONTACARGAS"}
        };
        int ry = ly + 16;
        for (String[] ri : resInfo) {
            Res r = state.res(ri[0]);
            if (r == null) continue;
            Color rc = r.busy ? SimConstants.C_ACCENT : SimConstants.C_SUCCESS;
            g2.setColor(rc);
            g2.fillOval(lx, ry - 8, 10, 10);
            g2.setColor(SimConstants.C_TEXT);
            g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(ri[1] + " — " + (r.busy ? "OCUPADO" : "LIBRE"), lx + 14, ry);
            ry += 16;
        }
    }

    private void drawEntityLegend(Graphics2D g2, int W, int H) {
        int lx = W - 185, ly = H - 95;
        g2.setColor(new Color(20, 20, 45, 200));
        g2.fillRoundRect(lx - 5, ly - 15, 180, 90, 8, 8);
        g2.setColor(SimConstants.C_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(lx - 5, ly - 15, 180, 90, 8, 8);

        g2.setFont(SimConstants.FONT_LABEL);
        g2.setColor(SimConstants.C_ACCENT2);
        g2.drawString("ENTIDADES", lx, ly);

        EType[] types = EType.values();
        int ey = ly + 16;
        for (EType t : types) {
            g2.setColor(t.color());
            g2.fillOval(lx, ey - 8, 10, 10);
            g2.setColor(SimConstants.C_TEXT);
            g2.setFont(SimConstants.FONT_SMALL);
            g2.drawString(t.label(), lx + 14, ey);
            ey += 14;
        }
    }
}
