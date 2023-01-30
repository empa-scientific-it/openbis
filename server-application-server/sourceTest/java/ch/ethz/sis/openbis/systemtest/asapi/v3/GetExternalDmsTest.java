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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;

public class GetExternalDmsTest extends AbstractExternalDmsTest
{

    @Test
    void legacyFieldsAreFilledCorrectlyWithAddressTypeUrl()
    {
        String address = "http://some.url/${template}";
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.URL)
                .withAddress(address));
        ExternalDms externalDms = get(id);
        assertThat(externalDms.isOpenbis(), is(false));
        assertThat(externalDms.getUrlTemplate(), is(address));
    }

    @Test
    void legacyFieldsAreFilledCorrectlyWithAddressTypeOpenBIS()
    {
        String address = "http://some.url/${template}";
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.OPENBIS)
                .withAddress(address));
        ExternalDms externalDms = get(id);
        assertThat(externalDms.isOpenbis(), is(true));
        assertThat(externalDms.getUrlTemplate(), is(address));
    }

    @Test
    void legacyFieldsAreFilledCorrectlyWithAddressTypeFileSystem()
    {
        String address = "localhost:/tmp/store";
        ExternalDmsPermId id = create(externalDms()
                .withType(ExternalDmsAddressType.URL)
                .withAddress(address));
        ExternalDms externalDms = get(id);
        assertThat(externalDms.isOpenbis(), is(false));
        assertThat(externalDms.getUrlTemplate(), is(address));
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExternalDmsFetchOptions fo = new ExternalDmsFetchOptions();

        v3api.getExternalDataManagementSystems(sessionToken, Arrays.asList(new ExternalDmsPermId("DMS_1"), new ExternalDmsPermId("DMS_3")), fo);

        assertAccessLog(
                "get-external-data-management-systems  EXTERNAL_DMS_IDS('[DMS_1, DMS_3]') FETCH_OPTIONS('ExternalDms\n')");
    }

}
