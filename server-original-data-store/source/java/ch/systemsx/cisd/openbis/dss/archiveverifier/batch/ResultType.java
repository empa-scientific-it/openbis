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

package ch.systemsx.cisd.openbis.dss.archiveverifier.batch;

/**
 * Type of a result of verifiying a single data set archive.
 * 
 * @author anttil
 */
public enum ResultType
{
    OK(0), WARNING(1), GENERAL_WARNING(1), ERROR(2), FATAL(2);

    private final int exitCode;

    private ResultType(int exitCode)
    {
        this.exitCode = exitCode;
    }

    public int getExitCode()
    {
        return this.exitCode;
    }

}
