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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerInformationOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetServerPublicInformationOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.pat.IPersonalAccessTokenConfig;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.pat.PersonalAccessTokenConstants;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class GetServerInformationOperationExecutor
        extends OperationExecutor<GetServerInformationOperation, GetServerInformationOperationResult>
        implements IGetServerInformationOperationExecutor
{
    @Autowired
    private IApplicationServerInternalApi server;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Autowired
    private IGetServerPublicInformationOperationExecutor getPublicInformationExecutor;

    @Autowired
    private IPersonalAccessTokenConfig personalAccessTokenConfig;

    @Override
    protected Class<? extends GetServerInformationOperation> getOperationClass()
    {
        return GetServerInformationOperation.class;
    }

    @Override
    protected GetServerInformationOperationResult doExecute(IOperationContext context, GetServerInformationOperation operation)
    {
        Map<String, String> info = new TreeMap<>();
        info.putAll(getPublicInformation(context));

        info.put("api-version", server.getMajorVersion() + "." + server.getMinorVersion());
        info.put("project-samples-enabled", Boolean.toString(CommonServiceProvider.getCommonServer().isProjectSamplesEnabled(null)));
        info.put("archiving-configured", Boolean.toString(CommonServiceProvider.getCommonServer().isArchivingConfigured(null)));
        info.put("enabled-technologies", configurer.getResolvedProps().getProperty(Constants.ENABLED_MODULES_KEY));
        info.put("create-continuous-sample-codes", configurer.getResolvedProps().getProperty(Constants.CREATE_CONTINUOUS_SAMPLES_CODES_KEY));
        info.put(PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_ENABLED_KEY,
                Boolean.toString(personalAccessTokenConfig.arePersonalAccessTokensEnabled()));
        info.put(PersonalAccessTokenConstants.PERSONAL_ACCESS_TOKENS_MAX_VALIDITY_PERIOD,
                Long.toString(personalAccessTokenConfig.getPersonalAccessTokensMaxValidityPeriod()));
        info.put("openbis-version", BuildAndEnvironmentInfo.INSTANCE.getVersion());

        return new GetServerInformationOperationResult(info);
    }

    private Map<String, String> getPublicInformation(IOperationContext context)
    {
        GetServerPublicInformationOperation operation = new GetServerPublicInformationOperation();
        GetServerPublicInformationOperationResult result =
                (GetServerPublicInformationOperationResult) getPublicInformationExecutor.execute(context, Collections.singletonList(operation))
                        .get(operation);
        return result.getServerInformation();
    }

}
