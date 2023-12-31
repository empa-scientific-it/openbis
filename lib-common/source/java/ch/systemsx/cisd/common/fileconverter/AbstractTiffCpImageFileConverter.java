/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import org.apache.log4j.Logger;

/**
 * An abstract file converter base class that uses the LibTiff tool <code>tiffcp</code>.
 *
 * @author Bernd Rinn
 */
abstract class AbstractTiffCpImageFileConverter extends AbstractExecutableFileConverter
{

    protected AbstractTiffCpImageFileConverter(Logger machineLog, Logger operationLog)
    {
        super(machineLog, operationLog);
    }

    @Override
    protected String getExecutableName()
    {
        return "tiffcp";
    }

    @Override
    public boolean isAvailable()
    {
        return getExecutablePath().startsWith("? ") == false;
    }

}
