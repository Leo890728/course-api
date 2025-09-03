package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.dto.StudentEnrollmentDTO;
import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.model.Teacher;
import fcu.pbiecs.spring_demo.service.CourseService;
import fcu.pbiecs.spring_demo.service.EnrollmentService;
import fcu.pbiecs.spring_demo.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "課程管理", description = "提供課程 CRUD API")
@RestController
@RequestMapping("api/courses")
public class CourseController {

    @Autowired
    CourseService courseService;

    @Autowired
    TeacherService teacherService;

    @Autowired
    EnrollmentService enrollmentService;

    @Operation(summary = "查詢所有課程", description = "取得所有課程的資訊")
    @GetMapping
    public ResponseEntity<List<Course>> getCourses(
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String searchKeyword) {

        // 處理搜尋功能
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            if (pageNumber == null || pageSize == null) {
                List<Course> courses = courseService.searchCourses(searchKeyword.trim());
                return ResponseEntity.ok(courses);
            }
            if (pageNumber < 0 || pageSize < 0) {
                throw new IllegalArgumentException("Invalid page number or page size");
            }
            // 搜尋 + 分頁查詢
            Page<Course> page = courseService.searchCourses(searchKeyword.trim(), pageNumber, pageSize);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }

        // 一般查詢
        if (pageNumber == null || pageSize == null) {
            List<Course> courses = courseService.getAllCourse();
            return ResponseEntity.ok(courses);
        }
        if (pageNumber < 0 || pageSize < 0) {
            throw new IllegalArgumentException("Invalid page number or page size");
        }
        // 分頁查詢
        Page<Course> page = courseService.getAllCourse(pageNumber, pageSize);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @Operation(summary = "查詢課程", description = "依照ID查詢課程資訊")
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable("id") int id) throws CourseService.CourseNotfoundException {
        return courseService.getCourseById(id);
    }

    @Operation(summary = "查詢選課學生", description = "查詢選課學生")
    @GetMapping("/{id}/Students")
    public ResponseEntity<List<StudentEnrollmentDTO>> getCourseStudents(
            @PathVariable("id") int id,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) throws CourseService.CourseNotfoundException {
        // Verify course exists
        courseService.getCourseById(id);
        
        if (pageNumber != null && pageSize != null) {
            if (pageNumber < 0 || pageSize < 0) {
                throw new IllegalArgumentException("Invalid page number or page size");
            }
            // 分頁查詢
            Page<StudentEnrollmentDTO> page = enrollmentService.getCourseEnrollments(id, pageNumber, pageSize);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } else {
            // 無分頁查詢
            List<StudentEnrollmentDTO> students = enrollmentService.getCourseEnrollments(id);
            return ResponseEntity.ok(students);
        }
    }

    @Operation(summary = "新增課程", description = "新增課程")
    @PostMapping
    public Course addCourse(@RequestBody Course course) throws CourseService.CourseAlreadyExistsException, TeacherService.TeacherNotfoundException {
        Teacher teacher = teacherService.getTeacherById(course.getTeacher().getTeacherId());
        course.setTeacher(teacher);
        return courseService.addCourse(course);
    }

    @Operation(summary = "刪除課程", description = "依照ID刪除課程")
    @DeleteMapping("/{id}")
    public void deleteCourseById(@PathVariable("id") int id) throws CourseService.CourseNotfoundException {
        courseService.deleteCourse(id);
    }

    @Operation(summary = "更新課程", description = "依照ID更新課程資訊")
    @PutMapping("/{id}")
    public void updateCourse(@PathVariable("id") int id, @RequestBody Course updatedCourse) throws CourseService.CourseNotfoundException, TeacherService.TeacherNotfoundException {
        Teacher teacher = teacherService.getTeacherById(updatedCourse.getTeacher().getTeacherId());
        updatedCourse.setCourseId(id);
        updatedCourse.setTeacher(teacher);
        courseService.updateCourse(updatedCourse);
    }

}
