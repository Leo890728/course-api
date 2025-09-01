package fcu.pbiecs.spring_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@ToString(exclude = {"teacher", "students"})
@Entity
@Table(name="Course")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="course_id")
    private Integer courseId;

    @Column(name="course_name")
    private String name;

    @Column(name="course_description")
    private String description;

    @Column(name="credits")
    private int credits;

    @ManyToOne(
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        optional = false
    )
    @JoinColumn(name="teacher_id", nullable = true)
    private Teacher teacher;

    @ManyToMany
    @JoinTable(
        name = "Enrollment",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonIgnore
    private List<Student> students;
}
