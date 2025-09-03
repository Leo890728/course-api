package fcu.pbiecs.spring_demo.dto;

import fcu.pbiecs.spring_demo.model.Teacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PopularCourseDTO {
    private Integer courseId;
    private String name;
    private String description;
    private Integer credits;
    private Teacher teacher;
    private Long enrollmentCount;
}