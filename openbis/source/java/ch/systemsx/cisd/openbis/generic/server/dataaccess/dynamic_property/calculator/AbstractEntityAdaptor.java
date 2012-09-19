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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.IDynamicPropertyEvaluator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityAdaptor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property.calculator.api.IEntityPropertyAdaptor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Abstract {@link IEntityAdaptor} implementation.
 * 
 * @author Piotr Buczek
 */
public class AbstractEntityAdaptor implements IEntityAdaptor
{
    protected final Map<String, IEntityPropertyAdaptor> propertiesByCode =
            new HashMap<String, IEntityPropertyAdaptor>();

    private final String code;

    protected final IDynamicPropertyEvaluator evaluator;

    public AbstractEntityAdaptor(String code)
    {
        this(code, null);
    }

    public AbstractEntityAdaptor(String code, IDynamicPropertyEvaluator evaluator)
    {
        this.code = code;
        this.evaluator = evaluator;
    }

    protected void initProperties(IEntityPropertiesHolder propertiesHolder)
    {
        if (evaluator == null)
        {
            throw new IllegalStateException(
                    "Couldn't init properties if the evaluator has not been set");
        }
        for (EntityPropertyPE property : propertiesHolder.getProperties())
        {
            EntityTypePropertyTypePE etpt = property.getEntityTypePropertyType();
            final PropertyTypePE propertyType = etpt.getPropertyType();
            final String propertyTypeCode = propertyType.getCode();
            if (etpt.isDynamic())
            {
                addProperty(new DynamicPropertyAdaptor(propertyTypeCode, this, property, evaluator));
            } else
            {
                final String value;
                if (property.getMaterialValue() != null)
                {
                    final MaterialPE material = property.getMaterialValue();
                    value =
                            MaterialIdentifier.print(material.getCode(), material.getEntityType()
                                    .getCode());
                } else if (property.getVocabularyTerm() != null)
                {
                    value = property.getVocabularyTerm().getCode();
                } else
                {
                    value = property.getValue();
                }
                if (propertyType.getTransformation() == null)
                {
                    addProperty(new BasicPropertyAdaptor(propertyTypeCode, value, property));
                } else
                {
                    addProperty(new XmlPropertyAdaptor(propertyTypeCode, value, property,
                            propertyType.getTransformation()));
                }
            }
        }
    }

    public void addProperty(IEntityPropertyAdaptor property)
    {
        propertiesByCode.put(property.propertyTypeCode().toUpperCase(), property);
    }

    @Override
    public String code()
    {
        return code;
    }

    @Override
    public IEntityPropertyAdaptor property(String propertyTypeCode)
    {
        return propertiesByCode.get(propertyTypeCode.toUpperCase());
    }

    @Override
    public String propertyValue(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.valueAsString();
    }

    @Override
    public String propertyRendered(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.renderedValue();
    }

    @Override
    public Collection<IEntityPropertyAdaptor> properties()
    {
        return propertiesByCode.values();
    }

}
