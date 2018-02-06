import java.applet.Applet;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Random;

public class Client extends Applet implements Runnable, KeyListener {

	private static final long serialVersionUID = -2729828280030658589L;
	static Socket socket;
	static DataInputStream in;
	static DataOutputStream out;
	Random r = new Random();

	int playerid;
	int[] x = new int[10]; // 10 = number of clients? I DUNNO
	int[] y = new int[10];

	boolean left, right, down, up;

	int playerx; //= 50 + r.nextInt(250);
	int playery; //= 50 + r.nextInt(250);

	public void init() {
		setSize(400, 400);
		addKeyListener(this);
		try {
			System.out.println("Connecting...");
			socket = new Socket("localhost", 7777);
			System.out.println("Connection successful.");
			in = new DataInputStream(socket.getInputStream());
			playerid = in.readInt();
			out = new DataOutputStream(socket.getOutputStream());
			Input input = new Input(in, this);
			Thread thread = new Thread(input);
			thread.start();
			Thread thread2 = new Thread(this);
			thread2.start();
		} catch (Exception e) {
			System.out.println("Unable to start client.");
		}
	}

	public void updateCoordinates(int pid, int x2, int y2) {
		this.x[pid] = x2;
		this.y[pid] = y2;
	}

	public void paint(Graphics g) {
		for (int i = 0; i < 10; i++) {
			g.fillOval(x[i], y[i], 25, 25);
		}
	}

	public void run() {
		while (true) {

			if (right == true) {
				playerx += 5;
			}
			if (left == true) {
				playerx -= 5;
			}
			if (down == true) {
				playery += 5;
			}
			if (up == true) {
				playery -= 5;
			}

			if (right || left || up || down) {
				try {
					out.writeInt(playerid);
					out.writeInt(playerx);
					out.writeInt(playery);
				} catch (Exception e) {
					System.out.println("Error sending Coordinates.");
				}
			}
			
			// Update
			repaint();
			
			// Loop with this delay
			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = true;
		}
	}

	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = false;
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = false;
		}
	}

	public void keyTyped(KeyEvent e) {
		
	}
}
