package ch.ethz.sis.openbis.generic.server.asapi.v3.pat;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;

public class ApplicationServerApiPersonalAccessTokenPostProcessor implements BeanPostProcessor
{

    @Override public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException
    {
        if (!(bean instanceof IApplicationServerInternalApi))
        {
            return bean;
        }

        if (bean instanceof ApplicationServerApiPersonalAccessTokenDecorator)
        {
            return bean;
        }

        return new ApplicationServerApiPersonalAccessTokenDecorator((IApplicationServerInternalApi) bean);
    }
}
