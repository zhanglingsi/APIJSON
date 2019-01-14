package com.zhangls.apijson.base.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.model.RequestRole;
import com.zhangls.apijson.base.service.*;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.zhangls.apijson.base.model.RequestMethod.GET;

/**
 * parser for parsing request to JSONObject
 *
 * @author Lemon
 */
@Slf4j
public abstract class AbstractParser<T> implements Parser<T>, SqlCreator {

    public AbstractParser() {
        this(null);
    }

    public AbstractParser(RequestMethod method) {
        this(method, false);
    }

    /**
     * @param method   null ? requestMethod = GET
     * @param noVerify 仅限于为服务端提供方法免验证特权，普通请求不要设置为true！ 如果对应Table有权限也建议用默认值false，保持和客户端权限一致
     */
    public AbstractParser(RequestMethod method, Boolean noVerify) {
        super();
        setMethod(method);
        setNoVerify(noVerify);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * User的信息访问白名单
     */
    @NotNull
    protected Visitor<T> visitor;

    @NotNull
    @Override
    public Visitor<T> getVisitor() {
        if (visitor == null) {
            visitor = new Visitor<T>() {

                @Override
                public T getId() {
                    return null;
                }

                @Override
                public List<T> getContactIdList() {
                    return null;
                }
            };
        }
        return visitor;
    }

    @Override
    public AbstractParser<T> setVisitor(@NotNull Visitor<T> visitor) {
        this.visitor = visitor;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected RequestMethod requestMethod;

    @NotNull
    @Override
    public RequestMethod getMethod() {
        return requestMethod;
    }

    @NotNull
    @Override
    public AbstractParser<T> setMethod(RequestMethod method) {
        this.requestMethod = method == null ? GET : method;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected JSONObject requestObject;

    @Override
    public JSONObject getRequest() {
        return requestObject;
    }

    @Override
    public AbstractParser<T> setRequest(JSONObject request) {
        this.requestObject = request;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Verifier<T> verifier;
    protected RequestRole globalRole;
    protected Boolean globalFormat;

    public AbstractParser<T> setGlobleRole(RequestRole globleRole) {
        this.globalRole = globleRole;
        return this;
    }

    protected String globleDatabase;

    public AbstractParser<T> setGlobleDatabase(String globleDatabase) {
        this.globleDatabase = globleDatabase;
        return this;
    }

    public AbstractParser<T> setGlobleFormat(Boolean globleFormat) {
        this.globalFormat = globleFormat;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Boolean isNoVerify() {
        return noVerifyLogin && noVerifyRole && noVerifyContent;
    }

    @Override
    public AbstractParser<T> setNoVerify(Boolean noVerify) {
        setNoVerifyLogin(noVerify);
        setNoVerifyRole(noVerify);
        setNoVerifyContent(noVerify);
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Boolean noVerifyLogin;

    @Override
    public Boolean isNoVerifyLogin() {
        return noVerifyLogin;
    }

    @Override
    public AbstractParser<T> setNoVerifyLogin(Boolean noVerifyLogin) {
        this.noVerifyLogin = noVerifyLogin;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Boolean noVerifyRole;

    @Override
    public Boolean isNoVerifyRole() {
        return noVerifyRole;
    }

    @Override
    public AbstractParser<T> setNoVerifyRole(Boolean noVerifyRole) {
        this.noVerifyRole = noVerifyRole;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Boolean noVerifyContent;

    @Override
    public Boolean isNoVerifyContent() {
        return noVerifyContent;
    }

    @Override
    public AbstractParser<T> setNoVerifyContent(Boolean noVerifyContent) {
        this.noVerifyContent = noVerifyContent;
        return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected SqlExecutor sqlExecutor;

    protected Map<String, Object> queryResultMap;


    /**
     * 解析请求json并获取对应结果
     */
    @Override
    public String parse(String request) {
        String responseJsonStr = JsonApi.toJSONString(parseResponse(request));
        log.info("【最终返回结果JSON串为】：{}", responseJsonStr);

        return responseJsonStr;
    }

    /**
     * 解析请求json并获取对应结果
     *
     * @param request
     * @return
     */
    @NotNull
    @Override
    public String parse(JSONObject request) {
        return JsonApi.toJSONString(parseResponse(request));
    }

    /**
     * 解析请求json并获取对应结果
     *
     * @param request 先parseRequest中URLDecoder.decode(request, UTF_8);再parseResponse(getCorrectRequest(...))
     * @return parseResponse(requestObject);
     */
    @NotNull
    @Override
    public JSONObject parseResponse(String request) {
        try {
            requestObject = parseRequest(request);
            log.info("【请求JSON串转换JSONObject成功！】");
        } catch (Exception e) {
            return ParserHelper.newErrorResult(e);
        }

        return parseResponse(requestObject);
    }

    /**
     * 解析请求json并获取对应结果
     *
     * @param request
     * @return requestObject
     */
    @NotNull
    @Override
    public JSONObject parseResponse(JSONObject request) {
        long startTime = System.currentTimeMillis();
        log.info("=========================================================================================================");
        log.info("【开始处理已经转化的JSONObject对象，开始时间为】：{}", startTime);

        requestObject = request;

        // 创建验证器接口
        verifier = createVerifier().setVisitor(getVisitor());

        if (RequestMethod.isPublicMethod(requestMethod).equals(Boolean.FALSE)) {
            try {
                if (!noVerifyLogin) {
                    onVerifyLogin();
                }
                if (!noVerifyContent) {
                    onVerifyContent();
                }
            } catch (Exception e) {
                return ParserHelper.extendErrorResult(requestObject, e);
            }
        }

        //必须在parseCorrectRequest后面，因为parseCorrectRequest可能会添加 @role
        if (!noVerifyRole && globalRole == null) {
            try {
                setGlobleRole(RequestRole.get(requestObject.getString(JsonApiRequest.KEY_ROLE)));
                requestObject.remove(JsonApiRequest.KEY_ROLE);
            } catch (Exception e) {
                return ParserHelper.extendErrorResult(requestObject, e);
            }
        }

        try {
            setGlobleDatabase(requestObject.getString(JsonApiRequest.KEY_DATABASE));
            setGlobleFormat(requestObject.getBooleanValue(JsonApiRequest.KEY_FORMAT));

            requestObject.remove(JsonApiRequest.KEY_DATABASE);
            requestObject.remove(JsonApiRequest.KEY_FORMAT);
        } catch (Exception e) {
            return ParserHelper.extendErrorResult(requestObject, e);
        }

        final String requestString = JsonApi.toJSONString(request);

        queryResultMap = Maps.newHashMap();

        Exception error = null;
        sqlExecutor = createSQLExecutor();
        try {
            requestObject = onObjectParse(request, null, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            error = e;
        }
        sqlExecutor.close();

        sqlExecutor = null;

        requestObject = error == null ? ParserHelper.extendSuccessResult(requestObject) : ParserHelper.extendErrorResult(requestObject, error);

        queryResultMap.clear();

        log.info("【请求方法为】：{}，【请求字符串为】：{}", requestMethod, requestString);

        log.info("【请求返回的结果串为】：{}", JsonApi.toJSONString(requestObject));

        long endTime = System.currentTimeMillis();

        log.info("【处理已经转化的JSONObject对象，结束时间为】：{}", endTime);
        log.info("【处理花费时间为】：{}", (endTime - startTime));
        log.info("=========================================================================================================");

        return globalFormat && JsonApiResponse.isSuccess(requestObject) ? new JsonApiResponse(requestObject) : requestObject;
    }


    protected void onVerifyLogin() throws Exception {
        verifier.verifyLogin();
    }

    protected void onVerifyContent() throws Exception {
        requestObject = parseCorrectRequest();
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

    @Override
    public JSONObject parseCorrectRequest(JSONObject target) throws Exception {
        return Structure.parseRequest(requestMethod, "", target, requestObject, getMaxUpdateCount(), this);
    }


    //TODO 启动时一次性加载Request所有内容，作为初始化。

    /**
     * 获取正确的请求，非GET请求必须是服务器指定的
     */
    @Override
    public JSONObject parseCorrectRequest() throws Exception {
        if (RequestMethod.isPublicMethod(requestMethod)) {
            //需要指定JSON结构的get请求可以改为post请求。一般只有对安全性要求高的才会指定，而这种情况用明文的GET方式几乎肯定不安全
            return requestObject;
        }

        String tag = requestObject.getString(JsonApiRequest.KEY_TAG);
        if (!StringUtil.isNotEmpty(tag, true)) {
            throw new IllegalArgumentException("请在最外层设置tag！一般是Table名，例如 \"tag\": \"User\" ");
        }
        Integer version = requestObject.getIntValue(JsonApiRequest.KEY_VERSION);

        JSONObject object = null;
        String error = "";
        try {
            object = getStructure("Request", JsonApiRequest.KEY_TAG, tag, version);
        } catch (Exception e) {
            error = e.getMessage();
        }

        //empty表示随意操作  || object.isEmpty()) {
        if (object == null) {
            throw new UnsupportedOperationException("非开放请求必须是Request表中校验规则允许的操作！\n " + error);
        }

        JSONObject target = null;
        //tag是table名
        if (JsonApiObject.isTableKey(tag) && object.containsKey(tag) == false) {
            target = new JSONObject(true);
            target.put(tag, object);
        } else {
            target = object;
        }
        //获取指定的JSON结构 >>>>>>>>>>>>>>

        requestObject.remove(JsonApiRequest.KEY_TAG);
        requestObject.remove(JsonApiRequest.KEY_VERSION);

        return parseCorrectRequest((JSONObject) target.clone());
    }


    //TODO 优化性能！

    /**
     * 获取正确的返回结果
     */
    @Override
    public JSONObject parseCorrectResponse(String table, JSONObject response) throws Exception {
        return response;
    }

    /**
     * 获取Request或Response内指定JSON结构
     *
     * @param table
     * @param key
     * @param value
     * @param version
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject getStructure(@NotNull String table, String key, String value, Integer version) throws Exception {
        SqlConfig config = createSQLConfig().setMethod(GET).setTable(table);
        config.setPrepared(false);
        config.setColumn(Arrays.asList("structure"));

        Map<String, Object> where = new HashMap<String, Object>();
        where.put("method", requestMethod.name());
        if (key != null) {
            where.put(key, value);
        }
        if (version > 0) {
            where.put(JsonApiRequest.KEY_VERSION + "{}", ">=" + version);
        }
        config.setWhere(where);
        config.setOrder(JsonApiRequest.KEY_VERSION + (version > 0 ? "+" : "-"));
        config.setCount(1);

        SqlExecutor executor = createSQLExecutor();

        //too many connections error: 不try-catch，可以让客户端看到是服务器内部异常
        try {
            JSONObject result = executor.execute(config.setCacheStatic(true));
            //解决返回值套了一层 "structure":{}
            return ParserHelper.getJSONObject(result, "structure");
        } finally {
            executor.close();
        }
    }


    //	protected SqlConfig itemConfig;

    /**
     * 获取单个对象，该对象处于parentObject内
     */
    @Override
    public JSONObject onObjectParse(final JSONObject request, String parentPath, String name, final SqlConfig arrayConfig) throws Exception {

        log.info("【parentPath】：{}， 【name】：{}，【arrayConfig】：{}", parentPath, name, arrayConfig);

        if (request == null) {
            return null;
        }

        Integer type = arrayConfig == null ? 0 : arrayConfig.getType();

        ObjectParser op = createObjectParser(request, parentPath, name, arrayConfig).parse();


        JSONObject response = null;
        //TODO SQL查询结果为空时，functionMap和customMap还有没有意义？
        if (op != null) {
            if (arrayConfig == null) {
                response = op.executeSQL().response();
            } else {//Array Item Child
                Integer query = arrayConfig.getQuery();

                //total 这里不能用arrayConfig.getType()，因为在createObjectParser.onChildParse传到onObjectParse时已被改掉
                if (type.equals(SqlConfig.TYPE_ITEM_CHILD_0) && query != JsonApiRequest.QUERY_TABLE
                        && arrayConfig.getPosition() == 0) {
                    JSONObject rp = op.setMethod(RequestMethod.HEAD).executeSQL().getSqlReponse();
                    if (rp != null) {
                        Integer index = parentPath.lastIndexOf("]/");
                        if (index >= 0) {
                            Integer total = rp.getIntValue(JsonApiResponse.KEY_COUNT);
                            putQueryResult(parentPath.substring(0, index) + "]/" + JsonApiResponse.KEY_TOTAL, total);

                            if (total <= arrayConfig.getCount() * arrayConfig.getPage()) {
                                //数量不够了，不再往后查询
                                query = JsonApiRequest.QUERY_TOTAL;
                            }
                        }
                    }

                    op.setMethod(requestMethod);
                }

                //Table
                if (query == JsonApiRequest.QUERY_TOTAL) {
                    //不再往后查询
                    response = null;
                } else {
                    response = op.executeSQL(
                            arrayConfig.getCount(), arrayConfig.getPage(), arrayConfig.getPosition()
                    ).response();
                }
            }

            op.recycle();
        }

        return response;
    }

    /**
     * 获取对象数组，该对象数组处于parentObject内
     *
     * @param parentPath parentObject的路径
     * @param name       parentObject的key
     * @param request    parentObject的value
     * @return
     * @throws Exception
     */
    @Override
    public JSONArray onArrayParse(JSONObject request, String parentPath, String name) throws Exception {
        //不能允许GETS，否则会被通过"[]":{"@role":"ADMIN"},"Table":{},"tag":"Table"绕过权限并能批量查询
        if (!RequestMethod.isGetMethod(requestMethod, false)) {
            throw new UnsupportedOperationException("key[]:{}只支持GET方法！不允许传 " + name + ":{} ！");
        }
        if (request == null || request.isEmpty()) {
            return null;
        }
        String path = ParserHelper.getAbsPath(parentPath, name);

        //不能改变，因为后面可能继续用到，导致1以上都改变 []:{0:{Comment[]:{0:{Comment:{}},1:{...},...}},1:{...},...}
        final Integer query = request.getIntValue(JsonApiRequest.KEY_QUERY);
        final Integer count = request.getIntValue(JsonApiRequest.KEY_COUNT);
        final Integer page = request.getIntValue(JsonApiRequest.KEY_PAGE);
        final String join = request.getString(JsonApiRequest.KEY_JOIN);
        request.remove(JsonApiRequest.KEY_QUERY);
        request.remove(JsonApiRequest.KEY_COUNT);
        request.remove(JsonApiRequest.KEY_PAGE);
        request.remove(JsonApiRequest.KEY_JOIN);

        if (request.isEmpty()) {
            return null;
        }


        //不用total限制数量了，只用中断机制，total只在query = 1,2的时候才获取
        Integer max = getMaxQueryCount();
        Integer size = count <= 0 || count > max ? max : count;


        //key[]:{Table:{}}中key equals Table时 提取Table
        Integer index = name == null ? -1 : name.lastIndexOf("[]");
        String childPath = index <= 0 ? null : Pair.parseEntry(name.substring(0, index), true).getKey();

        //判断第一个key，即Table是否存在，如果存在就提取
        String[] childKeys = StringUtil.split(childPath, "-", false);
        if (childKeys == null || childKeys.length <= 0 || request.containsKey(childKeys[0]) == false) {
            childKeys = null;
        }

        JSONArray response = new JSONArray();
        SqlConfig config = createSQLConfig()
                .setMethod(requestMethod)
                .setCount(size)
                .setPage(page)
                .setQuery(query)
                .setJoinList(onJoinParse(join, request));

        JSONObject parent;
        //生成size个
        for (Integer i = 0; i < size; i++) {
            parent = onObjectParse(request, path, "" + i, config.setType(SqlConfig.TYPE_ITEM).setPosition(i));
            if (parent == null || parent.isEmpty()) {
                break;
            }
            //key[]:{Table:{}}中key equals Table时 提取Table
            response.add(ParserHelper.getValue(parent, childKeys));
        }

        Object fo = childKeys == null || response.isEmpty() ? null : response.get(0);
        if (fo instanceof Boolean || fo instanceof Number || fo instanceof String) {
            putQueryResult(path, response);
        }


        //后面还可能用到，要还原
        request.put(JsonApiRequest.KEY_QUERY, query);
        request.put(JsonApiRequest.KEY_COUNT, count);
        request.put(JsonApiRequest.KEY_PAGE, page);
        request.put(JsonApiRequest.KEY_JOIN, join);

        return response;
    }

    /**
     * 多表同时筛选
     *
     * @param join    "&/User/id@,</User[]/User/id{}@,</[]/Comment/momentId@"
     * @param request
     * @return
     * @throws Exception
     */
    private List<Join> onJoinParse(String join, JSONObject request) throws Exception {
        String[] sArr = request == null || request.isEmpty() ? null : StringUtil.split(join);
        if (sArr == null || sArr.length <= 0) {
            return null;
        }

        List<Join> joinList = new ArrayList<>();


        JSONObject tableObj;
        String targetPath;

        JSONObject targetObj;
        String targetTable;
        String targetKey;

        String path;

        //		List<String> onList = new ArrayList<>();
        for (Integer i = 0; i < sArr.length; i++) {//User/id@
            //分割 /Table/key
            path = "" + sArr[i];

            Integer index = path.indexOf("/");
            if (index < 0) {
                throw new IllegalArgumentException(JsonApiRequest.KEY_JOIN + ":value 中value不合法！"
                        + "必须为 &/Table0/key0,</Table1/key1,... 这种形式！");
            }
            String joinType = path.substring(0, index); //& | ! < > ( ) <> () *
//			if (StringUtil.isEmpty(joinType, true)) {
//				joinType = "|"; // FULL JOIN
//			}
            path = path.substring(index + 1);

            index = path.indexOf("/");
            String tableKey = index < 0 ? null : path.substring(0, index); //User:owner
            String table = Pair.parseEntry(tableKey, true).getKey(); //User
            String key = StringUtil.isEmpty(table, true) ? null : path.substring(index + 1);//id@
            if (StringUtil.isEmpty(key, true)) {
                throw new IllegalArgumentException(JsonApiRequest.KEY_JOIN + ":value 中value不合法！"
                        + "必须为 &/Table0/key0,</Table1/key1,... 这种形式！");
            }

            //取出Table对应的JSONObject，及内部引用赋值 key:value
            tableObj = request.getJSONObject(tableKey);
            targetPath = tableObj == null ? null : tableObj.getString(key);
            if (StringUtil.isEmpty(targetPath, true)) {
                throw new IllegalArgumentException("/" + path + ":value 中value必须为引用赋值的路径 '/targetTable/targetKey' ！");
            }

            //取出引用赋值路径targetPath对应的Table和key
            index = targetPath.lastIndexOf("/");
            targetKey = index < 0 ? null : targetPath.substring(index + 1);
            if (StringUtil.isEmpty(targetKey, true)) {
                throw new IllegalArgumentException("/" + path + ":'/targetTable/targetKey' 中targetKey不能为空！");
            }

            targetPath = targetPath.substring(0, index);
            index = targetPath.lastIndexOf("/");
            targetTable = index < 0 ? targetPath : targetPath.substring(index + 1);


            //对引用的JSONObject添加条件
            targetObj = request.getJSONObject(targetTable);
            if (targetObj == null) {
                throw new IllegalArgumentException(targetTable + "." + targetKey
                        + ":'/targetTable/targetKey' 中路径对应的对象不存在！");
            }

            tableObj.put(key, tableObj.remove(key)); //保证和SQLExcecutor缓存的Config里where顺序一致，生成的SQL也就一致

            Join j = new Join();
            j.setPath(path);
            j.setOriginKey(key);
            j.setOriginValue(targetPath);
            j.setJoinType(joinType);
            j.setName(table);
            j.setTargetName(targetTable);
            j.setTargetKey(targetKey);
            j.setKeyAndType(key);
            j.setTable(getJoinObject(table, tableObj, key));

            joinList.add(j);

            //			onList.add(table + "." + key + " = " + targetTable + "." + targetKey); // ON User.id = Moment.userId

        }


        //拼接多个 SqlConfig 的SQL语句，然后执行，再把结果分别缓存(Moment, User等)到 SqlExecutor 的 cacheMap
        //		AbstractSQLConfig config0 = null;
        //		String sql = "SELECT " + config0.getColumnString() + " FROM " + config0.getTable() + " INNER JOIN " + targetTable + " ON "
        //				+ onList.get(0) + config0.getGroupString() + config0.getHavingString() + config0.getOrderString();


        return joinList;
    }


    private static final List<String> JOIN_COPY_KEY_LIST;

    static {
        JOIN_COPY_KEY_LIST = Lists.newArrayList();
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_DATABASE);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_SCHEMA);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_COLUMN);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_COMBINE);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_GROUP);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_HAVING);
        JOIN_COPY_KEY_LIST.add(JsonApiRequest.KEY_ORDER);
    }

    /**
     * 取指定json对象的id集合
     */
    private JSONObject getJoinObject(String table, JSONObject obj, String key) {
        if (obj == null || obj.isEmpty()) {
            return null;
        }
        if (StringUtil.isEmpty(key, true)) {
            return null;
        }

        //取出所有join条件
        JSONObject requestObj = new JSONObject(true);
        Set<String> set = new LinkedHashSet<>(obj.keySet());
        for (String k : set) {
            if (StringUtil.isEmpty(k, true)) {
                continue;
            }

            if (k.startsWith("@")) {
                if (JOIN_COPY_KEY_LIST.contains(k)) {
                    requestObj.put(k, obj.get(k));
                }
            } else {
                if (k.endsWith("@")) {
                    if (k.equals(key)) {
                        continue;
                    }
                    throw new UnsupportedOperationException(table + "." + k + " 不合法！" + JsonApiRequest.KEY_JOIN
                            + " 关联的Table中只能有1个 key@:value ！");
                }

                if (k.contains("()") == false) {
                    //					requestObj.put(k, obj.remove(k)); //remove是为了避免重复查询副表
                    requestObj.put(k, obj.get(k));
                }
            }
        }


        return requestObj;
    }

    @Override
    public Integer getMaxQueryCount() {
        return MAX_QUERY_COUNT;
    }

    @Override
    public Integer getMaxUpdateCount() {
        return MAX_UPDATE_COUNT;
    }




    //依赖引用关系 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    /**
     * 将已获取完成的object的内容替换requestObject里对应的值
     *
     * @param path   object的路径
     * @param result 需要被关联的object
     */
    @Override
    public synchronized void putQueryResult(String path, Object result) {
        queryResultMap.put(path, result);
    }

    /**
     * 根据路径获取值
     *
     * @param valuePath
     * @return parent == null ? valuePath : parent.get(keys[keys.length - 1])
     */
    @Override
    public Object getValueByPath(String valuePath) {
        if (StringUtil.isEmpty(valuePath, true)) {
            return null;
        }
        Object target = queryResultMap.get(valuePath);
        if (target != null) {
            return target;
        }

        //取出key被valuePath包含的result，再从里面获取key对应的value
        Set<String> set = queryResultMap.keySet();
        JSONObject parent = null;
        String[] keys = null;
        for (String path : set) {
            if (valuePath.startsWith(path + "/")) {
                try {
                    parent = (JSONObject) queryResultMap.get(path);
                } catch (Exception e) {
                    parent = null;
                }
                if (parent != null) {
                    keys = StringUtil.splitPath(valuePath.substring(path.length()));
                }
                break;
            }
        }

        //逐层到达targetKey的直接容器JSONObject parent
        if (keys != null && keys.length > 1) {
            //一步一步到达指定位置parentPath
            for (Integer i = 0; i < keys.length - 1; i++) {
                //不存在或路径错误(中间的key对应value不是JSONObject)
                if (parent == null) {
                    break;
                }
                parent = ParserHelper.getJSONObject(parent, keys[i]);
            }
        }

        if (parent != null) {
            //值为null应该报错NotExistExeption，一般都是id关联，不可为null，否则可能绕过安全机制
            target = parent.get(keys[keys.length - 1]);
            if (target != null) {
                return target;
            }
        }


        //从requestObject中取值
        target = ParserHelper.getValue(requestObject, StringUtil.splitPath(valuePath));
        if (target != null) {
            return target;
        }

        return valuePath;
    }

    //依赖引用关系 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    /**
     * 获取数据库返回的String
     *
     * @param config
     * @return
     * @throws Exception
     */
    @Override
    public synchronized JSONObject executeSQL(SqlConfig config) throws Exception {

        if (!noVerifyRole) {
            if (config.getRole() == null) {
                if (globalRole != null) {
                    config.setRole(globalRole);
                } else {
                    config.setRole(getVisitor().getId() == null ? RequestRole.UNKNOWN : RequestRole.LOGIN);
                }
            }
            verifier.verify(config);
        }

        if (config.getDatabase() == null && globleDatabase != null) {
            config.setDatabase(globleDatabase);
        }

        return parseCorrectResponse(config.getTable(), sqlExecutor.execute(config));
    }


}
