import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

public class Server {

	private static int WorldSize = 400;
	private static Random random = new Random();
	private static User[] user = new User[10];

	// Food related
	public static int maxFood = 20;
	private static HashMap<Integer, Food> foodList = new HashMap<Integer, Food>();

	public static void main(String arg[]) throws Exception {

		int serverPort = Integer.parseInt("7777");
		System.out.println("Starting server...");
		@SuppressWarnings("resource") // ---- TODO: Fix this ----
		ServerSocket serverSocket = new ServerSocket(serverPort);
		System.out.println("Server started... listens on port " + serverPort);

		// Food Handler
		for (int i = 0; i < maxFood; i++) {
			int tempX = random.nextInt(WorldSize);
			int tempY = random.nextInt(WorldSize);
			foodList.put(i, new Food(i, tempX, tempY));
		}

		Socket foodSocket = new Socket("localhost", serverPort);
		FoodHandler foodHandler = new FoodHandler(foodSocket, foodList);
		foodHandler.start();

		// Start listening
		while (true) {

			Socket userSocket = serverSocket.accept();

			for (int i = 0; i < 10; i++) {
				if (user[i] == null) {
					user[i] = new User(userSocket, user, i);
					// Don't print out FoodHandler
					if (i != 0) {
						System.out.println("Connection from: " + userSocket.getInetAddress() + " With a PID: " + i);
					}
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

class FoodHandler extends Thread {

	private HashMap<Integer, Food> foodList;
	private ObjectOutputStream out;

	public FoodHandler(Socket socket, HashMap<Integer, Food> foodList) throws IOException {
		this.foodList = foodList;
		this.out = new ObjectOutputStream(socket.getOutputStream());
		this.out.flush();
	}

	public void run() {

		while (true) {
			
			try {
				out.writeObject(new FoodPacket(0, 0, 0, foodList));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			try {
				Thread.sleep(2000); // Update every other second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
