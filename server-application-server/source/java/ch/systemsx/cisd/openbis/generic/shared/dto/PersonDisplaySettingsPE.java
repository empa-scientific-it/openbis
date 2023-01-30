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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.util.DisplaySettingsSerializationUtils;

/**
 * @author Jakub Straszewski
 */
@Entity
@Table(name = TableNames.PERSONS_TABLE)
public final class PersonDisplaySettingsPE implements IIdHolder, Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private transient Long id;

    private DisplaySettings displaySettings;

    private byte[] serializedDisplaySettings;

    //
    // IIdHolder
    //
    @Override
    @Id
    public final Long getId()
    {
        return id;
    }

    public final void setId(final Long id)
    {
        this.id = id;
    }

    @Transient
    public DisplaySettings getDisplaySettings()
    {
        if (displaySettings == null)
        {
            byte[] serializedSettings = getSerializedDisplaySettings();
            displaySettings =
                    DisplaySettingsSerializationUtils
                            .deserializeOrCreateDisplaySettings(serializedSettings);
        }
        return displaySettings;
    }

    public void setDisplaySettings(DisplaySettings displaySettings)
    {
        this.displaySettings = displaySettings;
        setSerializedDisplaySettings(DisplaySettingsSerializationUtils
                .serializeDisplaySettings(displaySettings));
    }

    @Transient
    public int getDisplaySettingsSize()
    {
        return serializedDisplaySettings == null ? 0 : serializedDisplaySettings.length;
    }

    @Column(name = ColumnNames.PERSON_DISPLAY_SETTINGS, updatable = true)
    // @Type(type = "org.springframework.orm.hibernate3.support.BlobByteArrayType")
    private byte[] getSerializedDisplaySettings()
    {
        return serializedDisplaySettings;
    }

    private void setSerializedDisplaySettings(final byte[] value)
    {
        this.serializedDisplaySettings = value;
    }
}
