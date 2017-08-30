/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class Breakout extends GraphicsProgram {

/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

/** Separation between bricks */
	private static final int BRICK_SEP = 4;

/** Width of a brick */
	private static final int BRICK_WIDTH =
	  (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

/** Number of turns */
	private static final int NTURNS = 3;
	
/** Frame delay for ball motion */
	private static final int BALL_MOTION_DELAY = 10;

/* Method: run() */
/** Runs the Breakout program. */
	public void run() {
		setUpGameBoard ();
		startGame();
	}

/** Sets up game board */
	private void setUpGameBoard() {
		lives = NTURNS - 1;
		brickTotal = NBRICKS_PER_ROW * NBRICK_ROWS;
		paddleHits = 0;
		gameScore = 0.0;
		buildBricks();
		buildPaddle();
		refreshScoreBoard();
		resetBall();
		addMouseListeners();
		addKeyListeners();
	}

/** Starts game */
	private void startGame() {
		moveBall();
	}

/** Assembles rows of bricks by color */
	private void buildBricks() {
		brickRowY = BRICK_Y_OFFSET;
		makeBrickRows(Color.RED);
		makeBrickRows(Color.ORANGE);
		makeBrickRows(Color.YELLOW);
		makeBrickRows(Color.GREEN);
		makeBrickRows(Color.CYAN);
	}

/** Creates paddle for game board */
	private void buildPaddle() {
		paddleX = (WIDTH - PADDLE_WIDTH) / 2;
		paddle.setFilled(true);
		paddle.setColor(Color.BLACK);
		add(paddle);
	}
	
/** Creates and subsequently refreshes scoreboard beneath paddle */
	private void refreshScoreBoard() {
		score = new GLabel("" + (int)gameScore);
		score.setLocation((WIDTH - score.getWidth()) / 2, HEIGHT - (PADDLE_Y_OFFSET - score.getAscent()) / 2);
		add(score);
	}

/** Places ball in center of the board and sets ball velocity */
	private void resetBall() {
		ballX = WIDTH / 2 - BALL_RADIUS;
		ballY = HEIGHT / 2 - BALL_RADIUS;
		ball.setFilled(true);
		ball.setColor(Color.BLACK);
		add(ball);
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5)) vx = -vx;
		vy = 3.0;
	}

/** Constructs two rows of bricks for each color and then advances offset from top wall for next two rows of bricks */
	private void makeBrickRows(Color COLOR) {
		double brickXOffset = (WIDTH - (((NBRICKS_PER_ROW - 1) * BRICK_SEP) + (NBRICKS_PER_ROW * BRICK_WIDTH))) / 2;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < NBRICKS_PER_ROW; j++) {
				GRect rect = new GRect(brickXOffset + (BRICK_WIDTH + BRICK_SEP) * j, brickRowY + (BRICK_HEIGHT + BRICK_SEP) * i, BRICK_WIDTH, BRICK_HEIGHT);
				rect.setFilled(true);
				rect.setColor(COLOR);
				add(rect);
			}
		}
		brickRowY += (BRICK_HEIGHT + BRICK_SEP) * 2;
	}

/** Sets ball in motion and dictates interaction with walls, bricks, and paddle */
	private void moveBall() {
		while(true)	{
			ballX += vx;
			ballY += vy;
			ball.setLocation(ballX, ballY);
			pause(BALL_MOTION_DELAY);
			if (pathUnobstructed() == false) break;
		}
		bounceClip.play();
		
/** Reverses x-vector of ball when it hits one of the lateral walls */
		if (((ballX + BALL_RADIUS * 2) >= WIDTH) || (ballX <= 0)) {
			vx = -vx;
			moveBall();
		}

/** Reverses y-vector of ball when it hits top wall */
		if (ballY <= 0) {
			vy = -vy;
			moveBall();
		}

/** Reverses y-vector of ball when it hits a paddle or a brick; in the latter case, the brick is also removed and points are added to game score*/
		if (getCollidingObject(ballX, ballY) != null) {
			GObject collider = (getCollidingObject(ballX, ballY));
			if (collider == paddle) {
				if (colliderTest == 1) {
					if ((ballY + 2 * BALL_RADIUS) < (HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT + vy + 1)) {
						vy = -vy;
					} else {
						vy = -vy;
						vx = -vx;
					}
				} else {
					vx = -vx;
				}
				paddleHits++;
				if (paddleHits % 7 == 0) {
					vx *= 2;
				}
				moveBall();
			} else {
				remove(collider);
				brickTotal--;
				if (collider.getColor() == Color.CYAN) {
					gameScore += 1.0;
				}
				if (collider.getColor() == Color.GREEN) {
					gameScore += 4.0;
				}
				if (collider.getColor() == Color.YELLOW) {
					gameScore += 7.0;
				}
				if (collider.getColor() == Color.ORANGE) {
					gameScore += 10.0;
				}
				if (collider.getColor() == Color.RED) {
					gameScore += 11.33;
				}
				remove(score);
				refreshScoreBoard();
				if (brickTotal == 0) {
					remove(ball);
					youWin();
				} else {
					vy = -vy;
					moveBall();
				}
			}
		}
		
/** Resets ball and either
 * 	a) moves to next turn when there are still turns left or
 * 	b) ends game and displays "GAME OVER" 
 * 	when ball hits bottom wall */
		if ((ballY + BALL_RADIUS * 2) >= HEIGHT) {
			if (lives > 0) {
				lives--;
				remove(ball);
				resetBall();
				moveBall();
			} else {
				remove(ball);
				gameOver();
			}
		}

	}

/** Variable determining brick row offset for each color set */
	private double brickRowY;

/** Variables determining x-vector and y-vector of ball velocity, respectively */
	private double vx, vy;

/** Generates random number for starting velocity of ball */
	private RandomGenerator rgen = RandomGenerator.getInstance();

/** Variable determining current position of ball on x-axis */
	private double ballX;
	
/** Variable determining current position of ball on y-axis */	
	private double ballY;

/** Object defining ball */	
	private GOval ball = new GOval(ballX, ballY, BALL_RADIUS * 2, BALL_RADIUS * 2);

/** Variable determining current amount of turns left in play */
	private int lives;

/** Total number of bricks on the game board at any given time */
	private int brickTotal;

/** Variable determining current position of paddle on the x-axis */
	private double paddleX;

/** Object defining paddle */
	private GRect paddle = new GRect(paddleX, HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT, PADDLE_WIDTH, PADDLE_HEIGHT);

/** Prevents error when top two test points of ball's containing rectangle hit paddle, deflecting ball along the x-axis instead of the y-axis */
	private int colliderTest;
	
/** Variable implemented to increase acceleration of ball every 7th time it hits the paddle */
	private int paddleHits;

/** Variable for game score */
	private double gameScore;
	
/** Label for game score */
	private GLabel score;
	
/** Test to determine if ball's path is obstructed */
	private boolean pathUnobstructed() {
		if (((ballX + BALL_RADIUS * 2) < WIDTH) && ((ballY + BALL_RADIUS * 2) < HEIGHT) && (ballX > 0) && (ballY > 0) && (getCollidingObject(ballX, ballY) == null)) {
			return true;
		} else {
			return false;
		}
	}

/** Test to determine if ball has collided with either a brick or the paddle */
	private GObject getCollidingObject(double x, double y) {
		if(getElementAt(x, y) == null) {
			if(getElementAt(x + 2 * BALL_RADIUS, y) == null) {
				if(getElementAt(x + 2 * BALL_RADIUS, y + BALL_RADIUS * 2) == null) {
					if(getElementAt(x, y + BALL_RADIUS * 2) == null) {
						return null;
					} else {
						colliderTest = 1;
						return (getElementAt(x, y + 2 * BALL_RADIUS));
					}
				} else {
					colliderTest = 1;
					return (getElementAt(x + 2 * BALL_RADIUS, y + 2 * BALL_RADIUS));
				}
			} else {
				colliderTest = 0;
				return (getElementAt(x + 2 * BALL_RADIUS, y));
			}
		} else {
			colliderTest = 0;
			return (getElementAt(x, y));
		}
	}

/** Displays "GAME OVER" once user has run out of turns */
	private void gameOver() {
		GLabel gameOver = new GLabel("GO QUEEF ON A CHODE");
		gameOver.setLocation((WIDTH - gameOver.getWidth()) / 2, (HEIGHT - gameOver.getAscent()) / 2);
		add(gameOver);
	}
	
/** Displays "YOU WIN" once user has eliminated all bricks */
	private void youWin() {
		GLabel youWin = new GLabel("YOU WIN!!!!!!!");
		youWin.setLocation((WIDTH - youWin.getWidth()) / 2, (HEIGHT - youWin.getAscent()) / 2);
		add(youWin);
	}

/** Synchronizes motion of paddle on the x-axis with motion of mouse on the x-axis */
	public void mouseMoved(MouseEvent e) {
		paddleX = e.getX() - PADDLE_WIDTH / 2;
		paddle.setLocation(paddleX, HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
	}
	
/** Bounce sound */
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");
}
