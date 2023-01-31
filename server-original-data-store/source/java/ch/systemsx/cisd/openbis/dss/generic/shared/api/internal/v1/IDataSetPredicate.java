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

package ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1;

import java.util.List;

/**
 * Interface implemented by classes which are able to extract data set codes from a method argument of type <code>A</code>.
 * <p>
 * <i>This is an internal class. Do not use it as a user of the API.</i>
 *
 * @author Franz-Josef Elmer
 */
public interface IDataSetPredicate<A>
{
    /**
     * Extracts from the specified argument data set codes. Returns an empty list if there is no data set code to extract.
     */
    public List<String> getDataSetCodes(A argument);
}
