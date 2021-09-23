package flood;

import static flood.Utils.*;

/** The input/output and GUI controller for play of a Signpost puzzle.
 *  @author P. N. Hilfinger. */
public class Controller {

    /** The default number of squares on a side of the board. */
    static final int DEFAULT_SIZE = 12;

    /** The default number of colors on a board. */
    static final int DEFAULT_COLORS = 6;

    /** Controller for a game represented by MODEL, using COMMANDS as the
     *  the source of commands, and PUZZLES to supply puzzles.  If LOGGING,
     *  prints commands received on standard output.  If TESTING, prints
     *  the board when possibly changed.  If VIEW is non-null, update it
     *  at appropriate points when the model changes. */
    public Controller(View view,
                      CommandSource commands, PuzzleSource puzzles,
                      boolean logging, boolean testing) {
        _view = view;
        _commands = commands;
        _puzzles = puzzles;
        _logging = logging;
        _testing = testing;
        _active = true;
        setType(DEFAULT_SIZE, DEFAULT_SIZE, DEFAULT_COLORS, 4);
        _solving = false;
    }

    /** Return true iff we have not received a Quit command. */
    boolean active() {
        return _active;
    }

    /** Clear the board and play one puzzle, until receiving a quit,
     *  new-game, or parameter-change request.  Update the viewer with
     *  each visible modification to the model. */
    void playPuzzle() {
        _model = _puzzles.getPuzzle(_width, _height, _ncolors, _extra);
        _solving = false;

        logPuzzle();
        logBoard();
        while (_active) {
            if (_view != null) {
                _view.update(_model);
            }

            String cmnd = _commands.getCommand();
            if (_logging) {
                System.out.println(cmnd);
            }
            String[] parts = cmnd.split("\\s+");
            switch (parts[0]) {
            case "QUIT":
                _active = false;
                return;
            case "NEW":
                return;
            case "TYPE":
                setType(toInt(parts[1]), toInt(parts[2]), toInt(parts[3]),
                        toInt(parts[4]));
                return;
            case "SET":
                colorActiveRegion(toInt(parts[1]), toInt(parts[2]));
                break;
            case "SEED":
                _puzzles.setSeed(toLong(parts[1]));
                break;
            case "RESTART":
                restart();
                break;
            case "UNDO":
                undo();
                break;
            case "REDO":
                redo();
                break;
            case "SOLVE":
                solve();
                break;
            case "BOARD":
                logBoard();
                break;
            case "":
                break;
            default:
                System.err.printf("Bad command: '%s'%n", cmnd);
                break;
            }
        }
    }

    /** Restart current puzzle. */
    private void restart() {
        _model.restart();
        _solving = false;
        logBoard();
    }

    /** Set current puzzle board to show solution steps.  In this mode, there
     *  is a suggested color.  If the user chooses that color, there is
     *  another suggestion for the next move, and so on until the puzzle is
     *  complete.  We leave solution mode when the user chooses a move other
     *  than the suggestion, finishes the puzzle, or starts a new one. */
    private void solve() {
        if (!_model.roundOver()) {
            _solving = true;
            setHint();
        }
    }

    /** Set the color of the active region to that of grid cell (ROW, COL). */
    private void colorActiveRegion(int row, int col) {
        if (!_model.isCell(row, col)) {
            System.err.printf("Nonexistent cell: (%d, %d)%n", row, col);
        }
        if (_model.roundOver()) {
            return;
        }
        int color = _model.get(row, col);
        if (_solving && color != _hint) {
            _solving = false;
        }
        _model.clearMarks();
        _model.setActiveRegionColor(color);
        if (_solving) {
            setHint();
        }
    }

    /** Set current puzzle parameters to dimensions WIDTH x HEIGHT,
     *  NCOLORS shades of color, and allowance for EXTRA additional
     *  moves beyond those determined necessary by the solver. */
    private void setType(int width, int height, int ncolors, int extra) {
        if (width <= 2 || height <= 2 || ncolors > Model.MAX_COLORS
            || ncolors <= 2 || extra < 0) {
            throw badArgs("improper type parameters");
        }
        _width = width;
        _height = height;
        _ncolors = ncolors;
        _extra = extra;
    }

    /** Back up one move, if possible.  Does nothing otherwise. */
    private void undo() {
        _model.undo();
        _solving = false;
        logBoard();
    }

    /** Redo one move, if possible.  Does nothing otherwise. */
    private void redo() {
        _model.redo();
        _solving = false;
        logBoard();
    }

    /** Suggest and mark a next move. */
    private void setHint() {
        _hint = Solver.chooseBestMove(_model);
        _model.setMarks(_model.adjacentCells(_hint));
    }

    /** If testing, print the contents of the board. */
    private void logBoard() {
        if (_testing) {
            System.out.printf("B[ %dx%d %d %d/%d%n%s]%n",
                              _model.height(), _model.width(), _model.ncolors(),
                              _model.numMoves(), _model.limit(), _model);
        }
    }

    /** If logging, print a representation of the puzzle suitable for input
     *  from a TestSource. */
    private void logPuzzle() {
        if (_logging) {
            System.out.printf("PUZZLE%n%d %d %d%n%s%d%nENDPUZZLE%n",
                              _model.width(), _model.height(),
                              _model.ncolors(), _model, _model.limit());
        }
    }

    /** The board. */
    private Model _model;

    /** Our view of _model. */
    private View _view;

    /** Puzzle dimensions. */
    private int _width, _height;

    /** Number of colors used. */
    private int _ncolors;

    /** Input source from standard input. */
    private CommandSource _commands;

    /** Input source from standard input. */
    private PuzzleSource _puzzles;

    /** True until user quits. */
    private boolean _active;

    /** True if in "solving" mode. */
    private boolean _solving;

    /** Number of extra moves to give player beyond those found necessary by
     *  the solved. */
    private int _extra;

    /** Current color hint, if in solving mode. */
    private int _hint;

    /** True iff we are logging commands on standard output. */
    private boolean _logging;

    /** True iff we are testing the program and printing board contents. */
    private boolean _testing;

}
