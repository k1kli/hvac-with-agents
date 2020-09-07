package hvac.util;

import hvac.time.DateTimeSimulator;

import java.time.format.DateTimeFormatter;

public class Logger {
    private String agentName = "unnamed agent";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void log(String msg) {
        System.out.println("[" + DateTimeSimulator.getCurrentDate().format(formatter) + "]"
        + agentName + ": " + msg);
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}
