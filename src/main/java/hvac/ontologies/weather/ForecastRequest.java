package hvac.ontologies.weather;

import jade.content.AgentAction;

import java.util.Date;

public class ForecastRequest implements AgentAction {
    private Date from;
    private Date to;

    public ForecastRequest() {}

    public ForecastRequest(Date from, Date to) {
        this.from = from;
        this.to = to;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }
}
