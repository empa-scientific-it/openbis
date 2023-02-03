/*
 * Copyright 2013 ETH Zuerich, CISD
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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.file.LineBasedUserStore.IUserEntryFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * An {@link IAuthenticationService} that delegates to another {@link IAuthenticationService} and keeps the returned value for user authentication in
 * a local cache which is written out to a password cache file which will be used to populate the cache on restart, so the password cache survives
 * restarts. Changing the password cache file will change the cache without restart, e.g. deleting a line from it will remove the cache entry with
 * immediate effect.
 * <p>
 * In order to make caching as smooth as possible for regular users of the system, a authentication request which is served from the cache under some
 * conditions triggers a re-validation request with the delegate authentication service. As re-validation is done asynchronously, it does not block
 * the user from working.
 * <p>
 * Two configurable time periods (in milli-seconds) are relevant:
 * <ul>
 * <li><code>cacheTimeMillis</code> is the time period after caching that a cache entry is kept. Older cache entries are treated as invalid and
 * ignored. The default is 28 hours, which means that any user who logs into the system once a day will never have to wait for the delegate
 * authentication system to respond as all cache updates are performed in asynchronous re-validations.</li>
 * <li><code>cacheTimeNoRevalidationMillis</code> is the time period after caching in which successful authentication requests do not trigger a
 * re-validation request. The default is 1 hour. This feature is meant to reduce the load on the delegate authentication system. Set it to
 * <code>cacheTimeMillis</code> to never re-validate or set to 0 to always re-validate.</li>
 * </ul>
 * 
 * @author Bernd Rinn
 */
public class CachingAuthenticationService implements IAuthenticationService
{
    public final static long ONE_MINUTE = 60 * 1000L;

    public final static long ONE_HOUR = 60 * ONE_MINUTE;

    public final static long CACHE_TIME_MILLIS_NO_REVALIDATION = ONE_HOUR;

    public final static long CACHE_TIME_MILLIS = 28 * ONE_HOUR;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CachingAuthenticationService.class);

    private enum CacheEntryStatus
    {
        /** Entry OK without validation. */
        OK,
        /** Entry OK but should request re-validation. */
        OK_REVALIDATE,
        /** No entry or entry expired. */
        NO_ENTRY,
    }

    /** A request for validation of a user. */
    final class ValidationRequest
    {
        private final UserCacheEntry user;

        private final long queuedAt;

        private final String passwordOrNull;

        private final boolean authenticated;

        ValidationRequest(UserCacheEntry user, String passwordOrNull, boolean authenticated,
                long now)
        {
            this.user = user;
            this.passwordOrNull = passwordOrNull;
            this.authenticated = authenticated;
            this.queuedAt = now;
        }

        /**
         * Returns the user entry to re-validate.
         */
        UserCacheEntry getUser()
        {
            return user;
        }

        /**
         * Returns the password the user supplied, or <code>null</code>, if this is a non-authenticating validation request.
         */
        String tryGetPassword()
        {
            return passwordOrNull;
        }

        /**
         * Returns <code>true</code> if this request does not include authentication.
         */
        boolean withoutAuthentication()
        {
            return (passwordOrNull == null);
        }

        /**
         * Returns <code>true</code>, if the user has been successfully authenticated from the cached entry with the given password.
         */
        boolean isAuthenticated()
        {
            return authenticated;
        }

        /**
         * Returns <code>true</code> if this is a valid request at the time when this method is called.
         */
        boolean isValid()
        {
            final UserCacheEntry current = userStore.tryGetUserById(user.getUserId());
            // OK, if there is no cache entry.
            if (current == null)
            {
                return true;
            }
            // Not OK, if the request has no password but the cache entry has one.
            if (passwordOrNull == null && current.hasPassword())
            {
                return false;
            }
            // OK, if the request has a password but the cache entry has none.
            if (passwordOrNull != null && current.hasPassword() == false)
            {
                return true;
            }
            // OK, if the time of request queuing is after the time of caching.
            return (current.getCachedAt() < queuedAt);
        }
    }

    /**
     * The class that performs re-validation of users and their passwords.
     * 
     * @author Bernd Rinn
     */
    final class RevalidationRunnable implements Runnable
    {
        void runOnce() throws InterruptedException
        {
            final ValidationRequest request = validationQueue.take();
            if (request.isValid() == false)
            {
                return;
            }
            final String userId = request.getUser().getUserId();
            final Principal p =
                    delegate.tryGetAndAuthenticateUser(userId,
                            request.tryGetPassword());
            // If a user got remove from the delegate system, remove the cache entry.
            if (p == null)
            {
                userStore.removeUser(userId);
                if (request.isAuthenticated())
                {
                    operationLog
                            .warn(String.format("User '%s' has been logged in which is no "
                                    + "longer a valid user.", userId));
                }
                return;
            }
            // Only update the cache if no authentication was requested or if the
            // authentication was successful.
            if (p.isAuthenticated() || request.withoutAuthentication())
            {
                userStore.addOrUpdateUser(new UserCacheEntry(p, request
                        .tryGetPassword(), timeProvider.getTimeInMilliseconds()));
            }
            if (request.isAuthenticated() && p.isAuthenticated() == false)
            {
                userStore.removeUser(userId);
                operationLog.warn(String.format(
                        "User '%s' has been logged in with an outdated password.",
                        userId));
            }
        }

        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    runOnce();
                } catch (Throwable th)
                {
                    operationLog.error(
                            "Exception in " + CachingAuthenticationService.class.getSimpleName()
                                    + " Revalidator: ", th);
                }
            }
        }
    }

    /** A interface with one method to authenticate by one type of id (userid or email). */
    interface IAuthenticator
    {
        Principal tryGetAndAuthenticate(String id, String passwordOrNull);
    }

    private final IUserStore<UserCacheEntry> userStore;

    private final IAuthenticationService delegate;

    private final IAuthenticator userIdAuthenticator;

    private final IAuthenticator emailAuthenticator;

    private final long cacheTimeNoRevalidationMillis;

    private final long cacheTimeMillis;

    private final BlockingQueue<ValidationRequest> validationQueue;

    private final ITimeProvider timeProvider;

    private final boolean caching;

    public CachingAuthenticationService(IAuthenticationService delegate,
            String passwordCacheFileName)
    {
        this(delegate, createUserStore(passwordCacheFileName));
    }

    public CachingAuthenticationService(IAuthenticationService authenticationService,
            IUserStore<UserCacheEntry> store)
    {
        this(authenticationService, store, CACHE_TIME_MILLIS_NO_REVALIDATION,
                CACHE_TIME_MILLIS);
    }

    public CachingAuthenticationService(IAuthenticationService delegate,
            String passwordCacheFileName,
            long cacheTimeNoRevalidationMillis,
            long cacheTimeMillis)
    {
        this(delegate, createUserStore(passwordCacheFileName),
                cacheTimeNoRevalidationMillis, cacheTimeMillis);
    }

    public CachingAuthenticationService(IAuthenticationService delegate,
            IUserStore<UserCacheEntry> userStore,
            long cacheTimeNoRevalidationMillis,
            long cacheTimeMillis)
    {
        this(delegate, userStore, cacheTimeNoRevalidationMillis, cacheTimeMillis, true,
                SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }

    public CachingAuthenticationService(CachingAuthenticationConfiguration config)
    {
        this(config.getDelegate(), createUserStore(config.getPasswordCacheFile()), config
                .getCacheTimeNoRevalidation(), config.getCacheTime());
    }

    // For unit tests.
    CachingAuthenticationService(IAuthenticationService delegate,
            IUserStore<UserCacheEntry> userStore,
            long cacheTimeNoRevalidationMillis,
            long cacheTimeMillis, boolean startRevalidationThread, ITimeProvider timeProvider)
    {
        this.delegate = delegate;
        this.userIdAuthenticator = new IAuthenticator()
            {
                @Override
                public Principal tryGetAndAuthenticate(String userId, String passwordOrNull)
                {
                    return CachingAuthenticationService.this.delegate.tryGetAndAuthenticateUser(
                            userId,
                            passwordOrNull);
                }
            };
        this.emailAuthenticator = new IAuthenticator()
            {
                @Override
                public Principal tryGetAndAuthenticate(String email, String passwordOrNull)
                {
                    return CachingAuthenticationService.this.delegate
                            .tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
                }
            };
        this.userStore = userStore;
        this.cacheTimeNoRevalidationMillis =
                Math.min(cacheTimeNoRevalidationMillis, cacheTimeMillis);
        this.cacheTimeMillis = cacheTimeMillis;
        this.validationQueue = new LinkedBlockingQueue<ValidationRequest>();
        this.timeProvider = timeProvider;
        this.caching = (cacheTimeMillis > 0);
        if (startRevalidationThread && caching)
        {
            final Thread t = new Thread(new RevalidationRunnable());
            t.setName(getClass().getSimpleName() + " - Validator");
            t.setDaemon(true);
            t.start();
        }
        if (operationLog.isInfoEnabled())
        {
            if (caching)
            {
                operationLog.info(String.format(
                        "Caching authentication results for %s, revalidating after %s.",
                        DateTimeUtils.renderDuration(cacheTimeMillis),
                        DateTimeUtils.renderDuration(cacheTimeNoRevalidationMillis)));
            } else
            {
                operationLog.info("Authentication caching is switched off.");
            }
        }
    }

    static IUserStore<UserCacheEntry> createUserStore(
            final String passwordCacheFileName)
    {
        if (StringUtils.isBlank(passwordCacheFileName))
        {
            return null;
        }
        return createUserStore(new File(passwordCacheFileName));
    }

    static IUserStore<UserCacheEntry> createUserStore(
            final File passwordCacheFile)
    {
        if (passwordCacheFile == null)
        {
            return null;
        }
        final ILineStore lineStore =
                new FileBasedLineStore(passwordCacheFile, "Password cache file");
        return new LineBasedUserStore<UserCacheEntry>(lineStore,
                new IUserEntryFactory<UserCacheEntry>()
                    {
                        @Override
                        public UserCacheEntry create(String line)
                        {
                            return new UserCacheEntry(line);
                        }
                    });
    }

    // For unit tests.
    BlockingQueue<ValidationRequest> getValidationQueue()
    {
        return validationQueue;
    }

    @Override
    public boolean authenticateUser(String userId, String password)
    {
        return Principal.isAuthenticated(tryGetAndAuthenticateUser(userId, password,
                userIdAuthenticator, userStore.tryGetUserById(userId)));
    }

    private Principal tryGetAndAuthenticateUser(String id,
            String passwordOrNull, IAuthenticator auth, UserCacheEntry entry)
    {
        // Note: getStatus() returns NO_ENTRY on entry == null
        final boolean requiresAuthentication = StringUtils.isNotEmpty(passwordOrNull);
        final long now = timeProvider.getTimeInMilliseconds();
        final CacheEntryStatus state =
                getStatus(entry, requiresAuthentication, now);
        switch (state)
        {
            case OK:
            {
                final boolean authenticated = entry.isPasswordCorrect(passwordOrNull);
                if (authenticated == false && requiresAuthentication)
                {
                    validationQueue.offer(new ValidationRequest(entry, passwordOrNull, false, now));
                }
                return toPrincipal(entry, authenticated);
            }
            case OK_REVALIDATE:
            {
                final boolean authenticated = entry.isPasswordCorrect(passwordOrNull);
                validationQueue.offer(new ValidationRequest(entry, passwordOrNull, authenticated,
                        now));
                return toPrincipal(entry, authenticated);
            }
            case NO_ENTRY:
            {
                final Principal p = auth.tryGetAndAuthenticate(id, passwordOrNull);
                if (p == null)
                {
                    return null;
                }
                if (caching)
                {
                    final UserCacheEntry user =
                            new UserCacheEntry(p, passwordOrNull,
                                    timeProvider.getTimeInMilliseconds());
                    userStore.addOrUpdateUser(user);
                }
                return p;
            }
            default:
                throw new Error("Unknown cache entry state " + state);
        }
    }

    private static Principal toPrincipal(UserEntry userOrNull, boolean authenticated)
    {
        if (userOrNull == null)
        {
            return null;
        }
        final Principal principal = userOrNull.asPrincipal();
        principal.setAuthenticated(authenticated);
        return principal;
    }

    private CacheEntryStatus getStatus(UserCacheEntry entry, boolean requirePassword, long now)
    {
        if (entry == null || caching == false)
        {
            return CacheEntryStatus.NO_ENTRY;
        }
        if (requirePassword && entry.hasPassword() == false)
        {
            return CacheEntryStatus.NO_ENTRY;
        }
        final long cachedAt = entry.getCachedAt();
        if (cachedAt + cacheTimeNoRevalidationMillis >= now)
        {
            return CacheEntryStatus.OK;
        } else if (cachedAt + cacheTimeMillis >= now)
        {
            return CacheEntryStatus.OK_REVALIDATE;
        } else
        {
            // Treat an expired entry like a missing entry.
            return CacheEntryStatus.NO_ENTRY;
        }
    }

    @Override
    @Deprecated
    public boolean authenticateUser(String dummyToken, String userId, String password)
    {
        return authenticateUser(userId, password);
    }

    @Override
    public Principal tryGetAndAuthenticateUser(String userId, String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(userId, passwordOrNull, userIdAuthenticator,
                userStore.tryGetUserById(userId));
    }

    @Override
    @Deprecated
    public Principal tryGetAndAuthenticateUser(String dummyToken, String userId,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(userId, passwordOrNull);
    }

    @Override
    public Principal getPrincipal(String userId) throws IllegalArgumentException
    {
        final Principal principalOrNull =
                tryGetAndAuthenticateUser(userId, null, userIdAuthenticator,
                        userStore.tryGetUserById(userId));
        if (principalOrNull == null)
        {
            throw new IllegalArgumentException("Cannot find user '" + userId + "'.");
        }
        return principalOrNull;
    }

    @Override
    @Deprecated
    public Principal getPrincipal(String dummyToken, String userId) throws IllegalArgumentException
    {
        return getPrincipal(userId);
    }

    @Override
    public Principal tryGetAndAuthenticateUserByEmail(String email, String passwordOrNull)
    {
        return tryGetAndAuthenticateUser(email, passwordOrNull, emailAuthenticator,
                userStore.tryGetUserByEmail(email));
    }

    @Override
    @Deprecated
    public Principal tryGetAndAuthenticateUserByEmail(String dummyToken, String email,
            String passwordOrNull)
    {
        return tryGetAndAuthenticateUserByEmail(email, passwordOrNull);
    }

    @Override
    public boolean supportsAuthenticatingByEmail()
    {
        return delegate.supportsAuthenticatingByEmail();
    }

    @Override
    public boolean supportsListingByUserId()
    {
        return delegate.supportsListingByUserId();
    }

    @Override
    public List<Principal> listPrincipalsByUserId(String userIdQuery)
            throws IllegalArgumentException
    {
        return delegate.listPrincipalsByUserId(userIdQuery);
    }

    @Override
    public boolean supportsListingByEmail()
    {
        return delegate.supportsListingByEmail();
    }

    @Override
    public List<Principal> listPrincipalsByEmail(String emailQuery) throws IllegalArgumentException
    {
        return delegate.listPrincipalsByEmail(emailQuery);
    }

    @Override
    public boolean supportsListingByLastName()
    {
        return delegate.supportsListingByLastName();
    }

    @Override
    public List<Principal> listPrincipalsByLastName(String lastNameQuery)
            throws IllegalArgumentException
    {
        return delegate.listPrincipalsByLastName(lastNameQuery);
    }

    @Override
    @Deprecated
    public String authenticateApplication()
    {
        return delegate.authenticateApplication();
    }

    @Override
    @Deprecated
    public List<Principal> listPrincipalsByUserId(String dummyToken, String userIdQuery)
            throws IllegalArgumentException
    {
        return delegate.listPrincipalsByUserId(dummyToken, userIdQuery);
    }

    @Override
    @Deprecated
    public List<Principal> listPrincipalsByEmail(String dummyToken, String emailQuery)
            throws IllegalArgumentException
    {
        return delegate.listPrincipalsByEmail(dummyToken, emailQuery);
    }

    @Override
    @Deprecated
    public List<Principal> listPrincipalsByLastName(String dummyToken, String lastNameQuery)
            throws IllegalArgumentException
    {
        return delegate.listPrincipalsByLastName(dummyToken, lastNameQuery);
    }

    @Override
    public boolean isRemote()
    {
        return delegate.isRemote();
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        userStore.check();
        delegate.check();
    }

    @Override
    public boolean isConfigured()
    {
        return (userStore != null) && (delegate != null) && delegate.isConfigured();
    }

}
