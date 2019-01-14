package apijson.demo.server.demo;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.service.ObjectParser;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.SqlExecutor;
import com.zhangls.apijson.base.service.Verifier;
import com.zhangls.apijson.base.service.impl.AbstractObjectParser;
import com.zhangls.apijson.base.service.impl.AbstractParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhangls on 2019/1/14.
 * @author zhangls
 */
@Slf4j
@Component
public class DemoParser extends AbstractParser<Long> {

    @Autowired
    private DemoVerifier demoVerifier;

    @Autowired
    private DemoSqlConfig demoSqlConfig;

    @Autowired
    private DemoSqlExecutor demoSqlExecutor;

//    @Autowired
//    private DemoFunction function;


    @Override
    public Verifier<Long> createVerifier() {
        return demoVerifier;
    }

    @Override
    public SqlConfig createSQLConfig() {
        return demoSqlConfig;
    }

    @Override
    public SqlExecutor createSQLExecutor() {
        return demoSqlExecutor;
    }

    @Override
    public Object onFunctionParse(JSONObject object, String function) throws Exception {
        return null;
    }

    @Override
    public ObjectParser createObjectParser(JSONObject request, String parentPath, String name, SqlConfig arrayConfig) throws Exception {

        AbstractObjectParser parser = new AbstractObjectParser(request, parentPath, name, arrayConfig) {
            @Override
            public JSONObject parseResponse(JsonApiRequest request) throws Exception {
                DemoParser demoParser = new DemoParser();

                demoParser.setNoVerifyLogin(noVerifyLogin);
                demoParser.setNoVerifyRole(noVerifyRole);

                return demoParser.parseResponse(request);
            }

            @Override
            public SqlConfig newSQLConfig() throws Exception {
                return DemoSqlConfig.newSQLConfig(method, table, sqlRequest, joinList);
            }
        };

        parser.setMethod(requestMethod);
        parser.setParser(this);

        return parser;
    }
}
