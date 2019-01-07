package apijson.demo.server.mapper;


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
     * @param sql
     * @return
     */
    List<LinkedHashMap<String, Object>> superManagerSelect(String sql);


//    @Insert("INSERT INTO users(userName,passWord,user_sex) VALUES(#{userName}, #{passWord}, #{userSex})")
//    Boolean insert(UserEntity user);
//
//    @Update("UPDATE users SET userName=#{userName},nick_name=#{nickName} WHERE id =#{id}")
//    Boolean update(UserEntity user);
//
//    @Delete("DELETE FROM users WHERE id =#{id}")
//    Boolean delete(Long id);


}
