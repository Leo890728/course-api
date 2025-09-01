package fcu.pbiecs.spring_demo.service;

import fcu.pbiecs.spring_demo.model.Enrollment;
import fcu.pbiecs.spring_demo.model.EnrollmentId;
import fcu.pbiecs.spring_demo.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


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

    public List<Enrollment> getAllEnrollment(){
        return enrollmentRepository.findAll();
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
}
