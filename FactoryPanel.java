import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
<<<<<<< Updated upstream
 * FactoryPanel - Layout animado de la fábrica con partículas en movimiento.
 *
 * FIXES:
 *  - FlowParticle almacena sx/sy correctamente (no calculados on-the-fly)
 *  - Partículas se generan en TODAS las conexiones activas
 *  - Barras de capacidad visibles en TODOS los almacenes
 *  - Engranes giran cuando locación está activa
 *  - Reloj muestra HR:MM en el panel
=======
 * FactoryPanel - Lienzo interactivo (Layout Editor)
 * Permite arrastrar locaciones, redimensionarlas, asignarles imágenes
 * y visualizar entidades/recursos durante la simulación.
>>>>>>> Stashed changes
 */
public class FactoryPanel extends JPanel {

    private SimState state;
<<<<<<< Updated upstream
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
=======
    private SpriteLoc dragged = null;
    private SpriteLoc selected = null;

    private int dragOffsetX, dragOffsetY;
    private boolean resizing = false;
    private final int HANDLE_SIZE = 10;

    /**
     * Representación gráfica de una locación.
     */
    class SpriteLoc {
        Loc l;
        Image img;

        public SpriteLoc(Loc l) {
            this.l = l;
        }
    }

    private final Map<String, SpriteLoc> sprites = new LinkedHashMap<>();
>>>>>>> Stashed changes

    /**
     * Caché de imágenes para evitar recargar PNG/JPG/GIF en cada frame.
     */
    private final Map<String, Image> imageCache = new HashMap<>();

    /**
     * Obtiene y cachea una imagen por ruta.
     */
    private Image getImage(String path) {
        if (path == null || path.trim().isEmpty()) return null;

        return imageCache.computeIfAbsent(path, p -> {
            try {
                return new ImageIcon(p).getImage();
            } catch (Exception e) {
                return null;
            }
        });
    }

    public FactoryPanel() {
        setBackground(SimConstants.BG_DARK);
        setPreferredSize(new Dimension(1080, 490));
<<<<<<< Updated upstream
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
=======

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (state == null) return;

                /*
                 * Si la simulación está corriendo, se bloquea la edición visual.
                 * Esto evita mover locaciones mientras las entidades/recursos están animándose.
                 */
                if (state.running) {
                    selected = null;

                    if (GraphicsDialog.instance != null) {
                        GraphicsDialog.instance.setSelectedLoc(null);
                    }

                    repaint();
                    return;
                }

                /*
                 * 1. Revisar si se hizo clic en el tirador de redimensionamiento
                 * de la locación seleccionada.
                 */
                if (selected != null) {
                    Loc l = selected.l;

                    boolean onResizeHandle =
                            e.getX() >= l.x + l.w - HANDLE_SIZE &&
                            e.getX() <= l.x + l.w + HANDLE_SIZE &&
                            e.getY() >= l.y + l.h - HANDLE_SIZE &&
                            e.getY() <= l.y + l.h + HANDLE_SIZE;

                    if (onResizeHandle) {
                        resizing = true;
                        dragged = selected;
                        return;
                    }
                }

                /*
                 * 2. Buscar si se hizo clic en alguna locación.
                 * Se revisa en orden inverso para seleccionar primero la que está encima.
                 */
                SpriteLoc clicked = null;

                java.util.List<SpriteLoc> list = new ArrayList<>(sprites.values());
                Collections.reverse(list);

                for (SpriteLoc s : list) {
                    if (e.getX() >= s.l.x &&
                            e.getX() <= s.l.x + s.l.w &&
                            e.getY() >= s.l.y &&
                            e.getY() <= s.l.y + s.l.h) {
                        clicked = s;
                        break;
                    }
                }

                selected = clicked;

                if (GraphicsDialog.instance != null) {
                    GraphicsDialog.instance.setSelectedLoc(clicked != null ? clicked.l : null);
                }

                if (clicked == null) {
                    repaint();
                    return;
                }

                if (SwingUtilities.isLeftMouseButton(e)) {
                    /*
                     * Si hay un icono seleccionado en GraphicsDialog,
                     * se asigna a la locación seleccionada.
                     */
                    if (GraphicsDialog.selectedItem != null) {
                        clicked.img = GraphicsDialog.selectedItem.img;
                        clicked.l.iconPath = GraphicsDialog.selectedItem.path;

                        /*
                         * Guardar icono en currentData para que quede asociado
                         * al modelo importado.
                         */
                        if (state.currentData != null) {
                            for (ProModelData.LocDef d : state.currentData.locations) {
                                if (d.name.equals(clicked.l.name)) {
                                    d.iconPath = clicked.l.iconPath;
                                    break;
                                }
                            }
                        }
                    }

                    /*
                     * Preparar arrastre.
                     */
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
                if (dragged != null &&
                        SwingUtilities.isLeftMouseButton(e) &&
                        state != null &&
                        !state.running) {

                    if (resizing) {
                        /*
                         * Redimensionar con tamaño mínimo.
                         */
                        int newW = e.getX() - dragged.l.x;
                        int newH = e.getY() - dragged.l.y;

                        dragged.l.w = Math.max(20, newW);
                        dragged.l.h = Math.max(20, newH);
                    } else {
                        /*
                         * Mover locación.
                         */
                        dragged.l.x = e.getX() - dragOffsetX;
                        dragged.l.y = e.getY() - dragOffsetY;
                    }

                    /*
                     * Guardar coordenadas en currentData.
                     */
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
>>>>>>> Stashed changes
    }

    /**
     * Recibe el estado actual de simulación y genera los sprites de locaciones.
     */
    public void setState(SimState s) {
        this.state = s;
<<<<<<< Updated upstream
        particles.clear();
=======

        sprites.clear();
        imageCache.clear();
        selected = null;
        dragged = null;
        resizing = false;

        if (s != null) {
            for (Loc loc : s.locs.values()) {
                SpriteLoc sl = new SpriteLoc(loc);

                if (loc.iconPath != null && !loc.iconPath.trim().isEmpty()) {
                    sl.img = getImage(loc.iconPath);
                }

                sprites.put(loc.name, sl);
            }
        }

>>>>>>> Stashed changes
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
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
<<<<<<< Updated upstream
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth(), H = getHeight();

        // Fondo degradado oscuro
        g2.setPaint(new GradientPaint(0, 0, new Color(10,10,28), W, H, new Color(18,18,42)));
=======

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        int W = getWidth();
        int H = getHeight();

        /*
         * Fondo tipo panel oscuro.
         */
        g2.setPaint(new GradientPaint(
                0,
                0,
                SimConstants.BG_DARK,
                W,
                H,
                SimConstants.BG_PANEL
        ));

>>>>>>> Stashed changes
        g2.fillRect(0, 0, W, H);

        drawGrid(g2, W, H);

<<<<<<< Updated upstream
        // Usar estado actual o estado vacío para mostrar layout estático
        SimState st = (state != null) ? state : new SimState(new SimParams());

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
=======
        if (state == null) {
            drawEmptyMessage(g2, W, H);
            return;
        }

        Rectangle clip = g2.getClipBounds();

        /*
         * Dibujar flechas de ruteo entre locaciones.
         */
        drawRoutes(g2);

        /*
         * Dibujar locaciones.
         */
        drawLocations(g2, clip);

        /*
         * Dibujar entidades.
         */
        drawEntities(g2);

        /*
         * Dibujar recursos móviles encima de las entidades.
         * Esto permite ver operadores/vehículos moverse como en ProModel.
         */
        drawResources(g2);
    }

    /**
     * Mensaje cuando todavía no hay modelo importado.
     */
    private void drawEmptyMessage(Graphics2D g2, int W, int H) {
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(SimConstants.C_MUTED);

        String msg = "Lienzo vacío. Utiliza 'Importar Modelo' para cargar elementos.";
        FontMetrics fm = g2.getFontMetrics();

        g2.drawString(
                msg,
                (W - fm.stringWidth(msg)) / 2,
                H / 2
        );
    }

    /**
     * Dibuja una cuadrícula de fondo.
     */
>>>>>>> Stashed changes
    private void drawGrid(Graphics2D g2, int W, int H) {
        g2.setColor(new Color(220, 220, 220));
        g2.setStroke(new BasicStroke(0.5f));

        for (int x = 0; x < W; x += 40) {
            g2.drawLine(x, 0, x, H);
        }

        for (int y = 0; y < H; y += 40) {
            g2.drawLine(0, y, W, y);
        }
    }

<<<<<<< Updated upstream
    // ── Conexiones / flechas ──────────────────────────────────────────────
    private void drawAllConnections(Graphics2D g2, SimState st) {
        for (String[] c : CONN) {
            Loc from = st.loc(c[0]); Loc to = st.loc(c[1]);
            if (from == null || to == null) continue;
            drawArrow(g2, from, to, c[2]);
        }
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
=======
    /**
     * Dibuja rutas/flechas usando la lista de procesamiento.
     */
    private void drawRoutes(Graphics2D g2) {
        if (state == null || state.routes == null) return;

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(1.5f));

        for (ProModelData.ProcDef proc : state.routes) {
            if (proc == null) continue;
            if (proc.destination == null) continue;
            if (proc.destination.trim().equalsIgnoreCase("Exit")) continue;

            java.util.List<SpriteLoc> froms = findSprites(proc.location);
            java.util.List<SpriteLoc> tos = findSprites(proc.destination);

            for (SpriteLoc from : froms) {
                for (SpriteLoc to : tos) {
                    if (from != null && to != null && from != to) {
                        Renderers.PathRenderer.drawArrow(
                                g2,
                                from.l.x + from.l.w / 2,
                                from.l.y + from.l.h / 2,
                                to.l.x + to.l.w / 2,
                                to.l.y + to.l.h / 2,
                                to.l.w,
                                to.l.h
                        );
                    }
                }
            }
        }
    }

    /**
     * Dibuja todas las locaciones visibles.
     */
    private void drawLocations(Graphics2D g2, Rectangle clip) {
        for (SpriteLoc s : sprites.values()) {
            if (s == null || s.l == null) continue;

            if (clip != null &&
                    !clip.intersects(new Rectangle(s.l.x, s.l.y, s.l.w, s.l.h))) {
                continue;
            }

            boolean isRunning = state != null && state.running;

            Renderers.LocationRenderer.draw(
                    g2,
                    s.l,
                    s.img,
                    s == selected,
                    isRunning,
                    this
            );
        }
    }

    /**
     * Dibuja entidades activas del sistema.
     */
    private void drawEntities(Graphics2D g2) {
        if (state == null || state.activeEntities == null) return;

        synchronized (state.activeEntities) {
            for (Entity e : state.activeEntities) {
                if (e == null) continue;

                Image entityImg = null;

                if (state.currentData != null) {
                    for (ProModelData.EntDef ed : state.currentData.entities) {
                        if (ed.name.equalsIgnoreCase(e.typeName)) {
                            String iconPath = ed.getIcon(e.currentGraphicId);

                            if (iconPath != null && !iconPath.trim().isEmpty()) {
                                entityImg = getImage(iconPath);
                            }

                            break;
                        }
                    }
                }

                Renderers.EntityRenderer.draw(
                        g2,
                        e,
                        state,
                        entityImg,
                        this
                );
            }
        }
    }

    /**
     * Dibuja recursos móviles.
     *
     * Los recursos deben venir desde currentData.resources.
     * Su posición se obtiene desde SimState.resAgents mediante:
     * - getAgentCurrentX(nombre)
     * - getAgentCurrentY(nombre)
     *
     * La imagen se asigna desde BuildDialog → Recursos.
     */
    private void drawResources(Graphics2D g2) {
        if (state == null || state.currentData == null) return;
        if (state.currentData.resources == null) return;

        for (ProModelData.ResDef r : state.currentData.resources) {
            if (r == null || r.name == null || r.name.trim().isEmpty()) {
                continue;
            }

            Image resourceImg = null;

            if (r.getIconPath() != null && !r.getIconPath().trim().isEmpty()) {
                resourceImg = getImage(r.getIconPath());
            }

            /*
             * Esta llamada requiere que Renderers.java tenga ResourceRenderer
             * con el método:
             *
             * draw(Graphics2D g2, String resourceName, SimState state, Image resourceImg, JPanel observer)
             */
            Renderers.ResourceRenderer.draw(
                    g2,
                    r.name,
                    state,
                    resourceImg,
                    this
            );
        }
    }

    /**
     * Busca sprites por nombre exacto o por subunidades.
     *
     * Ejemplo:
     * - Si busca MALTEADO y existe MALTEADO, devuelve MALTEADO.
     * - Si busca MALTEADO y existen MALTEADO.1, MALTEADO.2, devuelve esas subunidades.
     */
    private java.util.List<SpriteLoc> findSprites(String name) {
        java.util.List<SpriteLoc> res = new ArrayList<>();

        if (name == null || name.trim().isEmpty()) {
            return res;
        }

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
}
>>>>>>> Stashed changes
