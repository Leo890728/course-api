package fcu.pbiecs.spring_demo.service;

import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Teacher;
import fcu.pbiecs.spring_demo.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TeacherService {

    static public class TeacherNotfoundException extends Exception {
        public TeacherNotfoundException(String message) {
            super(message);
        }
    }

    static public class TeacherAlreadyExistsException extends Exception {
        public TeacherAlreadyExistsException(String message) {
            super(message);
        }
    }

    @Autowired
    private TeacherRepository teacherRepository;

    public List<Teacher> getAllTeacher(){
        return teacherRepository.findAll();
    }

    public Page<Teacher> getAllTeacher(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return teacherRepository.findAll(pageable);
    }

    public Teacher getTeacherById(int id) throws TeacherNotfoundException {
        Teacher teacher = teacherRepository.findById(id).orElse(null);
        if (teacher == null) {
            throw new TeacherNotfoundException("Teacher with ID " + id + " not found.");
        }
        return teacherRepository.findById(id).orElse(null);
    }

    public Teacher addTeacher(Teacher teacher)  {
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(Teacher teacher) throws TeacherNotfoundException {
        if (teacher == null || !teacherRepository.existsById(teacher.getTeacherId())) {
            throw new TeacherNotfoundException("Teacher with ID " + (teacher != null ? teacher.getTeacherId() : "null") + " not found.");
        }
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(int id) {
        teacherRepository.deleteById(id);
    }


}
