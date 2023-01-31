/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author pkupczyk
 */
public class ImportStateMarkerFile
{

    public static final File[] listMarkerFiles(final String prefix, final File dir)
    {
        File[] markerFiles = dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    if (file.getName().startsWith(prefix))
                    {
                        String state = file.getName().substring(prefix.length());
                        try
                        {
                            ImportState.valueOf(state.toUpperCase());
                        } catch (IllegalArgumentException e)
                        {
                            return false;
                        }
                        return true;
                    } else
                    {
                        return false;
                    }
                }
            });
        return markerFiles;
    }

    public static final File setMarkerFile(final String prefix, final File dir, final ImportState state)
    {
        return setMarkerFile(prefix, dir, state, null);
    }

    public static final File setMarkerFile(final String prefix, final File dir, final ImportState state, String content)
    {
        File[] filesWithMatchingPrefix = dir.listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.getName().startsWith(prefix);
                }
            });

        for (File fileWithMatchingPrefix : filesWithMatchingPrefix)
        {
            FileUtilities.delete(fileWithMatchingPrefix);
        }

        try
        {
            File markerFile = new File(dir, prefix + state.name().toLowerCase());
            markerFile.createNewFile();

            if (content != null)
            {
                FileUtils.writeStringToFile(markerFile, content);
            }

            return markerFile;
        } catch (IOException e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }
}
