package apijson.demo.server.common;

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
 * @author zhangls
 */

public class StandardSqlExecutor extends AbstractSQLExecutor {
    private static final String TAG = "DemoSQLExecutor";


    static {
        try { //加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            Log.d(TAG, "成功加载 MySQL 驱动！");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try { //加载驱动程序
            Class.forName("org.postgresql.Driver");
            Log.d(TAG, "成功加载 PostgresSQL 驱动！");
        } catch (ClassNotFoundException e) {
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
    private Map<String, Connection> connectionMap = new HashMap<>();

    /**
     * @param config
     * @return
     * @throws Exception
     */
    @SuppressWarnings("resource")
    private PreparedStatement getStatement(@NotNull SQLConfig config) throws Exception {
        Connection connection = connectionMap.get(config.getDatabase());
        if (connection == null || connection.isClosed()) {
            Log.i(TAG, "select  connection " + (connection == null ? " = null" : ("isClosed = " + connection.isClosed())));

            if (StandardSqlConfig.DATABASE_POSTGRESQL.equalsIgnoreCase(config.getDatabase())) {
                connection = DriverManager.getConnection(config.getDBUri(), config.getDBAccount(), config.getDBPassword());
            } else {
                connection = DriverManager.getConnection(config.getDBUri() + "?useUnicode=true&characterEncoding=UTF-8&user="
                        + config.getDBAccount() + "&password=" + config.getDBPassword());
            }
            connectionMap.put(config.getDatabase(), connection);
        }

        PreparedStatement statement = connection.prepareStatement(config.getSQL(config.isPrepared()));
        List<Object> valueList = config.isPrepared() ? config.getPreparedValueList() : null;

        if (valueList != null && valueList.isEmpty() == false) {

            for (int i = 0; i < valueList.size(); i++) {

                if (StandardSqlConfig.DATABASE_POSTGRESQL.equalsIgnoreCase(config.getDatabase())) {
                    statement.setObject(i + 1, valueList.get(i));
                } else {
                    statement.setString(i + 1, "" + valueList.get(i));
                }
            }
        }
        // statement.close();

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
