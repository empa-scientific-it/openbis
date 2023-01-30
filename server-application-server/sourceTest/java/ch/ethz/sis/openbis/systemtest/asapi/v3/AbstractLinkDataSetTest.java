package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.test.context.transaction.TestTransaction;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IContentCopyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.ContentCopyListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetHistoryPE;

public abstract class AbstractLinkDataSetTest extends AbstractExternalDmsTest
{

    private ExperimentPermId experiment;

    private SpacePermId space;

    private ProjectPermId project;

    private List<DataSetPermId> datasets = new ArrayList<DataSetPermId>();

    @BeforeClass
    protected void createData()
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("space-" + uuid());
        space = v3api.createSpaces(session, Collections.singletonList(spaceCreation)).get(0);

        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode("project-" + uuid());
        projectCreation.setSpaceId(space);
        project = v3api.createProjects(session, Collections.singletonList(projectCreation)).get(0);

        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setCode("experiment-" + uuid());
        experimentCreation.setProperty("DESCRIPTION", "DESCRIPTION");
        experimentCreation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        experimentCreation.setProjectId(project);
        experiment = v3api.createExperiments(session, Collections.singletonList(experimentCreation)).get(0);
    }

    @AfterClass
    protected void deleteData()
    {
        DataSetDeletionOptions dd = new DataSetDeletionOptions();
        dd.setReason("test");
        ExperimentDeletionOptions ed = new ExperimentDeletionOptions();
        ed.setReason("test");
        ProjectDeletionOptions pd = new ProjectDeletionOptions();
        pd.setReason("test");
        SpaceDeletionOptions sd = new SpaceDeletionOptions();
        sd.setReason("test");

        IDeletionId delDataSets = v3api.deleteDataSets(session, datasets, dd);
        IDeletionId delExperiments = v3api.deleteExperiments(session, Collections.singletonList(experiment), ed);
        v3api.confirmDeletions(session, Collections.singletonList(delDataSets));
        v3api.confirmDeletions(session, Collections.singletonList(delExperiments));
        v3api.deleteProjects(session, Collections.singletonList(project), pd);
        v3api.deleteSpaces(session, Collections.singletonList(space), sd);
    }

    protected DataSetPermId create(LinkDataCreationBuilder creation)
    {
        List<DataSetPermId> ids = v3api.createDataSets(session, Collections.singletonList(creation.build()));
        datasets.addAll(ids);
        return ids.get(0);
    }

    protected DataSet get(DataSetPermId id)
    {
        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withLinkedData().withExternalDms();
        Map<IDataSetId, DataSet> result = v3api.getDataSets(session, Collections.singletonList(id), fo);
        return result.get(id);
    }

    protected void update(LinkDataUpdateBuilder update)
    {
        v3api.updateDataSets(session, Collections.singletonList(update.build()));
    }

    protected LinkDataUpdateBuilder dataset(IDataSetId id)
    {
        return new LinkDataUpdateBuilder(id);
    }

    protected ContentCopyCreationBuilder copyAt(ExternalDmsPermId edmsId)
    {
        return new ContentCopyCreationBuilder(edmsId);
    }

    protected LinkDataCreationBuilder linkDataSet()
    {
        return new LinkDataCreationBuilder();
    }

    protected class ContentCopyCreationBuilder
    {

        private ExternalDmsPermId edmsId;

        private ExternalDmsAddressType type;

        private String externalCode;

        private String path;

        private String gitCommitHash;

        private String gitRepositoryId;

        public ExternalDmsPermId getEdmsId()
        {
            return edmsId;
        }

        public ExternalDmsAddressType getType()
        {
            return type;
        }

        public String getExternalCode()
        {
            return externalCode;
        }

        public String getPath()
        {
            return path;
        }

        public String getGitCommitHash()
        {
            return gitCommitHash;
        }

        public String getGitRepositoryId()
        {
            return gitRepositoryId;
        }

        public ContentCopyCreationBuilder(ExternalDmsPermId edmsId)
        {
            if (edmsId != null)
            {
                this.edmsId = edmsId;
                this.type = get(edmsId).getAddressType();
                switch (type)
                {
                    case OPENBIS:
                    case URL:
                        externalCode = uuid();
                        break;
                    case FILE_SYSTEM:
                        path = "/" + uuid();
                }
            }
        }

        public ContentCopyCreationBuilder withExternalCode(String externalCode)
        {
            this.externalCode = externalCode;
            return this;
        }

        public ContentCopyCreationBuilder withPath(String path)
        {
            this.path = path;
            return this;
        }

        public ContentCopyCreationBuilder withGitCommitHash(String gitCommitHash)
        {
            this.gitCommitHash = gitCommitHash;
            return this;
        }

        public ContentCopyCreationBuilder withGitRepositoryId(String gitRepositoryId)
        {
            this.gitRepositoryId = gitRepositoryId;
            return this;
        }

        public ContentCopyCreation build()
        {
            ContentCopyCreation creation = new ContentCopyCreation();
            creation.setExternalDmsId(edmsId);
            creation.setExternalId(externalCode);
            creation.setPath(path);
            creation.setGitCommitHash(gitCommitHash);
            creation.setGitRepositoryId(gitRepositoryId);
            return creation;
        }
    }

    protected class LinkDataCreationBuilder
    {

        private List<ContentCopyCreation> copies = new ArrayList<>();

        private String externalCode;

        private IExternalDmsId edms;

        public LinkDataCreationBuilder with(ContentCopyCreationBuilder copy1, ContentCopyCreationBuilder... rest)
        {
            LinkDataCreationBuilder result = with(copy1.build());
            for (ContentCopyCreationBuilder b : rest)
            {
                result = result.with(b.build());
            }
            return result;
        }

        public LinkDataCreationBuilder with(ContentCopyCreation first, ContentCopyCreation... rest)
        {
            copies.add(first);
            copies.addAll(Arrays.asList(rest));
            return this;
        }

        public LinkDataCreationBuilder withExternalCode(String externalCode)
        {
            this.externalCode = externalCode;
            return this;
        }

        public LinkDataCreationBuilder withExternalDms(IExternalDmsId externalDms)
        {
            this.edms = externalDms;
            return this;
        }

        public DataSetCreation build()
        {
            LinkedDataCreation linkData = new LinkedDataCreation();
            linkData.setContentCopies(copies);
            linkData.setExternalCode(externalCode);
            linkData.setExternalDmsId(edms);
            DataSetCreation creation = new DataSetCreation();
            creation.setLinkedData(linkData);
            creation.setTypeId(new EntityTypePermId("LINK_TYPE"));
            creation.setDataSetKind(DataSetKind.LINK);
            creation.setExperimentId(experiment);
            creation.setAutoGeneratedCode(true);
            creation.setDataStoreId(new DataStorePermId("STANDARD"));
            return creation;
        }
    }

    protected class LinkDataUpdateBuilder
    {
        private IDataSetId id;

        private String externalCode;

        private List<ContentCopyCreation> newCopies = new ArrayList<>();

        private List<IContentCopyId> removedCopies = new ArrayList<>();

        private List<ContentCopyCreation> replacementCopies = new ArrayList<>();

        private IExternalDmsId edms;

        public LinkDataUpdateBuilder(IDataSetId id)
        {
            this.id = id;
        }

        public LinkDataUpdateBuilder withExternalCode(String externalCode)
        {
            this.externalCode = externalCode;
            return this;
        }

        public LinkDataUpdateBuilder withNewCopies(ContentCopyCreationBuilder first, ContentCopyCreationBuilder... rest)
        {
            LinkDataUpdateBuilder result = withNewCopies(first.build());
            for (ContentCopyCreationBuilder b : rest)
            {
                result = result.withNewCopies(b.build());
            }

            return result;
        }

        public LinkDataUpdateBuilder withNewCopies(ContentCopyCreation first, ContentCopyCreation... rest)
        {
            newCopies.add(first);
            newCopies.addAll(Arrays.asList(rest));
            return this;
        }

        public LinkDataUpdateBuilder setCopies(ContentCopyCreationBuilder first, ContentCopyCreationBuilder... rest)
        {
            LinkDataUpdateBuilder result = setCopies(first.build());
            for (ContentCopyCreationBuilder b : rest)
            {
                result = result.setCopies(b.build());
            }

            return result;
        }

        public LinkDataUpdateBuilder setCopies(ContentCopyCreation first, ContentCopyCreation... rest)
        {
            replacementCopies.add(first);
            replacementCopies.addAll(Arrays.asList(rest));
            return this;
        }

        public LinkDataUpdateBuilder without(IContentCopyId first, IContentCopyId... rest)
        {
            removedCopies.add(first);
            removedCopies.addAll(Arrays.asList(rest));
            return this;
        }

        public LinkDataUpdateBuilder withExternalDms(IExternalDmsId externalDms)
        {
            this.edms = externalDms;
            return this;
        }

        public DataSetUpdate build()
        {
            DataSetUpdate update = new DataSetUpdate();
            update.setDataSetId(id);
            LinkedDataUpdate linkedDataUpdate = new LinkedDataUpdate();

            if (externalCode != null)
            {
                linkedDataUpdate.setExternalCode(externalCode);
            }

            if (edms != null)
            {
                linkedDataUpdate.setExternalDmsId(edms);
            }

            ContentCopyListUpdateValue ccluv = new ContentCopyListUpdateValue();
            if (newCopies.isEmpty() == false)
            {
                ccluv.add(newCopies.toArray(new ContentCopyCreation[0]));
            }

            if (removedCopies.isEmpty() == false)
            {
                ccluv.remove(removedCopies.toArray(new IContentCopyId[0]));
            }

            if (replacementCopies.isEmpty() == false)
            {
                ccluv.set(replacementCopies.toArray(new ContentCopyCreation[0]));
            }

            linkedDataUpdate.setContentCopyActions(ccluv.getActions());
            update.setLinkedData(linkedDataUpdate);
            return update;
        }
    }

    protected String stringify(ContentCopyCreation c)
    {
        return c.getExternalId() + " / " + c.getPath() + " / " + c.getGitCommitHash() + " / " + c.getGitRepositoryId() + " / " + c.getExternalDmsId();
    }

    protected String stringify(ContentCopy c)
    {
        return c.getExternalCode() + " / " + c.getPath() + " / " + c.getGitCommitHash() + " / " + c.getGitRepositoryId() + " / "
                + c.getExternalDms().getCode();
    }

    @DataProvider(name = "InvalidLocationCombinations")
    public static Object[][] invalidLocationCombinations()
    {
        return new Object[][] {
                { ExternalDmsAddressType.OPENBIS, null, null, null, null },
                { ExternalDmsAddressType.OPENBIS, null, "/path", null, null },
                { ExternalDmsAddressType.OPENBIS, null, null, "hash", "repoId" },
                { ExternalDmsAddressType.OPENBIS, null, "/path", "hash", "repoId" },
                { ExternalDmsAddressType.OPENBIS, "code", "/path", null, null },
                { ExternalDmsAddressType.OPENBIS, "code", null, "hash", "repoId" },
                { ExternalDmsAddressType.OPENBIS, "code", "/path", "hash", "repoId" },

                { ExternalDmsAddressType.URL, null, null, null, null },
                { ExternalDmsAddressType.URL, null, "/path", null, null },
                { ExternalDmsAddressType.URL, null, null, "hash", "repoId" },
                { ExternalDmsAddressType.URL, null, "/path", "hash", "repoId" },
                { ExternalDmsAddressType.URL, "code", "/path", null, null },
                { ExternalDmsAddressType.URL, "code", null, "hash", "repoId" },
                { ExternalDmsAddressType.URL, "code", "/path", "hash", "repoId" },

                { ExternalDmsAddressType.FILE_SYSTEM, null, null, null, null },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", null, null, null },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", "/path", null, null },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", "/path", "hash", "repoId" },
                { ExternalDmsAddressType.FILE_SYSTEM, "code", null, "hash", "repoId" },
                { ExternalDmsAddressType.FILE_SYSTEM, null, null, "hash", "repoId" }
        };
    }

    protected List<DataSetHistoryPE> historyOf(DataSetPermId id)
    {
        TestTransaction.end();

        Session session = daoFactory.getSessionFactory().openSession();
        Query query = session.createQuery(
                "FROM DataSetHistoryPE as entry "
                        + "LEFT JOIN FETCH entry.entityInternal "
                        + "WHERE entry.entityInternal.code = :code")
                .setParameter("code", id.getPermId());

        @SuppressWarnings("unchecked")
        List<DataSetHistoryPE> result = query.list();
        session.close();
        return result;
    }

}
