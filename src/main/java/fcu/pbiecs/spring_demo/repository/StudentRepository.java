package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Integer> {

    @Query("SELECT s FROM Student s WHERE s.firstName LIKE %?1%")
    List<Student> findByFirstNameContaining(String keyword);

    @Query("SELECT s FROM Student s WHERE s.email = ?1")
    Student findStudentByEmail(String email);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.studentId AS string) LIKE CONCAT('%', :keyword, '%')")
    List<Student> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT s FROM Student s WHERE " +
           "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(s.studentId AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<Student> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
