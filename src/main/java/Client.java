import java.net.*;
import java.io.*;
import java.lang.*;

public class Client{

    private InetAddress serverAddress;
    private InetAddress selfAddress;

    private DatagramSocket ds;

    private int requestNumber;

    public Client(String serverAddress){

        this.requestNumber = 0;

        try {
            this.ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
            this.selfAddress = InetAddress.getLocalHost();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }



    }

    public static void main(String args[]) throws IOException {
        Client client = new Client(args[0]);
        client.run();
    }

    public void run() {



        // INSERT UI FOR WHAT USER WANTS TO DO

        //SENDING A REQUEST MESSAGE



        //

    }



    //Class for when the Client receives a message
    public class ClientHandle implements Runnable {

        @Override
        public void run() {

        }

    }

}
