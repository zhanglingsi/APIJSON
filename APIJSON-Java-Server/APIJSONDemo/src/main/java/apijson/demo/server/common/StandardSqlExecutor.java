package apijson.demo.server.common;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import zuo.biao.apijson.Log;
import zuo.biao.apijson.server.AbstractSQLExecutor;
import zuo.biao.apijson.server.SQLConfig;

import javax.validation.constraints.NotNull;
import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangls on 2019/1/2.
 *
 * @author zhangls
 */
@Slf4j
public class StandardSqlExecutor extends AbstractSQLExecutor {
    private static final String TAG = "DemoSQLExecutor";


    static {
        try { //加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            log.info("【成功加载 MySQL 驱动！】");
        } catch (ClassNotFoundException e) {
            log.info("【严重ERROR】【加载 MySQL 驱动失败！】");
            e.printStackTrace();
        }

        try { //加载驱动程序
            Class.forName("org.postgresql.Driver");
            log.info("【成功加载 PostgresSQL 驱动！】");
        } catch (ClassNotFoundException e) {
            log.info("【严重ERROR】【加载 PostgresSQL 驱动失败！】");
            e.printStackTrace();
        }
    }


    @Override
    public ResultSet executeQuery(@NotNull SQLConfig config) throws Exception {
        return getStatement(config).executeQuery();
    }

    @Override
    public int executeUpdate(@NotNull SQLConfig config) throws Exception {
        return getStatement(config).executeUpdate();
    }


    //TODO String 改为 enum Database 解决大小写不一致(MySQL, mysql等)导致创建多余的 Connection
    private Map<String, Connection> connectionMap = Maps.newHashMap();

    /**
     * @param config
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
    private PreparedStatement getStatement(@NotNull SQLConfig config) throws Exception {
        Connection connection = connectionMap.get(config.getDatabase());

        if (connection == null || connection.isClosed()) {
            log.info("【获取数据库链接Connection对象为空或者已经被关闭！】");

            if (StandardSqlConfig.DATABASE_POSTGRESQL.equalsIgnoreCase(config.getDatabase())) {
                connection = DriverManager.getConnection(config.getDBUri(), config.getDBAccount(), config.getDBPassword());
            } else {
                connection = DriverManager.getConnection(config.getDBUri() + "?useUnicode=true&useSSL=false&characterEncoding=UTF-8&user="
                        + config.getDBAccount() + "&password=" + config.getDBPassword());
            }
            connectionMap.put(config.getDatabase(), connection);
        }

        PreparedStatement statement = null;

        try {
            String sql = config.getSQL(config.isPrepared());
            log.info("【★★★重要日志★★★】【执行SQL为：】：{}", sql);

            statement = connection.prepareStatement(sql);

            List<Object> valueList = config.isPrepared() ? config.getPreparedValueList() : null;

            if (valueList != null && !valueList.isEmpty()) {

                for (int i = 0; i < valueList.size(); i++) {

                    if (StandardSqlConfig.DATABASE_POSTGRESQL.equalsIgnoreCase(config.getDatabase())) {
                        statement.setObject(i + 1, valueList.get(i));
                    } else {
                        String val = String.valueOf(valueList.get(i));
                        log.info("【重要日志】【执行SQL第 {} 个参数为：】：{}", i + 1, val);
                        statement.setString(i + 1, val);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("【严重ERROR】【创建PreparedStatement对象失败！】");
            e.printStackTrace();
        }

        return statement;
    }


    /**
     * 关闭连接，释放资源
     */
    @Override
    public void close() {
        super.close();

        if (connectionMap == null) {
            return;
        }

        Collection<Connection> connections = connectionMap.values();

        if (connections != null) {
            for (Connection connection : connections) {
                try {
                    if (connection != null && connection.isClosed() == false) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        connectionMap.clear();
        connectionMap = null;
    }

}
