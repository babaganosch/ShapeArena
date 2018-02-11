import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Client extends JFrame implements Runnable, KeyListener{
	
	private static final long serialVersionUID = 5058081065501838682L;
	private int playerID;
	private int playerx;
	private int playery;
	
	private int[] x = new int[10];
	private int[] y = new int[10];
	
	private boolean left, right, down, up;

	private DataInputStream in;
	private DataOutputStream out;
	
	private Canvas canvas;
	public Client(){
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
			
			InputReader input = new InputReader(in, this);
			Thread readsInput = new Thread(input);
			readsInput.start();

			Thread thread = new Thread(this);
			thread.start();

		}catch(Exception e) {System.out.println("Unable to start client");}
	}
	
	public void updateCordinates(int pid, int x, int y){
		this.x[pid] = x;
		this.y[pid] = y;
		
		canvas.updateCordinates(pid, x, y);
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
	
	public void keyTyped(KeyEvent arg0) {}
	
	
	public void run() {
		while (true) {

			if (right == true) {
				playerx += 10;
			}
			if (left == true) {
				playerx -= 10;
			}
			if (down == true) {
				playery += 10;
			}
			if (up == true) {
				playery -= 10;
			}

			if (right || left || up || down) {
				try {
					out.writeInt(playerID);
					out.writeInt(playerx);
					out.writeInt(playery);
				} catch (Exception e) {
					System.out.println("Error sending Coordinates.");
				}
			}
			
			// Update
			canvas.repaint();
			
			// Loop with this delay
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Canvas extends JPanel{
	
	private static final long serialVersionUID = 756982496934224900L;
	private int[] x = new int[10];
	private int[] y = new int[10];
	
	public Canvas() {
		setVisible(true);
		setBackground(Color.WHITE);
	}
	
	public void updateCordinates(int pid, int x, int y){
		this.x[pid] = x;
		this.y[pid] = y;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (int i = 0; i < 10; i++) {
			g.fillOval(x[i], y[i], 25, 25);
		}
	}
}

class InputReader implements Runnable{
	
	public DataInputStream in;
	Client client;
	public InputReader(DataInputStream in, Client client){
		this.in = in;
		this.client = client;
	}
	
	public void run(){
		
		while(true){
			try {
				int playerID = in.readInt();
				int x = in.readInt();
				int y = in.readInt();
				client.updateCordinates(playerID, x, y);
			} 
			catch (IOException e) {e.printStackTrace();}
		}
	}
}
