package apijson.demo.server.common;

import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.Product;
import apijson.demo.server.model.User;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.impl.AbstractSQLConfig;
import com.zhangls.apijson.base.service.impl.Join;
import com.zhangls.apijson.utils.StringUtil;

import java.util.List;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
public class StandardSqlConfig extends AbstractSQLConfig {


    //表名映射，隐藏真实表名，对安全要求很高的表可以这么做
    static {
        TABLE_KEY_MAP.put(User.class.getSimpleName(), "apijson_user");
        TABLE_KEY_MAP.put(Privacy.class.getSimpleName(), "apijson_privacy");
        TABLE_KEY_MAP.put(Product.class.getSimpleName(), "tb_product");
    }

    @Override
    public String getDBUri() {
        return DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? "jdbc:postgresql://localhost:5432/postgres" : "jdbc:mysql://localhost:3306";
    }

    @Override
    public String getDBAccount() {
        return DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? "postgres" : "root";
    }

    @Override
    public String getDBPassword() {
        return DATABASE_POSTGRESQL.equalsIgnoreCase(getDatabase()) ? null : "123456";
    }

    @Override
    public String getSchema() {
        String s = super.getSchema();
        return StringUtil.isEmpty(s, true) ? "apijson" : s;
    }


    public StandardSqlConfig() {
        this(RequestMethod.GET);
    }

    public StandardSqlConfig(RequestMethod method) {
        super(method);
    }

    public StandardSqlConfig(RequestMethod method, String table) {
        super(method, table);
    }

    public StandardSqlConfig(RequestMethod method, int count, int page) {
        super(method, count, page);
    }


    /**
     * 获取SQL配置
     *
     * @param table
     * @param request
     * @return
     * @throws Exception
     */
    public static SqlConfig newSQLConfig(RequestMethod method, String table, JSONObject request, List<Join> joinList) throws Exception {
        return newSQLConfig(method, table, request, joinList, new Callback() {

            @Override
            public StandardSqlConfig getSQLConfig(RequestMethod method, String table) {
                return new StandardSqlConfig(method, table);
            }

            @Override
            public Object newId(RequestMethod method, String table) {
                return System.currentTimeMillis();
            }
        });
    }


}
