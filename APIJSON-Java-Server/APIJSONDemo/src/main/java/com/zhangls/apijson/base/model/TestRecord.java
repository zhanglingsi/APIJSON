package com.zhangls.apijson.base.model;

import com.zhangls.apijson.annotation.MethodAccess;
import lombok.Data;

import java.io.Serializable;

import java.sql.Timestamp;

import static com.zhangls.apijson.base.model.RequestRole.ADMIN;
import static com.zhangls.apijson.base.model.RequestRole.LOGIN;

/**
 * 条件测试
 *
 * @author zhangls
 */
@Data
@MethodAccess(
        GET = {LOGIN, ADMIN},
        HEAD = {LOGIN, ADMIN}
)
public class TestRecord implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 唯一标识
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 测试用例文档id
     */
    private Long documentId;
    /**
     * 创建日期
     */
    private Timestamp date;
    /**
     * 对比结果
     */
    private String compare;
    /**
     * 接口返回结果JSON  用json格式会导致强制排序，而请求中引用赋值只能引用上面的字段，必须有序。
     */
    private String response;
    /**
     * response 的校验标准，是一个 JSON 格式的 AST ，描述了正确 Response 的结构、里面的字段名称、类型、长度、取值范围 等属性。
     */
    private String standard;

}