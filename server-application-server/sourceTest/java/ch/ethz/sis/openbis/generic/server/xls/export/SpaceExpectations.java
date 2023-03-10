/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.xls.export;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.openbis.generic.server.business.bo.CollectionMatcher;

class SpaceExpectations extends Expectations
{

    public SpaceExpectations(final IApplicationServerApi api, final boolean exportReferred)
    {
        allowing(api).getSpaces(with(XLSExportTest.SESSION_TOKEN), with(new CollectionMatcher<>(
                        Arrays.asList(new SpacePermId("ELN_SETTINGS"), new SpacePermId("MATERIALS"),
                                new SpacePermId("PUBLICATIONS"))
                )),
                with(any(SpaceFetchOptions.class)));

        will(new CustomAction("getting spaces")
        {

            @Override
            public Object invoke(final Invocation invocation) throws Throwable
            {
                final SpaceFetchOptions fetchOptions = (SpaceFetchOptions) invocation.getParameter(2);
                fetchOptions.withRegistrator();

                final Space[] spaces = new Space[3];

                final Calendar calendar = Calendar.getInstance();
                calendar.set(2023, Calendar.MARCH, 10, 17, 23, 44);
                final Date registrationDate = calendar.getTime();

                final Person registrator = new Person();
                registrator.setUserId("test");

                spaces[0] = new Space();
                spaces[0].setFetchOptions(fetchOptions);
                spaces[0].setPermId(new SpacePermId("ELN_SETTINGS"));
                spaces[0].setCode("ELN_SETTINGS");
                spaces[0].setDescription("ELN Settings");
                spaces[0].setRegistrator(registrator);
                spaces[0].setRegistrationDate(registrationDate);

                spaces[1] = new Space();
                spaces[1].setFetchOptions(fetchOptions);
                spaces[1].setPermId(new SpacePermId("MATERIALS"));
                spaces[1].setCode("MATERIALS");
                spaces[1].setDescription("Folder for materials");
                spaces[1].setRegistrator(registrator);
                spaces[1].setRegistrationDate(registrationDate);

                spaces[2] = new Space();
                spaces[2].setFetchOptions(fetchOptions);
                spaces[2].setPermId(new SpacePermId("PUBLICATIONS"));
                spaces[2].setCode("PUBLICATIONS");
                spaces[2].setDescription("Folder for publications");
                spaces[2].setRegistrator(registrator);
                spaces[2].setRegistrationDate(registrationDate);

                return Arrays.stream(spaces).collect(Collectors.toMap(Space::getPermId, Function.identity(),
                        (space1, space2) -> space2, LinkedHashMap::new));
            }

        });
    }

}
