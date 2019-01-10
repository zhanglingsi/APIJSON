package apijson.demo.server.model;

import com.zhangls.apijson.annotation.MethodAccess;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.UNKNOWN;

/**
 * Created by zhangls on 2019/1/3.
 * @author zhangls
 * POST = {UNKNOWN, ADMIN} //只允许未登录角色和管理员角色新增评论，默认配置是 {LOGIN, ADMIN}
 */
@MethodAccess(POST = {UNKNOWN, ADMIN})
public class Product {
}
