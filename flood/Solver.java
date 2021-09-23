package flood;

import java.util.HashSet;

/** Functions for solving a Flood puzzle.
 *  @author Jingwei Jiang
 */
class Solver {

    /* The search functions here are heuristic; they need not necessarily find
     * an optimal (shortest) sequence of moves, and might well be improved.
     * In fact, the only requirements are that
     *   1. chooseBestMove always chooses a legal move that increases the size
     *      of the active region,
     *   2. movesNeeded returns a result corresponding to the number of moves
     *      needed using moves suggested by chooseBestMove.
     *
     * The staff solution uses a considerably more elaborate heuristic that
     * looks several moves ahead and chooses the first of a sequence of moves
     * that will lead to the largest active region.  Simon Tathum's version of
     * flood uses a still more elaborate heuristic than that.
     *
     * Feel free to look for better methods, but we suggest you first make sure
     * you can meet our very minimal requirements.  We have included a depth
     * parameter in the chooser method as a suggested help in doing deeper
     * searches, but have set the search depth to 1 so that you can essentially
     * ignore it, or change it at will.
     */


    /** Maximum search depth. */
    static final int SEARCH_DEPTH = 1;

    /** Returns an estimate of the number of moves needed to solve the
     *  puzzle depicted by MODEL. This estimate is always correct if it is
     *  possible to solve the puzzle in SEARCH_DEPTH moves. */
    static int movesNeeded(Model model) {
        Model work = new Model(model);
        int num;
        num = 0;
        while (!work.solved()) {
            num += 1;
            int nextc = chooseBestMove(work);
            work.setActiveRegionColor(nextc);
        }
        return num;
    }

    /** Return a heuristic guess of the best next move (color) for MODEL.
     *  Assumes that MODEL does not represent a solved puzzle.  If there is
     *  a win requiring SEARCH_DEPTH or fewer moves, returns a move that will
     *  lead to that win in the fewest moves.  Always returns a move that
     *  increases the size of the active region, and can lead to the maximum
     *  possible increase in its size in SEARCH_DEPTH moves.  When there are
     *  multiple moves with this property, always chooses the numerically
     *  smallest move. */
    static int chooseBestMove(Model model) {
        return chooser(model, SEARCH_DEPTH)[0];
    }

    /* To fulfill the requirements of the project, you can ignore the "depth"
     * parameter altogether in the following.  In that case, you can just
     * choose any color that expands the active region and an arbitrary
     * goodness value (such as the size of the resulting region).
     * For those who are more ambitious and have time on their hands,
     * our solution is somewhat more sophisticated and looks DEPTH moves
     * ahead to find the largest active region it can, returning its size for
     * the goodness plus the first move used to get there.  Simon Tatham,
     * from whom we got this puzzle, uses a slightly more sophisticated
     * method than that, but also searches several moves ahead.  Perhaps
     * you can do better.  We reflect the fact that we are not requiring
     * you to look multiple moves ahead by setting SEARCH_DEPTH to 1.
     * It is likely that you'll want to change it if you decide to do
     * something more complex than we require.
     */

    /** Return a pair (color, goodness), where goodness is the
     *  maximum size of the active region of MODEL in DEPTH moves, and color
     *  is a color that may be used as the first move to get to that size
     *  in DEPTH moves.  When DEPTH is 0, color is undefined and goodness is
     *  simply the current size of the active region.  Leaves MODEL with
     *  the same contents as initially.  Assumes that MODEL is initially
     *  unsolved. Always returns a move that increases the size of the active
     *  region.  If goodness indicates a win (equals the total number of
     *  cells on the board), returns a move that achieves it in the fewest
     *  moves (which will necessarily be 1 if DEPTH is 1). */
    static int[] chooser(Model model, int depth) {
        int winningScore = model.width() * model.height();
        if (depth == 0) {
            return new int[] { -1, model.activeRegionSize() };
        }
        int bestMove, maxSize;
        bestMove = -1;
        maxSize = -1;
        for (int c = 0; c < model.ncolors(); c++) {
            HashSet<Place> newadd = new HashSet<Place>(model.adjacentCells(c));
            model.setActiveRegionColor(c);
            int[] nextcolorpair;
            nextcolorpair = chooser(model, depth - 1);
            if (newadd.size() + nextcolorpair[1] > maxSize) {
                maxSize = newadd.size() + nextcolorpair[1];
                bestMove = c;
            }
            model.undo();
        }
        return new int[] { bestMove, maxSize };
    }

}
