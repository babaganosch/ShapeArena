import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Client extends JFrame implements Runnable, KeyListener {

	private static final long serialVersionUID = 5058081065501838682L;
	private Random random = new Random();
	private DataInputStream in;
	private DataOutputStream out;
	private Canvas canvas;

	private int playerID;
	private int playerx;
	private int playery;
	private int speed = 5;
	private int score = 20 + random.nextInt(5);

	private boolean left, right, down, up;

	public Client() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(400, 400);
		setLocationRelativeTo(null);
		setLayout(new BorderLayout());
		addKeyListener(this);
		setVisible(true);
		canvas = new Canvas();
		add(canvas, BorderLayout.CENTER);
		try {
			String serverIP = "127.0.0.1";
			int serverPort = Integer.parseInt("7777");
			Socket socket = new Socket(serverIP, serverPort);
			System.out.println("Connection successful.");

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			playerID = in.readInt();
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
				playerx += speed;
			}
			if (left == true) {
				playerx -= speed;
			}
			if (down == true) {
				playery += speed;
			}
			if (up == true) {
				playery -= speed;
			}

			if (right || left || up || down) {
				try {
					out.writeChar(0);
					out.writeInt(playerID);
					out.writeInt(playerx);
					out.writeInt(playery);
					out.writeInt(score);
				} catch (Exception e) {
					System.out.println("Error sending Coordinates.");
				}
			}

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

class Canvas extends JPanel {

	private static final long serialVersionUID = 756982496934224900L;
	private int[] x = new int[10];
	private int[] y = new int[10];
	private int[] size = new int[10];
	private int playerID;
	// Food related
	private int[] foodX = new int[20];
	private int[] foodY = new int[20];

	public Canvas() {
		setVisible(true);
		setBackground(Color.WHITE);
	}
	
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public void updateCordinates(int pid, int x, int y, int score) {
		this.x[pid] = x;
		this.y[pid] = y;
		this.size[pid] = score;
	}

	public void paintFood(int i, int foodX, int foodY) {
		this.foodX[i] = foodX;
		this.foodY[i] = foodY;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < 10; i++) {
			// Player colors:
			switch (i) {
			case 0:
				g.setColor(Color.blue);
				break;
			case 1:
				g.setColor(Color.red);
				break;
			case 2:
				g.setColor(Color.green);
				break;
			case 3:
				g.setColor(Color.orange);
				break;
			case 4:
				g.setColor(Color.lightGray);
				break;
			default:
				g.setColor(Color.black);
				break;
			}
			g.fillOval(x[i], y[i], size[i], size[i]);
		}
		Graphics2D g2 = (Graphics2D)g;
		Font currentFont = g2.getFont();
		Font newFont = currentFont.deriveFont(currentFont.getSize() * 5.0F);
		g2.setFont(newFont);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawString("" + size[playerID], 70, 200);
		g.setColor(Color.red);
		for (int i = 0; i < 20; i++) {
			g.fillOval(foodX[i], foodY[i], 5, 5);
		}
	}
}

class InputReader implements Runnable {

	public DataInputStream in;
	Client client;
	private char inPacketType;

	public InputReader(DataInputStream in, Client client) {
		this.in = in;
		this.client = client;
	}

	public void run() {

		while (true) {
			try {
				inPacketType = in.readChar();
				if (inPacketType == 0) {
					int playerID = in.readInt();
					int x = in.readInt();
					int y = in.readInt();
					int score = in.readInt();
					client.updateCordinates(playerID, x, y, score);
				} else if (inPacketType == 1) {
					int foodIndex = in.readInt();
					int foodX = in.readInt();
					int foodY = in.readInt();
					client.paintFood(foodIndex, foodX, foodY);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
