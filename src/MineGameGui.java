import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
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

public class MineGameGui extends Application {

    double lastMouseClickedPositionX = 0;
    double lastMouseClickedPositionY = 0;
    // 0 means left button, 1 means right button
    int lastMouseClickedButton = 0;
    boolean mouseClickProcessed = true;
    final double squareSize = 30d;
    boolean gameOver = false;

    @Override
    public void start(Stage stage) {

        MineGameEngine game = new MineGameEngine();
        game.init(20, 20, 5);

        // create a label to show won or lost
        Label statusLabel = new Label();
        statusLabel.setText("Come on!");

        // create a label to show mine count
        Label countLabel = new Label();
        countLabel.setText("There are " + game.minesLeft + " mines left!");
        countLabel.setPrefWidth(300);

        // create a button to restart a game
        Button restart = new Button();
        restart.setText("Restart the game!");

        //Add the labels and button to a layout pane
        Pane mineField = new Pane();
        mineField.setPrefSize(600, 600);


        HBox hBox = new HBox(countLabel, restart);
        //hBox.setPrefHeight(100);

        VBox vBox = new VBox();
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

        // MineGame game = new MineGame();
        // game.init(20, 20, 30);

        MineGameEngine.Block[][] blocks = game.getState();
        Rectangle[][] rectangles = new Rectangle[blocks.length][blocks[0].length];
        Text[][] texts = new Text[blocks.length][blocks[0].length];

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

        AnimationTimer animationTimer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                //System.out.println("animationTimer invoked at " + currentNanoTime);
                for (int i = 0; i < blocks.length; i++) {
                    for (int j = 0; j < blocks[i].length; j++) {
                        MineGameEngine.Block curr = blocks[i][j];
                        Rectangle rectangle = rectangles[i][j];
                        if (curr.isMarkedAsMine) {
                            rectangle.setFill(Color.YELLOW);
                        } else if (!curr.isRevealed) {
                            rectangle.setFill(Color.GRAY);

                        } else {
                            if (curr.type == -1) {
                                rectangle.setFill(Color.RED);
                            } else {
                                rectangle.setFill(Color.GREEN);
                                if (curr.type > 0) {
                                    texts[i][j].setText(String.valueOf(curr.type));
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
                    int ret = game.gameOver();
                    if (ret == 1) {
                        System.out.println("You won!!");
                        gameOver = true;
                        statusLabel.setText("You won!");
                    } else if (ret == 2) {
                        System.out.println("You lost!!");
                        gameOver = true;
                        statusLabel.setText("You lost!");
                    }
                }
            }
        };
        animationTimer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
