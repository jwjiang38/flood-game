package flood;

import java.util.Random;

import static flood.Utils.*;

/** A creator of random Flood puzzles.
 *  @author Jingwei Jiang
 */
class PuzzleGenerator implements PuzzleSource {

    /** A new PuzzleGenerator whose random-number source is seeded
     *  with SEED. */
    PuzzleGenerator(long seed) {
        _random = new Random(seed);
    }

    @Override
    public Model getPuzzle(int width, int height, int ncolors, int extra) {
        int [][] init = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                init[i][j] = _random.nextInt(ncolors);
            }
        }
        Model newmodel = new Model(init, ncolors);

        newmodel.setLimit(Solver.movesNeeded(newmodel) + extra);
        return newmodel;
    }


    @Override
    public void setSeed(long seed) {
        _random.setSeed(seed);
    }

    /** My PNRG. */
    private Random _random;

}
