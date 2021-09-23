package flood;

import static flood.Utils.*;

/** A (row, col) coordinate pair.
 *  @author P. N. Hilfinger
 */
class Place implements Comparable<Place> {

    /** The coordinates row and col represented by this Place. */
    protected final int row, col;

    /** Represents the pair (R, C). */
    private Place(int r, int c) {
        row = r;
        col = c;
    }

    /** A factory method that returns the Place at (ROW, COL).  When called
     *  twice with the same arguments, produced the same object. */
    public static Place pl(int row, int col) {
        Place result;
        try {
            while (row >= _places.length || col >= _places[0].length) {
                Place[][] newPlaces =
                    new Place[2 * _places.length][2 * _places[0].length];
                for (int r = 0; r < _places.length; r += 1) {
                    for (int c = 0; c < _places[0].length; c += 1) {
                        newPlaces[r][c] = _places[r][c];
                    }
                }
                _places = newPlaces;
            }
            result = _places[row][col];
            if (result == null) {
                result = _places[row][col] = new Place(row, col);
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException excp) {
            throw badArgs("negative row or column: (%d, %d)", row, col);
        }
    }

    @Override
    public boolean equals(Object other) {
        Place o = (Place) other;
        return row == o.row && col == o.col;
    }

    @Override
    public int hashCode() {
        return row << 8 + col;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", row, col);
    }

    @Override
    public int compareTo(Place o) {
        int c;
        c = row - o.row;
        if (c == 0) {
            c = col - o.col;
        }
        return c;
    }

    /** The cache of unique instances of all Places used, indexed by row and
     *  column. */
    private static Place[][] _places = { { new Place(0, 0) } };

}
