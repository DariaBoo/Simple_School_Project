package ua.foxminded.dao.implementation;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.dao.StudentDAO;
import ua.foxminded.dao.dataSource.DataSourceDAO;
import ua.foxminded.dao.dataSource.DataSourceDAOConfig;
import ua.foxminded.dao.exception.DAOException;
import ua.foxminded.domain.Student;

/**
 * 
 * @author Bogush Daria
 * @version 1.0
 *
 */
public class StudentDAOImpl implements StudentDAO {
    private static StudentDAOImpl instance;
    private static final Logger log = LoggerFactory.getLogger(StudentDAOImpl.class.getName());
    private static final String SQL_ADDSTUDENT = "INSERT INTO schoolManager.students (first_name, last_name) values ('%s','%s')";
    private static final String SQL_REMOVESTUDENT = "DELETE FROM schoolManager.students WHERE student_id = %d";
    private static final String SQL_FINDSTUDENTSBYCOURSE = "SELECT schoolmanager.students.student_id, schoolmanager.students.first_name, schoolmanager.students.last_name from schoolmanager.students\n"
            + "INNER JOIN schoolmanager.student_course ON schoolmanager.student_course.student_id = schoolmanager.students.student_id\n"
            + "INNER JOIN schoolmanager.courses ON schoolmanager.student_course.course_id = schoolmanager.courses.course_id\n"
            + "WHERE LOWER(schoolmanager.courses.course_name) = '%s'";

    private StudentDAOImpl() {

    }

    /**
     * Creates a StudentDAOImpl with ConnectionPool for tests
     * 
     * @author Bogush Daria
     * @param dataSource
     * @see DataSourceDAO
     */
    public StudentDAOImpl(String configFile) {
        DataSourceDAOConfig.setConfigFile(configFile);
    }

    /**
     * Returns instance of the class
     * 
     * @author Bogush Daria
     */
    public static StudentDAOImpl getInstance() {
        if (instance == null) {
            instance = new StudentDAOImpl();
        }
        log.info("Got the class instance");
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalInt addStudent(Student student) throws DAOException {
        int result = 0;
        log.trace("Add new student to the table 'students' with name {} and surname {}", student.getFirstName(),
                student.getLastName());
        String sql = String.format(SQL_ADDSTUDENT, student.getFirstName(), student.getLastName());
        log.trace("Create sql query {}", sql);
        log.info("Get connection");
        try (Connection connection = DataSourceDAO.getConnection();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql, RETURN_GENERATED_KEYS);
            log.info("Executed sql query {}", sql);
            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    result = resultSet.getInt(1);
                }
                log.debug("Got the id {} of the added student", result);
                return OptionalInt.of(result);
            } catch (SQLException sqlE) {
                log.error("Fail to connect to the database", sqlE);
                throw new DAOException("Fail to connect to the database while add new student", sqlE);
            }
        } catch (SQLException sqlE) {
            log.error("Fail to connect to the database", sqlE);
            throw new DAOException("Fail to connect to the database while add new student", sqlE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OptionalInt removeStudent(int studentID) throws DAOException {
        log.trace("Remove student from the table 'students' by studentID", studentID);
        String sql = String.format(SQL_REMOVESTUDENT, studentID);
        log.trace("Create sql query {}", sql);
        log.info("Get connection");
        try (Connection connection = DataSourceDAO.getConnection();
                Statement statement = connection.createStatement()) {
            log.debug("Deleted the student from the table 'students' by studentID {}", studentID);
            OptionalInt result = OptionalInt.of(statement.executeUpdate(sql));
            log.debug("Deleted the student from the table 'students' by studentID {} with result {}", studentID,
                    result);
            return result;
        } catch (SQLException sqlE) {
            log.error("Fail to connect to the database", sqlE);
            throw new DAOException("", sqlE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Student>> findStudentsByCourse(String courseName) throws DAOException {
        log.trace("Find students by course name {}", courseName);
        List<Student> students = new ArrayList<>();
        String course = courseName.toLowerCase();
        String sql = String.format(SQL_FINDSTUDENTSBYCOURSE, course);
        log.trace("Create sql query {}", sql);
        log.info("Get connection");
        try (Connection connection = DataSourceDAO.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            log.info("Executed sql query {} ", SQL_FINDSTUDENTSBYCOURSE);
            while (resultSet.next()) {
                students.add(new Student.StudentBuidler().setStudentID(resultSet.getInt("student_id"))
                        .setFirstName(resultSet.getString("first_name")).setLastName(resultSet.getString("last_name"))
                        .build());
            }
            log.debug("Took from resultSet list students {}", students);
            return Optional.ofNullable(students);
        } catch (SQLException sqlE) {
            log.error("Fail to connect to the database", sqlE);
            throw new DAOException("Fail to connect to the database while found students by course.", sqlE);
        }
    }
}
