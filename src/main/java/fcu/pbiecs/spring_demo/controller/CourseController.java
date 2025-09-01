package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.model.Teacher;
import fcu.pbiecs.spring_demo.service.CourseService;
import fcu.pbiecs.spring_demo.service.EnrollmentService;
import fcu.pbiecs.spring_demo.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Course> getCourses() {
        return courseService.getAllCourse();
    }

    @Operation(summary = "查詢課程", description = "依照ID查詢課程資訊")
    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable("id") int id) throws CourseService.CourseNotfoundException {
        return courseService.getCourseById(id);
    }

    @Operation(summary = "查詢選課學生", description = "查詢選課學生")
    @GetMapping("/{id}/Students")
    public List<Student> getCourseStudents(@PathVariable("id") int id) throws CourseService.CourseNotfoundException {
        Course course = courseService.getCourseById(id);
        return course.getStudents();
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
