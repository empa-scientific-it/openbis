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

package ch.systemsx.cisd.imagereaders;

/**
 * A marker interface for read parameters.
 * <p>
 * All concrete implementations must reside in the same package as {@link IReadParams} in order to
 * be visible for all {@link IImageReaderLibrary}-s. Do not put {@link IReadParams} implementations
 * within the package of a concrete image library, because then the code using them will require the
 * library JAR file to be on the classpath.
 * 
 * @author Kaloyan Enimanev
 */
public interface IReadParams
{

}
