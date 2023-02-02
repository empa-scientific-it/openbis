/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IMaterialBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.AbstractPluginBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningDAOFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ExperimentMetadaLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.IExperimentMetadataLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellFeatureVectorReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.IMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.HCSImageResolutionLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageResolutionLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;

/**
 * The unique {@link IScreeningBusinessObjectFactory} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
public final class ScreeningBusinessObjectFactory extends AbstractPluginBusinessObjectFactory
        implements IScreeningBusinessObjectFactory
{

    @Resource(name = ResourceNames.SCREENING_DAO_FACTORY)
    private IScreeningDAOFactory specificDAOFactory;

    public ScreeningBusinessObjectFactory()
    {
    }

    /**
     * @return null if the dataset is not found in the imaging database
     */
    @Override
    public IImageDatasetLoader tryCreateImageDatasetLoader(String datasetCode, String datastoreCode)
    {
        return HCSDatasetLoader.tryCreate(specificDAOFactory.getImagingQueryDAO(datastoreCode),
                datasetCode);
    }

    @Override
    public IImageResolutionLoader tryCreateImageResolutionLoader(String datasetCode,
            String datastoreCode)
    {
        return HCSImageResolutionLoader.tryCreate(
                specificDAOFactory.getImagingQueryDAO(datastoreCode), datasetCode);
    }

    @Override
    public IHCSFeatureVectorLoader createHCSFeatureVectorLoader(String datastoreCode)
    {
        final IImagingReadonlyQueryDAO dao = specificDAOFactory.getImagingQueryDAO(datastoreCode);
        return new IHCSFeatureVectorLoader()
            {
                @Override
                public WellFeatureCollection<FeatureVectorValues> fetchWellFeatureValuesIfPossible(
                        Session session, List<WellFeatureVectorReference> references)
                {

                    return FeatureVectorLoader.fetchWellFeatureValuesIfPossible(references, dao,
                            getProvider(session));
                }

                @Override
                public WellFeatureCollection<FeatureVectorValues> fetchDatasetFeatureValues(
                        Session session, List<String> datasetCodes, List<String> featureCodes)
                {
                    return FeatureVectorLoader.fetchWellFeatureCollection(datasetCodes,
                            featureCodes, dao, getProvider(session));
                }
            };
    }

    FeatureVectorLoader.IMetadataProvider getProvider(final Session session)
    {
        return new IMetadataProvider()
            {
                private IDatasetLister lister;

                @Override
                public void getSampleIdentifiers(List<String> samplePermIds)
                {

                }

                @Override
                public SampleIdentifier tryGetSampleIdentifier(String samplePermId)
                {
                    return null;
                }

                @Override
                public List<String> tryGetContainedDatasets(String datasetCode)
                {
                    List<String> result = getLister().listContainedCodes(datasetCode);
                    return result;
                }

                private IDatasetLister getLister()
                {
                    if (lister == null)
                    {
                        lister = createDatasetLister(session);
                    }
                    return lister;
                }

            };
    }

    @Override
    public IExperimentMetadataLoader createExperimentMetadataLoader(long experimentId,
            List<String> dataStoreCodes)
    {
        return new ExperimentMetadaLoader(experimentId, getImagingQueries(dataStoreCodes));
    }

    private List<IImagingReadonlyQueryDAO> getImagingQueries(List<String> dataStoreCodes)
    {
        List<IImagingReadonlyQueryDAO> imagingQueries = new ArrayList<IImagingReadonlyQueryDAO>();
        for (String dataStoreCode : dataStoreCodes)
        {
            imagingQueries.add(specificDAOFactory.getImagingQueryDAO(dataStoreCode));
        }
        return imagingQueries;
    }

    @Override
    public final ISampleBO createSampleBO(final Session session)
    {
        return getCommonBusinessObjectFactory().createSampleBO(session);
    }

    @Override
    public ISampleLister createSampleLister(Session session)
    {
        return getCommonBusinessObjectFactory().createSampleLister(session);
    }

    @Override
    public IMaterialLister createMaterialLister(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialLister(session);
    }

    @Override
    public IDataSetTable createDataSetTable(Session session)
    {
        return getCommonBusinessObjectFactory().createDataSetTable(session);
    }

    @Override
    public IExperimentBO createExperimentBO(Session session)
    {
        return getCommonBusinessObjectFactory().createExperimentBO(session);
    }

    @Override
    public IMaterialBO createMaterialBO(Session session)
    {
        return getCommonBusinessObjectFactory().createMaterialBO(session);
    }

    @Override
    public IDataBO createDataBO(Session session)
    {
        return getCommonBusinessObjectFactory().createDataBO(session);
    }

    @Override
    public IDatasetLister createDatasetLister(Session session)
    {
        return getCommonBusinessObjectFactory().createDatasetLister(session);
    }

    @Override
    public IDAOFactory getDAOFactory()
    {
        return getDaoFactory();
    }
}
