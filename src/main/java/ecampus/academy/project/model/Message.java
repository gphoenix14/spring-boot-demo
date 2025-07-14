package ecampus.academy.project.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "messages")
public class Message {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne
@JoinColumn(name = "sender_id")
private User sender;

@ManyToOne
@JoinColumn(name = "receiver_id")
private User receiver;

private String content;
private LocalDateTime timestamp;
private boolean isBroadcast;

/* === GET/SET === */
public Long getId(){return id;}
public void setId(Long id){this.id=id;}

public User getSender(){return sender;}
public void setSender(User sender){this.sender=sender;}

public User getReceiver(){return receiver;}
public void setReceiver(User receiver){this.receiver=receiver;}

public String getContent(){return content;}
public void setContent(String content){this.content=content;}

public LocalDateTime getTimestamp(){return timestamp;}
public void setTimestamp(LocalDateTime timestamp){this.timestamp=timestamp;}

public boolean isBroadcast(){return isBroadcast;}
public void setBroadcast(boolean isBroadcast){this.isBroadcast=isBroadcast;}
}
