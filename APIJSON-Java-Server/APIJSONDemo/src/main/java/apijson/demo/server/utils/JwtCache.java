package apijson.demo.server.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangls on 2019/1/9.
 *
 * @author zhangls
 */
@Slf4j
public class JwtCache {

    private static LoadingCache<String, String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000).maximumSize(100000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    /**
     * 添加缓存
     *
     * @param key
     * @param value
     */
    public static void setCache(String key, String value) {
        localCache.put(key, value);
    }

    public static String getCache(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if ("null".equals(value)) {
                return null;
            }
            return value;
        } catch (ExecutionException e) {
            log.error("getKey()方法错误 {}", e);
        }
        return null;
    }
}
