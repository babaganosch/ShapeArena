import java.io.DataInputStream;
import java.io.IOException;

public class Input implements Runnable {

	DataInputStream in;

	public Input(DataInputStream in) {
		this.in = in;
	}

	public void run() {
		while (true) {
			String message;
			try {
				message = in.readUTF();
				System.out.println(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
