import java.io.*;
import java.net.*;

public class User implements Runnable {

	private Thread activity = new Thread(this);

	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private User[] user;

	private int inX;
	private int inY;
	private int inScore;
	private int inPlayerID;
	private int playerID;

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

	public void run() {

		try {
			out.writeObject(new PlayerPacket(playerID, 0, 0, 0));
		} catch (IOException e) {
			System.out.println("Failed to send playerID");
		}

		while (true) {
			try {
				/*
				 * inPacketType 'P': Player Information 'F': Food Information
				 */
				Packet packet = null;
				try {
					packet = (Packet) in.readObject();
					// System.out.println("User recieved packet!");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if (packet instanceof PlayerPacket) {
					PlayerPacket temp = (PlayerPacket) packet;
					inPlayerID = temp.getId();
					inX = temp.getX();
					inY = temp.getY();
					inScore = temp.getScore();

					for (int i = 0; i < 10; i++) {
						if (user[i] != null) {
							user[i].out.writeObject(new PlayerPacket(inPlayerID, inX, inY, inScore));
							user[i].out.flush();
						}
					}

				} else if (packet instanceof FoodPacket) {
					FoodPacket temp = (FoodPacket) packet;
					inFoodIndex = temp.getId();
					inFoodX = temp.getX();
					inFoodY = temp.getY();

					for (int i = 0; i < 10; i++) {
						if (user[i] != null) {
							user[i].out.writeObject(new FoodPacket(inFoodIndex, inFoodX, inFoodY));
							user[i].out.flush();
						}
					}
				}

			} catch (IOException e) {
				// Disconnect
				user[playerID] = null;
				System.out.println("PID: " + playerID + " With IP: " + socket.getInetAddress() + " Disconnected.");
				break;
			}
		}
	}
}
