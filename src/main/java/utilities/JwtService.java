package utilities;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class JwtService {

    // Clé secrète connue uniquement du serveur (HS512 => au moins 64 caractères)
    private static final byte[] SECRET_KEY =
            "gestion-options-esprit-secret-key-change-me-0123456789-abcdefghijklmnopqrstuvwxyz"
                    .getBytes(StandardCharsets.UTF_8);

    // Durée de vie de l'access token
    public static final long ACCESS_MINUTES = 3L;

    // ===== Génération =====

    public static String generateAccessToken(String username, String issuer) {
        long duree = ACCESS_MINUTES *60 * 1000;   // en millisecondes
        return buildToken(username, issuer, "access", duree, null);
    }

    private static String buildToken(String username, String issuer, String type,
                                     long dureeMillis, String jti) {
        long now = System.currentTimeMillis();
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)                        // sub : l'utilisateur
                .setIssuer(issuer)                           // iss : l'émetteur
                .setIssuedAt(new Date(now))                  // iat : date d'émission
                .setExpiration(new Date(now + dureeMillis))  // exp : date d'expiration
                .claim("type", type);                        // notre propre claim
        if (jti != null) {
            builder.setId(jti);                              // jti : identifiant unique (partie C)
        }
        return builder.signWith(SignatureAlgorithm.HS512, SECRET_KEY).compact();
    }

    // ===== Validation =====

    // Lève une exception si la signature est invalide ou si le jeton a expiré
    private static Claims parse(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    // Valide un access token : signature, expiration et type
    public static Claims validateAccessToken(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("type"))) {
            throw new IllegalArgumentException("Ce jeton n'est pas un access token");
        }
        return claims;
    }

    // ===== Partie C : refresh token =====

    // Durée de vie du refresh token
    public static final long REFRESH_DAYS = 7L;

    // Registre des refresh tokens valides : jti -> utilisateur
    // (en mémoire pour l'atelier ; en production : table en base de données)
    private static final Map<String, String> REFRESH_STORE = new ConcurrentHashMap<>();

    // Génère un refresh token et l'enregistre dans le registre
    public static String generateRefreshToken(String username, String issuer) {
        String jti = UUID.randomUUID().toString();          // identifiant unique
        long duree = REFRESH_DAYS * 24 * 60 * 60 * 1000;     // en millisecondes
        String token = buildToken(username, issuer, "refresh", duree, jti);
        REFRESH_STORE.put(jti, username);
        return token;
    }

    // Valide un refresh token ET le consomme (usage unique). Retourne l'utilisateur.
    public static String useRefreshToken(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("type"))) {
            throw new IllegalArgumentException("Ce jeton n'est pas un refresh token");
        }
        // remove() supprime l'entrée et renvoie null si le jti est inconnu
        if (REFRESH_STORE.remove(claims.getId()) == null) {
            throw new IllegalArgumentException("Refresh token révoqué ou déjà utilisé");
        }
        return claims.getSubject();
    }

    // Révoque un refresh token (déconnexion)
    public static void revokeRefreshToken(String token) {
        try {
            REFRESH_STORE.remove(parse(token).getId());
        } catch (Exception e) {
            // jeton déjà invalide ou expiré : il n'y a rien à révoquer
        }
    }

}
