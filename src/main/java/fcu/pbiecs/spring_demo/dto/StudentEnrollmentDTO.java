package fcu.pbiecs.spring_demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentEnrollmentDTO {
    private Integer studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String birthday;
    private Date enrollmentDate;
}