import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MineGameEngine {

    enum GameStatus {
        NOTOVER,
        WON,
        LOST;
    }

    enum Type {
        MINE(-1),
        ZERO(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8);

        private int value;
        private static Map map = new HashMap<>();

        private Type(int value) {
            this.value = value;
        }

        static {
            for (Type type : Type.values()) {
                map.put(type.value, type);
            }
        }

        public static Type valueOf(int type) {
            return (Type) map.get(type);
        }

        public int getValue() {
            return value;
        }
    }

    static class Block {
        public Type type;
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
                blocks[i][j].type = Type.ZERO;
                blocks[i][j].isMarkedAsMine = false;
            }
        }

        Random r = new Random();
        int count = 0;
        while (count < numMines) {
            int row = r.nextInt(rows);
            int col = r.nextInt(cols);
            if (blocks[row][col].type != Type.MINE) {
                blocks[row][col].type = Type.MINE;
                count ++;
            }
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (blocks[i][j].type != Type.MINE) {
                    // top left
                    if (i-1 >= 0 && j-1 >= 0 && blocks[i-1][j-1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // top
                    if (i-1 >= 0 && blocks[i-1][j].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // top right
                    if (i-1 >= 0 && j+1 < cols && blocks[i-1][j+1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // right
                    if (j+1 < cols && blocks[i][j+1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // bottom right
                    if (i+1 < rows && j+1 < cols && blocks[i+1][j+1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // bottom
                    if (i+1 < rows && blocks[i+1][j].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // bottom left
                    if (i+1 < rows && j-1 >= 0 && blocks[i+1][j-1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                    // left
                    if (j-1 >= 0 && blocks[i][j-1].type == Type.MINE) {
                        blocks[i][j].type = Type.valueOf(blocks[i][j].type.getValue() + 1);
                    }
                }
            }
        }
    }

    public void print(){
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++){
                if (blocks[i][j].type == Type.MINE) {
                    System.out.print("x ");
                } else if (blocks[i][j].type == Type.ZERO) {
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

    public GameStatus getGameStatus() {
        int markedMines = 0;
        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                if (blocks[i][j].type == Type.MINE && blocks[i][j].isRevealed) {
                    return GameStatus.LOST;
                }
                if (blocks[i][j].type == Type.MINE && blocks[i][j].isMarkedAsMine){
                    markedMines++;
                }
            }
        }
        if (markedMines == this.numMines) {
            return GameStatus.WON;
        }

        return GameStatus.NOTOVER;
    }

        // try to reveal a block; if it is mine it will lead to game over as lost
    public void flip(int r, int c) {
        if (blocks[r][c].type != Type.ZERO) {
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


            if (blocks[row][col].type == Type.ZERO) {
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
            GameStatus result = bla.getGameStatus();
            if (result == GameStatus.WON) {
                System.out.println("You won!");
                break;
            }
            if (result == GameStatus.LOST) {
                System.out.println("You lost!");
                break;
            }
        }
    }
}


