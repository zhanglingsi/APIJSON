package com.zhangls.apijson.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * 非null注解
 * javax.validation.constraints.NotNull不在JDK里面，为了减少第三方库引用就在这里实现了一个替代品
 *
 * @author Lemon
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface NotNull {
}
