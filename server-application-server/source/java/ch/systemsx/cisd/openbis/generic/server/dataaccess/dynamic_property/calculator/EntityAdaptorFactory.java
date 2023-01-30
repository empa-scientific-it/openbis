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

import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IDataAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IExperimentAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IMaterialAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.ISampleAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;

/**
 * Factory of adaptors implementing {@link IEntityAdaptor}.
 * 
 * @author Piotr Buczek
 */
public class EntityAdaptorFactory
{
    /**
     * Returns an adaptor for specified entity based on its kind.
     * 
     * @param session
     */
    public static IEntityAdaptor create(IEntityInformationWithPropertiesHolder entity,
            IDynamicPropertyEvaluator evaluator, Session session)
    {

        switch (entity.getEntityKind())
        {
            case SAMPLE:
                return new SampleAdaptor((SamplePE) entity, evaluator, session);
            case EXPERIMENT:
                return new ExperimentAdaptor((ExperimentPE) entity, evaluator, session);
            case DATA_SET:
                return new ExternalDataAdaptor((DataPE) entity, evaluator, session);
            case MATERIAL:
                return new MaterialAdaptor((MaterialPE) entity, evaluator);
            default:
                throw new UnsupportedOperationException(""); // can't happen
        }
    }

    public static IExperimentAdaptor create(ExperimentPE entity,
            IDynamicPropertyEvaluator evaluator, Session session)
    {
        return new ExperimentAdaptor(entity, evaluator, session);
    }

    public static ISampleAdaptor create(SamplePE entity, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        return new SampleAdaptor(entity, evaluator, session);
    }

    public static IDataAdaptor create(DataPE entity, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        return new ExternalDataAdaptor(entity, evaluator, session);
    }

    public static IMaterialAdaptor create(MaterialPE entity, IDynamicPropertyEvaluator evaluator)
    {
        return new MaterialAdaptor(entity, evaluator);
    }
}
