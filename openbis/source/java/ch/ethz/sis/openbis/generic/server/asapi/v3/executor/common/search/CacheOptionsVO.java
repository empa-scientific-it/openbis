package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.Properties;

import ch.systemsx.cisd.common.utilities.ITimeProvider;

public class CacheOptionsVO
{

    private final int capacity;

    private final Properties serviceProperties;

    private final String sessionToken;

    private final boolean asyncStorage;

    final ITimeProvider timeProvider;

    public CacheOptionsVO(final int capacity, final Properties serviceProperties, final String sessionToken,
            final boolean asyncStorage, final ITimeProvider timeProvider)
    {
        this.capacity = capacity;
        this.serviceProperties = serviceProperties;
        this.sessionToken = sessionToken;
        this.asyncStorage = asyncStorage;
        this.timeProvider = timeProvider;
    }

    public int getCapacity()
    {
        return capacity;
    }

    public Properties getServiceProperties()
    {
        return serviceProperties;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public boolean isAsyncStorage()
    {
        return asyncStorage;
    }

    public ITimeProvider getTimeProvider()
    {
        return timeProvider;
    }

}
