package ecampus.academy.project.util;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {

private CryptoUtils(){}

public static KeyPair generateRsaKeyPair(){
try{
KeyPairGenerator kpg=KeyPairGenerator.getInstance("RSA");
kpg.initialize(2048,SecureRandom.getInstanceStrong());
return kpg.generateKeyPair();
}catch(GeneralSecurityException e){
throw new IllegalStateException("RSA key generation failed",e);
}
}

public static String encodePublicKeySsh(PublicKey publicKey){
try{
java.security.interfaces.RSAPublicKey rsa=(java.security.interfaces.RSAPublicKey)publicKey;
ByteArrayOutputStream buf=new ByteArrayOutputStream();
writeSshString(buf,"ssh-rsa".getBytes(StandardCharsets.US_ASCII));
writeSshMpInt(buf,rsa.getPublicExponent());
writeSshMpInt(buf,rsa.getModulus());
String b64=Base64.getEncoder().encodeToString(buf.toByteArray());
return "ssh-rsa "+b64;
}catch(Exception e){
throw new IllegalStateException("Encoding SSH key failed",e);
}
}

private static void writeSshString(ByteArrayOutputStream buf,byte[] data)throws Exception{
int len=data.length;
buf.write((len>>>24)&0xFF);
buf.write((len>>>16)&0xFF);
buf.write((len>>>8)&0xFF);
buf.write(len&0xFF);
buf.write(data);
}

private static void writeSshMpInt(ByteArrayOutputStream buf,BigInteger i)throws Exception{
byte[] data=i.toByteArray();
writeSshString(buf,data);
}

public static String encryptPrivateKey(char[] password,PrivateKey privateKey){
try{
byte[] salt=new byte[16];
SecureRandom.getInstanceStrong().nextBytes(salt);
SecretKeyFactory skf=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec=new PBEKeySpec(password,salt,100_000,256);
SecretKey tmp=skf.generateSecret(spec);
SecretKey secret=new SecretKeySpec(tmp.getEncoded(),"AES");
byte[] iv=new byte[16];
SecureRandom.getInstanceStrong().nextBytes(iv);
Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
cipher.init(Cipher.ENCRYPT_MODE,secret,new IvParameterSpec(iv));
byte[] cipherText=cipher.doFinal(privateKey.getEncoded());
byte[] combo=concat(salt,iv,cipherText);
return Base64.getEncoder().encodeToString(combo);
}catch(GeneralSecurityException e){
throw new IllegalStateException("Encrypting private key failed",e);
}
}

public static String encryptStringAES(String plaintext,char[] password){
try{
byte[] salt=new byte[16];
SecureRandom.getInstanceStrong().nextBytes(salt);
SecretKeyFactory skf=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec=new PBEKeySpec(password,salt,100_000,256);
SecretKey tmp=skf.generateSecret(spec);
SecretKey secret=new SecretKeySpec(tmp.getEncoded(),"AES");
byte[] iv=new byte[16];
SecureRandom.getInstanceStrong().nextBytes(iv);
Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
cipher.init(Cipher.ENCRYPT_MODE,secret,new IvParameterSpec(iv));
byte[] cipherText=cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
byte[] combo=concat(salt,iv,cipherText);
return Base64.getEncoder().encodeToString(combo);
}catch(GeneralSecurityException e){
throw new IllegalStateException("AES encryption failed",e);
}
}

private static byte[] concat(byte[]...parts){
int len=0;
for(byte[] p:parts)len+=p.length;
byte[] out=new byte[len];
int pos=0;
for(byte[] p:parts){
System.arraycopy(p,0,out,pos,p.length);
pos+=p.length;
}
return out;
}

public static String generateDynamicToken(String username){
try{
MessageDigest md=MessageDigest.getInstance("SHA-256");
String material=username.substring(0,Math.min(3,username.length()))+username.length()+username.charAt(username.length()-1);
byte[] digest=md.digest(material.getBytes());
return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0,12);
}catch(NoSuchAlgorithmException e){
throw new IllegalStateException(e);
}
}
public static PrivateKey decryptPrivateKey(char[] password,String encB64){
try{
byte[] combo=Base64.getDecoder().decode(encB64);
ByteBuffer buf=ByteBuffer.wrap(combo);
byte[] salt=new byte[16];buf.get(salt);
byte[] iv=new byte[16];buf.get(iv);
byte[] cipherText=new byte[buf.remaining()];buf.get(cipherText);

SecretKeyFactory skf=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec=new PBEKeySpec(password,salt,100_000,256);
SecretKey tmp=skf.generateSecret(spec);
SecretKey secret=new SecretKeySpec(tmp.getEncoded(),"AES");

Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
cipher.init(Cipher.DECRYPT_MODE,secret,new IvParameterSpec(iv));
byte[] pkcs8=cipher.doFinal(cipherText);

KeyFactory kf=KeyFactory.getInstance("RSA");
return kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
}catch(InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e){
throw new IllegalArgumentException("Password errata o chiave corrotta",e);
}
}


}
