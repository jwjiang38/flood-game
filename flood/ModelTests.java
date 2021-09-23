package flood;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static flood.Place.pl;
import static flood.Utils.*;

/** Tests of the Model class.
 *  @author P. N. Hilfinger
 */
public class ModelTests {

    @Rule
    public Timeout methodTimeout = Timeout.seconds(1);

    /** Check that the board represented by MODEL contains the colors
     *  given by BOARD. */
    static void checkContents(int[][] board, Model model) {
        assertEquals("Wrong width", board[0].length, model.width());
        assertEquals("Wrong height", board.length, model.height());
        for (int row = 0; row < board.length; row += 1) {
            for (int col = 0; col < board[0].length; col += 1) {
                assertEquals("Wrong contents",
                             board[row][col], model.get(row, col));
                assertEquals("Wrong contents by Place",
                             board[row][col], model.get(pl(row, col)));
            }
        }
    }

    @Test
    public void initTest1() {
        Model model = new Model(board1(), 4);
        checkContents(board1(), model);
        assertEquals("Wrong color count", 4, model.ncolors());
    }

    /** Make sure argument to Model constructor is not shared with the
     *  Model. */
    @Test
    public void initTest2() {
        int[][] init = board1();
        Model model = new Model(init, 4);
        model.setActiveRegionColor(1);
        assertTrue("Model shares structure with initialization array",
                   arrayEquals(board1(), init));
    }

    @Test
    public void copyTest() {
        Model model = new Model(board1(), 4);
        Model model2 = new Model(model);
        checkContents(board1(), model2);
        assertEquals("Wrong number of colors in copy", 4, model2.ncolors());
    }

    @Test
    public void limitTest1() {
        Model model = new Model(board1(), 4);
        assertEquals("Limit not initialized properly", 0, model.limit());
        model.setLimit(5);
        assertEquals("Limit not set properly", 5, model.limit());
    }

    @Test
    public void markTest() {
        Model model = new Model(board1(), 4);
        assertSetEquals("Marks not initialized properly",
                        Collections.emptySet(), model.markedCells());
        List<Place> marks =
            asList(pl(0, 1), pl(0, 2), pl(1, 2));
        model.setMarks(marks);
        assertSetEquals("Cells not marked properly",
                        marks, model.markedCells());

        List<Place> marks2 = asList(pl(0, 1), pl(0, 2));
        model.setMarks(marks2);
        assertSetEquals("Cells not marked properly, second call",
                        marks2, model.markedCells());
        model.clearMarks();
        assertSetEquals("Marks not cleared",
                        Collections.emptySet(), model.markedCells());
    }

    @Test
    public void solvedTest1() {
        assertFalse("Unsolved puzzle called solved",
                    new Model(board1(), 6).solved());
        assertTrue("Solved puzzle called unsolved",
                   new Model(solvedBoard(), 6).solved());
    }

    @Test
    public void colorsPresentTest() {
        Model model = new Model(board1(), 6);
        boolean[] present = model.colorsPresent();
        assertEquals("Must have ncolors() elements", 6, present.length);
        assertArrayEquals("Wrong colorss",
                          new boolean[] { true, true, true, true,
                                          false, false },
                          model.colorsPresent());
    }

    /** Check that the neighbors delivered by MODEL.forNeighbors(PLACE)
     *  are correct. */
    private void checkNeighbors(Model model, Place place,
                                List<Place> expected) {
        HashSet<Place> neighbors = new HashSet<>();
        model.forNeighbors(place, (p) -> {
            assertFalse("Duplicate neighbor", neighbors.contains(p));
            neighbors.add(p);
        });
        assertEquals(msg("Wrong neighbor set for %s", place),
                     new HashSet<Place>(expected), neighbors);
    }

    @Test
    public void forNeighborsTest() {
        Model model = new Model(new int[9][7], 4);
        checkNeighbors(model, pl(0, 0), asList(pl(1, 0), pl(0, 1)));
        checkNeighbors(model, pl(8, 0), asList(pl(8, 1), pl(7, 0)));
        checkNeighbors(model, pl(0, 1), asList(pl(0, 0), pl(0, 2), pl(1, 1)));
        checkNeighbors(model, pl(1, 0), asList(pl(0, 0), pl(2, 0), pl(1, 1)));
        checkNeighbors(model, pl(0, 6), asList(pl(0, 5), pl(1, 6)));
        checkNeighbors(model, pl(8, 6), asList(pl(7, 6), pl(8, 5)));
        checkNeighbors(model, pl(8, 1), asList(pl(7, 1), pl(8, 0), pl(8, 2)));
        checkNeighbors(model, pl(1, 6), asList(pl(1, 5), pl(0, 6), pl(2, 6)));
        checkNeighbors(model, pl(2, 3),
                       asList(pl(1, 3), pl(3, 3), pl(2, 2), pl(2, 4)));
    }

    @Test
    public void sameColorRegionTest() {
        Model model = new Model(board1(), 4);
        assertSetEquals("Wrong region found for (0, 0)",
                        asList(pl(0, 0), pl(1, 0)),
                        model.sameColorRegion(0, 0));
        assertSetEquals("Wrong region found for (0, 0)",
                        asList(pl(0, 0), pl(1, 0)),
                        model.sameColorRegion(pl(0, 0)));
        assertSetEquals("Wrong region found for (0, 1)",
                        asList(pl(0, 1), pl(0, 2), pl(1, 2)),
                        model.sameColorRegion(0, 1));
        assertSetEquals("Wrong region found for (4, 2)",
                        asList(pl(3, 1), pl(4, 1), pl(4, 2),
                               pl(4, 3), pl(3, 3)),
                        model.sameColorRegion(4, 2));
    }

    @Test
    public void findRegionTest() {
        Model model = new Model(board1(), 4);
        HashSet<Place> result = new HashSet<>();
        model.findRegion(pl(4, 2), 0, result);
        assertSetEquals("Failed to add to empty region starting from (4, 2)",
                        asList(pl(3, 1), pl(4, 1), pl(4, 2),
                               pl(4, 3), pl(3, 3)),
                        result);
        result.clear();
        result.add(pl(4, 3));
        model.findRegion(pl(4, 2), 0, result);
        assertSetEquals("Traversed existing region starting from (4, 2)",
                        asList(pl(3, 1), pl(4, 1), pl(4, 2),
                               pl(4, 3)),
                        result);
    }

    @Test
    public void setActiveRegionColorTest() {
        Model model = new Model(board1(), 4);
        assertEquals("Wrong initial active region size",
                     2, model.activeRegionSize());
        model.setActiveRegionColor(0);
        assertEquals("Wrong active region size after 0",
                     2, model.activeRegionSize());
        checkContents(BOARD1_0, model);
        model.setActiveRegionColor(3);
        assertEquals("Wrong active region size after 0, 3",
                     5, model.activeRegionSize());
        checkContents(BOARD1_03, model);
    }

    @Test
    public void adjacentCellsTest() {
        Model model = new Model(board1(), 4);
        assertSetEquals("Wrong adjacenies for color 3",
                        asList(pl(0, 1), pl(0, 2), pl(1, 2)),
                        model.adjacentCells(3));
        assertSetEquals("Wrong adjacenies for color 1",
                        asList(pl(1, 1), pl(2, 0), pl(3, 0)),
                        model.adjacentCells(1));
        assertSetEquals("Wrong adjacencies for color 0",
                        Collections.emptyList(), model.adjacentCells(0));
    }

    @Test
    public void undoRedoTest() {
        Model model = new Model(board1(), 4);
        assertEquals("Should be 0 moves at start", 0, model.numMoves());
        model.setActiveRegionColor(3);
        assertEquals("Should be 1 move", 1, model.numMoves());
        model.setActiveRegionColor(2);
        checkContents(BOARD1_32, model);
        assertEquals("Should be 2 moves", 2, model.numMoves());
        model.undo();
        assertEquals("Should be 1 move after undo", 1, model.numMoves());
        model.undo();
        assertEquals("Should be 0 moves after 2 undo's", 0, model.numMoves());
        checkContents(board1(), model);
        model.redo();
        assertEquals("Should be 1 move after 2 undo's and a redo",
                     1, model.numMoves());
        checkContents(BOARD1_03, model);
        model.redo();
        assertEquals("Should be 2 moves after 2 undo's and two 2 redo's",
                     2, model.numMoves());
        checkContents(BOARD1_32, model);
    }

    @Test
    public void restartTest() {
        Model model = new Model(board1(), 4);
        model.setActiveRegionColor(3);
        model.setActiveRegionColor(2);
        model.restart();
        assertEquals("Should be 0 moves after restart", 0, model.numMoves());
        checkContents(board1(), model);
    }

    @Test
    public void solvedTest2() {
        Model model = new Model(board1(), 4);
        model.setActiveRegionColor(3);
        assertFalse("Solved after only 1", model.solved());
        model.setActiveRegionColor(1);
        assertFalse("Solved after only 2", model.solved());
        model.setActiveRegionColor(2);
        assertFalse("Solved after only 3", model.solved());
        model.setActiveRegionColor(0);
        assertTrue("Solved after 4", model.solved());
    }

    @Test
    public void roundOverTest() {
        Model model;
        model = new Model(board1(), 4);
        model.setLimit(4);
        assertFalse("Round over at outset", model.roundOver());
        model.setActiveRegionColor(3);
        assertFalse("Round over after only 1", model.roundOver());
        model.setActiveRegionColor(1);
        assertFalse("Round over after only 2", model.roundOver());
        model.setActiveRegionColor(2);
        assertFalse("Round over after only 3", model.roundOver());
        model.setActiveRegionColor(0);
        assertTrue("Round over after 4", model.roundOver());

        model = new Model(board1(), 4);
        model.setLimit(2);
        assertFalse("Round over at outset (limit 2)", model.roundOver());
        model.setActiveRegionColor(3);
        assertFalse("Round over after only 1 (limit 2)", model.roundOver());
        model.setActiveRegionColor(1);
        assertTrue("Round not over after 2 (limit 2)", model.roundOver());
    }

    /** Return a sample initializing array for Flood puzzle.  We re-initialize
     *  our initializing array each time it is used to avoid cascading errors
     *  if a Model should (incorrectly) modify its input array parameter. */
    private int [][] board1() {
        return new int[][] {
            { 2, 3, 3, 1 },
            { 2, 1, 3, 2 },
            { 1, 2, 1, 2 },
            { 1, 0, 2, 0 },
            { 2, 0, 0, 0 }
        };
    }

    private int[][] solvedBoard() {
        return new int[][] {
            { 2, 2, 2, 2 },
            { 2, 2, 2, 2 },
            { 2, 2, 2, 2 }
        };
    }

    /** After setting board1's active region to 0. */
    private static final int[][] BOARD1_0 = {
        { 0, 3, 3, 1 },
        { 0, 1, 3, 2 },
        { 1, 2, 1, 2 },
        { 1, 0, 2, 0 },
        { 2, 0, 0, 0 }
    };

    /** After setting board1's active region to 0 and then 3. */
    private static final int[][] BOARD1_03 = {
        { 3, 3, 3, 1 },
        { 3, 1, 3, 2 },
        { 1, 2, 1, 2 },
        { 1, 0, 2, 0 },
        { 2, 0, 0, 0 }
    };

    /** After setting board1's active region to 3 and then 2. */
    private static final int[][] BOARD1_32 = {
        { 2, 2, 2, 1 },
        { 2, 1, 2, 2 },
        { 1, 2, 1, 2 },
        { 1, 0, 2, 0 },
        { 2, 0, 0, 0 }
    };

}
