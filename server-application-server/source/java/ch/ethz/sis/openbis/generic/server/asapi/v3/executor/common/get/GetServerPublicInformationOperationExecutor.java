/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.get;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

/**
 * @author pkupczyk
 */
@Component
public class GetServerPublicInformationOperationExecutor
        extends OperationExecutor<GetServerPublicInformationOperation, GetServerPublicInformationOperationResult>
        implements IGetServerPublicInformationOperationExecutor
{
    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Override
    protected Class<? extends GetServerPublicInformationOperation> getOperationClass()
    {
        return GetServerPublicInformationOperation.class;
    }

    @Override
    protected GetServerPublicInformationOperationResult doExecute(IOperationContext context, GetServerPublicInformationOperation operation)
    {
        Map<String, String> info = new TreeMap<>();
        info.put("authentication-service", configurer.getResolvedProps().getProperty(ComponentNames.AUTHENTICATION_SERVICE));
        info.put("authentication-service.switch-aai.link",
                configurer.getResolvedProps().getProperty(ComponentNames.AUTHENTICATION_SERVICE + "." + Constants.SWITCH_AAI_LINK));
        info.put("authentication-service.switch-aai.label",
                configurer.getResolvedProps().getProperty(ComponentNames.AUTHENTICATION_SERVICE + "." + Constants.SWITCH_AAI_LABEL));
        info.put("openbis.support.email", configurer.getResolvedProps().getProperty(ComponentNames.OPENBIS_SUPPORT_EMAIL));
        return new GetServerPublicInformationOperationResult(info);
    }

}
