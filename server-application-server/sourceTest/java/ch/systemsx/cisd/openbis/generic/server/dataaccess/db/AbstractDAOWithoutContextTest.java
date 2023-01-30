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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ScriptType;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses =
{ AbstractDAO.class })
public abstract class AbstractDAOWithoutContextTest extends
        AbstractTransactionalTestNGSpringContextTests
{
    static
    {
        TestInitializer.init();
    }

    static final Long ANOTHER_DATABASE_INSTANCE_ID = Long.valueOf(2);

    static final String EXCEED_CODE_LENGTH_CHARACTERS = StringUtils.repeat("A",
            Code.CODE_LENGTH_MAX + 1);

    protected IDAOFactory daoFactory;

    protected SessionFactory sessionFactory;

    private Long origDatabaseInstanceId;

    private Object currentDAO;

    @BeforeMethod(alwaysRun = true)
    public void setUp()
    {
        createAnotherDatabaseInstanceId();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown()
    {
        if (currentDAO != null)
        {
            resetDatabaseInstanceId(currentDAO);
        }
    }

    /**
     * Sets <code>daoFactory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setDaoFactory(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    /**
     * Sets <code>hibernate session factory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setHibernateSessionFactory(final SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Changes the database instance id of given {@link AbstractDAO} to a new value.
     */
    final void changeDatabaseInstanceId(final Object dao)
    {
        assertNull(origDatabaseInstanceId);
        assertTrue(dao instanceof AbstractDAO);
        currentDAO = dao;
    }

    /**
     * Resets the database instance id of given {@link AbstractDAO} to its original value.
     */
    final void resetDatabaseInstanceId(final Object dao)
    {
        assertTrue(dao instanceof AbstractDAO);
        assertNotNull(origDatabaseInstanceId);
        origDatabaseInstanceId = null;
        currentDAO = null;
    }

    /**
     * Creates <code>ANOTHER_DATABASE_INSTANCE_ID</code> in the database if needed.
     */
    private final Long createAnotherDatabaseInstanceId()
    {
        return 2L;
    }

    protected PersonPE getSystemPerson()
    {
        return getPerson("system");
    }

    protected PersonPE getTestPerson()
    {
        return getPerson("test");
    }

    protected PersonPE getPerson(final String userID)
    {
        final PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId(userID);
        assertNotNull("Person '" + userID + "' does not exists.", person);
        return person;
    }

    protected ScriptPE createScriptInDB(final ScriptType scriptType, final String name,
            String script, String description,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind)
    {
        final ScriptPE result = createScriptPE(scriptType, name, script, description, kind);
        daoFactory.getScriptDAO().createOrUpdate(result);
        assertEquals(scriptType, result.getScriptType());
        assertEquals(script, result.getScript());
        assertEquals(description, result.getDescription());
        assertEquals(name, result.getName());
        assertEquals(kind, result.getEntityKind());
        return result;
    }

    protected ScriptPE createScriptPE(final ScriptType scriptType, final String name,
            String script, String description,
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind kind)
    {
        final ScriptPE result = new ScriptPE();
        result.setScriptType(scriptType);
        result.setName(name);
        result.setScript(script);
        result.setDescription(description);
        result.setRegistrator(getSystemPerson());
        result.setEntityKind(kind);
        result.setPluginType(PluginType.JYTHON);
        return result;
    }

    protected SpacePE createSpace(final String spaceCode)
    {
        final SpacePE space = new SpacePE();
        space.setCode(spaceCode);
        space.setRegistrator(getSystemPerson());
        daoFactory.getSpaceDAO().createSpace(space);
        return space;
    }

    protected DataPE findData(String code)
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        DataPE data = dataDAO.tryToFindFullDataSetByCode(code, true, false);

        assertNotNull(data);

        return data;
    }

    protected ExternalDataPE findExternalData(String code)
    {
        final IDataDAO dataDAO = daoFactory.getDataDAO();
        DataPE data = dataDAO.tryToFindFullDataSetByCode(code, true, false);
        assertNotNull(data);

        ExternalDataPE externalData = data.tryAsExternalData();
        assertNotNull(externalData);

        return externalData;
    }

    protected SamplePE findSample(String permId)
    {
        final ISampleDAO sampleDAO = daoFactory.getSampleDAO();
        SamplePE sample = sampleDAO.tryToFindByPermID(permId);

        assertNotNull(sample);

        return sample;
    }

    protected ExperimentPE findExperiment(String permId)
    {
        final IExperimentDAO experimentDAO = daoFactory.getExperimentDAO();
        ExperimentPE experiment = experimentDAO.tryGetByPermID(permId);

        assertNotNull(experiment);

        return experiment;
    }

    protected ProjectPE findProject(String group, String project)
    {
        return daoFactory.getProjectDAO().tryFindProject(group, project);
    }

    protected ExperimentTypePE findExperimentType(String expType)
    {
        return (ExperimentTypePE) daoFactory.getEntityTypeDAO(EntityKind.EXPERIMENT)
                .tryToFindEntityTypeByCode(expType);
    }

    protected ExperimentPE createExperiment(String group, String project,
            String expCode, String expType)
    {
        final ExperimentPE result = new ExperimentPE();
        result.setCode(expCode);
        result.setPermId(daoFactory.getPermIdDAO().createPermId());
        result.setExperimentType(findExperimentType(expType));
        result.setProject(findProject(group, project));
        result.setRegistrator(getTestPerson());
        result.setRegistrationDate(new Date());
        result.setModificationDate(new Date());
        return result;
    }

    protected MaterialPE createMaterial(MaterialTypePE type, String code)
    {
        final MaterialPE material = new MaterialPE();
        material.setCode(code);
        material.setMaterialType(type);
        material.setRegistrationDate(new Date());
        material.setRegistrator(getSystemPerson());
        return material;
    }

    protected EntityTypePropertyTypePE createAssignment(EntityKind entityKind,
            EntityTypePE entityType, PropertyTypePE propertyType)
    {
        final PersonPE registrator = getTestPerson();
        EntityTypePropertyTypePE result =
                EntityTypePropertyTypePE.createEntityTypePropertyType(entityKind);
        result.setEntityType(entityType);
        result.setPropertyType(propertyType);
        result.setRegistrator(registrator);
        result.setRegistrationDate(new Date());
        result.setOrdinal(1L);
        return result;
    }

    protected final PropertyTypePE createPropertyType(final DataTypePE dataType, final String code,
            final VocabularyPE vocabularyOrNull, final MaterialTypePE materialTypeOrNull)
    {
        final PropertyTypePE propertyTypePE = new PropertyTypePE();
        propertyTypePE.setCode(code);
        propertyTypePE.setLabel(code);
        propertyTypePE.setDescription(code);
        propertyTypePE.setRegistrator(getSystemPerson());
        propertyTypePE.setType(dataType);
        if (DataTypeCode.CONTROLLEDVOCABULARY.equals(dataType.getCode()))
        {
            assertNotNull(vocabularyOrNull);
            propertyTypePE.setVocabulary(vocabularyOrNull);
        }
        if (DataTypeCode.MATERIAL.equals(dataType.getCode()))
        {
            propertyTypePE.setMaterialType(materialTypeOrNull);
        }
        return propertyTypePE;
    }

    protected PropertyTypePE selectFirstPropertyType()
    {
        List<PropertyTypePE> propertyTypes = daoFactory.getPropertyTypeDAO().listPropertyTypes();
        Assert.assertTrue(propertyTypes.size() > 0);
        Collections.sort(propertyTypes);
        PropertyTypePE result = propertyTypes.get(0);
        return result;
    }

    /**
     * Returns the first experiment found in the database.
     */
    protected ExperimentPE selectFirstExperiment()
    {

        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listExperiments();
        Collections.sort(experiments);
        return experiments.get(0);
    }

    /**
     * Returns the type of first experiment found in the database.
     */
    protected ExperimentTypePE selectFirstExperimentType()
    {
        return selectFirstExperiment().getExperimentType();
    }

    protected static void assertEqualsOrGreater(int minimalSize, int actualSize)
    {
        if (actualSize < minimalSize)
        {
            fail("At least " + minimalSize + " items expected, but only " + actualSize + " found.");
        }
    }

    protected AuthorizationGroupPE createAuthorizationGroup(String code, String desc)
    {
        AuthorizationGroupPE result = new AuthorizationGroupPE();
        result.setCode(code);
        result.setDescription(desc);
        result.setRegistrator(getSystemPerson());
        return result;
    }

    protected List<IEntityProperty> getSortedProperties(IEntityPropertiesHolder sample)
    {
        List<IEntityProperty> properties = sample.getProperties();
        Collections.sort(properties, new Comparator<IEntityProperty>()
            {
                @Override
                public int compare(IEntityProperty p1, IEntityProperty p2)
                {
                    return p1.getPropertyType().getCode().compareTo(p2.getPropertyType().getCode());
                }
            });
        return properties;
    }
}
