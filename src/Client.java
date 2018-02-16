import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;

public class Client extends JFrame implements Runnable, KeyListener {

	// ------------- DEBUG --------------
	public static boolean debug = false;
	// ----------------------------------

	// Global
	private static final long serialVersionUID = -7317687704845378703L;
	private Random random = new Random();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Canvas canvas;

	public static int room_size = 400;
	public static int maxFoods = 20;
	private boolean left, right, down, up;

	// Player related
	private int playerID;
	private int playerx;
	private int playery;
	private int speed = 5;
	private int score = 20 + random.nextInt(5); // Score = size

	public Client() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(room_size, room_size);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setVisible(true);
		canvas = new Canvas();
		add(canvas, BorderLayout.CENTER);
		try {
			// Connect to the server
			String serverIP = "localhost";
			int serverPort = Integer.parseInt("7777");
			@SuppressWarnings("resource") // ---- TODO: Fix this ----
			Socket socket = new Socket(serverIP, serverPort);
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

	public void paintFood(int i, int foodX, int foodY) {
		canvas.paintFood(i, foodX, foodY);
	}

	public void sendPlayerPackage() {
		try {
			out.writeObject(new PlayerPacket(playerID, playerx, playery, score));
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
			playerx = clamp(playerx + speed, room_size - score, 0);
		}
		if (left == true) {
			playerx = clamp(playerx - speed, room_size - score, 0);
		}
		if (down == true) {
			playery = clamp(playery + speed, room_size - score, 0);
		}
		if (up == true) {
			playery = clamp(playery - speed, room_size - score, 0);
		}
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
			if (right || left || up || down) {
				sendPlayerPackage();
			}

			// Update
			canvas.repaint();

			// Loop with this delay
			sleep(16); // 16 = about 60 FPS
		}
	}
}

class InputReader implements Runnable {

	public ObjectInputStream in;
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
			if (x >= 0 && x <= Client.room_size) {
				if (y >= 0 && y <= Client.room_size) {
					if (score >= 20 && score <= 70) {
						client.updateCoordinates(playerID, x, y, score);
					}
				}
			}
		}
	}

	public void handleFoodPacket(Packet packet, Boolean showDebug) {
		// Unpack the packet
		FoodPacket temp = (FoodPacket) packet;
		HashMap<Integer, Food> tempFoodList = temp.getFoodList();

		for (int i = 0; i < 20; i++) {
			Food tempFood = tempFoodList.get(i);
			int tempFoodI = tempFood.getId();
			int tempFoodX = tempFood.getX();
			int tempFoodY = tempFood.getY();

			// Print out what's received
			if (showDebug == true) {
				System.out.println("Packet Recieved: FOOD");
				System.out.println("Index: " + tempFoodI);
				System.out.println("x: " + tempFoodX);
				System.out.println("y: " + tempFoodY + "\n");
			}

			// Only update food if the packet was intact
			if (tempFoodI >= 0 && tempFoodI <= Client.maxFoods) {
				if (tempFoodX >= 0 && tempFoodX <= Client.room_size) {
					if (tempFoodY >= 0 && tempFoodY <= Client.room_size) {
						client.paintFood(tempFood.getId(), tempFood.getX(), tempFood.getY());
					}
				}
			}
		}

	}

	public void run() {

		while (true) {
			try {
				Packet packet = null;
				// Receive packet and store it.
				try {
					packet = (Packet) in.readObject();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Handle food packet.
				if (packet instanceof PlayerPacket) {

					handlePlayerPacket(packet, Client.debug);

				} else if (packet instanceof FoodPacket) {
					
					handleFoodPacket(packet, Client.debug);
					
				}

			} catch (Exception e) {
				System.out.println("Could not receive/forward packet.");
			}
		}
	}
}
