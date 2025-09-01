package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Enrollment;
import fcu.pbiecs.spring_demo.model.EnrollmentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

}
