/*
 * Copyright ETH 2016 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.sample.search.ListableSampleTypeSearchCriteria")
public class ListableSampleTypeSearchCriteria extends AbstractSearchCriteria
{
    private static final long serialVersionUID = 1L;

    private boolean listable;

    public ListableSampleTypeSearchCriteria()
    {

    }

    public void thatEquals(boolean value)
    {
        setListable(value);
    }

    public void setListable(boolean value)
    {
        this.listable = value;
    }

    public boolean isListable()
    {
        return listable;
    }

    @Override
    public String toString()
    {
        return "listable: " + isListable();
    }
}
