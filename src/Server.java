import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class Server extends Observable {

	// Global
	private int tickRate = 5;
	private int worldSize = 1000;
	private int maxUsers = 10;
	private Random random = new Random();
	private ClientHandler[] clientHandler = new ClientHandler[maxUsers];
	private ServerSocket serverSocket;
	private PacketHandler packetHandler;
	private HighscoreHandler highscoreHandler;
	private Observer serverFrame;

	private String port = "11100";

	// Food related
	private int maxFood = 20;
	private HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	public static void main(String[] args) throws Exception {
		new Server();
	}

	public Server() throws Exception {
		SetupServer(port);
		setupServerFrame();
		SetupObjects();
		StartListening();
	}

	public int getTickRate() {
		return tickRate;
	}

	public void setupServerFrame() {
		serverFrame = new ServerFrame(300, 300);
		addObserver(serverFrame);
		setChanged();
		notifyObservers(Integer.parseInt(port));
	}

	public void SetupServer(String port) throws IOException {
		// Setup the server
		int serverPort = Integer.parseInt(port);
		System.out.println("Starting server...");
		this.serverSocket = new ServerSocket(serverPort);
		System.out.println("Server started... listens on port " + serverPort);
	}

	public void SetupObjects() throws IOException {
		// Create the Food
		for (int i = 0; i < maxFood; i++) {
			int tempX = random.nextInt(worldSize);
			int tempY = random.nextInt(worldSize);
			foodList.put(i, new Food(i, tempX, tempY));
		}

		// Setup PacketHandler
		this.packetHandler = new PacketHandler(foodList, clientHandler, this);

		// Create HighscoreHandler
		this.highscoreHandler = new HighscoreHandler();
	}

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

	public void StartListening() throws Exception {
		// Start listening
		while (true) {

			Socket userSocket = serverSocket.accept();

			for (int i = 0; i < maxUsers; i++) {
				if (clientHandler[i] == null) {
					clientHandler[i] = new ClientHandler(userSocket, clientHandler, i, highscoreHandler, packetHandler, serverFrame);
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