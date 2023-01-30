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

package ch.systemsx.cisd.openbis.uitest.menu;

import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Link;

public class AdminMenu
{

    @Locate("openbis_top-menu_ADMINISTRATION_MENU_MANAGE_GROUPS")
    private Link spaces;

    @Locate("openbis_top-menu_VOCABULARY_MENU_BROWSE")
    private Link vocabularies;

    @Locate("ADMINISTRATION_MENU_MANAGE_TYPES")
    private Link types;

    @Lazy
    @Locate("openbis_top-menu_SAMPLE_MENU_TYPES")
    private Link sampleTypes;

    @Lazy
    @Locate("openbis_top-menu_EXPERIMENT_MENU_TYPES")
    private Link experimentTypes;

    @Lazy
    @Locate("openbis_top-menu_DATA_SET_MENU_TYPES")
    private Link dataSetTypes;

    @Locate("ADMINISTRATION_MENU_MANAGE_PROPERTY_TYPES")
    private Link metadata;

    @Lazy
    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES")
    private Link browsePropertyTypes;

    @Lazy
    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS")
    private Link browsePropertyTypeAssignments;

    @Lazy
    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES")
    private Link newPropertyType;

    @Lazy
    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE")
    private Link assignToSampleType;

    @Lazy
    @Locate("openbis_top-menu_PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE")
    private Link assignToDataSetType;

    @Locate("openbis_top-menu_SCRIPT_MENU_BROWSE")
    private Link scripts;

    @Locate("ADMINISTRATION_MENU_MANAGE_AUTHORIZATION")
    private Link authorization;

    @Lazy
    @Locate("openbis_top-menu_AUTHORIZATION_MENU_ROLES")
    private Link roles;

    public void spaces()
    {
        spaces.click();
    }

    public void vocabularies()
    {
        vocabularies.click();
    }

    public void sampleTypes()
    {
        types.highlight();
        sampleTypes.click();
    }

    public void experimentTypes()
    {
        types.highlight();
        experimentTypes.click();
    }

    public void dataSetTypes()
    {
        types.highlight();
        dataSetTypes.click();
    }

    public void browsePropertyTypes()
    {
        metadata.highlight();
        browsePropertyTypes.click();
    }

    public void browsePropertyTypeAssignments()
    {
        metadata.highlight();
        browsePropertyTypeAssignments.click();
    }

    public void newPropertyType()
    {
        metadata.highlight();
        newPropertyType.click();
    }

    public void assignPropertyTypeToSampleType()
    {
        metadata.highlight();
        assignToSampleType.click();
    }

    public void assignPropertyTypeToDataSetType()
    {
        metadata.highlight();
        assignToDataSetType.click();
    }

    public void scripts()
    {
        scripts.click();
    }

    public void roles()
    {
        authorization.highlight();
        roles.click();
    }
}
