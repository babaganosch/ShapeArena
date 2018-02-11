//import java.net.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Users implements Runnable {

	DataOutputStream out;
	DataInputStream in;
	Users[] user = new Users[10];
	int playerid;
	int playeridin;
	int xin;
	int yin;
	
	public Users(DataOutputStream out, DataInputStream in, Users[] user, int pid) {
		this.out = out;
		this.in = in;
		this.user = user;
		this.playerid = pid;
	}
	
	public void run() {
		try {
			out.writeInt(playerid);
		} catch (IOException e1) {
			System.out.println("Failed to send PlayerID");
		}
		/**
		 * If we get a message from a client, we want to
		 * send that message to all the users
		 */
		
		while (true) {
			
			
			try {
				playeridin = in.readInt();
				xin = in.readInt();
				yin = in.readInt();
				for (int i = 0; i < 10; i++) {
					if (user[i] != null) {
						user[i].out.writeInt(playeridin);
						user[i].out.writeInt(xin);
						user[i].out.writeInt(yin);
					}
				}
			} catch (IOException e) {
				// Disconnect
				user[playerid] = null;
				break;
			}
		}
	}

}
