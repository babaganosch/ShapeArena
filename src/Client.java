import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import javax.swing.JFrame;

public class Client extends JFrame implements Runnable, KeyListener {

	private static final long serialVersionUID = -7317687704845378703L;
	private Random random = new Random();
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Canvas canvas;

	private int playerID;
	private int playerx;
	private int playery;
	private int speed = 5;
	private int score = 20 + random.nextInt(5);

	public static int room_size = 400;

	private boolean left, right, down, up;
	
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
			String serverIP = "localhost";
			int serverPort = Integer.parseInt("7777");
			@SuppressWarnings("resource")
			Socket socket = new Socket(serverIP, serverPort);
			System.out.println("Connection successful.");
			
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(socket.getInputStream());
			
			playerID = ((PlayerPacket) in.readObject()).getId();
			canvas.setPlayerID(playerID);
			InputReader input = new InputReader(in, this);
			Thread readsInput = new Thread(input);
			readsInput.start();

			Thread thread = new Thread(this);
			thread.start();
		} catch (Exception e) {
			System.out.println("Unable to start client");
		}
	}
	
	public static void main(String arg[])
	{
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

	public void updateCordinates(int pid, int x, int y, int score) {
		canvas.updateCordinates(pid, x, y, score);
	}

	public void paintFood(int i, int foodX, int foodY) {
		canvas.paintFood(i, foodX, foodY);
	}

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

			//if (right || left || up || down) {
				try {
					// Send package!
					//System.out.println("Sending PLAYER PACKET!");
					out.writeObject(new PlayerPacket(playerID, playerx, playery, score));
					out.flush();
				} catch (Exception e) {
					System.out.println("Error sending Coordinates.");
				}
			//}

			// Update
			canvas.repaint();

			// Loop with this delay
			try {
				Thread.sleep(16); // 60 FPS
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class InputReader implements Runnable {

	public ObjectInputStream in;
	Client client;
	private boolean debug = false;

	public InputReader(ObjectInputStream in, Client client) {
		this.in = in;
		this.client = client;
	}

	public void run() {

		while (true) {
			
			Packet packet = null;
			try {
				packet = (Packet) in.readObject();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (packet instanceof PlayerPacket) {
				PlayerPacket temp = (PlayerPacket) packet;
				int playerID = temp.getId();
				int x = temp.getX();
				int y = temp.getY();
				int score = temp.getScore();
				
				if (debug == true) {
					System.out.println("Packet Recieved: PLAYER");
					System.out.println("playerID: " + playerID);
					System.out.println("x: " + x);
					System.out.println("y: " + y);
					System.out.println("score: " + score + "\n");
				}
				
				// Horrible.. horrible.. horrible code
				if (playerID >= 0 && playerID <= 10) {
					if (x >= 0 && x <= Client.room_size) {
						if (y >= 0 && y <= Client.room_size) {
							if (score >= 20 && score <= 70) {
								client.updateCordinates(playerID, x, y, score);
							}
						}
					}
				}
				
			} else if (packet instanceof FoodPacket) {
				FoodPacket temp = (FoodPacket) packet;
				int foodIndex = temp.getId();
				int foodX = temp.getX();
				int foodY = temp.getY();
				if (debug == true) {
					System.out.println("Packet Recieved: FOOD");
					System.out.println("Index: " + foodIndex);
					System.out.println("x: " + foodX);
					System.out.println("y: " + foodY + "\n");
				}
				// Horrible.. horrible.. horrible code
				if (foodIndex >= 0 && foodIndex <= 20) {
					if (foodX >= 0 && foodX <= Client.room_size) {
						if (foodY >= 0 && foodY <= Client.room_size) {
							client.paintFood(foodIndex, foodX, foodY);
						}
					}
				}
			}
		}
	}
}
