package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.operation;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener.IOperationListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static ch.ethz.sis.openbis.generic.asapi.v3.plugin.listener.IOperationListener.LISTENER_CLASS_KEY;

public class OperationListenerLoader implements ApplicationContextAware, InitializingBean,
        DisposableBean
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, OperationListenerLoader.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        CommonServiceProvider.setApplicationContext(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        try {
            List<IOperationListener<IOperation, IOperationResult>> operationListeners = new ArrayList<>();
            Properties properties = configurer.getResolvedProps();
            PropertyParametersUtil.SectionProperties[] operationListenersDefinitions =
                    PropertyParametersUtil.extractSectionProperties(properties, IOperationListener.LISTENER_PROPERTY_KEY, false);
            for (PropertyParametersUtil.SectionProperties sectionProperty:operationListenersDefinitions)
            {
                String key = sectionProperty.getKey();
                String operationListenerClassName = sectionProperty.getProperties().getProperty(LISTENER_CLASS_KEY);
                operationLog.info("Adding: " + key + " Class: " + operationListenerClassName);
                Class<?> operationListenerClass = Class.forName(operationListenerClassName);
                Constructor<?> operationListenerConstructor = operationListenerClass.getConstructor();
                IOperationListener<IOperation, IOperationResult> operationListener = (IOperationListener) operationListenerConstructor.newInstance();
                operationListeners.add(operationListener);
                operationLog.info("Added: " + key + " Class: " + operationListenerClassName);
            }
            OperationExecutor.setOperationListeners(operationListeners);
            operationLog.info("Operation Listeners Set");
        } catch (Exception ex)
        {
            operationLog.info("Failed to load the configured api listeners.", ex);
        }
    }

    @Override
    public void destroy() throws Exception
    {

    }
}
