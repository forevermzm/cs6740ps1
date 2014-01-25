import java.io.*;
import java.net.*;

/**
 * This class is the client side of the project. It is able to send out messages to and receive
 * message from Server. It sends out two kinds of messages: GREETING message for registering in
 * Server and MESSAGE which is user input.
 */
public class ChatClient extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private int portNumber;
    private BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));

    public ChatClient(InetAddress address, int portNumber) throws Exception {
        this.socket = new DatagramSocket(null);
        this.address = address;
        this.portNumber = portNumber;
    }

    /**
     * This method sends out a message to Server.
     * @param  message   message contents.
     */
    private synchronized void sendMessage(String message){
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket (data, data.length, address, portNumber);
            socket.send(packet);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void sendMessage() throws Exception {
        String userInput;
        if ((userInput = stdIn.readLine()) != null) {
            sendMessage("MESSAGE " + userInput);
        }
    }

    /**
     * This method handles the action of receiving message. Since the receive method will 
     * lock the socket until it receives the packet, I set a timeout so that the client 
     * can periodly check if it needs to send out a message to server.
     */
    public synchronized void getMessage() {
        try {
            socket.setSoTimeout(5);
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            System.out.println(new String(packet.getData()));
        } catch (SocketTimeoutException e) {
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("ChatClient is started.");
        try {
            sendMessage("GREETING message");

            while (true) {
                sendMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println(
                "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }
        try {
            InetAddress address = InetAddress.getByName(args[0]);
            int portNumber = Integer.parseInt(args[1]);

            ChatClient chatClient = new ChatClient(address, portNumber);
            new ReceivingThread(chatClient).start();
            chatClient.start();
        } catch (UnknownHostException e) {
            System.out.println("Host is not found!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

/**
 * This thread handles the receiving process. It will keep checking if there is a incoming
 * packet from Server. If so, it will print out the message to console.
 */
class ReceivingThread extends Thread {
    private ChatClient chatClient;

    public ReceivingThread (ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public void run() {
        try {
            while (true) {
                Thread.sleep(1);
                chatClient.getMessage();
            }
        } catch (Exception e) {
            System.out.println("I'm interuptted.");
        }
    }
}
