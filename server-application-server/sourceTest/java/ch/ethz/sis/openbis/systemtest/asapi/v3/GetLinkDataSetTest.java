package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;

public class GetLinkDataSetTest extends AbstractLinkDataSetTest
{

    @Test
    void legacyFieldsAreProperlyPopulatedWithOpenBISBasedDataSets()
    {
        String externalCode = uuid();
        ExternalDmsPermId openbis = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        DataSetPermId id = create(linkDataSet().with(copyAt(openbis).withExternalCode(externalCode)));
        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getExternalCode(), is(externalCode));
        assertThat(dataSet.getLinkedData().getExternalDms(), isSimilarTo(get(openbis)));
    }

    @Test
    void legacyFieldsAreProperlyPopulatedWithURLBasedDataSets()
    {
        String externalCode = uuid();
        ExternalDmsPermId url = create(externalDms().withType(ExternalDmsAddressType.URL));
        DataSetPermId id = create(linkDataSet().with(copyAt(url).withExternalCode(externalCode)));
        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getExternalCode(), is(externalCode));
        assertThat(dataSet.getLinkedData().getExternalDms(), isSimilarTo(get(url)));
    }

    @Test
    void legacyFieldsAreProperlyPopulatedWithFileSystemBasedDataSets()
    {
        ExternalDmsPermId fs = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        DataSetPermId id = create(linkDataSet().with(copyAt(fs).withPath("/path/to/dir")));
        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getExternalCode(), is(""));
        assertThat(dataSet.getLinkedData().getExternalDms(), is(nullValue()));
    }

    @Test
    void legacyFieldsAreProperlyPopulatedWhenThereAreNoCopies()
    {
        DataSetPermId id = create(linkDataSet());
        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getExternalCode(), is(""));
        assertThat(dataSet.getLinkedData().getExternalDms(), is(nullValue()));
    }

    @Test
    void legacyFieldsAreProperlyPopulatedWithMultipleCopiesThatAreAllFileBased()
    {
        ExternalDmsPermId fs1 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ExternalDmsPermId fs2 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));

        DataSetPermId id = create(linkDataSet().with(
                copyAt(fs1).withPath("/path/to/dir"),
                copyAt(fs1).withPath("/path/to/dir2"),
                copyAt(fs2).withPath("/path/to/dir")));
        DataSet dataSet = get(id);

        assertThat(dataSet.getLinkedData().getContentCopies().size(), is(3));
        assertThat(dataSet.getLinkedData().getExternalCode(), is(""));
        assertThat(dataSet.getLinkedData().getExternalDms(), is(nullValue()));
    }

    @Test
    void legacyFieldsAreProperlyPopulatedWithMultipleCopiesSomeOfWhichResideInOpenBISOrInUrl()
    {
        String code = uuid();
        ExternalDmsPermId openbis = create(externalDms().withType(ExternalDmsAddressType.OPENBIS));
        ExternalDmsPermId url = create(externalDms().withType(ExternalDmsAddressType.URL));
        ExternalDmsPermId fs1 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        ExternalDmsPermId fs2 = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));

        DataSetPermId id = create(linkDataSet().with(
                copyAt(fs1).withPath("/path/to/dir"),
                copyAt(fs1).withPath("/path/to/dir2"),
                copyAt(fs2).withPath("/path/to/dir"),
                copyAt(openbis).withExternalCode(code),
                copyAt(url).withExternalCode(code)));
        DataSet dataSet = get(id);

        assertThat(dataSet.getLinkedData().getContentCopies().size(), is(5));
        assertThat(dataSet.getLinkedData().getExternalCode(), is(code));
        assertThat(dataSet.getLinkedData().getExternalDms(), anyOf(isSimilarTo(get(url)), isSimilarTo(get(openbis))));
    }

}
