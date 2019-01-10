package com.zhangls.apijson.annotation;

import com.zhangls.apijson.base.model.RequestRole;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static com.zhangls.apijson.base.model.RequestRole.OWNER;
import static com.zhangls.apijson.base.model.RequestRole.CIRCLE;
import static com.zhangls.apijson.base.model.RequestRole.CONTACT;
import static com.zhangls.apijson.base.model.RequestRole.UNKNOWN;
import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.LOGIN;

/**
 * 请求方法权限，只允许某些角色通过对应方法访问
 *
 * 新建实体类增加此注解 自动赋权-默认的访问权限
 *
 * @author Lemon
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface MethodAccess {

    RequestRole[] GET() default {UNKNOWN, LOGIN, CONTACT, CIRCLE, OWNER, ADMIN};

    RequestRole[] HEAD() default {UNKNOWN, LOGIN, CONTACT, CIRCLE, OWNER, ADMIN};

    RequestRole[] GETS() default {LOGIN, CONTACT, CIRCLE, OWNER, ADMIN};

    RequestRole[] HEADS() default {LOGIN, CONTACT, CIRCLE, OWNER, ADMIN};

    RequestRole[] POST() default {OWNER, ADMIN};

    RequestRole[] PUT() default {OWNER, ADMIN};

    RequestRole[] DELETE() default {OWNER, ADMIN};

}
