/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.spring;

import java.util.Properties;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Post processor checking if there are no insecure instances of HttpInvokerServiceExporter registered as beans
 * 
 * @author Jakub Straszewski
 */
public class CheckSecureHttpInvokerBeanPostProcessor implements BeanPostProcessor, InitializingBean
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CheckSecureHttpInvokerBeanPostProcessor.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    public class InsecureHttpInvokerServiceExporterException extends BeanDefinitionValidationException
    {
        private static final long serialVersionUID = 1L;

        public InsecureHttpInvokerServiceExporterException(String msg)
        {
            super(msg);
        }
    }

    /**
     * Bean is considered insecure if it's an untrusted HttpInvokerServiceExporter
     */
    private static boolean isBeanSecure(Object bean)
    {
        if (false == bean instanceof HttpInvokerServiceExporter)
        {
            return true;
        }
        if (bean instanceof WhiteAndBlackListHttpInvokerServiceExporter ||
                bean instanceof WhiteAndBlackListStreamSupportingHttpInvokerExporter)
        {
            operationLog.info("Secure HTTP invoker service exporter: " + bean);
            return true;
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        if (configurer == null)
        {
            return;
        }
        WhiteAndBlackListCodebaseAwareObjectInputStream.populateWhiteAndBlackListOfApiParameterClasses(configurer.getResolvedProps());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException
    {
        if (false == isBeanSecure(bean))
        {
            throw new InsecureHttpInvokerServiceExporterException("Bean " + beanName + " is an instance of unsecure type HttpInvokerServiceExporter");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
    {
        return bean;
    }

}
