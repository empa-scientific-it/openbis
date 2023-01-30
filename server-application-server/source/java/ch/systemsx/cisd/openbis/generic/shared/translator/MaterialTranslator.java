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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collection.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * A {@link Material} &lt;---&gt; {@link MaterialPE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class MaterialTranslator
{

    private MaterialTranslator()
    {
        // Can not be instantiated.
    }

    public static NewMaterial translateToNewMaterial(Material material)
    {
        NewMaterial newMaterial = new NewMaterial(material.getCode());
        IEntityProperty[] properties = material.getProperties().toArray(new IEntityProperty[0]);
        newMaterial.setProperties(properties);
        return newMaterial;
    }

    public final static List<Material> translate(final List<MaterialPE> materials,
            Map<Long, Set<Metaproject>> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        final List<Material> result = new ArrayList<Material>();
        for (final MaterialPE material : materials)
        {
            result.add(MaterialTranslator.translate(material, metaprojects.get(material.getId()),
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
        }
        return result;
    }

    public final static Material translate(final MaterialPE materialPE,
            Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        return translate(materialPE, true, metaprojects, managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
    }

    public final static Material translate(final MaterialPE materialPE,
            final boolean withProperties, Collection<Metaproject> metaprojects,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (materialPE == null)
        {
            return null;
        }
        final Material result = new Material();
        result.setCode(materialPE.getCode());
        result.setId(HibernateUtils.getId(materialPE));
        result.setModificationDate(materialPE.getModificationDate());
        result.setMaterialType(MaterialTypeTranslator.translate(materialPE.getMaterialType(),
                new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>()));
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate());
        result.setRegistrator(PersonTranslator.translate(materialPE.getRegistrator()));
        result.setRegistrationDate(materialPE.getRegistrationDate());
        if (withProperties)
        {
            setProperties(materialPE, result, managedPropertyEvaluatorFactory, samplePropertyAccessValidator);
        }

        if (metaprojects != null)
        {
            result.setMetaprojects(metaprojects);
        }

        return result;
    }

    private static void setProperties(final MaterialPE materialPE, final Material result,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            IValidator<IIdentifierHolder> samplePropertyAccessValidator)
    {
        if (materialPE.isPropertiesInitialized())
        {
            result.setProperties(EntityPropertyTranslator.translate(materialPE.getProperties(),
                    new HashMap<MaterialTypePE, MaterialType>(), new HashMap<PropertyTypePE, PropertyType>(), 
                    managedPropertyEvaluatorFactory, samplePropertyAccessValidator));
        } else
        {
            result.setProperties(new ArrayList<IEntityProperty>());
        }
    }

}
