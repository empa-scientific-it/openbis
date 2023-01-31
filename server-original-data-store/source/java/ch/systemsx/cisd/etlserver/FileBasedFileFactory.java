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

import java.io.File;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;

/**
 * File factory based on {@link File}. Files are copies by creating hard links (if possible) if the parameter <var>hardLinkInsteadOfCopy</var> of the
 * constructor is set to <code>true</code>. Otherwise files are always copied.
 * 
 * @author Franz-Josef Elmer
 */
public class FileBasedFileFactory implements IFileFactory
{
    private final IImmutableCopier hardLinkCopierOrNull;

    /**
     * Creates a new instance with the specified copy policy. Uses default timing parameters.
     * 
     * @param hardLinkInsteadOfCopy If <code>true</code> hard links instead of copies are created.
     */
    public FileBasedFileFactory(final boolean hardLinkInsteadOfCopy)
    {
        this(hardLinkInsteadOfCopy, TimingParameters.getDefaultParameters());
    }

    /**
     * Creates a new instance with the specified copy policy.
     * 
     * @param hardLinkInsteadOfCopy If <code>true</code> hard links instead of copies are created.
     * @param timingParameters The timing parameters to use for copy oprations.
     */
    public FileBasedFileFactory(final boolean hardLinkInsteadOfCopy,
            final TimingParameters timingParameters)
    {
        this.hardLinkCopierOrNull =
                tryGetHardLinkCopier(hardLinkInsteadOfCopy, timingParameters);
    }

    private static IImmutableCopier tryGetHardLinkCopier(
            final boolean hardLinkInsteadOfCopy, final TimingParameters timingParameters)
    {
        if (hardLinkInsteadOfCopy)
        {
            return FastRecursiveHardLinkMaker.tryCreate(timingParameters, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        } else
        {
            return null;
        }
    }

    private final IFile wrap(final File file)
    {
        return new FileBasedFile(file, hardLinkCopierOrNull);
    }

    //
    // IFileFactory
    //

    @Override
    public final IFile create(final String path)
    {
        assert path != null : "Unspecified path.";
        final File file = new File(path);
        return wrap(file);
    }

    @Override
    public final IFile create(final IFile baseDir, final String relativePath)
    {
        assert baseDir != null : "Unspecified base directory.";
        assert relativePath != null : "Unspecified relative pate";
        assert FilenameUtils.getPrefixLength(relativePath) == 0 : String.format(
                "Given relative path '%s' is not relative.", relativePath);
        return wrap(new File(baseDir.getAbsolutePath(), relativePath));
    }

}
