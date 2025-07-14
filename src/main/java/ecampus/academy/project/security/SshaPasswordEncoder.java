package ecampus.academy.project.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.security.crypto.password.PasswordEncoder;

public class SshaPasswordEncoder implements PasswordEncoder {

    private static final SecureRandom RAND = new SecureRandom();
    private static final int SALT_LEN = 8;

    @Override
    public String encode(CharSequence rawPassword) {
        byte[] salt = new byte[SALT_LEN];
        RAND.nextBytes(salt);
        byte[] sha1 = sha1(rawPassword, salt);
        byte[] digSalt = concat(sha1, salt);
        return "{SSHA}" + Base64.getEncoder().encodeToString(digSalt);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encoded) {
        if (encoded == null || !encoded.startsWith("{SSHA}")) return false;
        byte[] data = Base64.getDecoder().decode(encoded.substring(6));
        byte[] salt = Arrays.copyOfRange(data, 20, data.length);
        byte[] sha1 = sha1(rawPassword, salt);
        return MessageDigest.isEqual(data, concat(sha1, salt));
    }

    private byte[] sha1(CharSequence pwd, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(pwd.toString().getBytes());
            md.update(salt);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
