package apijson.demo.server.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpSession;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.StandardVerifier;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import apijson.demo.server.model.BaseModel;
import com.zhangls.apijson.annotation.NotNull;
import com.zhangls.apijson.base.JsonApi;
import com.zhangls.apijson.base.JsonApiObject;
import com.zhangls.apijson.base.JsonApiRequest;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.base.model.RequestMethod;
import com.zhangls.apijson.base.model.RequestRole;
import com.zhangls.apijson.base.service.impl.RemoteFunction;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;


/**可远程调用的函数类
 * @author Lemon
 */
@Slf4j
public class DemoFunction extends RemoteFunction {
	private static final String TAG = "DemoFunction";

	private final RequestMethod method;
	private final HttpSession session;
	public DemoFunction(RequestMethod method, HttpSession session) {
		this.method = method;
		this.session = session;
	}

	public static void test() throws Exception {
		int i0 = 1, i1 = -2;
		JSONObject request = new JSONObject();
		request.put("id", 10);
		request.put("i0", i0);
		request.put("i1", i1);
		JSONArray arr = new JSONArray();
		arr.add(new JSONObject());
		request.put("arr", arr);

		JSONArray array = new JSONArray();
		array.add(1);
		array.add(2);
		array.add(4);
		array.add(10);
		request.put("array", array);

		request.put("position", 1);
		request.put("@position", 0);

		request.put("key", "key");
		JSONObject object = new JSONObject();
		object.put("key", true);
		request.put("object", object);


		log.info(TAG, "plus(1,-2) = " + new DemoFunction(null, null).invoke("plus(i0,i1)", request));
		log.info(TAG, "count([1,2,4,10]) = " + new DemoFunction(null, null).invoke("countArray(array)", request));
		log.info(TAG, "isContain([1,2,4,10], 10) = " + new DemoFunction(null, null).invoke("isContain(array,id)", request));
		log.info(TAG, "getFromArray([1,2,4,10], 0) = " + new DemoFunction(null, null).invoke("getFromArray(array,@position)", request));
		log.info(TAG, "getFromObject({key:true}, key) = " + new DemoFunction(null, null).invoke("getFromObject(object,key)", request));

		forceUseable();
	}

	/**测试可用性，不catch，不可用直接抛异常，强制在Function表修改为demo为可用的
	 */
	public static void forceUseable() { // throws UnsupportedOperationException {
		//查出所有的 Function 并校验是否已在应用层代码实现

		JSONObject request = new JSONObject();

		//Function[]<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest functionItem = new JsonApiRequest();

		//Function<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest function = new JsonApiRequest();
		functionItem.put("Function", function);
		//Function>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		request.putAll(functionItem.toArray(0, 0, "Function"));
		//Function[]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		JSONObject response = new StandardParser(RequestMethod.GET, true).parseResponse(request);
		if (JsonApiResponse.isSuccess(response) == false) {
			log.error(TAG, "\n\n\n\n\n !!!! 查询远程函数异常 !!!\n" + response.getString(JsonApiResponse.KEY_MSG) + "\n\n\n\n\n");
			return;
		}

		JSONArray fl = response.getJSONArray("Function[]");
		if (fl == null || fl.isEmpty()) {
			log.debug(TAG, "没有可用的远程函数");
			return;
		}

		JSONObject fi;
		for (int i = 0; i < fl.size(); i++) {
			fi = fl.getJSONObject(i);
			if (fi == null) {
				continue;
			}

			JSONObject demo = JsonApi.parseObject(fi.getString("demo"));
			if (demo == null) {
				exitWithError("字段 demo 的值必须为合法且非null的 JSONObejct 字符串！");
			}
			if (demo.containsKey("result()") == false) {
				demo.put("result()", getFunctionCall(fi.getString("name"), fi.getString("arguments")));
			}
			demo.put(JsonApiRequest.KEY_COLUMN, "id,name,arguments,demo");

			JSONObject r = new StandardParser(RequestMethod.GET, true).parseResponse(demo);
			if (JsonApiResponse.isSuccess(r) == false) {
				//				throw new UnsupportedOperationException("远程函数测试未通过！请修改 Function 表里的 demo！原因：" + JSONResponse.getMsg(r));
				exitWithError(JsonApiResponse.getMsg(r));
			}
		}
	}


	private static void exitWithError(String msg) {
		log.error(TAG, "\n远程函数文档测试未通过！\n请新增 demo 里的函数，或修改 Function 表里的 demo 为已有的函数示例！\n保证前端看到的远程函数文档是正确的！！！\n\n原因：\n" + msg);
		System.exit(1);
	}

	/**反射调用
	 * @param function 例如get(object,key)，参数只允许引用，不能直接传值
	 * @param json 不作为第一个参数，就不能远程调用invoke，避免死循环
	 * @return
	 */
	public Object invoke(String function, JSONObject json) throws Exception {
		return invoke(this, json, function);
	}



	/**
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object verifyIdList(@NotNull JSONObject request, @NotNull String idList) throws Exception {
		Object obj = request.get(idList);
		if (obj instanceof Collection == false) {
			throw new IllegalArgumentException(idList + " 不符合 Array 类型! 结构必须是 [] ！");
		}
		JSONArray array = (JSONArray) obj;
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i) instanceof Long == false && array.get(i) instanceof Integer == false) {
					throw new IllegalArgumentException(idList + " 内字符 " + array.getString(i) + " 不符合 Long 类型!");
				}
			}
		}
		return null;
	}


	/**
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object verifyURLList(@NotNull JSONObject request, @NotNull String urlList) throws Exception {
		Object obj = request.get(urlList);
		if (obj instanceof Collection == false) {
			throw new IllegalArgumentException(urlList + " 不符合 Array 类型! 结构必须是 [] ！");
		}
		JSONArray array = (JSONArray) obj;
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (StringUtil.isUrl(array.getString(i)) == false) {
					throw new IllegalArgumentException(urlList + " 内字符 " + array.getString(i) + " 不符合 URL 格式!");
				}
			}
		}
		return null;
	}


	/**
	 * @param rq
	 * @param momentId
	 * @return
	 * @throws Exception
	 */
	public int deleteCommentOfMoment(@NotNull JSONObject rq, @NotNull String momentId) throws Exception {
		if (method != RequestMethod.DELETE) {
			throw new UnsupportedOperationException("远程函数 deleteCommentOfMoment 只支持 DELETE 方法！");
		}

		long mid = rq.getLongValue(momentId);
		if (mid <= 0 || rq.getIntValue(JsonApiResponse.KEY_COUNT) <= 0) {
			return 0;
		}

		JsonApiRequest request = new JsonApiRequest();

		//Comment<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest comment = new JsonApiRequest();
		comment.put("momentId", mid);

		request.put("Comment", comment);
		//Comment>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		JSONObject rp = new StandardParser(RequestMethod.DELETE).setNoVerify(true).parseResponse(request);

		JSONObject c = rp.getJSONObject("Comment");
		return c == null ? 0 : c.getIntValue(JsonApiResponse.KEY_COUNT);
	}


	/**删除评论的子评论
	 * @param rq
	 * @param toId
	 * @return
	 */
	public int deleteChildComment(@NotNull JSONObject rq, @NotNull String toId) throws Exception {
		if (method != RequestMethod.DELETE) { //TODO 如果这样的判断太多，可以把 DemoFunction 分成对应不同 RequestMethod 的 GetFunciton 等，创建时根据 method 判断用哪个
			throw new UnsupportedOperationException("远程函数 deleteChildComment 只支持 DELETE 方法！");
		}

		long tid = rq.getLongValue(toId);
		if (tid <= 0 || rq.getIntValue(JsonApiResponse.KEY_COUNT) <= 0) {
			return 0;
		}

		//递归获取到全部子评论id

		JsonApiRequest request = new JsonApiRequest();

		//Comment<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest comment = new JsonApiRequest();
		comment.put("id{}", getChildCommentIdList(tid));

		request.put("Comment", comment);
		//Comment>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		JSONObject rp = new StandardParser(RequestMethod.DELETE).setNoVerify(true).parseResponse(request);

		JSONObject c = rp.getJSONObject("Comment");
		return c == null ? 0 : c.getIntValue(JsonApiResponse.KEY_COUNT);
	}


	private JSONArray getChildCommentIdList(long tid) {

		JSONArray arr = new JSONArray();

		JsonApiRequest request = new JsonApiRequest();

		//Comment-id[]<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest idItem = new JsonApiRequest();

		//Comment<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		JsonApiRequest comment = new JsonApiRequest();
		comment.put("toId", tid);
		comment.setColumn("id");
		idItem.put("Comment", comment);
		//Comment>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		request.putAll(idItem.toArray(0, 0, "Comment-id"));
		//Comment-id[]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

		JSONObject rp = new StandardParser().setNoVerify(true).parseResponse(request);

		JSONArray a = rp.getJSONArray("Comment-id[]");
		if (a != null) {
			arr.addAll(a);

			JSONArray a2;
			for (int i = 0; i < a.size(); i++) {

				a2 = getChildCommentIdList(a.getLongValue(i));
				if (a2 != null) {
					arr.addAll(a2);
				}
			}
		}

		return arr;
	}

	/**获取远程函数的demo，如果没有就自动补全
	 * @param request
	 * @return
	 */
	public JSONObject getFunctionDemo(@NotNull JSONObject request) {
		JSONObject demo = JsonApi.parseObject(request.getString("demo"));
		if (demo == null) {
			exitWithError("字段 demo 的值必须为合法且非null的 JSONObejct 字符串！");
		}
		if (demo.containsKey("result()") == false) {
			demo.put("result()", getFunctionCall(request.getString("name"), request.getString("arguments")));
		}
		return demo;
	}

	/**获取远程函数的demo，如果没有就自动补全
	 * @param request
	 * @return
	 */
	public String getFunctionDetail(@NotNull JSONObject request) {
		return getFunctionCall(request.getString("name"), request.getString("arguments"))
				+ ": " + StringUtil.getTrimedString(request.getString("detail"));
	}
	/**获取函数调用代码
	 * @param name
	 * @param arguments
	 * @return
	 */
	private static String getFunctionCall(String name, String arguments) {
		return name + "(" + arguments + ")";
	}

	/**TODO 仅用来测试 "key-()":"getIdList()" 和 "key()":"getIdList()"
	 * @param request
	 * @return JSONArray 只能用JSONArray，用long[]会在SQLConfig解析崩溃
	 * @throws Exception
	 */
	public JSONArray getIdList(@NotNull JSONObject request) throws Exception {
		return new JSONArray(new ArrayList<Object>(Arrays.asList(12, 15, 301, 82001, 82002, 38710)));
	}


	/**TODO 仅用来测试 "key-()":"verifyAccess()"
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object verifyAccess(@NotNull JSONObject request) throws Exception {
		long userId = request.getLongValue(JsonApiObject.KEY_USER_ID);
		RequestRole role = RequestRole.get(request.getString(JsonApiObject.KEY_ROLE));
		if (role == RequestRole.OWNER && userId != StandardVerifier.getVisitorId(session)) {
			throw new IllegalAccessException("登录用户与角色OWNER不匹配！");
		}
		return null;
	}




	public double plus(@NotNull JSONObject request, String i0, String i1) {
		return request.getDoubleValue(i0) + request.getDoubleValue(i1);
	}
	public double minus(@NotNull JSONObject request, String i0, String i1) {
		return request.getDoubleValue(i0) - request.getDoubleValue(i1);
	}
	public double multiply(@NotNull JSONObject request, String i0, String i1) {
		return request.getDoubleValue(i0) * request.getDoubleValue(i1);
	}
	public double divide(@NotNull JSONObject request, String i0, String i1) {
		return request.getDoubleValue(i0) / request.getDoubleValue(i1);
	}

	//判断是否为空 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否为空
	 * @param request
	 * @param array
	 * @return
	 */
	public boolean isArrayEmpty(@NotNull JSONObject request, String array) {
		return BaseModel.isEmpty(request.getJSONArray(array));
	}
	/**判断object是否为空
	 * @param request
	 * @param object
	 * @return
	 */
	public boolean isObjectEmpty(@NotNull JSONObject request, String object) {
		return BaseModel.isEmpty(request.getJSONObject(object));
	}
	//判断是否为空 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//判断是否为包含 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**判断array是否包含value
	 * @param request
	 * @param array
	 * @param value
	 * @return
	 */
	public boolean isContain(@NotNull JSONObject request, String array, String value) {
		//解决isContain((List<Long>) [82001,...], (Integer) 82001) == false及类似问题, list元素可能是从数据库查到的bigint类型的值
		//		return BaseModel.isContain(request.getJSONArray(array), request.get(value));

		//不用准确的的 request.getString(value).getClass() ，因为Long值转Integer崩溃，而且转成一种类型本身就和字符串对比效果一样了。
		List<String> list = com.alibaba.fastjson.JSON.parseArray(request.getString(array), String.class);
		return list != null && list.contains(request.getString(value));
	}
	/**判断object是否包含key
	 * @param request
	 * @param object
	 * @param key
	 * @return
	 */
	public boolean isContainKey(@NotNull JSONObject request, String object, String key) {
		return BaseModel.isContainKey(request.getJSONObject(object), request.getString(key));
	}
	/**判断object是否包含value
	 * @param request
	 * @param object
	 * @param value
	 * @return
	 */
	public boolean isContainValue(@NotNull JSONObject request, String object, String value) {
		return BaseModel.isContainValue(request.getJSONObject(object), request.get(value));
	}
	//判断是否为包含 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//获取集合长度 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取数量
	 * @param request
	 * @param array
	 * @return
	 */
	public int countArray(@NotNull JSONObject request, String array) {
		return BaseModel.count(request.getJSONArray(array));
	}
	/**获取数量
	 * @param request
	 * @param object
	 * @return
	 */
	public int countObject(@NotNull JSONObject request, String object) {
		return BaseModel.count(request.getJSONObject(object));
	}
	//获取集合长度 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>


	//根据键获取值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取
	 ** @param request
	 * @param array
	 * @param position 支持直接传数字，例如 getFromArray(array,0) ；或者引用当前对象的值，例如 "@position": 0, "result()": "getFromArray(array,@position)"
	 * @return
	 */
	public Object getFromArray(@NotNull JSONObject request, String array, String position) {
		int p;
		try {
			p = Integer.parseInt(position);
		} catch (Exception e) {
			p = request.getIntValue(position);
		}
		return BaseModel.get(request.getJSONArray(array), p);
	}
	/**获取
	 * @param request
	 * @param object
	 * @param key
	 * @return
	 */
	public Object getFromObject(@NotNull JSONObject request, String object, String key) {
		return BaseModel.get(request.getJSONObject(object), request.getString(key));
	}
	//根据键获取值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	//根据键移除值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**移除
	 ** @param request
	 * @param position 支持直接传数字，例如 getFromArray(array,0) ；或者引用当前对象的值，例如 "@position": 0, "result()": "getFromArray(array,@position)"
	 * @return
	 */
	public Object removeIndex(@NotNull JSONObject request, String position) {
		int p;
		try {
			p = Integer.parseInt(position);
		} catch (Exception e) {
			p = request.getIntValue(position);
		}
		request.remove(p);
		return null;
	}
	/**移除
	 * @param request
	 * @param key
	 * @return
	 */
	public Object removeKey(@NotNull JSONObject request, String key) {
		request.remove(key);
		return null;
	}
	//根据键获取值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>



	//获取非基本类型对应基本类型的非空值 <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	/**获取非空值
	 * @param request
	 * @param value
	 * @return
	 */
	public boolean booleanValue(@NotNull JSONObject request, String value) {
		return request.getBooleanValue(value);
	}
	/**获取非空值
	 * @param request
	 * @param value
	 * @return
	 */
	public int intValue(@NotNull JSONObject request, String value) {
		return request.getIntValue(value);
	}
	/**获取非空值
	 * @param request
	 * @param value
	 * @return
	 */
	public long longValue(@NotNull JSONObject request, String value) {
		return request.getLongValue(value);
	}
	/**获取非空值
	 * @param request
	 * @param value
	 * @return
	 */
	public float floatValue(@NotNull JSONObject request, String value) {
		return request.getFloatValue(value);
	}
	/**获取非空值
	 * @param request
	 * @param value
	 * @return
	 */
	public double doubleValue(@NotNull JSONObject request, String value) {
		return request.getDoubleValue(value);
	}
	//获取非基本类型对应基本类型的非空值 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>

	/**获取value，当value为null时获取defaultValue
	 * @param request
	 * @param value
	 * @param defaultValue
	 * @return v == null ? request.get(defaultValue) : v
	 */
	public Object getWithDefault(@NotNull JSONObject request, String value, String defaultValue) {
		Object v = request.get(value);
		return v == null ? request.get(defaultValue) : v;
	}



}