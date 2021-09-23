package flood;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.concurrent.ArrayBlockingQueue;

import java.awt.Dimension;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import static flood.Utils.*;

/** The GUI controller for a Flood board and buttons.
 *  @author P. N. Hilfinger
 */
class GUI extends TopLevel implements View {

    /** Margins around label placement are multiples of this (pixels). */
    static final int UNIT_MARGIN = 5;

    /** Size of pane used to contain help text. */
    static final Dimension TEXT_BOX_SIZE = new Dimension(500, 700);

    /** Resource name of "About" message. */
    static final String ABOUT_TEXT = "flood/About.html";

    /** Resource name of Signpost help text. */
    static final String HELP_TEXT = "flood/Help.html";

    /** A new window with given TITLE providing a view of MODEL. */
    GUI(String title) {
        super(title, true);
        addMenuButton("Game->New", this::newGame);
        addMenuButton("Game->Restart", this::restartGame);
        addMenuButton("Game->Solve", this::showSolution);
        addSeparator("Game");
        addMenuButton("Game->Undo", this::undo);
        addMenuButton("Game->Redo", this::redo);
        addSeparator("Game");
        addMenuButton("Type->12x12 Easy", (s) -> newSize(12, 12, 6, 4));
        addMenuButton("Type->12x12 Medium", (s) -> newSize(12, 12, 6, 2));
        addMenuButton("Type->12x12 Hard", (s) -> newSize(12, 12, 6, 0));
        addMenuButton("Type->16x16 Easy", (s) -> newSize(16, 16, 6, 4));
        addMenuButton("Type->16x16 Medium", (s) -> newSize(16, 16, 6, 2));
        addMenuButton("Type->16x16 Hard", (s) -> newSize(16, 16, 6, 0));
        addMenuButton("Type->12x12, 4 colors", (s) -> newSize(12, 12, 4, 0));
        addMenuButton("Type->12x12, 3 colors", (s) -> newSize(12, 12, 3, 0));
        addMenuButton("Type->Custom...", (s) -> newSize());
        addSeparator("Type");
        addMenuButton("Game->Seed", this::newSeed);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Help->About", (s) -> displayText("About", ABOUT_TEXT));
        addMenuButton("Help->Flood", (s) -> displayText("Flood Help",
                                                        HELP_TEXT));
        _extra = 4;
    }

    /** Response to "Quit" button click. */
    private void quit(String dummy) {
        _pendingCommands.offer("QUIT");
    }

    /** Response to "New Game" button click. */
    private void newGame(String dummy) {
        _pendingCommands.offer("NEW");
    }

    /** Response to "Undo" button click. */
    private void undo(String dummy) {
        _pendingCommands.offer("UNDO");
    }

    /** Response to "Redo" button click. */
    private void redo(String dummy) {
        _pendingCommands.offer("REDO");
    }

    /** Response to "New Game" button click. */
    private void restartGame(String dummy) {
        _pendingCommands.offer("RESTART");
    }

    /** Response to "Solve" button click. */
    private void showSolution(String dummy) {
        _pendingCommands.offer("SOLVE");
    }

    /** Display text in resource named TEXTRESOURCE in a new window titled
     *  TITLE. */
    private void displayText(String title, String textResource) {
        /* Implementation note: It would have been more convenient to avoid
         * having to read the resource and simply use dispPane.setPage on the
         * resource's URL.  However, we wanted to use this application with
         * a nonstandard ClassLoader, and arranging for straight Java to
         * understand non-standard URLS that access such a ClassLoader turns
         * out to be a bit more trouble than it's worth. */
        JFrame frame = new JFrame(title);
        JEditorPane dispPane = new JEditorPane();
        dispPane.setEditable(false);
        dispPane.setContentType("text/html");
        InputStream resource =
            GUI.class.getClassLoader().getResourceAsStream(textResource);
        StringWriter text = new StringWriter();
        try {
            while (true) {
                int c = resource.read();
                if (c < 0) {
                    dispPane.setText(text.toString());
                    break;
                }
                text.write(c);
            }
        } catch (IOException e) {
            return;
        }
        JScrollPane scroller = new JScrollPane(dispPane);
        scroller.setVerticalScrollBarPolicy(scroller.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setPreferredSize(TEXT_BOX_SIZE);
        frame.add(scroller);
        frame.pack();
        frame.setVisible(true);
    }

    /** Pattern describing the 'size' command's arguments. */
    private static final Pattern INT_PATN = Pattern.compile("\\d+$");

    /** Pattern describing the 'seed' command's arguments. */
    private static final Pattern SEED_PATN =
        Pattern.compile("\\s*(-?\\d{1,18})\\s*$");

    /** Set board size, number of colors, and allowed excess moves to
     *  to WIDTH, HEIGHT, NCOLORS, and EXTRA. */
    private void newSize(int width, int height, int ncolors, int extra) {
        _pendingCommands.offer(String.format("TYPE %d %d %d %d",
                                             width, height, ncolors, extra));
    }

    /** Respond to "Custom Size..." menu click. */
    private void newSize() {
        String[] response =
            getTextInputs(null, "Flood Configuration", "plain", 5,
                          "Width", String.format("%d", _width),
                          "Height", String.format("%d", _height),
                          "Colors", String.format("%d", _ncolors),
                          "Extra moves allowed", String.format("%d", _extra));
        if (response != null) {
            for (int i = 0; i < response.length; i += 1) {
                if (!Pattern.matches("\\d+$", response[i])) {
                    showMessage("Responses must be non-negative integers.",
                                "Error", "error");
                }
            }

            int width = toInt(response[0]),
                height = toInt(response[1]),
                ncolors = toInt(response[2]),
                extra = toInt(response[3]);
            if (width > 2 && height > 2 && ncolors > 2
                && ncolors <= Model.MAX_COLORS && extra >= 0) {
                newSize(width, height, ncolors, extra);
                _extra = extra;
            } else {
                showMessage("Bad board configuration parameters.",
                            "Error", "error");
            }
        }
    }

    /** Respond to "Seed" menu click. */
    private void newSeed(String dummy) {
        String response =
            getTextInput("Enter new random seed.", "New seed",  "plain", "");
        if (response != null) {
            Matcher mat = SEED_PATN.matcher(response);
            if (mat.matches()) {
                _pendingCommands.offer(String.format("SEED %s", mat.group(1)));
            } else {
                showMessage("Enter an integral seed value.", "Error", "error");
            }
        }
    }

    /** Return the next command from our widget, waiting for it as necessary.
     *  Clicking on grid cells results in "SET" messages.
     *  Menu-button clicks result in the messages "QUIT", "NEW", "UNDO",
     *  "REDO", "RESTART", "SEED", "SOLVE", "SIZE", or "LEVEL". */
    String readCommand() {
        try {
            return _pendingCommands.take();
        } catch (InterruptedException excp) {
            throw new Error("unexpected interrupt");
        }
    }

    @Override
    public void update(Model model) {
        if (_widget == null) {
            _widget = new BoardWidget(_pendingCommands);
            int pad = _widget.PADDING;
            add(_widget,
                new LayoutSpec("y", 0,
                               "ileft", pad, "iright", pad,
                               "itop", pad, "ibottom", pad,
                               "height", 1,
                               "width", 2));
            addLabel("0 / 0 moves", "MoveLabel",
                     new LayoutSpec("y", 1,
                                    "height", "REMAINDER",
                                    "x", 0,
                                    "anchor", "southwest",
                                    "ileft", 4 * UNIT_MARGIN,
                                    "itop", UNIT_MARGIN,
                                    "ibottom", UNIT_MARGIN));
            addLabel("                 ", "DoneLabel",
                     new LayoutSpec("y", 1,
                                    "x", 1,
                                    "anchor", "south",
                                    "ibottom", UNIT_MARGIN));
        }
        setLabel("MoveLabel",
                 String.format("%d / %d moves",
                               model.numMoves(), model.limit()));
        if (model.solved()) {
            setLabel("DoneLabel", "SOLVED!");
        } else if (model.limit() <= model.numMoves()) {
            setLabel("DoneLabel", "MOVE LIMIT REACHED");
        } else {
            setLabel("DoneLabel", "                 ");
        }
        if (_width != model.width() || _height != model.height()) {
            display(false);
        }
        _widget.update(model);
        _width = model.width();
        _height = model.height();
        _ncolors = model.ncolors();
        display(true);
    }

    /** The board widget. */
    private BoardWidget _widget;
    /** The current size of the model. */
    private int _width, _height;
    /** The current number of colors. */
    private int _ncolors;
    /** Last value set for _extra. */
    private int _extra;

    /** Queue of pending key presses. */
    private ArrayBlockingQueue<String> _pendingCommands =
        new ArrayBlockingQueue<>(5);

}
