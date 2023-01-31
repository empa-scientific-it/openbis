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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.util.List;

import ch.systemsx.cisd.common.action.IMapper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IDataSetImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.authorization.IAuthorizationService;

/**
 * @author Jakub Straszewski
 */
public class AuthorizationService implements IAuthorizationService
{
    private IEncapsulatedOpenBISService openBisService;

    public AuthorizationService(IEncapsulatedOpenBISService openBisService)
    {
        this.openBisService = openBisService;
    }

    @Override
    public boolean doesUserHaveRole(String user, String role, String spaceOrNull)
    {
        return openBisService.doesUserHaveRole(user, role, spaceOrNull);
    }

    @Override
    public List<IDataSetImmutable> filterToVisibleDatasets(String user,
            List<IDataSetImmutable> datasets)
    {
        // waiting for lambdas... (x) => x.getDataSetCode();
        IMapper<IDataSetImmutable, String> codeMapper = new IMapper<IDataSetImmutable, String>()
            {
                @Override
                public String map(IDataSetImmutable item)
                {
                    return item.getDataSetCode();
                }
            };

        return AuthorizationHelper.filterToVisible(openBisService, user, datasets,
                codeMapper, AuthorizationHelper.EntityKind.DATA_SET);
    }

    @Override
    public List<IExperimentImmutable> filterToVisibleExperiments(String user,
            List<IExperimentImmutable> experiments)
    {
        IMapper<IExperimentImmutable, String> idMapper =
                new IMapper<IExperimentImmutable, String>()
                    {
                        @Override
                        public String map(IExperimentImmutable item)
                        {
                            return item.getExperimentIdentifier();
                        }
                    };

        return AuthorizationHelper.filterToVisible(openBisService, user, experiments,
                idMapper, AuthorizationHelper.EntityKind.EXPERIMENT);

    }

    @Override
    public List<ISampleImmutable> filterToVisibleSamples(String user, List<ISampleImmutable> samples)
    {

        IMapper<ISampleImmutable, String> idMapper = new IMapper<ISampleImmutable, String>()
            {
                @Override
                public String map(ISampleImmutable item)
                {
                    return item.getSampleIdentifier();
                }
            };

        return AuthorizationHelper.filterToVisible(openBisService, user, samples, idMapper,
                AuthorizationHelper.EntityKind.SAMPLE);
    }
}
