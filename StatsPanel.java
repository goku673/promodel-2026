import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * StatsPanel - Tabla de estadísticas en tiempo real (panel inferior).
 */
public class StatsPanel extends JPanel {

    private SimState state;
    private final String[] COLS = {
        "Locación", "Cap.", "En Loc.", "En Cola", "Procesadas", "Utilización %"
    };
    private final DefaultTableModel model;
    private final JTable table;

    public StatsPanel() {
        setLayout(new BorderLayout());
        setBackground(SimConstants.BG_PANEL);
        setPreferredSize(new Dimension(0, 175));
        setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, SimConstants.C_ACCENT));

        JLabel hdr = new JLabel("  \u00f0\u009f\u0093\u008a  ESTADÍSTICAS EN TIEMPO REAL");
        hdr.setFont(SimConstants.FONT_LABEL);
        hdr.setForeground(SimConstants.C_ACCENT2);
        hdr.setOpaque(true);
        hdr.setBackground(SimConstants.BG_HEADER);
        hdr.setPreferredSize(new Dimension(0, 24));
        add(hdr, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(SimConstants.BG_PANEL);
        scroll.getViewport().setBackground(SimConstants.BG_PANEL);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        populateEmpty();
    }

    private void styleTable() {
        table.setBackground(SimConstants.BG_PANEL);
        table.setForeground(SimConstants.C_TEXT);
        table.setFont(SimConstants.FONT_SMALL);
        table.setRowHeight(22);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SimConstants.BG_CARD);
        table.setSelectionForeground(SimConstants.C_TEXT);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(SimConstants.BG_CARD);
        header.setForeground(SimConstants.C_ACCENT2);
        header.setFont(SimConstants.FONT_LABEL);
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SimConstants.C_ACCENT));

        int[] widths = {140, 50, 70, 70, 90, 200};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(5).setCellRenderer(new UtilRenderer());
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    public void setState(SimState s) {
        this.state = s;
        if (s == null) populateEmpty();
        else           refresh();
    }

    public void refresh() {
        if (state == null) return;
        model.setRowCount(0);
        for (Loc loc : state.locs.values()) {
            int cap = loc.cap == Integer.MAX_VALUE ? 999 : loc.cap;
            double util = loc.utilLive(state.clk, state.clk);
            model.addRow(new Object[]{
                loc.name, cap, loc.cnt, loc.waitingCount(), loc.processed, util
            });
        }
    }

    private void populateEmpty() {
        model.setRowCount(0);
    }

    // ── Renderer de barra de utilización ─────────────────────────────────
    static class UtilRenderer extends JPanel implements TableCellRenderer {
        private double value = 0;
        private final DecimalFormat df = new DecimalFormat("0.0");
        public UtilRenderer() { setOpaque(true); }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            value = val instanceof Double ? (Double) val : 0.0;
            Color bg = sel ? SimConstants.BG_CARD
                    : (row%2==0 ? Color.WHITE : new Color(245,245,245));
            setBackground(bg);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int W = getWidth()-8, H = getHeight()-6;
            int barW = (int)(W * value / 100.0);
            g2.setColor(new Color(40,40,60));
            g2.fillRoundRect(4,3,W,H,5,5);
            if (barW > 0) {
                Color c1,c2;
                if (value<50)      { c1=new Color(30,120,50);  c2=new Color(60,180,80); }
                else if (value<80) { c1=new Color(150,100,0);  c2=new Color(220,160,0); }
                else               { c1=new Color(160,30,30);  c2=new Color(230,60,60); }
                g2.setPaint(new GradientPaint(4,3,c1,4+barW,3,c2));
                g2.fillRoundRect(4,3,barW,H,5,5);
            }
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial",Font.BOLD,10));
            String txt = df.format(value)+"%";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(txt, 4+(W-fm.stringWidth(txt))/2, 3+H/2+4);
        }
    }

    // ── Renderer de filas alternas ────────────────────────────────────────
    static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t,val,sel,foc,row,col);
            setOpaque(true);
            setForeground(SimConstants.C_TEXT);
            setBackground(sel ? SimConstants.BG_CARD
                              : (row%2==0 ? Color.WHITE : new Color(245,245,245)));
            setBorder(BorderFactory.createEmptyBorder(0,6,0,4));
            return this;
        }
    }
}
