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
 * Kinds of fields connected with Data Set attributes that can be used in detailed text queries.
 * 
 * @author Piotr Buczek
 */
public enum DataSetAttributeSearchFieldKind implements Serializable, IAttributeSearchFieldKind
{

    // common fields

    CODE("Code"),

    DATA_SET_TYPE("Data Set Type"),

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

    MODIFIER_EMAIL("Modifier Email"),

    // physical data set fields

    LOCATOR_TYPE("Locator Type"),

    LOCATION("Location"),

    SHARE_ID("Share id"),

    SIZE("Size"),

    STORAGE_FORMAT("Storage format"),

    FILE_TYPE("File Type"),

    COMPLETE("Complete"),

    STATUS("Status"),

    ARCHIVING_REQUESTED("Archiving requested"),

    PRESENT_IN_ARCHIVE("Present in archive"),
    
    STORAGE_CONFIRMATION("Storage confirmed", new SearchFieldAvailableForAdmins(),
            new SearchFieldBooleanCriterionFactory()),

    SPEED_HINT("Speed hint"),

    EXTERNAL_DMS_CODE("External DMS code"),

    EXTERNAL_DMS_LABEL("External DMS label"),

    EXTERNAL_DMS_ADDRESS("External DMS address"),

    EXTERNAL_DMS_TYPE("External DMS type"),

    EXTERNAL_CODE("Link external code"),

    PATH("Link path"),

    COMMIT_HASH("Link commit hash"),

    COMMIT_REPOSITORY_ID("Link repository id");

    private final String description;

    private final ISearchFieldAvailability availability;

    private final ISearchFieldCriterionFactory criterionFactory;

    private DataSetAttributeSearchFieldKind(String description)
    {
        this(description, null, null);
    }

    private DataSetAttributeSearchFieldKind(String description,
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
