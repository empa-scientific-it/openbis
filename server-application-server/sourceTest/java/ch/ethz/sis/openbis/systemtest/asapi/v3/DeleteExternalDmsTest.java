/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.delete.ExternalDmsDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;

public class DeleteExternalDmsTest extends AbstractLinkDataSetTest
{

    @Test
    public void deletionOfNothingSucceeds()
    {
        v3api.deleteExternalDataManagementSystems(session, new ArrayList<IExternalDmsId>(), opts);
    }

    @Test
    public void givenExternalDmsIsRemoved()
    {
        ExternalDmsPermId edms = create(externalDms());
        delete(edms);
        ExternalDms externalDms = get(edms);
        assertThat(externalDms, is(nullValue()));
    }

    @Test
    public void unrelatedExternalDmsIsNotRemoved()
    {
        String first = uuid();
        String second = uuid();

        ExternalDmsPermId edms1 = create(externalDms().withCode(first));
        ExternalDmsPermId edms2 = create(externalDms().withCode(second));

        delete(edms1);

        ExternalDms externalDms = get(edms2);
        assertThat(externalDms.getCode(), is(second));
    }

    @Test
    public void deletionRemovesCopies()
    {
        ExternalDmsPermId edms = create(externalDms());
        ContentCopyCreation creation = copyAt(edms).build();
        DataSetPermId id = create(linkDataSet().with(creation));

        delete(edms);

        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getContentCopies().size(), is(0));
    }

    @Test
    public void unrelatedCopiesAreNotRemoved()
    {
        ExternalDmsPermId edms1 = create(externalDms());
        ExternalDmsPermId edms2 = create(externalDms());

        DataSetPermId id = create(linkDataSet().with(
                copyAt(edms1).withExternalCode("CODE1"),
                copyAt(edms2).withExternalCode("CODE2")));

        delete(edms1);

        DataSet dataSet = get(id);
        assertThat(dataSet.getLinkedData().getContentCopies().size(), is(1));
        assertThat(dataSet.getLinkedData().getContentCopies().get(0).getExternalCode(), is("CODE2"));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDmsDeletionOptions o = new ExternalDmsDeletionOptions();
        o.setReason("test-reason");

        v3api.deleteExternalDataManagementSystems(sessionToken,
                Arrays.asList(new ExternalDmsPermId("TEST-LOGGING-1"), new ExternalDmsPermId("TEST-LOGGING-2")), o);

        assertAccessLog(
                "delete-external-dms  EXTERNAL_DMS_IDS('[TEST-LOGGING-1, TEST-LOGGING-2]') DELETION_OPTIONS('ExternalDmsDeletionOptions[reason=test-reason]')");
    }

}
