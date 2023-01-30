/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.PersonBuilder;

/**
 * @author Kaloyan Enimanev
 */
public class VocabulariesProviderTest extends AbstractProviderTest
{
    private Vocabulary animals;

    @BeforeMethod
    public final void setUpExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(server).listVocabularies(SESSION_TOKEN, true, false);

                    animals = new Vocabulary();
                    animals.setCode("ANIMALS");
                    animals.setDescription("description");
                    animals.setRegistrator(new PersonBuilder().name("Rodger", "Federer")
                            .getPerson());
                    animals.setURLTemplate("http://doodle.com?par=%s");
                    animals.setChosenFromList(true);

                    will(returnValue(Arrays.asList(animals)));
                }
            });
    }

    @Test
    public void testBrowse()
    {
        VocabulariesProvider vocabulariesProvider =
                new VocabulariesProvider(server, SESSION_TOKEN, true, false);
        TypedTableModel<Vocabulary> tableModel =
                vocabulariesProvider.getTableModel(Integer.MAX_VALUE);

        assertEquals("[CODE, DESCRIPTION, IS_MANAGED_INTERNALLY, REGISTRATOR, REGISTRATION_DATE, "
                + "MODIFICATION_DATE, URL_TEMPLATE, VOCABULARY_SHOW_AVAILABLE_TERMS_IN_CHOOSERS]",
                getHeaderIDs(tableModel).toString());

        List<TableModelRowWithObject<Vocabulary>> rows = tableModel.getRows();
        assertSame(animals, rows.get(0).getObjectOrNull());

        List<String> expectedValues =
                Arrays.asList(animals.getCode(), animals.getDescription(), "no", animals
                        .getRegistrator().getLastName(), animals.getRegistrator().getFirstName(),
                        "", "", animals.getURLTemplate(), "yes");

        assertEquals(expectedValues.toString(), rows.get(0).getValues().toString());
        assertEquals(1, rows.size());
        context.assertIsSatisfied();
    }

}
