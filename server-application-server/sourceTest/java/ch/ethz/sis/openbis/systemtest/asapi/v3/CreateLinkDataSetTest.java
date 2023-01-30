package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class CreateLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void copyInOpenBIS()
    {
        ExternalDmsPermId openbis = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ContentCopyCreation creation = copyAt(openbis).build();
        DataSetPermId id = create(linkDataSet().with(creation));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(1));

        ContentCopy copy = contentCopies.get(0);
        assertThat(copy.getExternalCode(), is(creation.getExternalId()));
        assertThat(copy.getPath(), is(creation.getPath()));
        assertThat(copy.getGitCommitHash(), is(creation.getGitCommitHash()));
        assertThat(copy.getGitRepositoryId(), is(creation.getGitRepositoryId()));
        assertThat(copy.getExternalDms(), isSimilarTo(get(openbis)));
    }

    @Test
    void copyInURL()
    {
        ExternalDmsPermId url = create(externalDms().withType(ExternalDmsAddressType.URL));
        ContentCopyCreation creation = copyAt(url).build();
        DataSetPermId id = create(linkDataSet().with(creation));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(1));

        ContentCopy copy = contentCopies.get(0);
        assertThat(copy.getExternalCode(), is(creation.getExternalId()));
        assertThat(copy.getPath(), is(creation.getPath()));
        assertThat(copy.getGitCommitHash(), is(creation.getGitCommitHash()));
        assertThat(copy.getGitRepositoryId(), is(creation.getGitRepositoryId()));
        assertThat(copy.getExternalDms(), isSimilarTo(get(url)));
    }

    @Test
    void copyInPlainFileSystem()
    {
        ExternalDmsPermId fs = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ContentCopyCreation creation = copyAt(fs).withPath("/path/to/dir").build();
        DataSetPermId id = create(linkDataSet().with(creation));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(1));

        ContentCopy copy = contentCopies.get(0);
        assertThat(copy.getExternalCode(), is(creation.getExternalId()));
        assertThat(copy.getPath(), is(creation.getPath()));
        assertThat(copy.getGitCommitHash(), is(creation.getGitCommitHash()));
        assertThat(copy.getGitRepositoryId(), is(creation.getGitRepositoryId()));
        assertThat(copy.getExternalDms(), isSimilarTo(get(fs)));
    }

    @Test
    void copyInGit()
    {
        ExternalDmsPermId fs = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ContentCopyCreation creation = copyAt(fs).withPath("/path/to/dir").withGitCommitHash("asdfasfa").withGitRepositoryId("repo 1").build();
        DataSetPermId id = create(linkDataSet().with(creation));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(1));

        ContentCopy copy = contentCopies.get(0);
        assertThat(copy.getExternalCode(), is(creation.getExternalId()));
        assertThat(copy.getPath(), is(creation.getPath()));
        assertThat(copy.getGitCommitHash(), is(creation.getGitCommitHash()));
        assertThat(copy.getGitRepositoryId(), is(creation.getGitRepositoryId()));
        assertThat(copy.getExternalDms(), isSimilarTo(get(fs)));
    }

    @Test
    void noCopies()
    {
        DataSetPermId id = create(linkDataSet());
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        assertThat(contentCopies.size(), is(0));
    }

    @Test
    void multipleCopies()
    {
        ExternalDmsPermId openbis1 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ExternalDmsPermId openbis2 = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));

        ExternalDmsPermId url1 = create(externalDms().withType(ExternalDmsAddressType.URL));
        ExternalDmsPermId url2 = create(externalDms().withType(ExternalDmsAddressType.URL));

        ExternalDmsPermId fs1 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ExternalDmsPermId fs2 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));

        ContentCopyCreation creation1 = copyAt(openbis1).build();
        ContentCopyCreation creation2 = copyAt(openbis1).build();

        ContentCopyCreation creation3 = copyAt(openbis2).build();
        ContentCopyCreation creation4 = copyAt(openbis2).build();

        ContentCopyCreation creation5 = copyAt(url1).build();
        ContentCopyCreation creation6 = copyAt(url1).build();

        ContentCopyCreation creation7 = copyAt(url2).build();
        ContentCopyCreation creation8 = copyAt(url2).build();

        ContentCopyCreation creation9 = copyAt(fs1).withPath("/path/to/dir").withGitCommitHash("asdf").withGitRepositoryId("repo 1").build();
        ContentCopyCreation creation10 = copyAt(fs1).withPath("/path/to/dir2").build();

        ContentCopyCreation creation11 = copyAt(fs2).withPath("/path/to/dir").withGitCommitHash("asdf").withGitRepositoryId("repo 1").build();
        ContentCopyCreation creation12 = copyAt(fs2).withPath("/path/to/dir2").build();

        DataSetPermId id = create(linkDataSet().with(creation1, creation2, creation3, creation4, creation5, creation6, creation7, creation8,
                creation9, creation10, creation11, creation12));
        List<ContentCopy> contentCopies = get(id).getLinkedData().getContentCopies();

        Set<String> expected = new HashSet<String>();
        for (ContentCopyCreation c : Arrays.asList(creation1, creation2, creation3, creation4, creation5, creation6, creation7, creation8,
                creation9, creation10, creation11, creation12))
        {
            expected.add(stringify(c));
        }

        Set<String> actual = new HashSet<String>();
        for (ContentCopy c : contentCopies)
        {
            actual.add(stringify(c));
        }
        assertThat(contentCopies.size(), is(12));
        assertThat(actual, IsIterableContainingInAnyOrder.<String> containsInAnyOrder(expected.toArray(new String[0])));
    }

    @Test(dataProvider = "InvalidLocationCombinations", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Invalid arguments.*")
    void cannotLinkToExternalDmsOfWrongType(ExternalDmsAddressType dmsType, String externalCode, String path, String gitCommitHash, String gitRepositoryId)
    {
        ExternalDmsPermId edms = create(externalDms().withType(dmsType));
        create(linkDataSet().with(copyAt(edms)
                .withExternalCode(externalCode)
                .withPath(path)
                .withGitCommitHash(gitCommitHash)
                .withGitRepositoryId(gitRepositoryId)));
    }
}
