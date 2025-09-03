package fcu.pbiecs.spring_demo.dto;

import fcu.pbiecs.spring_demo.model.Teacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnrollmentDTO {
    private Integer courseId;
    private String name;
    private String description;
    private Integer credits;
    private Teacher teacher;
    private Date enrollmentDate;
}