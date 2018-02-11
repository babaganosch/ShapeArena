import java.io.*;
import java.net.*;

public class User implements Runnable{

	private Thread activity = new Thread(this);
	
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private User[] user;
	
	private int inX;
	private int inY;
	private int inPlayerID;
	private int playerID;
	
	public User(Socket socket, User[] user, int pid) throws Exception{
		
		this.user = user;
		this.playerID = pid;
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		
		activity.start();
	}
	
	public void run(){
		
		try {out.writeInt(playerID);} catch(IOException e) { System.out.println("Failed to send playerID");}
		
		while(true){
			try { 
				inPlayerID = in.readInt();
				inX = in.readInt();
				inY = in.readInt();
				
				for(int i = 0; i<10; i++)
				{
					if(user[i] != null)
					{
						user[i].out.writeInt(inPlayerID);
						user[i].out.writeInt(inX);
						user[i].out.writeInt(inY);
					}
				}
			
			} 
			catch (IOException e) {
				// Disconnect
				user[playerID] = null;
				System.out.println("PID: " + playerID + " With IP: " + socket.getInetAddress() + " Disconnected.");
				break;
			}
		}
	}
}
