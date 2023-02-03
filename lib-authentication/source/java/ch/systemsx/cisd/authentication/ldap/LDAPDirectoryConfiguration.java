/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.authentication.ldap;

import javax.naming.Context;

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * The configuration for an LDAP directory server. Example:
 * 
 * <pre>
 *    final LDAPDirectoryConfiguration config = new LDAPDirectoryConfiguration();
 *    config.setServerUrl("ldap://d.ethz.ch:636/DC=d,DC=ethz,DC=ch");
 *    config.setSecurityPrincipalTemplate("CN=%s,OU=EthUsers,DC=d,DC=ethz,DC=ch");
 *    config.setUserId(&lt;user&gt;);
 *    config.setPassword(&lt;password&gt;);
 *    final LDAPPrincipalQuery query = new LDAPPrincipalQuery(config);
 * </pre>
 * 
 * @author Bernd Rinn
 */
public final class LDAPDirectoryConfiguration
{

    static final String DEFAULT_QUERY_TEMPLATE =
            "(&(objectClass=organizationalPerson)(objectCategory=person)"
                    + "(objectClass=user)(%s))";

    private String userIdAttributeName = "uid";

    private String lastNameAttributeName = "sn";

    private String firstNameAttributeName = "givenName";

    private String emailAttributeName = "mail";

    private String emailAliasesAttributeName = "proxyAddresses";

    private String emailAttributePrefix = "smtp:";

    private String queryEmailForAliases = "false";

    private String securityProtocol = "ssl";

    private String securityAuthenticationMethod = "simple";

    private String referral = "follow";

    private long timeout = 10000L;

    private int maxRetries = 1;

    private long timeToWaitAfterFailure = 10000L;

    private String queryTemplate =
            DEFAULT_QUERY_TEMPLATE;

    private String securityPrincipalDistinguishedName;

    private String serverUrl;

    private String securityPrincipalPassword;

    private String searchBase = "";

    /**
     * Returns <code>true</code> if this configuration is complete.
     */
    public boolean isConfigured()
    {
        return StringUtils.isNotBlank(serverUrl)
                && StringUtils.isNotBlank(securityPrincipalDistinguishedName)
                && StringUtils.isNotBlank(securityPrincipalPassword);
    }

    /**
     * Default value: <code>uid</code>
     */
    public String getUserIdAttributeName()
    {
        return userIdAttributeName;
    }

    public void setUserIdAttributeName(String userIdAttributeName)
    {
        if (isResolved(userIdAttributeName))
        {
            this.userIdAttributeName = userIdAttributeName;
        }
    }

    /**
     * Default value: <code>sn</code>
     */
    public String getLastNameAttributeName()
    {
        return lastNameAttributeName;
    }

    public void setLastNameAttributeName(String lastNameAttributeName)
    {
        if (isResolved(lastNameAttributeName))
        {
            this.lastNameAttributeName = lastNameAttributeName;
        }
    }

    /**
     * Default value: <code>givenName</code>
     */
    public String getFirstNameAttributeName()
    {
        return firstNameAttributeName;
    }

    public void setFirstNameAttributeName(String firstNameAttributeName)
    {
        if (isResolved(firstNameAttributeName))
        {
            this.firstNameAttributeName = firstNameAttributeName;
        }
    }

    /**
     * Default value: <code>mail</code>
     */
    public String getEmailAttributeName()
    {
        return emailAttributeName;
    }

    public void setEmailAttributeName(String emailAttributeName)
    {
        if (isResolved(emailAttributeName))
        {
            this.emailAttributeName = emailAttributeName;
        }
    }

    public String getEmailAliasesAttributeName()
    {
        return emailAliasesAttributeName;
    }

    /**
     * Default value: <code>proxyAddresses</code>
     */
    public void setEmailAliasesAttributeName(String emailAliasesAttributeName)
    {
        if (isResolved(emailAliasesAttributeName))
        {
            this.emailAliasesAttributeName = emailAliasesAttributeName;
        }
    }

    public String getQueryEmailForAliases()
    {
        return queryEmailForAliases;
    }

    /**
     * If the query for emails should use the email aliases instead of the canonical email addresses. Default: <code>false</code>.
     */
    public void setQueryEmailForAliases(String queryEmailForAliases)
    {
        if (isResolved(queryEmailForAliases))
        {
            this.queryEmailForAliases = queryEmailForAliases;
        }
    }

    public String getEmailAttributePrefix()
    {
        return emailAttributePrefix;
    }

    /**
     * The prefix of email values that is used when doing a search.
     * <p>
     * Default value: <code>smtp:</code>
     */
    public void setEmailAttributePrefix(String emailAttributePrefix)
    {
        if (isResolved(emailAttributePrefix))
        {
            this.emailAttributePrefix = emailAttributePrefix;
        }
    }

    /**
     * Default value: <code>ssl</code>
     * 
     * @see Context#SECURITY_PROTOCOL
     */
    public String getSecurityProtocol()
    {
        return securityProtocol;
    }

    /**
     * Use <code>ssl</code> or <code>none</code>.
     * 
     * @see Context#SECURITY_PROTOCOL
     */
    public void setSecurityProtocol(String securityProtocol)
    {
        if (isResolved(securityProtocol))
        {
            this.securityProtocol = securityProtocol;
        }
    }

    /**
     * @see Context#SECURITY_PRINCIPAL
     */
    public String getSecurityPrincipalDistinguishedName()
    {
        return securityPrincipalDistinguishedName;
    }

    /**
     * The distinguished name for login of the application to the LDAP server.
     * <p>
     * Example: <code>CN=carl,OU=EthUsers,DC=d,DC=ethz,DC=ch</code>.
     * <p>
     * <strong>Mandatory.</strong>
     * 
     * @see Context#SECURITY_PRINCIPAL
     */
    public void setSecurityPrincipalDistinguishedName(String securityPrincipal)
    {
        this.securityPrincipalDistinguishedName = securityPrincipal;
    }

    public String getSecurityPrincipalPassword()
    {
        return securityPrincipalPassword;
    }

    /**
     * The password for login of the application to the LDAP server.
     * <p>
     * <strong>Mandatory.</strong>
     */
    public void setSecurityPrincipalPassword(String password)
    {
        this.securityPrincipalPassword = password;
    }

    /**
     * Default value: <code>simple</code>
     * 
     * @see Context#SECURITY_AUTHENTICATION
     */
    public String getSecurityAuthenticationMethod()
    {
        return securityAuthenticationMethod;
    }

    /**
     * @see Context#SECURITY_AUTHENTICATION
     */
    public void setSecurityAuthenticationMethod(String securityAuthenticationMethod)
    {
        if (isResolved(securityAuthenticationMethod))
        {
            this.securityAuthenticationMethod = securityAuthenticationMethod;
        }
    }

    /**
     * Default value: <code>follow</code>
     * 
     * @see Context#REFERRAL
     */
    public String getReferral()
    {
        return referral;
    }

    /**
     * @see Context#REFERRAL
     */
    public void setReferral(String referral)
    {
        if (isResolved(referral))
        {
            this.referral = referral;
        }
    }

    /**
     * Default value: <code>(&(objectClass=organizationalPerson)(objectCategory=person)(objectClass=user)(%s))</code>
     */
    public String getQueryTemplate()
    {
        return queryTemplate;
    }

    /**
     * Needs to contain exactly one '<code>%s</code>' which will be replaced with the filter condition, e.g. '<code>uid=mueller</code>'.
     */
    public void setQueryTemplate(String queryTemplate)
    {
        if (isResolved(queryTemplate))
        {
            this.queryTemplate = queryTemplate;
        }
    }
    
    public String getSearchBase()
    {
        return searchBase;
    }
    
    public void setSearchBase(String searchBase)
    {
        if (isResolved(searchBase))
        {
            this.searchBase = searchBase;
        }
    }

    /**
     * The read timeout (in s). Default value: <code>10s</code>
     */
    public String getTimeoutStr()
    {
        return Long.toString(timeout / 1000);
    }

    /**
     * Returns the timeout (in ms)
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * Set the read timeout as String in a format understood by {@link DateTimeUtils#parseDurationToMillis(String)}.
     */
    public void setTimeoutStr(String timeoutStr)
    {
        if (isResolved(timeoutStr))
        {
            this.timeout = DateTimeUtils.parseDurationToMillis(timeoutStr);
        }
    }

    /**
     * The time to wait after failure before retrying (in ms). Default value: <code>10000</code> (10s)
     */
    public long getTimeToWaitAfterFailure()
    {
        return timeToWaitAfterFailure;
    }

    /**
     * The time to wait after failure before retrying (in s). Default value: <code>10</code>
     */
    public String getTimeToWaitAfterFailureStr()
    {
        return Long.toString(timeToWaitAfterFailure / 1000);
    }

    /**
     * Set the time to wait after failure before retrying as a String in a format understood by {@link DateTimeUtils#parseDurationToMillis(String)}.
     */
    public void setTimeToWaitAfterFailureStr(String timeToWaitOnFailureStr)
    {
        if (isResolved(timeToWaitOnFailureStr))
        {
            this.timeToWaitAfterFailure =
                    DateTimeUtils.parseDurationToMillis(timeToWaitOnFailureStr);
        }
    }

    /**
     * The maximum number of times a failed query is retried. Default value: <code>9</code>
     */
    public int getMaxRetries()
    {
        return maxRetries;
    }

    /**
     * The maximum number of times a failed query is retried. Default value: <code>9</code>
     */
    public String getMaxRetriesStr()
    {
        return Integer.toString(maxRetries);
    }

    /**
     * Sets the maximum number of times a failed query is retried.
     */
    public void setMaxRetriesStr(String maxRetries)
    {
        if (isResolved(maxRetries))
        {
            this.maxRetries = Integer.parseInt(maxRetries);
        }
    }

    public String getServerUrl()
    {
        return serverUrl;
    }

    /**
     * Example: <code>ldap://d.ethz.ch:636/DC=d,DC=ethz,DC=ch</code>
     * <p>
     * <strong>Mandatory.</strong>
     */
    public void setServerUrl(String ldapUrl)
    {
        if (isResolved(ldapUrl))
        {
            this.serverUrl = ldapUrl;
        }
    }

    private static boolean isResolved(String name)
    {
        return StringUtils.isNotBlank(name) && name.startsWith("${") == false;
    }

}
