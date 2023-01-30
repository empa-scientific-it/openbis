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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

public class CreateExternalDmsTest extends AbstractExternalDmsTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Code is required.*")
    void creationFailsWithoutCode()
    {
        create(externalDms().withCode(null));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Type is required.*")
    void creationFailsWithoutType()
    {
        create(externalDms().withType(null));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Address is required.*")
    void creationFailsWithoutAddress()
    {
        create(externalDms().withAddress(null));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Code is required.*")
    void creationFailsWithEmptyCode()
    {
        create(externalDms().withCode(""));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Address is required.*")
    void creationFailsWithEmptyAddress()
    {
        create(externalDms().withAddress(""));
    }

    @Test
    void codeIsStoredProperly()
    {
        final String code = uuid();
        ExternalDmsPermId id = create(externalDms().withCode(code));
        ExternalDms dms = get(id);
        assertThat(dms.getCode(), is(code));
    }

    @Test
    void labelIsStoredProperly()
    {
        final String label = uuid();
        ExternalDmsPermId id = create(externalDms().withLabel(label));
        ExternalDms dms = get(id);
        assertThat(dms.getLabel(), is(label));
    }

    @Test
    void labelIsNotMandatory()
    {
        ExternalDmsPermId id = create(externalDms().withLabel(null));
        ExternalDms dms = get(id);
        assertThat(dms.getLabel(), is(nullValue()));
    }

    @Test
    void creationWithTypeOpenBISSucceeds()
    {
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.OPENBIS));
        ExternalDms dms = get(id);
        assertThat(dms.getAddressType(), is(ExternalDmsAddressType.OPENBIS));
    }

    @Test
    void creationWithTypeUrlSucceeds()
    {
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.URL));
        ExternalDms dms = get(id);
        assertThat(dms.getAddressType(), is(ExternalDmsAddressType.URL));
    }

    @Test
    void creationWithTypeFileSystemSucceeds()
    {
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.FILE_SYSTEM));
        ExternalDms dms = get(id);
        assertThat(dms.getAddressType(), is(ExternalDmsAddressType.FILE_SYSTEM));
    }

    @Test(dataProvider = "InvalidFileSystemAddresses", expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*Invalid address.*")
    void creationWithTypeFileSystemFailsWithInvalidAddress(String invalid)
    {
        create(externalDms().withType(ExternalDmsAddressType.FILE_SYSTEM).withAddress(invalid));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "(?s).*already exists.*")
    void codeHasToBeUnique()
    {
        final String code = uuid();
        create(externalDms().withCode(code));
        create(externalDms().withCode(code));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDmsCreation creation = new ExternalDmsCreation();
        creation.setCode("LOG_TEST_1");
        creation.setAddressType(ExternalDmsAddressType.URL);
        creation.setAddress("http://some.url1.com");

        ExternalDmsCreation creation2 = new ExternalDmsCreation();
        creation2.setCode("LOG_TEST_2");
        creation2.setAddressType(ExternalDmsAddressType.URL);
        creation2.setAddress("http://some.url2.com");

        v3api.createExternalDataManagementSystems(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-external-data-management-systems  NEW_EXTERNAL_DATA_MANAGEMENT_SYSTEMS('[ExternalDmsCreation[code=LOG_TEST_1], ExternalDmsCreation[code=LOG_TEST_2]]')");
    }

}
