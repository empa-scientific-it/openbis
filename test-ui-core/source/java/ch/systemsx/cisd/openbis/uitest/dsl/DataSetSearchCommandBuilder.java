/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.uitest.dsl;

import java.util.List;

import ch.systemsx.cisd.openbis.uitest.rmi.SearchForDataSetsOnBehalfOfUserRmi;
import ch.systemsx.cisd.openbis.uitest.rmi.SearchForDataSetsRmi;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;

/**
 * @author anttil
 */
public class DataSetSearchCommandBuilder extends SearchCommandBuilder<DataSet>
{
    @Override
    public Command<List<DataSet>> build()
    {
        if (this.user == null)
        {
            return new SearchForDataSetsRmi(criteria);
        } else
        {
            return new SearchForDataSetsOnBehalfOfUserRmi(criteria, user);
        }
    }
}
