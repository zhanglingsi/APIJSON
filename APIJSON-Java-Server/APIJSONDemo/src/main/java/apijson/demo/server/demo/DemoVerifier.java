package apijson.demo.server.demo;

import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.*;
import com.zhangls.apijson.annotation.MethodAccess;
import com.zhangls.apijson.base.service.Parser;
import com.zhangls.apijson.base.service.impl.AbstractVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by zhangls on 2019/1/14.
 * @author zhangls
 */
@Slf4j
@Component
public class DemoVerifier extends AbstractVerifier<Long> {

    static {
        ACCESS_MAP.put(User.class.getSimpleName(), getAccessMap(User.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Privacy.class.getSimpleName(), getAccessMap(Privacy.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Moment.class.getSimpleName(), getAccessMap(Moment.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Comment.class.getSimpleName(), getAccessMap(Comment.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Verify.class.getSimpleName(), getAccessMap(Verify.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Login.class.getSimpleName(), getAccessMap(Login.class.getAnnotation(MethodAccess.class)));
        ACCESS_MAP.put(Product.class.getSimpleName(), getAccessMap(Product.class.getAnnotation(MethodAccess.class)));
    }

    @Override
    public Parser<Long> createParser() {
        DemoParser parser = new DemoParser();
        parser.setVisitor(visitor);
        return parser;
    }

    /**
     * "User"
     * @return
     */
    @Override
    public String getVisitorKey() {
        return UtilConstants.Public.USER_;
    }

    /**
     * "userId"
     * @return
     */
    @Override
    public String getVisitorIdKey() {
        return UtilConstants.Login.USER_ID;
    }

    /**
     *
     * @param table
     * @return
     */
    @Override
    public String getVisitorIdKey(String table) {
        return UtilConstants.Public.USER_.equals(table) || UtilConstants.Public.PRIVACY_.equals(table) ? UtilConstants.Reset.ID : getVisitorIdKey();
    }
}
