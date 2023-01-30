/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.SearchDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.SearchSamplesOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.hibernate.Session;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import ch.systemsx.cisd.common.resource.ReleasableIterable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * {@link IEntityAdaptor} implementation for {@link SamplePE}.
 * 
 * @author Piotr Buczek
 */
public class SampleAdaptor extends AbstractEntityAdaptor implements ISampleAdaptor,
        INonAbstractEntityAdapter
{
    private final SamplePE samplePE;

    private final Session session;

    public SampleAdaptor(SamplePE samplePE, IDynamicPropertyEvaluator evaluator, Session session)
    {
        super(samplePE, evaluator);
        this.session = session;
        this.samplePE = samplePE;
    }

    public SamplePE samplePE()
    {
        return samplePE;
    }

    @Override
    public SamplePE entityPE()
    {
        return samplePE();
    }

    @Override
    public IExperimentAdaptor experiment()
    {
        IExperimentAdaptor adaptor = EntityAdaptorFactory.create(samplePE.getExperiment(), evaluator, session);
        getResources().add(adaptor);
        return adaptor;
    }

    @Override
    public Iterable<ISampleAdaptor> parents()
    {
        return parentsOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<ISampleAdaptor> parentsOfType(String typeCodeRegexp)
    {
        ReleasableIterable<ISampleAdaptor> iterable =
                new ReleasableIterable<ISampleAdaptor>(new SampleAdaptorRelationsLoader(samplePE, evaluator, session)
                        .parentsOfType(typeCodeRegexp));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public Iterable<ISampleAdaptor> children()
    {
        return childrenOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<ISampleAdaptor> childrenOfType(String typeCodeRegexp)
    {
        ReleasableIterable<ISampleAdaptor> iterable =
                new ReleasableIterable<ISampleAdaptor>(new SampleAdaptorRelationsLoader(samplePE, evaluator, session)
                        .childrenOfType(typeCodeRegexp));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public ISampleAdaptor container()
    {
        SamplePE container = samplePE.getContainer();
        if (container != null)
        {
            ISampleAdaptor adaptor = EntityAdaptorFactory.create(container, evaluator, session);
            getResources().add(adaptor);
            return adaptor;
        } else
        {
            return null;
        }
    }

    @Override
    public Iterable<ISampleAdaptor> contained()
    {
        return containedOfType("*");
    }

    @Override
    public Iterable<ISampleAdaptor> containedOfType(final String typeCodeRegexp)
    {
        final IDAOFactory daoFactory = CommonServiceProvider.getDAOFactory();
        final SearchSamplesOperationExecutor searchSamplesOperationExecutor =
                (SearchSamplesOperationExecutor) CommonServiceProvider
                        .tryToGetBean("search-samples-operation-executor");
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();

        final SampleFetchOptions fetchOptions = new SampleFetchOptions();
        final SampleSearchCriteria criteria = new SampleSearchCriteria().withAndOperator();
        criteria.withContainer().withPermId().thatEquals(samplePE.getPermId());
        criteria.withType().withCode().thatEquals(typeCodeRegexp);

        final PersonPE systemUser = personDAO.tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
        final Collection<Long> ids = searchSamplesOperationExecutor.executeDirectSQLSearchForIds(systemUser, criteria,
                fetchOptions);
        final List<SamplePE> samplePEs = sampleDAO.listByIDs(ids);

        return samplePEs.stream().map(samplePE -> new SampleAdaptor(samplePE, this.evaluator, this.session))
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<IDataAdaptor> dataSets()
    {
        return dataSetsOfType("*");
    }

    @Override
    public Iterable<IDataAdaptor> dataSetsOfType(final String typeCodeRegexp)
    {
        final IDAOFactory daoFactory = CommonServiceProvider.getDAOFactory();
        final SearchDataSetsOperationExecutor searchDataSetsOperationExecutor =
                (SearchDataSetsOperationExecutor) CommonServiceProvider
                        .tryToGetBean("search-data-sets-operation-executor");
        final IPersonDAO personDAO = daoFactory.getPersonDAO();
        final IDataDAO dataSetDAO = daoFactory.getDataDAO();

        final DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        final DataSetSearchCriteria criteria = new DataSetSearchCriteria().withAndOperator();
        criteria.withSample().withPermId().thatEquals(samplePE.getPermId());
        criteria.withType().withCode().thatEquals(typeCodeRegexp);

        final PersonPE systemUser = personDAO.tryFindPersonByUserId(PersonPE.SYSTEM_USER_ID);
        final Collection<Long> ids = searchDataSetsOperationExecutor.executeDirectSQLSearchForIds(systemUser, criteria,
                fetchOptions);
        final List<DataPE> dataPEs = dataSetDAO.listByIDs(ids);

        return dataPEs.stream().map(dataPE -> new ExternalDataAdaptor(dataPE, this.evaluator, this.session))
                .collect(Collectors.toList());
    }

}
