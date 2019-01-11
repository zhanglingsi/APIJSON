package apijson.demo.server.common;

import apijson.demo.server.model.*;
import com.zhangls.apijson.annotation.MethodAccess;
import com.zhangls.apijson.base.service.Visitor;
import com.zhangls.apijson.base.service.impl.AbstractVerifier;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;

/**
 * Created by zhangls on 2019/1/2.
 * @author zhangls
 */
public class StandardVerifier extends AbstractVerifier<Long> {

    static {
        ACCESS_MAP.put(User.class.getSimpleName(), getAccessMap(User.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Privacy.class.getSimpleName(), getAccessMap(Privacy.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Moment.class.getSimpleName(), getAccessMap(Moment.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Comment.class.getSimpleName(), getAccessMap(Comment.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Verify.class.getSimpleName(), getAccessMap(Verify.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Login.class.getSimpleName(), getAccessMap(Login.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Product.class.getSimpleName(), getAccessMap(Product.class.getAnnotation(MethodAccess.class)));
    }


    @NotNull
    @Override
    public StandardParser createParser() {
        StandardParser parser = new StandardParser();
        parser.setVisitor(visitor);
        return parser;
    }

    @Override
    public String getVisitorKey() {
        return UtilConstants.Public.USER_;
    }

    @Override
    public String getVisitorIdKey() {
        return UtilConstants.Login.USER_ID;
    }

    /**
     * 验证访问Key
     *
     * 如果表名 是User 或者 Privacy 则返回 "id"  否则返回 "userId"
     *
     * @param table
     * @return
     */
    @Override
    public String getVisitorIdKey(String table) {
        return UtilConstants.Public.USER_.equals(table) || UtilConstants.Public.PRIVACY_.equals(table) ? UtilConstants.Reset.ID : getVisitorIdKey();
    }

    /**
     * 登录校验
     *
     * @param session
     * @throws Exception
     * @author
     * @modifier Lemon
     */
    public static void verifyLogin(HttpSession session) throws Exception {
        new StandardVerifier().setVisitor(getVisitor(session)).verifyLogin();
    }


    /**
     * 获取来访用户的id
     *
     * @param session
     * @return
     * @author Lemon
     */
    public static long getVisitorId(HttpSession session) {
        if (session == null) {
            return 0;
        }
        Long id = (Long) session.getAttribute(UtilConstants.Login.USER_ID);
        if (id == null) {
            Visitor<Long> v = getVisitor(session);
            id = v == null ? 0 : value(v.getId());
            session.setAttribute(UtilConstants.Login.USER_ID, id);
        }
        return value(id);
    }

    /**
     * 获取来访用户
     *
     * @param session
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Visitor<Long> getVisitor(HttpSession session) {
        return session == null ? null : (Visitor<Long>) session.getAttribute(UtilConstants.Public.USER_);
    }

    public static long value(Long v) {
        return v == null ? 0 : v;
    }


}
