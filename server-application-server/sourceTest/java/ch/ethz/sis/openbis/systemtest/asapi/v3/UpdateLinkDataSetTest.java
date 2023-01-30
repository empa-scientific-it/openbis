package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.both;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import java.util.Date;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.internal.matchers.IsCollectionContaining;
import org.springframework.test.context.transaction.TestTransaction;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.ContentCopyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class UpdateLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void addCopy()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));
        String code = uuid();

        update(dataset(id).withNewCopies(copyAt(dms).withExternalCode(code)));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(2));
    }

    @Test
    void addFirstCopy()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet());
        String code = uuid();

        update(dataset(id).withNewCopies(copyAt(dms).withExternalCode(code)));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(1));
        assertThat(dataset.getLinkedData().getContentCopies().get(0).getExternalCode(), is(code));
    }

    @Test
    void removeCopy()
    {
        String removed = uuid();
        String stays = uuid();

        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms).withExternalCode(removed), copyAt(dms).withExternalCode(stays)));

        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        ContentCopyPermId removedId =
                contentCopies.get(0).getExternalCode().equals(removed) ? contentCopies.get(0).getId() : contentCopies.get(1).getId();
        update(dataset(id).without(removedId));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(1));
        assertThat(dataset.getLinkedData().getContentCopies().get(0).getExternalCode(), is(stays));
    }

    @Test
    void removeAndAddCopyWithSameInfoInSameRequest()
    {
        String externalCode = uuid();
        ExternalDmsPermId dms = create(externalDms());
        ContentCopyCreationBuilder copy = copyAt(dms).withExternalCode(externalCode);
        DataSetPermId id = create(linkDataSet().with(copy.build()));

        ContentCopyPermId toBeRemoved = get(id).getLinkedData().getContentCopies().get(0).getId();

        update(dataset(id).without(toBeRemoved).withNewCopies(copy));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(1));
        assertThat(dataset.getLinkedData().getContentCopies().get(0).getExternalCode(), is(externalCode));
    }

    @Test
    void removeLastCopy()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));

        ContentCopyPermId ccid = get(id).getLinkedData().getContentCopies().get(0).getId();
        update(dataset(id).without(ccid));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    void replaceCopies()
    {
        String code3 = uuid();
        String code4 = uuid();
        ExternalDmsPermId dms1 = create(externalDms());
        ExternalDmsPermId dms2 = create(externalDms());
        ExternalDmsPermId dms3 = create(externalDms());
        ExternalDmsPermId dms4 = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms1), copyAt(dms2)));

        update(dataset(id).setCopies(copyAt(dms3).withExternalCode(code3), copyAt(dms4).withExternalCode(code4)));

        List<ContentCopy> copies = get(id).getLinkedData().getContentCopies();

        assertThat(copies.size(), is(2));
        assertThat(copies, IsCollectionContaining.<ContentCopy> hasItems(
                both(Matchers.<ContentCopy> hasProperty("externalDms", isSimilarTo(get(dms3)))).and(
                        Matchers.<ContentCopy> hasProperty("externalCode", is(code3))),
                both(Matchers.<ContentCopy> hasProperty("externalDms", isSimilarTo(get(dms4)))).and(
                        Matchers.<ContentCopy> hasProperty("externalCode", is(code4)))));

    }

    @Test(dataProvider = "InvalidLocationCombinations", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Invalid arguments.*")
    void cannotLinkToExternalDmsOfWrongType(ExternalDmsAddressType dmsType, String externalCode, String path, String gitCommitHash,
            String gitRepositoryId)
    {
        ExternalDmsPermId edms = create(externalDms().withType(dmsType));

        DataSetPermId id = create(linkDataSet().with(copyAt(edms)));
        update(dataset(id).withNewCopies(copyAt(edms)
                .withExternalCode(externalCode)
                .withPath(path)
                .withGitCommitHash(gitCommitHash)
                .withGitRepositoryId(gitRepositoryId)));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*")
    void removingNonExistentCopyWillFail()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));

        ContentCopyPermId ccid = new ContentCopyPermId(uuid());
        update(dataset(id).without(ccid));
    }

    @Test
    void removeAllCopies()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)).with(copyAt(dms)));

        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();
        update(dataset(id).without(contentCopies.get(0).getId(), contentCopies.get(1).getId()));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getContentCopies().size(), is(0));
    }

    @Test
    void legacyExternalCodeCanBeSetOnSingleCopyOpenBISDms()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalCode(), is(code));
    }

    @Test
    void legacyExternalCodeCanBeSetOnSingleCopyUrlDms()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.URL));
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalCode(), is(code));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*multiple or zero copies.*")
    void legacyExternalCodeCanNotBeSetWithMultipleCopies()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        DataSetPermId id = create(linkDataSet().with(copyAt(dms), copyAt(dms)));
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalCode(), is(code));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*multiple or zero copies.*")
    void legacyExternalCodeCanNotBeSetWithNoCopies()
    {
        DataSetPermId id = create(linkDataSet());
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalCode(), is(code));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Cannot set external code.*")
    void legacyExternalCodeCannotBeSetOnSingleCopyFileSystemDms()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        DataSetPermId id = create(linkDataSet().with(copyAt(dms).withPath("/path")));
        String code = uuid();

        update(dataset(id).withExternalCode(code));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalCode(), is(code));
    }

    @Test
    void legacyExternalDmsCanBeSetOnSingleCopyOpenBISDms()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.URL));

        DataSetPermId id = create(linkDataSet().with(copyAt(dms1)));

        update(dataset(id).withExternalDms(dms2));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalDms(), isSimilarTo(get(dms2)));
    }

    @Test
    void legacyExternalDmsCanBeSetOnSingleCopyUrlDms()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.URL));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        DataSetPermId id = create(linkDataSet().with(copyAt(dms1)));

        update(dataset(id).withExternalDms(dms2));

        DataSet dataset = get(id);
        assertThat(dataset.getLinkedData().getExternalDms(), isSimilarTo(get(dms2)));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Cannot set external data management system to content copy of type.*")
    void legacyExternalDmsCannoBeSetFromFileSystemDms()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        DataSetPermId id = create(linkDataSet().with(copyAt(dms1).withPath("/path")));

        update(dataset(id).withExternalDms(dms2));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*using legacy methods.*")
    void legacyExternalDmsCannoBeSetToFileSystemDms()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));

        DataSetPermId id = create(linkDataSet().with(copyAt(dms1)));

        update(dataset(id).withExternalDms(dms2));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*multiple or zero copies.*")
    void legacyExternalDmsCanNotBeSetOnLinkDataSetWithNoCopies()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        DataSetPermId id = create(linkDataSet());

        update(dataset(id).withExternalDms(dms));
    }

    @Test
    void modificationTimeIsUpdatedWhenCopyIsAdded()
    {
        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));
        Date mod1 = get(id).getModificationDate();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        update(dataset(id).withNewCopies(copyAt(dms)));

        Date mod2 = get(id).getModificationDate();
        assertThat(mod1, lessThan(mod2));
    }

    @Test
    void modificationTimeIsUpdatedWhenCopyIsRemoved()
    {
        String removed = uuid();
        String stays = uuid();

        ExternalDmsPermId dms = create(externalDms());
        DataSetPermId id = create(linkDataSet().with(copyAt(dms).withExternalCode(removed), copyAt(dms).withExternalCode(stays)));
        DataSet dataSet = get(id);
        Date mod1 = dataSet.getModificationDate();

        List<ContentCopy> contentCopies = dataSet.getLinkedData().getContentCopies();
        ContentCopyPermId removedId =
                contentCopies.get(0).getExternalCode().equals(removed) ? contentCopies.get(0).getId() : contentCopies.get(1).getId();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        update(dataset(id).without(removedId));

        Date mod2 = get(id).getModificationDate();
        assertThat(mod1, lessThan(mod2));
    }

    @Test
    void modificationTimeIsUpdatedWhenSettingLegacyExternalCode()
    {
        ExternalDmsPermId dms = create(externalDms().withType(ExternalDmsAddressType.URL));
        DataSetPermId id = create(linkDataSet().with(copyAt(dms)));
        Date mod1 = get(id).getModificationDate();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        String code = uuid();
        update(dataset(id).withExternalCode(code));

        Date mod2 = get(id).getModificationDate();
        assertThat(mod1, lessThan(mod2));
    }

    @Test
    void modificationTimeIsUpdatedWhenSettingLegacyExternalDms()
    {
        ExternalDmsPermId dms1 = create(externalDms().withType(ExternalDmsAddressType.URL));
        ExternalDmsPermId dms2 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        DataSetPermId id = create(linkDataSet().with(copyAt(dms1)));
        Date mod1 = get(id).getModificationDate();

        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();

        update(dataset(id).withExternalDms(dms2));

        Date mod2 = get(id).getModificationDate();
        assertThat(mod1, lessThan(mod2));
    }
}
