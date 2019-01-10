package apijson.demo.server.controller;

import apijson.demo.server.common.StandardParser;
import apijson.demo.server.common.StandardVerifier;
import apijson.demo.server.model.Privacy;
import apijson.demo.server.model.User;
import apijson.demo.server.model.Verify;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiResponse;
import com.zhangls.apijson.utils.StringUtil;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

/**
 * Created by zhangls on 2019/1/2.
 *
 * @author zhangls
 */
public class LogoutController {

    public static final String USER_;
    public static final String PRIVACY_;
    public static final String VERIFY_;

    private static final String TAG = "Controller";

    static {
        USER_ = User.class.getSimpleName();
        PRIVACY_ = Privacy.class.getSimpleName();
        VERIFY_ = Verify.class.getSimpleName();
    }

    public static final String COUNT = JsonApiResponse.KEY_COUNT;

    public static final String ID = "id";

    /**
     * 退出登录，清空session
     *
     * @param session
     * @return
     */
    @PostMapping("logout")
    public JSONObject logout(HttpSession session) {
        long userId;
        try {
            //必须在session.invalidate();前！
            userId = StandardVerifier.getVisitorId(session);
//            Log.d(TAG, "logout  userId = " + userId + "; session.getId() = " + (session == null ? null : session.getId()));
            session.invalidate();
        } catch (Exception e) {
            return StandardParser.newErrorResult(e);
        }

        JSONObject result = StandardParser.newSuccessResult();
        JSONObject user = StandardParser.newSuccessResult();
        user.put(ID, userId);
        user.put(COUNT, 1);
        result.put(StringUtil.firstCase(USER_), user);

        return result;
    }
}
