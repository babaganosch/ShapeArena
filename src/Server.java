import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Observable;
import java.util.Random;

public class Server extends Observable {

	// Global
	public static int tickRate = 100;
	private static int worldSize = 600;
	private static int maxUsers = 10;
	private static Random random = new Random();
	private static User[] user = new User[maxUsers];
	private static boolean createFoodHandler = true;
	private ServerSocket serverSocket;
	private FoodHandler foodHandler;
	private HighscoreHandler highscoreHandler;
	private ServerFrame serverFrame;

	// Food related
	public static int maxFood = 20;
	private static HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	public Server() throws Exception {
		this.serverFrame = new ServerFrame(300, 300);
		SetupServer("11100");
		SetupObjects();
		StartListening();
		addObserver(serverFrame);
	}

	public static void main(String[] args) throws Exception {
		new Server();
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

		// Setup FoodHandler
		if (createFoodHandler) {
			this.foodHandler = new FoodHandler(foodList, user);
		}

		// Create Highscorehandler
		this.highscoreHandler = new HighscoreHandler();
	}

	public void StartListening() throws Exception {
		// Start listening
		while (true) {

			Socket userSocket = serverSocket.accept();
			String message = null;
			for (int i = 0; i < maxUsers; i++) {
				if (user[i] != null) {
					message += user[i].getSocket().getInetAddress().getHostAddress() + "\n";
				}
			}
			
			notifyObservers(message);
			
			for (int i = 0; i < maxUsers; i++) {
				if (user[i] == null) {
					user[i] = new User(userSocket, user, i, maxUsers, highscoreHandler, foodHandler, serverFrame);

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