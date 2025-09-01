package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Integer> {

    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %?1%")
    List<Student> findByFirstNameContaining(String keyword);

    @Query("SELECT s FROM Student s WHERE s.email = ?1")
    Student findStudentByEmail(String email);
}
