package com.carthagegg.controllers.front;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeGameController {

    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    @FXML private Label highScoreLabel;
    @FXML private VBox gameOverPane;
    @FXML private VBox startPane;
    @FXML private SidebarController sidebarController;

    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int ROWS = HEIGHT / TILE_SIZE;
    private static final int COLS = WIDTH / TILE_SIZE;

    private List<Point> snake = new ArrayList<>();
    private Point food;
    private Direction direction = Direction.RIGHT;
    private boolean gameOver = false;
    private int score = 0;
    private int highScore = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
        if (sidebarController != null) {
            sidebarController.setActiveItem("snake");
        }
        
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        drawBackground(gc);

        // Handle key presses
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if ((code == KeyCode.UP || code == KeyCode.W) && direction != Direction.DOWN) direction = Direction.UP;
            else if ((code == KeyCode.DOWN || code == KeyCode.S) && direction != Direction.UP) direction = Direction.DOWN;
            else if ((code == KeyCode.LEFT || code == KeyCode.A) && direction != Direction.RIGHT) direction = Direction.LEFT;
            else if ((code == KeyCode.RIGHT || code == KeyCode.D) && direction != Direction.LEFT) direction = Direction.RIGHT;
        });
    }

    @FXML private void moveUp() { if (direction != Direction.DOWN) direction = Direction.UP; gameCanvas.requestFocus(); }
    @FXML private void moveDown() { if (direction != Direction.UP) direction = Direction.DOWN; gameCanvas.requestFocus(); }
    @FXML private void moveLeft() { if (direction != Direction.RIGHT) direction = Direction.LEFT; gameCanvas.requestFocus(); }
    @FXML private void moveRight() { if (direction != Direction.LEFT) direction = Direction.RIGHT; gameCanvas.requestFocus(); }

    @FXML
    private void startGame() {
        startPane.setVisible(false);
        gameOverPane.setVisible(false);
        snake.clear();
        snake.add(new Point(COLS / 2, ROWS / 2));
        snake.add(new Point(COLS / 2 - 1, ROWS / 2));
        snake.add(new Point(COLS / 2 - 2, ROWS / 2));
        
        direction = Direction.RIGHT;
        score = 0;
        scoreLabel.setText("0");
        gameOver = false;
        spawnFood();

        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> runGame()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        
        gameCanvas.requestFocus();
    }

    private void runGame() {
        if (gameOver) return;

        update();
        draw();
    }

    private void update() {
        Point head = snake.get(0);
        int nextX = head.x;
        int nextY = head.y;

        switch (direction) {
            case UP -> nextY--;
            case DOWN -> nextY++;
            case LEFT -> nextX--;
            case RIGHT -> nextX++;
        }

        // Check wall collision
        if (nextX < 0 || nextX >= COLS || nextY < 0 || nextY >= ROWS) {
            endGame();
            return;
        }

        // Check self collision
        for (Point p : snake) {
            if (p.x == nextX && p.y == nextY) {
                endGame();
                return;
            }
        }

        Point nextHead = new Point(nextX, nextY);
        snake.add(0, nextHead);

        // Check food collision
        if (nextX == food.x && nextY == food.y) {
            score += 10;
            scoreLabel.setText(String.valueOf(score));
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void draw() {
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        drawBackground(gc);

        // Draw food
        gc.setFill(Color.web("#ef4444"));
        gc.fillRoundRect(food.x * TILE_SIZE + 2, food.y * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4, 8, 8);

        // Draw snake
        for (int i = 0; i < snake.size(); i++) {
            Point p = snake.get(i);
            if (i == 0) {
                gc.setFill(Color.web("#FFC107")); // Head
            } else {
                gc.setFill(Color.web("#D4AF37")); // Body
            }
            gc.fillRoundRect(p.x * TILE_SIZE + 1, p.y * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2, 5, 5);
        }
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.web("#0a0a0b"));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Optional: draw grid
        gc.setStroke(Color.web("#141416"));
        gc.setLineWidth(0.5);
        for (int i = 0; i < WIDTH; i += TILE_SIZE) {
            gc.strokeLine(i, 0, i, HEIGHT);
        }
        for (int i = 0; i < HEIGHT; i += TILE_SIZE) {
            gc.strokeLine(0, i, WIDTH, i);
        }
    }

    private void spawnFood() {
        Random rand = new Random();
        while (true) {
            int x = rand.nextInt(COLS);
            int y = rand.nextInt(ROWS);
            boolean onSnake = false;
            for (Point p : snake) {
                if (p.x == x && p.y == y) {
                    onSnake = true;
                    break;
                }
            }
            if (!onSnake) {
                food = new Point(x, y);
                break;
            }
        }
    }

    private void endGame() {
        gameOver = true;
        timeline.stop();
        gameOverPane.setVisible(true);
        if (score > highScore) {
            highScore = score;
            highScoreLabel.setText(String.valueOf(highScore));
        }
    }

    private static class Point {
        int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }
    }

    private enum Direction { UP, DOWN, LEFT, RIGHT }
}
