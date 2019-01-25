package apijson.demo.server.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.Serializable;

/**
 * Created by zhangls on 2019/1/24.
 * @author zhangls
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class JsonRequest implements Serializable {

    private static final long serialVersionUID = 8350327009975282483L;

    private String contentType;
    private String accept;
    private String reqUrl;
    private String reqJsonStr;

    public JsonRequest(String reqUrl, String reqJsonStr){
        this.contentType = MediaType.APPLICATION_JSON_UTF8_VALUE;
        this.accept = MediaType.APPLICATION_JSON_UTF8_VALUE;
        this.reqUrl = reqUrl;
        this.reqJsonStr = reqJsonStr;
    }


}
