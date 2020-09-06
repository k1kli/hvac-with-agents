package hvac.coordinator;

import hvac.database.entities.Employee;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Meeting {
    private String id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int roomId;

    Set<Employee> employees;

    public Meeting(String id, LocalDateTime startDate, LocalDateTime endDate, int roomId, Set<Employee> employees) {
        //for application use
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomId = roomId;
        this.employees = new HashSet<>(employees);
    }

    public String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    private void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    @SuppressWarnings("unused")
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    @SuppressWarnings("unused")
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    @SuppressWarnings("unused")
    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
