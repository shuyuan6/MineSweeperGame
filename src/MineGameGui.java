import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import javafx.scene.input.MouseEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MineGameGui extends Application {

    double lastMouseClickedPositionX = 0;
    double lastMouseClickedPositionY = 0;
    // 0 means left button, 1 means right button
    int lastMouseClickedButton = 0;
    boolean mouseClickProcessed = true;
    final double squareSize = 30d;
    static boolean gameOver = false;
    MineGameEngine.Block[][] blocks;

    Rectangle[][] rectangles;
    Text[][] texts;
    MineGameEngine game;
    Pane mineField = new Pane();
    Label statusLabel = new Label();
    Label countLabel = new Label();
    Label timerLabel = new Label();
    Button restart = new Button();
    HBox hBox = new HBox(countLabel, restart, timerLabel);
    VBox vBox = new VBox();
    static int remainingTime = 50;

    public void initGame() {
        remainingTime = 50;
        timerLabel.setText("Remaining time: " + remainingTime);
        statusLabel.setText("Come on!");
        gameOver = false;
        game = new MineGameEngine();
        game.init(20, 20, 5);
        blocks = game.getState();
        rectangles = new Rectangle[blocks.length][blocks[0].length];
        texts = new Text[blocks.length][blocks[0].length];

        for (int i = 0; i < blocks.length; i++) {
            for (int j = 0; j < blocks[i].length; j++) {
                Rectangle rectangle = new Rectangle(squareSize * j, squareSize * i, squareSize, squareSize);
                rectangles[i][j] = rectangle;

                Text text = new Text();
                text.setX((squareSize * j + 12));
                text.setY((squareSize * i + 20));
                text.toFront();
                texts[i][j] = text;

                mineField.getChildren().add(rectangle);
                mineField.getChildren().add(text);
            }
        }
    }

    @Override
    public void start(Stage stage) {

        mineField.setPrefSize(600, 600);

        initGame();

        // create a label to show mine count
        countLabel.setText("There are " + game.minesLeft + " mines left!");
        countLabel.setPrefWidth(300);

        // create a button to restart a game
        restart.setText("Restart the game!");
        restart.setOnAction(event ->
        {
            initGame();
        });

        vBox.getChildren().add(hBox);
        vBox.getChildren().add(mineField);
        vBox.getChildren().add(statusLabel);

        Scene scene = new Scene(vBox, 600, 700);
        stage.setTitle("MineGame");
        stage.setScene(scene);
        stage.show();

        EventHandler<MouseEvent> eventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                mouseClickProcessed = false;
                MouseButton mouseButton = e.getButton();
                double x = e.getX();
                double y = e.getY();
                if (mouseButton.equals(MouseButton.PRIMARY)) {
                    System.out.println("Left button clicked");
                    lastMouseClickedButton = 0;
                } else if (mouseButton.equals(MouseButton.SECONDARY)) {
                    System.out.println("Right button clicked");
                    lastMouseClickedButton = 1;
                }
                System.out.printf("Mouse click received at (%f, %f)\n", x, y);

                lastMouseClickedPositionX = x;
                lastMouseClickedPositionY = y;
            }
        };

        stage.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandler);

        AnimationTimer animationTimer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                // System.out.println("animationTimer invoked at " + currentNanoTime);
                for (int i = 0; i < blocks.length; i++) {
                    for (int j = 0; j < blocks[i].length; j++) {
                        MineGameEngine.Block curr = blocks[i][j];
                        Rectangle rectangle = rectangles[i][j];
                        if (curr.isMarkedAsMine) {
                            rectangle.setFill(Color.YELLOW);
                        } else if (!curr.isRevealed) {
                            rectangle.setFill(Color.GRAY);

                        } else {
                            if (curr.type == MineGameEngine.Type.MINE) {
                                rectangle.setFill(Color.RED);
                            } else {
                                rectangle.setFill(Color.GREEN);
                                if (curr.type.getValue() > 0) {
                                    texts[i][j].setText(String.valueOf(curr.type.getValue()));
                                }
                            }
                        }
                    }
                }

                if (game.minesLeft == 1) {
                    countLabel.setText("There is 1 mine left!");
                } else {
                    countLabel.setText("There are " + game.minesLeft + " mines left!");
                }

                timerLabel.setText("Remaining time: " + String.valueOf(remainingTime));

                if (gameOver) {
                    return;
                }

                if (!mouseClickProcessed) {
                    mouseClickProcessed = true;
                    int c = (int) (lastMouseClickedPositionX / squareSize);
                    int r = (int) ((lastMouseClickedPositionY - hBox.getHeight()) / squareSize);
                    System.out.println("c: " + c + ", r: " + r);
                    if (lastMouseClickedButton == 0) {
                        game.flip(r, c);
                    } else if (lastMouseClickedButton == 1) {
                        if (game.blocks[r][c].isMarkedAsMine) {
                            game.removeMarkAsMine(r, c);
                        } else {
                            game.markAsMine(r, c);
                        }
                    }
                    MineGameEngine.GameStatus ret = game.getGameStatus();
                    if (ret == MineGameEngine.GameStatus.WON) {
                        System.out.println("You won!!");
                        gameOver = true;
                        statusLabel.setText("You won!");
                    } else if (ret == MineGameEngine.GameStatus.LOST) {
                        System.out.println("You lost!!");
                        gameOver = true;
                        statusLabel.setText("You lost!");
                    }
                }
                if (remainingTime <= 0) {
                    gameOver = true;
                    statusLabel.setText("Time is over, you lost!");
                }
            }
        };
        animationTimer.start();
        System.out.println("Start method reaches end.");
    }

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        Runnable run = new Runnable() {
            @Override
            public void run() {
                if (!gameOver) {
                    remainingTime --;
                }
            }
        };

        executorService.scheduleAtFixedRate(
                run,
                0,
                1,
                TimeUnit.SECONDS);

        launch(args);
    }

}
