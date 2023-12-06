/*
 *  Copyright ETH 2023 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.dssapi.v3.executor.service;

import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.CustomDSSService;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.service.id.CustomDssServiceCode;
import ch.ethz.sis.openbis.generic.dssapi.v3.plugin.service.ICustomDSSServiceExecutor;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.reflection.ClassUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CustomDSSServiceProvider  implements InitializingBean, ICustomDSSServiceProvider
{

    public static final String SERVICES_PROPERTY_KEY = "custom-services";

    public static final String CLASS_KEY = "class";

    public static final String LABEL_KEY = "label";

    public static final String DESCRIPTION_KEY = "description";

    private final Map<String, ICustomDSSServiceExecutor> executors = new HashMap<>();

    private List<CustomDSSService> services;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ExtendedProperties props = DssPropertyParametersUtil.loadServiceProperties();
        Properties serviceProperties = props == null ? new Properties() : props;
        PropertyParametersUtil.SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        SERVICES_PROPERTY_KEY, false);
        ArrayList<CustomDSSService> list = new ArrayList<>();
        for (PropertyParametersUtil.SectionProperties sectionProperties : sectionsProperties)
        {
            String code = sectionProperties.getKey();
            Properties properties = sectionProperties.getProperties();
            String className = PropertyUtils.getMandatoryProperty(properties, CLASS_KEY);
            CustomDSSService service = new CustomDSSService();
            service.setCode(new CustomDssServiceCode(code));
            service.setLabel(properties.getProperty(LABEL_KEY, code));
            service.setDescription(properties.getProperty(DESCRIPTION_KEY, ""));
            ICustomDSSServiceExecutor
                    serviceExecutor = ClassUtils.create(ICustomDSSServiceExecutor.class, className, properties);
            list.add(service);
            executors.put(code, serviceExecutor);
        }
        services = Collections.unmodifiableList(list);
    }
    @Override
    public List<CustomDSSService> getCustomDSSServices()
    {
        return services;
    }

    @Override
    public ICustomDSSServiceExecutor tryGetCustomDSSServiceExecutor(String code)
    {
        return executors.get(code);
    }

}
