package fcu.pbiecs.spring_demo.service;

import fcu.pbiecs.spring_demo.model.Course;
import fcu.pbiecs.spring_demo.model.Student;
import fcu.pbiecs.spring_demo.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CourseService {

    public static class CourseNotfoundException extends Exception {
        public CourseNotfoundException(String m) {
            super(m);
        }
    }

    public static class CourseAlreadyExistsException extends Exception {
        public CourseAlreadyExistsException(String m) {
            super(m);
        }
    }

    @Autowired
    private CourseRepository courseRepository;

    public List<Course> getAllCourse(){
        return courseRepository.findAll();
    }

    public Page<Course> getAllCourse(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return courseRepository.findAll(pageable);
    }

    public Course getCourseById(int id) throws CourseNotfoundException {
        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) {
            throw new CourseNotfoundException("Course not found with id: " + id);
        }
        return course;
    }

    public Course addCourse(Course course)  {
        return courseRepository.save(course);
    }

    public void updateCourse(Course course) throws CourseNotfoundException {
        if (!courseRepository.existsById(course.getCourseId())) {
            throw new CourseNotfoundException("Course not found with id: " + course.getCourseId());
        }
        Course oldCourse = courseRepository.getReferenceById(course.getCourseId());
        oldCourse.setName(course.getName());
        oldCourse.setDescription(course.getDescription());
        oldCourse.setCredits(course.getCredits());
        oldCourse.setTeacher(course.getTeacher());
        courseRepository.save(oldCourse);
    }

    public void deleteCourse(int id) throws CourseNotfoundException {
        if (!courseRepository.existsById(id)) {
            throw new CourseNotfoundException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }


}
