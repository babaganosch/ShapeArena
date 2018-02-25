import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class User extends Observable implements Runnable{

	// Thread
	private Thread activity = new Thread(this);

	// Global
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private User[] user;
	private int maxUsers;
	private FoodHandler foodHandler;

	// Score
	HighscoreHandler highscoreHandler;
	private int topScore;

	// Player related
	private int playerID;

	// Food related
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	public User(Socket socket, User[] user, int pid, int maxUsers, HighscoreHandler scorehandler,
			FoodHandler foodHandler, Observer serverFrame) throws Exception {

		this.user = user;
		this.playerID = pid;
		this.socket = socket;
		this.maxUsers = maxUsers;
		this.highscoreHandler = scorehandler;
		this.foodHandler = foodHandler;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();
		this.in = new ObjectInputStream(socket.getInputStream());
		addObserver(serverFrame);
		
		activity.start();
	}

	public ObjectOutputStream getObjectOutputStream() {
		return out;
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

	public Socket getSocket() {
		return socket;
	}
	
	public int getId() {
		return playerID;
	}
	
	public String getConnectedUsers()
	{
		String message = "";
		for(User i: user){
			if(i != null){
				message += "Connected: " + i.getSocket().getInetAddress().getHostAddress() + " with ID: " + i.playerID + System.lineSeparator();
			}
		}
		return message;
	}
	

	public void run() {

		// Tell the client what playerID it is.
		initializeClient();

		while (true) {

			// Highscore handling
			highscoreHandler.setTopScore(topScore);

			try {

				Packet packet = null;

				// Receive packet
				try {
					packet = (Packet) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if (packet instanceof PlayerPacket) {
					PlayerPacket temp = (PlayerPacket) packet;
					int tempScore = temp.getScore();

					if (tempScore > topScore) {
						topScore = tempScore;
					}
				}

				if (packet instanceof FoodPacket) {

					// Unpack the packet
					FoodPacket temp = (FoodPacket) packet;

					// Update FoodHandlers foodList if ID: 0 (Receiver: FoodHandler)
					if (temp.getId() == 0) {
						foodList = temp.getFoodList();
						foodHandler.setFoodList(foodList);
					}
				}

				// Forward the packet to the other Users
				if (packet != null) {
					//System.out.println(packet);
					foodHandler.addPacket(packet);
				}
				/*
				for (int i = 0; i < maxUsers; i++) {
					if (user[i] != null && user[i].getSocket() != socket) {
						user[i].out.writeObject(packet);
						user[i].out.flush();
						user[i].out.reset();
					}
				}
				*/
			} catch (IOException e) {

				// Disconnect
				user[playerID] = null;
				System.out.println("Disconnection from: " + socket.getInetAddress() + ", with a PID: " + playerID);
				
				setChanged();
				notifyObservers(getConnectedUsers());

				break;
			}
		}
	}
}