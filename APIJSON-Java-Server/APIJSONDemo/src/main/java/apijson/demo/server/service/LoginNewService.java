package apijson.demo.server.service;


import com.alibaba.fastjson.JSONObject;
import com.zhangls.apijson.base.JsonResponse;

/**
 * Created by zhangls on 2019/1/4.
 */
public interface LoginNewService {

    /**
     * 登陆
     * @param reqJson
     * @return
     */
    public JsonResponse loginNewJson(JSONObject reqJson);
}
