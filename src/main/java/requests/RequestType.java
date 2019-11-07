package requests;

public enum RequestType {

    //Server Requests
    Denied, Invite, Confirm, ServerCancel,
    Scheduled, NotScheduled, Added, RoomChange,

    //Client Requests
    Request,  Accept, Reject, Withdraw,  Add, RequesterCancel

}
