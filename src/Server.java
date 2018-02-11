import java.io.*;
import java.net.*;

public class Server {
	
	// K�nda problem att ifall man skapar 1 sever 2 clienter. St�nger 1 client och startar en klient skickas inte alla meddelanden.
	static User[] user = new User[10];
	
	public static void main(String arg[]) throws Exception{
		
		System.out.println("Starting server...");
		int serverPort = Integer.parseInt("7777");								// Porten som v�r server lyssnar fr�n
		ServerSocket serverSocket = new ServerSocket(serverPort);				// Skapar en socket f�r att lyssna efter nya klienter
		System.out.println("Server started... listens on port " + serverPort);
		
		while(true){
			
			// V�ntar h�r tills n�sta klient ansluter sig �ven ifall det �r while loop. Detsamma som om man v�ntar p� att n�gon skriver n�got med scanner.
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
