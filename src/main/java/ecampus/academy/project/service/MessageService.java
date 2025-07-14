package ecampus.academy.project.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import ecampus.academy.project.model.Message;
import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.MessageRepository;
import ecampus.academy.project.repository.UserRepository;

@Service
public class MessageService {

private final MessageRepository messageRepository;
private final UserRepository userRepository;
private final Set<SseEmitter> emitters=Collections.synchronizedSet(new HashSet<>());

@Autowired
public MessageService(MessageRepository messageRepository,UserRepository userRepository){
this.messageRepository=messageRepository;
this.userRepository=userRepository;
}

/* salva messaggio (privato o broadcast) */
public Message save(Message message,String senderUsername,boolean isBroadcast,String receiverUsername){
User sender=userRepository.findByUsername(senderUsername)
        .orElseThrow(() -> new IllegalArgumentException("Utente mittente non trovato"));
message.setSender(sender);

if(!isBroadcast){
if(receiverUsername==null||receiverUsername.isBlank())
    throw new IllegalArgumentException("Username destinatario mancante");
User receiver=userRepository.findByUsername(receiverUsername)
        .orElseThrow(() -> new IllegalArgumentException("Utente destinatario non trovato"));
message.setReceiver(receiver);
message.setBroadcast(false);
}else{
message.setReceiver(null);
message.setBroadcast(true);
}

if(message.getTimestamp()==null)
    message.setTimestamp(LocalDateTime.now());

return messageRepository.save(message);
}

/* stream e query */
public Page<Message> findAllRelevantMessages(String currentUsername,Pageable pageable){
User current=userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
return messageRepository.findRelevantMessages(current.getId(),pageable);
}

public List<Message> findAllMessagesBetweenUsers(Long senderId,Long receiverId){
return messageRepository.findAllMessagesBetweenUsers(senderId,receiverId);
}

public List<Message> findAll(){return messageRepository.findAll();}

public void addEmitter(SseEmitter e){emitters.add(e);}
public void removeEmitter(SseEmitter e){emitters.remove(e);}
}
