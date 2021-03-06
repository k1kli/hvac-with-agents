package hvac.ontologies.meeting;

import hvac.util.Conversions;
import jade.content.Concept;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

@SuppressWarnings("unused")
public class Meeting implements Concept, Comparable<Meeting> {
    private String meetingID;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int peopleInRoom;
    private float temperature;

    public Meeting(){}

    public Meeting(String meetingID, LocalDateTime startDate, LocalDateTime endDate, int peopleInRoom, float temperature) {

        this.meetingID = meetingID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.peopleInRoom = peopleInRoom;
        this.temperature = temperature;
    }

    public Meeting(hvac.coordinator.Meeting meeting){
        this.meetingID = meeting.getId();
        this.startDate = meeting.getStartDate();
        this.endDate = meeting.getEndDate();
        this.peopleInRoom = meeting.getEmployees().size();
        this.temperature = 0;

    }

    public String getMeetingID() {
        return meetingID;
    }

    @SuppressWarnings("unused")
    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    @SuppressWarnings("unused")
    public Date getStartDate(){
        return Conversions.toDate(startDate);
    }

    @SuppressWarnings("unused")
    public void setStartDate(Date startDate){
        this.startDate = Conversions.toLocalDateTime(startDate);
    }

    public LocalDateTime getLocalStartDate() {
        return startDate;
    }

    @SuppressWarnings("unused")
    public void setLocalStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    @SuppressWarnings("unused")
    public Date getEndDate(){
        return Conversions.toDate(endDate);
    }

    @SuppressWarnings("unused")
    public void setEndDate(Date endDate){
        this.endDate = Conversions.toLocalDateTime(endDate);
    }

    public LocalDateTime getLocalEndDate() {
        return endDate;
    }

    public void setLocalEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getPeopleInRoom() {
        return peopleInRoom;
    }

    @SuppressWarnings("unused")
    public void setPeopleInRoom(int peopleInRoom){
        this.peopleInRoom = peopleInRoom;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float Temperature) {
        this.temperature = Temperature;
    }

    //TODO change to static?
    public long millisecondsOverlapping(Meeting meeting){
        long timeDifference = Duration.between(getLocalStartDate().isAfter(meeting.getLocalStartDate()) ?
                        getLocalStartDate() : meeting.getLocalStartDate(),
                (getLocalEndDate().isBefore(meeting.getLocalEndDate())) ?
                        getLocalEndDate() : meeting.getLocalEndDate()).toMillis();
        return timeDifference > 0 ? timeDifference : 0;
    }
    //TODO change to static?
    public boolean isOverlapping(Meeting meeting){
        return millisecondsOverlapping(meeting) > 0;
    }

    @Override
    public int compareTo(Meeting emp) {
        return this.getLocalStartDate().compareTo(emp.getLocalStartDate());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return peopleInRoom == meeting.peopleInRoom &&
                Float.compare(meeting.temperature, temperature) == 0 &&
                Objects.equals(meetingID, meeting.meetingID) &&
                startDate.equals(meeting.startDate) &&
                endDate.equals(meeting.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(meetingID, startDate, endDate, peopleInRoom, temperature);
    }
}
