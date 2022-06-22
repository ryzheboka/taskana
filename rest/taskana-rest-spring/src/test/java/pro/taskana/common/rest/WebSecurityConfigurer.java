package pro.taskana.common.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;

@EnableWebSecurity
public class WebSecurityConfigurer {
  @Autowired private ObjectPostProcessor<Object> objectPostProcessor;
  private final LdapAuthoritiesPopulator ldapAuthoritiesPopulator;
  private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

  private final String ldapServerUrl;
  private final String ldapBaseDn;
  private final String ldapGroupSearchBase;
  private final String ldapUserDnPatterns;

  @Autowired
  public WebSecurityConfigurer(
      @Value("${taskana.ldap.serverUrl:ldap://localhost:10389}") String ldapServerUrl,
      @Value("${taskana.ldap.baseDn:OU=Test,O=TASKANA}") String ldapBaseDn,
      @Value("${taskana.ldap.groupSearchBase:cn=groups}") String ldapGroupSearchBase,
      @Value("${taskana.ldap.userDnPatterns:uid={0},cn=users}") String ldapUserDnPatterns,
      LdapAuthoritiesPopulator ldapAuthoritiesPopulator,
      GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
    this.ldapServerUrl = ldapServerUrl;
    this.ldapBaseDn = ldapBaseDn;
    this.ldapGroupSearchBase = ldapGroupSearchBase;
    this.ldapUserDnPatterns = ldapUserDnPatterns;
    this.ldapAuthoritiesPopulator = ldapAuthoritiesPopulator;
    this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
  }

  @Bean
  AuthenticationManager ldapAuthenticationManager() throws Exception {
    AuthenticationManagerBuilder authenticationBuilder =
        new AuthenticationManagerBuilder(objectPostProcessor);
    return authenticationBuilder
        .ldapAuthentication()
        .userDnPatterns(ldapUserDnPatterns)
        .groupSearchBase(ldapGroupSearchBase)
        .ldapAuthoritiesPopulator(ldapAuthoritiesPopulator)
        .authoritiesMapper(grantedAuthoritiesMapper)
        .contextSource()
        .url(ldapServerUrl + "/" + ldapBaseDn)
        .and()
        .passwordCompare()
        .passwordAttribute("userPassword")
        .and()
        .and()
        .build();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests(
        (authz) -> {
          try {
            authz
                .and()
                .addFilter(jaasApiIntegrationFilter())
                .addFilterAfter(new SpringSecurityToJaasFilter(), JaasApiIntegrationFilter.class);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
    http.csrf().disable().httpBasic();
    http.headers().and().authorizeRequests().anyRequest().fullyAuthenticated();
    return http.build();
  }

  private JaasApiIntegrationFilter jaasApiIntegrationFilter() {
    JaasApiIntegrationFilter filter = new JaasApiIntegrationFilter();
    filter.setCreateEmptySubject(true);
    return filter;
  }
}
