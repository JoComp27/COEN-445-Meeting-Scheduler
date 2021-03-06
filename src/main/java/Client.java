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
    private List<String> ClientLog;

    public Client(String clientName) throws UnknownHostException {
        this.clientName = clientName;
        this.availability = new HashMap<>();

        this.meetings = new ArrayList<>();
        this.ClientLog = new ArrayList<>();

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

                        sendRequest(requestMessage.getCalendar(), requestMessage.getMinimum(), requestMessage.getParticipants(), requestMessage.getTopic());


                        break;
                    case Add:
                        AddMessage addMessage = new AddMessage();
                        addMessage.deserialize(inp);
                        sendAdd(addMessage.getMeetingNumber());

                        break;
                    case RequesterCancel:
                        RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage();
                        requesterCancelMessage.deserialize(inp);
                        sendRequesterCancel(requesterCancelMessage.getMeetingNumber());
                        break;
                    case Withdraw:
                        WithdrawMessage withdrawMessage = new WithdrawMessage();
                        withdrawMessage.deserialize(inp);
                        sendWithdraw(withdrawMessage.getMeetingNumber());
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

        private void sendRequest(Calendar calendar, int minimum, List<String> participants, String topic){

        //Create a RequestMessage
        RequestMessage requestMessage = new RequestMessage(countID.incrementAndGet(), calendar, minimum, participants, topic);

        //Add the sent request to my list
        synchronized (meetings){
            meetings.add(new ClientMeeting(requestMessage));
        }



        //Send the RequestMessage to the server
        UdpSend.sendMessage(requestMessage.serialize(), ds, serverAddress);

        Calendar cal = Calendar.getInstance();
        String currentTime = "Client[" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " "
                + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "]: ";
        FileReaderWriter.WriteFile("log", currentTime + "Request from '" + clientName + "' " + requestMessage.serialize() + "\n", true);
        ClientLog.add("Request from '" + clientName + "' " + requestMessage.serialize());

    }

    private void sendAccept(int meetingNumber){

        for(int i = 0 ; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == meetingNumber && !meetings.get(i).getState()){
                synchronized (meetings){
                    meetings.get(i).setCurrentAnswer(true);
                }

                AcceptMessage acceptMessage = new AcceptMessage(meetingNumber);
                UdpSend.sendMessage(acceptMessage.serialize(), ds, serverAddress);

                Calendar cal = Calendar.getInstance();
                String currentTime = "Client[" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " "
                        + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "]: ";
                FileReaderWriter.WriteFile("log", currentTime + "Accept from '" + clientName + "' " + acceptMessage.serialize() + "\n", true);
                ClientLog.add("Accept from '" + clientName + "' " + acceptMessage.serialize());

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

                Calendar cal = Calendar.getInstance();
                String currentTime = "Client[" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR) + " "
                        + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "]: ";
                FileReaderWriter.WriteFile("log", currentTime + "Reject from '" + clientName + "' " + rejectMessage.serialize() + "\n", true);
                ClientLog.add("Reject from '" + clientName + "' " + rejectMessage.serialize());
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

                WithdrawMessage withdrawMessage = new WithdrawMessage((meetingNumber));
                UdpSend.sendMessage(withdrawMessage.serialize(), ds, serverAddress);

                Calendar calendar = Calendar.getInstance();
                String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                        + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                FileReaderWriter.WriteFile("log", currentTime + "Add from '" + clientName + "' " + withdrawMessage.serialize() + "\n", true);
                ClientLog.add(currentTime + "Add from '" + clientName + "' " + withdrawMessage.serialize());

            }
        }

    }

    private void sendAdd(int meetingNumber){

        for(int i = 0; i < meetings.size(); i++){
            if(meetingNumber == meetings.get(i).getMeetingNumber()){
                if(!meetings.get(i).getUserType()) {
                    meetings.get(i).setCurrentAnswer(true);

                    AddMessage addMessage = new AddMessage((meetingNumber));
                    UdpSend.sendMessage(addMessage.serialize(), ds, serverAddress);

                    Calendar calendar = Calendar.getInstance();
                    String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                    FileReaderWriter.WriteFile("log", currentTime + "Add from '" + clientName + "' " + addMessage.serialize() + "\n", true);
                    ClientLog.add(currentTime + "Add from '" + clientName + "' " + addMessage.serialize());

                }

                return;
            }
        }

    }

    private void sendRequesterCancel(int meetingNumber){

        for(int i = 0; i < meetings.size(); i++){

            if(meetings.get(i).getMeetingNumber() == meetingNumber){

                if(meetings.get(i).getUserType() && meetings.get(i).getState()){

                    RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage((meetingNumber));
                    UdpSend.sendMessage(requesterCancelMessage.serialize(), ds, serverAddress);

                    Calendar calendar = Calendar.getInstance();
                    String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                    FileReaderWriter.WriteFile("log", currentTime + "Cancel from '" + clientName + "' " + requesterCancelMessage.serialize() + "\n", true);
                    ClientLog.add(currentTime + "Cancel from '" + clientName + "' " + requesterCancelMessage.serialize());

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

        Calendar calendar = Calendar.getInstance();
        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
        FileReaderWriter.WriteFile("log", currentTime + "Register from '" + clientName + "' " + registerMessage.serialize() + "\n", true);
        ClientLog.add(currentTime + "Register from '" + clientName + "' " + registerMessage.serialize());

    }

    private void handleDenied(DeniedMessage message) {  //Room Unavailable Message

        //Check if request RQ# exists inside its list of request and is the owner
        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getRequestNumber() == message.getRequestNumber()){
                //If true, Delete the request that was just sent to the server
                synchronized (meetings) {
                    meetings.remove(i);

                    Calendar calendar = Calendar.getInstance();
                    String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                    FileReaderWriter.WriteFile("log", currentTime + "Denied " + message.serialize() + "\n", true);
                    ClientLog.add(currentTime + "Denied " + message.serialize());

                }
                return;
            }
        }

        //If false, Server sent incorrect request
        System.out.println("Server sent denied for a non-existant request");

    }

    private void handleInvite(InviteMessage message) {
        //Add the new request into your list and make it a standby status meeting
        ClientMeeting newMeeting = new ClientMeeting(message);

        Calendar calendar = Calendar.getInstance();
        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
        FileReaderWriter.WriteFile("log", currentTime + "Invite for '" + clientName + "'" + message.serialize() + "\n", true);
        ClientLog.add(currentTime + "Invite for '" + clientName + "'" + message.serialize());

        if (!message.getRequester().equals(clientName)) {
            if (!availability.containsKey(CalendarUtil.calendarToString(newMeeting.getCalendar()))) {
                newMeeting.setCurrentAnswer(true);
                synchronized (meetings) {

                    meetings.add(newMeeting);

                }
                synchronized (availability) {
                    availability.put(CalendarUtil.calendarToString(newMeeting.getCalendar()), true);
                }

                //Send Accept

                System.out.println("Accepted meeting");

                sendAccept(newMeeting.getMeetingNumber());
                //UdpSend.sendMessage(new AcceptMessage(newMeeting.getMeetingNumber()).serialize(), ds, serverAddress);


            } else {
                newMeeting.setCurrentAnswer(false);
                synchronized (meetings) {
                    meetings.add(newMeeting);
                }

                //Send Reject

                System.out.println("Rejected meeting");

                sendReject(newMeeting.getMeetingNumber());
                //UdpSend.sendMessage(new RejectMessage(newMeeting.getMeetingNumber()).serialize(), ds, serverAddress);

            }

        } else {

            for (int i = 0; i < meetings.size(); i++) {
                meetings.get(i).setMeetingNumber(message.getMeetingNumber());

            }

            //Send Accept
            UdpSend.sendMessage(new AcceptMessage(newMeeting.getMeetingNumber()).serialize(), ds, serverAddress);

        }

    }

    private void handleConfirm(ConfirmMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == Integer.valueOf(message.getMeetingNumber())){
                if(meetings.get(i).getState() == false && meetings.get(i).getUserType() == false){
                    synchronized (meetings) {
                        meetings.get(i).receiveConfirmMessage(message);
                        Calendar calendar = Calendar.getInstance();
                        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                        FileReaderWriter.WriteFile("log", currentTime + "Confirm from '" + clientName + "' " + message.serialize() + "\n", true);
                        ClientLog.add(currentTime + "Confirm from '" + clientName + "' " + message.serialize());
                    }
                }
                return;
            }
        }




    }

    private void handleServerCancel(ServerCancelMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == Integer.valueOf(message.getMeetingNumber())){
                if(!meetings.get(i).getState() && !meetings.get(i).getUserType()) {
                    System.out.println("Meeting " + message.getMeetingNumber() + " was cancelled for this reason : " + message.getReason());
                    synchronized (meetings){
                        meetings.remove(i);
                        Calendar calendar = Calendar.getInstance();
                        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                        FileReaderWriter.WriteFile("log", currentTime + "Cancel for '" + clientName + "'" + message.serialize() + "\n", true);
                        ClientLog.add(currentTime + "Cancel for '" + clientName + "'" + message.serialize());
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

                    Calendar calendar = Calendar.getInstance();
                    String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                    FileReaderWriter.WriteFile("log", currentTime + "Scheduled for '" + clientName + "' " + message.serialize() + "\n", true);
                    ClientLog.add(currentTime + "Scheduled for '" + clientName + "' " + message.serialize());

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
                        Calendar calendar = Calendar.getInstance();
                        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                        FileReaderWriter.WriteFile("log", currentTime + "Not scheduled for '" + clientName + "' " + message.serialize() + "\n", true);
                        ClientLog.add(currentTime + "Not scheduled for '" + clientName + "' " + message.serialize());
                    }
                }
            }
        }

    }

    private void handleAdded(AddedMessage message) {

        for(int i = 0; i < meetings.size(); i++){
            if(meetings.get(i).getMeetingNumber() == Integer.valueOf(message.getMeetingNumber())){
                if(meetings.get(i).getState() && meetings.get(i).getUserType()){
                    synchronized (meetings){
                        meetings.get(i).getAcceptedMap().put(message.getSocketAddress(), true);

                        Calendar calendar = Calendar.getInstance();
                        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                        FileReaderWriter.WriteFile("log", currentTime + "Add for '" + clientName + "' " + message.serialize() + "\n", true);
                        ClientLog.add(currentTime + "Add for '" + clientName + "' " + message.serialize());
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

                        Calendar calendar = Calendar.getInstance();
                        String currentTime = "Client[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                        FileReaderWriter.WriteFile("log", currentTime + "Room change for '" + clientName + "' " + message.serialize() + "\n", true);
                        ClientLog.add(currentTime + "Room change for '" + clientName + "' " + message.serialize());
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

