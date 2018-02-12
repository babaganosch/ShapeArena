import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server {

	private static int WorldSize = 400;
	private static Random random = new Random();
	private static User[] user = new User[10];
	private static Socket userSocket;
	static ServerSocket serverSocket;
	private static int serverPort = Integer.parseInt("7777");

	// Food related
	private static int[] foodX = new int[20];
	private static int[] foodY = new int[20];
	private static int tempX;
	private static int tempY;

	public static void main(String arg[]) throws Exception {

		System.out.println("Starting server...");
		serverSocket = new ServerSocket(serverPort);
		System.out.println("Server started... listens on port " + serverPort);

		// Food Handler
		for (int i = 0; i < 20; i++) {
			tempX = random.nextInt(WorldSize);
			tempY = random.nextInt(WorldSize);
			new Food(tempX, tempY);
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
	private DataOutputStream out;

	public FoodHandler(int[] foodX, int[] foodY, Socket socket) throws IOException {
		this.foodX = foodX;
		this.foodY = foodY;
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public void run() {

		while (true) {
			/*
			try {
				out.writeChar(1);

				out.writeInt(foodX[3]);
				out.writeInt(foodY[3]);
			} catch (Exception e) {
				System.out.println("Error sending: Food coords.");
			}
			*/
			for (int i = 0; i < 20; i++) {
				try {
					out.writeChar(1);
					out.writeInt(i);
					out.writeInt(foodX[i]);
					out.writeInt(foodY[i]);
				} catch (Exception e) {
					System.out.println("Error sending: Food coords.");
				}
			}
			
			try {
				Thread.sleep(2000); // Update each second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
