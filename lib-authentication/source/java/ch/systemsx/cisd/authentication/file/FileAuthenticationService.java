/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.file;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.file.LineBasedUserStore.IUserEntryFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * An implementation of {@link IAuthenticationService} that gets the authentication information from a password store (which is usually backed by a
 * file).
 * <p>
 * The file contains:
 * <ul>
 * <li><code>user_id</code></li>
 * <li><code>email</code></li>
 * <li><code>first_name</code></li>
 * <li><code>last_name</code></li>
 * <li><code>password</code></li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
public class FileAuthenticationService implements IAuthenticationService
{
    private static final String DUMMY_TOKEN_STR = "DUMMY-TOKEN";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileAuthenticationService.class);

    private final IUserStore<UserEntry> userStore;

    private final IAuthenticationService listingServiceOrNull;

    /**
     * Returns a "standard" line-based user store for {@link UserEntry}s.
     */
    static IUserStore<UserEntry> createUserStore(final String passwordFileName)
    {
        if (StringUtils.isBlank(passwordFileName))
        {
            return null;
        }
        return createUserStore(new File(passwordFileName));
    }

    /**
     * Returns a "standard" line-based user store for {@link UserEntry}s.
     */
    static IUserStore<UserEntry> createUserStore(final File passwordFile)
    {
        if (passwordFile == null)
        {
            return null;
        }
        final ILineStore lineStore = new FileBasedLineStore(passwordFile, "Password file");
        return createUserStore(lineStore);
    }

    /**
     * For unit tests.
     */
    static IUserStore<UserEntry> createUserStore(final ILineStore lineStore)
    {
        return new LineBasedUserStore<UserEntry>(lineStore, new IUserEntryFactory<UserEntry>()
            {
                @Override
                public UserEntry create(String line)
                {
                    return new UserEntry(line);
                }
            });
    }

    public FileAuthenticationService(final String passwordFileName)
    {
        this(createUserStore(passwordFileName), null);
    }

    public FileAuthenticationService(IUserStore<UserEntry> userStore,
            IAuthenticationService listingServiceOrNull)
    {
        this.userStore = userStore;
        this.listingServiceOrNull = listingServiceOrNull;
    }

    /**
     * Returns the id of the password store, which we consider to be the token.
     */
    @Override
    public String authenticateApplication()
    {
        return DUMMY_TOKEN_STR;
    }

    @Override
    public boolean authenticateUser(String dummyToken, String user, String password)
    {
        return authenticateUser(user, password);
    }

    @Override
    public boolean authenticateUser(String userId, String password)
    {
        final boolean authenticated = userStore.isPasswordCorrect(userId, password);
        logAuthentication(userId, authenticated);
        return authenticated;
    }

    private void logAuthentication(final String user, final boolean authenticated)
    {
        if (operationLog.isInfoEnabled())
        {
            final String msg = "FILE: authentication of user '" + user + "': ";
            operationLog.info(msg + (authenticated ? "SUCCESS." : "FAILED."));
        }
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String dummyToken, String user,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(user, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String userId,
            String passwordOrNull)
    {
        final Principal principal =
                toPrincipal(userStore.tryGetAndAuthenticateUserById(userId, passwordOrNull));
        logAuthentication(userId, Principal.isAuthenticated(principal));
        return principal;
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String applicationToken, String email,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        final Principal principal =
                toPrincipal(userStore.tryGetAndAuthenticateUserByEmail(email, passwordOrNull));
        final String user = (principal != null) ? principal.getUserId() : "email:" + email;
        logAuthentication(user, Principal.isAuthenticated(principal));
        return principal;
    }

    static Principal toPrincipal(UserEntryAuthenticationState<? extends UserEntry> entryOrNull)
    {
        if (entryOrNull == null)
        {
            return null;
        }
        final UserEntry user = entryOrNull.getUserEntry();
        final Principal principal = user.asPrincipal();
        principal.setAuthenticated(entryOrNull.isAuthenticated());
        return principal;
    }

    static Principal toPrincipal(UserEntry userOrNull)
    {
        if (userOrNull == null)
        {
            return null;
        }
        return userOrNull.asPrincipal();
    }

    @Override
    public Principal getPrincipal(String applicationToken, String userId)
    {
        return getPrincipal(userId);
    }

    @Override
    public Principal getPrincipal(String userId)
    {
        final Principal principalOrNull = toPrincipal(userStore.tryGetUserById(userId));
        if (principalOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + userId + "'.");
        }
        return principalOrNull;
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String applicationToken, String emailQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByEmail(emailQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByEmail(emailQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String applicationToken, String lastNameQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByLastName(lastNameQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByLastName(lastNameQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String dummyToken, String userIdQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByUserId(userIdQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
    {
        if (listingServiceOrNull != null)
        {
            return listingServiceOrNull.listPrincipalsByUserId(userIdQuery);
        } else
        {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return (listingServiceOrNull != null)
                && listingServiceOrNull.supportsListingByEmail();
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return (listingServiceOrNull != null) && listingServiceOrNull.supportsListingByLastName();
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return (listingServiceOrNull != null) && listingServiceOrNull.supportsListingByUserId();
    }

    @Override
    public boolean supportsAuthenticatingByEmail()
    {
        return true;
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        userStore.check();
        if (listingServiceOrNull != null)
        {
            listingServiceOrNull.check();
        }
    }

    @Override
    public boolean isRemote()
    {
        return (listingServiceOrNull != null) && listingServiceOrNull.isRemote();
    }

    @Override
    public boolean isConfigured()
    {
        return userStore != null;
    }

}
