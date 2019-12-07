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

    private final int serverPort = 9997;

    private String clientName;

    private static DatagramSocket ds;

    private final InetSocketAddress serverAddress;

    private ArrayList<ClientMeeting> meetings;
    private HashMap<String, Boolean> availability;

    public Client(String clientName) throws UnknownHostException {
        this.clientName = clientName;
        this.availability = new HashMap<>();

        this.meetings = new ArrayList<>();

        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        serverAddress = new InetSocketAddress(InetAddress.getLocalHost(), serverPort);

    }

    public static void main(String args[]) throws IOException {

        if(args.length < 1){
            System.err.println("MISSING ARGUMENTS WHILE STARTING CLIENT");
            return;
        }

        String clientName = args[0];

        Client client = new Client(clientName);

        String fileName = "saveFile_" + clientName;

        //Checking if previous
        File saveFile = new File(fileName + ".txt");

        if(saveFile.exists()){

            //Add CLI to check if user wants to restore user or not.

            System.out.println("It seems that a restore file is available and could be loaded onto the" +
                    "client\nDo you wish to restore it?");

            String answer = "";

            Scanner scanner = new Scanner(System.in);

            while(!answer.equals("y") && !answer.equals("n")){

                answer = scanner.nextLine().trim();

                switch (answer) {
                    case "y":
                        System.out.println("Save will be restored for client " + clientName);
                        client.restoreFromSave(fileName);
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
        String first = "Request_1_2019,10,6,8_2_59000_asd";
        RequestMessage firstRequest = new RequestMessage();
        firstRequest.deserialize(first);
        UdpSend.sendMessage(firstRequest.serialize(), ds, serverAddress);
//        byte buf[] = message.getBytes();
//        byte[] buffer = new byte[100];
//        DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9997);
//        ds.send(DpSend);
//        System.out.println("MESSAGE SENT");



    }

    public void run() throws IOException {

        ClientListen clientListen = new ClientListen(); //Adding thread for client to listen to server messages
        Thread listenThread = new Thread(clientListen);
        listenThread.start();

        ClientSave clientSave = new ClientSave(); //Adding thread for client to save it's progress
        Thread saveThread = new Thread(clientSave);
        saveThread.start();

        sendRegistrationMessage();

        System.out.println("Local port is: " + ds.getLocalPort());
        Scanner sc = new Scanner(System.in);
        // loop while user not enters "bye"
        while (true) {
            String inp = sc.nextLine();

            if (!inp.isEmpty()) {

                String[] inputMessage = inp.trim().split("\\$");
                //int messageType = Integer.parseInt(inputMessage[0]);
                System.out.println("InputMessage: " + inputMessage[0]);
                //System.out.println("receivedMessage Value of: " + RequestType.valueOf(inputMessage[0]));
                RequestType receivedRequestType = RequestType.valueOf(inputMessage[0]);

                switch (receivedRequestType) {
                    case Request:
                        RequestMessage requestMessage = new RequestMessage();
                        requestMessage.deserialize(inp);
                        UdpSend.sendMessage(requestMessage.serialize(), ds, serverAddress);
                        break;
                    default:
                        System.out.println("Request type does not correspond. Exiting.");
                        break;
                }

                //sendMessageToServer(inp);
                // break the loop if user enters "bye"
                if (inp.equals("bye"))
                    break;
            }
        }

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

                        if (clientMeeting.getUserType() && clientMeeting.getState()) {
                            //Organizer and meeting is confirmed
                            //Organizer can cancel the meeting
                            System.out.println("This meeting is confirmed");
                            System.out.println("Meeting number: " + clientMeeting.getMeetingNumber());
                            System.out.println("Type 'Cancel_MeetingNumber' to cancel the meeting");

                        }
                        else if (!clientMeeting.getUserType() && clientMeeting.getState() && clientMeeting.isCurrentAnswer()) {
                            //Invitee, meeting is confirmed and current answer is accepted
                            //At confirm message, meeting is confirmed, can only withdraw
                            System.out.println("This meeting is confirmed");
                            System.out.println("Meeting number: " + clientMeeting.getMeetingNumber());
                            System.out.println("Type 'Withdraw_MeetingNumber' to withdraw from the meeting");
                        }
                        else if (!clientMeeting.getUserType() && clientMeeting.getState() && !clientMeeting.isCurrentAnswer()){
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
        UdpSend.sendMessage(requestMessage.serialize(), ds, serverAddress);

    }

    private void sendAccept(int meetingNumber){

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && !meetings.get(i).getState()){
                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(true);
                }

                AcceptMessage acceptMessage = new AcceptMessage(meetingNumber);
                UdpSend.sendMessage(acceptMessage.serialize(), ds, serverAddress);

            }
        }

    }

    private void sendReject(int meetingNumber){

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && !meetings.get(i).getState()){
                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(false);
                }

                RejectMessage rejectMessage = new RejectMessage(meetingNumber);
                UdpSend.sendMessage(rejectMessage.serialize(), ds, serverAddress);
            }
        }

    }

    private void sendWithdraw(int meetingNumber){

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && meetings.get(i).getState()
                    && !meetings.get(i).getUserType()){

                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(false);
                }

                WithdrawMessage withdrawMessage = new WithdrawMessage(meetingNumber);
                UdpSend.sendMessage(withdrawMessage.serialize(), ds, serverAddress);

            }
        }

    }

    private void sendAdd(int meetingNumber){

        for(int i = 0; i < meetings.size(); i++){
            if(meetingNumber == meetings.get(i).getMeetingNumber()){
                if(!meetings.get(i).getUserType()) {
                    meetings.get(i).setCurrentAnswer(true);

                    AddMessage addMessage = new AddMessage(meetingNumber);
                    UdpSend.sendMessage(addMessage.serialize(), ds, serverAddress);

                }
                return;
            }
        }

    }

    private void sendRequesterCancel(int meetingNumber){

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber){
                if(meetings.get(i).getUserType() && meetings.get(i).getState()){

                    RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage(meetingNumber);
                    UdpSend.sendMessage(requesterCancelMessage.serialize(), ds, serverAddress);

                }

                return;
            }
        }

    }

    private void sendRegistrationMessage(){

        RegisterMessage registerMessage = null;

        try {
            registerMessage = new RegisterMessage(clientName, new InetSocketAddress(InetAddress.getLocalHost(), ds.getLocalPort()));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        UdpSend.sendMessage(registerMessage.serialize(), ds, serverAddress);

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
        System.out.println("Got Invite");
        //Add the new request into your list and make it a standby status meeting
        ClientMeeting newMeeting = new ClientMeeting(message);
        if(!availability.containsKey(CalendarUtil.calendarToString(newMeeting.getCalendar()))){
            newMeeting.setCurrentAnswer(true);
            synchronized (meetings) {
                meetings.add(newMeeting);
            }

            //Send Accept
            System.out.println("Accepted meeting");

            UdpSend.sendMessage(new AcceptMessage(newMeeting.getMeetingNumber()).serialize(), ds, serverAddress);


        } else {
            newMeeting.setCurrentAnswer(false);
            synchronized (meetings) {
                meetings.add(newMeeting);
            }

            //Send Reject
            System.out.println("Rejected meeting");
            UdpSend.sendMessage(new RejectMessage(newMeeting.getMeetingNumber()).serialize(), ds, serverAddress);
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
                if(!meetings.get(i).getState() && !meetings.get(i).getUserType()) {
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
                if(!meetings.get(i).getState() && meetings.get(i).getUserType()){
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
                if(!meetings.get(i).getState() && meetings.get(i).getUserType()) {
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
                if(meetings.get(i).getState() && meetings.get(i).getUserType()){
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
                if(meetings.get(i).getState()){
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
                if (meetings.get(i).getState() && meetings.get(i).getUserType()) {
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
                byte[] buffer = new byte[100];
                /**Messages here and sends to client*/
                while (true) {
                    DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data

                    try {
                        ds.receive(DpReceive);        //Receive Data in Buffer
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    String message = new String(DpReceive.getData(), 0, DpReceive.getLength());
                    System.out.println("Server says: " + message);
                    /**NEED TO ADD IN TIMEOUT OPTIONS TO RESEND THE MESSAGE. HAVE YET TO
                     * COMPLETE THIS PORTION OF THE CODE
                     *
                     * Add in Thread and feed in the message*/
                    //This would be the thread managing method
                    new Thread(new ClientHandle(message)).start();

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

            String[] receivedMessage = message.split("\\$");
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

    private String serialize() {

        String result = ""; //meetings ArrayList

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
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FileReaderWriter.WriteFile("saveFile_" + clientName, serialize(), false);
            }

        }

    }

    private void restoreFromSave(String saveFile) {

        ArrayList<String> messageList = FileReaderWriter.ReadFile(saveFile);

        String message = "";

        for(String msgPortion : messageList){
            message += msgPortion;
        }

        System.out.println("Message: " + message);

        String[] subMessage = message.split("_");

        if(subMessage.length > 0 && !subMessage[0].isEmpty()){
            String[] meetings = subMessage[0].split(";");

            for(String meeting : meetings){
                ClientMeeting newMeeting = new ClientMeeting();
                newMeeting.deserialize(meeting);
                this.meetings.add(newMeeting);
            }
        }

       if(subMessage.length > 0 && !subMessage[1].isEmpty()) {
           String[] availability = subMessage[1].split(";");

           for (String available : availability) {
               this.availability.put(available, true);
           }
       }

    }

}

