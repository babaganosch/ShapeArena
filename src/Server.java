import java.net.*;

public class Server {
	
	static User[] user = new User[10];
	
	public static void main(String arg[]) throws Exception{
		
		System.out.println("Starting server...");
		int serverPort = Integer.parseInt("7777");
		ServerSocket serverSocket = new ServerSocket(serverPort);	
		System.out.println("Server started... listens on port " + serverPort);
		
		while(true){
			
			Socket userSocket = serverSocket.accept();
			
			for(int i = 0; i<10; i++){
				if(user[i] == null){
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
