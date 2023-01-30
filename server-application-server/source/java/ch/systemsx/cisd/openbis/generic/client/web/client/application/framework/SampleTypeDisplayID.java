/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

/**
 * Display setting identifiers of sample type drop down lists.
 * 
 * @author Izabela Adamczyk
 */
public enum SampleTypeDisplayID
{
    MAIN_SAMPLE_BROWSER("main_sample_browser", true),

    SAMPLE_REGISTRATION_PARENT_CHOOSER("sample_registration_parent_chooser", true),

    SAMPLE_REGISTRATION_CONTAINER_CHOOSER("sample_registration_container_chooser", true),

    // We don't store the following settings, because it's not clear if users would like that:

    DATA_SET_UPLOAD_SAMPLE_CHOOSER("data_set_upload_sample_chooser", false),

    DATA_SET_EDIT_SAMPLE_CHOOSER("data_set_edit_sample_chooser", false),

    SCRIPT_EDITOR_SAMPLE_CHOOSER("script_editor_sample_chooser", false),

    EXPERIMENT_REGISTRATION("experiment_registration", false),

    PROPERTY_ASSIGNMENT("property_assignment", false),

    SAMPLE_BATCH_UPDATE("sample_batch_update", false),

    SAMPLE_BATCH_REGISTRATION("sample_batch_registration", false),

    SAMPLE_REGISTRATION("sample_registration", false),

    SAMPLE_QUERY("sample_query", false),

    ;

    private static final String SAMPLE_TYPE_PREFIX = "sample-type";

    private final String id;

    private final boolean savable;

    private String suffix = "";

    private SampleTypeDisplayID(String displayID, boolean savable)
    {
        this.id = displayID;
        this.savable = savable;
    }

    public String createDisplayID()
    {
        return SAMPLE_TYPE_PREFIX + id + suffix;
    }

    public boolean isSavable()
    {
        return savable;
    }

    public SampleTypeDisplayID withSuffix(String value)
    {
        this.suffix = value;
        return this;
    }

}
