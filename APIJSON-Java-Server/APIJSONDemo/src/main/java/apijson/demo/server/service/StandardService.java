package apijson.demo.server.service;

import apijson.demo.server.common.JsonResponse;
import apijson.demo.server.model.LoginVo;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by zhangls on 2019/1/7.
 * @author zhangls
 */
public interface StandardService {

    /**
     * 通用 查询服务
     * @param reqJson 查询json串
     * @return 查询结果 json串
     */
    JSONObject queryService(JSONObject reqJson);

    /**
     * 通用 增删改服务
     * @param reqJson 增删改json串
     * @return 查询结果 json串
     */
    JSONObject insertService(JSONObject reqJson);

    /**
     * 登陆接口
     * @param vo
     * @return
     */
    JsonResponse loginService(LoginVo vo);

}
