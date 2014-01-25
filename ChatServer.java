import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This is a ChatServer uses the UDP protocol to talk to ChatClient. Once the client sends the GREETING message, the server will register the client into the greeted clients list. When the server receives a message from one of the greeted clients, it broadcast the message to all register clients.
 */
public class ChatServer {
    private DatagramSocket socket;
    // The HashSet is used to keep track of all registered clients.
    private Set<GreetedClient> greetedClients = new HashSet<GreetedClient>();

    public ChatServer(int port) {
        try {
            socket = new DatagramSocket(port);
        } catch ( IOException ex ) {
            System.out.println("Problem creating socket.");
        }
    }

    /**
     * This method handles all incoming packet to the specific socket. This method
     * and {@link #sendIncomingMessage(DatagramPacket packet) sendIncomingMessage}
     * is synchronized because send had receive should not run at the same time.
     * @return DatagramPacket
     */
    public synchronized DatagramPacket receivePacket() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return packet;
        }
    }

    /**
     * This method takes in the DatagramPakcet from one of the registered clients 
     * and broadcasts to all register clients.
     * @param packet Packet contains MESSAGE.
     */
    public synchronized void sendIncomingMessage(DatagramPacket packet) {
        String ip = packet.getAddress().getHostAddress();
        String port = Integer.toString(packet.getPort());
        String message = new String(packet.getData());
        message = message.substring(8);

        String incomingMessage = "<From " + ip + ":" + port + ">: " + message;
        for (GreetedClient greetedClient : greetedClients) {
            try {
                socket.send(greetedClient.generateINCOMINGPacket(incomingMessage.getBytes()));
            } catch (IOException e) {
                System.out.println("Failed to send out packet to :" + greetedClient.getAddress().getHostAddress() + ":" + greetedClient.getPort());
            }
        }
    }

    public void addGreetedClient(DatagramPacket packet) {
        greetedClients.add(new GreetedClient(packet));
    }

    public boolean containsGreetedClient(DatagramPacket packet) {
        return greetedClients.contains(new GreetedClient(packet));
    }

    private boolean isGreetingMessage(String packetMessage) {
        return packetMessage.startsWith("GREETING");
    }

    private boolean isMessage(String packetMessage) {
        return packetMessage.startsWith("MESSAGE");
    }

    /**
     * This class is the encapsulation of the registered client's information, 
     * includes InetAddress and port number. 
     */
    public class GreetedClient {
        private InetAddress address;
        private int port;

        public GreetedClient(DatagramPacket packet) {
            address = packet.getAddress();
            port = packet.getPort();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj instanceof GreetedClient) {
                GreetedClient anotherClient = (GreetedClient) obj;
                if ( this.address.equals(anotherClient.getAddress()) && this.port == anotherClient.getPort() )
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + address.hashCode();
            result = prime * result + (new Integer(port).hashCode());
            return result;
        }

        public DatagramPacket generateINCOMINGPacket(byte[] message) {
            return new DatagramPacket(message, message.length, address, port);
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println(
                "Usage: java ChatServer <port number>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        ChatServer chatServer = new ChatServer(port);

        System.out.println("Server Initialized...");
        while (true) {
            DatagramPacket packet = chatServer.receivePacket();
            String packetMessage = new String(packet.getData());
            // System.out.println("PacketMessage: " + packetMessage + " from: " + packet.getPort());

            // Ignore null messages.
            if (packetMessage == null) {
                continue;
            } else {
                if (chatServer.isGreetingMessage(packetMessage)) {
                    chatServer.addGreetedClient(packet);
                } else if (chatServer.isMessage(packetMessage)) {
                    if (chatServer.containsGreetedClient(packet))
                        chatServer.sendIncomingMessage(packet);
                }
            }
        }
    }
}