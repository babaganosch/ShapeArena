//import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Users implements Runnable {

	DataOutputStream out;
	DataInputStream in;
	Users[] user = new Users[10];

	public Users(DataOutputStream out, DataInputStream in, Users[] user) {
		this.out = out;
		this.in = in;
		this.user = user;
	}

	public void run() {
		while (true) {
			/**
			 * If we get a message from a client, we want to
			 * send that message to all the users
			 */
			try {
				String message = in.readUTF();
				for (int i = 0; i < 10; i++) {
					if (user[i] != null) {
						user[i].out.writeUTF(message);
					}
				}
			} catch (IOException e) {
				// Disconnect
				this.out = null;
				this.in = null;
			}
		}
	}

}
