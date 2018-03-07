package clientSide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
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

import packets.Food;
import packets.Packet;
import packets.PlayerInitializationPacket;
import packets.PlayerPacket;

/**
 * This is the main Client class, the interface between the player and the game.
 * When you start the client you'll be prompted to enter an IP address. If you
 * press Cancel you will connect to localhost, else you will connect to what you
 * have entered.
 * 
 * @author Lars Andersson
 * @version 2018-03-07
 */
public class Client extends JFrame
		implements Runnable, KeyListener, MouseListener, MouseMotionListener, ClientConstants {

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

	private boolean fullScreen = false;
	private boolean[] direction = new boolean[4];
	private static boolean disconnected = false;

	private int intArray[] = new int[11];

	private int playerCoordinates[] = new int[2];
	private float acceleration = maxAcceleration;
	private float friction = lowestFriction;
	private float currentSpeed[] = new float[4];
	private boolean adjustPosition;
	private boolean collided = false;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private HashMap<Integer, int[]> playerList = new HashMap<Integer, int[]>();

	/**
	 * Creates the Client object and tries to connect to the IP it's given. This is
	 * where all the initialization gets done for the player before we enter the
	 * server.
	 * 
	 * @param serverIP
	 *            The IP the Client will try to connect to.
	 */
	public Client(String serverIP) {

		// Initialize intArray
		intArray[currentMaxSpeed] = maxSpeed;
		intArray[invincibilityTimer] = 0;
		intArray[score] = 15 + random.nextInt(10);
		intArray[shrinkTimer] = 100;
		intArray[growth] = 0;
		intArray[screenWidth] = 720;
		intArray[screenHeight] = 480;
		intArray[slowDownSending] = 2;

		// Setup the JFrame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(intArray[screenWidth], intArray[screenHeight]);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setUndecorated(true);

		// Create the canvas
		canvas = new Canvas(intArray[screenWidth], intArray[screenHeight], roomSize);
		add(canvas, BorderLayout.CENTER);

		canvas.repaint();

		// Create the top bar for moving the frame around etc.
		topBar = new JPanel(new BorderLayout());
		topBar.addMouseListener(this);
		topBar.addMouseMotionListener(this);

		JLabel close = new JLabel("×");
		close.setBorder(new EmptyBorder(0, 0, 0, 10));
		close.setFont(new Font("Arial", Font.BOLD, 20));
		close.setForeground(Color.GRAY);
		close.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {		
				System.exit(0);
			}

			public void mouseEntered(MouseEvent e) {
				close.setForeground(Color.WHITE);
			}

			public void mouseExited(MouseEvent e) {
				close.setForeground(Color.GRAY);
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
					intArray[playerID] = ((PlayerInitializationPacket) packet).getId();
					canvas.setPlayerID(intArray[playerID]);
					gotPlayerID = true;
				}
			}

			// Put the player somewhere random on the map
			playerCoordinates[X] = random.nextInt(roomSize - 25);
			playerCoordinates[Y] = random.nextInt(roomSize - 25);

			// Initialization of adjustPosition depends on the start value of score being
			// even or odd,
			// to correctly synchronize adjusting the player position when shrinking
			if (intArray[score] % 2 == 0) {
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
			JOptionPane.showMessageDialog(null, "Could not connect to server");
			System.exit(0);
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
	 * Returns the player ID
	 * @return Returns the player ID as an int
	 */
	public int getPlayerId() {
		return intArray[playerID];
	}

	/**
	 * Sends out a PlayerPacket on the output stream containing information about
	 * our player, such as playerID, X coordinate, Y coordinate and Score. If we're
	 * disconnecting, send out a packet with 0 score.
	 */
	public void sendPlayerPackage() {
		try {
			if (!disconnected) {
				out.writeObject(new PlayerPacket(intArray[playerID], playerCoordinates[X], playerCoordinates[Y],
						intArray[score]));
				out.flush();
			} else {
				out.writeObject(new PlayerPacket(intArray[playerID], playerCoordinates[X], playerCoordinates[Y], 0));
				out.flush();
				playerList.remove(intArray[playerID]);
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
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] + acceleration, intArray[currentMaxSpeed], 0);
		} else {
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] - friction, intArray[currentMaxSpeed], 0);
		}
		if (direction[LEFT] == true) {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] + acceleration, intArray[currentMaxSpeed], 0);
		} else {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] - friction, intArray[currentMaxSpeed], 0);
		}
		if (direction[DOWN] == true) {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] + acceleration, intArray[currentMaxSpeed], 0);
		} else {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] - friction, intArray[currentMaxSpeed], 0);
		}
		if (direction[UP] == true) {
			currentSpeed[UP] = clampFloat(currentSpeed[UP] + acceleration, intArray[currentMaxSpeed], 0);
		} else {
			currentSpeed[UP] = clampFloat(currentSpeed[UP] - friction, intArray[currentMaxSpeed], 0);
		}

		// Move the player!
		playerCoordinates[X] = clamp(playerCoordinates[X] + (int) currentSpeed[RIGHT] - (int) currentSpeed[LEFT],
				roomSize - intArray[score], 0);
		playerCoordinates[Y] = clamp(playerCoordinates[Y] + (int) currentSpeed[DOWN] - (int) currentSpeed[UP],
				roomSize - intArray[score], 0);
	}

	/**
	 * Keeps the player shrinking if the player have a score larger than 25.
	 * Corrects the players position when it shrinks and also changes the max speed,
	 * acceleration and friction of the player depending on score.
	 */
	public void shrink() {
		if (intArray[shrinkTimer] <= 0) {
			if (intArray[score] > 25) {
				intArray[score]--;

				if (adjustPosition) {
					playerCoordinates[X] += 1;
					playerCoordinates[Y] += 1;
					adjustPosition = false;
				} else {
					adjustPosition = true;
				}

			}

			if (intArray[score] >= 150) {
				intArray[shrinkTimer] = 20;
				intArray[currentMaxSpeed] = maxSpeed - 2;
				acceleration = maxAcceleration + 0.4f;
				friction = lowestFriction - 0.2f;
			} else if (intArray[score] >= 100) {
				intArray[shrinkTimer] = 40;
				intArray[currentMaxSpeed] = maxSpeed - 2;
				acceleration = maxAcceleration + 0.2f;
				friction = lowestFriction - 0.2f;
			} else if (intArray[score] >= 50) {
				intArray[shrinkTimer] = 60;
				intArray[currentMaxSpeed] = maxSpeed - 1;
				acceleration = maxAcceleration + 0.1f;
				friction = lowestFriction - 0.1f;
			} else {
				intArray[shrinkTimer] = 75;
				intArray[currentMaxSpeed] = maxSpeed;
				acceleration = maxAcceleration;
				friction = lowestFriction;
			}
		}
		intArray[shrinkTimer]--;
	}

	/**
	 * Grows smooth if the player is supposed to grow more than one point of score.
	 * Also corrects the position.
	 */
	public void grow() {
		if (intArray[growth] > 0) {
			intArray[score] += 1;
			intArray[growth] -= 1;
		}
		if (intArray[score] % 2 == 0) {
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
		for (int i = 0; i < foodList.size(); i++) {
			Food tempFood = foodList.get(i);

			if (tempFood != null) {

				int tempFoodX = tempFood.getX();
				int tempFoodY = tempFood.getY();

				if (playerCoordinates[X] <= tempFoodX && playerCoordinates[X] >= (tempFoodX - intArray[score])) {
					if (playerCoordinates[Y] <= tempFoodY && playerCoordinates[Y] >= (tempFoodY - intArray[score])) {
						if (collidedFood == false) {

							// Print out debug message
							if (showDebug) {
								System.out.println("Client " + intArray[playerID] + " collided with food index " + i);
							}

							// Make sure we don't collide again
							collidedFood = true;

							// Increment score
							intArray[score]++;

							// Change adjustPosition
							if (adjustPosition) {
								adjustPosition = false;
							} else {
								adjustPosition = true;
							}

							try {
								// Print out debug message
								if (showDebug) {
									System.out.println("Client " + intArray[playerID] + " sending Food " + i);
								}

								// Create a new Food object with the same index as the one we've collided with.
								// Add the Food object to our foodList so we don't experience any graphical
								// delay
								// Toolkit.getDefaultToolkit().beep();
								tempFood = new Food(i, random.nextInt(roomSize - 5), random.nextInt(roomSize - 5));
								foodList.put(i, tempFood);

								// Send the new Food object to the server
								out.writeObject(tempFood);
								out.flush();
								
								invincible(5);
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

				if (intArray[playerID] != otherPlayerId) {
					if (playerCoordinates[X] <= (otherPlayerX + otherPlayerScore)
							&& playerCoordinates[X] >= (otherPlayerX - intArray[score])
							&& playerCoordinates[Y] <= (otherPlayerY + otherPlayerScore)
							&& playerCoordinates[Y] >= (otherPlayerY - intArray[score])) {

						// Other player eats us
						if (otherPlayerScore >= intArray[score]) {
							intArray[score] = 25;
							int newX = random.nextInt(roomSize - 25);
							int newY = random.nextInt(roomSize - 25);
							playerCoordinates[X] = newX;
							playerCoordinates[Y] = newY;
							canvas.died();
						} else {
							// We eat other player
							if (intArray[score] < 250) {
								intArray[growth] += otherPlayerScore * 0.3;
							}
						}

						// We've collided
						invincible(10);
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

			// Check for Food collision
			if (intArray[score] < maxScore) {
				checkFoodCollision(debug);
			}

		} else {
			intArray[invincibilityTimer]--;
		}

		// Reset invincibleTimer
		if (intArray[invincibilityTimer] <= 0) {
			intArray[invincibilityTimer] = 0;
			collided = false;
		}
	}

	/**
	 * Turns the player invincible.
	 * @param frames Decides how many frames the player will be invincible
	 */
	public void invincible(int frames) {
		collided = true;
		intArray[invincibilityTimer] = frames;
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
				canvas.setScreen(intArray[screenWidth], intArray[screenHeight]);
			} else {
				setExtendedState(JFrame.MAXIMIZED_BOTH);
				topBar.setVisible(false);
				fullScreen = true;
				canvas.setScreen(this.getWidth(), this.getHeight());
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
			if (intArray[slowDownSending] <= 0) {
				sendPlayerPackage();
				intArray[slowDownSending] = 2;
			} else {
				intArray[slowDownSending]--;
			}

			// Shrink
			shrink();

			// Grow
			grow();

			// Update ourself in the playerList
			updatePlayerList(intArray[playerID], playerCoordinates[X], playerCoordinates[Y], intArray[score]);

			// Update canvas foodList
			updateCanvasFood();

			// Check collision
			handleCollision();

			// Update
			canvas.updatePlayerList(playerList);
			canvas.setSpeed(intArray[currentMaxSpeed]);
			canvas.setInvincibleTimer(intArray[invincibilityTimer]);
			canvas.repaint();

			// Loop with this delay
			// 16ms = about 60 FPS, 32 = 30 FPS
			sleep(16);
		}
	}

	public void mouseDragged(MouseEvent e) {
		if ((JPanel) e.getSource() == topBar) {
			int x = e.getXOnScreen();
			int y = e.getYOnScreen();
			setLocation(getLocationOnScreen().x + x - intArray[lastX], getLocationOnScreen().y + y - intArray[lastY]);
			intArray[lastX] = x;
			intArray[lastY] = y;
		}
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if ((JPanel) e.getSource() == topBar) {
			intArray[lastX] = e.getXOnScreen();
			intArray[lastY] = e.getYOnScreen();
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
 * @author Lars Andersson
 * @version 2018-03-05
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

		if (playerID != client.getPlayerId()) {
			// Only update player from the packet
			client.updatePlayerList(playerID, x, y, score);
		}

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
