package hvac.database.entities;

import javax.persistence.*;

@Entity
@Table( name = "EMPLOYEE",
        indexes = {
                @Index(columnList = "ID", name = "employee_id")
        })
public class Employee {
    private int id;
    private String alias;

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
}
