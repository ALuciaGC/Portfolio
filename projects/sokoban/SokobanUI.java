import java.io.*;
import java.util.*;

/**
 * A text-based user interface for a Sokoban puzzle.
 * 
 * @author Dr Mark C. Sinclair
 * @version September 2021
 * 
 * @updated Ana Lucia Garcia Cantu_StudentID: S21042814
 * @version July 2022
 */
public class SokobanUI {
    /**
     * Default constructor
     */
    public SokobanUI() {
        scnr   = new Scanner(System.in);
        puzzle = new Sokoban(new File(FILENAME));
        player = new RandomPlayer();
        //creates stack to store Directions. Useful for command execution.
        moves = new Stack<Direction>();
    }

    /**
     * Main control loop.  This displays the puzzle, then enters a loop displaying a menu,
     * getting the user command, executing the command, displaying the puzzle and checking
     * if further moves are possible
     */
    public void menu() {
        String command = "";
        System.out.print(puzzle);
        while (!command.equalsIgnoreCase("Quit") && !puzzle.onTarget())  {
            displayMenu();
            command = getCommand();
            execute(command);
            if (command.equalsIgnoreCase("Quit")) 
                break;
            System.out.print(puzzle);
            if (puzzle.onTarget())
                System.out.println("puzzle is complete");
            trace("onTarget: "+puzzle.numOnTarget());
        }
    }

    /**
     * Display the user menu
     */
    private void displayMenu()  {
        System.out.println("Commands are:");
        System.out.println("   Move North         [N]");
        System.out.println("   Move South         [S]");
        System.out.println("   Move East          [E]");
        System.out.println("   Move West          [W]");
        System.out.println("   Player move        [P]");
        System.out.println("   Undo move       [Undo]");
        System.out.println("   Restart puzzle [Clear]");
        System.out.println("   Save to file    [Save]");
        System.out.println("   Load from file  [Load]");
        System.out.println("   To end program  [Quit]");    
    }

    /**
     * Get the user command
     * 
     * @return the user command string
     */
    private String getCommand() {
        System.out.print ("Enter command: ");
        return scnr.nextLine();
    }

    /**
     * Execute the user command string
     * 
     * @param command the user command string
     */
    private void execute(String command) {
        if (command.equalsIgnoreCase("Quit")) {
            System.out.println("Program closing down");
            //System.exit(0);
        } else if (command.equalsIgnoreCase("N")) {
            north();
        } else if (command.equalsIgnoreCase("S")) {
            south();
        } else if (command.equalsIgnoreCase("E")) {
            east();
        } else if (command.equalsIgnoreCase("W")) {
            west();
        } else if (command.equalsIgnoreCase("P")) {
            playerMove();
        } else if (command.equalsIgnoreCase("Undo")) {
            undo();
        } else if (command.equalsIgnoreCase("Clear")) {
            clear();
        } else if (command.equalsIgnoreCase("Save")) {
            save();
        } else if (command.equalsIgnoreCase("Load")) {
            load();
        } else {
            System.out.println("Unknown command (" + command + ")");
        }
    }

    /**
     * Move the actor north
     */
    private void north() {
        move(Direction.NORTH);
    }

    /**
     * Move the actor south
     */
    private void south() {
        move(Direction.SOUTH);
    }

    /**
     * Move the actor east
     */
    private void east() {
        move(Direction.EAST);
    }

    /**
     * Move the actor west
     */
    private void west() {
        move(Direction.WEST);
    }

    /**
     * Move the actor according to the computer player's choice
     */
    private void playerMove() {
        Vector<Direction> choices = puzzle.canMove();
        Direction         choice  = player.move(choices);
        move(choice);
    }  

    /**
     * If it is safe, move the actor to the next cell in a given direction
     * 
     * @param dir the direction to move
     */
    private void move(Direction dir) {
        if (!puzzle.canMove(dir)) {
            System.out.println("invalid move");
            return;
        }
        puzzle.move(dir);
        //adds dir to Stack
        moves.push(dir);
        if (puzzle.onTarget())
            System.out.println("game won!");
    }

    /**
     * Reset the game creating new puzzle, player and empty stack
     */
    private void clear() {
        puzzle = new Sokoban (new File(FILENAME));
        player = new RandomPlayer();
        moves = new Stack<Direction>();
        System.out.println("New game!");
    }

    /**
     * Undo a singular move
     */
    private void undo(){
        if(moves.empty())
            return; 
        //eliminates last move fron stack
        moves.pop();
        //creates a new stack "oldMoves" before clearing "moves" out
        Stack<Direction> oldMoves = moves;
        clear();
        //loops through oldMoves and executes move() with each play
        for (Direction play : oldMoves){
            move(play);
        }
        System.out.println("Last move undone!");
    }

    /**
     * Save moves
     */
    private void save(){
        try{
            PrintStream print = new PrintStream (new File(SAVEFILE));
            //loops through each move to be added to save file
            for (Direction play : moves)
                print.println(play);
            print.close();
            System.out.println("Game saved to file!");
        }
        catch(IOException e) {
            System.out.println("An input-output error ocurred");
        }
    }

    /**
     * Load moves from file
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
                //makes a move
                move(play);
            }
            fscnr.close();
            System.out.println("Game loaded from file!");
        }
        catch(IOException e) {
            System.out.println("An input-output error ocurred");
        }
    }

    /**
     * Get stack as String
     * 
     * @return stack as String
     */
    public String getMoves(){
        return moves.toString();
    }

    public static void main(String[] args) {
        SokobanUI ui = new SokobanUI();
        ui.menu();
    }

    /**
     * A trace method for debugging (active when traceOn is true)
     * 
     * @param s the string to output
     */
    public static void trace(String s) {
        if (traceOn)
            System.out.println("trace: " + s);
    }

    private Scanner             scnr           = null;
    private Sokoban             puzzle         = null;
    private Player              player         = null;
    private Stack<Direction>    moves = null;

    private static String  FILENAME = "screens/screen.1";
    private static final String SAVEFILE = "moves.txt";

    private static boolean   traceOn = false; // for debugging
}
