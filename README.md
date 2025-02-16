# LDAP Self Service Portal

LDAP Self Service Portal is a lightweight web application designed for managing OpenLDAP passwords.
It offers two main features:

- **Password Change:** Users who know their current password can update it using a straightforward
  form.
- **Password Reset:** If a user forgets their current password, they can request a reset link to be
  sent via email through SendGrid.

> [!NOTE]
> Reset IDs are stored in memory for 5 minutes, so please exercise caution if you plan to scale out
> or deploy in a serverless environment.

## Docker Usage

You can quickly get started with Docker using the command below:

```shell
docker run \
  --name ldap-ssp \
  --rm \
  -p 8080:8080 \
  ghcr.io/categolj/ldap-ssp:native \
  --spring.ldap.urls=ldaps://ldap.example.com:636 \
  --spring.ldap.username="cn=admin,dc=example,dc=com" \
  --spring.ldap.password=changeme \
  --spring.ldap.base="dc=examle,dc=com" \
  --ldap.id-attribute=cn \
  --ldap.user-search-base="ou=people" \
  --ldap.user-search-filter="(cn={0})" \
  --sendgrid.url=https://api.sendgrid.com \
  --sendgrid.from=noreply@example.com \
  --sendgrid.api-key=SG.xxxxx \
  --ssp.external-url=http://localhost:8080
```

---

## Configuration

The application can be configured using command-line parameters or environment variables. Below is a
list of available configuration options:

| Property Name               | Environment Variable        | Description                                                                                 | Default Value           |
|-----------------------------|-----------------------------|---------------------------------------------------------------------------------------------|-------------------------|
| `spring.ldap.urls`          | `SPRING_LDAP_URLS`          | Specifies the LDAP server URL                                                               |                         |
| `spring.ldap.username`      | `SPRING_LDAP_USERNAME`      | Specifies the LDAP connection username                                                      |                         |
| `spring.ldap.password`      | `SPRING_LDAP_PASSWORD`      | Specifies the LDAP connection password                                                      |                         |
| `spring.ldap.base`          | `SPRING_LDAP_BASE`          | Specifies the base LDAP DN for searches                                                     |                         |
| `ldap.email-attribute`      | `LDAP_EMAIL_ATTRIBUTE`      | Specifies the LDAP email attribute                                                          | `mail`                  |
| `ldap.first-name-attribute` | `LDAP_FIRST_NAME_ATTRIBUTE` | Specifies the LDAP first name attribute                                                     | `givenName`             |
| `ldap.group-search-base`    | `LDAP_GROUP_SEARCH_BASE`    | Specifies the base DN for LDAP group search                                                 |                         |
| `ldap.group-search-filter`  | `LDAP_GROUP_SEARCH_FILTER`  | Specifies the LDAP group search filter                                                      |                         |
| `ldap.id-attribute`         | `LDAP_ID_ATTRIBUTE`         | Specifies the LDAP identifier attribute                                                     | `cn`                    |
| `ldap.last-name-attribute`  | `LDAP_LAST_NAME_ATTRIBUTE`  | Specifies the LDAP last name attribute                                                      | `sn`                    |
| `ldap.password-attribute`   | `LDAP_PASSWORD_ATTRIBUTE`   | Specifies the LDAP password attribute                                                       | `userPassword`          |
| `ldap.user-search-base`     | `LDAP_USER_SEARCH_BASE`     | Specifies the base DN for LDAP user search                                                  |                         |
| `ldap.user-search-filter`   | `LDAP_USER_SEARCH_FILTER`   | Specifies the LDAP user search filter                                                       | `(cn={0})`              |
| `sendgrid.api-key`          | `SENDGRID_API_KEY`          | Specifies the SendGrid API key                                                              | `SG.xxxxx`              |
| `sendgrid.from`             | `SENDGRID_FROM`             | Specifies the default sender email address for SendGrid                                     | `noreply@example.com`   |
| `sendgrid.url`              | `SENDGRID_URL`              | Specifies the base URL for the SendGrid API. Use `https://api.sendgrid.com` for production. | `http://127.0.0.1:3030` |
| `ssp.external-url`          | `SSP_EXTERNAL_URL`          | Specifies the external URL for the LDAP Self Service Portal application                     | `http://localhost:8080` |

## License

This project is licensed under
the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).