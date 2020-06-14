package hvac.database.entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table( name = "EMPLOYEE",
        indexes = {
                @Index(columnList = "ID", name = "employee_id")
        })
public class Employee {
    private int id;
    private String alias;

    Set<Meeting> meetings = new HashSet<>();

    public Employee(){}//for hibernate

    public Employee(String alias) {
        this.alias = alias;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false, nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "ALIAS")
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @ManyToMany(mappedBy = "employees")
    public Set<Meeting> getMeetings() {
        return meetings;
    }

    public void setMeetings(Set<Meeting> meetings) {
        this.meetings = meetings;
    }
}
