package apijson.demo.server.service.impl;

import apijson.demo.server.common.UtilConstants;
import apijson.demo.server.mapper.StandardMapper;
import apijson.demo.server.service.LoginNewService;
import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * Created by zhangls on 2019/1/4.
 *  ##查询手机号是否存在  head方法
 *  SELECT  COUNT(*)  AS COUNT  FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0;
 *
 *  ##根据手机号码查询用户隐私表信息，从中获取用户ID
 *  SELECT * FROM `apijson`.`apijson_privacy` WHERE  (  (`phone`='13000082001')  )  LIMIT 1 OFFSET 0;
 *
 *  ##查询用户表中信息  验证用户名密码正确性  是否可以登录
 *  SELECT  COUNT(*)  AS COUNT  FROM `apijson`.`apijson_privacy` WHERE  (  (`id`='82001') AND (`_password`='123456')  )  LIMIT 1 OFFSET 0;
 *
 *  ##根据用户ID查询用户基本信息 并返回  setSession
 *  SELECT * FROM `apijson`.`apijson_user` WHERE  (  (`id`='82001')  )  LIMIT 1 OFFSET 0;
 *
 * @author zhangls
 */
@Service
public class LoginNewServiceImpl implements LoginNewService {

    @Autowired
    private StandardMapper mapper;

    @Override
    public JsonApiResponse loginNewJson(JSONObject reqJson) {

        //1. 查询手机号是否存在  head方法

        String phone = reqJson.getString(UtilConstants.Login.PHONE);
        String password = reqJson.getString(UtilConstants.Login.PASS_WORD);

        Boolean isPhone =  mapper.countUserByPhone(phone) > 0;

        LinkedHashMap<String, Object> user = mapper.queryUserByPhone(phone);




        //2. 根据手机号码查询用户隐私表信息，从中获取用户ID
        //3. 查询用户表中信息  验证用户名密码正确性  是否可以登录
        //4. 根据用户ID查询用户基本信息 并返回

        return null;
    }
}
