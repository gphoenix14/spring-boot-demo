package ecampus.academy.project.service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ecampus.academy.project.model.User;
import ecampus.academy.project.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

private final UserRepository userRepository;

@Autowired
public UserDetailsServiceImpl(UserRepository userRepository){
this.userRepository=userRepository;
}

@Override
public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException{
User user=userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("User not found"));
return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
}
}
