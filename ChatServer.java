import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private DatagramSocket socket;
    private List<GreetedClient> greetedClients = new ArrayList<GreetedClient>();

    public ChatServer(int port){
        try {
            socket = new DatagramSocket(port);
        } catch( Exception ex ){
            System.out.println("Problem creating socket.");
        }
    }

    public void start() throws Exception{
        System.out.println("Server Initialized...");
        while (true){
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String packetMessage = new String(packet.getData());
            System.out.println("PacketMessage: " + packetMessage);
            if (packetMessage == null) {
                continue;
            } else if (isGreetingMessage(packetMessage)){
                GreetedClient greetedClient = new GreetedClient(packet);
                greetedClients.add(greetedClient);
            } else if (isMessage(packetMessage)){
                sendIncomingMessage(packet);
            }
        }
    }

    private void sendIncomingMessage(DatagramPacket packet){
        String ip = packet.getAddress().getHostAddress();
        String port = Integer.toString(packet.getPort());
        String message = new String(packet.getData());
        message = message.substring(8);

        String incomingMessage = "<From " + ip + ":" + port + ">: " + message;
        for (GreetedClient greetedClient: greetedClients){
            try {
                socket.send(greetedClient.generateINCOMINGPacket(incomingMessage.getBytes()));
            } catch (IOException e) {
                System.out.println("Failed to send out packet to :" + greetedClient.getAddress().getHostAddress() + ":" + greetedClient.getPort());
            }
        }
    }

    private boolean isGreetingMessage(String packetMessage){
        return packetMessage.contains("GREETING");
    }

    private boolean isMessage(String packetMessage){
        return packetMessage.contains("MESSAGE");
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 1) {
            System.err.println(
                "Usage: java ChatServer <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);
        chatServer.start();
    }

    private class GreetedClient {
        private InetAddress address;
        private int port;

        public GreetedClient(DatagramPacket packet){
            address = packet.getAddress();
            port = packet.getPort();
        }

        public DatagramPacket generateINCOMINGPacket(byte[] message){
            System.out.println("Preparing to send " + (new String(message)));
            return new DatagramPacket(message, message.length, address, port);
        }

        public InetAddress getAddress(){
            return address;
        }

        public int getPort(){
            return port;
        }
    }
}