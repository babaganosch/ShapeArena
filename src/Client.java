import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

public class Client extends JFrame implements Runnable, KeyListener, ComponentListener {

	// ------------- DEBUG --------------
	private boolean debug = false;
	// ----------------------------------

	// Global
	private static final long serialVersionUID = -7317687704845378703L;
	private Random random = new Random();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Canvas canvas;
	private Socket socket;
	private boolean left, right, down, up;
	private boolean collided = false;
	private int invincibleTimer = 0;
	private int screenWidth = 720;
	private int screenHeight = 480;

	// Constants
	private static final int roomSize = 1000;
	private static final int maxFoods = 20;
	private static final int maxScore = 200;
	private static final int X = 0;
	private static final int Y = 1;

	// Player related
	private int playerID;
	private int playerCoord[] = new int[2];
	private int speed = 7;
	private int score = 15 + random.nextInt(10);
	private int shrinkTimer = 100;
	private int initialAliveTime = 75;
	private int aliveTimer = initialAliveTime;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private HashMap<Integer, int[]> playerList = new HashMap<Integer, int[]>();
	private Food[] tempFood = new Food[maxFoods];

	public Client() {

		// Setup the JFrame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(screenWidth, screenHeight);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setVisible(true);
		addComponentListener(this);

		// Create the canvas
		canvas = new Canvas(screenWidth, screenHeight, roomSize);
		add(canvas, BorderLayout.CENTER);
		canvas.repaint();
		try {

			// Connect to the server
			String serverIP = "176.10.136.66";
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

			// Out the player somewhere random on the map
			playerCoord[X] = random.nextInt(roomSize);
			playerCoord[Y] = random.nextInt(roomSize);

			// Initialize InputReader and start its thread.
			InputReader input = new InputReader(in, this);
			Thread readsInput = new Thread(input);
			readsInput.start();

			// Start the Client thread.
			Thread thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			System.out.println("Unable to start client");
			// e.printStackTrace();
		}
	}

	public static void main(String arg[]) {
		new Client();
	}

	public boolean getDebug() {
		return debug;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public int getMaxFoods() {
		return maxFoods;
	}

	public synchronized void updatePlayerList(int inPlayerId, int inX, int inY, int inScore) {
		int[] packet = new int[4];
		packet[0] = inPlayerId;
		packet[1] = inX;
		packet[2] = inY;
		packet[3] = inScore;
		this.playerList.put(inPlayerId, packet);
	}

	public void setFoodList(HashMap<Integer, Food> inFoodList) {
		this.foodList = inFoodList;
	}

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

	public void updateCoordinates(int pid, int x, int y, int score) {
		canvas.updateCoordinates(pid, x, y, score);
	}

	public void updateCanvasFood() {
		canvas.setFood(foodList);
	}

	public void sendPlayerPackage() {
		try {
			out.writeObject(new PlayerPacket(playerID, playerCoord[X], playerCoord[Y], score));
			out.flush();
		} catch (Exception e) {
			System.out.println("Error sending Coordinates.");
		}
	}

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void move() {
		if (right == true) {
			playerCoord[X] = clamp(playerCoord[X] + speed, roomSize - score, 0);
		}
		if (left == true) {
			playerCoord[X] = clamp(playerCoord[X] - speed, roomSize - score, 0);
		}
		if (down == true) {
			playerCoord[Y] = clamp(playerCoord[Y] + speed, roomSize - score, 0);
		}
		if (up == true) {
			playerCoord[Y] = clamp(playerCoord[Y] - speed, roomSize - score, 0);
		}
	}

	public void shrink() {
		if (shrinkTimer <= 0) {
			if (score > 25) {
				score--;
			}

			if (score >= 150) {
				shrinkTimer = 17;
				speed = 2;
			} else if (score >= 100) {
				shrinkTimer = 32;
				speed = 3;
			} else if (score >= 50) {
				shrinkTimer = 37;
				speed = 5;
			} else {
				shrinkTimer = 45;
				speed = 7;
			}
		}
		shrinkTimer--;
	}

	public void keepAlive() {
		if (aliveTimer <= 0) {
			sendPlayerPackage();
			aliveTimer = initialAliveTime;
		}
		aliveTimer--;
	}

	public void checkFoodCollision(Boolean showDebug) {
		Boolean collidedFood = false;
		// Loop through every Food and get their x and y coordinate, then check for
		// collision.
		for (int i = 0; i < maxFoods; i++) {
			tempFood[i] = foodList.get(i);

			if (tempFood[i] != null) {

				int tempFoodX = tempFood[i].getX();
				int tempFoodY = tempFood[i].getY();

				if (playerCoord[X] <= tempFoodX && playerCoord[X] >= (tempFoodX - score)) {
					if (playerCoord[Y] <= tempFoodY && playerCoord[Y] >= (tempFoodY - score)) {
						if (collidedFood == false) {

							// Print out debug message
							if (showDebug) {
								System.out.println("Client " + playerID + " collided with food index " + i);
							}

							// Make sure we don't collide again
							collidedFood = true;

							// Increment score
							score++;

							try {
								// Print out debug message
								if (showDebug) {
									System.out.println("Client " + playerID + " sending Food " + i);
								}

								// Create a new Food object with the same index as the one we've collided with.
								// Add the Food object to our foodList so we don't experience any graphical
								// delay
								Toolkit.getDefaultToolkit().beep();
								Food tempFood = new Food(i, random.nextInt(roomSize), random.nextInt(roomSize));
								foodList.put(i, tempFood);

								// Send the new Food object to the server
								out.writeObject(new FoodPacket(tempFood));
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

	public void checkPlayerCollision() {
		for (Integer i : playerList.keySet()) {
			if (i != null) {
				int otherPlayerId = playerList.get(i)[0];
				int otherPlayerX = playerList.get(i)[1];
				int otherPlayerY = playerList.get(i)[2];
				int otherPlayerScore = playerList.get(i)[3];

				if (playerID != otherPlayerId) {
					if (playerCoord[X] <= (otherPlayerX + otherPlayerScore) && playerCoord[X] >= (otherPlayerX - score)
							&& playerCoord[Y] <= (otherPlayerY + otherPlayerScore)
							&& playerCoord[Y] >= (otherPlayerY - score)) {

						// Other player eats us
						if (otherPlayerScore > score) {
							score = 25;
							playerCoord[X] = random.nextInt(roomSize);
							playerCoord[Y] = random.nextInt(roomSize);
						} else {
							// We eat other player
							score += otherPlayerScore * 0.3;
						}

						// We've collided
						invincible();
					}
				}
			}
		}
	}

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

	public void invincible() {
		collided = true;
		invincibleTimer = 60;
	}

	// KeyHandler:
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			left = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			up = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			right = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			down = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
			left = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
			up = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
			right = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
			down = false;
		}
	}

	public void keyTyped(KeyEvent arg0) {
	}

	public void run() {
		while (true) {

			// Movement
			move();

			// Update player
			sendPlayerPackage();

			// Check for Food collision
			if (score < maxScore) {
				checkFoodCollision(debug);
			}

			// Shrink
			shrink();

			// Update ourself in the playerList
			updatePlayerList(playerID, playerCoord[X], playerCoord[Y], score);

			// Update canvas foodList
			updateCanvasFood();

			// Check collision
			handleCollision();

			// Update
			canvas.updateCoordinates(playerID, playerCoord[X], playerCoord[Y], score);
			canvas.setSpeed(speed);
			canvas.setInvincibleTimer(invincibleTimer);
			canvas.repaint();

			// Loop with this delay
			// 16ms = about 60 FPS, 32 = 30 FPS
			sleep(32);
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
}

class InputReader implements Runnable {

	public ObjectInputStream in;
	public HashMap<Integer, Food> tempFoodList = new HashMap<Integer, Food>();
	Client client;

	public InputReader(ObjectInputStream in, Client client) {
		this.in = in;
		this.client = client;
	}

	public void handlePlayerPacket(Packet packet, Boolean showDebug) {

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
		client.updateCoordinates(playerID, x, y, score);
		client.updatePlayerList(playerID, x, y, score);

	}

	public void handleFoodPacket(Packet packet, Boolean showDebug) {

		// Unpack the packet
		FoodPacket temp = (FoodPacket) packet;

		// Only update foodList if ID is 1 (Receiver: Clients)
		if (temp.getId() == 1) {
			tempFoodList = temp.getFoodList();
			client.setFoodList(tempFoodList);
		}

	}

	public void run() {

		while (true) {
			try {

				// Receive packet and store it.
				Packet packet = null;
				try {
					packet = (Packet) in.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Handle packets.
				if (packet instanceof PlayerPacket) {

					handlePlayerPacket(packet, client.getDebug());

				} else if (packet instanceof FoodPacket) {

					handleFoodPacket(packet, client.getDebug());

				}

			} catch (Exception e) {
				System.out.println("Could not receive/forward packet.");
			}
		}
	}
}
