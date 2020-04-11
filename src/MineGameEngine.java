import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MineGameEngine {

    static class Block {
        // -1 means it is mine; 0~8 means there are the said number of mines neighboring
        public int type;
        public boolean isRevealed;
        public boolean isMarkedAsMine;
    }

    public Block[][] blocks;
    public int numMines;
    public int minesLeft;

    // initialize a game
    public void init(int rows, int cols, int numMines) {
        this.numMines = numMines;
        minesLeft = numMines;
        if (numMines > rows * cols) {
            throw new RuntimeException("Too many mines!");
        }
        blocks = new Block[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                blocks[i][j] = new Block();
                blocks[i][j].isRevealed = false;
                blocks[i][j].type = 0;
                blocks[i][j].isMarkedAsMine = false;
            }
        }

        Random r = new Random();
        int count = 0;
        while (count < numMines) {
            int row = r.nextInt(rows);
            int col = r.nextInt(cols);
            if (blocks[row][col].type != -1) {
                blocks[row][col].type = -1;
                count ++;
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (blocks[i][j].type != -1) {
                    // top left
                    if (i-1 >= 0 && j-1 >= 0 && blocks[i-1][j-1].type == -1) {
                        blocks[i][j].type++;
                    }
                    // top
                    if (i-1 >= 0 && blocks[i-1][j].type == -1) {
                        blocks[i][j].type++;
                    }
                    // top right
                    if (i-1 >= 0 && j+1 < cols && blocks[i-1][j+1].type == -1) {
                        blocks[i][j].type++;
                    }
                    // right
                    if (j+1 < cols && blocks[i][j+1].type == -1) {
                        blocks[i][j].type++;
                    }
                    // bottom right
                    if (i+1 < rows && j+1 < cols && blocks[i+1][j+1].type == -1) {
                        blocks[i][j].type++;
                    }
                    // bottom
                    if (i+1 < rows && blocks[i+1][j].type == -1) {
                        blocks[i][j].type++;
                    }
                    // bottom left
                    if (i+1 < rows && j-1 >= 0 && blocks[i+1][j-1].type ==-1) {
                        blocks[i][j].type++;
                    }
                    // left
                    if (j-1 >= 0 && blocks[i][j-1].type == -1) {
                        blocks[i][j].type++;
                    }
                }
            }
        }
    }

    public void print(){
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++){
                if (blocks[i][j].type == -1) {
                    System.out.print("x ");
                } else if (blocks[i][j].type == 0) {
                    System.out.print("  ");
                } else {
                    System.out.print(blocks[i][j].type + " ");
                }
            }
            System.out.print("\n");
        }
    }

    public void printForPlayer(){
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++){
                if (blocks[i][j].isMarkedAsMine) {
                    System.out.print("f ");
                }
                else if (!blocks[i][j].isRevealed) {
                    System.out.print("□ ");
                } else {
                    System.out.print(blocks[i][j].type + " ");
                }
            }
            System.out.print("\n");
        }
    }


    public void markAsMine(int r, int col) {
        if (!blocks[r][col].isRevealed) {
            blocks[r][col].isMarkedAsMine = true;
            minesLeft--;
        }
    }

    public void removeMarkAsMine(int r, int col) {
        if (blocks[r][col].isMarkedAsMine) {
            blocks[r][col].isMarkedAsMine = false;
            minesLeft++;
        }
    }

        // returns 0 to indicate not over; 1 to indicate won; 2 to indicate lost;

    public int gameOver() {
        int markedMines = 0;
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                if (blocks[i][j].type == -1 && blocks[i][j].isRevealed) {
                    return 2;
                }
                if (blocks[i][j].type == -1 && blocks[i][j].isMarkedAsMine){
                    markedMines++;
                }
            }
        }
        if (markedMines == this.numMines) {
            return 1;
        }

        return 0;
    }

        // try to reveal a block; if it is mine it will lead to game over as lost

    public void flip(int r, int c) {
        if (blocks[r][c].type != 0) {
            blocks[r][c].isRevealed = true;
            System.out.println( r + ", " + c + "is marked as revealed");
            return;
        }

        Queue<Integer> q = new LinkedList<>();
        q.add(r);
        q.add(c);
        blocks[r][c].isRevealed = true;
        System.out.println("Now adding: " + r + ", " + c);

        while (!q.isEmpty()) {
            int row = q.remove();
            int col = q.remove();


            if (blocks[row][col].type == 0) {
                // up
                if (row - 1 >= 0 && !blocks[row-1][col].isRevealed){
                    q.add(row - 1);
                    q.add(col);
                    blocks[row-1][col].isRevealed = true;
                    System.out.println("Now adding: " + (row-1) + ", " + col);
                }
                // up left
                if (row - 1 >= 0 && col - 1 >= 0 && !blocks[row-1][col-1].isRevealed) {
                    q.add(row - 1);
                    q.add(col - 1);
                    blocks[row-1][col-1].isRevealed = true;
                    System.out.println("Now adding: " + (row-1) + ", " + (col-1));
                }
                // up right
                if (row - 1 >= 0 && col + 1 < blocks[row].length && !blocks[row-1][col + 1].isRevealed) {
                    q.add(row - 1);
                    q.add(col + 1);
                    blocks[row-1][col+1].isRevealed = true;
                    System.out.println("Now adding: " + (row-1) + ", " + (col+1));
                }
                // down
                if (row + 1 < blocks.length && !blocks[row+1][col].isRevealed){
                    q.add(row + 1);
                    q.add(col);
                    blocks[row+1][col].isRevealed = true;
                    System.out.println("Now adding: " + (row+1) + ", " + (col));
                }
                // down left
                if (row + 1 < blocks.length && col - 1 >= 0 && !blocks[row + 1][col - 1].isRevealed) {
                    q.add(row + 1);
                    q.add(col - 1);
                    blocks[row+1][col-1].isRevealed = true;
                    System.out.println("Now adding: " + (row+1) + ", " + (col-1));
                }
                // down right
                if (row + 1 < blocks.length && col + 1 < blocks[row].length && !blocks[row+1][col + 1].isRevealed) {
                    q.add(row + 1);
                    q.add(col + 1);
                    blocks[row+1][col+1].isRevealed = true;
                    System.out.println("Now adding: " + (row+1) + ", " + (col+1));
                }
                // left
                if (col - 1 >= 0 && !blocks[row][col - 1].isRevealed){
                    q.add(row);
                    q.add(col - 1);
                    blocks[row][col-1].isRevealed = true;
                    System.out.println("Now adding: " + (row) + ", " + (col-1));
                }
                // right
                if (col + 1 < blocks[row].length && !blocks[row][col + 1].isRevealed){
                    q.add(row);
                    q.add(col + 1);
                    blocks[row][col+1].isRevealed = true;
                    System.out.println("Now adding: " + (row) + ", " + (col+1));
                }
            }
        }
    }

        // returns the current state of game as 2-D array of Blocks
    public Block[][] getState(){
        return blocks;
    }

    public static void main(String[] args) throws IOException {

        MineGameEngine bla = new MineGameEngine();
        bla.init(10, 10, 5);
        // bla.print();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String[] elements;
        while (true) {
            bla.printForPlayer();
            // bla.print();
            int r = 0;
            int c = 0;
            while (true) {
                String input = reader.readLine();
                elements = input.split(" ");
                if (elements.length != 3) {
                    System.out.println("Only 3 elements are allowed!");
                    continue;
                }
                try{
                    r = Integer.parseInt(elements[0]);
                    c = Integer.parseInt(elements[1]);
                }
                catch (NumberFormatException e) {
                    System.out.println("Input coordinates are not valid!" + e.getMessage());
                    continue;
                }
                break;
            }
            String action = elements[2];
            if (action.equals("flip")) {
                bla.flip(r, c);
            }
            else if (action.equals("mark")) {
                bla.markAsMine(r, c);
            } else {
                bla.removeMarkAsMine(r, c);
            }
            int result = bla.gameOver();
            if (result == 1) {
                System.out.println("You won!");
                break;
            }
            if (result == 2) {
                System.out.println("You lost!");
                break;
            }
        }
    }
}


