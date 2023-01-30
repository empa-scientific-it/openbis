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

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.generic.shared.IJythonEvaluatorPool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IScriptUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * The only productive implementation of {@link IScriptBO}. We are using an interface here to keep the system testable.
 * 
 * @author Izabela Adamczyk
 */
public final class ScriptBO extends AbstractBusinessObject implements IScriptBO
{

    private ScriptPE script;

    private final IScriptFactory scriptFactory;

    private final IJythonEvaluatorPool jythonEvaluatorPool;

    public ScriptBO(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService,
            IJythonEvaluatorPool jythonEvaluationPool)
    {
        this(daoFactory, session, new ScriptFactory(), managedPropertyEvaluatorFactory, dataSetTypeChecker,
                relationshipService, jythonEvaluationPool);
    }

    @Private
    // for testing
    ScriptBO(final IDAOFactory daoFactory, final Session session, IScriptFactory scriptFactory,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker,
            IRelationshipService relationshipService,
            IJythonEvaluatorPool jythonEvaluationPool)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.scriptFactory = scriptFactory;
        this.jythonEvaluatorPool = jythonEvaluationPool;
    }

    @Private
    interface IScriptFactory
    {
        ScriptPE create();
    }

    private static class ScriptFactory implements IScriptFactory
    {
        @Override
        public ScriptPE create()
        {
            return new ScriptPE();
        }
    }

    @Override
    public ScriptPE deleteByTechId(TechId groupId) throws UserFailureException
    {
        loadDataByTechId(groupId);
        try
        {
            getScriptDAO().delete(script);
            return script;
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Script '%s'", script.getName()));
            return null; // never invoked
        }
    }

    @Override
    public void loadDataByTechId(TechId id)
    {
        try
        {
            script = getScriptDAO().getByTechId(id);
        } catch (DataRetrievalFailureException exception)
        {
            throw new UserFailureException(exception.getMessage());
        }
    }

    @Override
    public void save() throws UserFailureException
    {
        assert script != null : "Script not defined";
        try
        {
            PluginUtils.checkScriptCompilation(script, jythonEvaluatorPool);
            getScriptDAO().createOrUpdate(script);
        } catch (final DataAccessException e)
        {
            throwException(e, "Script '" + script.getName() + "'");
        }
    }

    @Override
    public void define(Script newScript) throws UserFailureException
    {
        assert newScript != null : "Unspecified script.";
        script = scriptFactory.create();
        script.setName(newScript.getName());
        script.setDescription(newScript.getDescription());
        script.setRegistrator(findPerson());
        script.setScript(newScript.getScript());
        script.setScriptType(newScript.getScriptType());
        script.setPluginType(newScript.getPluginType());
        script.setEntityKind(newScript.getEntityKind() == null ? null
                : newScript.getEntityKind().length == 1 ? newScript.getEntityKind()[0] : null);
        script.setAvailable(true);
    }

    @Override
    public void tryDefineOrUpdateIfPossible(Script newScript)
    {
        assert newScript != null : "Script cannot be null";
        assert newScript.getName() != null : "Script name cannot be null";

        IScriptDAO scriptDAO = getScriptDAO();
        script = scriptDAO.tryFindByName(newScript.getName());
        if (script == null)
        {
            define(newScript);
            save();
        } else
        {
            if (newScript.getPluginType() == script.getPluginType()
                    && newScript.getScriptType() == script.getScriptType())
            {
                script.setDescription(newScript.getDescription());
                script.setAvailable(newScript.isAvailable());
                script.setScript(newScript.getScript());
                script.setEntityKind(newScript.getEntityKind() == null ? null : newScript
                        .getEntityKind().length == 1 ? newScript.getEntityKind()[0] : null);
                save();
                if (script.isDynamic())
                {
                    scheduleDynamicPropertiesEvaluation();
                }
            } else
            {
                StringBuilder sb = new StringBuilder("Cannot register ");
                sb.append(newScript.getPluginType())
                        .append(" ")
                        .append(newScript.getScriptType())
                        .append(" plugin '")
                        .append(newScript.getName())
                        .append("' because plugin of different kind with the same name already exists.");
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    @Override
    public void tryDeleteOrInvalidatePredeployedPlugin(String name, ScriptType scriptType)
    {
        assert name != null : "Script name cannot be null";
        assert scriptType != null : "Script type cannot be null";

        IScriptDAO scriptDAO = getScriptDAO();
        script = scriptDAO.tryFindByName(name);

        if (script != null)
        {
            if (script.getPluginType() == PluginType.PREDEPLOYED
                    && script.getScriptType() == scriptType)
            {
                script.setAvailable(false);
                save();
            } else
            {
                StringBuilder sb = new StringBuilder("Cannot delete ");
                sb.append(scriptType)
                        .append(" plugin '")
                        .append(name)
                        .append("' because plugin of different kind with the same name already exists.");
                throw new IllegalArgumentException(sb.toString());
            }
        }
    }

    @Override
    public void update(IScriptUpdates updates)
    {
        loadDataByTechId(TechId.create(updates));
        if (script.getModificationDate().equals(updates.getModificationDate()) == false)
        {
            throwModifiedEntityException("Plugin");
        }
        script.setName(updates.getName());
        script.setDescription(updates.getDescription());
        boolean scriptChanged = false;
        if (script.getScript() != updates.getScript()
                && script.getScript().equals(updates.getScript()) == false)
        {
            scriptChanged = true;
            script.setScript(updates.getScript());
            PluginUtils.checkScriptCompilation(script, jythonEvaluatorPool);
        }
        getScriptDAO().createOrUpdate(script);

        script.getScript();
        if (scriptChanged && script.isDynamic())
        {
            scheduleDynamicPropertiesEvaluation();
        }
    }

    @Override
    public final ScriptPE getScript() throws IllegalStateException
    {
        if (script == null)
        {
            throw new IllegalStateException("Unloaded script.");
        }
        return script;
    }

    private void scheduleDynamicPropertiesEvaluation()
    {
        for (EntityTypePropertyTypePE assignment : script.getPropertyAssignments())
        {
            getEntityPropertyTypeDAO(assignment.getEntityType().getEntityKind())
                    .scheduleDynamicPropertiesEvaluation(assignment);
        }
    }
}
