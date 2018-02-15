import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class User implements Runnable {
	
	// ------------- DEBUG --------------
	private boolean debug = false;
	// ----------------------------------

	// Thread
	private Thread activity = new Thread(this);

	// Global
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private User[] user;
	private int maxUsers = 10;

	// Player related
	private int playerID;
	private int inPlayerID;
	private int inX;
	private int inY;
	private int inScore;

	// Food related
	private int inFoodX, inFoodY, inFoodIndex;

	public User(Socket socket, User[] user, int pid) throws Exception {

		this.user = user;
		this.playerID = pid;
		this.socket = socket;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();
		this.in = new ObjectInputStream(socket.getInputStream());

		activity.start();
	}

	public void initializeClient() {
		// Send out playerID as a packet so the client know who he is.
		try {
			out.writeObject(new PlayerPacket(playerID, 0, 0, 0));
			out.flush();
		} catch (IOException e) {
			System.out.println("Failed to send playerID");
		}
	}

	public Packet readPacket(Packet packet) {
		try {
			packet = (Packet) in.readObject();
			return packet;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void handlePlayerPacket(Packet packet, Boolean showDebug) {
		// Handle the packet
		PlayerPacket temp = (PlayerPacket) packet;
		int inPlayerID = temp.getId();
		int inX = temp.getX();
		int inY = temp.getY();
		int inScore = temp.getScore();
		
		// Print out what's received
		if (showDebug == true) {
			System.out.println("Packet Recieved: PLAYER");
			System.out.println("playerID: " + inPlayerID);
			System.out.println("x: " + inX);
			System.out.println("y: " + inY);
			System.out.println("score: " + inScore + "\n");
		}
	}

	public void handleFoodPacket(Packet packet, Boolean showDebug) {
		// Handle the packet
		FoodPacket temp = (FoodPacket) packet;
		inFoodIndex = temp.getId();
		inFoodX = temp.getX();
		inFoodY = temp.getY();
		
		// Print out what's received
		if (showDebug == true) {
			System.out.println("Packet Recieved: FOOD");
			System.out.println("Index: " + inFoodIndex);
			System.out.println("x: " + inFoodX);
			System.out.println("y: " + inFoodY + "\n");
		}
	}
	
	public void run() {

		// Tell the client what playerID it is.
		initializeClient();

		while (true) {
			try {

				Packet packet = null;

				// Receive packet
				try {
					packet = (Packet) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if (packet instanceof PlayerPacket) {
					
					// Unpack the packet
					handlePlayerPacket(packet, debug);

					// Forward the packet to the other Users
					for (int i = 0; i < maxUsers; i++) {
						if (user[i] != null) {
							user[i].out.writeObject(new PlayerPacket(inPlayerID, inX, inY, inScore));
							user[i].out.flush();
						}
					}

				} else if (packet instanceof FoodPacket) {
					
					// Unpack the packet
					handleFoodPacket(packet, debug);

					// Forward the packet to the other Users
					for (int i = 0; i < maxUsers; i++) {
						if (user[i] != null) {
							user[i].out.writeObject(new FoodPacket(inFoodIndex, inFoodX, inFoodY));
							user[i].out.flush();
						}
					}
				}

			} catch (IOException e) {
				// Disconnect
				user[playerID] = null;
				System.out.println("Disconnection from: " + socket.getInetAddress() + " With a PID: " + playerID);
				break;
			}
		}
	}
}