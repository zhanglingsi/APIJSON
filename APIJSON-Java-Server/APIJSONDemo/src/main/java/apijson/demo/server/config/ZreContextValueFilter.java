package apijson.demo.server.config;

import apijson.demo.server.common.NumberDesensitization;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextValueFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by zhangls on 2019/1/24.
 *
 * @author zhangls
 */
@Slf4j
public class ZreContextValueFilter implements ContextValueFilter {

    /**
     * JSON 对象值脱敏
     *
     * @param context
     * @param object
     * @param name
     * @param value
     * @return
     */
    @Override
    public Object process(BeanContext context, Object object, String name, Object value) {
        if (value == null || !(value instanceof String)) {
            return value;
        }
        NumberDesensitization annotion = context.getAnnation(NumberDesensitization.class);
        if (annotion == null) {
            return value;
        }
        String propertyValue = (String) value;
        if (StringUtils.isEmpty(propertyValue)) {
            return "";
        }
        log.debug("【脱敏手机号成功：】{}", propertyValue);
        propertyValue = String.format("%s****%s", propertyValue.substring(0, 3), propertyValue.substring(7));
        return propertyValue;

    }
}
