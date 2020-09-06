package hvac.coordinator;

import hvac.database.entities.Employee;
import jade.core.AID;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class Meeting {
    private String id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private AID roomCoordinator;

    Set<Employee> employees;

    public Meeting(String id, LocalDateTime startDate, LocalDateTime endDate, AID roomCoordinator, Set<Employee> employees) {
        //for application use
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomCoordinator = roomCoordinator;
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

    public AID getRoomCoordinator() {
        return roomCoordinator;
    }

    public void setRoomCoordinator(AID roomCoordinator) {
        this.roomCoordinator = roomCoordinator;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    @SuppressWarnings("unused")
    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
