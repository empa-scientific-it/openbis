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

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_PERSON;
import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.springframework.dao.DataIntegrityViolationException;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.util.TimeIntervalChecker;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IModifierAndModificationDateBean;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for corresponding {@link ExperimentBO} class.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses =
{ ExperimentPE.class, ExperimentBO.class })
public final class ExperimentBOTest extends AbstractBOTest
{

    private static final String EXP_TYPE_UNEXISTENT = "EXP-TYPE-UNEXISTENT";

    private static final String PROJECT_UNEXISTENT = "PROJECT-UNEXISTENT";

    private static final String GROUP = "GROUP";

    private static final String PROJECT = "PROJECT";

    private static final String EXP_TYPE_CODE = "EXP-TYPE-CODE";

    private static final String EXP_CODE = "EXP-CODE";

    private IEntityPropertiesHolder entityAsPropertiesHolder;

    private IModifierAndModificationDateBean entityAsModifiableBean;

    private final ExperimentBO createExperimentBO()
    {
        return new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, relationshipService, null,
                managedPropertyEvaluatorFactory, new DataSetTypeWithoutExperimentChecker("NE.*"));
    }

    @BeforeMethod
    public void setUp()
    {
        entityAsPropertiesHolder = context.mock(IEntityPropertiesHolder.class);
        entityAsModifiableBean = context.mock(IModifierAndModificationDateBean.class);
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).tryfindByCodeAndProject(with(any(String.class)), with(any(ProjectPE.class)));
                    will(returnValue(null));
                }
            });
    }

    @Test
    public void testLoadByExperimentIdentifier() throws Exception
    {
        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        prepareLoadExperimentByIdentifier(identifier, exp);
        loadExperiment(identifier, exp);
        context.assertIsSatisfied();
    }

    private void prepareUpdateProperties(final Set<ExperimentPropertyPE> oldProperties,
            final List<IEntityProperty> newProperties, final EntityTypePE entityType,
            final PersonPE registrator, final List<ExperimentPropertyPE> updated)
    {
        final Set<ExperimentPropertyPE> newProps = new HashSet<ExperimentPropertyPE>(updated);
        prepareUpdateProperties(oldProperties, newProperties, entityType, registrator, newProps);
    }

    private void prepareUpdateProperties(final Set<ExperimentPropertyPE> oldProperties,
            final List<IEntityProperty> newProperties, final EntityTypePE entityType,
            final PersonPE registrator, final Set<ExperimentPropertyPE> newProps)
    {
        context.checking(new Expectations()
            {
                {
                    one(propertiesConverter).updateProperties(oldProperties, entityType,
                            newProperties, registrator, Collections.<String> emptySet());
                    will(returnValue(newProps));

                }
            });
    }

    private void prepareLoadExperimentByIdentifier(final ExperimentIdentifier identifier,
            final ExperimentPE exp)
    {
        prepareAnyDaoCreation();
        final ProjectPE project = exp.getProject();
        prepareTryFindProject(identifier, project);
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
    }

    private void prepareTryFindProject(final ProjectIdentifier identifier,
            final ProjectPE foundProject)
    {
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(
                            identifier.getSpaceCode(), identifier.getProjectCode());
                    will(Expectations.returnValue(foundProject));
                }
            });

    }

    @Test
    public void testGetExperimentFileAttachment() throws Exception
    {
        final ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        final AttachmentPE attachment1 = CommonTestUtils.createAttachment();
        final AttachmentPE attachment2 = CommonTestUtils.createAttachment();
        attachment2.setVersion(attachment1.getVersion() + 1);
        setExperimentAttachments(exp, attachment1, attachment2);
        final ProjectPE project = exp.getProject();
        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    one(projectDAO).tryFindProject(
                            identifier.getSpaceCode(), identifier.getProjectCode());
                    will(returnValue(project));

                    one(experimentDAO).tryFindByCodeAndProject(project,
                            identifier.getExperimentCode());
                    will(returnValue(exp));

                }
            });
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);

        // Get first attachment
        AssertJUnit.assertEquals(
                attachment1,
                expBO.getExperimentFileAttachment(attachment1.getFileName(),
                        attachment1.getVersion()));

        // Get another version of attachment
        AssertJUnit.assertEquals(
                attachment2,
                expBO.getExperimentFileAttachment(attachment2.getFileName(),
                        attachment2.getVersion()));

        // Try find not existing version of attachment
        testThrowingExceptionOnUnknownFileVersion(attachment2, expBO);

        // Try find not existing attachment (incorrect file name)
        testThrowingExceptionOnUnknownFilename(attachment2, expBO);
    }

    @Test
    public void testDefineAndSave()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ProjectPE project = createProject(groupCode, projectCode);
        final ExperimentTypePE type = createExperimentType(expTypeCode);
        final ExperimentPE experiment = createExperiment(project, expCode, type);
        TimeIntervalChecker timeIntervalChecker = new TimeIntervalChecker(daoFactory.getTransactionTimestamp());

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    atLeast(1).of(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(Expectations.returnValue(new ArrayList<ExperimentTypePropertyTypePE>(type
                            .getExperimentTypePropertyTypes())));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(groupCode, projectCode);
                    will(returnValue(project));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010112341234-1"));

                    one(experimentDAO).createOrUpdateExperiment(experiment, EXAMPLE_PERSON);
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        experimentBO.define(newExperiment);
        experimentBO.save();

        assertEquals(EXAMPLE_PERSON, experimentBO.getExperiment().getModifier());
        assertEquals(EXAMPLE_PERSON, experimentBO.getExperiment().getProject().getModifier());
        timeIntervalChecker
                .assertDateInInterval(experimentBO.getExperiment().getModificationDate());
        timeIntervalChecker.assertDateInInterval(experimentBO.getExperiment().getProject()
                .getModificationDate());
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUnexistentExperimentType()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_UNEXISTENT;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(null));
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        boolean exceptionThrown = false;
        try
        {
            experimentBO.define(newExperiment);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertTrue(e.getMessage().indexOf(
                    String.format(ExperimentBO.ERR_EXPERIMENT_TYPE_NOT_FOUND, expTypeCode)) > -1);
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testDefineWithUnexistentProject()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT_UNEXISTENT;
        final String groupCode = GROUP;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ExperimentTypePE type = createExperimentType(expTypeCode);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(groupCode, projectCode);
                    will(returnValue(null));

                    exactly(2).of(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(returnValue(new ArrayList<EntityTypePropertyTypePE>()));

                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        boolean exceptionThrown = false;
        try
        {
            experimentBO.define(newExperiment);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
            assertTrue(e.getMessage().indexOf(
                    String.format(ExperimentBO.ERR_PROJECT_NOT_FOUND,
                            createIdentifier(groupCode, projectCode, expCode))) > -1);
        }
        assertTrue(exceptionThrown);

        context.assertIsSatisfied();
    }

    @Test
    public final void testDefineAndSaveAlreadyExistingExperiment()
    {
        final String expCode = EXP_CODE;
        final String expTypeCode = EXP_TYPE_CODE;
        final String projectCode = PROJECT;
        final String groupCode = GROUP;

        final NewExperiment newExperiment = new NewExperiment();
        newExperiment.setIdentifier(createIdentifier(groupCode, projectCode, expCode));
        newExperiment.setExperimentTypeCode(expTypeCode);

        final ProjectPE project = createProject(groupCode, projectCode);
        final ExperimentTypePE type = createExperimentType(expTypeCode);
        final ExperimentPE experiment = createExperiment(project, expCode, type);

        prepareAnyDaoCreation();
        context.checking(new Expectations()
            {
                {
                    allowing(entityTypeDAO).listEntityTypes();
                    will(Expectations.returnValue(Collections.singletonList(type)));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(type);
                    will(Expectations.returnValue(new ArrayList<ExperimentTypePropertyTypePE>(type
                            .getExperimentTypePropertyTypes())));

                    one(entityTypeDAO).tryToFindEntityTypeByCode(expTypeCode);
                    will(returnValue(type));

                    one(projectDAO).tryFindProject(groupCode, projectCode);
                    will(returnValue(project));

                    one(permIdDAO).createPermId();
                    will(returnValue("2009010101011111-1"));

                    one(experimentDAO).createOrUpdateExperiment(experiment, EXAMPLE_PERSON);
                    will(throwException(new DataIntegrityViolationException(
                            "exception description...")));
                }
            });
        final ExperimentBO experimentBO = createExperimentBO();
        experimentBO.define(newExperiment);
        boolean exceptionThrown = false;
        try
        {
            experimentBO.save();
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    private void prepareAnyDaoCreation()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getEntityPropertyTypeDAO(EntityKind.EXPERIMENT);
                    will(Expectations.returnValue(entityPropertyTypeDAO));

                    allowing(daoFactory).getEntityTypeDAO(EntityKind.EXPERIMENT);
                    will(Expectations.returnValue(entityTypeDAO));

                    allowing(daoFactory).getProjectDAO();
                    will(returnValue(projectDAO));

                    allowing(daoFactory).getExperimentDAO();
                    will(returnValue(experimentDAO));

                    allowing(daoFactory).getDataDAO();
                    will(returnValue(dataDAO));

                    allowing(daoFactory).getPermIdDAO();
                    will(returnValue(permIdDAO));
                }
            });
    }

    @Test
    public final void testEditProperties()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        ExperimentTypePE experimentType = CommonTestUtils.createExperimentType();
        exp.setExperimentType(experimentType);

        ExperimentPropertyPE changedProperty = CommonTestUtils.createOrganProperty(experimentType);
        ExperimentPropertyPE deletedProperty = CommonTestUtils.createNotesProperty(experimentType);
        ExperimentPropertyPE addedProperty = CommonTestUtils.createCategoryProperty(experimentType);
        exp.setProperties(new HashSet<ExperimentPropertyPE>(Arrays.asList(changedProperty,
                deletedProperty)));

        prepareLoadExperimentByIdentifier(identifier, exp);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);
        bo.loadByExperimentIdentifier(identifier);

        ExperimentPE experiment = bo.getExperiment();
        assertTrue(experiment.getProperties().contains(changedProperty));
        assertTrue(experiment.getProperties().contains(deletedProperty));
        assertFalse(experiment.getProperties().contains(addedProperty));

        final List<IEntityProperty> newProperties = createDummyProperties();
        prepareUpdateProperties(exp.getProperties(), newProperties, experimentType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(),
                Arrays.asList(changedProperty, addedProperty));
        bo.updateProperties(experiment.getEntityType(), newProperties, Collections.<String> emptySet(), experiment, experiment);

        assertTrue(experiment.getProperties().contains(changedProperty));
        assertFalse(experiment.getProperties().contains(deletedProperty));
        assertTrue(experiment.getProperties().contains(addedProperty));

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesWithSameProperties()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createNotesProperty(entityType);
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(),
                Arrays.asList(materialProp, stringProp, termProp));
        prepareEntity(existingProperties, null);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfUnchangedProperties()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE materialProp2 = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createNotesProperty(entityType);
        ExperimentPropertyPE stringProp2 = CommonTestUtils.createNotesProperty(entityType);
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        ExperimentPropertyPE termProp2 = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(),
                Arrays.asList(stringProp2, materialProp2, termProp2));
        prepareEntity(existingProperties, null);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfChangedStringProperty()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createStringProperty(entityType, "alpha");
        ExperimentPropertyPE changedStringProp =
                CommonTestUtils.createStringProperty(entityType, "beta");
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        final Set<ExperimentPropertyPE> newConvertedProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, changedStringProp,
                        termProp));
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), newConvertedProperties);
        prepareEntity(existingProperties, newConvertedProperties);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfChangedMaterialProperty()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE changedMaterialProp =
                CommonTestUtils.createMaterialProperty(entityType, "BETA");
        ExperimentPropertyPE stringProp = CommonTestUtils.createStringProperty(entityType, "alpha");
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        final Set<ExperimentPropertyPE> newConvertedProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(changedMaterialProp, stringProp,
                        termProp));
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), newConvertedProperties);
        prepareEntity(existingProperties, newConvertedProperties);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfChangedTermProperty()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createStringProperty(entityType, "alpha");
        ExperimentPropertyPE termProp =
                CommonTestUtils.createTermProperty(entityType, CommonTestUtils.BRAIN);
        ExperimentPropertyPE changedTermProp =
                CommonTestUtils.createTermProperty(entityType, CommonTestUtils.LEG);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        final Set<ExperimentPropertyPE> newConvertedProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp,
                        changedTermProp));
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), newConvertedProperties);
        prepareEntity(existingProperties, newConvertedProperties);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfWithAddedTermProperty()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createStringProperty(entityType, "alpha");
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        final Set<ExperimentPropertyPE> newConvertedProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), newConvertedProperties);
        prepareEntity(existingProperties, newConvertedProperties);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdatePropertiesOfWithRemovedMaterialProperty()
    {
        ExperimentTypePE entityType = CommonTestUtils.createExperimentType();
        ExperimentPropertyPE materialProp = CommonTestUtils.createMaterialProperty(entityType);
        ExperimentPropertyPE stringProp = CommonTestUtils.createStringProperty(entityType, "alpha");
        ExperimentPropertyPE termProp = CommonTestUtils.createOrganProperty(entityType);
        final Set<ExperimentPropertyPE> existingProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(materialProp, stringProp, termProp));
        List<IEntityProperty> newProperties = createDummyProperties();
        final Set<ExperimentPropertyPE> newConvertedProperties =
                new HashSet<ExperimentPropertyPE>(Arrays.asList(stringProp, termProp));
        prepareUpdateProperties(existingProperties, newProperties, entityType,
                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), newConvertedProperties);
        prepareEntity(existingProperties, newConvertedProperties);
        ExperimentBO bo =
                new ExperimentBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION, propertiesConverter,
                        managedPropertyEvaluatorFactory, null, null);

        bo.updateProperties(entityType, newProperties, Collections.<String> emptySet(), entityAsPropertiesHolder,
                entityAsModifiableBean);

        context.assertIsSatisfied();
    }

    private void prepareEntity(final Set<ExperimentPropertyPE> existingProperties,
            final Set<ExperimentPropertyPE> newConvertedPropertiesOrNull)
    {
        context.checking(new Expectations()
            {
                {
                    one(entityAsPropertiesHolder).getProperties();
                    will(returnValue(existingProperties));

                    if (newConvertedPropertiesOrNull != null)
                    {
                        one(entityAsPropertiesHolder).setProperties(newConvertedPropertiesOrNull);
                        one(entityAsModifiableBean).setModifier(
                                ManagerTestTool.EXAMPLE_SESSION.tryGetPerson());
                        one(entityAsModifiableBean).setModificationDate(with(any(Date.class)));
                    }
                }
            });
    }

    private List<IEntityProperty> createDummyProperties()
    {
        return new ArrayList<IEntityProperty>();
    }

    @Test
    public final void testEditProject()
    {

        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentTypePE experimentType = CommonTestUtils.createExperimentType();
        final ExperimentPE exp = CommonTestUtils.createExperiment(identifier);
        exp.setExperimentType(experimentType);
        SpacePE space = CommonTestUtils.createSpace(identifier);
        SamplePE assignedSample = createSampleWithCode("assignedSample");
        assignedSample.setSpace(space);
        exp.setSamples(Arrays.asList(assignedSample));
        // prepareLoadExperimentByIdentifier(identifier, exp);
        final ProjectIdentifier newProjectIdentifier =
                new ProjectIdentifier("anotherSpace", "anotherProject");
        final ProjectPE newProject = CommonTestUtils.createProject(newProjectIdentifier);
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        updates.setExperimentId(new TechId(exp));
        updates.setVersion(exp.getVersion());
        updates.setProjectIdentifier(newProjectIdentifier);
        updates.setProperties(Collections.<IEntityProperty> emptyList());
        updates.setAttachments(Collections.<NewAttachment> emptyList());
        prepareAnyDaoCreation();
        prepareLoadingExperiment(exp);
        context.checking(new Expectations()
            {
                {
                    one(relationshipService).assignExperimentToProject(
                            ManagerTestTool.EXAMPLE_SESSION, exp, newProject);
                }
            });
        prepareTryFindProject(newProjectIdentifier, newProject);

        ExperimentBO expBO = createExperimentBO();
        expBO.update(updates);

        assertFalse(newProject.equals(exp.getProject()));
        assertFalse(newProject.getSpace().equals(assignedSample.getSpace()));

    }

    @Test
    public void testUpdateByAddingASample()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE experiment = CommonTestUtils.createExperiment(identifier);
        experiment.addSample(createSampleWithCode("S1"));
        experiment.setProperties(Collections.<EntityPropertyPE> emptySet());
        prepareLoadingExperiment(identifier, experiment);
        ExperimentUpdatesDTO update = createDefaultUpdateObject(experiment);
        update.setSampleCodes(new String[]
        { "S1", "S2" });
        prepareAddSamplesToExperiment(experiment, false, true, "S2");
        ExperimentBO experimentBO = loadExperiment(identifier, experiment);

        experimentBO.update(update);

        context.assertIsSatisfied();
    }

    @Test(expectedExceptionsMessageRegExp = "Sample '/S2' is already assigned.*", expectedExceptions = UserFailureException.class)
    public void testUpdateByAddingAnAlreadyAssignedSample()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE experiment = CommonTestUtils.createExperiment(identifier);
        experiment.addSample(createSampleWithCode("S1"));
        experiment.setProperties(Collections.<EntityPropertyPE> emptySet());
        prepareLoadingExperiment(identifier, experiment);
        ExperimentUpdatesDTO update = createDefaultUpdateObject(experiment);
        update.setSampleCodes(new String[]
        { "S1", "S2" });
        prepareAddSamplesToExperiment(experiment, true, true, "S2");
        ExperimentBO experimentBO = loadExperiment(identifier, experiment);

        experimentBO.update(update);

        context.assertIsSatisfied();
    }

    @Test(expectedExceptionsMessageRegExp = "Samples with following codes do not exist "
            + "in the project '/HOME_GROUP/PROJECT_EVOLUTION' nor in the space 'HOME_GROUP': '\\[S2\\]'\\.", expectedExceptions = UserFailureException.class)
    public void testUpdateByAddingAnUnknownSample()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        final ExperimentPE experiment = CommonTestUtils.createExperiment(identifier);
        experiment.addSample(createSampleWithCode("S1"));
        experiment.setProperties(Collections.<EntityPropertyPE> emptySet());
        prepareLoadingExperiment(identifier, experiment);
        ExperimentUpdatesDTO update = createDefaultUpdateObject(experiment);
        update.setSampleCodes(new String[]
        { "S1", "S2" });
        context.checking(new Expectations()
            {
                {
                    SpacePE space = experiment.getProject().getSpace();
                    one(sampleDAO).tryFindByCodeAndSpace("S2", space, true);
                }
            });
        ExperimentBO experimentBO = loadExperiment(identifier, experiment);

        experimentBO.update(update);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateByAddingAndRemovingSamples()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE experiment = CommonTestUtils.createExperiment(identifier);
        experiment.addSample(createSampleWithCode("S1"));
        experiment.addSample(createSampleWithCode("S2"));
        experiment.setProperties(Collections.<EntityPropertyPE> emptySet());
        prepareLoadingExperiment(identifier, experiment);
        ExperimentUpdatesDTO update = createDefaultUpdateObject(experiment);
        update.setSampleCodes(new String[]
        { "S3", "S2" });
        prepareRemoveSamplesFromExperiment(experiment, false, "S1");
        prepareAddSamplesToExperiment(experiment, false, true, "S1", "S3");
        ExperimentBO experimentBO = loadExperiment(identifier, experiment);

        experimentBO.update(update);

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateByRemovingASampleWithDataSets()
    {
        ExperimentIdentifier identifier = CommonTestUtils.createExperimentIdentifier();
        ExperimentPE experiment = CommonTestUtils.createExperiment(identifier);
        experiment.addSample(createSampleWithCode("S1"));
        experiment.addSample(createSampleWithCode("S2"));
        experiment.setProperties(Collections.<EntityPropertyPE> emptySet());
        prepareLoadingExperiment(identifier, experiment);
        ExperimentUpdatesDTO update = createDefaultUpdateObject(experiment);
        update.setSampleCodes(new String[]
        { "S2" });
        context.checking(new Expectations()
            {
                {
                    SpacePE space = experiment.getProject().getSpace();
                    one(sampleDAO).tryFindByCodeAndSpace("S1", space, true);
                    SamplePE sample = createSampleWithCode("S1");
                    will(returnValue(sample));
                }
            });
        prepareRemoveSamplesFromExperiment(experiment, true, "S1");
        ExperimentBO experimentBO = loadExperiment(identifier, experiment);

        try
        {
            experimentBO.update(update);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Operation cannot be performed, because the sample /S1 has the "
                    + "following datasets which need an experiment: [DS1]", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private ExperimentUpdatesDTO createDefaultUpdateObject(ExperimentPE experiment)
    {
        ExperimentUpdatesDTO update = new ExperimentUpdatesDTO();
        update.setExperimentId(new TechId(experiment));
        update.setProjectIdentifier(new ExperimentIdentifierFactory(experiment.getIdentifier())
                .createIdentifier());
        update.setAttachments(Collections.<NewAttachment> emptyList());
        update.setProperties(Collections.<IEntityProperty> emptyList());
        return update;
    }

    private void prepareLoadingExperiment(ExperimentIdentifier identifier,
            final ExperimentPE experiment)
    {
        prepareLoadingExperiment(experiment);
        prepareLoadExperimentByIdentifier(identifier, experiment);
        context.checking(new Expectations()
            {
                {
                    ProjectPE projectPE = experiment.getProject();
                    SpacePE space = projectPE.getSpace();
                    one(projectDAO).tryFindProject(space.getCode(), projectPE.getCode());
                    will(returnValue(projectPE));
                }
            });
    }

    private void prepareAddSamplesToExperiment(final ExperimentPE experiment,
            final boolean alreadyAssignedToAnExperiment, boolean ignoringProject, final String... sampleCodes)
    {
        context.checking(new Expectations()
            {
                {
                    SpacePE space = experiment.getProject().getSpace();
                    for (String sampleCode : sampleCodes)
                    {
                        one(sampleDAO).tryFindByCodeAndSpace(sampleCode, space, ignoringProject);
                        SamplePE sample = createSampleWithCode(sampleCode);
                        if (alreadyAssignedToAnExperiment)
                        {
                            ExperimentPE anotherExperiment = new ExperimentPE();
                            anotherExperiment.setCode(EXP_CODE + "1");
                            anotherExperiment.setProject(experiment.getProject());
                            sample.setExperiment(anotherExperiment);
                        }
                        will(returnValue(sample));
                        if (alreadyAssignedToAnExperiment)
                        {
                            break;
                        }
                        allowing(relationshipService).assignSampleToExperiment(EXAMPLE_SESSION, sample,
                                experiment);
                    }
                }
            });
    }

    private void prepareRemoveSamplesFromExperiment(final ExperimentPE experiment,
            final boolean samplesHaveDataSets, final String... sampleCodes)
    {
        context.checking(new Expectations()
            {
                {
                    SpacePE space = experiment.getProject().getSpace();
                    for (String sampleCode : sampleCodes)
                    {
                        allowing(sampleDAO).tryFindByCodeAndSpace(sampleCode, space);
                        SamplePE sample = createSampleWithCode(sampleCode);
                        will(returnValue(sample));

                        one(dataDAO).listDataSets(sample);
                        if (samplesHaveDataSets)
                        {
                            DataPE dataPE = new DataPE();
                            dataPE.setCode("DS1");
                            DataSetTypePE dataSetType = new DataSetTypePE();
                            dataSetType.setCode("A");
                            dataPE.setDataSetType(dataSetType);
                            will(returnValue(Arrays.asList(dataPE)));
                            break;
                        } else
                        {
                            will(returnValue(Arrays.asList()));
                        }
                        one(relationshipService).unassignSampleFromExperiment(EXAMPLE_SESSION,
                                sample);
                    }
                }
            });
    }

    private void prepareLoadingExperiment(final ExperimentPE experiment)
    {
        context.checking(new Expectations()
            {
                {
                    one(experimentDAO).tryGetByTechId(new TechId(experiment),
                            ExperimentBO.PROPERTY_TYPES);
                    will(returnValue(experiment));

                    one(entityTypeDAO).listEntityTypes();
                    will(returnValue(Arrays.asList(experiment.getExperimentType())));

                    allowing(entityPropertyTypeDAO).listEntityPropertyTypes(
                            experiment.getExperimentType());
                }
            });
    }

    @Test
    public void testDelete()
    {
        final List<TechId> experimentIds = new ArrayList<TechId>();
        experimentIds.add(new TechId(1L));
        experimentIds.add(new TechId(2L));
        experimentIds.add(new TechId(3L));
        final String reason = "reason";
        context.checking(new Expectations()
            {
                {
                    PersonPE registrator = EXAMPLE_SESSION.tryGetPerson();
                    one(experimentDAO).delete(experimentIds, registrator, reason);
                }
            });

        final ExperimentBO expBO = createExperimentBO();
        expBO.deleteByTechIds(experimentIds, reason);
        context.assertIsSatisfied();
    }

    private ExperimentBO loadExperiment(final ExperimentIdentifier identifier,
            final ExperimentPE exp)
    {
        final ExperimentBO expBO = createExperimentBO();
        expBO.loadByExperimentIdentifier(identifier);
        AssertJUnit.assertEquals(exp, expBO.getExperiment());
        return expBO;
    }

    private static SamplePE createSampleWithCode(String code)
    {
        SamplePE s = CommonTestUtils.createSample();
        s.setCode(code);
        return s;
    }

    private static String createIdentifier(final String groupCode,
            final String projectCode, final String expCode)
    {
        return "/" + groupCode + "/" + projectCode + "/" + expCode;
    }

    private static ExperimentPE createExperiment(ProjectPE project, final String expCode,
            ExperimentTypePE type)
    {
        ExperimentPE experiment = new ExperimentPE();
        experiment.setCode(expCode);
        experiment.setExperimentType(type);
        experiment.setProject(project);
        return experiment;
    }

    private static ProjectPE createProject(final String groupCode,
            final String projectCode)
    {
        ProjectPE project = new ProjectPE();
        project.setCode(projectCode);
        final SpacePE group = new SpacePE();
        group.setCode(groupCode);
        project.setSpace(group);
        return project;
    }

    private static ExperimentTypePE createExperimentType(final String expTypeCode)
    {
        ExperimentTypePE experimentType = new ExperimentTypePE();
        experimentType.setCode(expTypeCode);
        return experimentType;
    }

    private static void setExperimentAttachments(final ExperimentPE exp,
            final AttachmentPE attachment1, final AttachmentPE attachment2)
    {
        final HashSet<AttachmentPE> set = new HashSet<AttachmentPE>();
        set.add(attachment1);
        set.add(attachment2);
        exp.setInternalAttachments(set);
    }

    private static void testThrowingExceptionOnUnknownFileVersion(final AttachmentPE attachment,
            final ExperimentBO expBO)
    {
        boolean exceptionThrown = false;
        try
        {
            expBO.getExperimentFileAttachment(attachment.getFileName(),
                    attachment.getVersion() + 100);
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        } finally
        {
            AssertJUnit.assertTrue(exceptionThrown);
        }
    }

    private static void testThrowingExceptionOnUnknownFilename(final AttachmentPE attachment2,
            final ExperimentBO expBO)
    {
        boolean exceptionThrown;
        exceptionThrown = false;
        try
        {
            expBO.getExperimentFileAttachment("nonexistentAttachment.txt", attachment2.getVersion());
        } catch (UserFailureException e)
        {
            exceptionThrown = true;
        } finally
        {
            AssertJUnit.assertTrue(exceptionThrown);
        }
    }

}