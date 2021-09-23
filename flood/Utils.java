package flood;

import static java.lang.System.arraycopy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

/** Various utility methods.
 *  @author P. N. Hilfinger
 */
class Utils {

    /** Returns String.format(FORMAT, ARGS...). */
    static String msg(String format, Object... args) {
        return String.format(format, args);
    }

    /** Copy contents of SRC into DEST.  SRC and DEST must both be
     *  rectangular, with identical dimensions. */
    static void deepCopy(int[][] src, int[][] dest) {
        assert src.length == dest.length && src[0].length == dest[0].length;
        for (int i = 0; i < src.length; i += 1) {
            arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
    }

    /** Returns a deep copy of SRC: the result has no shared state with SRC. */
    static int[][] deepCopyOf(int[][] src) {
        int[][] result = new int[src.length][src[0].length];
        deepCopy(src, result);
        return result;
    }

    /** Returns true iff V1 and V2 have the same dimensions and each row of V1
     *  has the same contents as the corrsponding row of V2. */
    static boolean arrayEquals(int[][] v1, int[][] v2) {
        if (v1.length != v2.length) {
            return false;
        }
        for (int i = 0; i < v1.length; i += 1) {
            if (!Arrays.equals(v1[i], v2[i])) {
                return false;
            }
        }
        return true;
    }

    /** Check that the set of T's in EXPECTED is the same as that in ACTUAL.
     *  Use MSG as the error message if the check fails. */
    static <T> void assertSetEquals(String msg,
                                     Collection<T> expected,
                                     Collection<T> actual) {
        assertNotNull(msg, actual);
        assertEquals(msg, new HashSet<T>(expected), new HashSet<T>(actual));
    }

    /** Return an IllegalArgumentException whose message is formed from
     *  MSGFORMAT and ARGS as for String.format. */
    static IllegalArgumentException badArgs(String msgFormat, Object... args) {
        return new IllegalArgumentException(String.format(msgFormat, args));
    }

    /** Return integer denoted by NUMERAL. */
    static int toInt(String numeral) {
        return Integer.parseInt(numeral);
    }

    /** Return long integer denoted by NUMERAL. */
    static long toLong(String numeral) {
        return Long.parseLong(numeral);
    }

}

