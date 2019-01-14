package apijson.demo.server.dao;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhangls on 2019/1/9.
 *
 * @author zhangls
 */
@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 通用查询方法
     *
     * @param sql
     * @return
     */
    public List<LinkedHashMap<String, Object>> queryAll(String sql) {
        return jdbcTemplate.query(sql, (ResultSet resultSet, int rowNum) -> {

            LinkedHashMap<String, Object> map = Maps.newLinkedHashMap();

            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            Integer count = resultSetMetaData.getColumnCount();

            for (int i = 0; i < count; i++) {
                String columnName = resultSetMetaData.getColumnName(i + 1);
                map.put(columnName, resultSet.getObject(columnName));
            }

            return map;
        });
    }

    /**
     * 增删改
     *
     * @param sql
     * @return
     */
    public Integer updateAll(String sql) {
        return jdbcTemplate.update(sql);
    }

}
