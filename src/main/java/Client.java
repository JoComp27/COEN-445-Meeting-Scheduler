import Tools.CalendarUtil;
import Tools.FileReaderWriter;
import Tools.UdpSend;
import requests.*;

import java.net.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {

    private static final AtomicInteger countID = new AtomicInteger(0);  //Thread safe auto increment for RequestNumber

    private String clientPort;
    private String serverPort;

    private DatagramSocket ds;

    private ArrayList<ClientMeeting> meetings;
    private HashMap<String, Boolean> availability;

    public Client(String serverPort, String clientPort) throws UnknownHostException {
        this.clientPort = clientPort;
        this.serverPort = serverPort;

        this.meetings = new ArrayList<>();
    }

    public static void main(String args[]) throws IOException {

        if(args.length < 2){
            System.err.println("MISSING ARGUMENTS WHILE STARTING CLIENT");
            return;
        }

        String serverPort;
        String clientPort;

        try {
            serverPort = args[0].trim();
            clientPort = args[1].trim();
        } catch (NumberFormatException e) {
            e.printStackTrace();

            System.err.println("INVALID ARGUMENTS WERE GIVEN");
            return;

        }


        Client client = new Client(serverPort, clientPort);

        //Checking if previous
        File saveFile = new File("saveFile_" + clientPort + ".txt");

        if(saveFile.exists()){

            //Add CLI to check if user wants to restore user or not.

            System.out.println("It seems that a restore file is available and could be loaded onto the" +
                    "client\n Do you wish to restore it?");

            String answer = "";

            Scanner scanner = new Scanner(System.in);

            while(!answer.equals("y") || !answer.equals("n")){

                answer = scanner.nextLine().trim();
                switch (answer) {
                    case "y":
                        System.out.println("Save will be restored for client " + clientPort);
                        client.restoreFromSave(saveFile.getName());
                        break;
                    case "n":
                        System.out.println("Save will not be restored for client");
                        break;
                    default:
                        System.out.println("INVALID SAVE RESTORE ANSWER");
                }
            }


        }

        client.run();

    }

    private void sendMessageToServer(String message) throws IOException {

        // convert the String input into the byte array.
        byte buf[] = message.getBytes();
        byte[] buffer = new byte[100];
        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);
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
//        new Thread(new ClientSave(username)).start();
//
//        while(true){
//          // INSERT CLI FOR CLIENT
//
//        }
//
//        //

    }

    private void checkState(){
        //If meeting list is not empty
        if(!meetings.isEmpty()){
            //Get how many meetings this client is part of
            int meetingNumbers = meetings.size();
            System.out.println("You are a part of " + meetingNumbers + ", which meeting do you want to choose?");
            System.out.println("Type 'None' to not select any of the current meetings");
            Scanner scanner = new Scanner(System.in);
            String answer = scanner.nextLine();
            if(!answer.equals("None")) {
                try {
                    if (Integer.parseInt(answer) <= meetingNumbers) {
                        //Use the meeting the user chose
                        ClientMeeting clientMeeting = meetings.get(Integer.parseInt(answer));

                        if (clientMeeting.getUserType() == true && clientMeeting.getState() == true) {
                            //Organizer and meeting is confirmed
                            //Organizer can cancel the meeting
                            System.out.println("This meeting is confirmed");
                            System.out.println("Meeting number: " + clientMeeting.getMeetingNumber());
                            System.out.println("Type 'Cancel_MeetingNumber' to cancel the meeting");

                        }
                        else if (clientMeeting.getUserType() == false && clientMeeting.getState() == true && clientMeeting.isCurrentAnswer() == true) {
                            //Invitee, meeting is confirmed and current answer is accepted
                            //At confirm message, meeting is confirmed, can only withdraw
                            System.out.println("This meeting is confirmed");
                            System.out.println("Meeting number: " + clientMeeting.getMeetingNumber());
                            System.out.println("Type 'Withdraw_MeetingNumber' to withdraw from the meeting");
                        }
                        else if (clientMeeting.getUserType() == false && clientMeeting.getState() == true && clientMeeting.isCurrentAnswer() == false){
                            //Invitee, meeting is confirmed and current answer is not accepted
                            //At add stage
                            System.out.println("This meeting is confirmed");
                            System.out.println("Meeting number: " + clientMeeting.getMeetingNumber());
                            System.out.println("Type 'Add_MeetingNumber' to add yourself to the meeting");

                        }


                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            else{
                System.out.println("Type in your new request");
            }
        }
    }

    private void sendRequest(Calendar calendar, int minimum, List<String> participants, String topic){

        InetSocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Create a RequestMessage
        RequestMessage requestMessage = new RequestMessage(countID.incrementAndGet(), calendar, minimum, participants, topic);

        //Add the sent request to my list
        synchronized (meetings){
            meetings.add(new ClientMeeting(requestMessage));
        }

        synchronized (availability){
            availability.put(CalendarUtil.calendarToString(calendar), true);
        }

        //Send the RequestMessage to the server
        UdpSend.sendMessage(requestMessage.serialize(), socketAddress);

    }

    private void sendAccept(int meetingNumber){

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && meetings.get(i).getState() == false){
                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(true);
                }

                AcceptMessage acceptMessage = new AcceptMessage(meetingNumber);
                UdpSend.sendMessage(acceptMessage.serialize(), socketAddress);

            }
        }

    }

    private void sendReject(int meetingNumber){

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && meetings.get(i).getState() == false){
                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(false);
                }

                RejectMessage rejectMessage = new RejectMessage(meetingNumber);
                UdpSend.sendMessage(rejectMessage.serialize(), socketAddress);
            }
        }

    }

    private void sendWithdraw(int meetingNumber){

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && meetings.get(i).getState() == true
                    && meetings.get(i).getUserType() == false){

                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(false);
                }

                WithdrawMessage withdrawMessage = new WithdrawMessage(meetingNumber);
                UdpSend.sendMessage(withdrawMessage.serialize(), socketAddress);

            }
        }

    }

    private void sendAdd(int meetingNumber){

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < meetings.size(); i++){
            if(meetingNumber == meetings.get(i).getMeetingNumber()){
                if(meetings.get(i).getUserType() == false) {
                    meetings.get(i).setCurrentAnswer(true);

                    AddMessage addMessage = new AddMessage(meetingNumber);
                    UdpSend.sendMessage(addMessage.serialize(), socketAddress);

                }
                return;
            }
        }

    }

    private void sendRequesterCancel(int meetingNumber){

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber){
                if(meetings.get(i).getUserType() == true && meetings.get(i).getState() == true){

                    RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage(meetingNumber);
                    UdpSend.sendMessage(requesterCancelMessage.serialize(), socketAddress);

                }

                return;
            }
        }

    }

    private void handleDenied(DeniedMessage message) {  //Room Unavailable Message

        //Check if request RQ# exists inside its list of request and is the owner
        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getRequestNumber() == message.getRequestNumber()){
                //If true, Delete the request that was just sent to the server
                synchronized (meetings) {
                    meetings.remove(i);
                }
                return;
            }
        }

        //If false, Server sent incorrect request
        System.out.println("Server sent denied for a non-existant request");

    }

    private void handleInvite(InviteMessage message) {

        SocketAddress socketAddress = null;
        try {
            socketAddress = new InetSocketAddress(InetAddress.getLocalHost(),Integer.parseInt(serverPort));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //Add the new request into your list and make it a standby status meeting
        ClientMeeting newMeeting = new ClientMeeting(message);

        if(!availability.containsKey(CalendarUtil.calendarToString(newMeeting.getCalendar()))){
            newMeeting.setCurrentAnswer(true);
            synchronized (meetings) {
                meetings.add(newMeeting);
            }

            //Send Accept
            UdpSend.sendMessage(new AcceptMessage(newMeeting.getMeetingNumber()).serialize(), socketAddress);

        } else {
            newMeeting.setCurrentAnswer(false);
            synchronized (meetings) {
                meetings.add(newMeeting);
            }

            //Send Reject
            UdpSend.sendMessage(new RejectMessage(newMeeting.getMeetingNumber()).serialize(), socketAddress);
        }

    }

    private void handleConfirm(ConfirmMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == message.getMeetingNumber()){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == false){
                    synchronized (meetings) {
                        meetings.get(i).receiveConfirmMessage(message);
                    }
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

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == message.getMeetingNumber()){
                if(meetings.get(i).getState() == true && meetings.get(i).getUserType() == true){
                    synchronized (meetings){
                        meetings.get(i).getAcceptedMap().put(Integer.parseInt(message.getSocketAddress()), true);
                    }
                }
            }
        }

    }

    private void handleRoomChange(RoomChangeMessage message) {
        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == message.getMeetingNumber()){
                if(meetings.get(i).getState() == true){
                    synchronized (meetings){
                        meetings.get(i).setRoomNumber(message.getNewRoomNumber());
                    }
                }
            }
        }
    }

    private void handleServerWidthdraw(ServerWidthdrawMessage message){
        for(int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).getMeetingNumber() == message.getMeetingNumber()) {
                if (meetings.get(i).getState() == true && meetings.get(i).getUserType() == true) {
                    synchronized (meetings) {
                        meetings.get(i).getAcceptedMap().remove(Integer.parseInt(message.getIpAddress()));
                    }
                }
            }
        }
    }

    public class ClientListen implements Runnable {

        public ClientListen() {
        }

        @Override
        public void run() {

            //Like server, will listen to ip
            /**Create new server and binds to a free port. From source of the internet
             * the range should be 49152 - 65535.*/

            /**The port address is chosen randomly*/
            try (DatagramSocket serverSocket = new DatagramSocket(Integer.parseInt(clientPort))) {
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

                    RoomChangeMessage roomChangeMessage = new RoomChangeMessage();
                    roomChangeMessage.deserialize(message);
                    handleRoomChange(roomChangeMessage);

                    break;
                case ServerWidthdraw:
                    ServerWidthdrawMessage serverWidthdrawMessage = new ServerWidthdrawMessage();
                    serverWidthdrawMessage.deserialize(message);
                    handleServerWidthdraw(serverWidthdrawMessage);
                    break;

            }

        }

    }

    private String getClientData() {

        String result = "";

        result += "" + "_"; //meetings ArrayList

        for(int i = 0; i < meetings.size(); i++){
            if(i == 0) {
                result += meetings.get(i).serialize();
                continue;
            }

            result += ";" + meetings.get(i).serialize();

        }

        result += "_";

        for (String s : availability.keySet()) { //Availability Hashmap
            result += s + ";";
        }

        return result;

    }

    public class ClientSave implements Runnable{

        @Override
        public void run() {

            while(true){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FileReaderWriter.WriteFile("saveFile_" + clientPort, getClientData(), false);
            }

        }

    }

    private void restoreFromSave(String saveFile) {

        ArrayList<String> messageList = FileReaderWriter.ReadFile(saveFile);

        String message = "";

        for(String msgPortion : messageList){
            message += msgPortion;
        }

        String[] subMessage = message.split("_");

        String[] meetings = subMessage[0].split(";");
        String[] availability = subMessage[1].split(";");

        for(String meeting : meetings){
            ClientMeeting newMeeting = new ClientMeeting();
            newMeeting.deserialize(meeting);
            this.meetings.add(newMeeting);
        }

        for(String available : availability){
            this.availability.put(available, true);
        }

    }

}

