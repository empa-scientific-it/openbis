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

package ch.systemsx.cisd.common.filesystem.control;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Event source that generates events based on files in given directory. Events are named after the files that are found. Files are deleted after the
 * event has been read.
 * 
 * @author anttil
 */
public class ControlDirectoryEventFeed implements IEventFeed
{
    private final File controlDir;

    public ControlDirectoryEventFeed(File controlDir)
    {
        this.controlDir = controlDir;
    }

    @Override
    public List<String> getNewEvents(IEventFilter filter)
    {
        List<String> events = new ArrayList<String>();

        if (controlDir.exists() == false || controlDir.isDirectory() == false)
        {
            return events;
        }

        List<File> files = Arrays.asList(controlDir.listFiles());

        Collections.sort(files, new Comparator<File>()
            {
                @Override
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                }
            });

        for (File file : files)
        {
            String fileName = file.getName();
            if (file.isFile() && filter.accepts(fileName))
            {
                events.add(fileName);
                file.delete();
            }
        }
        return events;
    }
}
