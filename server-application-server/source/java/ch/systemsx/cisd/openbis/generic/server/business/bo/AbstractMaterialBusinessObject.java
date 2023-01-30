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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.IEntityInformationProvider;

/**
 * Generic operations on materials.
 * 
 * @author Tomasz Pylak
 */
public class AbstractMaterialBusinessObject extends AbstractBusinessObject
{
    protected AbstractMaterialBusinessObject(final IDAOFactory daoFactory, final Session session,
            IEntityInformationProvider entityInformationProvider,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, EntityKind.MATERIAL, entityInformationProvider, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    protected AbstractMaterialBusinessObject(final IDAOFactory daoFactory, final Session session,
            final IEntityPropertiesConverter entityPropertiesConverter,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, entityPropertiesConverter, managedPropertyEvaluatorFactory,
                dataSetTypeChecker, relationshipService);
    }

    private static final String PROPERTY_TYPES = "materialType.materialTypePropertyTypesInternal";

    protected MaterialPE getMaterialById(final TechId materialId)
    {
        assert materialId != null : "Material technical id unspecified.";
        String[] connections =
                { PROPERTY_TYPES };
        final MaterialPE result = getMaterialDAO().tryGetByTechId(materialId, connections);
        if (result == null)
        {
            throw new UserFailureException(String.format("Material with ID '%s' does not exist.",
                    materialId));
        }
        return result;
    }

}
