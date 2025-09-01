package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course,Integer> {

}
