/*
 * Copyright 2008 ETH Zuerich, CISD
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

/**
 * @author Franz-Josef Elmer
 */
public interface IFileFactory
{

    /** Creates given <var>path</var>. */
    public IFile create(String path);

    /** Creates given <var>relativePath</var> in given <var>baseDir</var>. */
    public IFile create(IFile baseDir, String relativePath);
}
