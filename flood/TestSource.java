package flood;

import java.util.Scanner;
import java.util.NoSuchElementException;

import static flood.Utils.*;

/** A type of InputSource that receives commands from a Scanner. This kind
 *  of source is intended for testing.
 *  @author P. N. Hilfinger
 */
class TestSource implements CommandSource, PuzzleSource {

    /** Provides commands and puzzles from SOURCE.  */
    TestSource(Scanner source) {
        source.useDelimiter("[ \t\n\r(,)]+");
        _source = source;
    }

    /** Returns a command string read from my source. At EOF, returns QUIT.
     *  Allows comment lines starting with "#", which are discarded. */
    @Override
    public String getCommand() {
        while (_source.hasNext()) {
            String line = _source.nextLine().trim().toUpperCase();
            if (!line.startsWith("#")) {
                return line;
            }
        }
        return "QUIT";
    }

    /** Create a model initialized to a puzzle.  Throws IllegalStateException
     *  if there is no valid puzzle available, or if the provided puzzle does
     *  not have dimensions WIDTH x HEIGHT with COLORS colors. EXTRA is ignored.
     *  */
    @Override
    public Model getPuzzle(int width, int height, int colors, int extra) {
        try {
            while (_source.hasNext(".*#.*")) {
                _source.next();
                _source.nextLine();
            }

            if (_source.hasNext("AUTOPUZZLE")) {
                _source.next();
                return _randomPuzzler.getPuzzle(width, height, colors, extra);
            }

            _source.next("PUZZLE");
            int w = _source.nextInt(),
                h = _source.nextInt(),
                nc = _source.nextInt();

            if (w != width || h != height || nc != colors) {
                throw badArgs("wrong puzzle parameters");
            }
            int[][] board = new int[height][width];
            for (int row = 0; row < height; row += 1) {
                for (int col = 0; col < width; col += 1) {
                    board[row][col] = _source.nextInt();
                }
            }
            Model model = new Model(board, colors);
            model.setLimit(_source.nextInt());
            _source.next("ENDPUZZLE");
            return model;
        } catch (NoSuchElementException | IllegalArgumentException excp) {
            System.err.println(excp);
            throw new IllegalStateException("missing or malformed puzzle");
        }
    }

    @Override
    public void setSeed(long seed) {
        _randomPuzzler.setSeed(seed);
    }

    /** Input source. */
    private Scanner _source;
    /** Source for random puzzles. */
    private PuzzleGenerator _randomPuzzler = new PuzzleGenerator(0);
}
