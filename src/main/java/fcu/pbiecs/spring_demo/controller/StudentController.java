package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.dto.CourseEnrollmentDTO;
import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Enrollment;
import fcu.pbiecs.spring_demo.model.EnrollmentId;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.service.CourseService;
import fcu.pbiecs.spring_demo.service.EnrollmentService;
import fcu.pbiecs.spring_demo.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;


@Tag(name = "學生管理", description = "提供學生 CRUD API")
@RestController
@RequestMapping("api/students")
public class StudentController {

    @Autowired
    StudentService studentService;

    @Autowired
    CourseService courseService;

    @Autowired
    EnrollmentService enrollmentService;

    @Operation(summary = "查詢所有學生", description = "取得所有學生的資訊")
    @GetMapping
    public ResponseEntity<List<Student>> getStudents(
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String searchKeyword) {

        // 處理搜尋功能
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            if (pageNumber == null || pageSize == null) {
                List<Student> students = studentService.searchStudents(searchKeyword.trim());
                return ResponseEntity.ok(students);
            }
            if (pageNumber < 0 || pageSize < 0) {
                throw new IllegalArgumentException("Invalid page number or page size");
            }
            // 搜尋 + 分頁查詢
            Page<Student> page = studentService.searchStudents(searchKeyword.trim(), pageNumber, pageSize);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }

        // 一般查詢
        if (pageNumber == null || pageSize == null) {
            List<Student> students = studentService.getAllStudent();
            return ResponseEntity.ok(students);
        }
        if (pageNumber < 0 || pageSize < 0) {
            throw new IllegalArgumentException("Invalid page number or page size");
        }
        // 分頁查詢
        Page<Student> page = studentService.getAllStudent(pageNumber, pageSize);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @Operation(summary = "查詢學生", description = "依照ID查詢學生資訊")
    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable("id") int id) throws StudentService.StudentNotfoundException {
        return studentService.getStudentById(id);
    }

    @Operation(summary = "新增學生", description = "新增一位學生")
    @PostMapping
    public Student addStudent(@RequestBody Student student) throws StudentService.StudentAlreadyExistsException {
        System.out.println(student.getStudentId());
        return studentService.addStudent(student);
    }

    @Operation(summary = "刪除學生", description = "依照ID刪除學生")
    @DeleteMapping("/{id}")
    public void deleteStudentById(@PathVariable("id") int id) throws StudentService.StudentNotfoundException {
        studentService.deleteStudent(id);
    }

    @Operation(summary = "更新學生", description = "依照ID更新學生資訊")
    @PutMapping("/{id}")
    public void updateStudent(@PathVariable("id") int id, @RequestBody Student updatedStudent) throws StudentService.StudentNotfoundException {
        updatedStudent.setStudentId(id);
        studentService.updateStudent(updatedStudent);
    }

    @Operation(summary = "查詢學生選課", description = "查詢學生選課")
    @GetMapping("/{id}/courses")
    public ResponseEntity<List<CourseEnrollmentDTO>> getStudentCourses(
            @PathVariable("id") int studentId,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) throws StudentService.StudentNotfoundException {
        // Verify student exists
        studentService.getStudentById(studentId);
        
        if (pageNumber != null && pageSize != null) {
            if (pageNumber < 0 || pageSize < 0) {
                throw new IllegalArgumentException("Invalid page number or page size");
            }
            // 分頁查詢
            Page<CourseEnrollmentDTO> page = enrollmentService.getStudentEnrollments(studentId, pageNumber, pageSize);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        } else {
            // 無分頁查詢
            List<CourseEnrollmentDTO> courses = enrollmentService.getStudentEnrollments(studentId);
            return ResponseEntity.ok(courses);
        }
    }

    @Operation(summary = "新增學生選課", description = "新增學生選課")
    @PostMapping("/{student_id}/courses/{course_id}")
    public Enrollment addStudentCourse(@PathVariable("student_id") int studentId, @PathVariable("course_id") int courseId) throws StudentService.StudentNotfoundException, CourseService.CourseNotfoundException, EnrollmentService.EnrollmentAlreadyExistsException {
        Student student = studentService.getStudentById(studentId);
        Course course = courseService.getCourseById(courseId);

        Enrollment enrollment = new Enrollment(
                new EnrollmentId(studentId, courseId),
                student,
                course,
                Date.valueOf(LocalDate.now())
        );
        return enrollmentService.addEnrollment(enrollment);
    }

    @Operation(summary = "刪除學生選課", description = "刪除學生選課")
    @DeleteMapping("/{student_id}/courses/{course_id}")
    public void deleteStudentCourses(@PathVariable("student_id") int studentId, @PathVariable("course_id") int courseId) throws StudentService.StudentNotfoundException, CourseService.CourseNotfoundException, EnrollmentService.EnrollmentNotfoundException {
        Student student = studentService.getStudentById(studentId);
        Course course = courseService.getCourseById(courseId);
        enrollmentService.deleteEnrollment(student.getStudentId(), course.getCourseId());
    }

    @Operation(summary = "依照名字查詢學生", description = "根據名字關鍵字查詢學生資訊")
    @GetMapping("/first_name/{firstName}")
    public List<Student> getStudentsByFirstName(@PathVariable("firstName") String firstName) {
        return studentService.findStudentsByFirstName(firstName);
    }

    @Operation(summary = "依照Email查詢學生", description = "根據Email查詢學生資訊")
    @GetMapping("/email/{email}")
    public Student getStudentsByEmail(@PathVariable("email") String email) {
        return studentService.findStudentsByEmail(email);
    }
}
