package fcu.pbiecs.spring_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@ToString(exclude = "courses")
@Entity
@Table(name="Teacher")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="teacher_id")
    private Integer teacherId;

    @Column(name="teacher_name")
    private String name;

    @Column(name="email")
    private String email;

    @Column(name="age")
    private int age;

    @OneToMany(
        mappedBy = "teacher",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL
    )
    @JsonIgnore
    private java.util.List<Course> courses;
}
