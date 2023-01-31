package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.spi.NOPLogger;
import org.apache.log4j.spi.NOPLoggerRepository;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.DefaultNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.PrefixBasedNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeConverter;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewVocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Script;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

public class MasterDataSynchronizerTest
{
    private static final String SYSTEM_USER_ID = "system";

    private static final String UPDATE_AND_PREFIX_PROVIDER = "updateAndPrefixProvider";

    private static final String USER_ID = "user";

    private static final String PASSWORD = "password";

    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private ISynchronizerFacade facade;

    private ICommonServer server;

    private IApplicationServerApi v3api;

    private MasterDataSynchronizer synchronizer;

    private Monitor monitor;

    private SyncConfig config;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        facade = context.mock(ISynchronizerFacade.class);
        server = context.mock(ICommonServer.class);
        v3api = context.mock(IApplicationServerApi.class);
        context.checking(new Expectations()
            {
                {
                    allowing(server).tryAuthenticate(USER_ID, PASSWORD);
                    SessionContextDTO session = new SessionContextDTO();
                    session.setSessionToken(SESSION_TOKEN);
                    will(returnValue(session));

                    allowing(server).listFileFormatTypes(SESSION_TOKEN);
                    will(returnValue(Arrays.asList()));
                }
            });
        config = new SyncConfig();
        config.setHarvesterUser(USER_ID);
        config.setHarvesterPass(PASSWORD);
        synchronizer = new MasterDataSynchronizer(config, facade, server, v3api);
        monitor = new Monitor("test", new NOPLogger(new NOPLoggerRepository(), "null"));
    }

    @DataProvider
    public Object[][] updateAndPrefixProvider()
    {
        return new Object[][] {
                { false, null },
                { false, "PR" },
                { true, null },
                { true, "PR" },
        };
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testNonexistentInternalVocabulary(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        builder.incomingVocabularies(new VocabularyBuilder("A").managedInternally().system().get());
        MasterData masterData = builder.prepare();

        // When & Then
        assertFailure(masterData, "There is no internal vocabulary $A.");
        context.assertIsSatisfied();
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testNonexistentVocabulary(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        VocabularyBuilder vocabulary = new VocabularyBuilder("A").registrator("admin");
        builder.incomingVocabularies(vocabulary.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareRegisterVocabulary(vocabulary.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testAlreadyExistentInternalVocabularyAddTerms(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        VocabularyBuilder existing = new VocabularyBuilder("A").managedInternally().system();
        existing.term("1").label("alpha");
        builder.existingVocabularies(existing.get());
        VocabularyBuilder incoming = new VocabularyBuilder("A").managedInternally().system().description("blabla");
        incoming.term("1").label("one");
        VocabularyTermBuilder term2 = incoming.term("2").label("two");
        builder.incomingVocabularies(incoming.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareAddVocabularyTerms("$A", existing.get().getId(), term2.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test
    public void testWithPrefixAlreadyExistentVocabularyAddTerms()
    {
        // Given
        config.setMasterDataUpdateAllowed(true);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator("PR"));
        VocabularyBuilder existing = new VocabularyBuilder("A").system();
        existing.term("1").label("alpha");
        builder.existingVocabularies(existing.get());
        VocabularyBuilder incoming = new VocabularyBuilder("A").system().description("blabla");
        incoming.term("1").label("one");
        incoming.term("2").label("two");
        builder.incomingVocabularies(incoming.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareRegisterVocabulary(incoming.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test
    public void testWithoutPrefixAlreadyExistentVocabularyAddTerms()
    {
        // Given
        config.setMasterDataUpdateAllowed(true);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(null));
        VocabularyBuilder existing = new VocabularyBuilder("A").system();
        existing.term("1").label("alpha");
        builder.existingVocabularies(existing.get());
        VocabularyBuilder incoming = new VocabularyBuilder("A").system()
                .description("1. description");
        VocabularyTermBuilder term1 = incoming.term("1").label("one");
        VocabularyTermBuilder term2 = incoming.term("2").label("two");
        builder.incomingVocabularies(incoming.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareUpdateVocabulary(incoming.get(), "incoming NewVocabulary[description=1. description] "
                + "differs from existing NewVocabulary[description=<null>]");
        prepareUpdateVocabularyTerm("A", term1.get(), "incoming VocabularyTerm[label=one] "
                + "differs from existing VocabularyTerm[label=alpha]");
        prepareAddVocabularyTerms("A", existing.get().getId(), term2.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test
    public void testWithoutPrefixAlreadyExistentVocabularyAddTermsButUpdatesNotAllowed()
    {
        // Given
        config.setMasterDataUpdateAllowed(false);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(null));
        VocabularyBuilder existing = new VocabularyBuilder("A").system();
        existing.term("1").label("alpha");
        builder.existingVocabularies(existing.get());
        VocabularyBuilder incoming = new VocabularyBuilder("A").system()
                .description("1. description");
        incoming.term("1").label("one");
        VocabularyTermBuilder term2 = incoming.term("2").label("two");
        builder.incomingVocabularies(incoming.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareAddVocabularyTerms("A", existing.get().getId(), term2.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testNonexistentInternalPropertyType(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        builder.incomingPropertyTypes(new PropertyTypeBuilder("A").managedInternally().system().get());
        MasterData masterData = builder.prepare();

        // When & Then
        assertFailure(masterData, "There is no internal property type $A.");
        context.assertIsSatisfied();
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testNonexistentPropertyTypes(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        PropertyTypeBuilder propertyType = new PropertyTypeBuilder("A").registrator("admin");
        builder.incomingPropertyTypes(propertyType.get());
        MasterData masterData = builder.prepare();

        // Expected actions
        prepareRegisterPropertyType(propertyType.get());

        // When
        synchronizer.synchronizeMasterData(masterData, monitor);

        // Then
        context.assertIsSatisfied();
    }

    @Test(dataProvider = UPDATE_AND_PREFIX_PROVIDER)
    public void testInternalPropertyTypeOfDifferentDataType(boolean withUpdate, String prefix)
    {
        // Given
        config.setMasterDataUpdateAllowed(withUpdate);
        TestFixtureBuilder builder = new TestFixtureBuilder(createTranslator(prefix));
        builder.incomingPropertyTypes(new PropertyTypeBuilder("A").type(DataTypeCode.VARCHAR)
                .managedInternally().system().get());
        builder.existingPropertyTypes(new PropertyTypeBuilder("A").type(DataTypeCode.CONTROLLEDVOCABULARY)
                .managedInternally().system().get());
        MasterData masterData = builder.prepare();

        // When & Then
        assertFailure(masterData, "The internal property type $A is not of data type VARCHAR but CONTROLLEDVOCABULARY.");
        context.assertIsSatisfied();
    }

    private void assertFailure(MasterData masterData, String... failureMessages)
    {
        try
        {
            synchronizer.synchronizeMasterData(masterData, monitor);
            fail("RuntimeException expected");
        } catch (RuntimeException e)
        {
            StringBuilder builder = new StringBuilder("Master data can not be synchronization "
                    + "because of the following reasons:\n");
            for (String failureMessage : failureMessages)
            {
                builder.append(failureMessage).append("\n");
            }
            assertEquals(e.getMessage(), builder.toString());
        }
    }

    private INameTranslator createTranslator(String prefixOrNull)
    {
        return prefixOrNull == null ? new DefaultNameTranslator() : new PrefixBasedNameTranslator(prefixOrNull);
    }

    private void prepareRegisterVocabulary(NewVocabulary vocabulary)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).registerVocabulary(vocabulary);
                }
            });
    }

    private void prepareUpdateVocabulary(NewVocabulary vocabulary, String diff)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).updateVocabulary(vocabulary, diff);
                }
            });
    }

    private void prepareAddVocabularyTerms(String vocabularyCode, long id, VocabularyTerm... vocabularyTerms)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).addVocabularyTerms(vocabularyCode, new TechId(id), Arrays.asList(vocabularyTerms));
                }
            });
    }

    private void prepareUpdateVocabularyTerm(String vocabularyCode, VocabularyTerm vocabularyTerm, String diff)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).updateVocabularyTerm(vocabularyCode, vocabularyTerm, diff);
                }
            });
    }

    private void prepareRegisterPropertyType(PropertyType propertyType)
    {
        context.checking(new Expectations()
            {
                {
                    one(facade).registerPropertyType(propertyType);
                }
            });
    }

    private final class TestFixtureBuilder
    {
        private final INameTranslator nameTranslator;

        private List<FileFormatType> existingFileFormatTypes = Collections.emptyList();

        private List<FileFormatType> incomingFileFormatTypes = Collections.emptyList();

        private List<Script> existingScripts = Collections.emptyList();

        private List<Script> incomingScripts = Collections.emptyList();

        private List<NewVocabulary> existingVocabularies = Collections.emptyList();

        private List<NewVocabulary> incomingVocabularies = Collections.emptyList();

        private List<PropertyType> existingPropertyTypes = Collections.emptyList();

        private List<PropertyType> incomingPropertyTypes = Collections.emptyList();

        private List<MaterialType> existingMaterialTypes = Collections.emptyList();

        private List<MaterialType> incomingMaterialTypes = Collections.emptyList();

        private List<ExternalDms> existingExternalDmss = Collections.emptyList();

        private List<ExternalDms> incomingExternalDmss = Collections.emptyList();

        private Function<? super NewVocabulary, ? extends String> vocabularyKeyMapper =
                v -> CodeConverter.tryToBusinessLayer(v.getCode(), v.isManagedInternally());

        private Function<? super PropertyType, ? extends String> propertyTypeKeyMapper =
                p -> CodeConverter.tryToBusinessLayer(p.getCode(), p.isManagedInternally());

        TestFixtureBuilder(INameTranslator nameTranslator)
        {
            this.nameTranslator = nameTranslator;
        }

        TestFixtureBuilder existingVocabularies(NewVocabulary... vocabularies)
        {
            existingVocabularies = Arrays.asList(vocabularies);
            existingVocabularies.forEach(v -> v.setCode(vocabularyKeyMapper.apply(v)));
            return this;
        }

        TestFixtureBuilder incomingVocabularies(NewVocabulary... vocabularies)
        {
            incomingVocabularies = Arrays.asList(vocabularies);
            incomingVocabularies.forEach(v -> v.setCode(nameTranslator.translate(v.getCode())));
            return this;
        }

        TestFixtureBuilder existingPropertyTypes(PropertyType... propertyTypes)
        {
            existingPropertyTypes = Arrays.asList(propertyTypes);
            existingPropertyTypes.forEach(t -> t.setCode(propertyTypeKeyMapper.apply(t)));
            return this;
        }

        TestFixtureBuilder incomingPropertyTypes(PropertyType... propertyTypes)
        {
            incomingPropertyTypes = Arrays.asList(propertyTypes);
            incomingPropertyTypes.forEach(t -> t.setCode(nameTranslator.translate(t.getCode())));
            return this;
        }

        MasterData prepare()
        {
            List<ExternalDmsPermId> existingExternalDmssIds =
                    existingExternalDmss.stream().map(ExternalDms::getPermId).collect(Collectors.toList());
            Map<ExternalDmsPermId, ExternalDms> existingExternalDmssMap = existingExternalDmss.stream().collect(
                    Collectors.toMap(ExternalDms::getPermId, Function.identity()));
            context.checking(new Expectations()
                {
                    {
                        allowing(server).listFileFormatTypes(SESSION_TOKEN);
                        will(returnValue(existingFileFormatTypes));
                        allowing(server).listScripts(SESSION_TOKEN, null, null);
                        will(returnValue(existingScripts));
                        allowing(server).listVocabularies(SESSION_TOKEN, true, false);
                        will(returnValue(existingVocabularies));
                        allowing(server).listPropertyTypes(SESSION_TOKEN, false);
                        will(returnValue(existingPropertyTypes));
                        allowing(server).listMaterialTypes(SESSION_TOKEN);
                        will(returnValue(existingMaterialTypes));
                        allowing(v3api).getExternalDataManagementSystems(with(SESSION_TOKEN),
                                with(existingExternalDmssIds),
                                with(any(ExternalDmsFetchOptions.class)));
                        will(returnValue(existingExternalDmssMap));
                        one(facade).printSummary();
                    }
                });
            MasterData masterData = new MasterData(nameTranslator);
            masterData.setVocabulariesToProcess(incomingVocabularies.stream().collect(
                    Collectors.toMap(vocabularyKeyMapper, Function.identity())));
            masterData.setPropertyTypesToProcess(incomingPropertyTypes.stream().collect(
                    Collectors.toMap(propertyTypeKeyMapper, Function.identity())));
            return masterData;
        }
    }

    private static final class VocabularyBuilder
    {
        private static long counter;

        private NewVocabulary vocabulary = new NewVocabulary();

        VocabularyBuilder(String code)
        {
            vocabulary.setCode(code);
            vocabulary.setId(counter++);
        }

        NewVocabulary get()
        {
            return vocabulary;
        }

        VocabularyBuilder description(String description)
        {
            vocabulary.setDescription(description);
            return this;
        }

        VocabularyBuilder managedInternally()
        {
            vocabulary.setManagedInternally(true);
            return this;
        }

        VocabularyBuilder system()
        {
            return registrator(SYSTEM_USER_ID);
        }

        VocabularyBuilder registrator(String userId)
        {
            Person person = new Person();
            person.setUserId(userId);
            vocabulary.setRegistrator(person);
            return this;
        }

        VocabularyTermBuilder term(String code)
        {
            VocabularyTermBuilder termBuilder = new VocabularyTermBuilder(code);
            vocabulary.getTerms().add(termBuilder.get());
            return termBuilder;
        }
    }

    private static final class VocabularyTermBuilder
    {
        private VocabularyTerm term = new VocabularyTerm();

        VocabularyTermBuilder(String code)
        {
            term.setCode(code);
        }

        VocabularyTerm get()
        {
            return term;
        }

        VocabularyTermBuilder label(String label)
        {
            term.setLabel(label);
            return this;
        }
    }

    private static final class PropertyTypeBuilder
    {
        private PropertyType propertyType = new PropertyType();

        private String code;

        PropertyTypeBuilder(String code)
        {
            this.code = code;
        }

        PropertyType get()
        {
            propertyType.setCode(code);
            return propertyType;
        }

        PropertyTypeBuilder managedInternally()
        {
            propertyType.setManagedInternally(true);
            return this;
        }

        PropertyTypeBuilder type(DataTypeCode typeCode)
        {
            propertyType.setDataType(new DataType(typeCode));
            return this;
        }

        PropertyTypeBuilder system()
        {
            return registrator(SYSTEM_USER_ID);
        }

        PropertyTypeBuilder registrator(String userId)
        {
            Person person = new Person();
            person.setUserId(userId);
            propertyType.setRegistrator(person);
            return this;
        }

    }
}
