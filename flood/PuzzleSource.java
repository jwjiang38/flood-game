package flood;

/** Describes a source of Flood puzzles.
 *  @author P. N. Hilfinger
 */
interface PuzzleSource {

    /** Returns a WIDTH x HEIGHT Model containing a puzzle with COLORS colors
     *  of squares.  Sets the move limit of the result to a conservative
     *  (i.e., possibly high) heuristic estimate of the minimum number of
     *  moves required to solve the puzzle plus EXTRA. Requires that
     *  WIDTH x HEIGHT > COLORS > 2. */
    Model getPuzzle(int width, int height, int colors, int extra);

    /** Reseed the random number generator with SEED. */
    void setSeed(long seed);

}
