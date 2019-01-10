package com.zhangls.apijson.base.model;


import com.zhangls.apijson.annotation.MethodAccess;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

import static com.zhangls.apijson.base.model.RequestRole.LOGIN;
import static com.zhangls.apijson.base.model.RequestRole.ADMIN;


/**
 * 测试用例文档
 *
 * @author zhangls
 */
@Data
@MethodAccess(
        GET = {LOGIN, ADMIN},
        HEAD = {LOGIN, ADMIN},
        PUT = {LOGIN, ADMIN}
)
public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 用户id  应该用adminId，只有当登录账户是管理员时才能操作文档。  需要先建Admin表，新增登录等相关接口。
     */
    private Long userId;
    /**
     * 接口版本号  <=0 - 不限制版本，任意版本都可用这个接口  >0 - 在这个版本添加的接口
     */
    private Integer version;
    /**
     * 接口名称
     */
    private String name;
    /**
     * 请求地址
     */
    private String url;
    /**
     * 请求  用json格式会导致强制排序，而请求中引用赋值只能引用上面的字段，必须有序。
     */
    private String request;
    /**
     * 创建日期
     */
    private Timestamp date;
    /**
     * 标准返回结果
     */
    private String response;

}