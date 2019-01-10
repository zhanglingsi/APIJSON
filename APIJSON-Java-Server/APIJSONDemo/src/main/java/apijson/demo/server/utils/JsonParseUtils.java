package apijson.demo.server.utils;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.exception.*;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import zuo.biao.apijson.parser.APIJSONProvider;
import zuo.biao.apijson.parser.SQLExplorer;
import zuo.biao.apijson.parser.SQLProviderException;
import zuo.biao.apijson.parser.StatementType;

import javax.activation.UnsupportedDataTypeException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

/**
 * Created by zhangls on 2019/1/4.
 * @author zhangls
 */
@Slf4j
public class JsonParseUtils {


    /**
     * 解析Json to Sql
     * @param reqJson
     * @return
     * @throws SQLProviderException
     */
    public static String JsonToSql(JSONObject reqJson) throws SQLProviderException {
        //一个APIJSON解析器拿到一个json对象
        APIJSONProvider apijsonProvider = new APIJSONProvider(reqJson);
        //当前为查询模式，还支持新增，修改，删除
        apijsonProvider.setStatementType(StatementType.SELECT);
        //装载APIJSON解析器
        SQLExplorer builder = new SQLExplorer(apijsonProvider);
        //拿到SQL
        return builder.getSQL();
    }

    /**
     * 新建错误状态内容
     *
     * @param e
     * @return
     */
    public static JSONObject newErrorResult(Exception e) {
        if (e != null) {
            e.printStackTrace();

            int code;
            if (e instanceof UnsupportedEncodingException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_ENCODING;
            } else if (e instanceof IllegalAccessException) {
                code = JsonApiResponse.CODE_ILLEGAL_ACCESS;
            } else if (e instanceof UnsupportedOperationException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_OPERATION;
            } else if (e instanceof NotExistException) {
                code = JsonApiResponse.CODE_NOT_FOUND;
            } else if (e instanceof IllegalArgumentException) {
                code = JsonApiResponse.CODE_ILLEGAL_ARGUMENT;
            } else if (e instanceof NotLoggedInException) {
                code = JsonApiResponse.CODE_NOT_LOGGED_IN;
            } else if (e instanceof TimeoutException) {
                code = JsonApiResponse.CODE_TIME_OUT;
            } else if (e instanceof ConflictException) {
                code = JsonApiResponse.CODE_CONFLICT;
            } else if (e instanceof ConditionErrorException) {
                code = JsonApiResponse.CODE_CONDITION_ERROR;
            } else if (e instanceof UnsupportedDataTypeException) {
                code = JsonApiResponse.CODE_UNSUPPORTED_TYPE;
            } else if (e instanceof OutOfRangeException) {
                code = JsonApiResponse.CODE_OUT_OF_RANGE;
            } else if (e instanceof NullPointerException) {
                code = JsonApiResponse.CODE_NULL_POINTER;
            } else {
                code = JsonApiResponse.CODE_SERVER_ERROR;
            }

            return newResult(code, e.getMessage());
        }

        return newResult(JsonApiResponse.CODE_SERVER_ERROR, JsonApiResponse.MSG_SERVER_ERROR);
    }

    /**
     * 新建带状态内容的JSONObject
     *
     * @param code
     * @param msg
     * @return
     */
    public static JSONObject newResult(int code, String msg) {
        return extendResult(null, code, msg);
    }

    /**
     * 添加JSONObject的状态内容，一般用于错误提示结果
     *
     * @param object
     * @param code
     * @param msg
     * @return
     */
    public static JSONObject extendResult(JSONObject object, Integer code, String msg) {
        if (object == null) {
            object = new JSONObject(true);
        }
        if (!object.containsKey(JsonApiResponse.KEY_CODE)) {
            object.put(JsonApiResponse.KEY_CODE, code);
        }
        String m = StringUtil.getString(object.getString(JsonApiResponse.KEY_MSG));
        if (!m.isEmpty()) {
            msg = m + " ;\n " + StringUtil.getString(msg);
        }
        object.put(JsonApiResponse.KEY_MSG, msg);

        return object;
    }


    /**
     * 添加请求成功的状态内容
     *
     * @param object
     * @return
     */
    public static JSONObject extendSuccessResult(JSONObject object) {
        return extendResult(object, JsonApiResponse.CODE_SUCCESS, JsonApiResponse.MSG_SUCCEED);
    }

    /**
     * 获取请求成功的状态内容
     *
     * @return
     */
    public static JSONObject newSuccessResult() {
        return newResult(JsonApiResponse.CODE_SUCCESS, JsonApiResponse.MSG_SUCCEED);
    }

    /**
     * 添加请求成功的状态内容
     *
     * @param object
     * @return
     */
    public static JSONObject extendErrorResult(JSONObject object, Exception e) {
        JSONObject error = newErrorResult(e);
        return extendResult(object, error.getIntValue(JsonApiResponse.KEY_CODE), error.getString(JsonApiResponse.KEY_MSG));
    }

    /**
     * 解析请求JSONObject
     *
     * @param request => URLDecoder.decode(request, UTF_8);
     * @return
     * @throws Exception
     */
    @NotNull
    public static JSONObject parseRequest(String request) throws Exception {
        JSONObject obj = JsonApi.parseObject(request);
        if (obj == null) {
            throw new UnsupportedEncodingException("【JSON格式不合法！】");
        }
        return obj;
    }

}
