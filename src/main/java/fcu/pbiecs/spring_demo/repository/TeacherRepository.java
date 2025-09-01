package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher,Integer> {

}
