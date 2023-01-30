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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

/**
 * Contexts in which web applications are available in OpenBIS.
 * 
 * @author pkupczyk
 */
public enum WebAppContext
{

    MODULES_MENU("modules-menu"), EXPERIMENT_DETAILS_VIEW("experiment-details-view"),
    SAMPLE_DETAILS_VIEW("sample-details-view"), DATA_SET_DETAILS_VIEW("data-set-details-view"),
    MATERIAL_DETAILS_VIEW("material-details-view");

    private final String name;

    private WebAppContext(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

}
