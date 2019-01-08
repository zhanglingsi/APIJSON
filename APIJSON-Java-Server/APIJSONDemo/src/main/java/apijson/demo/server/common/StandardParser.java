package apijson.demo.server.common;

import apijson.demo.server.test.DemoFunction;
import com.alibaba.fastjson.JSONObject;
import zuo.biao.apijson.RequestMethod;
import zuo.biao.apijson.server.AbstractParser;
import zuo.biao.apijson.server.JSONRequest;
import zuo.biao.apijson.server.SQLConfig;

import javax.servlet.http.HttpSession;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
public class StandardParser extends AbstractParser<Long> {


    public StandardParser() {
        super();
    }

    public StandardParser(RequestMethod method) {
        super(method);
    }

    public StandardParser(RequestMethod method, boolean noVerify) {
        super(method, noVerify);
    }

    protected HttpSession session;

    public HttpSession getSession() {
        return session;
    }

    public StandardParser setSession(HttpSession session) {
        this.session = session;
        setVisitor(StandardVerifier.getVisitor(session));
        return this;
    }


    @Override
    public StandardVerifier createVerifier() {
        return new StandardVerifier();
    }

    @Override
    public StandardSqlConfig createSQLConfig() {
        return new StandardSqlConfig();
    }

    @Override
    public StandardSqlExecutor createSQLExecutor() {
        return new StandardSqlExecutor();
    }

    @Override
    public JSONObject parseResponse(JSONObject request) {
        //补充format
        if (session != null && request != null && request.get(JSONRequest.KEY_FORMAT) == null) {
            request.put(JSONRequest.KEY_FORMAT, session.getAttribute(JSONRequest.KEY_FORMAT));
        }
        return super.parseResponse(request);
    }

    private DemoFunction function;

    @Override
    public Object onFunctionParse(JSONObject json, String fun) throws Exception {
        if (function == null) {
            function = new DemoFunction(requestMethod, session);
        }
        return function.invoke(fun, json);
    }


    @Override
    public StandardObjectParser createObjectParser(JSONObject request, String parentPath, String name, SQLConfig arrayConfig) throws Exception {

        return new StandardObjectParser(session, request, parentPath, name, arrayConfig) {

            //TODO 删除，onPUTArrayParse改用MySQL函数JSON_ADD, JSON_REMOVE等
            @Override
            public JSONObject parseResponse(JSONRequest request) throws Exception {
                StandardParser demoParser = new StandardParser(RequestMethod.GET);
                demoParser.setSession(session);
                //parser.setNoVerifyRequest(noVerifyRequest)
                demoParser.setNoVerifyLogin(noVerifyLogin);
                demoParser.setNoVerifyRole(noVerifyRole);
                return demoParser.parseResponse(request);
            }
        }.setMethod(requestMethod).setParser(this);
    }


    @Override
    protected void onVerifyContent() throws Exception {
        //补充全局缺省版本号  //可能在默认为1的前提下这个请求version就需要为0  requestObject.getIntValue(JSONRequest.KEY_VERSION) <= 0) {
        if (session != null && requestObject.get(JSONRequest.KEY_VERSION) == null) {
            requestObject.put(JSONRequest.KEY_VERSION, session.getAttribute(JSONRequest.KEY_VERSION));
        }
        super.onVerifyContent();
    }

    //	//可重写来设置最大查询数量
    //	@Override
    //	public int getMaxQueryCount() {
    //		return 50;
    //	}

}
