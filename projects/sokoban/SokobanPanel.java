
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;
/**
* Graphical representation of a Sokoban game for the final assignemnt of Module PE7041.
*
* @author Ana Lucia Garcia Cantu_StudentID: S21042814
* @version July 2022
*/
@SuppressWarnings({ "deprecation", "serial" })
public class SokobanPanel extends JPanel  implements Observer, ActionListener
{
    /**
     * Construct a Sokoban Panel from a screen file
     * 
     * @param file the file
     */
    public SokobanPanel(File file) {
        this(fileAsString(file));
    }

    /**
     * Construct a Sokoban Panel from a standard screen String
     * 
     * @param screen as a String
     */
    public SokobanPanel(String screen){
        //defensive code against null String
        if (screen == null)
            throw new SokobanException("Screen cannot be null");

        Scanner           scnr = null;
        ArrayList<String> lines = new ArrayList<>();
        scnr = new Scanner(screen);
        //sets the number of rows and columns from the scanned screen
        while (scnr.hasNextLine()) {
            String line = scnr.nextLine();
            if (line.length() > 0) {
                lines.add(line);
                numRows++;
                if (line.length() > numCols)
                    numCols = line.length();
            }
        }
        scnr.close();

        grid = new JPanel(new GridLayout(numRows,numCols));
        cells = new PanelCell[numRows][numCols];
        for (int row=0; row<numRows; row++) {
            String line = lines.get(row);
            for (int col=0; col<numCols; col++) {
                char display = (col < line.length()) ? line.charAt(col) : Sokoban.EMPTY;
                //creates individual PanelCells with specific display, row and col
                cells[row][col] = new PanelCell(display, this, row, col);
                grid.add(cells[row][col]);
            }
        }
        
        //set up main game grid
        setLayout(new BorderLayout());
        add(grid,BorderLayout.NORTH);

        //sets up buttons
        clear = new JButton("Clear");
        clear.setFont(Bfont);
        clear.addActionListener(this);
        undo = new JButton("Undo");
        undo.setFont(Bfont);
        undo.addActionListener(this);
        save = new JButton("Save");
        save.setFont(Bfont);
        save.addActionListener(this);
        load = new JButton("Load");
        load.setFont(Bfont);
        load.addActionListener(this);
        level = new JButton("Choose the level");
        level.setFont(Bfont);
        level.addActionListener(this);

        //sets up buttons panel
        JPanel center = new JPanel(new GridLayout(1,5));
        center.add(clear);
        center.add(undo);
        center.add(save);
        center.add(load);
        center.add(level);
        //center.setPreferredSize(20, screenSize);
        //center.setBackground(Color.RED);
        add(center, BorderLayout.CENTER);

        //sets up status bar
        status = new JTextArea();
        add(new JScrollPane(status), BorderLayout.SOUTH);
        status.setText("Help the mail lady deliver the packages!");
        status.setFont(Sfont);

        //sets up puzzle from predetermined or selected screen
        setScreen(FILENAME);
    }

    /**
     * Sets up a new Sokoban game from either the predetermined or a selected screen
     * Sets up an empty stack needed to run button commands
     * 
     * @param screen as a String
     */
    private void setScreen(String file){
        game = new Sokoban(new File(file));
        game.addObserver(this);
        //creates stack to store Directions. Useful for button execution.
        stack = new Stack<Direction>();
    }

    /**
     * Updates PanelCells when a move has been made:
     * The Cell variable returns three cell values, which are passed through setNewIcon() 
     * to receive an updated icon
     * 
     * @param o the observable
     * @param arg the cell that was assigned
     */
    @Override
    public void update(Observable o, Object arg) {
        //defensive code against a null arg
        if (arg == null)
            throw new SokobanException("arg (Cell) is null");
        Cell c= (Cell) arg;
        //updates the PanelCell's icon using the row, column and new display values
        cells[c.getRow()][c.getCol()].setNewIcon(c.getDisplay());
    }

    /**
     * Handles button presses: clear, undo, save, load and level
     * Assigns the action event to the aeInput variable
     * 
     * @param ae the ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == clear){
            aeInput = ae.getSource().toString();
            clear();
        }
        else if (ae.getSource() == undo){
            aeInput = ae.getSource().toString();
            undo();
        }
        else if (ae.getSource() == save){
            aeInput = ae.getSource().toString();
            save();
        }
        else if (ae.getSource() == load){
            aeInput = ae.getSource().toString();
            load();
        }
        else if (ae.getSource() == level){
            aeInput = ae.getSource().toString();
            level();
        }
    }

    /**
     * Sets the status bar to a given string
     * 
     * @param String s
     */
    public void setStatus(String s) {
        status.setText(s);
    }

    /**
     * Makes a move under specific conditions:
     * If valid, makes the move
     * If invalid, provides feedback through the status bar
     * If winning move, opens the level window
     * 
     * @param dir the direction to move
     */
    public void makeMove(Direction dir) {
        //which are the valid moves? 
        Vector<Direction> dirs = new Vector<>();
        for (Direction checkDir: Direction.values()) {
            if (game.canMove(checkDir))
                dirs.add(checkDir);
        }
        if (!game.canMove(dir)) {
            //based on dirs Vector, provides feedback to user on possible moves through the status bar
            setStatus("Invalid move. You may move in the following directions: " + dirs.toString());
            return;
        }
        game.move(dir);
        //adds dir to stack
        stack.push(dir);
        //if won
        if (game.onTarget()){
            setStatus("Game won!");
            level();
        }
    }

    /**
     * Resets the game
     */
    private void clear() {
        game.clear();
        game = new Sokoban (new File(FILENAME));
        game.addObserver(this);
        stack = new Stack<Direction>();
    }

    /**
     * Undoes a singular move
     */
    private void undo(){
        if(stack.empty())
            return; 

        //eliminates the last move in the stack
        stack.pop();
        //creates a new stack "oldMoves" before clearing "moves" out
        Stack<Direction> oldMoves = stack;
        clear();
        //loops through oldMoves and executes MakeMove() with each play
        for (Direction play : oldMoves){
            makeMove(play);
        }
        setStatus("Last move undone!");
    }

    /**
     * Saves moves to file
     */
    private void save(){
        try{
            PrintStream print = new PrintStream (new File(SAVEFILE));
            //adds all plays made to the selected savefile
            for (Direction play : stack)
                print.println(play);
            print.close();
            setStatus("Game saved to file!");
        }
        catch(IOException e) {
            setStatus("An input-output error ocurred");
        }
    }

    /**
     * Loads moves from file
     */
    private void load(){
        try{
            Scanner fscnr = new Scanner (new File(SAVEFILE));
            clear();
            while (fscnr.hasNext()){
                //creates string "a" for each line in scnr
                String a = fscnr.next();
                //creates a Direction variable that contains all the lines in scnr
                Direction play = Direction.fromString(a);
                makeMove(play);
            }
            fscnr.close();
            setStatus("Game loaded from file!");
        }
        catch(IOException e) {
            setStatus("An input-output error ocurred");
        }
    }

    /**
     * Updates FILENAME from user selection
     */
    private void level(){
        String response = JOptionPane.showInputDialog(this, "Choose your level (0-99)");
        //defensive code against a null response
        if (response != null){
            try{
                int levelNum = Integer.parseInt(response);
                //defensive code against a response outside limits
                if (levelNum < 1 || levelNum > 99)
                    setStatus("Please type in a number between 0-99");
                else{
                    StringBuffer buf = new StringBuffer(FILENAME);
                    //eliminates the last char in the FILENAME
                    buf.deleteCharAt(buf.length()-1); 
                    //adds user;s response
                    buf.append(response);
                    FILENAME = buf.toString();
                    frame.remove(this);
                    //creates a new panel with the updated FILENAME
                    SokobanPanel panel = new SokobanPanel(new File (FILENAME));
                    frame.add(panel);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.pack();
                    frame.setVisible(true);
                }
            } catch(NumberFormatException nfe) {
                //defensive code against a non-number response
                setStatus("Please enter a number");
            }
        }
    }

    /**
     * Gets the number of rows
     * 
     * @return the number of rows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Gets the number of columns
     * 
     * @return the number of columns
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * Getter method for testing
     * Gets the stack as a String
     * 
     * @return stack as String
     */
    public String getStack(){
        return stack.toString();
    }

    /**
     * Testing method
     * 
     * @return Sokoban Panel as a String
     */
    public String panelToString(){
        StringBuffer buf = new StringBuffer();
        buf.append("SokobanPanel(" + FILENAME + ")");
        return buf.toString();
    }

    /**
     * Testing method
     * 
     * @return status bar as a String
     */
    public String statusToString(){
        return status.getText();
    }

    /**
     * Convert file content into String
     * Used with Constructor.
     * 
     * @param file the file
     * @return the file as a string
     */
    public static String fileAsString(File file) {
        if (file == null)
            throw new SokobanException("File cannot be null");
        Scanner      fscnr = null;
        StringBuffer sb    = new StringBuffer();
        try {
            fscnr = new Scanner(file);
            while (fscnr.hasNextLine())
                sb.append(fscnr.nextLine()+"\n");
        } catch(IOException e) {
            throw new SokobanException(""+e);
        } finally {
            if (fscnr != null)
                fscnr.close();
        }
        return sb.toString();
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

    public static void main(String[] args){
        SokobanPanel panel = new SokobanPanel(new File(FILENAME));
        frame = new JFrame("Sokoban");

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private Sokoban             game    = null;
    private PanelCell[][]       cells   = null;
    private Stack<Direction>    stack   = null;
    private JPanel              grid    = null;
    private JButton             clear   = null;
    private JButton             undo    = null;
    private JButton             save    = null;
    private JButton             load    = null;
    private JButton             level   = null;
    private JTextArea           status  = null;

    public  String  aeInput     = null;
    private int     numRows     = 0;
    private int     numCols     = 0;

    private static        JFrame  frame          = null;
    private static final  String  SAVEFILE       = "moves.txt";
    private static        String  FILENAME       = "screens/screen.1";
    public static         Font    Sfont          = new Font("Dialog", Font.BOLD, 20);
    public static         Font    Bfont          = new Font("Dialog", Font.PLAIN, 15);

    private static        boolean traceOn        = false; // for debugging
}
