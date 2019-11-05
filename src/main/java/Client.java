import requests.RequestType;

import java.net.*;
import java.util.Scanner;
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
//            this.serverAddress = InetAddress.getLocalHost();
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

    	Scanner sc = new Scanner(System.in); 
        DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
         
        byte buf[] = null; 

    	
        // loop while user not enters "bye" 
        while (true) 
        { 
            String inp = sc.nextLine(); 
  
            // convert the String input into the byte array. 
            buf = inp.getBytes(); 
  
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, serverAddress, 9999); 
  
             try {
				ds.send(DpSend);
				System.out.println("MESSAGE SENT");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
  
            // break the loop if user enters "bye" 
            if (inp.equals("bye")) 
                break; 
        } 

    	
//        //Create thread to listen to messages
//        new Thread(new ClientListen(serverAddress)).start();
//
//        while(true){
//            // INSERT UI FOR WHAT USER WANTS TO DO
//
//            //SENDING A REQUEST MESSAGE
//        }
//
//        //

    }

    public class ClientListen implements Runnable {

        InetAddress serverAddress;

        public ClientListen(InetAddress serverAddress){
            this.serverAddress = serverAddress;
        }

        @Override
        public void run() {

            //Like server, will listen to ip
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
                    System.out.println("Server says: " + message);

                    /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                     * COMPLETE THIS PORTION OF THE CODE
                     *
                     * Add in Thread and feed in the message*/
                    //This would be the thread managing method
                    new Thread(new ClientHandle(message)).start();

                }

            }catch (SocketException e){
                e.printStackTrace();
            }catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    //Class for when the Client receives a message
    public class ClientHandle implements Runnable {

        String message;

        public ClientHandle(String message){
            this.message = message;
        }

        @Override
        public void run() {

            String[] receivedMessage = message.split("_");
            int messageType = Integer.parseInt(receivedMessage[0]);
            RequestType receivedRequestType = RequestType.values()[messageType];

            switch(receivedRequestType){
                case Request :

                    break;


            }

        }

    }

}
