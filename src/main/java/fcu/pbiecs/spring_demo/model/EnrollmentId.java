package fcu.pbiecs.spring_demo.model;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@Embeddable
public class EnrollmentId implements Serializable {
    private Integer studentId;
    private Integer courseId;
}
