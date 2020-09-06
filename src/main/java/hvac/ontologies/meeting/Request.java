package hvac.ontologies.meeting;

import jade.content.AgentAction;

@SuppressWarnings("unused")
public class Request implements AgentAction {
    private Meeting meeting;
    private RequestStatus status;

    public Request(){}
    public Request(Meeting meeting, RequestStatus status) {
        this.meeting = meeting;
        this.status = status;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
