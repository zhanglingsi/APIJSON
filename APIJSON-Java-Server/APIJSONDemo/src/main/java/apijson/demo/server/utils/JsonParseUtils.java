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

}
