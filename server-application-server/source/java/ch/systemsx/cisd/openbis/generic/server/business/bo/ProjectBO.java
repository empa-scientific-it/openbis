/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.EntityHistoryCreator;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletedExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IProjectBO}. We are using an interface here to keep the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class ProjectBO extends AbstractBusinessObject implements IProjectBO
{

    /**
     * The business object held by this implementation.
     * <p>
     * Package protected so that <i>Unit Test</i> can access it.
     * </p>
     */
    private ProjectPE project;

    private boolean dataChanged;

    private final List<AttachmentPE> attachments = new ArrayList<AttachmentPE>();

    private EntityHistoryCreator historyCreator;

    public ProjectBO(final IDAOFactory daoFactory, final Session session,
            IRelationshipService relationshipService,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, EntityHistoryCreator historyCreator)
    {
        super(daoFactory, session, managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
        this.historyCreator = historyCreator;
    }

    private ProjectPE createProject(final NewProject newProject, String leaderIdOrNull)
    {
        final ProjectPE result = new ProjectPE();
        ProjectIdentifier projectIdentifier =
                new ProjectIdentifierFactory(newProject.getIdentifier()).createIdentifier();
        final SpacePE group =
                SpaceIdentifierHelper.tryGetSpace(projectIdentifier, session.tryGetPerson(), this);
        result.setSpace(group);
        result.setRegistrator(findPerson());
        result.setPermId(getOrCreatePermID(newProject));
        result.setCode(projectIdentifier.getProjectCode());
        result.setDescription(newProject.getDescription());
        if (leaderIdOrNull != null)
        {
            PersonPE leader = getPersonDAO().tryFindPersonByUserId(leaderIdOrNull);
            if (leader == null)
            {
                throw new UserFailureException("Person '%s' not found in the database.");
            }
            result.setProjectLeader(leader);
        }
        addAttachments(result, newProject.getAttachments(), attachments);
        return result;
    }

    @Override
    public final void save()
    {
        assert project != null : "Can not save an undefined project.";
        if (dataChanged)
        {
            try
            {
                getProjectDAO().createProject(project, findPerson());
            } catch (final DataAccessException ex)
            {
                throwException(ex, "Project '" + project.getCode() + "'");
            }
            dataChanged = false;
        }
        saveAttachment(project, attachments);
    }

    @Override
    public final ProjectPE getProject()
    {
        return project;
    }

    @Override
    public ProjectPE tryFindByProjectId(IProjectId projectId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("Project id cannot be null");
        }
        if (projectId instanceof ProjectIdentifierId)
        {
            ProjectIdentifierId identifierId = (ProjectIdentifierId) projectId;
            ProjectIdentifier identifier =
                    new ProjectIdentifierFactory(identifierId.getIdentifier()).createIdentifier();
            return tryFindByIdentifier(identifier);
        }
        if (projectId instanceof ProjectPermIdId)
        {
            ProjectPermIdId permIdId = (ProjectPermIdId) projectId;
            return getProjectDAO().tryGetByPermID(permIdId.getPermId());
        }
        if (projectId instanceof ProjectTechIdId)
        {
            ProjectTechIdId techIdId = (ProjectTechIdId) projectId;
            return getProjectDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        }
        throw new IllegalArgumentException("Unsupported project id: " + projectId);
    }

    private ProjectPE tryFindByIdentifier(ProjectIdentifier identifier)
    {
        return getProjectDAO().tryFindProject(
                identifier.getSpaceCode(), identifier.getProjectCode());
    }

    @Override
    public void define(final NewProject newProject, String creatorId) throws UserFailureException
    {
        assert newProject != null : "Unspecified new project.";
        assert newProject.getIdentifier() != null : "Unspecified project identifier.";

        this.project = createProject(newProject, creatorId);
        dataChanged = true;
    }

    @Override
    public void loadByProjectIdentifier(ProjectIdentifier identifier)
    {
        String spaceCode = identifier.getSpaceCode();
        String projectCode = identifier.getProjectCode();
        project = getProjectDAO().tryFindProject(spaceCode, projectCode);
        if (project == null)
        {
            throw new UserFailureException(
                    String.format("Project '%s' does not exist.", identifier));
        }
        dataChanged = false;
    }

    @Override
    public void loadByPermId(String permId)
    {
        project = getProjectDAO().tryGetByPermID(permId);
        if (project == null)
        {
            throw new UserFailureException(String.format(
                    "Project with PERM_ID '%s' does not exist.", permId));
        }
        dataChanged = false;
    }

    @Override
    public void loadDataByTechId(TechId projectId)
    {
        try
        {
            project = getProjectDAO().getByTechId(projectId);
        } catch (ObjectRetrievalFailureException exception)
        {
            throw new UserFailureException(String.format("Project with ID '%s' does not exist.",
                    projectId));
        }
        dataChanged = false;
    }

    @Override
    public final void addAttachment(final AttachmentPE attachment)
    {
        assert project != null : "no project has been loaded";
        prepareAttachment(project, attachment);
        attachments.add(attachment);
    }

    @Override
    public AttachmentPE tryGetProjectFileAttachment(final String filename, final Integer versionOrNull)
    {
        checkProjectLoaded();
        project.ensureAttachmentsLoaded();
        AttachmentPE att =
                versionOrNull == null ? getAttachment(filename) : getAttachment(filename,
                        versionOrNull);
        if (att != null)
        {
            HibernateUtils.initialize(att.getAttachmentContent());
            return att;
        } else
        {
            return null;
        }
    }

    @Override
    public AttachmentPE getProjectFileAttachment(final String filename, final Integer versionOrNull)
    {
        AttachmentPE attachment = tryGetProjectFileAttachment(filename, versionOrNull);

        if (attachment != null)
        {
            return attachment;
        } else
        {
            throw new UserFailureException(
                    "Attachment '"
                            + filename
                            + "' "
                            + (versionOrNull == null ? "(latest version)" : "(version '"
                                    + versionOrNull + "')")
                            + " not found in project '"
                            + project.getIdentifier() + "'.");
        }
    }

    private AttachmentPE getAttachment(String filename, final int version)
    {
        final Set<AttachmentPE> attachmentsSet = project.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename) && att.getVersion() == version)
            {
                return att;
            }
        }

        return null;
    }

    private AttachmentPE getAttachment(String filename)
    {
        AttachmentPE latest = null;
        final Set<AttachmentPE> attachmentsSet = project.getAttachments();
        for (AttachmentPE att : attachmentsSet)
        {
            if (att.getFileName().equals(filename))
            {
                if (latest == null || latest.getVersion() < att.getVersion())
                {
                    latest = att;
                }
            }
        }

        return latest;
    }

    private void checkProjectLoaded()
    {
        if (project == null)
        {
            throw new IllegalStateException("Unloaded project.");
        }
    }

    @Override
    public final void enrichWithAttachments()
    {
        if (project != null)
        {
            project.ensureAttachmentsLoaded();
        }
    }

    @Override
    public void update(ProjectUpdatesDTO updates)
    {
        loadDataByTechId(updates.getTechId());
        if (updates.getVersion() != project.getVersion())
        {
            throwModifiedEntityException("Project");
        }
        project.setDescription(updates.getDescription());
        for (NewAttachment attachment : updates.getAttachments())
        {
            attachments.add(prepareAttachment(project, attachment));
        }
        String groupCode = updates.getSpaceCode();
        if (groupCode != null && groupCode.equals(project.getSpace().getCode()) == false)
        {

            relationshipService.assignProjectToSpace(session, project, findGroup(groupCode));
        }
        dataChanged = true;
        
        //Schedule projects experiments for index update, they require to update the Space on the index at least
        reindex(ExperimentPE.class, project.getExperiments());
    }

    private SpacePE findGroup(String groupCode)
    {
        SpacePE group =
                getSpaceDAO().tryFindSpaceByCode(groupCode);
        if (group == null)
        {
            throw UserFailureException
                    .fromTemplate("No space with the name '%s' found!", groupCode);
        }
        return group;
    }

    private static final String propertyHistoryQuery =
            "SELECT 1 as a, 1 as b, 1 as c, 1 as d, 1 as e, 1 as f, 1 as g, 1 as h, 1 as i FROM materials WHERE id = -1 and id IN (:entityIds)";

    private static final String ENTITY_TYPE = "case "
            + "when h.space_id is not null then 'SPACE' "
            + "when h.expe_id is not null then 'EXPERIMENT' "
            + "else 'UNKNOWN' end as entity_type";

    private static final String relationshipHistoryQuery =
            "SELECT p.perm_id, h.relation_type, h.entity_perm_id, " + ENTITY_TYPE + ", "
                    + "pers.user_id, h.valid_from_timestamp, h.valid_until_timestamp "
                    + "FROM projects p, project_relationships_history h, persons pers "
                    + "WHERE p.id = h.main_proj_id AND "
                    + "h.main_proj_id IN (:entityIds) AND "
                    + "h.pers_id_author = pers.id "
                    + "ORDER BY 1, valid_from_timestamp";

    public static final String sqlAttributesHistory =
            "SELECT p.id, p.code, p.perm_id, p.description, "
                    + "p.registration_timestamp, r.user_id as registrator "
                    + "FROM projects p "
                    + "JOIN persons r on p.pers_id_registerer = r.id "
                    + "WHERE p.id IN (:entityIds)";

    @Override
    public void deleteByTechId(TechId projectId, String reason) throws UserFailureException
    {
        loadDataByTechId(projectId);
        try
        {
            List<String> codes = new ArrayList<String>();
            List<String> trashedCodes = new ArrayList<String>();
            List<ExperimentPE> experiments =
                    getExperimentDAO().listExperimentsWithProperties(Collections.singletonList(project), false, false);
            for (ExperimentPE experiment : experiments)
            {
                codes.add(experiment.getCode());
            }
            IDeletionDAO deletionDAO = getDeletionDAO();
            List<DeletionPE> deletionPEs = deletionDAO.listAllEntities();
            List<TechId> ids = new ArrayList<TechId>();
            for (DeletionPE deletion : deletionPEs)
            {
                ids.add(new TechId(deletion.getId()));
            }
            List<TechId> deletedExperimentIds = deletionDAO.findTrashedExperimentIds(ids);
            List<DeletedExperimentPE> deletedExperiments =
                    cast(deletionDAO.listDeletedEntities(EntityKind.EXPERIMENT,
                            deletedExperimentIds));
            for (DeletedExperimentPE deletedExperiment : deletedExperiments)
            {
                if (deletedExperiment.getProject().getId() == project.getId())
                {
                    trashedCodes.add(deletedExperiment.getCode());
                }
            }
            if (codes.isEmpty() && trashedCodes.isEmpty())
            {
                List<Long> idsToDelete = Collections.singletonList(projectId.getId());
                String content = historyCreator.apply(getSessionFactory().getCurrentSession(), idsToDelete,
                        propertyHistoryQuery, relationshipHistoryQuery, sqlAttributesHistory,
                        Arrays.asList(project), null, session.tryGetPerson());
                getProjectDAO().delete(project);
                getEventDAO().persist(createDeletionEvent(project, session.tryGetPerson(), reason, content));
            } else
            {
                StringBuilder builder = new StringBuilder();
                if (codes.isEmpty() == false)
                {
                    builder.append("the following experiments still exist: ");
                    builder.append(CollectionUtils.abbreviate(codes, 10));
                }
                if (trashedCodes.isEmpty() == false)
                {
                    if (codes.isEmpty() == false)
                    {
                        builder.append("\nIn addition ");
                    }
                    builder.append("the following experiments are in the trash can: ");
                    builder.append(CollectionUtils.abbreviate(trashedCodes, 10));
                }
                throw new UserFailureException("Project '" + project.getCode()
                        + "' can not be deleted because " + builder);
            }
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Project '%s'", project.getCode()));
        }
    }

    public static EventPE createDeletionEvent(ProjectPE project, PersonPE registrator, String reason, String content)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.PROJECT);
        event.setIdentifiers(Collections.singletonList(project.getPermId()));
        event.setDescription(getDeletionDescription(project));
        event.setReason(reason);
        event.setRegistrator(registrator);
        event.setContent(content);
        return event;
    }

    @SuppressWarnings("unchecked")
    private final static <T> T cast(final Object object)
    {
        return (T) object;
    }

    private static String getDeletionDescription(ProjectPE project)
    {
        return String.format("%s", project.getIdentifier());
    }
}
