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

import java.util.List;

import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * Interface for authentication.
 * 
 * @author Franz-Josef Elmer
 */
public interface IAuthenticationService extends ISelfTestable
{
    /**
     * Returns <code>true</code> if this authentication service is configured.
     */
    public boolean isConfigured();

    /**
     * Attempts authentication for the given user credentials.
     * 
     * @return <code>true</code> if the <var>user</var> has been successfully authenticated.
     */
    public boolean authenticateUser(String user, String password);

    /**
     * Returns the user details for the given <var>userId</var>, optionally trying to authenticating the user with the given
     * <var>passwordOrNull</var>.
     * 
     * @param user The user id to get the details for.
     * @param passwordOrNull The password to use for the authentication request. If <code>null</code>, the user will not be authenticated.
     * @return The Principal object, if a user with this <var>userId</var> exist, <code>null</code> otherwise. You can check with
     *         {@link Principal#isAuthenticated()} or {@link Principal#isAuthenticated(Principal)} whether the authentication request has been
     *         successful.
     */
    public Principal tryGetAndAuthenticateUser(String user, String passwordOrNull);

    /**
     * For a given user name returns additional details encapsulated in returned <code>Principal</code>.
     * 
     * @return The <code>Principal</code> object for the given <var>user</var>.
     * @throws IllegalArgumentException If the <var>user</var> cannot be found.
     */
    public Principal getPrincipal(String user) throws IllegalArgumentException;

    /**
     * Returns <code>true</code> if this authentication service supports authenticating users by their email address.
     */
    public boolean supportsAuthenticatingByEmail();

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by user id.
     * <p>
     * Note that this does not refer to the methods that return only one principal like {@link #getPrincipal(String)} or
     * {@link #tryGetAndAuthenticateUser(String, String)}.
     */
    public boolean supportsListingByUserId();

    /**
     * Returns a list of all users that match the <var>userIdQuery</var>.
     * 
     * @param userIdQuery The query for user ids to list. As user ids are unique, it can only ever return more than one user if it contains one or
     *            more wildcard characters ( <code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     */
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
            throws IllegalArgumentException;

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by email address.
     * <p>
     * Note that this also refers to the method {@link #tryGetAndAuthenticateUserByEmail(String, String)}.
     */
    public boolean supportsListingByEmail();

    /**
     * Returns the user details for the given <var>email</var>, optionally trying to authenticating the user with the given <var>passwordOrNull</var>.
     * <p>
     * <b>Note: if multiple users with this email address exist in the authentication repository, the first one regarding an arbitrary (repository
     * determined) order will be returned.</b>
     * <p>
     * <i>Only available, if {@link #supportsListingByEmail()} returns <code>true</code>, otherwise will throw an
     * {@link UnsupportedOperationException}.</i>
     * 
     * @param email The email of the user to get the details for.
     * @param passwordOrNull The password to use for the authentication request. If <code>null</code>, the user will not be authenticated.
     * @return The Principal object, if a user with this <var>email</var> exist, <code>null</code> otherwise. You can check with
     *         {@link Principal#isAuthenticated()} or {@link Principal#isAuthenticated(Principal)} whether the authentication request has been
     *         successful.
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     */
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull);

    /**
     * Returns a list of all users that match the <var>emailQuery</var>.
     * 
     * @param emailQuery The query for email addresses to list. May contain one or more wildcard characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     */
    public List<Principal> listPrincipalsByEmail(String emailQuery) throws IllegalArgumentException;

    /**
     * Returns <code>true</code> if this authentication service supports listing of principals by last name.
     */
    public boolean supportsListingByLastName();

    /**
     * Returns a list of all users that match the <var>lastNameQuery</var>.
     * 
     * @param lastNameQuery The query for last names to list. May contain one or more wildcard characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     */
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
            throws IllegalArgumentException;

    //
    // Deprecated methods
    //

    /**
     * Dummy operation, kept for backward compatibility. Don't use.
     * 
     * @deprecated This is a no-op.
     */
    @Deprecated
    public String authenticateApplication();

    /**
     * Attempts authentication for the given user credentials.
     * 
     * @return <code>true</code> if the <var>user</var> has been successfully authenticated.
     * @deprecated Use {@link #authenticateUser(String, String)}
     */
    @Deprecated
    public boolean authenticateUser(String dummyToken, String user, String password);

    /**
     * Returns the user details for the given <var>userId</var>, optionally trying to authenticating the user with the given
     * <var>passwordOrNull</var>.
     * 
     * @param dummyToken Some string that is ignored. Can be <code>null</code>.
     * @param user The user id to get the details for.
     * @param passwordOrNull The password to use for the authentication request. If <code>null</code>, the user will not be authenticated.
     * @return The Principal object, if a user with this <var>userId</var> exist, <code>null</code> otherwise. You can check with
     *         {@link Principal#isAuthenticated()} or {@link Principal#isAuthenticated(Principal)} whether the authentication request has been
     *         successful.
     * @deprecated Use {@link #tryGetAndAuthenticateUser(String, String)}
     */
    @Deprecated
    public Principal tryGetAndAuthenticateUser(String dummyToken, String user, String passwordOrNull);

    /**
     * For a given user name returns additional details encapsulated in returned <code>Principal</code>.
     * 
     * @return The <code>Principal</code> object for the given <var>user</var>.
     * @throws IllegalArgumentException If the <var>user</var> cannot be found.
     * @deprecated Use {@link #getPrincipal(String)}
     */
    @Deprecated
    public Principal getPrincipal(String dummyToken, String user) throws IllegalArgumentException;

    /**
     * Returns a list of all users that match the <var>userIdQuery</var>.
     * 
     * @param dummyToken Some string that is ignored. Can be <code>null</code>.
     * @param userIdQuery The query for user ids to list. As user ids are unique, it can only ever return more than one user if it contains one or
     *            more wildcard characters ( <code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     * @deprecated Use {@link #listPrincipalsByUserId(String)}
     */
    @Deprecated
    public List<Principal> listPrincipalsByUserId(String dummyToken, String userIdQuery)
            throws IllegalArgumentException;

    /**
     * Returns the user details for the given <var>email</var>, optionally trying to authenticating the user with the given <var>passwordOrNull</var>.
     * <p>
     * <b>Note: if multiple users with this email address exist in the authentication repository, the first one regarding an arbitrary (repository
     * determined) order will be returned.</b>
     * 
     * @param dummyToken Some string that is ignored. Can be <code>null</code>.
     * @param email The email of the user to get the details for.
     * @param passwordOrNull The password to use for the authentication request. If <code>null</code>, the user will not be authenticated.
     * @return The Principal object, if a user with this <var>email</var> exist, <code>null</code> otherwise. You can check with
     *         {@link Principal#isAuthenticated()} or {@link Principal#isAuthenticated(Principal)} whether the authentication request has been
     *         successful.
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     * @throws IllegalArgumentException If the <var>applicationToken</var> is invalid.
     * @deprecated Use {@link #tryGetAndAuthenticateUserByEmail(String, String)}
     */
    @Deprecated
    public Principal tryGetAndAuthenticateUserByEmail(String dummyToken, String email,
            String passwordOrNull);

    /**
     * Returns a list of all users that match the <var>emailQuery</var>.
     * 
     * @param dummyToken Some string that is ignored. Can be <code>null</code>.
     * @param emailQuery The query for email addresses to list. May contain one or more wildcard characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     * @throws IllegalArgumentException If the <var>applicationToken</var> is invalid.
     * @deprecated Use {@link #listPrincipalsByEmail(String)}
     */
    @Deprecated
    public List<Principal> listPrincipalsByEmail(String dummyToken, String emailQuery)
            throws IllegalArgumentException;

    /**
     * Returns a list of all users that match the <var>lastNameQuery</var>.
     * 
     * @param dummyToken Some string that is ignored. Can be <code>null</code>.
     * @param lastNameQuery The query for last names to list. May contain one or more wildcard characters (<code>*</code>).
     * @throws UnsupportedOperationException if this authentication service does not support this operation.
     * @throws IllegalArgumentException If the <var>applicationToken</var> is invalid.
     * @deprecated {@link #listPrincipalsByLastName(String)}
     */
    @Deprecated
    public List<Principal> listPrincipalsByLastName(String dummyToken, String lastNameQuery)
            throws IllegalArgumentException;

}