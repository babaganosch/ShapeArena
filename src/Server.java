import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {

	private static int WorldSize = 400;
	private static Random random = new Random();
	private static User[] user = new User[10];
	private static ServerSocket serverSocket;
	private static Socket userSocket;
	private static int serverPort = Integer.parseInt("7777");

	// Food related
	private static int[] foodX = new int[20];
	private static int[] foodY = new int[20];

	public static void main(String arg[]) throws Exception {

		System.out.println("Starting server...");
		serverSocket = new ServerSocket(serverPort);
		System.out.println("Server started... listens on port " + serverPort);

		// Food Handler
		for (int i = 0; i < 20; i++) {
			int tempX = random.nextInt(WorldSize);
			int tempY = random.nextInt(WorldSize);
			foodX[i] = tempX;
			foodY[i] = tempY;
		}

		Socket foodSocket = new Socket("localhost", serverPort);
		FoodHandler foodHandler = new FoodHandler(foodX, foodY, foodSocket);
		foodHandler.start();

		// Start listening
		while (true) {

			userSocket = serverSocket.accept();

			for (int i = 0; i < 10; i++) {
				if (user[i] == null) {
					user[i] = new User(userSocket, user, i);
					System.out.println("Connection from: " + userSocket.getInetAddress() + " With a PID: " + i);
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

	private int[] foodX = new int[20];
	private int[] foodY = new int[20];
	private ObjectOutputStream out;

	public FoodHandler(int[] foodX, int[] foodY, Socket socket) throws IOException {
		this.foodX = foodX;
		this.foodY = foodY;
		this.out = new ObjectOutputStream(socket.getOutputStream());
	}

	public void run() {

		while (true) {
			
			for (int i = 0; i < 20; i++) {
				try {
					// SOUP
					out.writeObject(new FoodPacket(i, foodX[i], foodY[i]));
					out.flush();
					
				} catch (Exception e) {
					System.out.println("Error sending: Food coords.");
				}
			}
			
			try {
				Thread.sleep(2000); // Update every other second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
