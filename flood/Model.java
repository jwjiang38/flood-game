package flood;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static flood.Place.pl;
import static flood.Utils.*;

/** The state of a Flood puzzle.  The cells have 0-based column and row numbers,
 *  and each contains a non-negative number indicating a color.  Methods allow
 *  querying the colors of the cells, setting the color of the active region,
 *  determining if the puzzle is solved, setting and checking the move
 *  limit, and undoing or redoing moves.  It is possible to "mark" selected
 *  cells, and check which cells are currently marked.
 *
 *  The rows and columns of the model may be accessed either by row and column
 *  index or using Places (which represent a row and column index).  Row 0
 *  is intended to represent the top row when the board is displayed.
 *
 *  @author Jingwei Jiang
 */
class Model {

    /** Limit on number of colors that may be used in a puzzle. */
    static final int MAX_COLORS = 10;

    /** A Model whose initial state is given by INITIAL with NCOLORS>2 colors.
     *  INITIAL[r][c] is the color row r and column c.
     *  Does not modify INITIAL, nor does the resulting Model share structure
     *  with it.
     */
    Model(int[][] initial, int ncolors) {
        if (initial.length == 0 || initial[0].length == 0) {
            throw badArgs("empty model");
        }
        if (ncolors < 3 || ncolors > MAX_COLORS) {
            badArgs("# colors must be 3 .. %d.", MAX_COLORS);
        }
        _height = initial.length;
        _width = initial[0].length;
        _ncolors = ncolors;
        _current = _lastHistory = 0;
        _limit = 0;
        _cells = new int[_height][_width];
        deepCopy(initial, _cells);
        _active = sameColorRegion(0, 0);
        _history.add(new GameState());
        _history.get(0).saveState();
    }

    /** Initializes a copy of MODEL, not including its redo history. Does not
     *  modify or share structure with MODEL.  */
    Model(Model model) {
        _width = model.width(); _height = model.height();
        _ncolors = model._ncolors;
        _current = _lastHistory = 0;
        _limit = model._limit - model._current;
        _active = new HashSet<>(model.sameColorRegion(0, 0));
        _cells = deepCopyOf(model._cells);
        _marks.addAll(model._marks);
        _history.add(new GameState());
        _history.get(0).saveState();
    }

    /** Returns the width (number of columns of cells) of the board. */
    final int width() {
        return _width;
    }

    /** Returns the height (number of rows of cells) of the board. */
    final int height() {
        return _height;
    }

    /** Returns number of colors in puzzle. */
    final int ncolors() {
        return _ncolors;
    }

    /** Return the number of moves since the initial board (not including
     *  undone moves). */
    int numMoves() {
        return _current;
    }

    /** Return the current move limit. Initially 0 until changed by setLimit. */
    int limit() {
        return _limit;
    }

    /** Set the move limitAssumes that markedCells() is empty.  to LIMIT>0. */
    void setLimit(int limit) {
        if (limit < 0) {
            badArgs("limit must be non-negative");
        }
        _limit = limit;
    }

    /** Returns true iff (ROW, COL) is a valid cell location. */
    final boolean isCell(int row, int col) {
        return 0 <= col && col < width() && 0 <= row && row < height();
    }

    /** Apply ACTION.accept method to all four orthogonal neighbors of PLACE. */
    void forNeighbors(Place place, Consumer<Place> action) {
        if (isCell(place.row + 1, place.col)) {
            action.accept(pl(place.row + 1, place.col));
        }
        if (isCell(place.row - 1, place.col)) {
            action.accept(pl(place.row - 1, place.col));
        }
        if (isCell(place.row,   place.col - 1)) {
            action.accept(pl(place.row,  place.col - 1));
        }
        if (isCell(place.row, place.col + 1)) {
            action.accept(pl(place.row,  place.col + 1));
        }
    }

    /** Return to initial board, removing any marks. */
    void restart() {
        _history.get(0).restoreState();
        _current = _lastHistory = 0;
        clearMarks();
    }

    /** Undo one move, if possible, removing any marks.  Does nothing if
     *  at the initial board. */
    void undo() {
        if (_current > 0) {
            _current -= 1;
            _history.get(_current).restoreState();
            clearMarks();
        }
    }

    /** Redo one move, if possible, removing any marks. Does nothing if
     *  there are no available undone boards. */
    void redo() {
        if (_current < _lastHistory) {
            _current += 1;
            _history.get(_current).restoreState();
            clearMarks();
        }
    }

    /** Returns an array with ncolors() elements, such that colors[c] iff
     *  there is a cell with color c present on the board. */
    boolean[] colorsPresent() {
        boolean[] color = new boolean[_ncolors];
        for (int i = 0; i < _height; i++) {
            for (int j = 0; j < _width; j++) {
                color[_cells[i][j]] = true;
            }
        }
        return color;
    }

    /** Returns the size of the active region. */
    int activeRegionSize() {
        return _active.size();
    }

    /** Returns true iff the puzzle is solved. */
    final boolean solved() {
        if (_active.size() == _width * _height) {
            return true;
        }
        return false;
    }

    /** Returns true if this puzzle round is over: the puzzle is solved or
     *  the move limit has been reached. */
    boolean roundOver() {
        if (_current == _limit | _active.size() == _width * _height) {
            return true;
        }
        return false;
    }

    /** Returns the contents of location (ROW, COL). */
    final int get(int row, int col) {
        return _cells[row][col];
    }

    /** Return the contents of location (row, col), where PLACE represents
     *  those coordinates. */
    final int get(Place place) {
        return _cells[place.row][place.col];
    }

    /** Returns a list of the unique coordinate pairs for all cells
     *  having the same color as that at (ROW, COL) and reachable from it by a
     *  sequence of cells of that color that are each adjacent to the next
     *  horziontally or vertically. */
    HashSet<Place> sameColorRegion(int row, int col) {
        return findRegion(pl(row, col), get(row, col), new HashSet<>());
    }

    /** Returns sameColorRegion(PLACE.row, PLACE.col). */
    HashSet<Place> sameColorRegion(Place place) {
        return findRegion(place, get(place), new HashSet<>());
    }

    /** Add to RESULT the coordinates of all cells whose color is COLOR that are
     *  reachable by an orthogonal path from START without traversing cells
     *  in RESULT. Returns RESULT. */
    HashSet<Place> findRegion(Place start, int color, HashSet<Place> result) {
        result.add(start);
        boolean a = isCell(start.row - 1, start.col);
        if (isCell(start.row - 1, start.col)) {
            if ((!result.contains(pl(start.row - 1, start.col)))
                    && (_cells[start.row - 1][start.col] == color)) {
                findRegion(pl(start.row - 1, start.col), color, result);
            }
        }
        if (isCell(start.row + 1, start.col)
                && (!result.contains(pl(start.row + 1, start.col))
                && _cells[start.row + 1][start.col] == color)) {
            findRegion(pl(start.row + 1, start.col), color, result);
        }
        if (isCell(start.row, start.col - 1)
                && (!result.contains(pl(start.row, start.col - 1))
                && _cells[start.row][start.col - 1] == color)) {
            findRegion(pl(start.row, start.col - 1), color, result);
        }
        if (isCell(start.row, start.col + 1)
                && (!result.contains(pl(start.row, start.col + 1))
                && _cells[start.row][start.col + 1] == color)) {
            findRegion(pl(start.row, start.col + 1), color, result);
        }
        return result;
    }

    /** Set all cells in the active region to have color NEWCOLOR. Records
     *  this change to allow undoing it, updates the active region, and
     *  clears all marks. */
    void setActiveRegionColor(int newColor) {
        _current += 1;
        for (Place place : _active) {
            _cells[place.row][place.col] = newColor;
        }
        _active = sameColorRegion(0,  0);
        if (_current < _history.size()) {
            GameState a = _history.get(_current);
            a.saveState();
            _history.set(_current, a);
        } else {
            _history.add(new GameState());
            _history.get(_history.size() - 1).saveState();
        }
        _lastHistory = _current;

    }

    /** Return the set of cells that are adjacent to the active region and
     *  have color COLOR.  The contents of markedCells() is undefined
     *  after this. */
    Set<Place> adjacentCells(int color) {
        HashSet<Place> adj = new HashSet<>();
        for (Place pos : _active) {
            if (isCell(pos.row - 1, pos.col)) {
                if (!_active.contains(pl(pos.row - 1, pos.col))
                        && _cells[pos.row - 1][pos.col] == color) {
                    HashSet<Place> newadd
                            = sameColorRegion(pos.row - 1, pos.col);
                    for (Place added : newadd) {
                        if (!_active.contains(added)) {
                            adj.add(added);
                        }
                    }
                }
            }
            if (isCell(pos.row + 1, pos.col)) {
                if (!_active.contains(pl(pos.row + 1, pos.col))
                        && _cells[pos.row + 1][pos.col] == color) {
                    HashSet<Place> newadd
                            = sameColorRegion(pos.row + 1, pos.col);
                    for (Place added : newadd) {
                        if (!_active.contains(added)) {
                            adj.add(added);
                        }
                    }
                }
            }
            if (isCell(pos.row, pos.col - 1)) {
                if (!_active.contains(pl(pos.row, pos.col - 1))
                        && _cells[pos.row][pos.col - 1] == color) {
                    HashSet<Place> newadd
                            = sameColorRegion(pos.row, pos.col - 1);
                    for (Place added : newadd) {
                        if (!_active.contains(added)) {
                            adj.add(added);
                        }
                    }
                }
            }
            if (isCell(pos.row, pos.col + 1)) {
                if (!_active.contains(pl(pos.row, pos.col + 1))
                        && _cells[pos.row][pos.col + 1] == color) {
                    HashSet<Place> newadd
                            = sameColorRegion(pos.row, pos.col + 1);
                    for (Place added : newadd) {
                        if (!_active.contains(added)) {
                            adj.add(added);
                        }
                    }
                }
            }
        }
        _marks = adj;
        return adj;
    }

    /** Set markedCells() from the contents of MARKS. */
    void setMarks(Collection<Place> marks) {
        _marks.clear();
        _marks.addAll(marks);
    }

    /** Clear the set of marked cells. */
    void clearMarks() {
        _marks.clear();
    }

    /** Return the current list of marked cells. */
    Set<Place> markedCells() {
        return _readOnlyMarks;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        Set<Place> marked = markedCells();
        String sep;
        for (int row = 0; row < height(); row += 1) {
            for (int col = 0; col < width(); col += 1) {
                sep = "";
                if (marked.contains(pl(row, col))) {
                    out.format("%s*%d", sep, get(row, col));
                } else {
                    out.format("%s%2d", sep, get(row, col));
                }
                sep = " ";
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Dimensions of board. */
    private int _width, _height;

    /** Number of colors. */
    private int _ncolors;

    /** The current board contents. */
    private int[][] _cells;

    /** The current active region. */
    private HashSet<Place> _active;

    /** Move limit. */
    private int _limit;

    /** Represents enough of the state of a game to allow undoing and
     *  redoing of moves. */
    private class GameState {

        /** A holder for the _cells and _active instance variables of this
         *  Model. */
        GameState() {
            _savedCells = new int[_height][_width];
            _savedActiveRegion = new HashSet<>();
        }

        /** Initialize to the current state of the Model. */
        void saveState() {
            deepCopy(_cells, _savedCells);
            _savedActiveRegion.clear();
            _savedActiveRegion.addAll(_active);
        }

        /** Restore the current Model's state from our saved state. */
        void restoreState() {
            deepCopy(_savedCells, _cells);
            _active.clear();
            _active.addAll(_savedActiveRegion);
        }

        /** Contents of board. */
        private int[][] _savedCells;
        /** Contents of active region (redundant, but included to avoid
         *  recomputation). */
        private HashSet<Place> _savedActiveRegion;
    }

    /** The set of marked cells. */
    private HashSet<Place> _marks = new HashSet<>();

    /** A read-only view of _marks. */
    private Set<Place> _readOnlyMarks = Collections.unmodifiableSet(_marks);

    /** A sequence of puzzle states.  The initial puzzle is at index 0.
     *  _history[_current] is equal to the current puzzle state.
     *  _history[_current+1] through _history[_lastHistory] are undone
     *  states that can be redone.  _lastHistory is reset to _current after
     *  each move.  _history only expands: there can be more than
     *  _lastHistory+1 elements in it at any time, with those following
     *  _lastHistory being available for re-use.  This is basically an
     *  optimization to avoid constant allocation and deallocation of
     *  arrays. */
    private ArrayList<GameState> _history = new ArrayList<>();

    /** The position of the current state in _history.  This is always
     *  non-negative and <=_lastHistory.  */
    private int _current;

    /** The index of the last valid state in _history, including those
     *  that can be redone (with indices >_current). */
    private int _lastHistory;

}


