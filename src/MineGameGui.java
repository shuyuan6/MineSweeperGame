import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
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
        // create the button to show won or lost
        Label lblstatus = new Label();
        lblstatus.setText("Come on!");

        MineGameEngine game = new MineGameEngine();
        game.init(20, 20, 5);

        //create the button to show mine count
        Label lblcount = new Label();
        lblcount.setText("There are " + game.minesLeft + " mines left!");

        //Add the button to a layout pane
        BorderPane paneprimary =  new BorderPane();
        BorderPane panesecondary = new BorderPane();





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

        Group group = new Group();
        Scene scene = new Scene(paneprimary, 600, 700);
        stage.setTitle("MineGame");
        stage.setScene(scene);
        stage.show();

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

                panesecondary.getChildren().add(rectangle);
                panesecondary.getChildren().add(text);
            }
        }

        paneprimary.setTop(lblcount);
        paneprimary.setBottom(lblstatus);
        paneprimary.setCenter(panesecondary);

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
                    lblcount.setText("There is 1 mine left!");
                } else {
                    lblcount.setText("There are " + game.minesLeft + " mines left!");
                }

                if (gameOver) {
                    return;
                }

                if (!mouseClickProcessed) {
                    mouseClickProcessed = true;
                    int c = (int) (lastMouseClickedPositionX / squareSize);
                    int r = (int) ((lastMouseClickedPositionY - lblcount.getHeight()) / squareSize);
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
                        lblstatus.setText("You won!");
                    } else if (ret == 2) {
                        System.out.println("You lost!!");
                        gameOver = true;
                        lblstatus.setText("You lost!");
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
