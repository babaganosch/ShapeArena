import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

public class Client extends JFrame implements Runnable, KeyListener {

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

	// Constants
	private static final int screenWidth = 720;
	private static final int screenHeight = 480;
	private static final int roomSize = 1000;
	private static final int maxFoods = 20;
	private static final int X = 0;
	private static final int Y = 1;

	// Player related
	private int playerID;
	private int playerCoord[] = new int[2];
	private int speed = 7;
	private int score = 15 + random.nextInt(10);
	private int maxScore = 200;
	private int shrinkTimer = 100;
	private int initialAliveTime = 75;
	private int aliveTimer = initialAliveTime;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private Food[] tempFood = new Food[maxFoods];

	public Client() {

		// Setup the JFrame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(screenWidth, screenHeight);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setVisible(true);
		// Create the canvas
		canvas = new Canvas(screenWidth, screenHeight, roomSize);
		add(canvas, BorderLayout.CENTER);

		try {

			// Connect to the server
			String serverIP = "localhost";
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
			
			//Out the player somewhere random on the map
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
			} else if (score >= 100) {
				shrinkTimer = 32;
			} else if (score >= 50) {
				shrinkTimer = 37;
			} else {
				shrinkTimer = 45;
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
		Boolean collided = false;
		// Loop through every Food and get their x and y coordinate, then check for
		// collision.
		for (int i = 0; i < maxFoods; i++) {
			tempFood[i] = foodList.get(i);

			if (tempFood[i] != null) {

				int tempFoodX = tempFood[i].getX();
				int tempFoodY = tempFood[i].getY();

				if (playerCoord[X] <= tempFoodX && playerCoord[X] >= (tempFoodX - score)) {
					if (playerCoord[Y] <= tempFoodY && playerCoord[Y] >= (tempFoodY - score)) {
						if (collided == false) {

							// Print out debug message
							if (showDebug) {
								System.out.println("Client " + playerID + " collided with food index " + i);
							}

							// Make sure we don't collide again
							collided = true;

							// Increment score
							score++;

							try {
								// Print out debug message
								if (showDebug) {
									System.out.println("Client " + playerID + " sending Food " + i);
								}
								
								// Create a new Food object with the same index as the one we've collided with.
								// Add the Food object to our foodList so we don't experience any graphical delay
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
		collided = false;
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

			// Send package only if we move, save bandwidth mang
			if (left || right || up || down) {
				sendPlayerPackage();
			}

			// Check for Food collision
			if (score < maxScore) {
				checkFoodCollision(debug);
			}

			// Shrink
			shrink();

			// Update canvas foodList
			updateCanvasFood();

			// Update our client even if we don't move
			keepAlive();

			// Update
			canvas.updateCoordinates(playerID, playerCoord[X], playerCoord[Y], score);
			canvas.repaint();

			// Loop with this delay
			// 16ms = about 60 FPS, 32 = 30 FPS
			sleep(32);
		}
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

		// Only update player if the packet was intact
		if (playerID >= 0 && playerID <= 10) {
			if (x >= 0 && x <= client.getRoomSize()) {
				if (y >= 0 && y <= client.getRoomSize()) {
					if (score >= 20 && score <= 254) {
						client.updateCoordinates(playerID, x, y, score);
					}
				}
			}
		}
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
