package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course,Integer> {
    
    // 根據關鍵字搜尋課程 (不分頁)
    @Query("SELECT c FROM Course c LEFT JOIN c.teacher t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(c.courseId AS string) LIKE CONCAT('%', :keyword, '%'))")
    List<Course> findByKeyword(@Param("keyword") String keyword);

    // 根據關鍵字搜尋課程 (分頁)
    @Query("SELECT c FROM Course c LEFT JOIN c.teacher t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(c.courseId AS string) LIKE CONCAT('%', :keyword, '%'))")
    Page<Course> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
