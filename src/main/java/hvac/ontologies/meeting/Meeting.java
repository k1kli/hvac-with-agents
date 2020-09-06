package hvac.ontologies.meeting;

import jade.content.Concept;

import java.util.Date;

@SuppressWarnings("unused")
public class Meeting implements Concept, Comparable<Meeting> {
    private String meetingID;
    private Date startDate;
    private Date endDate;
    private int peopleInRoom;
    private float temperature;

    public Meeting() {}

    public Meeting(String meetingID, Date startDate, Date endDate, int peopleInRoom, float temperature) {
        this.meetingID = meetingID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleInRoom = peopleInRoom;
        this.temperature = temperature;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setPeopleInRoom(int peopleInRoom) {
        this.peopleInRoom = peopleInRoom;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getPeopleInRoom() {
        return peopleInRoom;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float Temperature) {
        this.temperature = Temperature;
    }

    //TODO change to static?
    public long millisecondsOverlapping(Meeting meeting){
        long timeDifference = (endDate.before(meeting.getEndDate()) ? endDate : meeting.getEndDate()).getTime()
                - (startDate.after(meeting.getStartDate()) ? startDate : meeting.getStartDate()).getTime();
        return timeDifference > 0 ? timeDifference : 0;
    }
    //TODO change to static?
    public boolean isOverlapping(Meeting meeting){
        return millisecondsOverlapping(meeting) > 0;
    }

    @Override
    public int compareTo(Meeting emp) {
        return this.getStartDate().compareTo(emp.getStartDate());
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "meetingID='" + meetingID + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", peopleInRoom=" + peopleInRoom +
                ", temperature=" + temperature +
                '}';
    }
}
