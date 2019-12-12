import Tools.CalendarUtil;
import Tools.FileReaderWriter;
import Tools.UdpSend;
import requests.*;

import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class Server implements Runnable{

    private HashMap<String, Boolean[]> scheduleMap;     //String Date and Time, Boolean Array of size 2: True = Booked, False = Not Booked.
    private HashMap<String, Meeting> meetingMap;        //String MeetingNumber, Meeting Class
    private HashMap<String, InetSocketAddress> clientAddressMap;         //String ClientName, InetSocketAddress client socket address
    private List<String> ServerLog;


    private DatagramSocket serverSocket;

    public Server (){
        this.scheduleMap = new HashMap<>();
        this.meetingMap = new HashMap<>();
        this.clientAddressMap = new HashMap<>();
        this.ServerLog = new ArrayList<>();

        try {
            this.serverSocket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(), 9997));
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        System.out.println("SERVER LAUNCHED");

        Server server = new Server();

        //Checking if previous
        File saveFile = new File("server.txt");

        if(saveFile.exists()){

            //Add CLI to check if user wants to restore user or not.

            System.out.println("It seems that a restore file is available and could be loaded onto the" +
                    "server\n Do you wish to restore it?");

            String answer = "";

            Scanner scanner = new Scanner(System.in);

            while(!answer.equals("y") && !answer.equals("n")){

                answer = scanner.nextLine().trim();

                switch (answer) {
                    case "y":
                        System.out.println("Save will be restored for server");
                        server.loadServer();
                        break;
                    case "n":
                        System.out.println("Save will not be restored for client");
                        break;
                    default:
                        System.out.println("INVALID SAVE RESTORE ANSWER");
                }
            }


        }

        server.run();
    }

    @Override
    public void run() {
        /**Create new server and binds to a free port. From source of the internet
         * the range should be 49152 - 65535.*/

        ServerSave serverSave = new ServerSave();
        Thread saveThread = new Thread(serverSave);
        saveThread.start();

    	try {
			System.out.println("Server Address: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
        /**The port address is chosen randomly*/
        try {

            /**Messages here and sends to client*/

            ServerCommand serverCommand = new ServerCommand();
            Thread threadServerCommand = new Thread(serverCommand);
            threadServerCommand.start();

            while(true){
                byte[] buffer = new byte[100];
            	System.out.println("-------------- SERVER STARTED TO LISTEN --------------");
                DatagramPacket DpReceive = new DatagramPacket(buffer, buffer.length);   //Create Datapacket to receive the data
                serverSocket.receive(DpReceive);        //Receive Data in Buffer

                //System.out.println(DpReceive.getData());
                //System.out.println(DpReceive.getAddress());
                String message = new String(DpReceive.getData());


                //System.out.println("DpReceive getAddress" + DpReceive.getAddress());
                //System.out.println("DpReceive socket address" + DpReceive.getSocketAddress());

                System.out.println("Client says: " + message);

                int port = DpReceive.getPort();
                System.out.println("Port: " + port);



                //threadServerHandle.start();
                //System.out.println("DpReceive Socket Address: " + DpReceive.getSocketAddress());
                ServerHandle serverHandle = new ServerHandle(message, port, DpReceive.getSocketAddress());

                Thread threadServerHandle = new Thread(serverHandle);
                //= new ServerHandle(message, port);
                //threadServerHandle = new Thread(serverHandle);
                threadServerHandle.start();


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

    public class ServerHandle implements Runnable{
        String message;
        int port;
        SocketAddress socketAddress;
        String messageToClient = "Initial value";

        public ServerHandle(String message, int port, SocketAddress socketAddress){
            this.message = message;
            this.port = port;
            this.socketAddress = socketAddress;

        }

        /**Takes the message received from the datagramPacket and separate the message using the "_"*/
        @Override
        public void run() {
            String[] receivedMessage = message.split("\\$");
            System.out.println("The received message: " + message);


            int messageType = Integer.parseInt(receivedMessage[0]);
            System.out.println("Message type: " + messageType);
            RequestType receivedRequestType = RequestType.values()[messageType];
            System.out.println(receivedRequestType);


            FileReaderWriter file = new FileReaderWriter();
            String currentDir = System.getProperty("user.dir");
            String filePath = currentDir + "log.txt";
            Calendar calendar = Calendar.getInstance();
            String currentTime = "Server[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                    + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";

            /**Cases to how to treat each of the requestTypes.*/
            switch(receivedRequestType){
                case Register:
                    RegisterMessage registerMessage = new RegisterMessage();
                    registerMessage.deserialize(message);
                    clientAddressMap.put(registerMessage.getClientName(), registerMessage.getClientSocketAddress());
                    FileReaderWriter.WriteFile("log", currentTime + "Registered '" + registerMessage.getClientName() + "'" + "\n", true);
                    ServerLog.add(currentTime + "Registered '" + registerMessage.getClientName() + "'");

                    break;
                case Request:

                    //System.out.println(" Receiving Port: " + port);

                    RequestMessage requestMessage = new RequestMessage();

                    requestMessage.deserialize(message);

                    //clientAddressMap.put(requestMessage.getParticipants().get(0), (InetSocketAddress) socketAddress);


                    String time = CalendarUtil.calendarToString(requestMessage.getCalendar());

                    String name = "";
                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                name = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                    }


                    Meeting meeting = new Meeting(requestMessage, 0, new HashMap<String, Boolean>(), 0, name, 0);


                    //If this meeting does not exist yet
                    if(!scheduleMap.containsKey(time)){


                        //Make first room taken
                        synchronized(scheduleMap) {
                            scheduleMap.put(time, new Boolean[]{true, false});
                        }

                        messageToClient = "Room 1 is available";

                        meeting.setRoomNumber(1);
                        //Set all participants accepted value to false (none have accepted in this stage)
                        meeting.setAcceptedMap();
                        //Add meeting to hashmap that lists all existing meetings
                        synchronized(meetingMap) {
                            meetingMap.put(Integer.toString(meeting.getId()), meeting);
                        }

                        InviteMessage inviteMessage = new InviteMessage();
                        inviteMessage.setMeetingNumber(meeting.getId());
                        inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                        inviteMessage.setTopic(meeting.getRequestMessage().getTopic());
                        inviteMessage.setRequester(meeting.getOrganizer().trim());


                        for(String s: meeting.getRequestMessage().getParticipants()){
                            socketAddress = clientAddressMap.get(s);

                            //If you add participant in list that does not have a running client
                            if(socketAddress == null){
                                continue;
                            }
                            UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                            FileReaderWriter.WriteFile("log", currentTime + "Invited '" + s + "'" + inviteMessage.serialize() + "\n", true);
                            ServerLog.add(currentTime + "Invited '" + s + "'" + inviteMessage.serialize());

                        }


                        /**Writes the message in the log file.*/
                        file.WriteFile(filePath, message, true);
                    }
                    else if(scheduleMap.containsKey(time)){
                        //If first room not taken
                        if(!scheduleMap.get(time)[0]) {
                            //Set room number to 1
                            meeting.setRoomNumber(1);
                            //Set all participants accepted value to false (none have accepted in this stage)
                            meeting.setAcceptedMap();
                            synchronized (meetingMap) {
                                meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            }
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[0] = true;

                            synchronized(scheduleMap) {
                                scheduleMap.put(time, roomArray);
                            }
                            messageToClient = "Room 1 is available";

                            InviteMessage inviteMessage = new InviteMessage();
                            inviteMessage.setMeetingNumber(meeting.getId());
                            inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                            inviteMessage.setTopic(meeting.getRequestMessage().getTopic().trim());
                            inviteMessage.setRequester(meeting.getOrganizer().trim());


                            for(String s: meeting.getRequestMessage().getParticipants()){
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if(socketAddress == null){
                                    continue;
                                }
                                UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Invited '" + s + "'" + inviteMessage.serialize() + "\n", true);
                                ServerLog.add(currentTime + "Invited '" + s + "'" + inviteMessage.serialize());

                            }
                            //Create meeting
                            //Add meeting to meetingMap
                        }
                        else if(!scheduleMap.get(time)[1]){
                            //Set room number to 2
                            meeting.setRoomNumber(2);
                            //Set all participants accepted value to false (none have accepted in this stage)
                            meeting.setAcceptedMap();

                            synchronized(meetingMap) {
                                meetingMap.put(Integer.toString(meeting.getId()), meeting);
                            }
                            Boolean roomArray[] = scheduleMap.get(time);
                            roomArray[1] = true;
                            synchronized(scheduleMap) {
                                scheduleMap.put(time, roomArray);
                            }
                            messageToClient = "Room 2 is available";

                            InviteMessage inviteMessage = new InviteMessage();
                            inviteMessage.setMeetingNumber(meeting.getId());
                            inviteMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                            inviteMessage.setTopic(meeting.getRequestMessage().getTopic().trim());
                            inviteMessage.setRequester(meeting.getOrganizer().trim());

                            for(String s: meeting.getRequestMessage().getParticipants()){
                                socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if(socketAddress == null){
                                    continue;
                                }
                                UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Invited '" + s + "'" + inviteMessage.serialize() + "\n", true);
                                ServerLog.add("Invited '" + s + "'" + inviteMessage.serialize());

                            }

                        }
                        else{
                            DeniedMessage deniedMessage = new DeniedMessage();
                            deniedMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                            deniedMessage.setUnavailable("Unavailable");

                            UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                            FileReaderWriter.WriteFile("log", currentTime + "Denied " + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber() + "\n", true);
                            ServerLog.add(currentTime + "Denied " + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber());

                            break;
                        }
                    }
                    else{
                        DeniedMessage deniedMessage = new DeniedMessage();
                        deniedMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                        deniedMessage.setUnavailable("Unavailable");

                        UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + "Denied " + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber() + "\n", true);
                        ServerLog.add(currentTime + "Denied " + deniedMessage.getUnavailable() + " " + deniedMessage.getRequestNumber());

                        break;
                    }

                    //Delay before checking the minimum requirement
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //If the accepted numbers >= minimum
                    if(meeting.getAcceptedParticipants() >= meeting.getRequestMessage().getMinimum()){
                        //Send confirm messages to all participants (including organizer)
                        ConfirmMessage confirmMessage = new ConfirmMessage();
                        confirmMessage.setMeetingNumber(meeting.getId());
                        confirmMessage.setRoomNumber(meeting.getRoomNumber());

                        for(String s: meeting.getRequestMessage().getParticipants()){
                            socketAddress = clientAddressMap.get(s);

                            //If you add participant in list that does not have a running client
                            if(socketAddress == null){
                                continue;
                            }
                            UdpSend.sendMessage(confirmMessage.serialize(), serverSocket, socketAddress);

                            FileReaderWriter.WriteFile("log", currentTime + "Confirmed '" + s + "' " + confirmMessage.serialize() + "\n", true);
                            ServerLog.add("Confirmed '" + s + "' " + confirmMessage.serialize());

                        }

                        //Send to organizer of Scheduled meeting
                        ScheduledMessage scheduledMessage = new ScheduledMessage();
                        scheduledMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                        scheduledMessage.setMeetingNumber(meeting.getId());
                        scheduledMessage.setRoomNumber(meeting.getRoomNumber());
                        String person = "";
                        List<String> listAccepted = new ArrayList<>();
                        for (Map.Entry<String, Boolean> entry : meeting.getAcceptedMap().entrySet()) {
                            Boolean value = entry.getValue();
                            if (value == true && entry.getValue().equals(value)) {
                                person = entry.getKey();

                                listAccepted.add(person);

                            }
                        }
                        String[] arrayAccepted = new String[listAccepted.size()];
                        for (int i = 0; i < listAccepted.size(); i++) {
                            arrayAccepted[i] = listAccepted.get(i);
                        }
                        scheduledMessage.setListOfConfirmedParticipants(arrayAccepted);
                        for (String s : clientAddressMap.keySet()) {
                            if (meeting.getOrganizer().equals(s)){
                                socketAddress = clientAddressMap.get(s);
                                UdpSend.sendMessage(scheduledMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Scheduled '" + s + "' " + scheduledMessage.serialize() + "\n", true);
                                ServerLog.add("Scheduled '" + s + "' " + scheduledMessage.serialize());

                            }
                        }


                    }
                    //If accepted numbers < minimum
                    else{
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage();
                        serverCancelMessage.setMeetingNumber(meeting.getId());
                        serverCancelMessage.setReason("Number lower than minimum");
                        String person = "";

                        //Find the name if they accepted (true)
                        for (Map.Entry<String, Boolean> entry : meeting.getAcceptedMap().entrySet()) {
                            Boolean value = entry.getValue();
                            if (value == true && entry.getValue().equals(value)) {
                                person = entry.getKey();

                                socketAddress = clientAddressMap.get(person);

                                //If you add participant in list that does not have a running client
                                if (socketAddress == null) {
                                    continue;
                                }
                                UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Canceled '" + person + "' " + serverCancelMessage.serialize() + "\n", true);
                                ServerLog.add("Canceled '" + person + "' " + serverCancelMessage.serialize());
                            }
                        }

                        //Remove that meeting from the meetingMap
                        synchronized (meetingMap) {
                            meetingMap.remove(Integer.toString(meeting.getId()));
                        }

                        //Send to organizer of Not scheduled meeting
                        NotScheduledMessage notScheduledMessage = new NotScheduledMessage();
                        notScheduledMessage.setRequestNumber(meeting.getRequestMessage().getRequestNumber());
                        notScheduledMessage.setCalendar(meeting.getRequestMessage().getCalendar());
                        notScheduledMessage.setMinimum(meeting.getRequestMessage().getMinimum());

                        String person2 = "";
                        List<String> listAccepted = new ArrayList<>();
                        for (Map.Entry<String, Boolean> entry : meeting.getAcceptedMap().entrySet()) {
                            Boolean value = entry.getValue();
                            if (value == true && entry.getValue().equals(value)) {
                                person2 = entry.getKey();

                                listAccepted.add(person2);

                            }
                        }

                        notScheduledMessage.setParticipants(listAccepted);
                        notScheduledMessage.setTopic(meeting.getRequestMessage().getTopic());
                        for (String s : clientAddressMap.keySet()) {
                            if (meeting.getOrganizer().equals(s)){
                                socketAddress = clientAddressMap.get(s);
                                UdpSend.sendMessage(notScheduledMessage.serialize(), serverSocket, socketAddress);

                                FileReaderWriter.WriteFile("log", currentTime + "Not scheduled '" + s + "' " + notScheduledMessage.serialize() + "\n", true);
                                ServerLog.add("Not scheduled '" + s + "' " + notScheduledMessage.serialize());

                            }
                        }


                    }


                    break;
                case Accept:
                    AcceptMessage acceptMessage = new AcceptMessage();
                    acceptMessage.deserialize(message);
                    String meetingNumberAccept = Integer.toString(acceptMessage.getMeetingNumber());

                    Meeting acceptMeeting = null;
                    boolean foundMatchAccept = false;
                    for(String typeKey : meetingMap.keySet()){
                        String key = typeKey.toString();
                        String value = meetingMap.get(typeKey).getRequestMessage().getParticipants().toString();
                        System.out.println("Hashmap for meetings");
                        System.out.println(key + ": " + value);
                    }

                    String participantName = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantName = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }
                    //System.out.println("Meeting number is: " + meetingNumberAccept);

                    //Go through all the participants in the existing meetings
                    if(meetingMap.containsKey(meetingNumberAccept)) {
                        //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                        for (int i = 0; i < meetingMap.get(meetingNumberAccept).getRequestMessage().getParticipants().size(); i++) {
                            if (meetingMap.get(meetingNumberAccept).getRequestMessage().getParticipants().get(i).equals(participantName)) {
                                //System.out.println("In here: " + participantName);
                                acceptMeeting = meetingMap.get(meetingNumberAccept);
                                foundMatchAccept = true;
                            }
                        }
                    }

                    if(!foundMatchAccept){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }


                    //Check if client is in the meeting AND if they already accepted the meeting
                    //System.out.println(participantName + " This participant says: " + acceptMeeting.getAcceptedMap().get(participantName));
                    if(acceptMeeting.getAcceptedMap().containsKey(participantName) && !acceptMeeting.getAcceptedMap().get(participantName)){
                        synchronized(acceptMeeting) {
                            //Increment accepted count
                            acceptMeeting.incrementAcceptedParticipants();
                            acceptMeeting.incrementAnsweredNumber();
                            //Make accepted boolean true
                            //System.out.println("You are now true: " + participantName);
                            acceptMeeting.getAcceptedMap().replace(participantName, true);
                        }

                        FileReaderWriter.WriteFile("log", currentTime + "Accepted '" + participantName + "' " + "\n", true);
                        ServerLog.add("Accepted '" + participantName + "' ");
                    }

                    break;
                case Reject:
                    RejectMessage rejectMessage = new RejectMessage();
                    rejectMessage.deserialize(message);
                    String meetingNumberReject = Integer.toString(rejectMessage.getMeetingNumber());

                    Meeting rejectMeeting = null;
                    boolean foundMatchReject = false;

                    String participantName2 = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantName2 = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }


                        //Go through all the participants in the existing meetings
                        for (int j = 0; j < meetingMap.size(); j++) {
                            //If the client is a valid participant, the meeting that will be manipulated will be set to the participant's meeting
                            for(int i = 0; i < meetingMap.get(meetingNumberReject).getRequestMessage().getParticipants().size(); i++) {
                                if (meetingMap.containsKey(meetingNumberReject) && meetingMap.get(meetingNumberReject).getRequestMessage().getParticipants().get(i).equals(participantName2)) {
                                    rejectMeeting = meetingMap.get(meetingNumberReject);
                                    foundMatchReject = true;
                                }
                            }
                        }


                    if(!foundMatchReject){
                        messageToClient = "You are not in a scheduled meeting";
                        break;
                    }

                    //Check if client is in the meeting AND if they already accepted the meeting
                    if(rejectMeeting.getAcceptedMap().containsKey(participantName2) && !rejectMeeting.getAcceptedMap().get(participantName2)){
                        messageToClient = "You have rejected the meeting";
                        rejectMeeting.incrementAnsweredNumber();

                        FileReaderWriter.WriteFile("log", currentTime + "Rejected '" + participantName2 + "'" + "\n", true);
                        ServerLog.add("Rejected '" + participantName2 + "'");
                    }
                    else{
                        //If client has already accepted, they cannot Reject
                        messageToClient = "You cannot send this message";
                    }



                    break;
                case Withdraw:
                    WithdrawMessage withdrawMessage = new WithdrawMessage();
                    String theWithdrawMessage = message.trim();
                    withdrawMessage.deserialize(theWithdrawMessage);
                    String withdrawMeetingNumber = Integer.toString(withdrawMessage.getMeetingNumber());
                    //int withdrawMeetingNumberINT = withdrawMessage.getMeetingNumber();
                    List<String> NotAcceptedParticipants = new ArrayList<>();
                    List<SocketAddress> NotAcceptedSocketAddress = new ArrayList<>();
                    List<String> acceptedParticipants = new ArrayList<>();
                    List<SocketAddress> acceptedSocketAddress = new ArrayList<>();
                    SocketAddress hostSocketAddressWithdraw = null;
                    String hostNameWithdraw = "";
                    String participantWithdraw = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantWithdraw = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }

                    System.out.println("Name: " + participantWithdraw);

                    /**If the meeting number exists*/
                    if (meetingMap.containsKey(withdrawMeetingNumber)){
                        Meeting withdrawMeeting = meetingMap.get(withdrawMeetingNumber);

                        for (String s : clientAddressMap.keySet()){
                            if (withdrawMeeting.getOrganizer().equals(s)){
                                hostSocketAddressWithdraw = clientAddressMap.get(s);
                                hostNameWithdraw = s;
                            }
                        }
                        //System.out.println("Host Name: " + hostNameWithdraw);
                        //System.out.println("getOrganizer: " + withdrawMeeting.getOrganizer());
                        //System.out.println("withdrawMeeting.getOrganizer() != hostNameWithdraw: " + withdrawMeeting.getOrganizer() != hostNameWithdraw);

                        /**If the client is invited in the meeting*/
                        if (withdrawMeeting.getAcceptedMap().containsKey(participantWithdraw)){
                            /**If the client is NOT the Host.*/
                            if (withdrawMeeting.getOrganizer() != participantWithdraw){
                                /**Withdraws the client that sent the withdraw command from the meeting
                                 * CHECK IF IT SENDS TO ALL OTHER CLIENTS */
                                synchronized (withdrawMeeting){
                                    withdrawMeeting.getAcceptedMap().remove(participantWithdraw);
                                }
                                System.out.println(withdrawMeeting.getAcceptedMap());
                                ServerWidthdrawMessage serverWidthdrawMessage = new ServerWidthdrawMessage(Integer.valueOf(withdrawMeetingNumber), participantWithdraw);
                                System.out.println("Removed: " + participantWithdraw);
                                UdpSend.sendMessage(serverWidthdrawMessage.serialize(), serverSocket, hostSocketAddressWithdraw);

                                FileReaderWriter.WriteFile("log", currentTime + "Withdrawn '" + participantWithdraw +"' " + serverWidthdrawMessage.serialize() + "\n", true);
                                ServerLog.add("Withdrawn '" + participantWithdraw +"' " + serverWidthdrawMessage.serialize());

                                /**Adding all participants that has yet to accepted the invite*/
                                Set<String> allParticipants = withdrawMeeting.getAcceptedMap().keySet();
                                for (String s : allParticipants){
                                    if (withdrawMeeting.getAcceptedMap().get(s) == false){
                                        NotAcceptedParticipants.add(s);
                                    }
                                }
                                for (String s : clientAddressMap.keySet()){
                                    if (NotAcceptedParticipants.equals(s)){
                                        NotAcceptedSocketAddress.add(clientAddressMap.get(s));
                                    }
                                }

                                /**Sending invite message to those not who have not accepted
                                 * CHECK IF IT SENDS TO ALL OTHER CLIENTS*/
                                RequestMessage requestMessageFromMeeting = withdrawMeeting.getRequestMessage();
                                InviteMessage inviteMessage = new InviteMessage(Integer.valueOf(withdrawMeetingNumber),
                                        requestMessageFromMeeting.getCalendar(),
                                        requestMessageFromMeeting.getTopic(),
                                        withdrawMeeting.getOrganizer());
                                for (int i = 0; i < NotAcceptedParticipants.size(); i++){
                                    UdpSend.sendMessage(inviteMessage.serialize(), serverSocket, NotAcceptedSocketAddress.get(i));
                                }

                                try {
                                    TimeUnit.MINUTES.sleep(1);
                                    /**Save all the participants who have accepted after timeout*/
                                    for (String s : allParticipants){
                                        if (withdrawMeeting.getAcceptedMap().get(s) == false){
                                            acceptedParticipants.add(s);
                                        }
                                    }
                                    for (String s : clientAddressMap.keySet()){
                                        if (acceptedParticipants.equals(s)){
                                            acceptedSocketAddress.add(clientAddressMap.get(s));
                                        }
                                    }

                                    int minimumParticipants = withdrawMeeting.getRequestMessage().getMinimum();

                                    if (acceptedSocketAddress.size() < minimumParticipants){
                                        List<String> participants = new ArrayList<>();
                                        List<SocketAddress> participantsSocketAddress = new ArrayList<>();

                                        for (String s : allParticipants){
                                            participants.add(s);
                                        }

                                        for (String s : clientAddressMap.keySet()){
                                            if (participants.equals(s)){
                                                participantsSocketAddress.add(clientAddressMap.get(s));
                                            }
                                        }

                                        /**NOT SURE IF IT WORKS FOR SENDING THE MESSAGE TO ALL*/
                                        for (int i = 0; i < participantsSocketAddress.size(); i++){
                                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(Integer.valueOf(withdrawMeetingNumber), "Not enough participants for meeting #" + withdrawMeetingNumber);
                                            UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, participantsSocketAddress.get(i));

                                            FileReaderWriter.WriteFile("log", currentTime + "Cancel to '" + participantsSocketAddress.get(i) + "' " + serverCancelMessage.serialize() + "\n", true);
                                            ServerLog.add("Cancel to '" + participantsSocketAddress.get(i) + "' " + serverCancelMessage.serialize());
                                        }

                                        String date = CalendarUtil.calendarToString(withdrawMeeting.getRequestMessage().getCalendar());
                                        int roomNumber = withdrawMeeting.getRoomNumber();
                                        Boolean[] rooms = scheduleMap.get(date);

                                        if(roomNumber == 1){
                                            synchronized (scheduleMap){
                                                rooms[roomNumber - 1] = false;
                                                scheduleMap.replace(date, rooms);
                                            }

                                            synchronized (meetingMap){
                                                meetingMap.remove(withdrawMeetingNumber);
                                            }
                                        }
                                        else if (roomNumber == 2){

                                            synchronized (scheduleMap){
                                                rooms[roomNumber - 1] = false;
                                                scheduleMap.replace(date, rooms);
                                            }

                                            synchronized (meetingMap){
                                                meetingMap.remove(withdrawMeetingNumber);
                                            }

                                        }

                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                messageToClient = "You are the host. You cannot withdraw from the meeting.";
                                System.out.println(messageToClient);
                                DeniedMessage deniedMessage = new DeniedMessage(Integer.valueOf(withdrawMessage.getMeetingNumber()), messageToClient);
                                UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                            }

                        }
                        else{
                            messageToClient = "You were never invited";
                            System.out.println(messageToClient);
                            DeniedMessage deniedMessage = new DeniedMessage(Integer.valueOf(withdrawMessage.getMeetingNumber()), messageToClient);
                            UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                        }
                    }
                    else{
                        messageToClient = "Meeting does not exist.";
                        System.out.println(messageToClient);
                        DeniedMessage deniedMessage = new DeniedMessage(Integer.valueOf(withdrawMessage.getMeetingNumber()), messageToClient);
                        UdpSend.sendMessage(deniedMessage.serialize(), serverSocket, socketAddress);
                    }
                    break;
                case Add:

                    /**receivedMessage[] -> 0 is Add, 1 Meeting Number
                     * Take meeting number, go to meetingMap search for that key.
                     * if exist, get the Meeting object, use method getAcceptedMap with "port".
                     *      if port does not exist, exit and return message "not invited"
                     *      else if check if the port number exists in getAcceptedMap.
                     *      else fetch the status of the requestor.
                     *          if true, return "already accepted"
                     *          else, change to "true". Return "Updated".
                     * */

                    String theAddMessage = message.trim();

                    //System.out.println(theAddMessage + "please");
                    AddMessage addMessage = new AddMessage();
                    addMessage.deserialize(theAddMessage);

                    String meetingNumber = Integer.toString(addMessage.getMeetingNumber());
                    //String meetingNumber = receivedMessage[1];
                    //System.out.println("Contains meeting? " + meetingMap.containsKey(meetingNumber));

                    for(String typeKey : meetingMap.keySet()){
                        String key = typeKey.toString();
                        String value = meetingMap.get(typeKey).getRequestMessage().getParticipants().toString();
                        System.out.println("Hashmap for meetings");
                        System.out.println(key + ": " + value);
                    }

                    String participantAdd = "";
                    //InetSocketAddress bruh = null;

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        //System.out.println("Loop: " + 1);
                        //System.out.println("Key: " + entry.getKey());
                        //System.out.println("Value: " + entry.getValue());
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                participantAdd = entry.getKey();
                                //bruh = entry.getValue();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }

                    //System.out.println("Name of the client: " + participantAdd);
                    //System.out.println("Value at Client: " + bruh);
                    //System.out.println(" Receiving Port: " + port);

                    /*for (int i = 0; i < meetingMap.get(meetingNumber).getAcceptedMap().size(); i++){
                        System.out.println(meetingMap.get(meetingNumber).getAcceptedMap().keySet());
                    }*/
                    /**If your meeting number does not exist*/
                    if (!meetingMap.containsKey(meetingNumber)){
                        messageToClient = "The meeting number you provided does not exist";
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), messageToClient);
                        System.out.println(messageToClient);
                        UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);

                        FileReaderWriter.WriteFile("log", currentTime + messageToClient + " '" + participantAdd + "'" + "\n", true);
                        ServerLog.add(currentTime + messageToClient + " '" + participantAdd + "'");

                    }
                    /**If you're not invited in the meeting*/
                    else if(!meetingMap.get(meetingNumber).getAcceptedMap().containsKey(participantAdd)){
                        System.out.println("AcceptedMap: ");
                        //System.out.println(meetingMap.get(meetingNumber).getAcceptedMap());
                        //System.out.println("Organizer Name: " + meetingMap.get(meetingNumber).getOrganizer());
                        messageToClient = "You are not invited in the meeting.";
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), messageToClient);
                        System.out.println(messageToClient);
                        UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + messageToClient + " '" + participantAdd + "'" + "\n", true);
                        ServerLog.add(currentTime + messageToClient + " '" + participantAdd + "'");
                    }
                    else{
                        Meeting theMeeting = meetingMap.get(meetingNumber);
                        //System.out.println("Bob: " + theMeeting.getAcceptedMap().get("Bob"));
                        //System.out.println("Cass: " + theMeeting.getAcceptedMap().get(participantAdd));

                        if (theMeeting.getAcceptedMap().get(participantAdd)){
                            messageToClient = "You have already accepted the meeting";
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(addMessage.getMeetingNumber(), messageToClient);
                            System.out.println(messageToClient);
                            UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                            FileReaderWriter.WriteFile("log", currentTime + messageToClient + " '" + participantAdd + "'" + "\n", true);
                            ServerLog.add(currentTime + messageToClient + " '" + participantAdd + "'");

                        }else{


                            synchronized (theMeeting){
                                theMeeting.getAcceptedMap().put(participantAdd, true);
                            }

                            SocketAddress hostSocketAddressAdd = null;
                            String hostAdd = theMeeting.getOrganizer();
                            for (String s : clientAddressMap.keySet()){
                                if (hostAdd.equals(s)){
                                    hostSocketAddressAdd = clientAddressMap.get(s);
                                }
                            }

                            /*if (socketAddress == hostSocketAddress){
                                AddedMessage addedMessage = new AddedMessage(addMessage.getMeetingNumber(), socketAddress.toString());
                                ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), theMeeting.getRoomNumber());
                                UdpSend.sendMessage(confirmMessage.serialize(), socketAddress);
                            }
                            else{
                                AddedMessage addedMessage = new AddedMessage(addMessage.getMeetingNumber(), socketAddress.toString());
                                UdpSend.sendMessage(addedMessage.serialize(), hostSocketAddress);
                                messageToClient = "You are added to the meeting " + meetingNumber;
                                ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), theMeeting.getRoomNumber());
                                UdpSend.sendMessage(confirmMessage.serialize(), socketAddress);
                            }*/

                            AddedMessage addedMessage = new AddedMessage((addMessage.getMeetingNumber()), participantAdd);
                            UdpSend.sendMessage(addedMessage.serialize(), serverSocket, hostSocketAddressAdd);
                            FileReaderWriter.WriteFile("log", currentTime + "Added '" + participantAdd + "'" + "\n", true);
                            ServerLog.add(currentTime + "Added '" + participantAdd + "'");

                            messageToClient = "You are added to the meeting " + meetingNumber;
                            ConfirmMessage confirmMessage = new ConfirmMessage(addedMessage.getMeetingNumber(), (theMeeting.getRoomNumber()));
                            UdpSend.sendMessage(confirmMessage.serialize(), serverSocket, socketAddress);
                            System.out.println(messageToClient);
                            FileReaderWriter.WriteFile("log", currentTime + "Added '" + participantAdd + "' message sent to '" + hostAdd + "'" + "\n", true);
                            ServerLog.add(currentTime + "Added '" + participantAdd + "' message sent to '" + hostAdd + "'");
                        }
                    }

                    //Do something
                    break;
                case RequesterCancel:

                    /**receivedMessage[] -> 0 is Cancel (or RequesterCancel in this case), 1 Meeting Number
                     * if(meeting number exist)
                     *      Get the meeting connected the meeting number.
                     *      if(port of received message == meeting.getOrganizer())
                     *          String date = meeting.getRequestMessage[2]
                     *          String roomNumber = meeting.getRoomNumber
                     *          scheduleMap(date, roomNumber) -> Change room number boolean into False.
                     *          meetingMap -> delete meeting with specified meeting number (key included)
                     *          return Cancel message to all clients.
                     *
                     *      else
                     *          Not requestor, cannot cancel meeting
                     * else
                     *      Meeting does not exist*/

                    RequesterCancelMessage requesterCancelMessage = new RequesterCancelMessage();
                    String requesterCancelMesssage = message.trim();
                    requesterCancelMessage.deserialize(requesterCancelMesssage);
                    String mNumber = Integer.toString(requesterCancelMessage.getMeetingNumber());
                    //System.out.println("Meeting number: " + mNumber + "please");
                    //String mNumber = receivedMessage[1];
                    System.out.println("Meeting number " + mNumber);
                    System.out.println("Meeting Map: " + meetingMap);

                    String requesterCancelName = "";

                    //Find the name using the port number, assign to participantName
                    for (Map.Entry<String, InetSocketAddress> entry : clientAddressMap.entrySet()) {
                        try {
                            InetSocketAddress temp = new InetSocketAddress(InetAddress.getLocalHost(), port);
                            if (entry.getValue().equals(temp)) {
                                requesterCancelName = entry.getKey();
                            }
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }

                    }

                    if(meetingMap.containsKey(mNumber)){
                        Meeting theMeeting = meetingMap.get(mNumber);
                        System.out.println(theMeeting.getRoomNumber());
                        if(requesterCancelName == theMeeting.getOrganizer()){
                            System.out.println("Organizer" + theMeeting.getOrganizer() + " Receiving Port: " + port);
                            String date = CalendarUtil.calendarToString(theMeeting.getRequestMessage().getCalendar());
                            System.out.println(date);
                            int roomNumber = theMeeting.getRoomNumber();
                            Boolean[] rooms = scheduleMap.get(date);
                            System.out.println("Boolean Rooms: " + rooms);

                            /**NEED TO SEND MESSAGE TO ALL INVITED PARTICIPANTS. SENDING THE SAME
                             * MESSAGE TO DIFFERENT PORT NUMBER.
                             *
                             * Create array, store all port number getAcceptedMap.
                             * Loop through the array
                             *      Create ServerCancelMessage object to send message
                             *      Call the serialize function to send.
                             * */

                            List<String> participants = new ArrayList<>();
                            List<SocketAddress> nonHostSocketAddress = new ArrayList<>();

                            if(roomNumber == 1){
                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/
                                Set<String> theName = theMeeting.getAcceptedMap().keySet();
                                for (String aName : theName){
                                    participants.add(aName);
                                }
                                for (int i = 0; i < participants.size(); i++){
                                    System.out.println(participants.get(i));
                                }

                                /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.
                                try {
                                    socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), participants.get(0));
                                } catch (UnknownHostException e) {
                                    e.printStackTrace();
                                }
                                ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);*/

                                for (String s : participants){
                                    if (!s.equals(theMeeting.getOrganizer())){
                                        nonHostSocketAddress.add(clientAddressMap.get(s));
                                    }
                                }

                                System.out.println("Socket Address" + nonHostSocketAddress.get(0));

                               /**Loop through the save ports and send message to them*/
                                for (int i = 0; i < nonHostSocketAddress.size(); i++){
                                    System.out.println("Socket Address" + nonHostSocketAddress.get(i));
                                     /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/


                                    /**THINKING OF CREATING THREAD TO SEND MESSAGE TO ALL INVITED PEOPLE*/
                                    /*SocketAddress finalSocketAddress = nonHostSocketAddress;
                                    Thread thread = new Thread(){
                                        public void run(){
                                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                            UdpSend.sendMessage(serverCancelMessage.serialize(), finalSocketAddress);
                                        }
                                    };
                                    thread.start();*/

                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, nonHostSocketAddress.get(i));
                                    FileReaderWriter.WriteFile("log", currentTime + "Meeting Cancel sent to '" + nonHostSocketAddress.get(i) + "'" + "\n", true);
                                    ServerLog.add(currentTime + "Meeting Cancel sent to '" + nonHostSocketAddress.get(i) + "'");
                                }
                                synchronized (scheduleMap){
                                    rooms[roomNumber - 1] = false;
                                    scheduleMap.replace(date, rooms);
                                }

                                synchronized (meetingMap){
                                    meetingMap.remove(mNumber);
                                }

                            }
                            else if(roomNumber == 2){
                                /**CHECK THE INDEX WELL. MIGHT HAVE TO SUBTRACT 1*/
                                Set<String> theName = theMeeting.getAcceptedMap().keySet();
                                for (String aName : theName){
                                    participants.add(aName);
                                }
                                for (int i = 0; i < participants.size(); i++){
                                    System.out.println(participants.get(i));
                                }

                                /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.
                                 try {
                                 socketAddress = new InetSocketAddress(InetAddress.getLocalHost(), participants.get(0));
                                 } catch (UnknownHostException e) {
                                 e.printStackTrace();
                                 }
                                 ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                 UdpSend.sendMessage(serverCancelMessage.serialize(), socketAddress);*/

                                for (String s : participants){
                                    if (!s.equals(theMeeting.getOrganizer())){
                                        nonHostSocketAddress.add(clientAddressMap.get(s));
                                    }
                                }

                                /**Loop through the save ports and send message to them*/
                                for (int i = 0; i < nonHostSocketAddress.size(); i++){

                                    /**USE UDPSEND TOOL TO SEND THE MESSAGE TO SERVERS.*/


                                    /**THINKING OF CREATING THREAD TO SEND MESSAGE TO ALL INVITED PEOPLE*/
                                    /*SocketAddress finalSocketAddress = nonHostSocketAddress;
                                    Thread thread = new Thread(){
                                        public void run(){
                                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                            UdpSend.sendMessage(serverCancelMessage.serialize(), finalSocketAddress);
                                        }
                                    };
                                    thread.start();*/

                                    ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), "The Host has cancelled the meeting");
                                    UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, nonHostSocketAddress.get(i));
                                    FileReaderWriter.WriteFile("log", currentTime + "Meeting Cancel sent to '" + nonHostSocketAddress.get(i) + "'" + "\n", true);
                                    ServerLog.add(currentTime + "Meeting Cancel sent to '" + nonHostSocketAddress.get(i) + "'");
                                }
                                synchronized (scheduleMap){
                                    rooms[roomNumber - 1] = false;
                                    scheduleMap.replace(date, rooms);
                                }

                                synchronized (meetingMap){
                                    meetingMap.remove(mNumber);
                                }

                            }
                            System.out.println("meetingMap: " + meetingMap);
                            System.out.println("scheduleMap" + scheduleMap);

                        }
                        /**MIGHT HAVE TO CHANGE THE SERVERCANCELMESSAGE TO DENIEDMESSAGE TYPE.
                         * WILL HAVE TO TEST IT THOROUGHLY.*/
                        else{
                            messageToClient = "Not requestor, cannot cancel meeting";
                            System.out.println(messageToClient);
                            ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), messageToClient);
                            UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                            FileReaderWriter.WriteFile("log", currentTime + messageToClient + " '" + requesterCancelName + "'" + "\n", true);
                            ServerLog.add(currentTime + messageToClient + " '" + requesterCancelName + "'");
                        }
                    }
                    else{
                        messageToClient = "Meeting does not exist";
                        System.out.println(messageToClient);
                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage(requesterCancelMessage.getMeetingNumber(), messageToClient);
                        UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);
                        FileReaderWriter.WriteFile("log", currentTime + messageToClient + " '" + requesterCancelName + "'" + "\n", true);
                        ServerLog.add(currentTime + messageToClient + " '" + requesterCancelName + "'");


                    }

                    break;
                default:
                    System.out.println("Request type does not correspond. Exiting.");
                    break;

            }

        }

        public String getMessageToClient(){
            return messageToClient;
        }
    }

    public class ServerCommand implements Runnable {

        String message = "";
        //String messageToServer = "Set Server Value";

        public ServerCommand() {

        }


        @Override
        public void run() {

            while(true) {
                Scanner scanner = new Scanner(System.in);
                message = scanner.nextLine();

                //RoomChangeMessage roomChangeMessage = new RoomChangeMessage();


                String[] commandMessage = message.split("\\$");


                if (commandMessage[0].equals("RoomChange")) {
                    System.out.println("Server Command");

                    RoomChangeMessage roomChangeMessage = new RoomChangeMessage();
                    roomChangeMessage.deserialize(message);
                    String meetingNumberRC = Integer.toString(roomChangeMessage.getMeetingNumber());
                    Meeting roomChangeMeeting = null;
                    int newRoomNumber = roomChangeMessage.getNewRoomNumber() - 1;

                    if (newRoomNumber != 0 && newRoomNumber != 1) {
                        System.out.println("Choose a room number of 1 or 2");
                        //break;
                    }


                    //If meeting number exists
                    if (meetingMap.containsKey(meetingNumberRC)) {
                        System.out.println("Has meeting number");
                        System.out.println(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()));
                        //If the new room number found at the same time as the meeting number is false (FREE)
                        if (!scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber]) {
                            //Make the meeting's room number to the new one
                            meetingMap.get(meetingNumberRC).setRoomNumber(newRoomNumber);
                            scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber] = true;

                            roomChangeMeeting = meetingMap.get(meetingNumberRC);

                            for (String s : roomChangeMeeting.getRequestMessage().getParticipants()) {
                                SocketAddress socketAddress = clientAddressMap.get(s);

                                //If you add participant in list that does not have a running client
                                if (socketAddress == null) {
                                    continue;
                                }
                                UdpSend.sendMessage(roomChangeMessage.serialize(), serverSocket, socketAddress);
                                Calendar calendar = Calendar.getInstance();
                                String currentTime = "Server[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                        + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                                FileReaderWriter.WriteFile("log", currentTime + "Room changed '" + s + "' " + roomChangeMessage.serialize() + "\n", true);
                                ServerLog.add(currentTime + "Room changed '" + s + "' " + roomChangeMessage.serialize());

                            }

                            System.out.println("We are changing rooms!");
                        } else if (scheduleMap.get(CalendarUtil.calendarToString(meetingMap.get(meetingNumberRC).getRequestMessage().getCalendar()))[newRoomNumber]) {
                            System.out.println("They are already in that room");

                        }

                    } else {
                        System.out.println("Meeting room is busy or number does not exist");

                        roomChangeMeeting = meetingMap.get(meetingNumberRC);


                        ServerCancelMessage serverCancelMessage = new ServerCancelMessage();
                        serverCancelMessage.setMeetingNumber(roomChangeMeeting.getId());
                        serverCancelMessage.setReason("Room change");
                        String person = "";

                        //Find the name if they accepted (true)
                        for (Map.Entry<String, Boolean> entry : roomChangeMeeting.getAcceptedMap().entrySet()) {
                            System.out.println(entry);
                            Boolean value = entry.getValue();
                            if (value == true && entry.getValue().equals(value)) {
                                person = entry.getKey();
                                System.out.println(person);

                                SocketAddress socketAddress = clientAddressMap.get(person);

                                //If you add participant in list that does not have a running client
                                if (socketAddress == null) {
                                    continue;
                                }
                                UdpSend.sendMessage(serverCancelMessage.serialize(), serverSocket, socketAddress);

                                Calendar calendar = Calendar.getInstance();
                                String currentTime = "Server[" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " "
                                        + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "]: ";
                                FileReaderWriter.WriteFile("log", currentTime + "Canceled '" + person + "' " + serverCancelMessage.serialize() + "\n", true);
                                ServerLog.add("Canceled '" + person + "' " + serverCancelMessage.serialize());
                            }
                        }
                    }
                }
                else {
                    //messageToServer = "Not a room change command";
                }

            }
        }

    }

    public String getCommandMessage() {
        return null;
    }

    private String getServerState(){
        String result = "";

        for(Map.Entry<String, Boolean[]> entry :  scheduleMap.entrySet()){
            result += entry.getKey() + "!" + entry.getValue()[0] + "!" + entry.getValue()[1] + ";";
        }

        result += "_";

        for(Map.Entry<String, Meeting> entry :  meetingMap.entrySet()){
            result += entry.getValue().serialize() + ";";
        }

        return result;
    }

    private void loadServer(){

        ArrayList<String> fileString = FileReaderWriter.ReadFile("server");

        String message = "";

        for(int i = 0; i < fileString.size(); i++){
            message += fileString.get(i);
        }

        String[] subMessage = message.split("_");

        if(subMessage.length > 0 && !subMessage[0].isEmpty()){
            String[] scheduleMapMessages = subMessage[0].split(";");

            for(int i = 0; i < scheduleMapMessages.length ; i++){
                String[] sMMSplit = scheduleMapMessages[i].split("!");

                Boolean[] availability = {Boolean.parseBoolean(sMMSplit[1]), Boolean.parseBoolean(sMMSplit[2])};

                scheduleMap.put(sMMSplit[0], availability);
            }

        }

        if(subMessage.length > 0 && !subMessage[1].isEmpty()){
            String[] meetingMapString = subMessage[1].split(";");

            for(int i = 0 ; i < meetingMapString.length ; i++) {

                Meeting meeting = new Meeting(meetingMapString[i]);

                meetingMap.put(Integer.toString(meeting.getId()), meeting);

            }
        }
    }

    public class ServerSave implements Runnable {
        @Override
        public void run(){

            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }

                FileReaderWriter.WriteFile("server", getServerState(), false);

            }

        }
    }

}
