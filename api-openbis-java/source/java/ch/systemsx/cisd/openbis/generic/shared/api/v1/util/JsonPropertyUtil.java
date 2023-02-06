/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.util;

/**
 * @author pkupczyk
 */
public class JsonPropertyUtil
{

    /*
     * Unfortunately some of our API DTOs are not in the API packages or refer to classes that are not in the API packages (for instance classes in
     * ch.systemsx.cisd.openbis.generic.shared.basic.dto package). To make things even worse these classes are reused in both the API and in GWT.
     * Creating a utility class that is reused by all these DTOs is therefore impossible. If that utility class is located in the API package then it
     * won't be available in GWT. On the other hand if it is located in a GWT module then is breaks the API code independence. Until we don't fix the
     * DTOs the best we can do is to have two JsonPropertyUtil classes. Once we have all API DTOs is the API packages the other JsonPropertyUtil class
     * can be removed.
     */

    public static final String toStringOrNull(final Long longOrNull)
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil
                .toStringOrNull(longOrNull);
    }

    public static final Long toLongOrNull(final String stringOrNull)
    {
        return ch.systemsx.cisd.openbis.generic.shared.basic.util.JsonPropertyUtil
                .toLongOrNull(stringOrNull);
    }

}
