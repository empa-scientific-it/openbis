/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.common.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Extensions of {@link CommonAnnotationBeanPostProcessor} which ignores missing beans.
 *
 * @author Franz-Josef Elmer
 */
public class AnnotationBeanPostProcessorIgnoringMissingBeans extends CommonAnnotationBeanPostProcessor
{
    private static final long serialVersionUID = 1L;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AnnotationBeanPostProcessorIgnoringMissingBeans.class);

    @Override
    protected Object autowireResource(BeanFactory factory, LookupElement element,
            String requestingBeanName) throws BeansException
    {
        String beanName = element.getName();
        if (factory.containsBean(beanName) == false)
        {
            operationLog.warn("Couldn't find bean '" + beanName + "' requested by bean '"
                    + requestingBeanName + "'.");
            return null;
        }
        return super.autowireResource(factory, element, requestingBeanName);
    }

}
