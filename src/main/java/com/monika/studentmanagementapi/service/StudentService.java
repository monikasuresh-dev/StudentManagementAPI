package com.monika.studentmanagementapi.service;

import com.monika.studentmanagementapi.exception.StudentNotFoundException;
import com.monika.studentmanagementapi.model.Student;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Business logic for student records.
 *
 * Data is kept in memory only, so ALL records are lost when the application stops.
 * ConcurrentHashMap + AtomicLong keep it safe if several requests arrive at once.
 */
@Service
public class StudentService {

    private final Map<Long, Student> students = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public Student create(Student input) {
        long id = idGenerator.incrementAndGet(); // unique id, never reused
        Student saved = new Student(id, input.getName(), input.getEmail(),
                input.getDepartment(), input.getYear());
        students.put(id, saved);
        return saved;
    }

    public List<Student> findAll() {
        List<Student> all = new ArrayList<>(students.values());
        all.sort(Comparator.comparing(Student::getId));
        return all;
    }

    public Student findById(Long id) {
        Student student = students.get(id);
        if (student == null) {
            throw new StudentNotFoundException(id);
        }
        return student;
    }

    public Student update(Long id, Student input) {
        findById(id); // throws StudentNotFoundException if the id does not exist
        Student updated = new Student(id, input.getName(), input.getEmail(),
                input.getDepartment(), input.getYear());
        students.put(id, updated);
        return updated;
    }

    public void delete(Long id) {
        if (students.remove(id) == null) {
            throw new StudentNotFoundException(id);
        }
    }
}
