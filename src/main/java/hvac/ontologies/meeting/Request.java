package hvac.ontologies.meeting;

import jade.content.AgentAction;

@SuppressWarnings("unused")
public class Request implements AgentAction {
    private Meeting meeting;
    private RequestStatus requestStatus;

    public Request(){}
  
    public Request(Meeting meeting, RequestStatus requestStatus) {
        this.meeting = meeting;
        this.requestStatus = requestStatus;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    @SuppressWarnings("unused")
    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    @SuppressWarnings("unused")
    public int getRequestStatus() {
        return requestStatus.getValue();
    }

    @SuppressWarnings("unused")
    public void setRequestStatus(int requestStatus) {
        this.requestStatus = RequestStatus.valueOf(requestStatus);
    }

    public RequestStatus getStatus() {
        return requestStatus;
    }

    public void setStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
}
