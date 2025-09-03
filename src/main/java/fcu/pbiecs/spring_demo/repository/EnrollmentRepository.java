package fcu.pbiecs.spring_demo.repository;

import fcu.pbiecs.spring_demo.model.Enrollment;
import fcu.pbiecs.spring_demo.model.EnrollmentId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    // 根據學生ID查詢選課記錄 (無分頁)
    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Integer studentId);

    // 根據學生ID查詢選課記錄 (分頁)
    @Query("SELECT e FROM Enrollment e WHERE e.student.studentId = :studentId")
    Page<Enrollment> findByStudentId(@Param("studentId") Integer studentId, Pageable pageable);

    // 根據課程ID查詢選課記錄 (無分頁)
    @Query("SELECT e FROM Enrollment e WHERE e.course.courseId = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") Integer courseId);

    // 根據課程ID查詢選課記錄 (分頁)
    @Query("SELECT e FROM Enrollment e WHERE e.course.courseId = :courseId")
    Page<Enrollment> findByCourseId(@Param("courseId") Integer courseId, Pageable pageable);

    // 查詢最熱門課程 (按選課人數排序)
    @Query("SELECT e.course.courseId as courseId, COUNT(e) as enrollmentCount " +
           "FROM Enrollment e " +
           "GROUP BY e.course.courseId " +
           "ORDER BY COUNT(e) DESC")
    List<Object[]> findTopCoursesByEnrollmentCount(Pageable pageable);
}
