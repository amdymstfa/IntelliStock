package com.logistics.intellistock.security;

import com.logistics.intellistock.entity.User;
import com.logistics.intellistock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByLoginAndIsActive(username, true)
      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return org.springframework.security.core.userdetails.User.builder()
      .username(user.getLogin())
      .password(user.getPassword())
      .authorities(getAuthorities(user))
      .accountExpired(false)
      .accountLocked(!user.getIsActive())
      .credentialsExpired(false)
      .disabled(!user.getIsActive())
      .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(User user) {
    return Collections.singletonList(
      new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
    );
  }
}
