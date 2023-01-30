/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.CustomASServiceCode;
import ch.ethz.sis.openbis.generic.asapi.v3.plugin.service.ICustomASServiceExecutor;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil.SectionProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class CustomASServiceProvider implements InitializingBean, ICustomASServiceProvider
{
    public static final String SERVICES_PROPERTY_KEY = "services";

    public static final String CLASS_KEY = "class";

    public static final String LABEL_KEY = "label";

    public static final String DESCRIPTION_KEY = "description";

    private List<CustomASService> services;

    private Map<String, ICustomASServiceExecutor> executors = new HashMap<String, ICustomASServiceExecutor>();

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    protected ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Properties serviceProperties = configurer == null ? new Properties() : configurer.getResolvedProps();
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        SERVICES_PROPERTY_KEY, false);
        ArrayList<CustomASService> list = new ArrayList<CustomASService>();
        for (SectionProperties sectionProperties : sectionsProperties)
        {
            String code = sectionProperties.getKey();
            Properties properties = sectionProperties.getProperties();
            String className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
            CustomASService service = new CustomASService();
            service.setCode(new CustomASServiceCode(code));
            service.setLabel(properties.getProperty(LABEL_KEY, code));
            service.setDescription(properties.getProperty(DESCRIPTION_KEY, ""));
            ICustomASServiceExecutor serviceExecutor = ClassUtils.create(ICustomASServiceExecutor.class, className, properties);
            list.add(service);
            executors.put(code, serviceExecutor);
        }
        services = Collections.unmodifiableList(list);
    }

    @Override
    public List<CustomASService> getCustomASServices()
    {
        return services;
    }

    @Override
    public ICustomASServiceExecutor tryGetCustomASServiceExecutor(String code)
    {
        return executors.get(code);
    }

}
