import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javafx.scene.layout.Border;
import javax.swing.border.*;

/**
 * Graphical representation of a cell in a Sokoban game for final assignemnt of Module PE7041.
 *
 * @author Ana Lucia Garcia Cantu_StudentID: S21042814
 * @version July 2022
 */
public class PanelCell extends JPanel implements MouseListener, KeyListener
{
    /**
     * Constructor that creates an individual Cell
     * 
     * @param d the display character
     * @param p the parent SokobanPanel
     * @param r the row in the game
     * @param c the column in the game
     */
    public PanelCell(char d, SokobanPanel p, int r, int c){
        if (p == null)
            throw new SokobanException("Panel cannot be null");
        if ((r<0) || (r>=p.getNumRows()))
            throw new SokobanException("Invalid row (" + r + ")");
        if ((c<0) || (c>=p.getNumCols()))
            throw new SokobanException("Invalid column (" + c + ")");
        panel   = p;
        row     = r;
        col     = c;
        display = d;

        //sets up cell grid
        JPanel cellPanel = new JPanel(new GridLayout(1,1));

        //gets the height and width of the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int)screenSize.getWidth()-350;
        int screenHeight = (int)screenSize.getHeight()-250;
        //gets the height and width of the cell
        cellWidth = screenWidth/panel.getNumCols();
        cellHeight = screenHeight/panel.getNumRows();

        //creates label to contain icon
        label = new JLabel();
        add(label);
        ImageIcon icon = new ImageIcon(new ImageIcon(getImageName(display)).getImage().getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH));
        label.setIcon(icon);

        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocus();
        
        setFont(font);
        setBorder(new EmptyBorder(new Insets(-5,-5,-5,-5)));
        setPreferredSize(new Dimension(cellWidth,cellHeight));
    }

    // unimplemented methods from MouseListener interface
    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    //unimplemented methods from KeyListener interface
    @Override
    public void keyReleased(KeyEvent e) {}

    /**
     * Requests focus in window.
     * 
     * @Param e the MouseEvent
     */
    public void mouseEntered(MouseEvent e) {
        requestFocusInWindow();
    }

    /**
     * Clears the status bar.
     * 
     * @Param e the MouseEvent
     */
    public void mouseExited(MouseEvent e) {
        panel.setStatus("");
    }

    /**
     * Clears the status bar.
     * 
     * @Param e the KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        panel.setStatus("");
    }

    /**
     * Handles typed keys.
     * 
     * @param e the KeyEvent
     */
    public void keyTyped(KeyEvent e){
        char c = Character.toUpperCase(e.getKeyChar());
        trace("c: "+c);
        //defensive code against invalid characters
        if (!isValid(c))
            panel.setStatus("Please type your move as N, S, W or E");
        else{
            //assign converted char to Direction variable
            Direction dir = charToDir(c);
            panel.makeMove(dir);
        }
    }

    /**
     * Converts char to corresponding Direction.
     * 
     * @param c th =e char
     */
    public Direction charToDir(char c){
        if (c == NORTH)
            return(Direction.NORTH);
        else if (c == SOUTH)
            return(Direction.SOUTH);
        else if (c == EAST)
            return(Direction.EAST);
        else if (c == WEST)
            return(Direction.WEST);
        else 
        //defensive code against invalid characters    
            throw new IllegalArgumentException("Invalid character");
    }

    /**
     * Returns true if the typed char is valid.
     * 
     * @param c the character
     */
    public boolean isValid(char c){
        return ((c == NORTH) || (c == SOUTH) || (c == EAST) || (c == WEST));
    }

    /**
     * Equates Sokoban's valid displays to char and returns the String that will form part 
     * of the final image file name. 
     * 
     * @param display the character assigned when creating the cell
     * @return String
     */
    private String setImage(char display){
        if (display == Sokoban.BOX)
            image = "box";
        else if (display == Sokoban.ACTOR)
            image = "actor";
        else if (display == Sokoban.TARGET)
            image = "target";
        else if (display == Sokoban.TARGET_BOX)
            image = "target_box";
        else if (display == Sokoban.TARGET_ACTOR)
            image = "target_actor";
        else if (display == Sokoban.WALL)
            image = "wall";
        else if (display == Sokoban.EMPTY)
            image = "";
        else
        //defensive code against invalid characters    
            throw new IllegalArgumentException("Invalid display character");
        return image;
    }

    /**
     * Gets the name of the image based on a char.
     * 
     * @param display the char
     * @return image name
     */
    public String getImageName(char display){
        return getImageName(setImage(display));
    }

    /**
     * Gets the name of the image based on a String.
     * 
     * @param image the String
     * @return image name as a String
     */
    public static String getImageName(String image){
        StringBuffer buf = new StringBuffer(ICON_DIR);
        buf.append(image);
        buf.append(".png");
        return buf.toString();
    }

    /**
     * Updates cell icon when a move has been made and a new
     * display is assigned.
     * 
     * @param display the char
     */
    public void setNewIcon(char display){
        //assigns new display
        this.display = display;

        ImageIcon icon = new ImageIcon(new ImageIcon(getImageName(display)).getImage().getScaledInstance(cellWidth, cellHeight, Image.SCALE_SMOOTH));
        label.setIcon(icon);
        add(label);
    }

    /**
     * Trace method for debugging (active when traceOn is true)
     * 
     * @param s the string to output
     */
    public static void trace(String s) {
        if (traceOn)
            System.out.println("trace: " + s);
    }

    private SokobanPanel   panel;
    private Sokoban        game;
    private ImageIcon      icon;
    private String         image;
    private char           display;
    private int            row;
    private int            col;
    private int            cellHeight;
    private int            cellWidth;

    public static final char NORTH         = 'N';
    public static final char SOUTH         = 'S';
    public static final char EAST          = 'E';
    public static final char WEST          = 'W';

    private JLabel      label      = null;

    private static final String     ICON_DIR   = "icons/";
    public static Font              font = new Font("Dialog", Font.BOLD, 100);

    private static boolean          traceOn    = false; // for debugging
}
