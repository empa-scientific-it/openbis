package ch.ethz.sis.openbis.systemtest.deletion;

import static org.testng.Assert.fail;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.TestTransaction;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.create.AttachmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IdListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.PhysicalDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ProprietaryStorageFormatPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.RelativeLocationLocatorTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.create.MaterialCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.update.MaterialUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.update.ProjectUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.time.DateFormatThreadLocal;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.AttachmentEntry;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.deletion.AttributeEntry;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentContentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;

public abstract class DeletionTest extends AbstractTest
{

    private static final String TIMESTAMP_OK = "timestamp ok";

    public static void newTx()
    {
        TestTransaction.end();
        TestTransaction.start();
    }

    @Autowired
    protected SessionFactory sessionFactory;

    protected String sessionToken;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public Set<String> attachmentSet(String... values)
    {
        return attachmentSetFor(TEST_USER, values);
    }

    public Set<String> attachmentSetFor(String userId, String... values)
    {
        return entitySet("ATTACHMENT", userId, values);
    }

    public Set<String> unknownSet(String... values)
    {
        return unknownSetFor(TEST_USER, values);
    }

    public Set<String> unknownSetFor(String userId, String... values)
    {
        return entitySet("UNKNOWN", userId, values);
    }

    public Set<String> spaceSet(String... values)
    {
        return spaceSetFor(TEST_USER, values);
    }

    public Set<String> spaceSetFor(String userId, String... values)
    {
        return entitySet("SPACE", userId, values);
    }

    public Set<String> projectSet(String... values)
    {
        return projectSetFor(TEST_USER, values);
    }

    public Set<String> projectSetFor(String userId, String... values)
    {
        return entitySet("PROJECT", userId, values);
    }

    public Set<String> experimentSet(String... values)
    {
        return experimentSetFor(TEST_USER, values);
    }

    public Set<String> experimentSetFor(String userId, String... values)
    {
        return entitySet("EXPERIMENT", userId, values);
    }

    public Set<String> sampleSet(String... values)
    {
        return sampleSetFor(TEST_USER, values);
    }

    public Set<String> sampleSetFor(String userId, String... values)
    {
        return entitySet("SAMPLE", userId, values);
    }

    public Set<String> dataSetSet(String... values)
    {
        return dataSetSetFor(TEST_USER, values);
    }

    public Set<String> dataSetSetFor(String userId, String... values)
    {
        return entitySet("DATA_SET", userId, values);
    }

    public Set<String> propertiesSet(String... values)
    {
        return set("PROPERTY", null, TEST_USER, values);
    }

    private Set<String> entitySet(String entityType, String userId, String... values)
    {
        return set("RELATIONSHIP", entityType, userId, values);
    }

    private Set<String> set(String type, String entityTypeOrNull, String userId, String... values)
    {
        Set<String> result = new HashSet<>();
        for (String value : values)
        {
            result.add(render(type, value, entityTypeOrNull, userId));
        }
        return result;
    }

    public Set<String> set(String... values)
    {
        return new HashSet<String>(Arrays.asList(values));
    }

    @BeforeClass
    public void setSessionToken()
    {
        sessionToken = v3api.login(TEST_USER, PASSWORD);
    }

    @SuppressWarnings("unchecked")
    public <T extends IProjectId> void delete(T... ids)
    {
        ProjectDeletionOptions deletionOptions = new ProjectDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteProjects(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IExperimentId> void delete(T... ids)
    {
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteExperiments(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    public <T extends ISampleId> void delete(T... ids)
    {
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteSamples(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    public <T extends ISpaceId> void delete(T... ids)
    {
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteSpaces(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IMaterialId> void delete(T... ids)
    {
        MaterialDeletionOptions deletionOptions = new MaterialDeletionOptions();
        deletionOptions.setReason("reason");
        v3api.deleteMaterials(sessionToken, Arrays.asList(ids), deletionOptions);
    }

    @SuppressWarnings("unchecked")
    public <T extends IDataSetId> void delete(T... ids)
    {
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("reason");
        IDeletionId delId = v3api.deleteDataSets(sessionToken, Arrays.asList(ids), deletionOptions);
        v3api.confirmDeletions(sessionToken, Collections.singletonList(delId));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public final void assertPropertiesHistory(String permId, final String key, String... expected) throws Exception
    {
        List<Set<String>> values = new ArrayList<>();
        for (String e : expected)
        {
            if (e == "")
            {
                values.add(set());
            } else
            {
                values.add(propertiesSet(e));
            }
        }

        assertHistory(permId, key, values.toArray(new Set[0]));
    }

    public void assertRegistrationTimestampAttribute(String permId, final Date after, final Date before) throws Exception
    {
        IModificationFilter filter = new IModificationFilter()
            {
                @Override
                public boolean accept(Modification modification)
                {
                    return modification.type.equals(AttributeEntry.ATTRIBUTE)
                            && modification.key.equals("REGISTRATION_TIMESTAMP");
                }

                @Override
                public String getDescription()
                {
                    return "Attributes";
                }
            };
        IChangeRenderer renderer = new IChangeRenderer()
            {
                @Override
                public String render(Change change)
                {
                    return isInbetween(change.value, after, before) ? TIMESTAMP_OK
                            : "ERROR: Timestamp " + change.value + " is not between " + after + " and " + before;
                }
            };

        assertHistory(permId, filter, renderer, Collections.singleton(TIMESTAMP_OK));
    }

    private boolean isInbetween(String timestampValue, Date after, Date before)
    {
        if (timestampValue.startsWith(AttributeEntry.ATTRIBUTE) == false)
        {
            fail("Timestamp value does not start with " + AttributeEntry.ATTRIBUTE + ": " + timestampValue);
        }
        String timestampString = timestampValue.substring(AttributeEntry.ATTRIBUTE.length() + 1);
        try
        {
            long timestamp = DateFormatThreadLocal.DATE_FORMAT.get().parse(timestampString).getTime();
            long afterTimestamp = (after.getTime() / 1000 - 1) * 1000;
            long beforeTimestamp = (before.getTime() / 1000 + 2) * 1000;
            return timestamp >= afterTimestamp && timestamp <= beforeTimestamp;
        } catch (ParseException ex)
        {
            fail("Invalid time stamp format: " + timestampString);
            return false;
        }
    }

    public void assertAttachment(String permId, Set<String> attachments) throws Exception
    {
        IModificationFilter filter = new IModificationFilter()
            {
                @Override
                public boolean accept(Modification modification)
                {
                    return modification.type.equals(AttachmentEntry.ATTACHMENT);
                }

                @Override
                public String getDescription()
                {
                    return "Attachments";
                }
            };
        IChangeRenderer renderer = new IChangeRenderer()
            {
                @Override
                public String render(Change change)
                {
                    return change.key + " = " + change.value + " <" + change.attachmentContent + ">";
                }
            };
        assertHistory(permId, filter, renderer, attachments);
    }

    public void assertAttributes(String permId, Map<String, String> expectations) throws Exception
    {
        IModificationFilter filter = new IModificationFilter()
            {
                @Override
                public boolean accept(Modification modification)
                {
                    return modification.type.equals(AttributeEntry.ATTRIBUTE)
                            && modification.key.equals("REGISTRATION_TIMESTAMP") == false;
                }

                @Override
                public String getDescription()
                {
                    return "Attributes";
                }
            };
        Set<Entry<String, String>> entrySet = expectations.entrySet();
        Set<String> expected = new HashSet<>();
        for (Entry<String, String> entry : entrySet)
        {
            String key = entry.getKey();
            String value = entry.getValue();
            expected.add(key + " = " + render(AttributeEntry.ATTRIBUTE, value, null, null));
        }
        IChangeRenderer renderer = new IChangeRenderer()
            {
                @Override
                public String render(Change change)
                {
                    return change.key + " = " + change.value;
                }
            };
        assertHistory(permId, filter, renderer, expected);
    }

    @SafeVarargs
    public final void assertHistory(String permId, final String key, Set<String>... expected) throws Exception
    {
        IModificationFilter filter = new IModificationFilter()
            {
                @Override
                public boolean accept(Modification modification)
                {
                    return modification.key.equals(key);
                }

                @Override
                public String getDescription()
                {
                    return "Property " + key;
                }
            };
        IChangeRenderer renderer = new IChangeRenderer()
            {
                @Override
                public String render(Change change)
                {
                    return change.value;
                }
            };
        assertHistory(permId, filter, renderer, expected);
    }

    @SafeVarargs
    private final void assertHistory(String permId, IModificationFilter filter, IChangeRenderer renderer,
            Set<String>... expected) throws Exception
    {
        List<Change> changes = getSortedAndFilteredChanges(permId, filter);

        List<Set<String>> actualValues = new ArrayList<>();

        Date currentTime = changes.get(0).time;
        Set<String> currentSet = new HashSet<>();
        StringBuilder builder = new StringBuilder();
        for (Change change : changes)
        {
            if (equals(currentTime, change) == false)
            {
                actualValues.add(currentSet);
                currentSet = new HashSet<>(currentSet);
                currentTime = change.time;
            }
            String value = renderer.render(change);
            if (change.isRemoval)
            {
                if (!currentSet.remove(value))
                {
                    throw new IllegalStateException("Removing " + change + " from empty set");
                }
            } else
            {
                currentSet.add(value);
            }
            builder.append("\n").append(change);
        }

        actualValues.add(currentSet);

        List<Set<String>> expectedList = Arrays.asList(expected);

        if (expectedList.equals(actualValues) == false)
        {

            Assert.assertEquals(render(actualValues), render(expectedList),
                    filter.getDescription() + " of entity " + permId + " has wrong value history. Expected <"
                            + render(expectedList) + ">, actual <" + render(actualValues) + ">. Raw changes: "
                            + builder);
        }
    }

    private String render(List<Set<String>> values)
    {
        StringBuilder builder = new StringBuilder();

        for (Set<String> set : values)
        {
            ArrayList<String> sortedSet = new ArrayList<String>(set);
            Collections.sort(sortedSet);

            if (builder.length() > 0)
            {
                builder.append('\n');
            }
            builder.append("[\n");
            builder.append(ch.systemsx.cisd.common.shared.basic.string.StringUtils.joinList(sortedSet, ",\n"));
            builder.append("\n]");
        }
        return builder.toString();
    }

    private boolean equals(Date currentTime, Change change)
    {
        return currentTime == null ? change.time == null : currentTime.equals(change.time);
    }

    private static interface IModificationFilter
    {
        public String getDescription();

        public boolean accept(Modification modification);

    }

    private static interface IChangeRenderer
    {
        public String render(Change change);
    }

    private List<Change> getSortedAndFilteredChanges(String permId, IModificationFilter filter) throws IOException, JsonProcessingException
    {
        Session session = sessionFactory.getCurrentSession();
        Query query = session.createQuery("SELECT e FROM EventPE e WHERE :permId IN e.identifiersInternal").setParameter("permId", permId);
        EventPE event = (EventPE) query.uniqueResult();
        String attachmentContent = getAttachmentContent(event);

        JsonNode tree = new ObjectMapper().reader().readTree(event.getContent());

        List<Modification> allMods =
                IteratorUtils.toList(IteratorUtils.transformedIterator(tree.get(permId).elements(), new JsonNodeToModificationTransformer()));

        List<Modification> mods = new ArrayList<>();
        for (Modification mod : allMods)
        {
            if (filter.accept(mod))
            {
                mods.add(mod);
            }
        }

        List<Change> changes = new ArrayList<>();
        for (Modification mod : mods)
        {
            String value = render(mod.type, mod.value, mod.entityType, mod.userId);
            changes.add(new Change(mod.validFrom, mod.key, mod.userId, value, attachmentContent, false));
            if (mod.validUntil != null)
            {
                changes.add(new Change(mod.validUntil, mod.key, mod.userId, value, attachmentContent, true));
            }
        }

        Collections.sort(changes);

        return changes;
    }

    private String getAttachmentContent(EventPE event)
    {
        AttachmentContentPE attachmentContentPE = event.getAttachmentContent();
        String attachmentContent = "";
        if (attachmentContentPE != null)
        {
            attachmentContent = new String(attachmentContentPE.getValue());
        }
        return attachmentContent;
    }

    private String render(String type, String value, String entityTypeOrNull, String userId)
    {
        String result = type + ":" + value;
        if (entityTypeOrNull != null)
        {
            result += "[" + entityTypeOrNull + "]";
        }
        if (userId != null)
        {
            result += "(user:" + userId + ")";
        }
        return result;
    }

    private class JsonNodeToModificationTransformer implements Transformer<JsonNode, Modification>
    {
        @Override
        public Modification transform(JsonNode node)
        {
            Modification m = new Modification();
            m.userId = asString(node.get("userId"));
            m.type = asString(node.get("type"));
            m.key = asString(node.get("key"));
            m.value = asString(node.get("value"));
            m.entityType = asString(node.get("entityType"));
            m.validFrom = asDate(node.get("validFrom"));
            m.validUntil = asDate(node.get("validUntil"));
            return m;
        }

        private Date asDate(JsonNode node)
        {
            String textValue = asString(node);
            try
            {
                return StringUtils.isBlank(textValue) ? null : dateFormat.parse(textValue);
            } catch (ParseException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        private String asString(JsonNode node)
        {
            if (node == null)
            {
                return null;
            }
            return node.textValue();
        }
    }

    private static class Modification
    {
        public String userId;

        public String type;

        public String key;

        public String value;

        public String entityType;

        public Date validFrom;

        public Date validUntil;
    }

    public Map<String, String> props(String... keyvals)
    {
        Map<String, String> props = new HashMap<String, String>();
        for (int i = 0; i < keyvals.length; i += 2)
        {
            String key = keyvals[i];
            String val = keyvals[i + 1];
            props.put(key, val);
        }
        return props;
    }

    protected SamplePermId createSample(IExperimentId experiment, ISpaceId space, String code, String... properties)
    {
        SampleCreation sample = new SampleCreation();
        sample.setCode(code);
        sample.setExperimentId(experiment);
        sample.setSpaceId(space);
        sample.setTypeId(new EntityTypePermId("DELETION_TEST"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            sample.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createSamples(sessionToken, Arrays.asList(sample)).get(0);
    }

    protected DataSetPermId createDataSet(IExperimentId experiment, String code, String... properties)
    {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(experiment);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST"));
        creation.setCode(code + "-" + UUID.randomUUID().toString());
        creation.setDataSetKind(DataSetKind.PHYSICAL);
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        PhysicalDataCreation data = new PhysicalDataCreation();
        data.setLocation("test/location/" + code);
        data.setFileFormatTypeId(new FileFormatTypePermId("TIFF"));
        data.setLocatorTypeId(new RelativeLocationLocatorTypePermId());
        data.setStorageFormatId(new ProprietaryStorageFormatPermId());
        creation.setPhysicalData(data);

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            creation.setProperty(entry.getKey(), entry.getValue());
        }
        return v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);
    }

    protected DataSetPermId createContainerDataSet(IExperimentId experiment, String code, String... properties)
    {
        DataSetCreation creation = new DataSetCreation();
        creation.setExperimentId(experiment);
        creation.setTypeId(new EntityTypePermId("DELETION_TEST_CONTAINER"));
        creation.setCode(code + "-" + UUID.randomUUID().toString());
        creation.setDataSetKind(DataSetKind.CONTAINER);
        creation.setDataStoreId(new DataStorePermId("STANDARD"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            creation.setProperty(entry.getKey(), entry.getValue());
        }
        return v3api.createDataSets(sessionToken, Arrays.asList(creation)).get(0);
    }

    protected ExperimentPermId createExperiment(IProjectId projectId, String code, String... properties)
    {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode(code);
        experimentCreation.setProjectId(projectId);
        experimentCreation.setTypeId(new EntityTypePermId("DELETION_TEST"));

        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            experimentCreation.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createExperiments(sessionToken, Arrays.asList(experimentCreation)).get(0);
    }

    protected SpacePermId createSpace(String code)
    {
        SpaceCreation space = new SpaceCreation();
        space.setCode(code);
        return v3api.createSpaces(sessionToken, Arrays.asList(space)).get(0);
    }

    protected ProjectPermId createProject(SpacePermId space, String code)
    {
        ProjectCreation project = new ProjectCreation();
        project.setCode(code);
        project.setSpaceId(space);
        project.setDescription("description /" + space + "/" + code);
        return v3api.createProjects(sessionToken, Arrays.asList(project)).get(0);
    }

    protected void addAttachment(ProjectPermId project, AttachmentCreation... attachments)
    {
        ProjectUpdate projectUpdate = new ProjectUpdate();
        projectUpdate.setProjectId(project);
        projectUpdate.getAttachments().add(attachments);
        v3api.updateProjects(sessionToken, Arrays.asList(projectUpdate));
    }

    protected void addAttachment(ExperimentPermId experiment, AttachmentCreation... attachments)
    {
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.getAttachments().add(attachments);
        v3api.updateExperiments(sessionToken, Arrays.asList(experimentUpdate));
    }

    protected void addAttachment(SamplePermId sample, AttachmentCreation... attachments)
    {
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample);
        sampleUpdate.getAttachments().add(attachments);
        v3api.updateSamples(sessionToken, Arrays.asList(sampleUpdate));
    }

    protected MaterialPermId createMaterial(String code, Map<String, String> properties)
    {
        return createMaterial(code, "DELETION_TEST", properties);
    }

    protected MaterialPermId createMaterial(String code, String typeCode, Map<String, String> properties)
    {
        MaterialCreation material = new MaterialCreation();
        material.setCode(code);
        material.setTypeId(new EntityTypePermId(typeCode));

        for (Map.Entry<String, String> entry : properties.entrySet())
        {
            material.setProperty(entry.getKey(), entry.getValue());
        }

        return v3api.createMaterials(sessionToken, Arrays.asList(material)).get(0);
    }

    protected void updateChildren(SamplePermId sample, SamplePermId... children)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(children);
        update.setChildActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateParents(SamplePermId sample, SamplePermId... parents)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(parents);
        update.setParentActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateComponents(SamplePermId sample, SamplePermId... components)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        IdListUpdateValue<ISampleId> upd = new IdListUpdateValue<ISampleId>();
        upd.set(components);
        update.setComponentActions(upd.getActions());
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateContainer(SamplePermId sample, SamplePermId... containers)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        if (containers.length == 0)
        {
            update.setContainerId(null);
        } else if (containers.length == 1)
        {
            update.setContainerId(containers[0]);
        } else
        {
            throw new IllegalArgumentException("multiple containers not allowed");
        }
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void updateChildren(DataSetPermId dataSet, IDataSetId... children)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(children);
        update.setChildActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateParents(DataSetPermId dataSet, DataSetPermId... parents)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(parents);
        update.setParentActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateComponents(DataSetPermId dataSet, DataSetPermId... components)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(components);
        update.setComponentActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void updateContainers(DataSetPermId dataSet, DataSetPermId... components)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        IdListUpdateValue<IDataSetId> upd = new IdListUpdateValue<IDataSetId>();
        upd.set(components);
        update.setContainerActions(upd.getActions());
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IMaterialId material, String... properties)
    {
        MaterialUpdate update = new MaterialUpdate();
        update.setMaterialId(material);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateMaterials(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IExperimentId experiment, String... properties)
    {
        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(experiment);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateExperiments(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(ISampleId sample, String... properties)
    {
        SampleUpdate update = new SampleUpdate();
        update.setSampleId(sample);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateSamples(sessionToken, Arrays.asList(update));
    }

    protected void setProperties(IDataSetId dataSet, String... properties)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSet);
        for (Map.Entry<String, String> entry : props(properties).entrySet())
        {
            update.setProperty(entry.getKey(), entry.getValue());
        }
        v3api.updateDataSets(sessionToken, Arrays.asList(update));
    }
}
