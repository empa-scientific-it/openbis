/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.suite.headless;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.uitest.type.DataSet;

/**
 * @author anttil
 */
public class MultiDssReporting extends HeadlessSuite
{

    @Test
    public void dataSetsFromAllDataStoreServersAreVisibleInReports() throws Exception
    {
        DataSet internal = create(aDataSet());
        DataSet external = create(aDataSet().inExternalDss());

        QueryTableModel reportFromInternal =
                reportInInternal(internal.getCode(), external.getCode());
        QueryTableModel reportFromExternal =
                reportInExternal(internal.getCode(), external.getCode());

        assertThat(reportFromInternal.getRows().size(), is(2));
        assertThat(reportFromExternal.getRows().size(), is(2));
    }
}
