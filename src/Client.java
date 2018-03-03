import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
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

public class Client extends JFrame implements Runnable, KeyListener, ComponentListener, MouseListener, MouseMotionListener {

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
	private boolean collided = false;
	private boolean fullScreen = false;
	private static boolean disconnected = false;
	private int invincibleTimer = 0;
	private int screenWidth = 720;
	private int screenHeight = 480;
	private int slowDownSending = 2;
	private int lastX, lastY;
	private Color foregroundColor = new Color(249, 65, 32);
	private boolean left, right, down, up;

	// Constants
	private static final int roomSize = 1000;
	private static final int maxFoods = 20;
	private static final int maxScore = 200;
	private static final int initialAliveTime = 75;
	private static final float maxAcceleration = 0.45f;
	private static final float lowestFriction = 0.3f;
	private static final int X = 0, Y = 1;
	private static final int RIGHT = 0, LEFT = 1, DOWN = 2, UP = 3;
	private static final int maxSpeed = 4;

	// Player related
	private int playerID;
	private int playerCoordinates[] = new int[2];
	private int currentMaxSpeed = maxSpeed;
	private float acceleration = maxAcceleration;
	private float friction = lowestFriction;
	private float currentSpeed[] = new float[4];
	private int score = 15 + random.nextInt(10);
	private int shrinkTimer = 100;
	private int aliveTimer = initialAliveTime;
	private int growth = 0;
	private boolean adjustPosition;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();
	private HashMap<Integer, int[]> playerList = new HashMap<Integer, int[]>();
	private Food[] tempFood = new Food[maxFoods];

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
		
		//Create the top bar for moving the frame around etc.
	    topBar = new JPanel(new BorderLayout());
	    topBar.addMouseListener(this);
	    topBar.addMouseMotionListener(this);

	    JLabel close = new JLabel("×");
	    close.setBorder(new EmptyBorder(0, 0, 0, 20));
	    close.setFont(new Font("Arial", Font.BOLD, 20));
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

	    topBar.setBackground(foregroundColor);
	    topBar.add(close, BorderLayout.EAST);
	    this.add(topBar, BorderLayout.NORTH);
		
		try {

			// Connect to the server
			//serverIP = "localhost";//"176.10.136.66";
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
			
			// Initialization of adjustPosition depends on the start value of score being even or odd,
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
			// e.printStackTrace();
		}
		
		setVisible(true);
		repaint();
	}
	public static void main(String[] args) {	
		JFrame ipFrame = new JFrame();
		String serverIP = JOptionPane.showInputDialog(ipFrame, "IP adress:");
		
		if(serverIP == null) {
			serverIP = "localhost";
		}
		new Client(serverIP);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				disconnected = true;
			}
		}, "Shutdown-thread"));
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

	public void updateCoordinates(int pid, int x, int y, int score) {
		canvas.updateCoordinates(pid, x, y, score);
	}

	public void updateCanvasFood() {
		canvas.setFood(foodList);
	}

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

	public void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void move() {
		// Acceleration and friction
		if (right == true) {
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[RIGHT] = clampFloat(currentSpeed[RIGHT] - friction, currentMaxSpeed, 0);
		}
		if (left == true) {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[LEFT] = clampFloat(currentSpeed[LEFT] - friction, currentMaxSpeed, 0);
		}
		if (down == true) {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] + acceleration, currentMaxSpeed, 0);
		} else {
			currentSpeed[DOWN] = clampFloat(currentSpeed[DOWN] - friction, currentMaxSpeed, 0);
		}
		if (up == true) {
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
								Toolkit.getDefaultToolkit().beep();
								Food tempFood = new Food(i, random.nextInt(roomSize - 5), random.nextInt(roomSize - 5));
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
		if (e.getKeyCode() == KeyEvent.VK_F11) {
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
				;
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
			canvas.updateCoordinates(playerID, playerCoordinates[X], playerCoordinates[Y], score);
			canvas.setSpeed(maxSpeed);
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
