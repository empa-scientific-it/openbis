/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.DataType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.EntityKind;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IDataSetType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExperimentType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IFileFormatType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMaterialType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyAssignment;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IPropertyType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.ISampleType;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystemType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IVocabularyTermUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.MaterialTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;

/**
 * @author Franz-Josef Elmer
 */
public class MasterDataRegistrationTransactionTest extends AssertJUnit
{
    private static final long VOCA_TERM_ID = 4711L;

    private static final long VOCA_ID = 42L;

    private static final String KNOWN_PROPERTY = "DESCRIPTION";

    private static final String SESSION_TOKEN = "seesion-token";

    private static final String KNOWN = "KNOWN";

    private static final String UNKNOWN = "UNKNOWN";

    private Mockery context;

    private ICommonServer server;

    private MasterDataRegistrationTransaction transaction;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        server = context.mock(ICommonServer.class);
        transaction =
                new MasterDataRegistrationTransaction(EncapsulatedCommonServer.create(server,
                        SESSION_TOKEN));
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateExperimentTypeOfKnownType()
    {
        prepareListExperimentTypes();

        IExperimentType type = transaction.getOrCreateNewExperimentType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateExperimentTypeOfUnknownType()
    {
        prepareListExperimentTypes();

        IExperimentType type = transaction.getOrCreateNewExperimentType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListExperimentTypes()
    {
        context.checking(new Expectations()
            {
                {
                    ExperimentType type =
                            new ExperimentTypeBuilder().code(KNOWN).getExperimentType();
                    one(server).listExperimentTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateSampleTypeOfKnownType()
    {
        prepareListSampleTypes();

        ISampleType type = transaction.getOrCreateNewSampleType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateSampleTypeOfUnknownType()
    {
        prepareListSampleTypes();

        ISampleType type = transaction.getOrCreateNewSampleType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListSampleTypes()
    {
        context.checking(new Expectations()
            {
                {
                    SampleType type = new SampleTypeBuilder().code(KNOWN).getSampleType();
                    one(server).listSampleTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateDataSetTypeOfKnownType()
    {
        prepareListDataSetTypes();

        IDataSetType type = transaction.getOrCreateNewDataSetType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateDataSetTypeOfUnknownType()
    {
        prepareListDataSetTypes();

        IDataSetType type = transaction.getOrCreateNewDataSetType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListDataSetTypes()
    {
        context.checking(new Expectations()
            {
                {

                    DataSetType type = new DataSetTypeBuilder().code(KNOWN).getDataSetType();
                    one(server).listDataSetTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateMaterialTypeOfKnownType()
    {
        prepareListMaterialTypes();

        IMaterialType type = transaction.getOrCreateNewMaterialType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateMaterialTypeOfUnknownType()
    {
        prepareListMaterialTypes();

        IMaterialType type = transaction.getOrCreateNewMaterialType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListMaterialTypes()
    {
        context.checking(new Expectations()
            {
                {

                    MaterialType type = new MaterialTypeBuilder().code(KNOWN).getMaterialType();
                    one(server).listMaterialTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateFileFormatTypeOfKnownType()
    {
        prepareListFileFormatTypes();

        IFileFormatType type = transaction.getOrCreateNewFileFormatType(KNOWN);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateFileFormatTypeOfUnknownType()
    {
        prepareListFileFormatTypes();

        IFileFormatType type = transaction.getOrCreateNewFileFormatType(UNKNOWN);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListFileFormatTypes()
    {
        context.checking(new Expectations()
            {
                {
                    FileFormatType type = new FileFormatType(KNOWN);
                    one(server).listFileFormatTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreatePropertyTypeOfKnownType()
    {
        prepareListPropertyTypes();

        IPropertyType type = transaction.getOrCreateNewPropertyType(KNOWN, DataType.INTEGER);
        type.setDescription("description");

        assertEquals(KNOWN, type.getCode());
        assertEquals(DataType.VARCHAR, type.getDataType());
        assertEquals(null, type.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreatePropertyTypeOfUnknownType()
    {
        prepareListPropertyTypes();

        IPropertyType type = transaction.getOrCreateNewPropertyType(UNKNOWN, DataType.INTEGER);
        type.setDescription("description");

        assertEquals(UNKNOWN, type.getCode());
        assertEquals(DataType.INTEGER, type.getDataType());
        assertEquals("description", type.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListPropertyTypes()
    {
        context.checking(new Expectations()
            {
                {
                    PropertyType type = new PropertyType();
                    type.setCode(KNOWN);
                    type.setDataType(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType(
                            DataTypeCode.VARCHAR));
                    one(server).listPropertyTypes(SESSION_TOKEN, false);
                    will(returnValue(Arrays.asList(type)));
                }
            });
    }

    @Test
    public void testGetOrCreateKnownVocabulary()
    {
        prepareListVocabularies();

        IVocabulary vocabulary = transaction.getOrCreateNewVocabulary(KNOWN);
        vocabulary.setDescription("description");

        assertEquals(KNOWN, vocabulary.getCode());
        assertEquals(null, vocabulary.getDescription());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateUnknownVocabulary()
    {
        prepareListVocabularies();

        IVocabulary vocabulary = transaction.getOrCreateNewVocabulary(UNKNOWN);
        vocabulary.setDescription("description");

        assertEquals(UNKNOWN, vocabulary.getCode());
        assertEquals("description", vocabulary.getDescription());
        context.assertIsSatisfied();
    }

    private void prepareListVocabularies()
    {
        context.checking(new Expectations()
            {
                {
                    Vocabulary vocabulary = new Vocabulary();
                    vocabulary.setCode(KNOWN);
                    vocabulary.setId(VOCA_ID);
                    VocabularyTerm term1 = new VocabularyTerm();
                    term1.setId(VOCA_TERM_ID);
                    term1.setCode("T1");
                    term1.setLabel("t1");
                    vocabulary.getTerms().add(term1);
                    one(server).listVocabularies(SESSION_TOKEN, true, false);
                    will(returnValue(Arrays.asList(vocabulary)));
                }
            });
    }

    @Test
    public void testAssignPropertyTypeWithExistingAssigment()
    {
        prepareListAssigments();

        IPropertyAssignment assigment =
                transaction.assignPropertyType(new SampleTypeImmutable(KNOWN),
                        new PropertyTypeImmutable(KNOWN_PROPERTY, DataType.VARCHAR));
        assigment.setSection("top");

        assertEquals(KNOWN, assigment.getEntityTypeCode());
        assertEquals(KNOWN_PROPERTY, assigment.getPropertyTypeCode());
        assertEquals(EntityKind.SAMPLE, assigment.getEntityKind());
        assertEquals(null, assigment.getSection());
        context.assertIsSatisfied();
    }

    @Test
    public void testAssignPropertyType()
    {
        prepareListAssigments();

        IPropertyAssignment assigment =
                transaction.assignPropertyType(new SampleTypeImmutable(UNKNOWN),
                        new PropertyTypeImmutable(KNOWN_PROPERTY, DataType.VARCHAR));
        assigment.setSection("top");

        assertEquals(UNKNOWN, assigment.getEntityTypeCode());
        assertEquals(KNOWN_PROPERTY, assigment.getPropertyTypeCode());
        assertEquals(EntityKind.SAMPLE, assigment.getEntityKind());
        assertEquals("top", assigment.getSection());
        context.assertIsSatisfied();
    }

    private void prepareListAssigments()
    {
        context.checking(new Expectations()
            {
                {
                    List<SampleTypePropertyType> assigments =
                            new SampleTypeBuilder()
                                    .code(KNOWN)
                                    .propertyType(KNOWN_PROPERTY, "Description",
                                            DataTypeCode.VARCHAR)
                                    .getSampleType()
                                    .getAssignedPropertyTypes();
                    one(server).listEntityTypePropertyTypes(SESSION_TOKEN);
                    will(returnValue(assigments));
                }
            });
    }

    @Test
    public void testGetOrCreateExistingExternalDataManagementSystem()
    {
        context.checking(new Expectations()
            {
                {
                    ExternalDataManagementSystem edms = new ExternalDataManagementSystem();
                    edms.setCode(KNOWN);
                    edms.setLabel("old label");
                    edms.setUrlTemplate("old url template");
                    edms.setAddress(edms.getUrlTemplate());
                    edms.setOpenBIS(false);

                    one(server).getExternalDataManagementSystem(SESSION_TOKEN, KNOWN);
                    will(returnValue(edms));
                }
            });

        IExternalDataManagementSystem edms =
                transaction.getOrCreateNewExternalDataManagementSystem(KNOWN);
        edms.setLabel("new label");
        edms.setAddress("new url template");
        edms.setAddressType(ExternalDataManagementSystemType.OPENBIS);

        assertEquals(KNOWN, edms.getCode());
        assertEquals("old label", edms.getLabel());
        assertEquals("old url template", edms.getAddress());
        assertFalse(edms.isOpenBIS());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetOrCreateNonExistingExistingExternalDataManagementSystem()
    {
        context.checking(new Expectations()
            {
                {
                    one(server).getExternalDataManagementSystem(SESSION_TOKEN, UNKNOWN);
                    will(returnValue(null));
                }
            });

        IExternalDataManagementSystem edms =
                transaction.getOrCreateNewExternalDataManagementSystem(UNKNOWN);
        edms.setLabel("new label");
        edms.setAddress("new url template");
        edms.setAddressType(ExternalDataManagementSystemType.OPENBIS);

        assertEquals(UNKNOWN, edms.getCode());
        assertEquals("new label", edms.getLabel());
        assertEquals("new url template", edms.getAddress());
        assertTrue(edms.isOpenBIS());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddTermToExistingVocabulary()
    {
        prepareListVocabularies();
        final RecordingMatcher<List<VocabularyTerm>> newTermsMatcher =
                new RecordingMatcher<List<VocabularyTerm>>();
        context.checking(new Expectations()
            {
                {
                    one(server).addVocabularyTerms(with(SESSION_TOKEN), with(new TechId(VOCA_ID)),
                            with(newTermsMatcher), with((Long) null));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(KNOWN);
        IVocabularyTerm term = transaction.createNewVocabularyTerm("ABC");
        term.setLabel("abc");
        term.setDescription("Hello abc");
        voca.addTerm(term);
        transaction.commit();

        assertEquals("[abc [ABC]]", newTermsMatcher.recordedObject().toString());
        assertEquals("Hello abc", newTermsMatcher.recordedObject().get(0).getDescription());
        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateTermOfExistingVocabulary()
    {
        prepareListVocabularies();
        final RecordingMatcher<IVocabularyTermUpdates> termMatcher =
                new RecordingMatcher<IVocabularyTermUpdates>();
        context.checking(new Expectations()
            {
                {
                    one(server).updateVocabularyTerm(with(SESSION_TOKEN), with(termMatcher));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(KNOWN);
        IVocabularyTerm term = transaction.getVocabularyTerm(voca, "t1");
        term.setLabel("Term one");
        term.setDescription("a new description");
        transaction.updateVocabularyTerm(term);
        transaction.commit();

        assertEquals("Term one [T1]", termMatcher.recordedObject().toString());
        assertEquals("a new description", termMatcher.recordedObject().getDescription());
        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUnknownTermOfExistingVocabulary()
    {
        prepareListVocabularies();

        IVocabulary voca = transaction.getOrCreateNewVocabulary(KNOWN);
        try
        {
            transaction.getVocabularyTerm(voca, "unknown");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Vocabulary KNOWN has no term UNKNOWN.", ex.getMessage());
        }

        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testAddTermToNewVocabulary()
    {
        prepareListVocabularies();
        final RecordingMatcher<NewVocabulary> newVocaMatcher =
                new RecordingMatcher<NewVocabulary>();
        context.checking(new Expectations()
            {
                {
                    one(server).registerVocabulary(with(SESSION_TOKEN), with(newVocaMatcher));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(UNKNOWN);
        voca.setDescription("Hello new vocabulary");
        voca.setManagedInternally(true);
        voca.setChosenFromList(true);
        voca.setManagedInternally(true);
        voca.setUrlTemplate("url-template");

        IVocabularyTerm term1 = transaction.createNewVocabularyTerm("A");
        term1.setLabel("Alpha");
        term1.setDescription("Hello A");
        term1.setOrdinal(3L);
        voca.addTerm(term1);
        IVocabularyTerm term2 = transaction.createNewVocabularyTerm("B");
        term2.setLabel("Beta");
        term2.setDescription("Hello B");
        term2.setOrdinal(1L);
        voca.addTerm(term2);
        transaction.commit();

        NewVocabulary newVoca = newVocaMatcher.recordedObject();
        assertEquals(UNKNOWN, newVoca.getCode());
        assertEquals("Hello new vocabulary", newVoca.getDescription());
        assertEquals("url-template", newVoca.getURLTemplate());
        assertEquals(true, newVoca.isChosenFromList());
        assertEquals(true, newVoca.isManagedInternally());
        List<VocabularyTerm> terms = newVoca.getTerms();
        assertEquals("[Beta [B], Alpha [A]]", terms.toString());
        assertEquals("Hello B", terms.get(0).getDescription());
        assertEquals(1L, terms.get(0).getOrdinal().longValue());
        assertEquals("Hello A", terms.get(1).getDescription());
        assertEquals(3L, terms.get(1).getOrdinal().longValue());
        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateTermOfNewVocabulary()
    {
        prepareListVocabularies();
        final RecordingMatcher<NewVocabulary> newVocaMatcher =
                new RecordingMatcher<NewVocabulary>();
        context.checking(new Expectations()
            {
                {
                    one(server).registerVocabulary(with(SESSION_TOKEN), with(newVocaMatcher));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(UNKNOWN);
        voca.addTerm(transaction.createNewVocabularyTerm("Alpha"));
        IVocabularyTerm term = transaction.getVocabularyTerm(voca, "ALpha");
        term.setLabel("Alphaaaa");
        transaction.updateVocabularyTerm(term);
        transaction.commit();

        NewVocabulary newVoca = newVocaMatcher.recordedObject();
        assertEquals(UNKNOWN, newVoca.getCode());
        List<VocabularyTerm> terms = newVoca.getTerms();
        assertEquals("[Alphaaaa [Alpha]]", terms.toString());
        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testGetUnknownTermOfNewVocabulary()
    {
        prepareListVocabularies();

        IVocabulary voca = transaction.getOrCreateNewVocabulary(UNKNOWN);
        try
        {
            transaction.getVocabularyTerm(voca, "abc");
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Vocabulary UNKNOWN has no term ABC.", ex.getMessage());
        }

        assertEquals(false, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddVocabularyTermsFailed()
    {
        prepareListVocabularies();
        final Exception throwable = new RuntimeException("Oohps!");
        context.checking(new Expectations()
            {
                {
                    one(server).addVocabularyTerms(with(SESSION_TOKEN), with(new TechId(VOCA_ID)),
                            with(any(List.class)), with((Long) null));
                    will(throwException(throwable));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(KNOWN);
        voca.addTerm(transaction.createNewVocabularyTerm("ABC"));
        voca.addTerm(transaction.createNewVocabularyTerm("Delta"));
        transaction.commit();

        assertEquals("Failed to register new terms [ABC, DELTA]: Oohps!", transaction
                .getTransactionErrors().getErrors().get(0).getDescription());
        assertSame(throwable, transaction.getTransactionErrors().getErrors().get(0).getException());
        assertEquals(1, transaction.getTransactionErrors().getErrors().size());
        assertEquals(true, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateVocabularyFailed()
    {
        prepareListVocabularies();
        final Exception throwable = new RuntimeException("Oohps!");
        context.checking(new Expectations()
            {
                {
                    one(server).registerVocabulary(with(SESSION_TOKEN),
                            with(any(NewVocabulary.class)));
                    will(throwException(throwable));
                }
            });

        transaction.getOrCreateNewVocabulary(UNKNOWN);
        transaction.commit();

        assertEquals("Failed to register vocabulary 'UNKNOWN': Oohps!", transaction
                .getTransactionErrors().getErrors().get(0).getDescription());
        assertSame(throwable, transaction.getTransactionErrors().getErrors().get(0).getException());
        assertEquals(1, transaction.getTransactionErrors().getErrors().size());
        assertEquals(true, transaction.hasErrors());
        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateVocabularyTermsFailed()
    {
        prepareListVocabularies();
        final Exception throwable = new RuntimeException("Oohps!");
        context.checking(new Expectations()
            {
                {
                    one(server).updateVocabularyTerm(with(SESSION_TOKEN),
                            with(any(IVocabularyTermUpdates.class)));
                    will(throwException(throwable));
                }
            });

        IVocabulary voca = transaction.getOrCreateNewVocabulary(KNOWN);
        IVocabularyTerm term = transaction.getVocabularyTerm(voca, "t1");
        transaction.updateVocabularyTerm(term);
        transaction.commit();

        assertEquals("Failed to update vocabulary term 'T1': Oohps!", transaction
                .getTransactionErrors().getErrors().get(0).getDescription());
        assertSame(throwable, transaction.getTransactionErrors().getErrors().get(0).getException());
        assertEquals(1, transaction.getTransactionErrors().getErrors().size());
        assertEquals(true, transaction.hasErrors());
        context.assertIsSatisfied();
    }
}
