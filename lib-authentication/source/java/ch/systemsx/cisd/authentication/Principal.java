/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.systemsx.cisd.common.reflection.AbstractHashable;

/**
 * This class defines in its constructor minimum information that we must know about a <code>Principal</code>.
 * <p>
 * It is also possible to put additional <code>Object</code> properties related to this <code>Principal</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class Principal extends AbstractHashable implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private boolean authenticated;

    private boolean anonymous;

    private Map<String, String> properties;

    /**
     * Returns <code>true</code>, if <var>principalOrNull</var> is not <code>null</code> and has been successfully authenticated.
     */
    public static boolean isAuthenticated(Principal principalOrNull)
    {
        return (principalOrNull == null) ? false : principalOrNull.isAuthenticated();
    }

    /**
     * For bean operations only.
     */
    public Principal()
    {
    }

    /**
     * Constructor which accepts mandatory parameters but no properties
     * 
     * @param userId Must not be <code>null</code>.
     * @param firstName can not be <code>null</code>.
     * @param lastName can not be <code>null</code>.
     * @param email can not be <code>null</code>.
     */
    public Principal(final String userId, final String firstName, final String lastName,
            final String email)
    {
        this(userId, firstName, lastName, email, false, new HashMap<String, String>());
    }

    /**
     * Constructor which accepts mandatory parameters but no properties
     * 
     * @param userId Must not be <code>null</code>.
     * @param firstName can not be <code>null</code>.
     * @param lastName can not be <code>null</code>.
     * @param email can not be <code>null</code>.
     * @param authenticated <code>true</code> if this principal has been authenticated in the operation.
     */
    public Principal(final String userId, final String firstName, final String lastName,
            final String email, boolean authenticated)
    {
        this(userId, firstName, lastName, email, authenticated, new HashMap<String, String>());
    }

    /**
     * Standard constructor which accepts mandatory parameters and properties.
     * 
     * @param userId Must not be <code>null</code>.
     * @param firstName can not be <code>null</code>.
     * @param lastName can not be <code>null</code>.
     * @param email can not be <code>null</code>.
     * @param authenticated <code>true</code> if this principal has been authenticated in the operation.
     * @param properties can not be <code>null</code>.
     */
    public Principal(final String userId, final String firstName, final String lastName,
            final String email, boolean authenticated, final Map<String, String> properties)
    {
        assert userId != null;
        assert firstName != null;
        assert lastName != null;
        assert email != null;
        assert properties != null;

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.authenticated = authenticated;
        this.properties = properties;
    }

    /**
     * Returns the id of the user.
     */
    public String getUserId()
    {
        return userId;
    }

    /**
     * Returns <code>email</code>.
     */
    public final String getEmail()
    {
        return email;
    }

    /**
     * Returns <code>firstName</code>.
     */
    public final String getFirstName()
    {
        return firstName;
    }

    /**
     * Returns <code>lastName</code>.
     */
    public final String getLastName()
    {
        return lastName;
    }

    /**
     * Returns the property for given <var>key</var>, or <code>null</code>, if no property exists for this <var>key</var>.
     */
    public final String getProperty(final String key)
    {
        return (properties != null) ? properties.get(key) : null;
    }

    /** Retuns an unmodifiable <code>Set</code> of present properties. */
    public final Set<String> getPropertyNames()
    {
        return Collections.unmodifiableSet(properties.keySet());
    }

    /**
     * Returns <code>true</code> if this principal has been successfully authenticated.
     */
    public boolean isAuthenticated()
    {
        return authenticated;
    }

    /**
     * Returns <code>true</code> if this principal has been authenticated anonymously.
     */
    public boolean isAnonymous()
    {
        return anonymous;
    }

    /**
     * Returns the property map for this principal.
     */
    public Map<String, String> getProperties()
    {
        return properties;
    }

    /**
     * For bean operations only.
     */
    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    /**
     * For bean operations only.
     */
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    /**
     * For bean operations only.
     */
    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * For bean operations only.
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * For bean operations only.
     */
    public void setAuthenticated(boolean authenticated)
    {
        this.authenticated = authenticated;
    }

    /**
     * For bean operations only.
     */
    public void setAnonymous(boolean anonymous)
    {
        this.anonymous = anonymous;
    }

    /**
     * For bean operations only.
     */
    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

}