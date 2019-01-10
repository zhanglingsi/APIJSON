package zuo.biao.apijson.server;

import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import zuo.biao.apijson.parser.APIJSONProvider;
import zuo.biao.apijson.parser.SQLExplorer;
import zuo.biao.apijson.parser.StatementType;

/**
 * Created by zhangls on 2019/1/9.
 */
public class SQLParseTest {

    @Test
    public void jdbcTemplateTest(){
        return;
    }

    public static void main(String[] args) throws Exception{

        String json = "{'Product':{'id':'1'}}";
        JSONObject obj = JSONObject.parseObject(json);

        APIJSONProvider apijsonProvider = new APIJSONProvider(obj); //一个APIJSON解析器拿到一个json对象
        apijsonProvider.setStatementType(StatementType.SELECT); //当前为查询模式，还支持新增，修改，删除
        SQLExplorer builder = new SQLExplorer(apijsonProvider); //装载APIJSON解析器
        System.out.println(builder.getSQL());//拿到SQL
    }
}
