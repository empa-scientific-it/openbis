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

import org.apache.commons.lang3.StringUtils;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * A configuration object for the {@link CachingAuthenticationService}.
 * 
 * @author Bernd Rinn
 */
public class CachingAuthenticationConfiguration
{
    private String passwordCacheFile;

    private IAuthenticationService delegate;

    private long cacheTime = CachingAuthenticationService.CACHE_TIME_MILLIS;

    private long cacheTimeNoRevalidation =
            CachingAuthenticationService.CACHE_TIME_MILLIS_NO_REVALIDATION;

    /**
     * Returns the path of the password cache file.
     */
    public String getPasswordCacheFile()
    {
        return passwordCacheFile;
    }

    /**
     * Sets the path of the password cache file.
     */
    public void setPasswordCacheFile(String passwordCacheFile)
    {
        this.passwordCacheFile = passwordCacheFile;
    }

    /**
     * Returns the delegate authentication service.
     */
    public IAuthenticationService getDelegate()
    {
        return delegate;
    }

    /**
     * Sets the delegate authentication service.
     */
    public void setDelegate(IAuthenticationService delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Returns the cache time (in ms).
     */
    public long getCacheTime()
    {
        return cacheTime;
    }

    /**
     * Sets the cache time (in ms).
     */
    public void setCacheTime(long cacheTime)
    {
        this.cacheTime = cacheTime;
    }

    /**
     * Returns the cache time (in s, as String).
     */
    public String getCacheTimeStr()
    {
        return Long.toString(getCacheTime() / 1000);
    }

    /**
     * Sets the cache time as String in a format understood by {@link DateTimeUtils#parseDurationToMillis(String)}.
     */
    public void setCacheTimeStr(String cacheTimeStr)
    {
        if (isResolved(cacheTimeStr))
        {
            setCacheTime(DateTimeUtils.parseDurationToMillis(cacheTimeStr));
        }
    }

    /**
     * Returns the time to return cache value without triggering revalidation (in ms).
     */
    public long getCacheTimeNoRevalidation()
    {
        return cacheTimeNoRevalidation;
    }

    /**
     * Sets the time to return cache value without triggering revalidation (in ms).
     */
    public void setCacheTimeNoRevalidation(long cacheTimeNoRevalidation)
    {
        this.cacheTimeNoRevalidation = cacheTimeNoRevalidation;
    }

    /**
     * Returns the time to return cache value without triggering revalidation (in s, as String).
     */
    public String getCacheTimeNoRevalidationStr()
    {
        return Long.toString(getCacheTimeNoRevalidation() / 1000);
    }

    /**
     * Sets the time to return cache value without triggering revalidation as String in a format understood by
     * {@link DateTimeUtils#parseDurationToMillis(String)}..
     */
    public void setCacheTimeNoRevalidationStr(String cacheTimeNoRevalidationStr)
    {
        if (isResolved(cacheTimeNoRevalidationStr))
        {
            setCacheTimeNoRevalidation(DateTimeUtils
                    .parseDurationToMillis(cacheTimeNoRevalidationStr));
        }
    }

    private static boolean isResolved(String name)
    {
        return StringUtils.isNotBlank(name) && name.startsWith("${") == false;
    }

}
