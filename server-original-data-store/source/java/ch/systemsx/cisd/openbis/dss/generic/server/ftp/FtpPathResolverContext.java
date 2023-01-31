/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.server.ISessionTokenProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.fs.ResolverContext;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 * An object holding all necessary context information for ftp path resolution.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpPathResolverContext implements ISessionTokenProvider
{
    private final String sessionToken;

    private final IServiceForDataStoreServer service;

    private final IGeneralInformationService generalInfoService;

    private final IApplicationServerApi v3api;

    private final IFtpPathResolverRegistry resolverRegistry;

    private final Cache cache;

    private final ResolverContext resolverContext;

    public FtpPathResolverContext(String sessionToken, IServiceForDataStoreServer service,
            IGeneralInformationService generalInfoService, IApplicationServerApi v3api,
            IFtpPathResolverRegistry resolverRegistry, Cache cache, String path)
    {
        this.sessionToken = sessionToken;
        this.service = service;
        this.generalInfoService = generalInfoService;
        this.resolverRegistry = resolverRegistry;
        this.v3api = v3api;
        this.cache = cache;
        this.resolverContext = new ResolverContext(sessionToken, cache, v3api, path);
    }

    public FtpPathResolverContext cloneForPath(String path)
    {
        return new FtpPathResolverContext(sessionToken, service, generalInfoService, v3api, resolverRegistry,
                cache, path);
    }

    @Override
    public String getSessionToken()
    {
        return sessionToken;
    }

    public IServiceForDataStoreServer getService()
    {
        return service;
    }

    public IApplicationServerApi getV3Api()
    {
        return v3api;
    }

    public Cache getCache()
    {
        return cache;
    }

    public DataSet getDataSet(String dataSetCode)
    {
        DataSet dataSet = cache.getDataSet(dataSetCode);
        if (dataSet == null)
        {
            EnumSet<DataSetFetchOption> fetchOptions = EnumSet.of(DataSetFetchOption.BASIC, DataSetFetchOption.PARENTS, DataSetFetchOption.CHILDREN);

            List<DataSet> dataSetsWithMetaData =
                    generalInfoService.getDataSetMetaData(sessionToken, Arrays.asList(dataSetCode),
                            fetchOptions);

            dataSet = dataSetsWithMetaData.get(0);
            cache.putDataSet(dataSet);
        }
        return dataSet;
    }

    public List<AbstractExternalData> listDataSetsByCode(List<String> codes)
    {
        List<String> codesToAskFor = new ArrayList<String>();
        List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
        for (String code : codes)
        {
            AbstractExternalData dataSet = cache.getExternalData(code);
            if (dataSet == null)
            {
                codesToAskFor.add(code);
            } else
            {
                dataSets.add(dataSet);
            }
        }
        if (codesToAskFor.isEmpty() == false)
        {
            List<AbstractExternalData> newDataSets =
                    service.listDataSetsByCode(sessionToken, codesToAskFor);
            for (AbstractExternalData newDataSet : newDataSets)
            {
                cache.putExternalData(newDataSet);
                dataSets.add(newDataSet);
            }
        }
        return dataSets;
    }

    public Experiment getExperiment(String experimentId)
    {
        Experiment experiment = cache.getExperiment(experimentId);
        if (experiment == null)
        {
            ExperimentIdentifier experimentIdentifier =
                    new ExperimentIdentifierFactory(experimentId).createIdentifier();
            List<Experiment> result =
                    service.listExperiments(sessionToken,
                            Collections.singletonList(experimentIdentifier),
                            new ExperimentFetchOptions());
            if (result.isEmpty())
            {
                throw new UserFailureException("Unknown experiment '" + experimentIdentifier + "'.");
            }
            experiment = result.get(0);
            cache.putExperiment(experiment);
        }
        return experiment;
    }

    public List<AbstractExternalData> getDataSets(Experiment experiment)
    {
        String experimentPermId = experiment.getPermId();
        List<AbstractExternalData> dataSets = cache.getDataSetsByExperiment(experimentPermId);
        if (dataSets == null)
        {
            dataSets = getAvailableDataSets(experiment);
            cache.putDataSetsForExperiment(dataSets, experimentPermId);
        }
        return dataSets;
    }

    private List<AbstractExternalData> getAvailableDataSets(Experiment experiment)
    {
        List<AbstractExternalData> availableDataSets = new ArrayList<>();
        List<AbstractExternalData> dataSets = service.listDataSetsByExperimentID(sessionToken, new TechId(experiment));
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.isAvailable())
            {
                availableDataSets.add(dataSet);
            }
        }
        return availableDataSets;
    }

    public IGeneralInformationService getGeneralInfoService()
    {
        return generalInfoService;
    }

    public IFtpPathResolverRegistry getResolverRegistry()
    {
        return resolverRegistry;
    }

    public ResolverContext getResolverContext()
    {
        return resolverContext;
    }

}
