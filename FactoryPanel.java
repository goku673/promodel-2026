import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * FactoryPanel - Lienzo interactivo (Layout Editor)
 * Permite arrastrar locaciones, redimensionarlas y asignarles imágenes.
 */
public class FactoryPanel extends JPanel {

    private SimState state;
    private SpriteLoc dragged = null;
    private SpriteLoc selected = null; // Sprite actualmente seleccionado
    
    private int dragOffsetX, dragOffsetY;
    private boolean resizing = false;
    private final int HANDLE_SIZE = 10;
    
    // Representación gráfica de una locación
    class SpriteLoc {
        Loc l;
        Image img;
        public SpriteLoc(Loc l) { this.l = l; }
    }
    
    private final Map<String, SpriteLoc> sprites = new LinkedHashMap<>();

    public FactoryPanel() {
        setBackground(SimConstants.BG_DARK);
        setPreferredSize(new Dimension(1080, 490));
        
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == null) return;
                
                // Si la simulación está corriendo, bloquear edición
                if (state.running) {
                    selected = null;
                    if (GraphicsDialog.instance != null) {
                        GraphicsDialog.instance.setSelectedLoc(null);
                    }
                    repaint();
                    return;
                }
                
                // 1. Revisar si hicimos clic en el tirador de redimensionamiento del elemento seleccionado
                if (selected != null) {
                    Loc l = selected.l;
                    if (e.getX() >= l.x + l.w - HANDLE_SIZE && e.getX() <= l.x + l.w + HANDLE_SIZE &&
                        e.getY() >= l.y + l.h - HANDLE_SIZE && e.getY() <= l.y + l.h + HANDLE_SIZE) {
                        resizing = true;
                        dragged = selected;
                        return; // Empezamos a redimensionar
                    }
                }
                
                // 2. Buscar si hicimos clic en algún sprite
                SpriteLoc clicked = null;
                java.util.List<SpriteLoc> list = new ArrayList<>(sprites.values());
                Collections.reverse(list);
                
                for (SpriteLoc s : list) {
                    if (e.getX() >= s.l.x && e.getX() <= s.l.x + s.l.w &&
                        e.getY() >= s.l.y && e.getY() <= s.l.y + s.l.h) {
                        clicked = s;
                        break;
                    }
                }
                
                selected = clicked; // Actualizar selección
                
                if (GraphicsDialog.instance != null) {
                    GraphicsDialog.instance.setSelectedLoc(clicked != null ? clicked.l : null);
                }
                
                if (clicked == null) {
                    repaint();
                    return;
                }
                
                if (SwingUtilities.isLeftMouseButton(e)) {
                    // Si hay un icono seleccionado en Gráficas, aplicarlo
                    if (GraphicsDialog.selectedItem != null) {
                        clicked.img = GraphicsDialog.selectedItem.img;
                        clicked.l.iconPath = GraphicsDialog.selectedItem.path;
                        
                        // Guardar icono en currentData
                        if (state.currentData != null) {
                            for (ProModelData.LocDef d : state.currentData.locations) {
                                if (d.name.equals(clicked.l.name)) {
                                    d.iconPath = clicked.l.iconPath;
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Preparar arrastre
                    resizing = false;
                    dragged = clicked;
                    dragOffsetX = e.getX() - clicked.l.x;
                    dragOffsetY = e.getY() - clicked.l.y;
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragged = null;
                resizing = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragged != null && SwingUtilities.isLeftMouseButton(e) && !state.running) {
                    if (resizing) {
                        // Redimensionar limitando a un tamaño mínimo
                        int newW = e.getX() - dragged.l.x;
                        int newH = e.getY() - dragged.l.y;
                        dragged.l.w = Math.max(20, newW);
                        dragged.l.h = Math.max(20, newH);
                    } else {
                        // Mover
                        dragged.l.x = e.getX() - dragOffsetX;
                        dragged.l.y = e.getY() - dragOffsetY;
                    }
                    
                    // Guardar coordenadas en currentData
                    if (state.currentData != null) {
                        for (ProModelData.LocDef d : state.currentData.locations) {
                            if (d.name.equals(dragged.l.name)) {
                                d.x = dragged.l.x;
                                d.y = dragged.l.y;
                                d.w = dragged.l.w;
                                d.h = dragged.l.h;
                                break;
                            }
                        }
                    }
                    
                    repaint();
                }
            }
        };
        
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setState(SimState s) {
        this.state = s;
        sprites.clear();
        selected = null;
        if (s != null) {
            for (Loc loc : s.locs.values()) {
                SpriteLoc sl = new SpriteLoc(loc);
                if (loc.iconPath != null) {
                    try {
                        sl.img = new ImageIcon(loc.iconPath).getImage();
                    } catch (Exception e) {}
                }
                sprites.put(loc.name, sl);
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();
        g2.setPaint(new GradientPaint(0, 0, SimConstants.BG_DARK, W, H, SimConstants.BG_PANEL));
        g2.fillRect(0, 0, W, H);
        drawGrid(g2, W, H);

        if (state == null) {
            g2.setFont(new Font("Arial", Font.BOLD, 18));
            g2.setColor(SimConstants.C_MUTED);
            String msg = "Lienzo vacío. Utiliza 'Importar Modelo' para cargar elementos.";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);
            return;
        }

        // Dibujar flechas de ruteo
        if (state != null && state.routes != null) {
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.5f));
            for (ProModelData.ProcDef proc : state.routes) {
                if (proc.destination == null || proc.destination.trim().equalsIgnoreCase("Exit")) continue;
                
                java.util.List<SpriteLoc> froms = findSprites(proc.location);
                java.util.List<SpriteLoc> tos = findSprites(proc.destination);
                
                for (SpriteLoc from : froms) {
                    for (SpriteLoc to : tos) {
                        if (from != to) {
                            drawArrow(g2, 
                                from.l.x + from.l.w / 2, from.l.y + from.l.h / 2, 
                                to.l.x + to.l.w / 2, to.l.y + to.l.h / 2,
                                to.l.w, to.l.h);
                        }
                    }
                }
            }
        }

        // Dibujar todas las locaciones
        for (SpriteLoc s : sprites.values()) {
            drawSprite(g2, s);
        }
        
        // Dibujar entidades (Animación e interpolación)
        if (state != null && state.activeEntities != null) {
            synchronized (state.activeEntities) {
                for (Entity e : state.activeEntities) {
                    float ex = e.curX;
                    float ey = e.curY;
                    
                    if (e.moving && e.moveEndTime > e.moveStartTime) {
                        double p = (state.clk - e.moveStartTime) / (e.moveEndTime - e.moveStartTime);
                        if (p < 0) p = 0;
                        if (p > 1) p = 1;
                        ex = e.curX + (float) (p * (e.targetX - e.curX));
                        ey = e.curY + (float) (p * (e.targetY - e.curY));
                    }
                    
                    int size = 30; // Tamaño de la pieza
                    if (e.iconPath != null && !e.iconPath.isEmpty()) {
                        Image img = new ImageIcon(e.iconPath).getImage();
                        g2.drawImage(img, (int)ex - size/2, (int)ey - size/2, size, size, this);
                    } else {
                        g2.setColor(Color.RED);
                        g2.fillOval((int)ex - size/2, (int)ey - size/2, size, size);
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawOval((int)ex - size/2, (int)ey - size/2, size, size);
                    }
                }
            }
        }
    }

    private void drawGrid(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(0.5f));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int cx2, int cy2, int w2, int h2) {
        double dx = cx2 - x1;
        double dy = cy2 - y1;
        if (dx == 0 && dy == 0) return;
        
        // Calcular intersección con el borde del rectángulo destino
        double sx = (dx != 0) ? Math.abs((w2 / 2.0 + 4) / dx) : Double.MAX_VALUE;
        double sy = (dy != 0) ? Math.abs((h2 / 2.0 + 4) / dy) : Double.MAX_VALUE;
        double s = Math.min(sx, sy);
        
        // Si la distancia es muy corta, no dibujar
        if (s >= 1.0) return; 
        
        int x2 = (int) (cx2 - s * dx);
        int y2 = (int) (cy2 - s * dy);
        
        // Calcular intersección origen para que la línea no atraviese el centro
        // opcional: pero como se dibuja debajo, no importa el origen
        
        g2.drawLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int arrowSize = 12;
        int x3 = (int) (x2 - arrowSize * Math.cos(angle - Math.PI / 7));
        int y3 = (int) (y2 - arrowSize * Math.sin(angle - Math.PI / 7));
        int x4 = (int) (x2 - arrowSize * Math.cos(angle + Math.PI / 7));
        int y4 = (int) (y2 - arrowSize * Math.sin(angle + Math.PI / 7));
        g2.fillPolygon(new int[]{x2, x3, x4}, new int[]{y2, y3, y4}, 3);
    }

    private java.util.List<SpriteLoc> findSprites(String name) {
        java.util.List<SpriteLoc> res = new ArrayList<>();
        if (sprites.containsKey(name)) {
            res.add(sprites.get(name));
        } else {
            for (String k : sprites.keySet()) {
                if (k.startsWith(name + ".")) {
                    res.add(sprites.get(k));
                }
            }
        }
        return res;
    }

    private void drawSprite(Graphics2D g2, SpriteLoc s) {
        Loc loc = s.l;
        int x = loc.x, y = loc.y, w = loc.w, h = loc.h;

        // 1. Dibujar Imagen o Recuadro por defecto
        if (s.img != null) {
            g2.drawImage(s.img, x, y, w, h, this);
        } else {
            g2.setColor(new Color(230, 230, 230));
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.setColor(SimConstants.C_BORDER);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(x, y, w, h, 8, 8);
            
            // X central
            g2.setColor(Color.RED);
            g2.drawLine(x + w/2 - 10, y + h/2 - 10, x + w/2 + 10, y + h/2 + 10);
            g2.drawLine(x + w/2 + 10, y + h/2 - 10, x + w/2 - 10, y + h/2 + 10);
        }

        // 2. Nombre de la locación centrado debajo
        g2.setFont(SimConstants.FONT_SMALL);
        g2.setColor(SimConstants.C_TEXT);
        String nm = loc.name.replace("_", " ");
        if (nm.matches(".*\\.\\d+$")) {
            nm = nm.substring(0, nm.lastIndexOf('.'));
        }
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(nm, x + (w - fm.stringWidth(nm)) / 2, y + h + 14);

        // 3. Marco y tiradores de selección
        if (s == selected && state != null && !state.running) {
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[]{5f, 5f}, 0.0f));
            g2.drawRect(x - 2, y - 2, w + 4, h + 4);
            
            // Tirador inferior derecho para redimensionar
            g2.setColor(Color.WHITE);
            g2.fillRect(x + w - HANDLE_SIZE/2, y + h - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE);
            g2.setColor(Color.BLUE);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRect(x + w - HANDLE_SIZE/2, y + h - HANDLE_SIZE/2, HANDLE_SIZE, HANDLE_SIZE);
        }

        // 4. Medidor (Gauge)
        if (loc.showGauge && loc.cap > 0 && loc.cap < Integer.MAX_VALUE) {
            int gw = w - 4;
            int gh = 8;
            int gx = x + 2;
            int gy = y + h - gh - 2;
            
            g2.setColor(new Color(30, 30, 30, 200));
            g2.fillRect(gx, gy, gw, gh);
            
            g2.setColor(new Color(40, 220, 80));
            int fillW = (int) (gw * ((double) loc.cnt / loc.cap));
            if (fillW > gw) fillW = gw;
            g2.fillRect(gx, gy, fillW, gh);
            
            g2.setColor(Color.BLACK);
            g2.drawRect(gx, gy, gw, gh);
        }

        // 5. Contador
        if (loc.showCounter) {
            String valStr = "0";
            if ("Entradas Totales".equals(loc.counterType)) valStr = String.valueOf(loc.totalEntries);
            else if ("Salidas".equals(loc.counterType)) valStr = String.valueOf(loc.processed);
            else valStr = String.valueOf(loc.cnt);
            
            g2.setFont(new Font("Monospaced", Font.BOLD, 14));
            FontMetrics fm2 = g2.getFontMetrics();
            int cw = fm2.stringWidth(valStr) + 12;
            int ch = fm2.getHeight() + 6;
            int cx = x + w - cw;
            int cy = y;
            
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(cx, cy, cw, ch);
            g2.setColor(Color.WHITE);
            g2.drawString(valStr, cx + 6, cy + fm2.getAscent() + 3);
            g2.setColor(new Color(150, 150, 150));
            g2.drawRect(cx, cy, cw, ch);
        }
    }
}
