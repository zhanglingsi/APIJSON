package apijson.demo.server.utils;

import apijson.demo.server.common.UtilConstants;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Created by zhangls on 2019/1/8.
 * @author zhangls
 */
@Slf4j
public class JwtUtils {

    /**
     * 生成 Jwt token
     * @param claims 可以保存的用户信息，不用session保存了
     * @return
     */
    public static String createToken(Map<String, Object> claims) {

        long nowMillis = System.currentTimeMillis();
        long ttlMillis = nowMillis + (3600 * 1000L * 24);

        DateTime now = new DateTime(nowMillis);
        DateTime exp = new DateTime(ttlMillis);

        JwtBuilder jwtBuilder = Jwts.builder()
                .setClaims(claims)
                .setId(UtilConstants.Jwt.JWT_ID)
                .setIssuedAt(now.toDate())
                .setExpiration(exp.toDate())
                .setSubject("123")
                .signWith(SignatureAlgorithm.HS256, UtilConstants.Jwt.JWT_KEY);

        return UtilConstants.Jwt.JWT_BEARER + jwtBuilder.compact();
    }

    /**
     * 获取Jwt token
     * @param token
     * @return
     * @throws Exception
     */
    public static Claims getToken(String token) throws Exception{
        Claims claims = Jwts.parser().setSigningKey(UtilConstants.Jwt.JWT_KEY).parseClaimsJws(token).getBody();

        return claims;
    }
}
