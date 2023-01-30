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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * Manipulations on {@link EntityTypePE} subclasses.
 * 
 * @author Tomasz Pylak
 */
public final class EntityTypeBO extends AbstractBusinessObject implements IEntityTypeBO
{
    public static void assertValidDataSetTypeMainPattern(String pattern)
    {
        if (pattern == null)
        {
            return;
        }
        try
        {
            Pattern.compile(pattern);
        } catch (PatternSyntaxException ex)
        {
            int index = ex.getIndex();
            throw new UserFailureException("The pattern '" + pattern + "' is invalid: "
                    + ex.getDescription() + (index < 0 ? "" : " at position " + (index + 1) + "."));
        }
    }

    private EntityTypePE entityTypePE;

    private EntityKind entityKind;

    public EntityTypeBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    private ScriptPE getValidationScriptPE(EntityType entityType)
    {
        if (entityType.getValidationScript() == null
                || entityType.getValidationScript().getName() == null
                || entityType.getValidationScript().getName().equals(""))
        {
            return null;
        } else
        {
            ScriptPE script =
                    getScriptDAO().tryFindByName(entityType.getValidationScript().getName());

            if (script != null && entityType.isEntityKind(script.getEntityKind()))
            {
                return script;
            } else
            {
                return null;
            }
        }
    }

    private EntityTypePE convertGeneric(EntityType entityType, EntityKind kind) throws UserFailureException
    {
        EntityTypePE typePE = EntityTypePE.createEntityTypePE(kind);
        typePE.setCode(entityType.getCode());
        typePE.setDescription(entityType.getDescription());
        typePE.setValidationScript(getValidationScriptPE(entityType));

        return typePE;
    }

    @Override
    public final void save() throws UserFailureException
    {
        assert entityTypePE != null : "Entity type not defined.";
        assert entityKind != null : "Entity kind not defined.";
        try
        {
            getEntityTypeDAO(entityKind).createOrUpdateEntityType(entityTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("Entity type '%s' ", entityTypePE.getCode()));
        }
    }

    @Override
    public void define(SampleType entityType)
    {
        SampleTypePE sampleTypePE = new SampleTypePE();
        sampleTypePE.setCode(entityType.getCode());
        sampleTypePE.setDescription(entityType.getDescription());
        sampleTypePE.setContainerHierarchyDepth(entityType.getContainerHierarchyDepth());
        sampleTypePE.setGeneratedFromHierarchyDepth(entityType.getGeneratedFromHierarchyDepth());
        sampleTypePE.setListable(entityType.isListable());
        sampleTypePE.setSubcodeUnique(entityType.isSubcodeUnique());
        sampleTypePE.setAutoGeneratedCode(entityType.isAutoGeneratedCode());
        sampleTypePE.setShowParentMetadata(entityType.isShowParentMetadata());
        sampleTypePE.setGeneratedCodePrefix(entityType.getGeneratedCodePrefix());
        sampleTypePE.setValidationScript(getValidationScriptPE(entityType));

        this.entityKind = EntityKind.SAMPLE;
        this.entityTypePE = sampleTypePE;
    }

    @Override
    public void define(MaterialType entityType)
    {
        this.entityKind = EntityKind.MATERIAL;
        this.entityTypePE = convertGeneric(entityType, entityKind);
    }

    @Override
    public void define(ExperimentType entityType)
    {
        this.entityKind = EntityKind.EXPERIMENT;
        this.entityTypePE = convertGeneric(entityType, entityKind);
    }

    @Override
    public void define(DataSetType entityType)
    {
        DataSetTypePE dataSetTypePE = new DataSetTypePE();
        dataSetTypePE.setCode(entityType.getCode());
        dataSetTypePE.setDescription(entityType.getDescription());
        dataSetTypePE.setMainDataSetPath(entityType.getMainDataSetPath());
        String mainDataSetPattern = entityType.getMainDataSetPattern();
        assertValidDataSetTypeMainPattern(mainDataSetPattern);
        dataSetTypePE.setMainDataSetPattern(mainDataSetPattern);
        dataSetTypePE.setDeletionDisallow(entityType.isDeletionDisallow());
        dataSetTypePE.setValidationScript(getValidationScriptPE(entityType));

        this.entityKind = EntityKind.DATA_SET;
        this.entityTypePE = dataSetTypePE;
    }

    @Override
    public void load(EntityKind kind, String code)
    {
        this.entityKind = kind;
        this.entityTypePE = getEntityTypeDAO(entityKind).tryToFindEntityTypeByCode(code);
        if (entityTypePE == null)
        {
            throw new UserFailureException(String.format("'%s' not found.", code));
        }
    }

    @Override
    public void delete()
    {
        assert entityKind != null;
        assert entityTypePE != null : "Type not loaded";
        try
        {
            List<TechId> entitiesInTrash =
                    getDeletionDAO().listDeletedEntitiesForType(entityKind,
                            new TechId(entityTypePE.getId()));
            if (false == entitiesInTrash.isEmpty())
            {
                throw UserFailureException.fromTemplate(
                        "'%s' is referred from entities in the trash can. "
                                + "Please empty the trash can and try deleting it again.",
                        entityTypePE.getCode());
            }
            getEntityTypeDAO(entityKind).deleteEntityType(entityTypePE);
        } catch (final DataAccessException e)
        {
            throwException(e, String.format("'%s'", entityTypePE.getCode()));
        }
    }
}
