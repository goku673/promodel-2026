import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Renderers
 *
 * Clases auxiliares para aislar la lógica de dibujo.
 *
 * Contiene:
 * - PathRenderer: dibuja flechas/rutas.
 * - LocationRenderer: dibuja locaciones.
 * - EntityRenderer: dibuja entidades animadas.
 * - ResourceRenderer: dibuja recursos móviles.
 */
public class Renderers {

    // ─────────────────────────────────────────────────────────────────────
    // FLECHAS / RUTAS
    // ─────────────────────────────────────────────────────────────────────
    public static class PathRenderer {

        public static void drawArrow(
                Graphics2D g2,
                int x1,
                int y1,
                int cx2,
                int cy2,
                int w2,
                int h2
        ) {
            double dx = cx2 - x1;
            double dy = cy2 - y1;

            if (dx == 0 && dy == 0) {
                return;
            }

            double sx = (dx != 0)
                    ? Math.abs((w2 / 2.0 + 4) / dx)
                    : Double.MAX_VALUE;

            double sy = (dy != 0)
                    ? Math.abs((h2 / 2.0 + 4) / dy)
                    : Double.MAX_VALUE;

            double s = Math.min(sx, sy);

            if (s >= 1.0) {
                return;
            }

            int x2 = (int) (cx2 - s * dx);
            int y2 = (int) (cy2 - s * dy);

            g2.drawLine(x1, y1, x2, y2);

            double angle = Math.atan2(y2 - y1, x2 - x1);
            int arrowSize = 12;

            int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 7));
            int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 7));

            int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 7));
            int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 7));

            g2.fillPolygon(
                    new int[]{x2, x3, x4},
                    new int[]{y2, y3, y4},
                    3
            );
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ENTIDADES
    // ─────────────────────────────────────────────────────────────────────
    public static class EntityRenderer {

        public static void draw(
                Graphics2D g2,
                Entity e,
                SimState state,
                Image entityImg,
                JPanel observer
        ) {
            if (g2 == null || e == null || state == null) {
                return;
            }

            float ex;
            float ey;

            /*
             * Si la entidad está moviéndose, se interpola entre:
             * curX/curY -> targetX/targetY
             *
             * Esto depende de que state.clk avance suavemente en SimWorker/SimEngine.
             */
            if (e.moving && e.moveEndTime > e.moveStartTime) {
                double p = (state.clk - e.moveStartTime) / (e.moveEndTime - e.moveStartTime);

                if (p < 0) {
                    p = 0;
                }

                if (p > 1) {
                    p = 1;
                }

                ex = e.curX + (float) (p * (e.targetX - e.curX));
                ey = e.curY + (float) (p * (e.targetY - e.curY));
            } else {
                Loc loc = state.locs.get(e.curLoc);

                if (loc != null) {
                    if (loc.cap == 1) {
                        ex = loc.x + loc.w / 2f;
                        ey = loc.y + loc.h / 2f;
                    } else {
                        int cellW = Math.max(1, (loc.w - 10) / 3);
                        int cellH = Math.max(1, (loc.h - 10) / 3);

                        int offset = e.id % 9;
                        int col = offset % 3;
                        int row = offset / 3;

                        ex = loc.x + 5 + col * cellW + cellW / 2f;
                        ey = loc.y + 5 + row * cellH + cellH / 2f;
                    }
                } else {
                    ex = e.curX;
                    ey = e.curY;
                }
            }

            int size = 24;

            Loc curLocation = state.locs.get(e.curLoc);

            if (curLocation != null) {
                int minDim = Math.min(curLocation.w, curLocation.h);
                size = Math.max(16, Math.min(minDim / 3, 48));
            }

            Rectangle clip = g2.getClipBounds();

            if (clip != null) {
                int half = size / 2;

                if (ex + half < clip.x
                        || ex - half > clip.x + clip.width
                        || ey + half < clip.y
                        || ey - half > clip.y + clip.height) {
                    return;
                }
            }

            // Sombra
            g2.setColor(new Color(0, 0, 0, 70));
            g2.fillOval(
                    (int) ex - size / 2 + 3,
                    (int) ey - size / 2 + 4,
                    size,
                    size
            );

            if (entityImg != null) {
                g2.drawImage(
                        entityImg,
                        (int) ex - size / 2,
                        (int) ey - size / 2,
                        size,
                        size,
                        observer
                );
            } else {
                g2.setColor(new Color(220, 40, 40));
                g2.fillOval(
                        (int) ex - size / 2,
                        (int) ey - size / 2,
                        size,
                        size
                );

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(
                        (int) ex - size / 2,
                        (int) ey - size / 2,
                        size,
                        size
                );
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // LOCACIONES
    // ─────────────────────────────────────────────────────────────────────
    public static class LocationRenderer {

        public static void draw(
                Graphics2D g2,
                Loc loc,
                Image img,
                boolean isSelected,
                boolean isRunning,
                JPanel observer
        ) {
            if (g2 == null || loc == null) {
                return;
            }

            int x = loc.x;
            int y = loc.y;
            int w = loc.w;
            int h = loc.h;

            Object oldAA = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Sombra
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(x + 4, y + 4, w, h, 16, 16);

            // Fondo
            GradientPaint gp = new GradientPaint(
                    x,
                    y,
                    new Color(245, 248, 255),
                    x,
                    y + h,
                    new Color(215, 225, 245)
            );

            g2.setPaint(gp);
            g2.fillRoundRect(x, y, w, h, 16, 16);

            // Imagen de la locación
            if (img != null) {
                int imgPad = 8;
                int imgH = Math.max(24, h - 34);

                g2.drawImage(
                        img,
                        x + imgPad,
                        y + imgPad,
                        Math.max(24, w - imgPad * 2),
                        imgH,
                        observer
                );
            } else {
                g2.setColor(new Color(180, 190, 210));
                g2.fillRoundRect(x + 10, y + 10, Math.max(1, w - 20), Math.max(1, h - 34), 12, 12);

                g2.setColor(new Color(90, 100, 120));
                g2.setFont(new Font("Arial", Font.BOLD, 11));

                String txt = "LOC";
                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        txt,
                        x + (w - fm.stringWidth(txt)) / 2,
                        y + Math.max(24, (h - 18) / 2)
                );
            }

            // Borde
            if (isSelected) {
                g2.setColor(new Color(255, 180, 40));
                g2.setStroke(new BasicStroke(3f));
            } else if (isRunning) {
                g2.setColor(new Color(70, 160, 230));
                g2.setStroke(new BasicStroke(2f));
            } else {
                g2.setColor(new Color(70, 80, 100));
                g2.setStroke(new BasicStroke(1.5f));
            }

            g2.drawRoundRect(x, y, w, h, 16, 16);

            // Barra inferior con nombre
            int barH = 20;

            g2.setColor(new Color(25, 35, 55, 210));
            g2.fillRect(x, y + h - barH, w, barH);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));

            String name = loc.name != null ? loc.name : "";
            FontMetrics fm = g2.getFontMetrics();

            if (fm.stringWidth(name) > w - 8) {
                name = trimText(name, fm, w - 8);
            }

            g2.drawString(
                    name,
                    x + (w - fm.stringWidth(name)) / 2,
                    y + h - 6
            );

            // Contador visual
            if (loc.showCounter) {
                drawCounter(g2, loc, x, y);
            }

            // Medidor visual
            if (loc.showGauge) {
                drawGauge(g2, loc, x, y, w, h);
            }

            // Tirador de redimensionamiento
            if (isSelected && !isRunning) {
                g2.setColor(new Color(255, 180, 40));
                g2.fillRect(x + w - 8, y + h - 8, 8, 8);

                g2.setColor(Color.BLACK);
                g2.drawRect(x + w - 8, y + h - 8, 8, 8);
            }

            if (oldAA != null) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
            }
        }

        /**
         * Contadores corregidos:
         *
         * - Contenido Actual  -> loc.cnt
         * - Entradas Totales  -> loc.totalEntries
         * - Salidas           -> loc.processed
         */
        private static void drawCounter(Graphics2D g2, Loc loc, int x, int y) {
            int value = loc.cnt;
            String label = "ACT";

            if (loc.counterType != null) {
                String type = loc.counterType.toLowerCase();

                if (type.contains("entrada")) {
                    value = loc.totalEntries;
                    label = "ENT";
                } else if (type.contains("salida")) {
                    value = loc.processed;
                    label = "SAL";
                } else {
                    value = loc.cnt;
                    label = "ACT";
                }
            }

            String text = label + ": " + value;

            g2.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();

            int bw = Math.max(38, fm.stringWidth(text) + 12);
            int bh = 18;

            int bx = x + 4;
            int by = y + 4;

            g2.setColor(new Color(0, 0, 0, 175));
            g2.fillRoundRect(bx, by, bw, bh, 10, 10);

            g2.setColor(Color.WHITE);
            g2.drawRoundRect(bx, by, bw, bh, 10, 10);

            g2.drawString(text, bx + 6, by + 13);
        }

        private static void drawGauge(Graphics2D g2, Loc loc, int x, int y, int w, int h) {
            int gaugeW = Math.max(20, w - 16);
            int gaugeH = 6;

            int gx = x + 8;
            int gy = y + h - 29;

            double ratio;

            if (loc.cap == Integer.MAX_VALUE || loc.cap <= 0) {
                ratio = 0.0;
            } else {
                ratio = Math.min(1.0, Math.max(0.0, loc.cnt / (double) loc.cap));
            }

            g2.setColor(new Color(40, 40, 40, 170));
            g2.fillRoundRect(gx, gy, gaugeW, gaugeH, 8, 8);

            Color fill;

            if (ratio < 0.5) {
                fill = new Color(50, 190, 90);
            } else if (ratio < 0.85) {
                fill = new Color(240, 180, 40);
            } else {
                fill = new Color(220, 60, 50);
            }

            g2.setColor(fill);
            g2.fillRoundRect(gx, gy, (int) (gaugeW * ratio), gaugeH, 8, 8);
        }

        private static String trimText(String text, FontMetrics fm, int maxWidth) {
            if (text == null) {
                return "";
            }

            String t = text;

            while (t.length() > 3 && fm.stringWidth(t + "...") > maxWidth) {
                t = t.substring(0, t.length() - 1);
            }

            return t + "...";
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // RECURSOS MÓVILES
    // ─────────────────────────────────────────────────────────────────────
    public static class ResourceRenderer {

        /**
         * Dibuja un recurso usando la posición interpolada guardada en SimState.
         *
         * FactoryPanel debe llamarlo así:
         *
         * Renderers.ResourceRenderer.draw(g2, resName, state, resourceImg, this);
         */
        public static void draw(
                Graphics2D g2,
                String resourceName,
                SimState state,
                Image resourceImg,
                JPanel observer
        ) {
            if (g2 == null || state == null || resourceName == null) {
                return;
            }

            if (state.resAgents == null || !state.resAgents.containsKey(resourceName)) {
                return;
            }

            float rx = state.getAgentCurrentX(resourceName);
            float ry = state.getAgentCurrentY(resourceName);

            int size = 34;

            Rectangle clip = g2.getClipBounds();

            if (clip != null) {
                int half = size / 2;

                if (rx + half < clip.x
                        || rx - half > clip.x + clip.width
                        || ry + half < clip.y
                        || ry - half > clip.y + clip.height) {
                    return;
                }
            }

            // Sombra
            g2.setColor(new Color(0, 0, 0, 80));
            g2.fillOval(
                    (int) rx - size / 2 + 3,
                    (int) ry - size / 2 + 4,
                    size,
                    size
            );

            if (resourceImg != null) {
                g2.drawImage(
                        resourceImg,
                        (int) rx - size / 2,
                        (int) ry - size / 2,
                        size,
                        size,
                        observer
                );
            } else {
                drawDefaultResource(g2, rx, ry, size);
            }

            // Borde visible
            g2.setColor(new Color(20, 25, 35));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(
                    (int) rx - size / 2,
                    (int) ry - size / 2,
                    size,
                    size
            );

            drawResourceLabel(g2, resourceName, rx, ry, size);
        }

        private static void drawDefaultResource(Graphics2D g2, float rx, float ry, int size) {
            int x = (int) rx - size / 2;
            int y = (int) ry - size / 2;

            GradientPaint gp = new GradientPaint(
                    x,
                    y,
                    new Color(40, 120, 230),
                    x,
                    y + size,
                    new Color(20, 70, 160)
            );

            g2.setPaint(gp);
            g2.fillOval(x, y, size, size);

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f));

            int cx = (int) rx;
            int cy = (int) ry;

            // Figura simple tipo operador
            g2.drawOval(cx - 5, cy - 11, 10, 10);
            g2.drawLine(cx, cy - 1, cx, cy + 10);
            g2.drawLine(cx - 9, cy + 3, cx + 9, cy + 3);
            g2.drawLine(cx, cy + 10, cx - 7, cy + 16);
            g2.drawLine(cx, cy + 10, cx + 7, cy + 16);
        }

        private static void drawResourceLabel(
                Graphics2D g2,
                String resourceName,
                float rx,
                float ry,
                int size
        ) {
            String label = resourceName;

            g2.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();

            if (fm.stringWidth(label) > 110) {
                label = trimLabel(label, fm, 110);
            }

            int lw = fm.stringWidth(label) + 10;
            int lh = 17;

            int lx = (int) rx - lw / 2;
            int ly = (int) ry + size / 2 + 4;

            g2.setColor(new Color(0, 0, 0, 175));
            g2.fillRoundRect(lx, ly, lw, lh, 10, 10);

            g2.setColor(Color.WHITE);
            g2.drawString(label, lx + 5, ly + 12);
        }

        private static String trimLabel(String text, FontMetrics fm, int maxWidth) {
            if (text == null) {
                return "";
            }

            String t = text;

            while (t.length() > 3 && fm.stringWidth(t + "...") > maxWidth) {
                t = t.substring(0, t.length() - 1);
            }

            return t + "...";
        }
    }
}