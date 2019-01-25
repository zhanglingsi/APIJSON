package apijson.demo.server.controller;


import apijson.demo.server.common.JsonRequest;
import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.model.LoginVo;
import apijson.demo.server.service.StandardService;
import apijson.demo.server.utils.JwtUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 生成token服务 POST请求  参数 userName (登陆用户名), password (登陆密码), ip (访问IP), time(token超时时间)
 * 调用格式 1 > http://IP:PORT/api/generateToken
 * {"data":"WupXwe45MOAJ0M3fBnCu2CN_XSVb6fGXN4XyoA79B-8","code":"000000","message":"成功"}
 * <p>
 * <p>
 * 标准查询Controller 查询资源 共有8种，如下：
 * <p>
 * 1. 标准统一查询接口：返回多种数据 apiName : 服务名 ，apiId ：服务编号（版本号） token ：JWT生成的签名字符串
 * 调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataJson
 * 调用格式 2 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataXml
 * 调用格式 3 > http://IP:PORT/api/{apiName}/{apiId}/{token}?wsdl
 * <p>
 * 2. 文件服务支持预览、下载：
 * 文件预览 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/readFile/文件名全称
 * 文件下载 2 > http://IP:PORT/api/{apiName}/{apiId}/{token}/downloadFile/文件名全称
 * <p>
 * 3. 获取去掉title字段的JSON数据：
 * 调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataJsonWithoutTitle?pageNo=1&pageSize=2&search=&showCloumns=
 * 说明：参数名         类型    是否必填   描述
 * showCloumns	String	否	     选择需要显示的列，多个列用逗号分隔，不传或为空表示查询全部列
 * <p>
 * 正常返回  没有title结果
 * <p>
 * 4. 数据校验接口
 * 调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getIsHasData?search=
 * 返回格式 {
 * "data": {
 * "return": true //true 为有数据，false为没数据
 * }
 * }
 * <p>
 * 5. 字段及相关字典列表查询
 * 调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/structuredData
 * <p>
 * 获取数据接口（服务可用时间为工作日8:00 - 18:00）
 * Created by zhangls on 2019/1/7.
 *
 * @author zhangls
 */
@Slf4j
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class StandardController {


    @Autowired
    private StandardService service;

    /**
     * 获取 查询
     * <p>
     * eg. 查询tb_product 商品信息  主表  关联 tb_pro_type 商品类型表
     * <p>
     * 查询所有商品信息 （系统默认查询最大条数100）
     * <p>
     * 接口可定义  apiName = product  ， apiId = proAll
     * <p>
     * queryStr ：必须POST提交
     * 参数名	        参数类型	    必填	    描述
     * pageNo	    String	    是	    分页参数，页号
     * pageSize	    String	    是	    分页参数，一页显示的数量，一页最大100条
     * search	    String	    是	    高级搜索参数，json字符串。如果没有传空字符串
     * <p>
     * 简单查询举例：search=[{"xm.like":"张%"}] ，表示查询姓“张”的员工
     * 组合查询举例：search=[{"xm.like":"张%"},{"and.xb.eq":"男"}] ，表示查询姓“张”并且性别为“男”的的员工
     * <p>
     * 逻辑运算符	      描述
     * eq	          等于
     * neq	          不等于
     * gt	          大于
     * gte	          大于等于
     * lt	          小于
     * lte	          小于等于
     * like	          模糊匹配（类似数据库的LIKE操作，支持通配符）
     * in	          包含（类似数据库的IN操作）
     * isNull	      为空，举例：{"xm.isNull":""}
     * isNotNull	  不为空，举例：{"xm.isNotNull":""}
     * <p>
     * <p>
     * 连接运算符	   描述
     * and	       且
     * or	       或
     * <p>
     * 每个字段的可选查询条件范围参照title列表里面对应字段的filterType字段，根据字段支持的查询运算符进行查询，支持简单查询、组合查询。
     * <p>
     * "title":[ //数据字典
     * {
     * "dataCode":"xm",//字段名
     * "dataName":"姓名",//信息项名称
     * "filterType":"like;eq",//支持的查询运算符 该字段可like和eq
     * "dicTable":"",//字段字典：{'原始值':'字典映射值'}
     * "shareKind":"",//共享类型（1：无条件共享，2：有条件共享）
     * "isFixed":true//是否为必选条件（true是，false否）
     * }
     * ]
     */
    @PostMapping("/{apiName}/{apiId}/{method}/json")
    public String getJson(@RequestBody(required=false) String queryStr,
                                    @PathVariable String apiName,
                                    @PathVariable String apiId,
                                    @PathVariable String method,
                                    final HttpServletRequest request) {
        log.info("【请求服务名为：】{}", apiName);
        log.info("【请求服务版本为：】{}", apiId);
        log.info("【请求服务的方法为：】{}", method);


        // 获取JWT中的用户信息
        Claims claims = (Claims) request.getAttribute(UtilConstants.Jwt.JWT_USER_INFO);
        log.info("【获取JWT中的用户信息为】：{}", claims);

        // 1. 根据服务名、服务版本号，查询服务网关表
        // 2. 根据method，和ID，查询该用户是否有权限调用该服务。
        // 3. 调用服务。并返回。


        return null;
    }
}
