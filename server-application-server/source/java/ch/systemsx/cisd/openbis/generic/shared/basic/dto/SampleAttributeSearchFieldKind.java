/*
 * Copyright 2009 ETH Zuerich, CISD
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

import java.io.Serializable;

/**
 * Kinds of fields connected with Sample attributes that can be used in detailed text queries.
 * 
 * @author Piotr Buczek
 */
public enum SampleAttributeSearchFieldKind implements Serializable, IAttributeSearchFieldKind
{
    CODE("Code"),

    SAMPLE_TYPE("Sample Type"),

    PERM_ID("Perm Id"),

    IDENTIFIER("Identifier"),

    SPACE("Space"),

    PROJECT("Project"),

    PROJECT_PERM_ID("Project Perm Id"),

    PROJECT_SPACE("Project Space"),

    METAPROJECT("Metaproject"),

    REGISTRATION_DATE(CommonAttributeSearchFieldKindDecsriptions.REGISTRATION_DATE_DESCRIPTION,
            null, new SearchFieldDateCriterionFactory()),

    MODIFICATION_DATE(CommonAttributeSearchFieldKindDecsriptions.MODIFICATION_DATE_DESCRIPTION,
            null, new SearchFieldDateCriterionFactory()),

    REGISTRATION_DATE_FROM(
            CommonAttributeSearchFieldKindDecsriptions.REGISTRATION_DATE_FROM_DESCRIPTION, null,
            new SearchFieldDateCriterionFactory()),

    MODIFICATION_DATE_FROM(
            CommonAttributeSearchFieldKindDecsriptions.MODIFICATION_DATE_FROM_DESCRIPTION, null,
            new SearchFieldDateCriterionFactory()),

    REGISTRATION_DATE_UNTIL(
            CommonAttributeSearchFieldKindDecsriptions.REGISTRATION_DATE_UNTIL_DESCRIPTION, null,
            new SearchFieldDateCriterionFactory()),

    MODIFICATION_DATE_UNTIL(
            CommonAttributeSearchFieldKindDecsriptions.MODIFICATION_DATE_UNTIL_DESCRIPTION, null,
            new SearchFieldDateCriterionFactory()),

    REGISTRATOR_USER_ID("Registrator User Id"),

    REGISTRATOR_FIRST_NAME("Registrator First Name"),

    REGISTRATOR_LAST_NAME("Registrator Last Name"),

    REGISTRATOR_EMAIL("Registrator Email"),

    MODIFIER_USER_ID("Modifier User Id"),

    MODIFIER_FIRST_NAME("Modifier First Name"),

    MODIFIER_LAST_NAME("Modifier Last Name"),

    MODIFIER_EMAIL("Modifier Email");

    private final String description;

    private final ISearchFieldAvailability availability;

    private final ISearchFieldCriterionFactory criterionFactory;

    private SampleAttributeSearchFieldKind(String description)
    {
        this(description, null, null);
    }

    private SampleAttributeSearchFieldKind(String description,
            ISearchFieldAvailability availability, ISearchFieldCriterionFactory criterionFactory)
    {
        this.description = description;
        this.availability = availability;
        this.criterionFactory = criterionFactory;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public String getCode()
    {
        return name();
    }

    @Override
    public ISearchFieldAvailability getAvailability()
    {
        return availability;
    }

    @Override
    public ISearchFieldCriterionFactory getCriterionFactory()
    {
        return criterionFactory;
    }

}
