/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.global.search.GlobalSearchTextCriteria")
public class GlobalSearchTextCriteria extends AbstractFieldSearchCriteria<AbstractStringValue>
{

    private static final long serialVersionUID = 1L;

    public GlobalSearchTextCriteria()
    {
        super("anything", SearchFieldType.ANY_FIELD);
    }

    /**
     * Set the value to this criterion which is interpreted as a 'contains' match of any of the words in the phrase.
     * The result should contain any of the specified words.
     *
     * @param string the phrase to be matched.
     */
    public void thatContains(String string)
    {
        setFieldValue(new StringContainsValue(string));
    }

    /**
     * Set the value to this criterion which is interpreted as a 'contains' match of the whole phrase phrase.
     *
     * @param string the phrase to be matched.
     */
    public void thatContainsExactly(String string)
    {
        setFieldValue(new StringContainsExactlyValue(string));
    }

    /**
     * Set the value to this criterion which is interpreted as a lexical match.
     *
     * @param string the phrase to be matched.
     */
    public void thatMatches(final String string)
    {
        setFieldValue(new StringMatchesValue(string));
    }

    /**
     * Set the value to this criterion which is interpreted as a lexical "starts with" match.
     *
     * @param string the phrase to be matched.
     */
    public void thatStartsWith(final String string)
    {
        setFieldValue(new StringStartsWithValue(string));
    }

}