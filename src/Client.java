import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

	static Socket socket;
	static DataInputStream in;
	static DataOutputStream out;

	public static void main(String[] args) throws Exception {
		System.out.println("Connecting...");
		socket = new Socket("localhost", 7777);
		System.out.println("Connection successful.");
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		Input input = new Input(in);
		Thread thread = new Thread(input);
		thread.start();
		Scanner sc = new Scanner(System.in);
		
		while (true) {
				String sendMessage = sc.nextLine();
				out.writeUTF(sendMessage);
		}
	}

}
