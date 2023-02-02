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

package ch.systemsx.cisd.common.filesystem;

/**
 * An enum to specify how to deal with existing files when copying files and directories.
 * 
 * @author Bernd Rinn
 */
public enum CopyModeExisting
{
    /** It is an error, if a target already exists. */
    ERROR,
    /**
     * An already existing target will be ignored (i.e. the existing version will remain unchanged).
     */
    IGNORE,
    /** A target already existing will be overwritten. */
    OVERWRITE
}