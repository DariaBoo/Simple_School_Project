package ua.foxminded.dao.implementation;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.foxminded.dao.GroupDAO;
import ua.foxminded.dao.dataSource.DataSourceDAO;
import ua.foxminded.dao.dataSource.DataSourceDAOConfig;
import ua.foxminded.dao.exception.DAOException;
import ua.foxminded.domain.Group;

/**
 * 
 * @author Bogush Daria
 * @version 1.0
 *
 */
public class GroupDAOImpl implements GroupDAO {
    private static GroupDAOImpl instance;
    private static final Logger log = LoggerFactory.getLogger(GroupDAOImpl.class.getName());
    private static final String SQL_SELECTBYSIZE = "SELECT * FROM schoolmanager.groups WHERE count_of_students <= %d ORDER BY group_id ASC";

    private GroupDAOImpl() {

    }

    /**
     * Creates a GroupDAOImpl with ConnectionPool for tests
     * 
     * @author Bogush Daria
     * @param dataSource
     * @see DataSourceDAO
     */
    public GroupDAOImpl(String configFile) {
        DataSourceDAOConfig.setConfigFile(configFile);
    }

    /**
     * Returns instance of the class
     * 
     * @author Bogush Daria
     */
    public static GroupDAOImpl getInstance() {
        if (instance == null) {
            instance = new GroupDAOImpl();
        }
        log.info("Got the class instance");
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Group>> selectBySize(int groupSize) throws DAOException {
        log.trace("Select groups with less or equal groupSize {}", groupSize);
        List<Group> groups = new ArrayList<>();
        String sql = String.format(SQL_SELECTBYSIZE, groupSize);
        log.trace("Create sql query {}", sql);
        log.info("Get connection");
        try (Connection connection = DataSourceDAO.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            log.info("Executed sql query {} with groupSize {}", SQL_SELECTBYSIZE, groupSize);
            while (resultSet.next()) {
                groups.add(new Group(resultSet.getInt("group_id"), resultSet.getString("group_name"),
                        resultSet.getInt("count_of_students")));
            }
            log.debug("Took from resultSet {}", groups);
            return Optional.ofNullable(groups);
        } catch (SQLException sqlE) {
            log.error("Fail to connect to the database", sqlE);
            throw new DAOException("Fail to connect to the database while select groups by size.", sqlE);
        } 
    }
}
