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
			System.out.println(userSocket.getInetAddress().getHostName() + " connected");
			
			for(int i = 0; i<10; i++){
				if(user[i] == null){
					user[i] = new User(userSocket, user, i);
					break;
				}
			}
		}
	}
}
