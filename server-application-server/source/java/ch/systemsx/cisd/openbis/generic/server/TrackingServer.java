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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.ITrackingServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.SearchlinkUtilities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

public final class TrackingServer extends AbstractServer<ITrackingServer> implements
        ITrackingServer
{
    private final ICommonBusinessObjectFactory businessObjectFactory;

    public TrackingServer(final IOpenBisSessionManager sessionManager,
            final IDAOFactory daoFactory, final ICommonBusinessObjectFactory businessObjectFactory)
    {
        this(sessionManager, daoFactory, null, businessObjectFactory);
    }

    TrackingServer(final IOpenBisSessionManager sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.businessObjectFactory = businessObjectFactory;
    }

    ICommonBusinessObjectFactory getBusinessObjectFactory()
    {
        return businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    @Override
    public final ITrackingServer createLogger(IInvocationLoggerContext context)
    {
        return new TrackingServerLogger(getSessionManager(), context);
    }

    //
    // ITrackingServer
    //

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public List<Sample> listSamples(String sessionToken, TrackingSampleCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        final ListOrSearchSampleCriteria listerCriteria = new ListOrSearchSampleCriteria(criteria);
        listerCriteria.setEnrichDependentSamplesWithProperties(true);
        List<Sample> result = sampleLister.list(listerCriteria);
        fillSearchLinks(result, session.getBaseIndexURL());
        return result;
    }

    private void fillSearchLinks(List<Sample> samples, String baseIndexURL)
    {
        for (Sample sample : samples)
        {
            sample.setPermlink(SearchlinkUtilities.createSearchlinkURL(baseIndexURL,
                    EntityKind.SAMPLE, sample.getCode()));
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_ADMIN)
    public List<AbstractExternalData> listDataSets(String sessionToken, TrackingDataSetCriteria criteria)
    {
        final Session session = getSession(sessionToken);

        // retrieve data sets connected to samples of type specified in criteria
        // (these samples don't have properties loaded but ids are loaded)
        final IDatasetLister datasetLister = businessObjectFactory.createDatasetLister(session);
        final List<AbstractExternalData> dataSets = datasetLister.listByTrackingCriteria(criteria);
        // retrieve samples enriched with their dependent samples and properties
        // (drawback - samples directly connected to data sets are retrieved twice)
        final ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        final ListOrSearchSampleCriteria listerCriteria =
                new ListOrSearchSampleCriteria(extractConnectedSampleIds(dataSets));
        listerCriteria.setEnrichDependentSamplesWithProperties(true);
        final List<Sample> enrichedSamples = sampleLister.list(listerCriteria);
        // replace data set samples with enriched ones
        replaceConnectedSamples(dataSets, enrichedSamples);
        return dataSets;
    }

    private List<Long> extractConnectedSampleIds(List<AbstractExternalData> dataSets)
    {
        final List<Long> sampleIds = new ArrayList<Long>();
        for (AbstractExternalData dataSet : dataSets)
        {
            assert dataSet.getSample() != null : "data set is not connected to a sample";
            sampleIds.add(dataSet.getSample().getId());
        }
        return sampleIds;
    }

    private void replaceConnectedSamples(List<AbstractExternalData> dataSets, List<Sample> enrichedSamples)
    {
        // list orders are not the same - map is needed for quick search
        // <sample id, sample>
        final Map<Long, Sample> enrichedSamplesMap =
                new HashMap<Long, Sample>(enrichedSamples.size());
        for (Sample sample : enrichedSamples)
        {
            enrichedSamplesMap.put(sample.getId(), sample);
        }

        for (AbstractExternalData dataSet : dataSets)
        {
            final Sample enrichedSample = enrichedSamplesMap.get(dataSet.getSample().getId());
            assert enrichedSample != null;
            dataSet.setSample(enrichedSample);
        }
    }

}
