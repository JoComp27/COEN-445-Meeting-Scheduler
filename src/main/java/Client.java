import Tools.CalendarUtil;
import Tools.UdpSend;
import requests.*;

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.lang.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment for RequestNumber

    private InetAddress serverAddress;
    private DatagramSocket ds;

    private String username;
    private ArrayList<ClientMeeting> meetings;

    private HashMap<String, Boolean> availability;

    public Client(String serverAddress, String username) throws UnknownHostException {
        this.serverAddress = InetAddress.getByName(serverAddress);
        this.username = username;

        meetings = new ArrayList<>();
    }

    public static void main(String args[]) throws IOException {

        if (args.length == 0) {
            System.out.println("Server IP is missing");
            return;
        }

        Scanner sc = new Scanner(System.in);
        String username = sc.nextLine();

        Client client = new Client(args[0], username);

        //Checking if previous
        File saveFile = new File("clientSave.txt");

        if(saveFile.exists()){
            client.restoreFromSave(saveFile, username);
        }

        client.run();

    }

    private void restoreFromSave(File saveFile, String username) {



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

    private void handleDenied(DeniedMessage message) {  //Room Unavailable Message

        //Check if request RQ# exists inside its list of request and is the owner
        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getRequestNumber() == message.getRequestNumber()){
                //If true, Delete the request that was just sent to the server
                meetings.remove(i);
                return;
            }
        }

        //If false, Server sent incorrect request
        System.out.println("Server sent denied for a non-existant request");

    }

    private void handleInvite(InviteMessage message) {

        //Add the new request into your list and make it a standby status meeting
        ClientMeeting newMeeting = new ClientMeeting(message);

        if(!availability.containsKey(CalendarUtil.calendarToString(newMeeting.getCalendar()))){
            newMeeting.setCurrentAnswer(true);
            meetings.add(newMeeting);

            //Send Accept
            UdpSend.sendMessage(new AcceptMessage(newMeeting.getMeetingNumber()).serialize(), 9997);

        } else {
            newMeeting.setCurrentAnswer(false);
            meetings.add(newMeeting);

            //Send Reject
            UdpSend.sendMessage(new RejectMessage(newMeeting.getMeetingNumber()).serialize(), 9997);
        }

    }

    private void handleConfirm(ConfirmMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == message.getMeetingNumber()){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == false){
                    meetings.get(i).receiveConfirmMessage(message);
                }
                return;
            }
        }

    }

    private void handleServerCancel(ServerCancelMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == message.getMeetingNumber()){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == false) {
                    System.out.println("Meeting " + message.getMeetingNumber() + " was cancelled for this reason : " + message.getReason());
                    synchronized (meetings){
                        meetings.remove(i);
                    }
                }
            }
        }

    }

    private void handleScheduled(ScheduledMessage message) {

        //Check if request RQ# is part of my list and is in standby (Only Host should receive)
        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getRequestNumber() == message.getRequestNumber()){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == true){
                    //Change Meeting to complete and change info in meeting
                    meetings.get(i).receiveScheduledMessage(message);

                }
                return;
            }
        }

    }

    private void handleNotScheduled(NotScheduledMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getRequestNumber() == message.getRequestNumber()){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == true) {
                    synchronized (meetings){
                        meetings.remove(i);
                    }
                }
            }
        }

    }

    private void handleAdded(AddedMessage message) {

    }

    private void handleRoomChange(RoomChangeMessage message) {

    }

    private void handleServerWidthdraw(ServerWidthdrawMessage message){

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
                    handleNotScheduled(notScheduledMessage);
                    break;
                case Added:
                    AddedMessage addedMessage = new AddedMessage();
                    addedMessage.deserialize(message);
                    handleAdded(addedMessage);
                    break;
                case RoomChange:
/*                    RoomChangeMessage roomChangeMessage = new RoomChangeMessage();
                    roomChangeMessage.deserialize(message);
                    handleRoomChange(roomChangeMessage);*/
                    break;
                case ServerWidthdraw:
                    ServerWidthdrawMessage serverWidthdrawMessage = new ServerWidthdrawMessage();
                    serverWidthdrawMessage.deserialize(message);
                    handleServerWidthdraw(serverWidthdrawMessage);
                    break;

            }

        }

    }

}

