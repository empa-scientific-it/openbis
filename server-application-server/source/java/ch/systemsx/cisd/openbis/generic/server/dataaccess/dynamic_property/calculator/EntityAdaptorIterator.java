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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator;

import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author anttil
 */
public class EntityAdaptorIterator<T> extends ScrollableResultsIterator<T>
{

    private final IDynamicPropertyEvaluator evaluator;

    private final Session session;

    public EntityAdaptorIterator(ScrollableResults scroll, IDynamicPropertyEvaluator evaluator,
            Session session)
    {
        super(scroll);
        this.evaluator = evaluator;
        this.session = session;

    }

    @SuppressWarnings("unchecked")
    @Override
    public T parseValue(Object[] result)
    {
        Object o = result[0];
        if (o instanceof SamplePE)
        {
            return (T) EntityAdaptorFactory.create((SamplePE) o, evaluator, session);
        } else if (o instanceof DataPE)
        {
            return (T) EntityAdaptorFactory.create((DataPE) o, evaluator, session);
        } else
        {
            throw new IllegalStateException("Unknown result type: " + o);
        }
    }
}
