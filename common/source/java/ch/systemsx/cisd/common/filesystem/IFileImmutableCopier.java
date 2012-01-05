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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;


/**
 * A role which can perform an immutable copy of a file. <i>Immutable</i> here means, that the
 * copied file must not be changed or else the original file may be changed, too. It is, however,
 * safe to delete the file. This restrictions allows to use hard links for performing the copy which
 * can save a lot of disk space.
 * 
 * @author Bernd Rinn
 */
public interface IFileImmutableCopier
{
    /**
     * Creates an immutable copy of the {@link File} <code>source</code> in
     * <code>destinationDirectory</code>. <code>destinationDirectory/<name></code> must not
     * exist already or else an error is flagged.
     * <p>
     * Note that this method does not perform any checks about whether <var>source</var> exists and
     * is accessible. Use methods like {@link FileUtilities#checkPathFullyAccessible(File, String)}
     * for checking prior to calling this method where appropriate.
     * </p>
     * <p>
     * <i>Can use hard links if available.</i>
     * </p>
     * 
     * @param source The source file. Can not be <code>null</code> or a directory.
     * @param destinationDirectory The directory where given <var>source</var> should be copied. Can
     *            not be <code>null</code> and must be an existing directory.
     * @param nameOrNull The link name in the destination file. If it is <code>null</code>, the name
     *            of <var>source</var> will be used instead.
     * @return <code>true</code>, if the source file was copied successfully, <code>false</code>
     *         otherwise.
     */
    Status copyFileImmutably(File source, File destinationDirectory,
            String nameOrNull);

    /**
     * Creates an immutable copy of the {@link File} <code>source</code> in
     * <code>destinationDirectory</code>.
     * <p>
     * Note that this method does not perform any checks about whether <var>source</var> exists and
     * is accessible. Use methods like {@link FileUtilities#checkPathFullyAccessible(File, String)}
     * for checking prior to calling this method where appropriate.
     * </p>
     * <p>
     * <i>Can use hard links if available.</i>
     * </p>
     * 
     * @param source The source file. Can not be <code>null</code> or a directory.
     * @param destinationDirectory The directory where given <var>source</var> should be copied. Can
     *            not be <code>null</code> and must be an existing directory.
     * @param nameOrNull The link name in the destination file. If it is <code>null</code>, the name
     *            of <var>source</var> will be used instead.
     * @param mode Determines how to deal with target files that do already exist.
     * @return <code>true</code>, if the source file was copied successfully, <code>false</code>
     *         otherwise.
     */
    Status copyFileImmutably(File source, File destinationDirectory,
            String nameOrNull, CopyModeExisting mode);
}
