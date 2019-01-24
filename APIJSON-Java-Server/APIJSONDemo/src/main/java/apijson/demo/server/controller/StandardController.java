package apijson.demo.server.controller;


import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.service.StandardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 生成token服务 POST请求  参数 userName (登陆用户名), password (登陆密码), ip (访问IP), time(token超时时间)
 * 调用格式 1 > http://IP:PORT/api/generateToken
 *         {"data":"WupXwe45MOAJ0M3fBnCu2CN_XSVb6fGXN4XyoA79B-8","code":"20000","message":"成功"}
 *
 *
 * 标准查询Controller 查询资源 共有8种，如下：
 *
 * 1. 标准统一查询接口：返回多种数据 apiName : 服务名 ，apiId ：服务编号（版本号） token ：JWT生成的签名字符串
 *    调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataJson
 *    调用格式 2 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataXml
 *    调用格式 3 > http://IP:PORT/api/{apiName}/{apiId}/{token}?wsdl
 *
 * 2. 文件服务支持预览、下载：
 *    文件预览 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/readFile/文件名全称
 *    文件下载 2 > http://IP:PORT/api/{apiName}/{apiId}/{token}/downloadFile/文件名全称
 *
 * 3. 获取去掉title字段的JSON数据：
 *    调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getDataJsonWithoutTitle?pageNo=1&pageSize=2&search=&showCloumns=
 *    说明：参数名         类型    是否必填   描述
 *         showCloumns	String	否	     选择需要显示的列，多个列用逗号分隔，不传或为空表示查询全部列
 *
 *         正常返回  没有title结果
 *
 * 4. 数据校验接口
 *    调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/getIsHasData?search=
 *    返回格式 {
 *               "data": {
 *                  "return": true //true 为有数据，false为没数据
 *               }
 *            }
 *
 * 5. 字段及相关字典列表查询
 *    调用格式 1 > http://IP:PORT/api/{apiName}/{apiId}/{token}/structuredData
 *
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
     * 生成token
     * @param reqJson 参数 userName (登陆用户名), password (登陆密码), ip (访问IP), time(token超时时间)
     * @return
     */
    public String generateToken(@RequestBody String reqJson){
        return null;
    }


    /**
     * 获取 查询
     *
     * eg. 查询tb_product 商品信息  主表  关联 tb_pro_type 商品类型表
     *
     * 查询所有商品信息 （系统默认查询最大条数100）
     *
     * 接口可定义  apiName = product  ， apiId = proAll
     *
     * queryStr ：必须POST提交
     * 参数名	        参数类型	    必填	    描述
     * pageNo	    String	    是	    分页参数，页号
     * pageSize	    String	    是	    分页参数，一页显示的数量，一页最大100条
     * search	    String	    是	    高级搜索参数，json字符串。如果没有传空字符串
     *
     * 简单查询举例：search=[{"xm.like":"张%"}] ，表示查询姓“张”的员工
     * 组合查询举例：search=[{"xm.like":"张%"},{"and.xb.eq":"男"}] ，表示查询姓“张”并且性别为“男”的的员工
     *
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
     *
     *
     * 连接运算符	   描述
     * and	       且
     * or	       或
     *
     * 每个字段的可选查询条件范围参照title列表里面对应字段的filterType字段，根据字段支持的查询运算符进行查询，支持简单查询、组合查询。
     *
     * "title":[ //数据字典
     *           {
     *           "dataCode":"xm",//字段名
     *           "dataName":"姓名",//信息项名称
     *           "filterType":"like;eq",//支持的查询运算符 该字段可like和eq
     *           "dicTable":"",//字段字典：{'原始值':'字典映射值'}
     *           "shareKind":"",//共享类型（1：无条件共享，2：有条件共享）
     *           }
     *         ]
     *
     */
    @PostMapping("/{apiName}/{apiId}/{token}/getDataJson")
    public JsonResponse getDataJson(@RequestBody String queryStr,
                                    @PathVariable String apiName,
                                    @PathVariable String apiId,
                                    @PathVariable String token,
                                    final HttpServletRequest request) {
        log.info("【进入 getDataJson 方法，请求apiCode为】：{}", apiName);
        log.info("【进入 getDataJson 方法，请求apiId为】：{}", apiId);
        log.info("【进入 getDataJson 方法，请求token为】：{}", token);
        log.info("【进入 getDataJson 方法，请求JSON串为】：{}", queryStr);



//        // 获取JWT中的用户信息
//        Claims claims = (Claims) request.getAttribute(UtilConstants.Jwt.JWT_USER_INFO);
//        log.info("【获取JWT中的用户信息为】：{}", claims);
//
//        // 1. 访问权限
//        JSONObject jsonValidate = ControllerUtils.standardValidator(GET, queryStr);
//        log.info("【验证后的，返回的JSON串为】：{}", jsonValidate);
//
//        // 验证通过
//        if (jsonValidate.containsKey(JsonApiResponse.KEY_CODE) && jsonValidate.get(JsonApiResponse.KEY_CODE).equals(JsonApiResponse.CODE_SUCCESS)) {
//            // 有可能返回业务异常信息
//            return service.queryService(jsonValidate);
//        } else {
//            jsonValidate.put("request", queryStr);
//
//            return jsonValidate;
//        }

//        return jsonValidate;
        return null;
    }
}
