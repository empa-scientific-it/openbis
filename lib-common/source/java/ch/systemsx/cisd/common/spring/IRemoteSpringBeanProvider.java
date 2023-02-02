package ch.systemsx.cisd.common.spring;

public interface IRemoteSpringBeanProvider
{
    public <T> T create(final Class<T> serviceInterface, final String serviceURL,
            final long serverTimeoutInMillis);

}
