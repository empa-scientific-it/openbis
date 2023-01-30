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

import java.util.List;

import org.hibernate.Session;

import ch.systemsx.cisd.common.resource.ReleasableIterable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetRelationshipPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.util.RelationshipUtils;

/**
 * {@link IEntityAdaptor} implementation for {@link ExternalDataPE}.
 * 
 * @author Piotr Buczek
 */
public class ExternalDataAdaptor extends AbstractEntityAdaptor implements IDataAdaptor,
        INonAbstractEntityAdapter
{
    private final DataPE externalDataPE;

    private final Session session;

    public ExternalDataAdaptor(DataPE externalDataPE, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        super(externalDataPE, evaluator);
        this.session = session;
        this.externalDataPE = externalDataPE;
    }

    public DataPE externalDataPE()
    {
        return externalDataPE;
    }

    @Override
    public DataPE entityPE()
    {
        return externalDataPE();
    }

    @Override
    public IExperimentAdaptor experiment()
    {
        ExperimentPE experiment = externalDataPE.getExperiment();
        if (experiment != null)
        {
            IExperimentAdaptor adaptor = EntityAdaptorFactory.create(experiment, evaluator, session);
            getResources().add(adaptor);
            return adaptor;
        }
        return null;
    }

    @Override
    public ISampleAdaptor sample()
    {
        SamplePE sample = externalDataPE.tryGetSample();
        if (sample != null)
        {
            ISampleAdaptor adaptor = EntityAdaptorFactory.create(sample, evaluator, session);
            getResources().add(adaptor);
            return adaptor;
        }
        return null;
    }

    @Override
    public Iterable<IDataAdaptor> parents()
    {
        return parentsOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> parentsOfType(String typeCodeRegexp)
    {
        return getParentsOfType(typeCodeRegexp, evaluator.getParentChildRelationshipTypeId());
    }

    private Iterable<IDataAdaptor> getParentsOfType(String typeCodeRegexp, Long relationshipTypeId)
    {
        ReleasableIterable<IDataAdaptor> iterable =
                new ReleasableIterable<IDataAdaptor>(new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                        .parentsOfType(typeCodeRegexp, relationshipTypeId));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public Iterable<IDataAdaptor> children()
    {
        return childrenOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> childrenOfType(String typeCodeRegexp)
    {
        return getChildrenOfType(typeCodeRegexp, evaluator.getParentChildRelationshipTypeId());
    }

    private Iterable<IDataAdaptor> getChildrenOfType(String typeCodeRegexp, Long relationshipTypeId)
    {
        ReleasableIterable<IDataAdaptor> iterable =
                new ReleasableIterable<IDataAdaptor>(new ExternalDataAdaptorRelationsLoader(externalDataPE, evaluator, session)
                        .childrenOfType(typeCodeRegexp, relationshipTypeId));
        getResources().add(iterable);
        return iterable;
    }

    @Override
    public IDataAdaptor container()
    {
        List<DataSetRelationshipPE> relationships = RelationshipUtils.getContainerComponentRelationships(
                externalDataPE.getParentRelationships());
        if (relationships.isEmpty())
        {
            return null;
        }
        IDataAdaptor adaptor = EntityAdaptorFactory.create(relationships.get(0).getParentDataSet(), evaluator, session);
        getResources().add(adaptor);
        return adaptor;
    }

    @Override
    public Iterable<IDataAdaptor> containers()
    {
        return getParentsOfType(ENTITY_TYPE_ANY_CODE_REGEXP, evaluator.getContainerComponentRelationshipTypeId());
    }

    @Override
    public Iterable<IDataAdaptor> contained()
    {
        return containedOfType(ENTITY_TYPE_ANY_CODE_REGEXP);
    }

    @Override
    public Iterable<IDataAdaptor> containedOfType(String typeCodeRegexp)
    {
        return getChildrenOfType(typeCodeRegexp, evaluator.getContainerComponentRelationshipTypeId());
    }

}
