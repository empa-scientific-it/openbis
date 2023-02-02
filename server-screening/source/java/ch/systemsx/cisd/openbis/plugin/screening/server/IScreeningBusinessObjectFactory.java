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

import java.util.List;

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
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.IExperimentMetadataLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageResolutionLoader;

/**
 * A <i>screening</i> plugin specific business object factory.
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningBusinessObjectFactory
{
    public IImageDatasetLoader tryCreateImageDatasetLoader(String datasetCode, String datastoreCode);

    public IImageResolutionLoader tryCreateImageResolutionLoader(String datasetCode,
            String datastoreCode);

    /**
     * Note that the loader should be used only for the datasets from the specified data store server.
     */
    public IHCSFeatureVectorLoader createHCSFeatureVectorLoader(String datastoreCode);

    /**
     * @param experimentId the experiment id
     * @param dataStoreCodes the codes of all data stores containing data sets for the specified experimentId
     */
    public IExperimentMetadataLoader createExperimentMetadataLoader(long experimentId,
            List<String> dataStoreCodes);

    public ISampleBO createSampleBO(final Session session);

    public IDataSetTable createDataSetTable(final Session session);

    public IExperimentBO createExperimentBO(Session session);

    public IMaterialBO createMaterialBO(Session session);

    public ISampleLister createSampleLister(Session session);

    public IMaterialLister createMaterialLister(Session session);

    public IDataBO createDataBO(Session session);

    public IDatasetLister createDatasetLister(Session session);
    
    public IDAOFactory getDAOFactory();
}
