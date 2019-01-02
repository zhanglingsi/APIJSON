package apijson.demo.server.common;

import com.alibaba.fastjson.JSONObject;
import zuo.biao.apijson.RequestMethod;
import zuo.biao.apijson.StringUtil;
import zuo.biao.apijson.server.AbstractObjectParser;
import zuo.biao.apijson.server.Parser;
import zuo.biao.apijson.server.SQLConfig;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
public abstract class StandardObjectParser extends AbstractObjectParser {

    static {
        //手机
        COMPILE_MAP.put("phone", StringUtil.PATTERN_PHONE);
        //邮箱
        COMPILE_MAP.put("email", StringUtil.PATTERN_EMAIL);
        //身份证号
        COMPILE_MAP.put("idCard", StringUtil.PATTERN_ID_CARD);
    }

    /**
     * for single object
     *
     * @param parentPath
     * @param request
     * @param name
     * @throws Exception
     */
    public StandardObjectParser(HttpSession session, @NotNull JSONObject request, String parentPath, String name, SQLConfig arrayConfig) throws Exception {
        super(request, parentPath, name, arrayConfig);
    }

    @Override
    public StandardObjectParser setMethod(RequestMethod method) {
        super.setMethod(method);
        return this;
    }

    @Override
    public StandardObjectParser setParser(Parser<?> parser) {
        super.setParser(parser);
        return this;
    }

    @Override
    public SQLConfig newSQLConfig() throws Exception {
        return StandardSqlConfig.newSQLConfig(method, table, sqlRequest, joinList);
    }


}
