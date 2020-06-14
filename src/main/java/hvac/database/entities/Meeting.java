package hvac.database.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table( name = "MEETING",
        indexes = {
                @Index(columnList = "ID", name = "meeting_id")
        })
public class Meeting {
    private String id;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String roomId;


    Set<Employee> employees = new HashSet<>();

    public Meeting(){}//for hibernate

    public Meeting(String id, LocalDateTime startDate, LocalDateTime endDate, String roomId, Set<Employee> employees) {
        //for application use
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomId = roomId;
        this.employees = new HashSet<>(employees);
    }

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    @Column(name = "START_DATE", nullable = false)
    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    @Column(name = "END_DATE", nullable = false)
    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    @Column(name = "ROOM_ID", nullable = false)
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @ManyToMany(cascade = { CascadeType.ALL })
    @JoinTable(
            name = "MEETING_EMPLOYEE",
            joinColumns = { @JoinColumn(name = "MEETING_ID") },
            inverseJoinColumns = { @JoinColumn(name = "EMPLOYEE_ID") }
    )
    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
