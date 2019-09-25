package messagetype;

public enum RequestType {

    //Server Requests
    Denied, Invite, Confirm, Cancel, Scheduled,
    NotScheduled, Added, RoomChange,

    //Client Requests
    Request,  Accept, Reject, Widthdraw,  Add

}
