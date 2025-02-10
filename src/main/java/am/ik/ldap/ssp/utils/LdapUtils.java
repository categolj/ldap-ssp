package am.ik.ldap.ssp.utils;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import org.springframework.ldap.support.LdapNameBuilder;

public class LdapUtils {

	public static LdapName getFullDn(LdapName dn, Context baseCtx) throws NamingException {
		LdapName baseDn = LdapNameBuilder.newInstance(baseCtx.getNameInNamespace()).build();
		if (dn.startsWith(baseDn)) {
			return dn;
		}
		baseDn.addAll(dn);
		return baseDn;
	}

}
