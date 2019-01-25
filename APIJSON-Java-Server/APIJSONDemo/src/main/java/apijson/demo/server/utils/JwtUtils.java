package apijson.demo.server.utils;

import apijson.demo.server.common.UtilConstants;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zhangls on 2019/1/8.
 *
 * @author zhangls
 */
@Slf4j
public class JwtUtils {

    /**
     * 生成 Jwt token
     *
     * @param claims 可以保存的用户信息，不用session保存了
     *               iss: jwt签发者
     *               sub: jwt所面向的用户
     *               aud: 接收jwt的一方
     *               exp: jwt的过期时间，这个过期时间必须要大于签发时间
     *               nbf: 定义在什么时间之前，该jwt都是不可用的.
     *               iat: jwt的签发时间
     *               jti: jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击。
     * @return
     */
    public static String createJwt(Map<String, Object> claims, String subject, Integer seconds) {

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(UtilConstants.Jwt.JWT_KEY);
//        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        // jwt的签发时间
        DateTime now = DateTime.now();
        // 设置过期时间
        DateTime exp = now.plusSeconds(seconds);

        // 生成key
        javax.crypto.SecretKey key = generalKey();

        JwtBuilder jwtBuilder = Jwts.builder()
                //如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                .setClaims(claims)
                //设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                .setId(UUID.randomUUID().toString())
                //iat: jwt的签发时间
                .setIssuedAt(now.toDate())
                //设置过期时间
                .setExpiration(exp.toDate())
                //sub(Subject)：代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid之类的，作为什么用户的唯一标志。
                .setSubject(subject)
                //设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, key);

        return UtilConstants.Jwt.JWT_BEARER + jwtBuilder.compact();
    }

    /**
     * 创建 Claims
     * @param userInfo
     * @return
     */
    public static Map<String, Object> createClaims(JSONObject userInfo){

        return JSONObject.toJavaObject(userInfo, Map.class);
    }
    /**
     * JWTToken刷新生命周期
     * 1、登录成功后将用户的JWT生成的Token作为k、v存储到cache缓存里面(这时候k、v值一样)
     * 2、当该用户在次请求时，通过JWTFilter层层校验之后会进入到doGetAuthenticationInfo进行身份验证
     * 3、当该用户这次请求JWTToken值还在生命周期内，则会通过重新PUT的方式k、v都为Token值，缓存中的token值生命周期时间重新计算(这时候k、v值一样)
     * 4、当该用户这次请求jwt生成的token值已经超时，但该token对应cache中的k还是存在，则表示该用户一直在操作只是JWT的token失效了，程序会给token对应的k映射的v值重新生成JWTToken并覆盖v值，该缓存生命周期重新计算
     * 5、当该用户这次请求jwt在生成的token值已经超时，并在cache中不存在对应的k，则表示该用户账户空闲超时，返回用户信息已失效，请重新登录。
     * 6、每次当返回为true情况下，都会给Response的Header中设置Authorization，该Authorization映射的v为cache对应的v值。
     * 7、注：当前端接收到Response的Header中的Authorization值会存储起来，作为以后请求token使用
     * @param userName
     * @param passWord
     * @return
     */
    public Boolean jwtRefresh(String userName,String passWord){
        return true;
    }

    /**
     * 生成 私钥
     *
     * @return
     */
    public static SecretKey generalKey() {
        String stringKey = UtilConstants.Jwt.JWT_KEY;
        byte[] encodedKey = Base64.decodeBase64(stringKey);

        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }

    /**
     * 解密jwt
     *
     * @param jwt
     * @return
     * @throws Exception
     */
    public static Claims parseJWT(String jwt) {

        return Jwts.parser().setSigningKey(generalKey()).parseClaimsJws(jwt).getBody();
    }


    public static void main(String[] args) throws Exception {

        Map<String, Object> map = Maps.newHashMap();

        map.put("userName","zhangls");
        map.put("roleName","admin");

        String token = JwtUtils.createJwt(map,"{\"userId\":\"1001\",\"roleId\":\"2001\"}", 300);

        String token1 = JwtUtils.createJwt(map,"{\"userId\":\"1234\",\"roleId\":\"4321\"}", 300);

        log.info("【jwt】: {}", token.substring(UtilConstants.Jwt.JWT_BEARER.length()));
        log.info("【jwt1】: {}", token1.substring(UtilConstants.Jwt.JWT_BEARER.length()));


//        Thread.sleep(2000);

        Claims claims = JwtUtils.parseJWT(token.substring(UtilConstants.Jwt.JWT_BEARER.length()));

        Claims claims1 = JwtUtils.parseJWT(token1.substring(UtilConstants.Jwt.JWT_BEARER.length()));

        // {sub={"userId":"1001","roleId":"2001"}, roleName=admin, userName=zhangls, exp=1547006112, iat=1547005812, jti=585fbae7-7e25-4bb6-89c0-1f594365ceb6}
        log.info("【私有数据解密】：{}", claims);
        // {sub={"userId":"1234","roleId":"4321"}, roleName=admin, userName=zhangls, exp=1547006113, iat=1547005813, jti=6d07ccd8-9ee3-42fa-96ba-864acd5a6ed0}
        log.info("【私有数据解密1】：{}", claims1);

    }
}
