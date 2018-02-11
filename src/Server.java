import java.io.*;
import java.net.*;

public class Server {
	
	// Kända problem att ifall man skapar 1 sever 2 clienter. Stänger 1 client och startar en klient skickas inte alla meddelanden.
	static User[] user = new User[10];
	
	public static void main(String arg[]) throws Exception{
		
		System.out.println("Starting server...");
		int serverPort = Integer.parseInt("7777");								// Porten som vår server lyssnar från
		ServerSocket serverSocket = new ServerSocket(serverPort);				// Skapar en socket för att lyssna efter nya klienter
		System.out.println("Server started... listens on port " + serverPort);
		
		while(true){
			
			// Väntar här tills nästa klient ansluter sig även ifall det är while loop. Detsamma som om man väntar på att någon skriver något med scanner.
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
