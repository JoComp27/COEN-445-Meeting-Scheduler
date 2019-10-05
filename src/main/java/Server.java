package requests
import java.net.*
import java.io.*

public class Server implements Runnable{
    /**Port where the server will be listening to clients*/
    private final int clientPort;

    public Server(int clientPort){
       this.clientPort = clientPort;
    }

    @Override
    public void run() {
        /**Create new server and binds to a free port. From source of the internet
         * the range should be 49152 - 65535.*/

        try(DatagramSocket serverSocket = new DatagramSocket(9999)) {
            /**Messages here and sends to client*/
            String message = "The message, for now it's a test";
            DatagramPacket datagramPacket = new DatagramPacket(
                    message.getBytes(),     //Bytes of the message
                    message.length(),       //Length of the message
                    InetAddress.getLocalHost(),     //Through the local Host
                    clientPort                      //To the clientPort
            );
            serverSocket.send(datagramPacket);      //Method to send packet to client
        }catch (SocketException e){
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
