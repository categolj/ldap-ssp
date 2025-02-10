package am.ik.ldap.ssp.user;

import am.ik.ldap.ssp.LdapProps;
import am.ik.ldap.ssp.utils.LdapUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.ldap.LdapName;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class UserService {

	private final LdapTemplate ldapTemplate;

	private final LdapProps props;

	public UserService(LdapTemplate ldapTemplate, LdapProps props) {
		this.ldapTemplate = ldapTemplate;
		this.props = props;
	}

	public Map<String, Object> findUser(String userId) {
		LdapQuery query = LdapQueryBuilder.query().where(this.props.idAttribute()).is(userId);
		List<Map<String, Object>> results = this.ldapTemplate.search(query,
				(ContextMapper<Map<String, Object>>) ctx -> {
					DirContextAdapter dirCtx = (DirContextAdapter) ctx;
					LdapName fullDn = LdapUtils.getFullDn((LdapName) dirCtx.getDn(), dirCtx);
					Map<String, Object> attributes = new LinkedHashMap<>();
					attributes.put("dn", fullDn.toString());
					for (NamingEnumeration<? extends Attribute> i = dirCtx.getAttributes().getAll(); i.hasMore();) {
						Attribute attribute = i.next();
						String name = attribute.getID();
						if (attribute.size() == 1) {
							if (!this.props.passwordAttribute().equals(name)) {
								attributes.put(name, attribute.get());
							}
						}
						else {
							Iterable<?> iterable = (Iterable<?>) attribute;
							attributes.put(name, StreamSupport.stream(iterable.spliterator(), false).toList());
						}
					}
					return attributes;
				});
		if (results.isEmpty()) {
			throw new IllegalArgumentException("User Not Found: " + userId);
		}
		return results.getFirst();
	}

}
