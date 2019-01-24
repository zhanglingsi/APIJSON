package apijson.demo.server.mapper;


import apijson.demo.server.model.LoginVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhangls on 2019/1/7.
 * @author zhangls
 */
@Mapper
public interface StandardMapper {

    /**
     * 通用查询方法
     *
     * @param sql 执行原生SQL语句
     * @return 返回有序查询结果
     */
    List<LinkedHashMap<String, Object>> standardSelect(String sql);

    /**
     * 通用方法 增、删、改 都用此方法
     * @param sql 执行原生SQL语句
     * @return 影响的行数
     */
    Integer standardInsert(String sql);

    /**
     * 登陆时 查询手机是否存在
     * @param phone
     * @return
     */
    Integer countUserByPhone(String phone);


    /**
     * 登陆时 查询用户密码表记录
     * @param phone
     * @return
     */
    LinkedHashMap<String, Object> queryUserByPhone(String phone);


    /**
     * 登陆时 查询用户密码表记录
     * @param vo
     * @return
     */
    LinkedHashMap<String, Object> queryUserByName(LoginVo vo);

}
