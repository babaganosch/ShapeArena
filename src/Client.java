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

	private int roomSize = 600;
	private int maxFoods = 20;
	private boolean left, right, down, up;

	// Player related
	private int playerID;
	private int playerx;
	private int playery;
	private int speed = 5;
	private int score = 20 + random.nextInt(5); // Score = size
	private int maxScore = 200;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private Food[] tempFood = new Food[maxFoods];

	public Client() {

		// Setup the JFrame
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(roomSize, roomSize);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setVisible(true);
		canvas = new Canvas();
		add(canvas, BorderLayout.CENTER);

		try {

			// Connect to the server
			String serverIP = "localhost"; //"192.168.1.172";
			int serverPort = Integer.parseInt("11100");

			this.socket = new Socket(serverIP, serverPort);
			System.out.println("Connection successful.");

			// Initialize the Input/Output streams
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());

			// Receive packet with playerID from User.
			playerID = ((PlayerPacket) in.readObject()).getId();
			canvas.setPlayerID(playerID);

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

	public void setFoodList(HashMap<Integer, Food> foodList) {
		this.foodList = foodList;
	}

	public int clamp(int value, int max, int min) {
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

	public void paintFood(HashMap<Integer, Food> foodList) {
		for (Integer i : foodList.keySet()) {
			canvas.paintFood(foodList.get(i).getId(), foodList.get(i).getX(), foodList.get(i).getY());
		}
	}

	public void sendPlayerPackage() {
		try {
			out.writeObject(new PlayerPacket(playerID, playerx, playery, score));
			out.flush();
		} catch (Exception e) {
			System.out.println("Error sending Coordinates.");
		}
		canvas.updateCoordinates(playerID, playerx, playery, score);
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
			playerx = clamp(playerx + speed, roomSize - score, 0);
		}
		if (left == true) {
			playerx = clamp(playerx - speed, roomSize - score, 0);
		}
		if (down == true) {
			playery = clamp(playery + speed, roomSize - score, 0);
		}
		if (up == true) {
			playery = clamp(playery - speed, roomSize - score, 0);
		}
	}

	public void checkFoodCollision(Boolean showDebug) {
		Boolean collided = false;
		for (int i = 0; i < maxFoods; i++) {
			tempFood[i] = foodList.get(i);
			if (tempFood[i] != null) {
				int tempFoodX = tempFood[i].getX();
				int tempFoodY = tempFood[i].getY();

				if (playerx <= tempFoodX && playerx >= (tempFoodX - score)) {
					if (playery <= tempFoodY && playery >= (tempFoodY - score)) {
						if (collided == false) {

							// Print out debug message
							if (showDebug) {
								System.out.println("Client " + playerID + " collided with food index " + i);
							}

							// Make sure we don't collide again
							collided = true;

							// Increment score
							score++;

							// Spawn a new Food somewhere else and add it to the foodList
							foodList.put(i, new Food(i, random.nextInt(roomSize), random.nextInt(roomSize)));

						}
					}
				}
			}
		}

		if (collided == true) {
			// Send out the new foodList
			try {
				// Print out debug message
				if (showDebug) {
					System.out.println("Client " + playerID + " sending out a FoodPacket with ID: 0");
				}
				// Send out the packet with ID 0 (Receiver: FoodHandler)
				out.writeObject(new FoodPacket(0, foodList));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
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

			// Send package
			sendPlayerPackage();

			// Check for Food collision
			if (score <= maxScore) {
				checkFoodCollision(debug);
			}

			// Paint out Food
			paintFood(foodList);

			// Update
			canvas.repaint();

			// Loop with this delay
			sleep(16); // 16ms = about 60 FPS
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
