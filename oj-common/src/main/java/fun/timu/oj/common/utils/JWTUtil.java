package fun.timu.oj.common.utils;


import fun.timu.oj.common.model.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class JWTUtil {


    /**
     * 主题
     */
    private static final String SUBJECT = "cloud";

    /**
     * 加密密钥
     */
    private static final String SECRET = "cloud.net168";

    /**
     * 令牌前缀
     */
    private static final String TOKNE_PREFIX = "cloud-link";


    /**
     * token过期时间，7天
     */
    private static final long EXPIRED = 1000 * 60 * 60 * 24 * 7;

    /**
     * Token黑名单Redis前缀
     */
    public static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    /**
     * 将token加入黑名单
     *
     * @param token         JWT令牌
     * @param redisTemplate Redis操作模板
     * @return 是否成功添加到黑名单
     */
    public static boolean addToBlacklist(String token, RedisTemplate<Object, Object> redisTemplate) {
        if (token != null && token.startsWith(TOKNE_PREFIX)) {
            String tokenValue = token.replace(TOKNE_PREFIX, "");
            try {
                // 计算剩余有效时间
                Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(tokenValue).getBody();
                Date expiration = claims.getExpiration();
                long ttl = expiration.getTime() - System.currentTimeMillis();
                if (ttl > 0) {
                    // 将token加入黑名单，过期时间与token剩余有效期一致
                    redisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + tokenValue, "1", ttl, TimeUnit.MILLISECONDS);
                    log.info("Token已添加至黑名单，有效期：{}毫秒", ttl);
                    return true;
                }
            } catch (Exception e) {
                log.error("添加Token至黑名单失败：{}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * 检查token是否在黑名单中
     *
     * @param token         JWT令牌
     * @param redisTemplate Redis操作模板
     * @return 如果在黑名单中返回true，否则返回false
     */
    public static boolean isInBlacklist(String token, RedisTemplate<Object, Object> redisTemplate) {
        if (token != null && token.startsWith(TOKNE_PREFIX)) {
            String tokenValue = token.replace(TOKNE_PREFIX, "");
            return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + tokenValue));
        }
        return false;
    }

    /**
     * 生成JSON Web Token (JWT)
     *
     * @param loginUser 登录用户对象，包含用户相关信息
     * @return 生成的JWT字符串
     * <p>
     * 此方法负责根据登录用户信息创建一个JWT它首先检查登录用户对象是否为空，
     * 如果为空则抛出空指针异常然后，它使用Jwts.builder()来构建JWT，在这个过程中，
     * 它将用户信息作为payload的一部分，并设置令牌的过期时间最后，它使用指定的算法
     * 和密钥对令牌进行签名并返回生成的JWT字符串
     */
    public static String geneJsonWebTokne(LoginUser loginUser) {

        // 检查登录用户对象是否为空
        if (loginUser == null) {
            throw new NullPointerException("对象为空");
        }

        // 构建JWT，设置主题、用户信息、签发时间、过期时间和签名算法
        String token = Jwts.builder().setSubject(SUBJECT)
                // 配置payload
                .claim("head_img", loginUser.getHeadImg()).claim("account_no", loginUser.getAccountNo()).claim("phone", loginUser.getPhone()).claim("auth", loginUser.getAuth()).setIssuedAt(new Date()).setExpiration(new Date(CommonUtil.getCurrentTimestamp() + EXPIRED)).signWith(SignatureAlgorithm.HS256, SECRET).compact();

        // 在令牌前添加前缀
        token = TOKNE_PREFIX + token;
        return token;
    }


    /**
     * 解密jwt
     *
     * @param token 待解密的jwt字符串
     * @return 解密成功返回Claims对象，包含jwt中的声明，解密失败返回null
     */
    public static Claims checkJWT(String token) {

        try {
            // 使用SECRET作为签名密钥，解析传入的token并获取jwt的body部分
            final Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKNE_PREFIX, "")).getBody();
            return claims;
        } catch (Exception e) {

            // 记录jwt解密失败的日志
            log.error("jwt 解密失败");
            return null;
        }

    }
}