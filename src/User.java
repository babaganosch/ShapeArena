import java.io.*;
import java.net.*;

public class User implements Runnable{

	private Thread activity = new Thread(this);
	
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private User[] user;
	
	private char inPacketType;
	private int inX;
	private int inY;
	private int inScore;
	private int inPlayerID;
	private int playerID;
	
	// Food related
	//private int[] inFoodX = new int[20];
	//private int[] inFoodY = new int[20];
	private int inFoodX, inFoodY, inFoodIndex;
	
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
		
		while(true) {
			try { 
				/* inPacketType
				 * 0: Player Information
				 * 1: Food Information
				 */
				inPacketType = in.readChar();
				
				if (inPacketType == 0) {
					inPlayerID = in.readInt();
					inX = in.readInt();
					inY = in.readInt();
					inScore = in.readInt();
				
					for(int i = 0; i<10; i++)
					{
						if(user[i] != null)
						{
							user[i].out.writeChar(0);
							user[i].out.writeInt(inPlayerID);
							user[i].out.writeInt(inX);
							user[i].out.writeInt(inY);
							user[i].out.writeInt(inScore);
						}
					}
				
				} else if (inPacketType == 1) {
					inFoodIndex = in.readInt();
					inFoodX = in.readInt();
					inFoodY = in.readInt();
					for(int i = 0; i<10; i++)
					{
						if(user[i] != null)
						{
							user[i].out.writeChar(1);
							user[i].out.writeInt(inFoodIndex);
							user[i].out.writeInt(inFoodX);
							user[i].out.writeInt(inFoodY);
						}
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
