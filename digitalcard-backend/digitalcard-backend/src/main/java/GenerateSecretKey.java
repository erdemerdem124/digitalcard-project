import io.jsonwebtoken.security.Keys;
import java.util.Base64;
public class GenerateSecretKey {

	public static void main(String[] args) {
		 // HMAC-SHA512 için 512 bit (64 byte) uzunluğunda rastgele bir key oluşturur
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512).getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);
        System.out.println("Yeni Base64 Kodlu Secret Key: " + base64Key);
	}

}
