package flood;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

/** Tests of the PuzzleGenerator class.
 *  @author P. N. Hilfinger
 */
public class PuzzleGeneratorTests {

    @Rule
    public Timeout methodTimeout = Timeout.seconds(10);

    @Test
    public void puzzleTest() {
        PuzzleGenerator gen = new PuzzleGenerator(10);
        Model model = gen.getPuzzle(5, 4, 6, 0);
        assertTrue("Wrong puzzle parameters",
                   model.width() == 5 && model.height() == 4
                   && model.ncolors() == 6);
        for (int row = 0; row < model.height(); row += 1) {
            for (int col = 0; col < model.width(); col += 1) {
                assertTrue("Bad color used in puzzle",
                           model.get(row, col) >= 0
                           && model.get(row, col) < model.ncolors());
            }
        }
    }

}
