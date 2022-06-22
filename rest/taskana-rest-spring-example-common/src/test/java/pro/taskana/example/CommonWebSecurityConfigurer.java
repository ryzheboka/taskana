package pro.taskana.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.jaasapi.JaasApiIntegrationFilter;

import pro.taskana.common.rest.SpringSecurityToJaasFilter;

@EnableWebSecurity
public class CommonWebSecurityConfigurer {

  private final GrantedAuthoritiesMapper grantedAuthoritiesMapper;

  private final String ldapServerUrl;
  private final String ldapBaseDn;
  private final String ldapGroupSearchBase;
  private final String ldapUserDnPatterns;

  @Autowired
  public CommonWebSecurityConfigurer(
      @Value("${taskana.ldap.serverUrl:ldap://localhost:10389}") String ldapServerUrl,
      @Value("${taskana.ldap.baseDn:OU=Test,O=TASKANA}") String ldapBaseDn,
      @Value("${taskana.ldap.groupSearchBase:cn=groups}") String ldapGroupSearchBase,
      @Value("${taskana.ldap.userDnPatterns:uid={0},cn=users}") String ldapUserDnPatterns,
      GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
    this.ldapServerUrl = ldapServerUrl;
    this.ldapBaseDn = ldapBaseDn;
    this.ldapGroupSearchBase = ldapGroupSearchBase;
    this.ldapUserDnPatterns = ldapUserDnPatterns;
    this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .and()
        .csrf()
        .disable()
        .httpBasic()
        .and()
        .addFilter(jaasApiIntegrationFilter())
        .addFilterAfter(new SpringSecurityToJaasFilter(), JaasApiIntegrationFilter.class)
        .authorizeRequests()
        .anyRequest()
        .fullyAuthenticated();

    return http.build();
  }

  //  @Bean
  //  public EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean() {
  //    EmbeddedLdapServerContextSourceFactoryBean contextSourceFactoryBean =
  //        EmbeddedLdapServerContextSourceFactoryBean.fromEmbeddedLdapServer();
  //    contextSourceFactoryBean.setPort(11389);
  //    contextSourceFactoryBean.setRoot(ldapBaseDn);
  //
  //    return contextSourceFactoryBean;
  //  }

  @Bean
  LdapAuthoritiesPopulator authorities(BaseLdapPathContextSource contextSource) {
    DefaultLdapAuthoritiesPopulator authoritiesPopulator =
        new DefaultLdapAuthoritiesPopulator(contextSource, ldapGroupSearchBase);
    return authoritiesPopulator;
  }

  @Bean
  AuthenticationManager authenticationManager(
      BaseLdapPathContextSource contextSource, LdapAuthoritiesPopulator authorities) {
    LdapBindAuthenticationManagerFactory factory =
        new LdapBindAuthenticationManagerFactory(contextSource);
    factory.setUserDnPatterns(ldapUserDnPatterns);
    factory.setLdapAuthoritiesPopulator(authorities);
    factory.setAuthoritiesMapper(grantedAuthoritiesMapper);
    return factory.createAuthenticationManager();
  }

  //  @Override
  //  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
  //    auth.ldapAuthentication()
  //        .contextSource()
  //        .and()
  //        .passwordCompare()
  //        .passwordAttribute("userPassword");
  //  }

  private JaasApiIntegrationFilter jaasApiIntegrationFilter() {
    JaasApiIntegrationFilter filter = new JaasApiIntegrationFilter();
    filter.setCreateEmptySubject(true);
    return filter;
  }
}
