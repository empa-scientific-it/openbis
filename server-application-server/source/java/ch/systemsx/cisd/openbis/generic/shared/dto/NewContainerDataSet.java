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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class NewContainerDataSet extends NewExternalData
{
    private static final long serialVersionUID = 1L;

    private List<String> containedDataSetCodes = new ArrayList<String>();

    public List<String> getContainedDataSetCodes()
    {
        return containedDataSetCodes;
    }

    public void setContainedDataSetCodes(List<String> containedDataSetCodes)
    {
        this.containedDataSetCodes = containedDataSetCodes;
    }

    @Override
    protected void appendMore(ToStringBuilder builder)
    {
        builder.append("contained", containedDataSetCodes);
    }

}
