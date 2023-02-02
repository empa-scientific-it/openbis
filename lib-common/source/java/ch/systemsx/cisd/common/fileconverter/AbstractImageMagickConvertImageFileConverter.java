/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.fileconverter;

import java.util.Collections;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * An abstract file converter base class that uses the ImageMagick tool <code>convert</code>.
 * 
 * @author Bernd Rinn
 */
abstract class AbstractImageMagickConvertImageFileConverter extends
        AbstractExecutableFileConverter
{
    private final String imageMagickVersionOrNull;

    protected AbstractImageMagickConvertImageFileConverter(Logger machineLog, Logger operationLog)
    {
        super(machineLog, operationLog);
        if (getExecutablePath().startsWith("? "))
        {
            imageMagickVersionOrNull = null;
        } else
        {
            imageMagickVersionOrNull = tryGetImageMagickVersion();

        }
    }

    private static String tryExtractImageMagickVersion(String imageMagickVersionLine)
    {
        if (imageMagickVersionLine.startsWith("Version: ImageMagick") == false)
        {
            return null;
        } else
        {
            final String[] versionStringParts = imageMagickVersionLine.split("\\s+");
            if (versionStringParts.length < 3)
            {
                return null;
            }
            return versionStringParts[2];
        }
    }

    private String tryGetImageMagickVersion()
    {
        final ProcessResult result = runExecutable(Collections.singletonList("--version"));
        ProcessExecutionHelper.log(result);
        final String versionString = tryExtractImageMagickVersion(result.getOutput().get(0));
        return versionString;
    }

    @Override
    public boolean isAvailable()
    {
        if (imageMagickVersionOrNull == null)
        {
            return false;
        }
        String[] imageMagickVersionParts = imageMagickVersionOrNull.split("\\.");
        if (imageMagickVersionParts.length != 3)
        {
            return false;
        }
        final int imageMagickMajorVersion = Integer.parseInt(imageMagickVersionParts[0]);
        final int imageMagickMinorVersion = Integer.parseInt(imageMagickVersionParts[1]);
        if (imageMagickMajorVersion < 6 || imageMagickMinorVersion < 2)
        {
            return false;
        }
        return true;
    }

    /**
     * Checks for convert v6.2 or newer being installed and executable.
     */
    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        super.check();
        if (isAvailable() == false)
        {
            if (imageMagickVersionOrNull == null)
            {
                throw new ConfigurationFailureException("Invalid convert utility.");
            } else
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Convert utility is too old (expected: v6.2 or newer, found: v%s)",
                        imageMagickVersionOrNull);
            }
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using convert executable '%s', ImageMagick version %s",
                    getExecutablePath(), imageMagickVersionOrNull));
        }
    }

    @Override
    protected String getExecutableName()
    {
        return "convert";
    }

}
