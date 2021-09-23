package flood;

import ucb.gui2.Pad;

import java.util.concurrent.ArrayBlockingQueue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.event.MouseEvent;
import static java.awt.RenderingHints.*;

/** A widget that displays a Signpost puzzle.
 *  @author P. N. Hilfinger
 */
class BoardWidget extends Pad {

    /* Parameters controlling sizes, speeds, colors, and fonts. */

    /** Colors of squares, arrows, and grid lines. */
    static final Color
        BACKGROUND_COLOR = new Color(220, 220, 220),
        GRID_LINE_COLOR = Color.black,
        MARK_COLOR = Color.black;

    /** Basic cell RGB color values (from Simon Tatham). */
    static final Color[] CELL_RGB = {
        Color.red,
        Color.yellow,
        Color.green,
        new Color(0x334cff),
        new Color(0xff8000),
        new Color(0x8000b3),
        new Color(0x804c4c),
        new Color(0x66ccff),
        new Color(0xff99ff),
        new Color(0xb3ffb3),
        new Color(0xffa07a)
    };

    /** Dimensions of features and spacing. */
    static final int
        CELL_SIDE = 35,
        GRID_LINE_WIDTH = 2,
        PADDING = CELL_SIDE / 2,
        OFFSET = 2,
        MARK_RAD = CELL_SIDE / 3,
        MARK_OFFSET = (CELL_SIDE - MARK_RAD) / 2;

    /** Strokes for ordinary grid lines and those that are parts of
     *  boundaries. */
    static final BasicStroke
        GRIDLINE_STROKE = new BasicStroke(GRID_LINE_WIDTH);

    /** A graphical representation of a Signpost board that sends commands
     *  derived from mouse clicks to COMMANDS. */
    BoardWidget(ArrayBlockingQueue<String> commands) {
        _commands = commands;
        setMouseHandler("click", this::mouseClicked);
    }

    /** Draw the grid lines on G. */
    private void drawGrid(Graphics2D g) {
        g.setColor(GRID_LINE_COLOR);
        g.setStroke(GRIDLINE_STROKE);
        g.drawLine(cx(0), cy(0), cx(_width), cy(0));
        g.drawLine(cx(0), cy(0), cx(0), cy(_height));
        g.drawLine(cx(0), cy(_height), cx(_width), cy(_height));
        g.drawLine(cx(_width), cy(0), cx(_width), cy(_height));

        for (int row = 0; row < _height; row += 1) {
            for (int col = 0; col < _width; col += 1) {
                if (col + 1 < _width
                    && _model.get(row, col) != _model.get(row, col + 1)) {
                    g.drawLine(cx(col + 1), cy(row), cx(col + 1), cy(row + 1));
                }
                if (row + 1 < _height
                    && _model.get(row, col) != _model.get(row + 1, col)) {
                    g.drawLine(cx(col), cy(row + 1), cx(col + 1), cy(row + 1));
                }
            }
        }
    }

    /** Set the cell colors on G from the model. */
    private void drawCells(Graphics2D g) {
        for (int row = 0; row < _height; row += 1) {
            for (int col = 0; col < _width; col += 1) {
                g.setColor(CELL_RGB[_model.get(row, col)]);
                g.fillRect(cx(col), cy(row), CELL_SIDE, CELL_SIDE);
            }
        }
    }

    /** Mark each of the cells on G from the model. */
    private void drawMarks(Graphics2D g) {
        g.setColor(MARK_COLOR);
        for (Place p : _model.markedCells()) {
            g.fillOval(cx(p.col) + MARK_OFFSET, cy(p.row) + MARK_OFFSET,
                       MARK_RAD, MARK_RAD);
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, _boardWidth, _boardHeight);

        drawCells(g);
        drawGrid(g);
        drawMarks(g);
    }

    /** Handle mouse pressed event E, recording the starting square of a
     *  connection, if appropriate. */
    private synchronized void mouseClicked(String unused, MouseEvent e) {
        int x = x(e), y = y(e);
        if (e.getButton() != MouseEvent.BUTTON1 || !_model.isCell(y, x)) {
            return;
        }
        _commands.offer(String.format("SET %d %d", y, x));
    }

    /** Return the column index of the square on which EVENT occurred. */
    private int x(MouseEvent event) {
        return (int) Math.floorDiv(event.getX() - OFFSET, CELL_SIDE);
    }

    /** Return the row index of the square on which EVENT occurred. */
    private int y(MouseEvent event) {
        return (int) Math.floorDiv(event.getY() - OFFSET, CELL_SIDE);
    }

    /** Revise the displayed board according to MODEL. */
    void update(Model model) {
        _model = new Model(model);
        _width = model.width();
        _height = model.height();
        _boardWidth = _width * CELL_SIDE + 2 * OFFSET;
        _boardHeight = _height * CELL_SIDE + 2 * OFFSET;
        setPreferredSize(_boardWidth, _boardHeight);
        repaint();
    }

    /** Return pixel coordinates of the top of row ROW relative to window. */
    private int cy(int row) {
        return OFFSET + row * CELL_SIDE;
    }

    /** Return pixel coordinates of the left side of column COL relative
     *  to window. */
    private int cx(int col) {
        return OFFSET + col * CELL_SIDE;
    }

    /** Number of rows and of columns. */
    private int _height, _width;

    /** Queue on which to post commands (from mouse clicks). */
    private ArrayBlockingQueue<String> _commands;

    /** Current model being displayed. */
    private Model _model;
    /** Length (in pixels) of the side of the board. */
    private int _boardWidth, _boardHeight;
}
