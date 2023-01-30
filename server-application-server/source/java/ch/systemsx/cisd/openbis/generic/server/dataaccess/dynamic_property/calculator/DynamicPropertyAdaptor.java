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

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.DynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.hotdeploy_plugins.api.IEntityPropertyAdaptor;

/**
 * {@link IEntityPropertyAdaptor} for dynamic property with lazy evaluation and cyclic dependencies detection.
 * 
 * @author Piotr Buczek
 */
public class DynamicPropertyAdaptor implements IEntityPropertyAdaptor
{
    /** state of lazy evaluation of the property value (analogy to graph search) */
    private enum State
    {
        /** initial state before evaluation has been triggered */
        EMPTY,

        /** state reached when evaluation is triggered */
        EVALUATING,

        /** state reached when evaluation has been finished */
        EVALUATED
    }

    private State state = State.EMPTY;

    private String value = null;

    private final String code;

    private final EntityPropertyPE propertyPE;

    private final IEntityAdaptor entityAdaptor;

    private final IDynamicPropertyEvaluator evaluator;

    public DynamicPropertyAdaptor(String code, IEntityAdaptor entityAdaptor,
            EntityPropertyPE propertyPE, IDynamicPropertyEvaluator evaluator)
    {
        this.code = code;
        this.entityAdaptor = entityAdaptor;
        this.propertyPE = propertyPE;
        this.evaluator = evaluator;
    }

    @Override
    public String valueAsString()
    {
        switch (state)
        {
            case EMPTY:
                // start evaluation
                state = State.EVALUATING;
                value = doEvaluate();
                state = State.EVALUATED;
                break;
            case EVALUATING:
                // cycle found - return an error
                StringBuilder path = new StringBuilder();
                for (EntityTypePropertyTypePE etpt : evaluator.getEvaluationPath())
                {
                    path.append(etpt.getPropertyType().getCode() + " -> ");
                }
                path.append(propertyTypeCode());
                String errorMsg =
                        String.format("cycle of dependencies found between dynamic properties: %s",
                                path.toString());
                value = DynamicPropertyEvaluator.errorPropertyValue(errorMsg);
                state = State.EVALUATED;
                break;
            case EVALUATED:
                // value was already computed
                break;
        }
        return value;
    }

    private String doEvaluate()
    {
        EntityTypePropertyTypePE etpt = propertyPE.getEntityTypePropertyType();
        return evaluator.evaluateProperty(entityAdaptor, etpt);
    }

    @Override
    public String renderedValue()
    {
        return valueAsString();
    }

    @Override
    public String propertyTypeCode()
    {
        return code;
    }

    public EntityPropertyPE getPropertyPE()
    {
        return propertyPE;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyTypeCode() + " " + valueAsString();
    }

}
