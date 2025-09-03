package fcu.pbiecs.spring_demo.controller;

import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.model.Teacher;
import fcu.pbiecs.spring_demo.service.TeacherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教師管理", description = "提供教師 CRUD API")
@RestController
@RequestMapping("api/teachers")
public class TeacherController {

    @Autowired
    TeacherService teacherService;

    @Operation(summary = "查詢所有教師", description = "取得所有教師的資訊")
    @GetMapping
    public ResponseEntity<List<Teacher>> getTeachers(
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            @RequestParam(value = "search", required = false) String searchKeyword) {

        // 處理搜尋功能
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            if (pageNumber == null || pageSize == null) {
                List<Teacher> teachers = teacherService.searchTeachers(searchKeyword.trim());
                return ResponseEntity.ok(teachers);
            }
            if (pageNumber < 0 || pageSize < 0) {
                throw new IllegalArgumentException("Invalid page number or page size");
            }
            // 搜尋 + 分頁查詢
            Page<Teacher> page = teacherService.searchTeachers(searchKeyword.trim(), pageNumber, pageSize);
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
            headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
            return ResponseEntity.ok().headers(headers).body(page.getContent());
        }

        // 一般查詢
        if (pageNumber == null || pageSize == null) {
            List<Teacher> teachers = teacherService.getAllTeacher();
            return ResponseEntity.ok(teachers);
        }
        if (pageNumber < 0 || pageSize < 0) {
            throw new IllegalArgumentException("Invalid page number or page size");
        }
        // 分頁查詢
        Page<Teacher> page = teacherService.getAllTeacher(pageNumber, pageSize);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
        headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @Operation(summary = "查詢教師", description = "依照ID查詢教師資訊")
    @GetMapping("/{id}")
    public Teacher getTeachersById(@PathVariable("id") int id) throws TeacherService.TeacherNotfoundException {
        return teacherService.getTeacherById(id);
    }

    @Operation(summary = "取得教師課程", description = "取得教師課程")
    @GetMapping("/{id}/courses")
    public List<Course> getTeachersCourses(@PathVariable("id") int id) throws TeacherService.TeacherNotfoundException {
        Teacher teacher = teacherService.getTeacherById(id);
        if (teacher == null) {
            return List.of();
        } else {
            return teacher.getCourses();
        }
    }

    @Operation(summary = "新增教師", description = "新增一位教師")
    @PostMapping
    public Teacher addTeachers(@RequestBody Teacher teacher) throws TeacherService.TeacherAlreadyExistsException {
        return teacherService.addTeacher(teacher);
    }

    @Operation(summary = "刪除教師", description = "依照ID刪除教師")
    @DeleteMapping("/{id}")
    public void deleteTeachersById(@PathVariable("id") int id) {
        teacherService.deleteTeacher(id);
    }

    @Operation(summary = "更新教師", description = "依照ID更新教師資訊")
    @PutMapping("/{id}")
    public void updateTeachers(@PathVariable("id") int id, @RequestBody Teacher updatedTeacher) throws TeacherService.TeacherNotfoundException {
        Teacher teacher = teacherService.getTeacherById(id);
        updatedTeacher.setTeacherId(id);
        teacherService.updateTeacher(updatedTeacher);
    }
}
