package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeacherRepository extends JpaRepository<Teacher,Integer> {

    @Query("SELECT t FROM Teacher t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(t.teacherId AS string) LIKE CONCAT('%', :keyword, '%')")
    List<Teacher> findByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Teacher t WHERE " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(t.teacherId AS string) LIKE CONCAT('%', :keyword, '%')")
    Page<Teacher> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
