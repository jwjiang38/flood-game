package flood;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** Tests of the PuzzleGenerator class.
 *  @author P. N. Hilfinger
 */
public class SolverTests {

    @Rule
    public Timeout methodTimeout = Timeout.seconds(10);

    @Test
    public void win1Test() {
        Model model = new Model(ONE_MOVE_BOARD, 6);
        assertEquals("Needs multiple moves", 1, Solver.movesNeeded(model));
        assertEquals("Wrong move chosen", 2, Solver.chooseBestMove(model));
    }

    @Test
    public void win2Test() {
        Model model = new Model(TWO_MOVE_BOARD, 6);
        assertEquals("Needs >3 moves", 3, Solver.movesNeeded(model));
        int best = Solver.chooseBestMove(model);
        assertTrue("Bad move chosen", 0 <= best && best <= 2);
    }

    @Test
    public void multistepTest() {
        int c;
        PuzzleSource src = new PuzzleGenerator(42);
        Model model = src.getPuzzle(10, 10, 6, 0);
        int lim = model.limit();
        for (c = 0; c < lim && !model.solved(); c += 1) {
            model.setActiveRegionColor(Solver.chooseBestMove(model));
        }
        assertTrue("Specified move count did not solve puzzle", model.solved());
        assertEquals("Wrong number of moves used", lim, model.numMoves());
        assertEquals("numMoves wrong", lim, c);
    }

    static final int[][] ONE_MOVE_BOARD = {
        { 1, 1, 1, 1 },
        { 1, 1, 2, 1 },
        { 2, 1, 1, 1 },
        { 2, 2, 1, 1 },
        { 1, 1, 1, 2 }
    };

    static final int[][] TWO_MOVE_BOARD = {
        { 3, 3, 3, 3 },
        { 2, 1, 1, 0 },
        { 2, 2, 0, 0 }
    };


}
