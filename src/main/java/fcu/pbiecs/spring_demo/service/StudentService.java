package fcu.pbiecs.spring_demo.service;

import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {

    static public class StudentNotfoundException extends Exception {
        public StudentNotfoundException(String m) {
            super(m);
        }
    }

    static public class StudentAlreadyExistsException extends Exception {
        public StudentAlreadyExistsException(String m) {
            super(m);
        }
    }

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudent() {
        return studentRepository.findAll();
    }

    public Page<Student> getAllStudent(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return studentRepository.findAll(pageable);
    }

    public Student getStudentById(int id) throws StudentNotfoundException {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            throw new StudentNotfoundException("Student not found with id: " + id);
        }
        return student;
    }

    public Student addStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Student student) throws StudentNotfoundException {
        if (!studentRepository.existsById(student.getStudentId())) {
            throw new StudentNotfoundException("Student not found with id: " + student.getStudentId());
        }
        return studentRepository.save(student);
    }

    public void deleteStudent(int id) throws StudentNotfoundException {
        if (!studentRepository.existsById(id)) {
            throw new StudentNotfoundException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    public List<Student> findStudentsByFirstName(String firstName) {
        return studentRepository.findByFirstNameContaining(firstName);
    }

    public Student findStudentsByEmail(String email) {
        return studentRepository.findStudentByEmail(email);
    }

    public List<Student> searchStudents(String keyword) {
        return studentRepository.findByKeyword(keyword);
    }

    public Page<Student> searchStudents(String keyword, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return studentRepository.findByKeyword(keyword, pageable);
    }


}