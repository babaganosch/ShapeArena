import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * This is the main Client class, the interface between the player and the game.
 * When you start the client you'll be prompted to enter an IP address. If you
 * press Cancel you will connect to localhost, else you will connect to what you
 * have entered.
 * 
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class Client extends JFrame
		implements Runnable, KeyListener, ComponentListener, MouseListener, MouseMotionListener {

	// ------------- DEBUG --------------
	private boolean debug = false;
	// ----------------------------------

	// Global
	private static final long serialVersionUID = -7317687704845378703L;
	private Random random = new Random();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Canvas canvas;
	private JPanel topBar;
	private Socket socket;
	// Changed the color-theme to a less shrieking color, left the old color-codes
	// though.
	private Color topBarColor = new Color(50, 45, 45); // (249, 65, 32) <- Orange
	private int screenWidth = 720;
	private int screenHeight = 480;
	private int slowDownSending = 2;
	private int lastX, lastY;
	private boolean fullScreen = false;
	private boolean[] direction = new boolean[4];
	private static boolean disconnected = false;

	// Constants
	private static final int roomSize = 1000;
	private static final int maxFoods = 20;
	private static final int maxScore = 200;
	private static final float maxAcceleration = 0.45f;
	private static final float lowestFriction = 0.3f;
	private static final int X = 0, Y = 1;
	private static final int RIGHT = 0, LEFT = 1, DOWN = 2, UP = 3;
	private static final int maxSpeed = 4;

	// Player related
	private int playerID;
	private int playerCoordinates[] = new int[2];
	private int currentMaxSpeed = maxSpeed;
	private int invincibleTimer = 0;
	private int score = 15 + random.nextInt(10);
	private int shrinkTimer = 100;
	private int growth = 0;
	private float acceleration = maxAcceleration;
	private float friction = lowestFriction;
	private float currentSpeed[] = new float[4];
	private boolean adjustPosition;
	private boolean collided = false;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private HashMap<Integer, int[]> playerList = new HashMap<Integer, int[]>();
	private Food[] tempFood = new Food[maxFoods];

	/**
	 * Creates the Client object and tries to connect to the IP it's given. This is
	 * where all the initialization gets done for the player before we enter the
	 * server.
	 * 
	 * @param serverIP
	 *            The IP the Client will try to connect to.
	 */
	public Client(String serverIP) {

		// Setup the JFrame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(screenWidth, screenHeight);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setUndecorated(true);

		addComponentListener(this);

		// Create the canvas
		canvas = new Canvas(screenWidth, screenHeight, roomSize);
		add(canvas, BorderLayout.CENTER);

		canvas.repaint();

		// Create the top bar for moving the frame around etc.
		topBar = new JPanel(new BorderLayout());
		topBar.addMouseListener(this);
		topBar.addMouseMotionListener(this);

		JLabel close = new JLabel("×");
		close.setBorder(new EmptyBorder(0, 0, 0, 20));
		close.setFont(new Font("Arial", Font.BOLD, 20));
		close.setForeground(Color.DARK_GRAY);
		close.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				System.exit(0);
			}

			public void mouseEntered(MouseEvent e) {
				close.setForeground(Color.WHITE);
			}

			public void mouseExited(MouseEvent e) {
				close.setForeground(Color.DARK_GRAY);
			}
		});

		topBar.setBackground(topBarColor);
		topBar.add(close, BorderLayout.EAST);
		this.add(topBar, BorderLayout.NORTH);

		try {

			// Connect to the server
			int serverPort = Integer.parseInt("11100");

			this.socket = new Socket(serverIP, serverPort);
			System.out.println("Connection successful.");

			// Initialize the Input/Output streams
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());

			// Wait for a InitializationPacket from ClientHandler to initialize playerID
			boolean gotPlayerID = false;
			while (!gotPlayerID) {
				Packet packet = null;
				// Receive packet with playerID from ClientHandler.
				packet = (Packet) in.readObject();
				if (packet instanceof PlayerInitializationPacket) {
					playerID = ((PlayerInitializationPacket) packet).getId();
					canvas.setPlayerID(playerID);
					gotPlayerID = true;
				}
			}

			// Put the player somewhere random on the map
			playerCoordinates[X] = random.nextInt(roomSize - 25);
			playerCoordinates[Y] = random.nextInt(roomSize - 25);

			// Initialization of adjustPosition depends on the start value of score being
			// even or odd,
			// to correctly synchronize adjusting the player position when shrinking
			if (score % 2 == 0) {
				adjustPosition = true;
			} else {
				adjustPosition = false;
			}

			// Initialize InputReader and start its thread.
			InputReader input = new InputReader(in, this);
			Thread readsInput = new Thread(input);
			readsInput.start();

			// Start the Client thread.
			Thread thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			System.out.println("Unable to start client");
		}

		setVisible(true);
		repaint();
	}

	/**
	 * First creates the connection IP prompt, then creates the Client and give it
	 * the IP we've entered.
	 * 
	 * @param args
	 *            Not used.
	 */
	public static void main(String[] args) {
		JFrame ipFrame = new JFrame();
		String serverIP = JOptionPane.showInputDialog(ipFrame, "IP adress:");

		if (serverIP == null) {
			serverIP = "localhost";
		}
		new Client(serverIP);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				disconnected = true;
			}
		}, "Shutdown-thread"));
	}

	/**
	 * Getter for the debug boolean
	 * 
	 * @return Returns either true or false whether debug is true or false.
	 */
	public boolean getDebug() {
		return debug;
	}

	/**
	 * Getter for the roomSize variable.
	 * 
	 * @return Returns an int representing the current room size of the map.
	 */
	public int getRoomSize() {
		return roomSize;
	}

	/**
	 * Getter for the maxFoods variable.
	 * 
	 * @return Returns an int representing how many Food objects the server should
	 *         have.
	 */
	public int getMaxFoods() {
		return maxFoods;
	}

	/**
	 * Updates the HashMap playerList with a player.
	 * 
	 * @param inPlayerId
	 *            The unique ID of the player.
	 * @param inX
	 *            The X coordinate of the player.
	 * @param inY
	 *            The Y coordinate of the player.
	 * @param inScore
	 *            The score of the player.
	 */
	public synchronized void updatePlayerList(int inPlayerId, int inX, int inY, int inScore) {
		int[] info = new int[3];
		info[0] = inX;
		info[1] = inY;
		info[2] = inScore;
		if (inScore != 0) {
			playerList.put(inPlayerId, info);
		} else {
			playerList.remove(inPlayerId);
		}
	}

	/**
	 * Updates the HashMap foodList with a new HashMap.
	 * 
	 * @param inFoodList
	 *            The new HashMap.
	 */
	public HashMap<Integer, Food> getFoodList() {
		return foodList;
	}

	/**
	 * Clamps the input value between a minimum value and a maximum value.
	 * 
	 * @param value
	 *            The value we would like to clamp.
	 * @param max
	 *            The maximum value.
	 * @param min
	 *            The minimum value.
	 * @return Returns an int of the clamped value.
	 */
	public int clamp(int value, int max, int min) {
		// Clamp value between min and max value.
		if (value <= min) {
			return min;
		} else if (value >= max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * Clamps the input value between a minimum value and a maximum value.
	 * 
	 * @param value
	 *            The value we would like to clamp.
	 * @param max
	 *            The maximum value.
	 * @param min
	 *            The minimum value.
	 * @return Returns an float of the clamped value.
	 */
	public float clampFloat(float value, float max, float min) {
		// Clamp value between min and max value.
		if (value <= min) {
			return min;
		} else if (value >= max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * Updates the canvas foodList with the clients foodList.
	 */
	public void updateCanvasFood() {
		canvas.setFood(foodList);
	}

	/**
	 * Sends out a PlayerPacket on the output stream containing information about
	 * our player, such as playerID, X coordinate, Y coordinate and Score. If we're
	 * disconnecting, send out a packet with 0 score.
	 */
	public void sendPlayerPackage() {
		try {
			if (!disconnected) {
				out.writeObject(new PlayerPacket(playerID, playerCoordinates[X], playerCoordinates[Y], score));
				out.flush();
			} else {
				out.writeObject(new PlayerPacket(playerID, playerCoordinates[X], playerCoordinates[Y], 0));
				out.flush();
				playerList.remove(playerID);
			}

		} catch (Exception e) {
			System.out.println("Error sending Coordinates.");
		}
	}

	/**
	 * Puts the thread in sleep for a given time.
	 * 
	 * @param time
	 *            How long the thread should sleep.
	 */
	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The movement of the player. First it checks what direction we should be
	 * moving and accelerates or deaccelerates in that direction. Finally it moves
	 * the player.
	 */
	public void move() {
		// Acceleration and friction
		if (direction[RIGHT] == true) {
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] - friction, currentMaxSpeed, 0);
		}
		if (direction[LEFT] == true) {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] - friction, currentMaxSpeed, 0);
		}
		if (direction[DOWN] == true) {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] - friction, currentMaxSpeed, 0);
		}
		if (direction[UP] == true) {
			currentSpeed[UP] = clampFloat(currentSpeed[UP] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[UP] = clampFloat(currentSpeed[UP] - friction, currentMaxSpeed, 0);
		}

		// Move the player!
		playerCoordinates[X] = clamp(playerCoordinates[X] + (int) currentSpeed[RIGHT] - (int) currentSpeed[LEFT],
				roomSize - score, 0);
		playerCoordinates[Y] = clamp(playerCoordinates[Y] + (int) currentSpeed[DOWN] - (int) currentSpeed[UP],
				roomSize - score, 0);
	}

	/**
	 * Keeps the player shrinking if the player have a score larger than 25.
	 * Corrects the players position when it shrinks and also changes the max speed,
	 * acceleration and friction of the player depending on score.
	 */
	public void shrink() {
		if (shrinkTimer <= 0) {
			if (score > 25) {
				score--;

				if (adjustPosition) {
					playerCoordinates[X] += 1;
					playerCoordinates[Y] += 1;
					adjustPosition = false;
				} else {
					adjustPosition = true;
				}

			}

			if (score >= 150) {
				shrinkTimer = 40;
				currentMaxSpeed = maxSpeed - 2;
				acceleration = maxAcceleration + 0.4f;
				friction = lowestFriction - 0.2f;
			} else if (score >= 100) {
				shrinkTimer = 55;
				currentMaxSpeed = maxSpeed - 2;
				acceleration = maxAcceleration + 0.2f;
				friction = lowestFriction - 0.2f;
			} else if (score >= 50) {
				shrinkTimer = 65;
				currentMaxSpeed = maxSpeed - 1;
				;
				acceleration = maxAcceleration + 0.1f;
				friction = lowestFriction - 0.1f;
			} else {
				shrinkTimer = 85;
				currentMaxSpeed = maxSpeed;
				acceleration = maxAcceleration;
				friction = lowestFriction;
			}
		}
		shrinkTimer--;
	}

	/**
	 * Grows smooth if the player is supposed to grow more than one point of score.
	 * Also corrects the position.
	 */
	public void grow() {
		if (growth > 0) {
			score += 1;
			growth -= 1;
		}
		if (score % 2 == 0) {
			adjustPosition = true;
		} else {
			adjustPosition = false;
		}
	}

	/**
	 * Checks for player collision with Food objects from the foodList. If the
	 * player eats a Food object, create a new Food with the same index as the old
	 * and add it to the foodList. Then send out the new Food object on the output
	 * stream.
	 * 
	 * @param showDebug
	 *            Shows debug messages if this boolean is true.
	 */
	public void checkFoodCollision(Boolean showDebug) {
		Boolean collidedFood = false;
		// Loop through every Food and get their x and y coordinate, then check for
		// collision.
		for (int i = 0; i < maxFoods; i++) {
			tempFood[i] = foodList.get(i);

			if (tempFood[i] != null) {

				int tempFoodX = tempFood[i].getX();
				int tempFoodY = tempFood[i].getY();

				if (playerCoordinates[X] <= tempFoodX && playerCoordinates[X] >= (tempFoodX - score)) {
					if (playerCoordinates[Y] <= tempFoodY && playerCoordinates[Y] >= (tempFoodY - score)) {
						if (collidedFood == false) {

							// Print out debug message
							if (showDebug) {
								System.out.println("Client " + playerID + " collided with food index " + i);
							}

							// Make sure we don't collide again
							collidedFood = true;

							// Increment score
							score++;

							// Change adjustPosition
							if (adjustPosition) {
								adjustPosition = false;
							} else {
								adjustPosition = true;
							}

							try {
								// Print out debug message
								if (showDebug) {
									System.out.println("Client " + playerID + " sending Food " + i);
								}

								// Create a new Food object with the same index as the one we've collided with.
								// Add the Food object to our foodList so we don't experience any graphical
								// delay
								// Toolkit.getDefaultToolkit().beep();
								Food tempFood = new Food(i, random.nextInt(roomSize - 5), random.nextInt(roomSize - 5));
								foodList.put(i, tempFood);

								// Send the new Food object to the server
								out.writeObject(tempFood);
								out.flush();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		collidedFood = false;
	}

	/**
	 * Checks for player collision with an other player. All collision in Shape
	 * Arena is handled client side. If the player collides with an other player the
	 * method checks which player has the highest score. If you have the higher
	 * score you gain 30% of the other players score. If you have the lower score
	 * you reset your own score to 25, and also teleports to a random location on
	 * the map. When the player have collided we gain invincibility for a while, to
	 * prevent the player from eating the other player more than once.
	 */
	public void checkPlayerCollision() {
		for (Integer i : playerList.keySet()) {
			if (i != null) {
				int otherPlayerId = i;
				int otherPlayerX = playerList.get(i)[0];
				int otherPlayerY = playerList.get(i)[1];
				int otherPlayerScore = playerList.get(i)[2];

				if (playerID != otherPlayerId) {
					if (playerCoordinates[X] <= (otherPlayerX + otherPlayerScore)
							&& playerCoordinates[X] >= (otherPlayerX - score)
							&& playerCoordinates[Y] <= (otherPlayerY + otherPlayerScore)
							&& playerCoordinates[Y] >= (otherPlayerY - score)) {

						// Other player eats us
						if (otherPlayerScore >= score) {
							score = 25;
							int newX = random.nextInt(roomSize - 25);
							int newY = random.nextInt(roomSize - 25);
							playerCoordinates[X] = newX;
							playerCoordinates[Y] = newY;
							canvas.died();
						} else {
							// We eat other player
							growth += otherPlayerScore * 0.3;
						}

						// We've collided
						invincible();
					}
				}
			}
		}
	}

	/**
	 * Handles the invincibility timer. If it is not 0, don't check for player
	 * collision.
	 */
	public void handleCollision() {
		// Check for collision if we're not invincible
		if (!collided) {
			checkPlayerCollision();
		} else {
			invincibleTimer--;
		}

		// Reset invincibleTimer
		if (invincibleTimer <= 0) {
			invincibleTimer = 0;
			collided = false;
		}
	}

	/**
	 * Turns the player invincible.
	 */
	public void invincible() {
		collided = true;
		invincibleTimer = 60;
	}

	// KeyHandler:
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			direction[LEFT] = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			direction[UP] = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			direction[RIGHT] = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			direction[DOWN] = true;
		}

	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			direction[LEFT] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			direction[UP] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			direction[RIGHT] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			direction[DOWN] = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_F) {
			if (fullScreen) {
				setExtendedState(JFrame.NORMAL);
				topBar.setVisible(true);
				fullScreen = false;
			} else {
				setExtendedState(JFrame.MAXIMIZED_BOTH);
				topBar.setVisible(false);
				fullScreen = true;
			}
		}
	}

	public void keyTyped(KeyEvent arg0) {
	}

	public void run() {
		while (true) {

			// Movement
			move();

			// Update player
			if (slowDownSending <= 0) {
				sendPlayerPackage();
				slowDownSending = 2;
			} else {
				slowDownSending--;
			}

			// Check for Food collision
			if (score < maxScore) {
				checkFoodCollision(debug);
			}

			// Shrink
			shrink();

			// Grow
			grow();

			// Update ourself in the playerList
			updatePlayerList(playerID, playerCoordinates[X], playerCoordinates[Y], score);

			// Update canvas foodList
			updateCanvasFood();

			// Check collision
			handleCollision();

			// Update
			canvas.updatePlayerList(playerList);
			canvas.setSpeed(currentMaxSpeed);
			canvas.setInvincibleTimer(invincibleTimer);
			canvas.repaint();

			// Loop with this delay
			// 16ms = about 60 FPS, 32 = 30 FPS
			sleep(16);
		}
	}

	public void componentResized(ComponentEvent e) {
		int width = this.getWidth();
		int height = this.getHeight();

		this.screenWidth = width;
		this.screenHeight = height;
		canvas.setScreen(width, height);
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if ((JPanel) e.getSource() == topBar) {
			int x = e.getXOnScreen();
			int y = e.getYOnScreen();
			setLocation(getLocationOnScreen().x + x - lastX, getLocationOnScreen().y + y - lastY);
			lastX = x;
			lastY = y;
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if ((JPanel) e.getSource() == topBar) {
			lastX = e.getXOnScreen();
			lastY = e.getYOnScreen();
		}
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}

/**
 * This class handles all the reading from the input stream. This is where we
 * receive all the information from the server.
 * 
 * @author Hasse Aro
 * @version 2018-03-xx
 */
class InputReader implements Runnable {

	public ObjectInputStream in;
	Client client;

	/**
	 * Creates the InputReader.
	 * 
	 * @param in
	 *            The Object Input Stream it should listen on.
	 * @param client
	 *            The Client which it should give the information to.
	 */
	public InputReader(ObjectInputStream in, Client client) {
		this.in = in;
		this.client = client;
	}

	/**
	 * Unpack the packet we've received as a PlayerPacket and give the information
	 * to the Client.
	 * 
	 * @param packet
	 *            The packet to handle.
	 * @param showDebug
	 *            Print debug messages if this boolean is true.
	 */
	public void handlePlayerPacket(Object packet, Boolean showDebug) {

		// Handle the packet
		PlayerPacket temp = (PlayerPacket) packet;
		int playerID = temp.getId();
		int x = temp.getX();
		int y = temp.getY();
		int score = temp.getScore();

		// Print out what's received
		if (showDebug == true) {
			System.out.println("Packet Recieved: PLAYER");
			System.out.println("playerID: " + playerID);
			System.out.println("x: " + x);
			System.out.println("y: " + y);
			System.out.println("score: " + score + "\n");
		}

		// Only update player from the packet
		client.updatePlayerList(playerID, x, y, score);

	}

	/**
	 * Unpack the packet we've received as a Food array and give the information to
	 * the Client.
	 * 
	 * @param packet
	 *            The packet to handle.
	 * @param showDebug
	 *            Print debug messages if this boolean is true.
	 */
	public void handleFoodPacket(Object packet, Boolean showDebug) {

		// Unpack the packet
		Food[] temp = (Food[]) packet;
		
		for (int i = 0; i < temp.length; i++) {
			client.getFoodList().put(temp[i].getId(), temp[i]);
		}

	}

	public void run() {

		while (true) {
			try {

				// Receive packet and store it.
				Object packet = null;
				try {
					packet = (Object) in.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Handle packets.
				if (packet instanceof PlayerPacket) {

					handlePlayerPacket(packet, client.getDebug());

				} else if (packet instanceof Food[]) {

					handleFoodPacket(packet, client.getDebug());

				}

			} catch (Exception e) {
				System.out.println("Could not receive/forward packet.");
			}
		}
	}
}
