Authentication Systems
======================

Generic openBIS currently supports four authentication systems: a
self-contained system based on a UNIX-like passwd file, LDAP, and Single Sign
On (e.g., through SWITCHaai). Beside this there are also so called stacked
authentication methods available. Stacked authentication methods use
multiple authentication systems in the order indicated by the name. The
first authentication system being able to provide an entry for a
particular user id will be used. If you need full control over what
authentication systems are used in what order, you can define your own
stacked authentication service in the Spring application context file:
`$INSTALL_PATH/servers/openBIS-server/jetty/webapps/openbis/WEB-INF/classes/genericCommonContext.xml.`


## The default authentication configuration

In the template service properties, we set
`authentication-service = file-ldap-caching-authentication-service`,
which means that file-based authentication, and LDAP are used for
authentication, in this order. As LDAP are not configured in
the template service properties, this effectively corresponds
to `file-authentication-service`, however when LDAP is
configured, they are picked up on server start and are used to
authenticate users when they are not found in the local `passwd` file.
Furthermore, as it is a caching authentication service, it will cache
authentication results from LDAP in file `$INSTALL_PATH/servers/openBIS-server/jetty/etc/passwd_cache`. See section
*Authentication Cache* below for details on this caching.


## The file based authentication system

This authentication schema uses the file
`$INSTALL_PATH/servers/openBIS-server/jetty/etc/passwd` to determine whether a login to the
system is successful or not.

The script `$INSTALL_PATH/servers/openBIS-server/jetty/bin/passwd.sh` can be used to maintain
this file. This script supports the options:

```bash
passwd list | [remove|show|test] <userid> | [add|change] <userid> [option [...]]
    --help                 : Prints out a description of the options.
    [-P,--change-password] : Read the new password from the console,
    [-e,--email] VAL       : Email address of the user.
    [-f,--first-name] VAL  : First name of the user.
    [-l,--last-name] VAL   : Last name of the user.
    [-p,--password] VAL    : The password.
```

A new user can be added with

```bash
prompt> passwd.sh add [-f <first name>] [-l <last name>] [-e <email>] [-p <password>] <username>
```

If no password is provided with the `-p` option, the system will ask for
a password of the new user on the console. Please note that providing a
password on the command line can be a security risk, because the
password can be found in the shell history, and, for a short time, in
the `ps` table. Thus `-p` is not recommended in normal operation.

The password of a user can be tested with

```bash
prompt> passwd.sh test <username>
```

The system will ask for the current password on the console and then
print whether the user was authenticated successfully or not.

An account can be changed with

```bash
prompt> passwd.sh change [-f <first name>] [-l <last name>] [-e <email>] [-P] <username>
```

An account can be removed with

```bash
prompt> passwd.sh remove <username>
```

The details of an account can be queried with

```bash
prompt> passwd.sh show <username>
```

All accounts can be listed with

```bash
prompt> passwd.sh list
```

The password file contains each user in a separate line. The fields of
each line are separated by colon and contain (in this order): *User Id*,
*Email Address*, *First Name*, *Last Name* and *Password Hash*.
The *Password Hash* field represents the
[salted](http://en.wikipedia.org/wiki/Salted_hash)
[SHA1](http://en.wikipedia.org/wiki/Sha1) hash of the user's password in
[BASE64 encoding](http://en.wikipedia.org/wiki/Base64).


## The interface to LDAP

To work with an LDAP server, you need to provide the server URL with
(example) and set the
`authentication-service = ldap-authentication-service`

```bash
ldap.server.url = ldap://d.ethz.ch/DC=d,DC=ethz,DC=ch
```

and the details of an LDAP account who is allowed to make queries on the
LDAP server with (example)

```bash
ldap.security.principal.distinguished.name = CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch
ldap.security.principal.password = Carls_LDAP_Password
```

Note: A space-separated list of URLs can be provided if distinguished
name and password  are valid for all specified LDAP servers.


## Authentication Cache

If configuring a caching authentication service like
`file-ldap-caching-authentication-service`, authentication results
from remote authentication services like LDAP are cached
locally in the openBIS Application Server. The advantage is a faster
login time on repeated logins when one or more remote authentication
services are slow. The disadvantage is that changes to data in the
remote authentication system (like a changed password or email address)
are becoming known to openBIS only with a delay. In order to minimize
this effect, the authentication caching performs "re-validation" of
authentication requests asynchronously. That means it doesn't block the
user from logging in because it is performed in different thread than
the login.

There are two service properties which give you control over the working
of the authentication cache:

- `authentication.cache.time` lets you set for how long (after putting
    it into the cache) a cache entry (read: "user name and password")
    will be kept if the user does not have a successful login to openBIS
    in this period of time (as successful logins will trigger
    re-validation and thus renewal of the cache entry). The default is
    28h, which means that users logging into the system every day will
    never experience a delay from slow remote authentication systems. A
    non-positive value will disable caching.
- `authentication.cache.time-no-revalidation` lets you set for how
    long (after putting it into the cache) a cache entry will *not* be
    re-validated if the login was successful. This allows you to reduce
    the load that openBIS creates on the remote authentication servers
    for successful logins of the same user. The default is 1h. Setting
    it to 0 will always trigger re-validation, setting it to
    `authentication.cache.time` will not perform re-validation at all
    and thus expire every cache entry after that time.

An administrator with shell access to the openBIS Application Server can
see and change the current cache entries in the
file `$INSTALL_PATH/servers/openBIS-server/jetty/etc/passwd_cache`. The format is the same
as for the file-based authentication system (see section *The file based
authentication system* above), but has an additional field *Cached At*
added to the end of each line. *Cached At* is the time (in milli-seconds
since start of the Unix epoch, which is midnight *Universal Time
Coordinated*, 1 January 1970) when the entry was cached. Removing a line
from this file will remove the corresponding cache entry. The
authentication cash survives openBIS Application Server restarts because
of this persisted file. If you need to clear the cache altogether, it is
sufficient to remove the `passwd_cache` file at any time. No server
restart is needed to make changes to this file take effect.

You can switch off authentication caching by either
setting `authentication.cache.time = -1`, or by choosing an
authentication service which does not have `caching` in its name.


## Anonymous Login

In order to allow anonymous login a certain user known by openBIS (not
necessarily by the authentication system) has to be specified. This is
done by the property `user-for-anonymous-login`. The value is the user
ID. The display settings and the authorization settings of this user are
used for the anonymous login.

Anonymous login is possible with URL parameter `anonymous` set to `true`
or by property `default-anonymous-login` in web configuration properties
(see [Web Client Customization](./installation-and-configuration-guide.md#web-client-customizations)). Note, that for the ELN client the property `default-anonymous-login` isn't used. Anonymous login needs only the property `user-for-anonymous-login` for an existing user with some rights.


## Single Sign On Authentication

Currently only Shibboleth SSO is supported. For more details see [Single Sign On Authentication](./installation-and-configuration-guide.md#single-sign-on-authentication).