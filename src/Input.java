import java.io.DataInputStream;
import java.io.IOException;

public class Input implements Runnable {

	DataInputStream in;
	Client client;
	
	public Input(DataInputStream in, Client c) {
		this.in = in;
		this.client = c;
	}

	public void run() {
		while (true) {
			try {
				int playerid = in.readInt();
				int x = in.readInt();
				int y = in.readInt();
				client.updateCoordinates(playerid, x, y);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
