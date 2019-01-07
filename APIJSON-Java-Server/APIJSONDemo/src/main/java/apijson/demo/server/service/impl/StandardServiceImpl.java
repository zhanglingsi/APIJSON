package apijson.demo.server.service.impl;


import apijson.demo.server.mapper.StandardMapper;
import apijson.demo.server.service.StandardService;
import apijson.demo.server.utils.JsonParseUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zuo.biao.apijson.parser.SQLProviderException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by zhangls on 2019/1/7.
 * @author zhangls
 */
@Slf4j
@Service
public class StandardServiceImpl implements StandardService {

    @Autowired
    private StandardMapper mapper;


    @Override
    public JSONObject queryService(JSONObject reqJson) {
        String sql = null;
        try{
            sql = JsonParseUtils.JsonToSql(reqJson);
            List<LinkedHashMap<String,Object>> ls = mapper.standardSelect(sql);

        }catch (SQLProviderException ex){
            log.error("【JSON串 转 SQL语句 失败！】");
            return JsonParseUtils.newErrorResult(ex);
        }catch (Exception e){
            log.error("【查询语句失败！】");
            return JsonParseUtils.newErrorResult(e);
        }

        return null;
    }

    @Override
    public JSONObject insertService(JSONObject reqJson) {
        return null;
    }
}
