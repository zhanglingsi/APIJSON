package apijson.demo.server.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 *
 * 注册-服务选项表
 *
 * Created by zhangls on 2019/1/25.
 * @author zhangls
 */
@Slf4j
public class ServiceOption implements Serializable {

    private static final long serialVersionUID = 8350327877975452483L;

    /**
     * 服务-是否可以查询
     */
    @Getter
    @Setter
    private Boolean hasSearch;

    /**
     * 服务-是否是文件服务
     */
    @Getter
    @Setter
    private Boolean hasFile;


}
