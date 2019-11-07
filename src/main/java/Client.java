import Tools.UdpSend;
import requests.*;

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.lang.*;

public class Client{

    private int requestNumber;


    public Client(){

        this.requestNumber = 0;

    }

    public static void main(String args[]) throws IOException {
        Client client = new Client();
        client.run();
    }

    public void run() throws IOException {

    	Scanner sc = new Scanner(System.in);
    	
        // loop while user not enters "bye" 
        while (true) 
        { 
            String inp = sc.nextLine(); 
  
            UdpSend.sendMessage(inp, 9999);
  
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

    private void handleDenied(DeniedMessage message) {



    }

    private void handleInvite(InviteMessage message) {

    }

    private void handleConfirm(ConfirmMessage message){

    }

    private void handleServerCancel(ServerCancelMessage message){

    }

    private void handleScheduled(ScheduledMessage message){

    }

    private void handleNotSchedules(NotScheduledMessage message) {

    }

    private void handleAdded(AddedMessage message){

    }

    private void handleRoomChange(RoomChangeMessage message) {

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
                case Denied :
                    DeniedMessage deniedMessage = new DeniedMessage();
                    deniedMessage.deserialize(message);
                    handleDenied(deniedMessage);
                    break;
                case Invite :

                    break;
                case Confirm :

                    break;
                case ServerCancel :

                    break;
                case Scheduled :

                    break;
                case NotScheduled :

                    break;
                case Added :

                    break;
                case RoomChange :

                    break;

            }

        }

    }

}
