package apijson.demo.server.model;

import com.zhangls.apijson.annotation.MethodAccess;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.CIRCLE;
import static com.zhangls.apijson.base.model.RequestRole.CONTACT;
import static com.zhangls.apijson.base.model.RequestRole.LOGIN;
import static com.zhangls.apijson.base.model.RequestRole.OWNER;

;

/**
 * 动态
 * TODO 还要细分，LOGIN,CONTACT只允许修改praiseUserIdList。数据库加role没用，应该将praiseUserIdList移到Praise表
 * @author Lemon
 */
@MethodAccess(
        PUT = {LOGIN, CONTACT, CIRCLE, OWNER, ADMIN}
)
public class Moment {
}