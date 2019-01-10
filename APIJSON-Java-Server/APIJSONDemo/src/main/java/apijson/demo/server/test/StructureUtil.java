package apijson.demo.server.test;

import apijson.demo.server.common.StandardSqlConfig;
import apijson.demo.server.common.StandardSqlExecutor;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.SqlCreator;
import com.zhangls.apijson.base.service.SqlExecutor;
import com.zhangls.apijson.base.service.impl.Structure;
import lombok.extern.slf4j.Slf4j;


/**
 * 结构校验
 *
 * @author Lemon
 */
@Slf4j
public class StructureUtil {
    private static final String TAG = "Structure";


    static final String requestString = "{\"Comment\":{\"DISALLOW\": \"id\", \"NECESSARY\": \"userId,momentId,content\"}, \"ADD\":{\"Comment:to\":{}}}";
    static final String responseString = "{\"User\":{\"REMOVE\": \"phone\", \"REPLACE\":{\"sex\":2}, \"ADD\":{\"name\":\"api\"}}, \"PUT\":{\"Comment:to\":{}}}";

    /**
     * 测试
     *
     * @throws Exception
     */
    public static void test() throws Exception {
        JSONObject request;

        SqlCreator creator = new SqlCreator() {

            @Override
            public SqlConfig createSQLConfig() {
                return new StandardSqlConfig();
            }

            @Override
            public SqlExecutor createSQLExecutor() {
                return new StandardSqlExecutor();
            }
        };

        try {
            request = JsonApi.parseObject("{\"Comment\":{\"userId\":0}}");
            log.debug(TAG, "test  parseRequest = " + Structure.parseRequest(RequestMethod.POST, "", JsonApi.parseObject(requestString), request, creator));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            request = JsonApi.parseObject("{\"Comment\":{\"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
            log.debug(TAG, "test  parseRequest = " + Structure.parseRequest(RequestMethod.POST, "", JsonApi.parseObject(requestString), request, creator));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            request = JsonApi.parseObject("{\"Comment\":{\"id\":0, \"userId\":0, \"momentId\":0, \"content\":\"apijson\"}}");
            log.debug(TAG, "test  parseRequest = " + Structure.parseRequest(RequestMethod.POST, "", JsonApi.parseObject(requestString), request, creator));
        } catch (Exception e) {
            e.printStackTrace();
        }


        JSONObject response;
        try {
            response = JsonApi.parseObject("{\"User\":{\"userId\":0}}");
            log.debug(TAG, "test  parseResponse = " + Structure.parseResponse(RequestMethod.GET, "", JsonApi.parseObject(responseString), response, creator, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            response = JsonApi.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\"}}");
            log.debug(TAG, "test  parseResponse = " + Structure.parseResponse(RequestMethod.GET, "", JsonApi.parseObject(responseString), response, creator, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            response = JsonApi.parseObject("{\"User\":{\"userId\":0, \"phone\":\"12345678\", \"sex\":1}}");
            log.debug(TAG, "test  parseResponse = " + Structure.parseResponse(RequestMethod.GET, "", JsonApi.parseObject(responseString), response, creator, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            response = JsonApi.parseObject("{\"User\":{\"id\":0, \"name\":\"tommy\", \"phone\":\"12345678\", \"sex\":1}}");
            log.debug(TAG, "test  parseResponse = " + Structure.parseResponse(RequestMethod.GET, "", JsonApi.parseObject(responseString), response, creator, null));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
