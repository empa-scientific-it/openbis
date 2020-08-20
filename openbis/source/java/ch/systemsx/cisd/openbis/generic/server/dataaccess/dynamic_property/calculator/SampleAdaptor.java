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

import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import org.hibernate.Session;

import ch.systemsx.cisd.common.resource.ReleasableIterable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

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
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<ISampleAdaptor> containedOfType(String typeCodeRegexp)
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<IDataAdaptor> dataSets()
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

    @Override
    public Iterable<IDataAdaptor> dataSetsOfType(String typeCodeRegexp)
    {
        //TODO Lucene: Method to be implemented if is used after removing lucene.
        throw new NotImplementedException("TODO Lucene: Method to be implemented if is used after removing lucene.");
    }

}
