package requests;
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;


public class Server implements Runnable{
    /**Port where the server will be listening to clients*/
    private final int clientPort;

    public Server(int clientPort){
       this.clientPort = clientPort;
    }

    public void main(String[] args){
        run();
    }

    public void manageMessage(String message){
        System.out.println("Manage the threads.");
    }

    @Override
    public void run() {
        /**Create new server and binds to a free port. From source of the internet
         * the range should be 49152 - 65535.*/

        /**The port address is chosen randomly*/
        try(DatagramSocket serverSocket = new DatagramSocket(9999)) {
            byte[] buffer = new byte[65535];
            /**Messages here and sends to client*/
            while(true){
                DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
                serverSocket.receive(DpReceive);        //Receive Data in Buffer
                String message = new String(DpReceive.getData());
                System.out.println("Client says: " + message);

                /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                 * COMPLETE THIS PORTION OF THE CODE
                 *
                 * Add in Thread and feed in the message*/
                //This would be the thread managing method
                manageMessage(message);

                if(message.equals("Bye")){
                    System.out.println("Client says bye. Exiting");
                    break;
                }
            }

        }catch (SocketException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
