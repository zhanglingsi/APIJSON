package apijson.demo.server.model;

import static zuo.biao.apijson.RequestRole.ADMIN;
import static zuo.biao.apijson.RequestRole.CIRCLE;
import static zuo.biao.apijson.RequestRole.CONTACT;
import static zuo.biao.apijson.RequestRole.LOGIN;
import static zuo.biao.apijson.RequestRole.OWNER;

import zuo.biao.apijson.MethodAccess;

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