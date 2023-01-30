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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Placeholder data sets are an abstraction pointing to a non-yet existing data set which is about to be created. As of S216, place holder data sets
 * are removed from openbis but this is used on the Data store server and can stay.
 * 
 * @author Kaloyan Enimanev
 */
public class PlaceholderDataSet extends AbstractExternalData
{

    private static final long serialVersionUID = 1L;

    public PlaceholderDataSet()
    {
        super(false);
    }

    @Override
    public boolean isPlaceHolderDataSet()
    {
        return true;
    }

}
