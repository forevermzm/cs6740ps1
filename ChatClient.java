import java.io.*;
import java.net.*;

public class ChatClient{
    private DatagramSocket socket;

    public ChatClient(){
        socket = new DatagramSocket(null);        
    }

    private void greetServer(InetAddress address, int portNumber){
        // Convert string to byte array.
        byte[] data = "GREETING message".getBytes();
        DatagramPacket packet = new DatagramPacket (data, data.length, address, portNumber);
        socket.send(packet);
    }

    public static void main(String[] args){
        if (args.length != 2) {
            System.err.println(
                "Usage: java ChatClient <host name> <port number>");
            System.exit(1);
        }

        try {
            InetAddress address = InetAddress.getByName(args[0]);
            int portNumber = Integer.parseInt(args[1]);
            
            ChatClient chatClient = new ChatClient();
            chatClient.greetServer(address, portNumber);

            BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                data = ("MESSAGE " + userInput).getBytes();
                packet = new DatagramPacket(data, data.length, address, portNumber);
                socket.send(packet);
                socket.receive(packet);
                System.out.println(new String(packet.getData()));
            }

        } catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}