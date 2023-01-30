/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialTypeCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.entity.AbstractCreateEntityTypeExecutor;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IEntityTypeBO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
@Component
public class CreateMaterialTypesExecutor extends AbstractCreateEntityTypeExecutor<MaterialTypeCreation, MaterialType, MaterialTypePE>
        implements ICreateMaterialTypeExecutor
{

    @Autowired
    private IMaterialTypeAuthorizationExecutor authorizationExecutor;

    @Override
    protected ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind getPEEntityKind()
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind.MATERIAL;
    }

    @Override
    protected EntityKind getDAOEntityKind()
    {
        return EntityKind.MATERIAL;
    }

    @Override
    protected MaterialType newType()
    {
        return new MaterialType();
    }

    @Override
    protected void checkTypeSpecificFields(MaterialTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void fillTypeSpecificFields(MaterialType type, MaterialTypeCreation creation)
    {
        // nothing to do
    }

    @Override
    protected void defineType(IOperationContext context, MaterialType type)
    {
        IEntityTypeBO typeBO = businessObjectFactory.createEntityTypeBO(context.getSession());
        typeBO.define(type);
        typeBO.save();
    }

    @Override
    protected void checkAccess(IOperationContext context)
    {
        authorizationExecutor.canCreate(context);
    }

}
