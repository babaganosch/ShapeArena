import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

/**
 * This is the main server class for the Shape Arena.
 * Observed by ServerFrame.
 * @author Hasse Aro
 * @version 2018-03-xx
 */
public class Server extends Observable implements Runnable {

	// Global
	private int tickRate = 2;
	private int worldSize = 1000;
	private int maxUsers = 10;
	private Random random = new Random();
	private ClientHandler[] clientHandler = new ClientHandler[maxUsers];
	private ServerSocket serverSocket;
	private PacketHandler packetHandler;
	private HighscoreHandler highscoreHandler;
	private Observer serverFrame;

	private static String port = "11100";

	// Food related
	private int maxFood = 20;
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	/**
	 * Creates a new server object.
	 * @param port Port the server listens on.
	 */
	public Server(String port) {
		try {
			SetupServer(port);
			setupServerFrame();
			SetupObjects();
			Thread serverThread = new Thread(this);
			serverThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Server(port);
	}
	
	/**
	 * Returns the tickRate variable from the server
	 * @return Returns an int representing the tick rate
	 */
	public int getTickRate() {
		return tickRate;
	}

	/**
	 * Creates the Server Frame
	 */
	public void setupServerFrame() {
		serverFrame = new ServerFrame();
		addObserver(serverFrame);
		setChanged();
		notifyObservers(Integer.parseInt(port));
	}

	/**
	 * Starts the server and listens on the port given to it.
	 * Else throws IOException
	 * @param port The port the server listens on.
	 * @throws IOException The exception if it failed to start the server.
	 */
	public void SetupServer(String port) throws IOException {
		// Setup the server
		int serverPort = Integer.parseInt(port);
		System.out.println("Starting server...");
		this.serverSocket = new ServerSocket(serverPort);
		System.out.println("Server started... listens on port " + serverPort);
	}
	
	/**
	 * Starts the Packet Handler and the Highscore Handler.
	 * Also creates the initial Food objects.
	 * @throws IOException Throws exception if something went wrong.
	 */
	public void SetupObjects() throws IOException {
		// Create the Food
		for (int i = 0; i < maxFood; i++) {
			int tempX = random.nextInt(worldSize - 5);
			int tempY = random.nextInt(worldSize - 5);
			foodList.put(i, new Food(i, tempX, tempY));
		}

		// Setup PacketHandler
		this.packetHandler = new PacketHandler(foodList, clientHandler, this, serverFrame);

		// Create HighscoreHandler
		this.highscoreHandler = new HighscoreHandler();
	}

	/**
	 * Returns info about the connected clients.
	 * @return Returns a string containing information about every connected client to the server.
	 */
	public String getConnectedUsers() {
		String message = "";
		for (ClientHandler i : clientHandler) {
			if (i != null) {
				message += "Connected: " + i.getSocket().getInetAddress().getHostAddress() + " with ID: " + i.getId()
						+ System.lineSeparator();
			}
		}
		return message;
	}

	public void run() {
		// Start listening
		while (true) {

			Socket userSocket = null;
			try {
				userSocket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < maxUsers; i++) {
				if (clientHandler[i] == null) {
					try {
						clientHandler[i] = new ClientHandler(userSocket, clientHandler, i, highscoreHandler, packetHandler, serverFrame);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					setChanged();
					notifyObservers(getConnectedUsers());
					System.out.println("Connection from: " + userSocket.getInetAddress() + ", with a PID: " + i);
					break;

					/**
					 * Break the loop. After we created the client we want to go back to the
					 * while-loop and wait for a new connection.
					 */
				}
			}

		}
	}
}