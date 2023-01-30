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

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.update.ExternalDmsUpdate;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class UpdateExternalDmsTest extends AbstractExternalDmsTest
{

    @Test
    void validUpdateSucceeds()
    {
        ExternalDmsPermId edms = create(externalDms());
        update(externalDms(edms).withAddress("address").withLabel("label"));
        ExternalDms updated = get(edms);

        assertThat(updated.getAddress(), is("address"));
        assertThat(updated.getLabel(), is("label"));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Address is required.*")
    void addressCannotBeSetToBeNull()
    {
        ExternalDmsPermId edms = create(externalDms());
        update(externalDms(edms).withAddress(null));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Address is required.*")
    void addressCannotBeSetToBeEmpty()
    {
        ExternalDmsPermId edms = create(externalDms());
        update(externalDms(edms).withAddress(""));
    }

    @Test(dataProvider = "InvalidFileSystemAddresses", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Invalid address.*")
    void updateWithTypeFileSystemFailsWithInvalidAddress(String invalid)
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        update(externalDms(edms).withAddress(invalid));
    }

    @Test
    void updateWithTypeFileSystemSucceedsWithValidAddress()
    {
        ExternalDmsPermId edms = create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM));
        update(externalDms(edms).withAddress("host:/valid/path/" + uuid()));
    }

    @Test
    void labelCanBeSetToNull()
    {
        ExternalDmsPermId edms = create(externalDms());
        update(externalDms(edms).withLabel(null));
        ExternalDms updated = get(edms);

        assertThat(updated.getLabel(), is(nullValue()));
    }

    @Test
    void labelCanBeSetToBeEmpty()
    {
        ExternalDmsPermId edms = create(externalDms());
        update(externalDms(edms).withLabel(""));
        ExternalDms updated = get(edms);

        assertThat(updated.getLabel(), is(""));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDmsUpdate update = new ExternalDmsUpdate();
        update.setExternalDmsId(new ExternalDmsPermId("DMS_1"));

        ExternalDmsUpdate update2 = new ExternalDmsUpdate();
        update2.setExternalDmsId(new ExternalDmsPermId("DMS_2"));

        v3api.updateExternalDataManagementSystems(sessionToken, Arrays.asList(update, update2));

        assertAccessLog(
                "update-external-dms  EXTERNAL_DMS_UPDATES('[ExternalDmsUpdate[externalDmsId=DMS_1], ExternalDmsUpdate[externalDmsId=DMS_2]]')");
    }

}
