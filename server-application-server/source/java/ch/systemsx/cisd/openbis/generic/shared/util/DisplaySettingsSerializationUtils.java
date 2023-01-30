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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Utility class containing methods for serialization and deserialization of {@link DisplaySettings}
 * 
 * @author Piotr Buczek
 */
public class DisplaySettingsSerializationUtils
{

    private DisplaySettingsSerializationUtils()
    {
        // Can not instantiate.
    }

    public static byte[] serializeDisplaySettings(DisplaySettings displaySettings)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            new ObjectOutputStream(baos).writeObject(displaySettings);
            return baos.toByteArray();
        } catch (IOException ex)
        {
            return null;
        }
    }

    public static DisplaySettings deserializeOrCreateDisplaySettings(byte[] serializedSettingsOrNull)
    {
        DisplaySettings result = null;
        if (serializedSettingsOrNull != null)
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(serializedSettingsOrNull);
            try
            {
                result = (DisplaySettings) new ObjectInputStream(bais).readObject();
            } catch (Exception ex)
            {
                // ignored using default settings
            }
        }
        if (result == null)
        {
            result = new DisplaySettings();
        }
        return result;
    }

}
