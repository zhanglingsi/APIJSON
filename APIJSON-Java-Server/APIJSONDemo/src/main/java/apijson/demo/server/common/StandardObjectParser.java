package apijson.demo.server.common;

import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.service.Parser;
import com.zhangls.apijson.base.service.SqlConfig;
import com.zhangls.apijson.base.service.impl.AbstractObjectParser;
import com.zhangls.apijson.utils.StringUtil;

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

    public StandardObjectParser(HttpSession session, @NotNull JSONObject request, String parentPath, String name, SqlConfig arrayConfig) throws Exception {
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
    public SqlConfig newSQLConfig() throws Exception {
        return StandardSqlConfig.newSQLConfig(method, table, sqlRequest, joinList);
    }


}
