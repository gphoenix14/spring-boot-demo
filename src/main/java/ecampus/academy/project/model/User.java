package ecampus.academy.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="users")
public class User {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
private Long id;

@Column(nullable=false,unique=true)
private String username;

@Column(nullable=false,length=128)
private String password;

@Column(nullable=false,columnDefinition="TEXT")
private String publicKey;

@Column(nullable=false,columnDefinition="TEXT")
private String privateKeyEnc;

public Long getId(){return id;}
public void setId(Long id){this.id=id;}
public String getUsername(){return username;}
public void setUsername(String username){this.username=username;}
public String getPassword(){return password;}
public void setPassword(String password){this.password=password;}
public String getPublicKey(){return publicKey;}
public void setPublicKey(String publicKey){this.publicKey=publicKey;}
public String getPrivateKeyEnc(){return privateKeyEnc;}
public void setPrivateKeyEnc(String privateKeyEnc){this.privateKeyEnc=privateKeyEnc;}
}
