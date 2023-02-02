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
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncBasedRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A fast {@link IImmutableCopier} that uses a fallback option whenever one of the fast copiers for either files or directories is not available.
 * 
 * @author Bernd Rinn
 */
public class FastRecursiveHardLinkMaker implements IImmutableCopier
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FastRecursiveHardLinkMaker.class);

    private static final String RSYNC_EXEC = "rsync";

    private static final String LN_EXEC = "ln";

    private final static int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    private final IImmutableCopier fallbackCopierOrNull;

    private final IFileImmutableCopier internFileCopierOrNull;

    private final IDirectoryImmutableCopier rsyncBasedDirectoryCopierOrNull;

    public final static IImmutableCopier tryCreate(final List<String> additionalCmdLineFlagsOrNull)
    {
        return tryCreate(TimingParameters.getDefaultParameters(), additionalCmdLineFlagsOrNull);
    }

    public final static IImmutableCopier tryCreate(final TimingParameters timingParameters,
            final List<String> additionalCmdLineFlagsOrNull)
    {
        final File rsyncExecOrNull = OSUtilities.findExecutable(RSYNC_EXEC);
        final File lnExecOrNull = OSUtilities.findExecutable(LN_EXEC);
        try
        {
            return create(rsyncExecOrNull, lnExecOrNull, timingParameters, false, additionalCmdLineFlagsOrNull);
        } catch (ConfigurationFailureException ex)
        {
            return null;
        }
    }

    public final static IImmutableCopier create(final File rsyncExecutable, final File lnExecutable,
            final List<String> additionalCmdLineFlagsOrNull)
    {
        return create(rsyncExecutable, lnExecutable, TimingParameters.getDefaultParameters(), false, additionalCmdLineFlagsOrNull);
    }

    public final static IImmutableCopier create(final File rsyncExecutable,
            final File lnExecutable, final TimingParameters parameters,
            final List<String> additionalCmdLineFlagsOrNull)
    {
        return new FastRecursiveHardLinkMaker(rsyncExecutable, lnExecutable, parameters, false, additionalCmdLineFlagsOrNull);
    }

    public final static IImmutableCopier create(final File rsyncExecutable,
            final File lnExecutable, final TimingParameters parameters, final boolean neverUseNative,
            final List<String> additionalCmdLineFlagsOrNull)
    {
        return new FastRecursiveHardLinkMaker(rsyncExecutable, lnExecutable, parameters,
                neverUseNative, additionalCmdLineFlagsOrNull);
    }

    private FastRecursiveHardLinkMaker(final File rsyncExcutable, final File lnExecutable,
            final TimingParameters timingParameters, final boolean neverUseNative,
            final List<String> additionalCmdLineFlagsOrNull)
            throws ConfigurationFailureException
    {
        this.internFileCopierOrNull =
                neverUseNative ? null : FastHardLinkMaker.tryCreate(timingParameters);
        this.rsyncBasedDirectoryCopierOrNull =
                (rsyncExcutable == null) ? null : new RsyncBasedRecursiveHardLinkMaker(rsyncExcutable, timingParameters,
                        DEFAULT_MAX_ERRORS_TO_IGNORE, additionalCmdLineFlagsOrNull);
        if (internFileCopierOrNull == null)
        {
            this.fallbackCopierOrNull =
                    RecursiveHardLinkMaker.tryCreate(HardLinkMaker.create(lnExecutable,
                            timingParameters));
        } else
        {
            this.fallbackCopierOrNull = RecursiveHardLinkMaker.tryCreate(internFileCopierOrNull);
        }
        if ((internFileCopierOrNull == null && fallbackCopierOrNull == null)
                || (rsyncBasedDirectoryCopierOrNull == null && fallbackCopierOrNull == null))
        {
            throw new ConfigurationFailureException("FastRecursiveHardLinkMaker not operational");
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(timingParameters.toString());
            if (internFileCopierOrNull != null)
            {
                operationLog.info("Using native library to create hard link copies of files.");
            } else
            {
                operationLog.info("Using 'ln' to create hard link copies of files.");
            }
            if (rsyncBasedDirectoryCopierOrNull != null)
            {
                operationLog.info("Using 'rsync' to traverse directories when making recursive "
                        + "hard link copies.");
            } else
            {
                operationLog.info("Using Java to traverse directories when making recursive hard "
                        + "link copies");
            }
        }
    }

    @Override
    public Status copyImmutably(File source, File destinationDirectory, String nameOrNull)
    {
        return copyImmutably(source, destinationDirectory, nameOrNull, CopyModeExisting.ERROR);
    }

    @Override
    public Status copyImmutably(File source, File destinationDirectory, String nameOrNull,
            CopyModeExisting mode)
    {
        if (source.isDirectory())
        {
            final File target = getTarget(source, destinationDirectory, nameOrNull, mode);
            if (rsyncBasedDirectoryCopierOrNull != null && (mode != CopyModeExisting.OVERWRITE
                    || target.exists() == false))
            {
                return rsyncBasedDirectoryCopierOrNull.copyDirectoryImmutably(source,
                        destinationDirectory, nameOrNull, mode);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull,
                        mode);
            }
        } else
        {
            if (internFileCopierOrNull != null)
            {
                return internFileCopierOrNull.copyFileImmutably(source, destinationDirectory,
                        nameOrNull, mode);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull,
                        mode);
            }
        }
    }

    private final static File getTarget(final File srcDir, final File destDir,
            final String nameOrNull, final CopyModeExisting mode) throws IOExceptionUnchecked
    {
        final String name = (nameOrNull == null) ? srcDir.getName() : nameOrNull;
        return new File(destDir, name);
    }
}
