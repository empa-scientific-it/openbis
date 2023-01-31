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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.DateFormatUtils;

import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;

/**
 * Generate a filename comprised of a timestamp and the information specified in the constructor.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssUniqueFilenameGenerator
{
    private final String sectionSeparator = "_";

    private final String threadName;

    private final String name;

    private final String extensionOrNull;

    private final ITimeProvider timeProvider;

    /**
     * Specify the information used to generate the filename
     * 
     * @param threadName
     * @param name
     * @param extensionOrNull
     */
    public DssUniqueFilenameGenerator(String threadName, String name, String extensionOrNull)
    {
        this(SystemTimeProvider.SYSTEM_TIME_PROVIDER, threadName, name, extensionOrNull);
    }

    public DssUniqueFilenameGenerator(ITimeProvider timeProvider, String threadName, String name, String extensionOrNull)
    {
        super();
        this.timeProvider = timeProvider;
        this.threadName = threadName.replace(File.separator, "_slash_");
        this.name = name;
        this.extensionOrNull = extensionOrNull;
    }

    public String generateFilename()
    {

        // The log file name is YYYY-MM-DD_HH-mm-ss-SSS_threadName_name.log
        StringBuilder filename = new StringBuilder();
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(timeProvider.getTimeInMilliseconds());

        String dateSection = DateFormatUtils.ISO_DATE_FORMAT.format(calendar);
        filename.append(dateSection);
        filename.append(sectionSeparator);

        String timeSection = DateFormatUtils.format(calendar, "HH-mm-ss-SSS");
        filename.append(timeSection);
        filename.append(sectionSeparator);

        filename.append(threadName);
        filename.append(sectionSeparator);

        filename.append(name);
        if (null != extensionOrNull)
        {
            filename.append(extensionOrNull);
        }
        return filename.toString();
    }

}
