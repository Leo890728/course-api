package fcu.pbiecs.spring_demo.service;

import com.github.javafaker.Faker;
import fcu.pbiecs.spring_demo.model.*;
import fcu.pbiecs.spring_demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGeneratorService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final Random random = new Random();
    private final Faker faker = new Faker(new Locale("zh-TW"));
    private final Faker englishFaker = new Faker();

    public interface ProgressCallback {
        void updateProgress(String step, int current, int total);
    }

    @Transactional
    public void generateRandomData(int studentCount, int teacherCount, int courseCount, int enrollmentCount, ProgressCallback callback) {
        try {
            // 清除現有資料
            if (callback != null) callback.updateProgress("清除現有資料", 0, 5);
            enrollmentRepository.deleteAll();
            courseRepository.deleteAll();
            studentRepository.deleteAll();
            teacherRepository.deleteAll();

            // 生成學生資料
            if (callback != null) callback.updateProgress("生成學生資料", 1, 5);
            List<Student> students = generateStudents(studentCount);
            studentRepository.saveAll(students);

            // 生成老師資料
            if (callback != null) callback.updateProgress("生成老師資料", 2, 5);
            List<Teacher> teachers = generateTeachers(teacherCount);
            teacherRepository.saveAll(teachers);

            // 生成課程資料
            if (callback != null) callback.updateProgress("生成課程資料", 3, 5);
            List<Course> courses = generateCourses(courseCount, teachers);
            courseRepository.saveAll(courses);

            // 生成選課資料
            if (callback != null) callback.updateProgress("生成選課資料", 4, 5);
            generateEnrollmentsBatchWithProgress(enrollmentCount, students, courses, callback);

            if (callback != null) callback.updateProgress("完成", 5, 5);

        } catch (Exception e) {
            throw new RuntimeException("生成隨機資料時發生錯誤: " + e.getMessage(), e);
        }
    }

    // 保持舊版本方法的兼容性
    public void generateRandomData(int studentCount, int teacherCount, int courseCount, int enrollmentCount) {
        generateRandomData(studentCount, teacherCount, courseCount, enrollmentCount, null);
    }

    private List<Student> generateStudents(int count) {
        if (count > 1000) {
            // 對於大量資料，使用批量SQL插入
            return generateStudentsBatch(count);
        }
        
        List<Student> students = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();

        for (int i = 0; i < count; i++) {
            Student student = new Student();
            
            // 使用Faker生成中文名字
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            student.setFirstName(firstName);
            student.setLastName(lastName);

            // 唯一的email
            String email;
            do {
                email = englishFaker.internet().emailAddress();
            } while (usedEmails.contains(email));
            usedEmails.add(email);
            student.setEmail(email);

            // 使用Faker生成隨機生日 (18-25歲)
            LocalDate birthDate = faker.date().birthday(18, 25).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            student.setBirthday(birthDate.toString());

            students.add(student);
        }

        return students;
    }

    private List<Student> generateStudentsBatch(int count) {
        // 使用原生SQL批量插入，提升效率
        StringBuilder sql = new StringBuilder("INSERT INTO Student (first_name, last_name, email, date_of_birth) VALUES ");
        List<Object> parameters = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            
            String email;
            do {
                email = englishFaker.internet().emailAddress();
            } while (usedEmails.contains(email));
            usedEmails.add(email);
            
            LocalDate birthDate = faker.date().birthday(18, 25).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            
            if (i > 0) sql.append(", ");
            sql.append("(?, ?, ?, ?)");
            
            parameters.add(firstName);
            parameters.add(lastName);
            parameters.add(email);
            parameters.add(birthDate.toString());
        }
        
        // 執行批量插入
        var query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < parameters.size(); i++) {
            query.setParameter(i + 1, parameters.get(i));
        }
        query.executeUpdate();
        
        // 返回插入的學生列表（從資料庫重新讀取）
        return studentRepository.findAll();
    }

    private List<Teacher> generateTeachers(int count) {
        List<Teacher> teachers = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();

        for (int i = 0; i < count; i++) {
            Teacher teacher = new Teacher();
            
            // 使用Faker生成老師名字
            String name = faker.name().fullName();
            teacher.setName(name);

            // 唯一的email
            String email;
            do {
                email = englishFaker.internet().emailAddress();
            } while (usedEmails.contains(email));
            usedEmails.add(email);
            teacher.setEmail(email);

            // 隨機年齡 (30-65歲)
            teacher.setAge(faker.number().numberBetween(30, 66));

            teachers.add(teacher);
        }

        return teachers;
    }

    private List<Course> generateCourses(int count, List<Teacher> teachers) {
        List<Course> courses = new ArrayList<>();
        // 直接讀取指定數量的課程，避免讀取全部資料
        List<String[]> courseData = readCoursesFromCSVLimited(count);
        
        if (courseData.isEmpty()) {
            throw new RuntimeException("無法讀取課程CSV檔案或檔案為空");
        }

        Set<String> usedCourseNames = new HashSet<>();
        
        for (int i = 0; i < Math.min(count, courseData.size()); i++) {
            Course course = new Course();
            
            String[] courseInfo = courseData.get(i);
            String courseName = courseInfo[0];
            
            // 確保課程名稱不重複
            if (usedCourseNames.contains(courseName)) {
                continue;
            }
            usedCourseNames.add(courseName);
            
            course.setName(courseName);
            course.setDescription(courseInfo[2]);
            
            // 使用CSV中的學分數
            try {
                course.setCredits(Integer.parseInt(courseInfo[1]));
            } catch (NumberFormatException e) {
                // 如果解析失敗，使用預設隨機學分數
                course.setCredits(faker.number().numberBetween(1, 5));
            }
            
            // 隨機分配老師 - 在@Transactional中，實體不會分離
            if (!teachers.isEmpty()) {
                Teacher randomTeacher = teachers.get(faker.number().numberBetween(0, teachers.size()));
                course.setTeacher(randomTeacher);
            }

            courses.add(course);
        }

        return courses;
    }

    private void generateEnrollments(int count, List<Student> students, List<Course> courses) {
        if (students.isEmpty() || courses.isEmpty()) {
            return;
        }

        Set<String> enrollmentPairs = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            Student randomStudent = students.get(faker.number().numberBetween(0, students.size()));
            Course randomCourse = courses.get(faker.number().numberBetween(0, courses.size()));
            
            String enrollmentKey = randomStudent.getStudentId() + "-" + randomCourse.getCourseId();
            
            // 避免重複選課
            if (enrollmentPairs.contains(enrollmentKey)) {
                continue;
            }
            enrollmentPairs.add(enrollmentKey);

            Enrollment enrollment = new Enrollment();
            EnrollmentId enrollmentId = new EnrollmentId();
            enrollmentId.setStudentId(randomStudent.getStudentId());
            enrollmentId.setCourseId(randomCourse.getCourseId());
            
            enrollment.setId(enrollmentId);
            enrollment.setStudent(randomStudent);
            enrollment.setCourse(randomCourse);
            
            // 使用Faker生成隨機選課日期 (過去一年內)
            LocalDate enrollmentDate = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            enrollment.setEnrollmentDate(Date.valueOf(enrollmentDate));

            enrollmentRepository.save(enrollment);
        }
    }

    private void generateEnrollmentsBatch(int count, List<Student> students, List<Course> courses) {
        if (students.isEmpty() || courses.isEmpty()) {
            return;
        }

        if (count > 5000) {
            // 對於大量選課資料，使用SQL批量插入
            generateEnrollmentsSQLBatch(count, students, courses);
            return;
        }

        Set<String> enrollmentPairs = new HashSet<>();
        List<Enrollment> enrollments = new ArrayList<>();
        int batchSize = 1000; // 批次大小
        
        for (int i = 0; i < count; i++) {
            Student randomStudent = students.get(faker.number().numberBetween(0, students.size()));
            Course randomCourse = courses.get(faker.number().numberBetween(0, courses.size()));
            
            String enrollmentKey = randomStudent.getStudentId() + "-" + randomCourse.getCourseId();
            
            // 避免重複選課
            if (enrollmentPairs.contains(enrollmentKey)) {
                continue;
            }
            enrollmentPairs.add(enrollmentKey);

            Enrollment enrollment = new Enrollment();
            EnrollmentId enrollmentId = new EnrollmentId();
            enrollmentId.setStudentId(randomStudent.getStudentId());
            enrollmentId.setCourseId(randomCourse.getCourseId());
            
            enrollment.setId(enrollmentId);
            enrollment.setStudent(randomStudent);
            enrollment.setCourse(randomCourse);
            
            // 使用Faker生成隨機選課日期 (過去一年內)
            LocalDate enrollmentDate = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            enrollment.setEnrollmentDate(Date.valueOf(enrollmentDate));

            enrollments.add(enrollment);
            
            // 批次儲存
            if (enrollments.size() >= batchSize) {
                enrollmentRepository.saveAll(enrollments);
                enrollments.clear();
            }
        }
        
        // 儲存剩餘的資料
        if (!enrollments.isEmpty()) {
            enrollmentRepository.saveAll(enrollments);
        }
    }

    private void generateEnrollmentsSQLBatch(int count, List<Student> students, List<Course> courses) {
        Set<String> enrollmentPairs = new HashSet<>();
        final int MEGA_BATCH_SIZE = 10000; // 每批10,000筆，避免記憶體問題
        int processed = 0;
        int attemptCount = 0;
        
        while (processed < count && attemptCount < count * 2) { // 防止無限迴圈
            StringBuilder sql = new StringBuilder("INSERT INTO Enrollment (student_id, course_id, enrollment_date) VALUES ");
            List<Object> parameters = new ArrayList<>();
            int batchCount = 0;
            
            for (int i = 0; i < MEGA_BATCH_SIZE && processed < count && attemptCount < count * 2; i++, attemptCount++) {
                Student randomStudent = students.get(faker.number().numberBetween(0, students.size()));
                Course randomCourse = courses.get(faker.number().numberBetween(0, courses.size()));
                
                String enrollmentKey = randomStudent.getStudentId() + "-" + randomCourse.getCourseId();
                
                // 避免重複選課
                if (enrollmentPairs.contains(enrollmentKey)) {
                    continue;
                }
                enrollmentPairs.add(enrollmentKey);
                
                LocalDate enrollmentDate = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                
                if (batchCount > 0) sql.append(", ");
                sql.append("(?, ?, ?)");
                
                parameters.add(randomStudent.getStudentId());
                parameters.add(randomCourse.getCourseId());
                parameters.add(Date.valueOf(enrollmentDate));
                batchCount++;
                processed++;
            }
            
            if (batchCount > 0) {
                var query = entityManager.createNativeQuery(sql.toString());
                for (int i = 0; i < parameters.size(); i++) {
                    query.setParameter(i + 1, parameters.get(i));
                }
                query.executeUpdate();
                
                // 定期清理快取避免記憶體堆積
                if (processed % 50000 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }
    }

    private void generateEnrollmentsBatchWithProgress(int count, List<Student> students, List<Course> courses, ProgressCallback callback) {
        if (count <= 5000) {
            generateEnrollmentsBatch(count, students, courses);
            return;
        }
        
        Set<String> enrollmentPairs = new HashSet<>();
        final int MEGA_BATCH_SIZE = 10000;
        int processed = 0;
        int attemptCount = 0;
        
        while (processed < count && attemptCount < count * 2) {
            StringBuilder sql = new StringBuilder("INSERT INTO Enrollment (student_id, course_id, enrollment_date) VALUES ");
            List<Object> parameters = new ArrayList<>();
            int batchCount = 0;
            
            for (int i = 0; i < MEGA_BATCH_SIZE && processed < count && attemptCount < count * 2; i++, attemptCount++) {
                Student randomStudent = students.get(faker.number().numberBetween(0, students.size()));
                Course randomCourse = courses.get(faker.number().numberBetween(0, courses.size()));
                
                String enrollmentKey = randomStudent.getStudentId() + "-" + randomCourse.getCourseId();
                
                if (enrollmentPairs.contains(enrollmentKey)) {
                    continue;
                }
                enrollmentPairs.add(enrollmentKey);
                
                LocalDate enrollmentDate = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS)
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                
                if (batchCount > 0) sql.append(", ");
                sql.append("(?, ?, ?)");
                
                parameters.add(randomStudent.getStudentId());
                parameters.add(randomCourse.getCourseId());
                parameters.add(Date.valueOf(enrollmentDate));
                batchCount++;
                processed++;
            }
            
            if (batchCount > 0) {
                var query = entityManager.createNativeQuery(sql.toString());
                for (int i = 0; i < parameters.size(); i++) {
                    query.setParameter(i + 1, parameters.get(i));
                }
                query.executeUpdate();
                
                // 詳細進度回報
                if (callback != null && processed % 50000 == 0) {
                    int percentage = (int) (((processed * 100.0) / count) * 0.2 + 80); // 在80-100%之間
                    callback.updateProgress(String.format("生成選課資料 (%d/%d)", processed, count), 4, 5);
                }
                
                if (processed % 50000 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        }
    }

    private List<String[]> readCoursesFromCSV() {
        List<String[]> courseData = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader("courses.csv"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    courseData.add(new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()});
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("讀取課程CSV檔案時發生錯誤: " + e.getMessage(), e);
        }
        
        return courseData;
    }

    private List<String[]> readCoursesFromCSVLimited(int maxCount) {
        List<String[]> courseData = new ArrayList<>();
        int readCount = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader("courses.csv"))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null && readCount < maxCount) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // 跳過標題行
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    courseData.add(new String[]{parts[0].trim(), parts[1].trim(), parts[2].trim()});
                    readCount++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("讀取課程CSV檔案時發生錯誤: " + e.getMessage(), e);
        }
        
        return courseData;
    }

    public int getAvailableCourseCount() {
        try {
            List<String[]> courseData = readCoursesFromCSV();
            return courseData.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public void clearAllData() {
        try {
            // 方法1: 使用原生SQL並暫時禁用外鍵約束檢查
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            
            entityManager.createNativeQuery("TRUNCATE TABLE Enrollment").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Course").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Student").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE Teacher").executeUpdate();
            
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            
            // 清除Hibernate的一級快取
            entityManager.clear();
        } catch (Exception e) {
            try {
                // 如果TRUNCATE失敗，回退到DELETE
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                
                entityManager.createNativeQuery("DELETE FROM Enrollment").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM Course").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM Student").executeUpdate();
                entityManager.createNativeQuery("DELETE FROM Teacher").executeUpdate();
                
                entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
                entityManager.clear();
            } catch (Exception fallbackException) {
                throw new RuntimeException("清除資料失敗: " + fallbackException.getMessage(), fallbackException);
            }
        }
    }

}