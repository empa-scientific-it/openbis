/*
 * Copyright ETH 2010 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.common.filesystem;

import java.io.File;

/**
 * Role that implements a strategy deciding on whether a file is allowed to be overwritten.
 * 
 * @author Bernd Rinn
 */
public interface IFileOverwriteStrategy
{
    /**
     * Returns <code>true</code> if the existing <var>outputFile</var> can be overwritten and <code>false</code> otherwise.
     */
    public boolean overwriteAllowed(File outputFile);
}