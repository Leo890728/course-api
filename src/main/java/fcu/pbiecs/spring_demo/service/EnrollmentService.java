package fcu.pbiecs.spring_demo.service;

import fcu.pbiecs.spring_demo.dto.CourseEnrollmentDTO;
import fcu.pbiecs.spring_demo.dto.PopularCourseDTO;
import fcu.pbiecs.spring_demo.dto.StudentEnrollmentDTO;
import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Enrollment;
import fcu.pbiecs.spring_demo.model.EnrollmentId;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.repository.EnrollmentRepository;
import fcu.pbiecs.spring_demo.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class EnrollmentService {

    public static class EnrollmentNotfoundException extends Exception {
        public EnrollmentNotfoundException(String m) {
            super(m);
        }
    }

    public static class EnrollmentAlreadyExistsException extends Exception {
        public EnrollmentAlreadyExistsException(String m) {
            super(m);
        }
    }

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseService courseService;

    public List<Enrollment> getAllEnrollment(){
        return enrollmentRepository.findAll();
    }

    public Page<Enrollment> getAllEnrollment(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return enrollmentRepository.findAll(pageable);
    }

    public Enrollment addEnrollment(Enrollment enrollment) throws EnrollmentAlreadyExistsException {
        if (enrollmentRepository.existsById(enrollment.getId())) {
            throw new EnrollmentAlreadyExistsException("Enrollment already exists with id: " + enrollment.getId().getStudentId() + ", " + enrollment.getId().getCourseId());
        }
        return enrollmentRepository.save(enrollment);

    }

    public void deleteEnrollment(Integer studentId, Integer courseId) throws EnrollmentNotfoundException {
        EnrollmentId id = new EnrollmentId(studentId, courseId);
        if (!enrollmentRepository.existsById(id)) {
            throw new EnrollmentNotfoundException("Enrollment not found with studentId: " + studentId + " and courseId: " + courseId);
        }
        enrollmentRepository.deleteById(id);
    }

    public List<CourseEnrollmentDTO> getStudentEnrollments(Integer studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        return enrollments.stream()
                .map(enrollment -> {
                    Course course = enrollment.getCourse();
                    return new CourseEnrollmentDTO(
                            course.getCourseId(),
                            course.getName(),
                            course.getDescription(),
                            course.getCredits(),
                            course.getTeacher(),
                            enrollment.getEnrollmentDate()
                    );
                })
                .collect(Collectors.toList());
    }

    public Page<CourseEnrollmentDTO> getStudentEnrollments(Integer studentId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByStudentId(studentId, pageable);

        return enrollmentPage.map(enrollment -> {
            Course course = enrollment.getCourse();
            return new CourseEnrollmentDTO(
                    course.getCourseId(),
                    course.getName(),
                    course.getDescription(),
                    course.getCredits(),
                    course.getTeacher(),
                    enrollment.getEnrollmentDate()
            );
        });
    }

    public List<StudentEnrollmentDTO> getCourseEnrollments(Integer courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        return enrollments.stream()
                .map(enrollment -> {
                    Student student = enrollment.getStudent();
                    return new StudentEnrollmentDTO(
                            student.getStudentId(),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getEmail(),
                            student.getBirthday(),
                            enrollment.getEnrollmentDate()
                    );
                })
                .collect(Collectors.toList());
    }

    public Page<StudentEnrollmentDTO> getCourseEnrollments(Integer courseId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Enrollment> enrollmentPage = enrollmentRepository.findByCourseId(courseId, pageable);

        return enrollmentPage.map(enrollment -> {
            Student student = enrollment.getStudent();
            return new StudentEnrollmentDTO(
                    student.getStudentId(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getEmail(),
                    student.getBirthday(),
                    enrollment.getEnrollmentDate()
            );
        });
    }

    public List<PopularCourseDTO> getTopPopularCourses(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = enrollmentRepository.findTopCoursesByEnrollmentCount(pageable);
        
        return results.stream()
                .map(result -> {
                    Integer courseId = (Integer) result[0];
                    Long enrollmentCount = (Long) result[1];
                    
                    try {
                        Course course = courseService.getCourseById(courseId);
                        return new PopularCourseDTO(
                                course.getCourseId(),
                                course.getName(),
                                course.getDescription(),
                                course.getCredits(),
                                course.getTeacher(),
                                enrollmentCount
                        );
                    } catch (CourseService.CourseNotfoundException e) {
                        // Skip courses that might have been deleted
                        return null;
                    }
                })
                .filter(dto -> dto != null) // Remove null entries
                .collect(Collectors.toList());
    }
}
