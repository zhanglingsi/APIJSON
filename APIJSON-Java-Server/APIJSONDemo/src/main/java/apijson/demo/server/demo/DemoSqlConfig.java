package apijson.demo.server.demo;

import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.Product;
import apijson.demo.server.model.User;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.impl.AbstractSQLConfig;
import com.zhangls.apijson.base.service.impl.Join;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhangls on 2019/1/14.
 *
 * @author zhangls
 */
@Slf4j
@Component
public class DemoSqlConfig extends AbstractSQLConfig {

    //表名映射，隐藏真实表名，对安全要求很高的表可以这么做
    static {
        TABLE_KEY_MAP.put(User.class.getSimpleName(), "apijson_user");
        TABLE_KEY_MAP.put(Privacy.class.getSimpleName(), "apijson_privacy");
        TABLE_KEY_MAP.put(Product.class.getSimpleName(), "tb_product");
    }

    @Override
    public String getDBUri() {
        return null;
    }

    @Override
    public String getDBAccount() {
        return null;
    }

    @Override
    public String getDBPassword() {
        return null;
    }

    @Override
    public String getSchema() {

        return "apijson";
    }

    public DemoSqlConfig(){}

    public DemoSqlConfig(RequestMethod method, String table) {
        super(method, table);
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
            public DemoSqlConfig getSQLConfig(RequestMethod method, String table) {
                return new DemoSqlConfig(method, table);
            }

            @Override
            public Object newId(RequestMethod method, String table) {
                return System.currentTimeMillis();
            }
        });
    }

}
