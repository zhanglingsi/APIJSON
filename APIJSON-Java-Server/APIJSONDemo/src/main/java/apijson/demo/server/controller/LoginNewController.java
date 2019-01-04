package apijson.demo.server.controller;


import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.service.LoginNewService;
import apijson.demo.server.utils.JsonParseUtils;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonResponse;
import com.zhangls.apijson.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;


/**
 * Controller层的职责
 * 1. 验证收到的请求  异常：返回异常结果，和异常码， 必须的请求参数是否为空，格式是否正确，解析是否正常等
 * 2. 校验请求正常（表示该请求可以被处理）：发送给Service层调用 并返回结果
 * 3. 返回结果处理（将用户信息添加到session，操作cookie等）
 * <p>
 * service层的职责
 * 1. 业务逻辑校验  注册时 输入的两次密码不一致  登陆时用户名不存在 等业务逻辑校验
 * 2. 处理业务，包括事物原子性等 调用DAO层 访问数据库  并缓存查询结果  key：sql语句  value：ResultSet
 * 3. 封装返回结果
 * <p>
 * DAO层的职责
 * 1. 查询参数校验，查询对象非空校验，插入表数据的唯一校验，以及非空校验等
 * 2. 访问数据库，从数据源连接池获取Connection对象，并创建PreparedStatement，设置sql语句，并设置所需参数。
 * 3. 执行SQL，并对返回的ResultSet进行转换，封装成对象，返回给service层
 * <p>
 * 登陆
 *
 * @author zhangls
 * @see <pre>
 * {
 * "type": 0,  //登录方式，非必须  0-密码 1-验证码
 * "phone": "13000082001",
 * "password": "1234567",
 * "version": 1 //全局版本号，非必须
 * }
 * </pre>
 */
@Slf4j
@RestController
public class LoginNewController {

    @Autowired
    private LoginNewService service;

    /**
     * 登陆
     *
     * @param reqStr  【请求字符串】
     * @param session 【登陆会话信息】
     * @return
     */
    @PostMapping("loginNew")
    public JSONObject login(@RequestBody String reqStr, HttpSession session) {
        log.info("#################登陆开始##########################################################################");
        // 1. 打印请求字符串
        log.info("【登陆请求字符串】：{}", reqStr);

        // 2. 验证请求字符串
        JSONObject reqParse = null;
        try {
            reqParse = JsonParseUtils.parseRequest(reqStr);
            String phone = reqParse.getString(UtilConstants.Login.PHONE);
            String password = reqParse.getString(UtilConstants.Login.PASS_WORD);

            if (!StringUtil.isPhone(phone)) {
                throw new IllegalArgumentException("手机号不合法！");
            }

            // 密码登陆
            if (reqParse.getInteger(UtilConstants.Login.TYPE).equals(UtilConstants.Login.LOGIN_TYPE_PASSWORD)) {
                if (!StringUtil.isPassword(password)) {
                    throw new IllegalArgumentException("密码不合法！");
                }
            } else {
                if (!StringUtil.isVerify(password)) {
                    throw new IllegalArgumentException("验证码不合法！");
                }
            }
        } catch (Exception e) {
            return JsonParseUtils.extendErrorResult(reqParse, e);
        }

        // 3. 请求Service返回结果(JSONObject对象)
        JsonResponse response = service.loginNewJson(reqParse);

        // 4. 设置登陆会话Session
        //用户id
//        session.setAttribute(UtilConstants.Login.USER_ID, userId);
//        //登录方式
//        session.setAttribute(UtilConstants.Login.TYPE, isPassword ? UtilConstants.Login.LOGIN_TYPE_PASSWORD : UtilConstants.Login.LOGIN_TYPE_VERIFY);
//        //用户
//        session.setAttribute(UtilConstants.Public.USER_, user);
//        //用户隐私信息
//        session.setAttribute(UtilConstants.Public.PRIVACY_, privacy);
//        //记住登录
//        session.setAttribute(UtilConstants.Login.REMEMBER, remember);
//        //全局默认版本号
//        session.setAttribute(UtilConstants.Login.VERSION, version);
//        //全局默认格式化配置
//        session.setAttribute(UtilConstants.Login.FORMAT, format);
//
//        //设置session过期时间
//        session.setMaxInactiveInterval(60 * 60 * 24 * (remember ? 7 : 1));
//
//        response.put(UtilConstants.Login.REMEMBER, remember);


        log.info("#################登陆结束##########################################################################");
        return response;
    }
}
