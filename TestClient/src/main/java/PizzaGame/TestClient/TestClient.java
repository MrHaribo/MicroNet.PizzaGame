package PizzaGame.TestClient;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;

import micronet.network.IPeer;
import micronet.network.Request;
import micronet.network.Response;
import micronet.network.StatusCode;
import micronet.network.factory.PeerFactory;
import micronet.serialization.Serialization;

public class TestClient {

	private static Semaphore commandLock = new Semaphore(1);
	
	private static TextIO textIO;
	private static IPeer peer;

	public enum Command {
		REGISTER,
		LOGIN,
		ORDER,
		EXIT
	}
	
	private static boolean isLoggedIn = false;
	
	public static void main(String[] args) throws InterruptedException {

		textIO = TextIoFactory.getTextIO();
		textIO.getTextTerminal().println("The Pizza Game!");

		createPeer();
		
		processCommands();

		peer.shutdown();
		textIO.dispose();
	}
	
	private static void processCommands() throws InterruptedException {
		while(true) {
			
			commandLock.acquire();
			
			Command cmd = textIO.newEnumInputReader(Command.class).read("\nSelect Command:");
			
			switch (cmd) {
			case EXIT:
				commandLock.release();
				return;
			case REGISTER:
				sendRegister(readCredentials());
				break;
			case LOGIN:
				sendLogin(readCredentials());
				break;
			case ORDER:
				if (!isLoggedIn) {
					textIO.getTextTerminal().println("You must login to place orders");
					commandLock.release();
					continue;
				}
				
				PizzaType pizzaType = textIO.newEnumInputReader(PizzaType.class).read("\nSelect Pizza:");
				if (pizzaType == null) {
					textIO.getTextTerminal().println("Unknown Pizza Type");
					commandLock.release();
					continue;
				}
				
				sendOrderPizza(pizzaType);
				break;
			default:
				textIO.getTextTerminal().println("Unknown Command");
				commandLock.release();
				break;
			}
		}
	}

	private static void sendOrderPizza(PizzaType type) {
		textIO.getTextTerminal().printf("Sending Pizza Order -> %s\n", type);
		peer.sendRequest(URI.create("mn://pizza/order"), new Request(type.toString()), response -> onOrder(response));
	}

	private static void onOrder(Response response) {
		textIO.getTextTerminal().printf("Pizza Received -> %s\n", response);
		commandLock.release();
	}

	private static void sendLogin(CredentialValues credentials) {
		textIO.getTextTerminal().printf("Sending Login -> username=%s password=%s\n", 
				credentials.getUsername(), hidePasswordString(credentials.getPassword()));

		Request loginRequest = new Request(Serialization.serialize(credentials));
		peer.sendRequest(URI.create("mn://account/login"), loginRequest, response -> onLogin(response));
	}

	static void onLogin(Response response) {
		textIO.getTextTerminal().printf("Login Response -> %s\n", response);

		if (response.getStatus() == StatusCode.OK) {
			isLoggedIn = true;
		}
		commandLock.release();
	}

	private static void sendRegister(CredentialValues credentials) {
		textIO.getTextTerminal().printf("Sending Register -> username=%s password=%s\n", 
				credentials.getUsername(), hidePasswordString(credentials.getPassword()));

		Request request = new Request(Serialization.serialize(credentials));
		peer.sendRequest(URI.create("mn://account/register"), request, response -> onRegister(response));
	}

	private static void onRegister(Response response) {
		textIO.getTextTerminal().printf("Register Response -> %s\n", response);
		commandLock.release();
	}
	
	private static void createPeer() {
		Thread networkThread = new Thread(() -> {
			peer = PeerFactory.createClientPeer();
		});
		networkThread.setDaemon(true);
		networkThread.start();
	}

	private static String hidePasswordString(String passwordString) {
		char[] charArray = new char[passwordString.length()];
		Arrays.fill(charArray, '*');
		return new String(charArray);
	}
	
	private static CredentialValues readCredentials() {
		String username = textIO.newStringInputReader().read("Username");
		String password = textIO.newStringInputReader().withMinLength(4).withInputMasking(true).read("Password");
		
		CredentialValues credentials = new CredentialValues();
		credentials.setUsername(username);
		credentials.setPassword(password);
		return credentials;
	}
}
