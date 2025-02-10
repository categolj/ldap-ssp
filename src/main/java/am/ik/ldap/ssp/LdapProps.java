package am.ik.ldap.ssp;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ldap")
public record LdapProps(@DefaultValue("") String userSearchBase, @DefaultValue("(cn={0})") String userSearchFilter,
		String groupSearchBase, String groupSearchFilter, @DefaultValue("cn") String idAttribute,
		@DefaultValue("givenName") String firstNameAttribute, @DefaultValue("sn") String lastNameAttribute,
		@DefaultValue("mail") String emailAttribute, @DefaultValue("userPassword") String passwordAttribute,
		@DefaultValue("users") String userRole, @DefaultValue("administrators") String adminRole) {

}
