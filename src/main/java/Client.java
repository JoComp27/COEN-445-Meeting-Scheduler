import requests.*;

import java.net.*;
import java.util.Scanner;
import java.io.*;
import java.lang.*;

public class Client {

    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment for RequestNumber

    private InetAddress serverAddress;
    private InetAddress selfAddress;

    private DatagramSocket ds;

    private int requestNumber;

    public Client(String serverAddress) throws UnknownHostException {
        this.requestNumber = 0;
        this.serverAddress = InetAddress.getByName(serverAddress);

    }

    public static void main(String args[]) throws IOException {

        if (args.length == 0) {
            System.out.println("Server IP is missing");
            return;
        }
        Client client = new Client(args[0]);
        client.run();
    }

    private void sendMessageToServer(String message) throws IOException {

        // convert the String input into the byte array.
        byte buf[] = message.getBytes();
        byte[] buffer = new byte[100];
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, serverAddress, 9997);
        ds.send(DpSend);
        System.out.println("MESSAGE SENT");

        DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
        ds.receive(DpReceive);        //Receive Data in Buffer
        String messageFromServer = new String(DpReceive.getData());
        System.out.println("Server says: " + messageFromServer);

    }

    public void run() throws IOException {
        ds = new DatagramSocket();
        Scanner sc = new Scanner(System.in);
        // loop while user not enters "bye"
        while (true) {
            String inp = sc.nextLine();

            sendMessageToServer(inp);
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
    //Room Unavailable Message

        //Check if request RQ# exists inside its list of request
        //and is the owner

        //If true, Delete the request that was just sent to the server

        //If false, Server sent incorrect request

    }

    private void handleInvite(InviteMessage message) {

        //Add the new request into your list and make it a standby status meeting

        //Add option to accept or reject the newly added

    }

    private void handleConfirm(ConfirmMessage message) {

    }

    private void handleServerCancel(ServerCancelMessage message) {



    }

    private void handleScheduled(ScheduledMessage message) {

        //Check if request RQ# and MT# is part of my list and is in standby (Only Host should receive)

        //Will update the request with the members with the list of members that have have accepted the invite

    }

    private void handleNotSchedules(NotScheduledMessage message) {

    }

    private void handleAdded(AddedMessage message) {

    }

    private void handleRoomChange(RoomChangeMessage message) {

    }

    public class ClientListen implements Runnable {

        InetAddress serverAddress;

        public ClientListen(InetAddress serverAddress) {
            this.serverAddress = serverAddress;
        }

        @Override
        public void run() {

            //Like server, will listen to ip
            /**Create new server and binds to a free port. From source of the internet
             * the range should be 49152 - 65535.*/

            /**The port address is chosen randomly*/
            try (DatagramSocket serverSocket = new DatagramSocket(9997)) {
                byte[] buffer = new byte[100];
                /**Messages here and sends to client*/
                while (true) {
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

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    //Class for when the Client receives a message
    public class ClientHandle implements Runnable {

        String message;

        public ClientHandle(String message) {
            this.message = message;
        }

        @Override
        public void run() {

            String[] receivedMessage = message.split("_");
            int messageType = Integer.parseInt(receivedMessage[0]);
            RequestType receivedRequestType = RequestType.values()[messageType];

            switch (receivedRequestType) {
                case Denied:
                    DeniedMessage deniedMessage = new DeniedMessage();
                    deniedMessage.deserialize(message);
                    handleDenied(deniedMessage);
                    break;
                case Invite:
                    InviteMessage inviteMessage = new InviteMessage();
                    inviteMessage.deserialize(message);
                    handleInvite(inviteMessage);
                    break;
                case Confirm:
                    ConfirmMessage confirmMessage = new ConfirmMessage();
                    confirmMessage.deserialize(message);
                    handleConfirm(confirmMessage);
                    break;
                case ServerCancel:
                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage();
                    serverCancelMessage.deserialize(message);
                    handleServerCancel(serverCancelMessage);
                    break;
                case Scheduled:
                    ScheduledMessage scheduledMessage = new ScheduledMessage();
                    scheduledMessage.deserialize(message);
                    handleScheduled(scheduledMessage);
                    break;
                case NotScheduled:
                    NotScheduledMessage notScheduledMessage = new NotScheduledMessage();
                    notScheduledMessage.deserialize(message);
                    handleNotSchedules(notScheduledMessage);
                    break;
                case Added:
                    AddedMessage addedMessage = new AddedMessage();
                    addedMessage.deserialize(message);
                    handleAdded(addedMessage);
                    break;
                case RoomChange:
                    RoomChangeMessage roomChangeMessage = new RoomChangeMessage();
                    roomChangeMessage.deserialize(message);
                    handleRoomChange(roomChangeMessage);
                    break;

            }

        }

    }

}

