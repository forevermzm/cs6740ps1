import java.io.*;
import java.net.*;

public class ChatClient extends Thread{
    private DatagramSocket socket;
    private InetAddress address;
    private int portNumber;
    private BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in));

    public ChatClient(InetAddress address, int portNumber) throws Exception{
        this.socket = new DatagramSocket(null);
        this.address = address;
        this.portNumber = portNumber;
    }

    public ChatClient(int clientPort, InetAddress address, int portNumber) throws Exception{
        this.socket = new DatagramSocket(clientPort);
        this.address = address;
        this.portNumber = portNumber;
    }

    private synchronized void sendMessage(String message) throws Exception{
        // System.out.println(message);
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket (data, data.length, address, portNumber);
        socket.send(packet);
        // notify();
    }    

    private void sendMessage() throws Exception{
        String userInput;
        if ((userInput = stdIn.readLine()) != null) {
            sendMessage("MESSAGE " + userInput);
        }
    }

    public synchronized void getMessage(){
        try {
            socket.setSoTimeout(1000);
            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            // System.out.println("I am here");
            socket.receive(packet);
            // System.out.println("It is reaching me here.");
            System.out.println(new String(packet.getData()));
        } catch (SocketTimeoutException e){
            // System.out.println("It's timed out.");
            // e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } 
    }

    public void run(){
        System.out.println("ChatClient is started.");
        try {
            sendMessage("GREETING message");

            while (true) {
                // System.out.println("Send Message: ");
                sendMessage();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}

class ReceivingThread extends Thread {
        private ChatClient chatClient;

        public ReceivingThread (ChatClient chatClient){
            this.chatClient = chatClient;
        }

        public static void main(String[] args){
            if (args.length != 3) {
                System.err.println(
                    "Usage: java ChatClient <host name> <port number>");
                System.exit(1);
            }
            try {
                InetAddress address = InetAddress.getByName(args[0]);
                int portNumber = Integer.parseInt(args[1]);

                ChatClient chatClient = new ChatClient(Integer.parseInt(args[2]), address, portNumber);
                new ReceivingThread(chatClient).start();
                chatClient.start();
            } catch (UnknownHostException e){
                System.out.println("Host is not found!");
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                while (true){
                    Thread.sleep(1);
                    chatClient.getMessage();
                }
            } catch (Exception e){
                System.out.println("I'm interuptted.");
            }
        }
    }
