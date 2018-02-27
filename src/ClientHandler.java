import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ClientHandler extends Observable implements Runnable{

	// Thread
	private Thread activity = new Thread(this);

	// Global
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private ClientHandler[] clientHandler;
	private PacketHandler packetHandler;
	private boolean isReady = false;

	// Score
	HighscoreHandler highscoreHandler;
	private int topScore;

	// Player related
	private int playerID;

	// Food related
	//private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	public ClientHandler(Socket socket, ClientHandler[] user, int pid, HighscoreHandler scorehandler,
			PacketHandler packetHandler, Observer serverFrame) throws Exception {

		this.clientHandler = user;
		this.playerID = pid;
		this.socket = socket;
		this.highscoreHandler = scorehandler;
		this.packetHandler = packetHandler;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();
		this.in = new ObjectInputStream(socket.getInputStream());
		addObserver(serverFrame);
		
		activity.start();
	}

	public ObjectOutputStream getObjectOutputStream() {
		return out;
	}
	
	public boolean isReady() {
		return isReady;
	}

	public synchronized void initializeClient() {
		// Send out playerID as a packet so the client know who he is.
		try {
			out.writeObject(new PlayerInitializationPacket(playerID));
			out.flush();
		} catch (IOException e) {
			System.out.println("Failed to send playerID");
		}
		
		try {
			Thread.sleep(32);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		isReady = true;
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
		for(ClientHandler i: clientHandler){
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

					// Update FoodHandlers foodList if ID: 0 (Receiver: PacketHandler)
					if (temp.getId() == 0) {
						Food tempFood = temp.getFood();
						packetHandler.updateFood(tempFood);
					}
				}

				// Forward the packet to the other Users
				if (packet != null) {
					packetHandler.addPacket(packet);
				}
				
			} catch (IOException e) {

				// Disconnect
				clientHandler[playerID] = null;
				System.out.println("Disconnection from: " + socket.getInetAddress() + ", with a PID: " + playerID);
				
				setChanged();
				notifyObservers(getConnectedUsers());

				break;
			}
		}
	}
}