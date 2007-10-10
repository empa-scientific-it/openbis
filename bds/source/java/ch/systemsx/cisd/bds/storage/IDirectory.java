/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds.storage;

import java.io.File;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IDirectory extends INode, Iterable<INode>
{
    public INode getNode(String name);
    
    public IDirectory makeDirectory(String name);
    
    public IFile<File> addRealFile(File file);
    
    public IFile<String> addKeyValuePair(String key, String value);
    
    public ILink addLink(String name, INode node);
}
