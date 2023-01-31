/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.authorization.IDssSessionAuthorizer;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Implementation of {@link IDssSessionAuthorizer} that asks the openBIS application server to check the data set codes.
 * 
 * @author Bernd Rinn
 */
public class DatasetSessionAuthorizer implements IDssSessionAuthorizer
{

    private static final int MILLIS_PER_MINUTE = 60 * 1000;

    static protected final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DatasetSessionAuthorizer.class);

    private final DatasetAuthorizationCache authCacheOrNull;

    /**
     * Creates the authorizer with default timing parameters of <code>cacheExpirationMins=5</code> and <code>cleanupTimerMins=180</code> (3 hours).
     */
    public DatasetSessionAuthorizer()
    {
        this(5, 3 * 60);
    }

    /**
     * Creates the authorizer.
     * 
     * @param cacheExpirationMins Cache expiration time (in minutes). Set to 0 to disable the cache.
     * @param cleanupTimerMins Time interval between two calls of the cache cleanup timer (in minutes).
     */
    public DatasetSessionAuthorizer(int cacheExpirationMins, int cleanupTimerMins)
    {
        authCacheOrNull =
                (cacheExpirationMins == 0) ? null : new DatasetAuthorizationCache(
                        cacheExpirationMins * MILLIS_PER_MINUTE, cleanupTimerMins
                                * MILLIS_PER_MINUTE);
    }

    @Override
    public Status checkDatasetAccess(String sessionToken, List<String> datasetCodes)
    {
        final Status cachedStatus = tryGetCached(sessionToken, datasetCodes);
        if (cachedStatus != null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Access of session '%s' to data sets '%s' on openBIS "
                                + "application server (from authorization cache): %s.",
                        sessionToken, datasetCodes, cachedStatus));
            }
            return cachedStatus;
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Checking access of session '%s' to data sets '%s' on "
                    + "openBIS application server.", sessionToken, datasetCodes));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        try
        {
            openBISService.checkDataSetCollectionAccess(sessionToken, datasetCodes);
            cachePutAll(sessionToken, datasetCodes, true);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            if (datasetCodes.size() == 1)
            {
                cachePut(sessionToken, datasetCodes.get(0), false);
            }
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkDatasetAccess(String sessionToken, String datasetCode)
    {
        final Status cachedStatus = tryGetCached(sessionToken, datasetCode);
        if (cachedStatus != null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Access of session '%s' to data set '%s' on openBIS "
                                + "application server (from authorization cache): %s.",
                        sessionToken, datasetCode, cachedStatus));
            }
            return cachedStatus;
        }

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Checking access of session '%s' to data set '%s' on "
                    + "openBIS application server.", sessionToken, datasetCode));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        try
        {
            openBISService.checkDataSetAccess(sessionToken, datasetCode);
            cachePut(sessionToken, datasetCode, true);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            cachePut(sessionToken, datasetCode, false);
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkSpaceWriteable(String sessionToken, SpaceIdentifier spaceId)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Checking whether space '%s' is writable to session '%s' on "
                            + "openBIS application server.",
                    spaceId, sessionToken));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkSpaceAccess(sessionToken, spaceId);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkExperimentWriteable(String sessionToken, String experimentIdentifier)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Checking whether experiment '%s' is writable to session '%s' on "
                            + "openBIS application server.",
                    experimentIdentifier, sessionToken));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkExperimentAccess(sessionToken, experimentIdentifier);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkSampleWriteable(String sessionToken, String sampleIdentifier)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Checking whether sample '%s' is writable to session '%s' on "
                            + "openBIS application server.",
                    sampleIdentifier, sessionToken));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkSampleAccess(sessionToken, sampleIdentifier);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkInstanceAdminAuthorization(String sessionToken)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Checking if session '%s' has instance admin privileges on "
                            + "openBIS application server.",
                    sessionToken));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkInstanceAdminAuthorization(sessionToken);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    @Override
    public Status checkProjectPowerUserAuthorization(String sessionToken)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(
                    "Checking if session '%s' has project power user privileges on "
                            + "openBIS application server.",
                    sessionToken));
        }
        final IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        try
        {
            openBISService.checkProjectPowerUserAuthorization(sessionToken);
            return Status.OK;
        } catch (UserFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }
    }

    /**
     * Clears all entries from the cache (for unit tests).
     */
    public void clearCache()
    {
        if (authCacheOrNull == null)
        {
            return;
        }
        authCacheOrNull.clear();
    }

    private void cachePut(String sessionToken, String datasetCode, boolean authorized)
    {
        if (authCacheOrNull == null)
        {
            return;
        }
        authCacheOrNull.put(sessionToken, datasetCode, authorized);
    }

    private void cachePutAll(String sessionToken, List<String> datasetCodes, boolean authorized)
    {
        if (authCacheOrNull == null)
        {
            return;
        }
        authCacheOrNull.putAll(sessionToken, datasetCodes, authorized);
    }

    private Status tryGetCached(String sessionToken, String datasetCode)
    {
        if (authCacheOrNull == null)
        {
            return null;
        }
        final Boolean authorized = authCacheOrNull.tryGet(sessionToken, datasetCode);
        return (authorized == null) ? null : (authorized ? Status.OK : Status.createError());
    }

    private Status tryGetCached(String sessionToken, List<String> datasetCodes)
    {
        if (authCacheOrNull == null)
        {
            return null;
        }
        for (String code : datasetCodes)
        {
            final Boolean authorized = authCacheOrNull.tryGet(sessionToken, code);
            if (authorized == null)
            {
                return null;
            } else if (authorized == false)
            {
                return Status.createError();
            }
        }
        return Status.OK;
    }

}