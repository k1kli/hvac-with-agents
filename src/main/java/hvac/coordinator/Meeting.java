package hvac.coordinator;

import hvac.database.entities.Employee;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Meeting {
    private String id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String roomId;


    Set<Employee> employees;


    public Meeting(String id, LocalDateTime startDate, LocalDateTime endDate, String roomId, Set<Employee> employees) {
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

    private void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
