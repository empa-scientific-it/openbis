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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.action.ITerminable;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.ITextHandler;

/**
 * Interface that represents a role that copies files and directories to another directory, usually on another file system.
 * <p>
 * <i>Note: If the copier is terminated, the <var>destinationDirectory</var> is in an undefined state afterwards.</i>
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public interface IPathCopier extends ITerminable, ISelfTestable
{
    /**
     * Returns <code>true</code> if progress is enabled. Enabled progress allows to retrieve progress information via stdout. In this case a provided
     * {@link ITextHandler} in the copy commands will receive this progress information
     */
    boolean isProgressEnabled();

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copy(File sourcePath, File destinationDirectory,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Copies the content of <var>sourcePath</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. It needs to be an existing, readable directory. The content of the directory is copied rather than the
     *            directory itself.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyContent(File sourcePath, File destinationDirectory,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var> on <var>destinationHost</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param destinationHostOrNull The host where the <var>destinationDirectory</var> resides or null if it is local.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or <code>null</code>, if the bulk transfer should be
     *            using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is used. May be <code>null</code> or the name of a
     *            non-existent file, in which case no password is used when accessing the rsync server.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyToRemote(File sourcePath, String destinationDirectory, String destinationHostOrNull,
            String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Copies the content of <var>sourcePath</var> to <var>destinationDir</var> on <var>destinationHost</var>.
     * 
     * @param sourcePath The source to copy. It needs to be an existing, readable directory. The content of the directory is copied rather than the
     *            directory itself.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param destinationHostOrNull The host where the <var>destinationDirectory</var> resides or null if it is local.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or <code>null</code>, if the bulk transfer should be
     *            using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is used. May be <code>null</code> or the name of a
     *            non-existent file, in which case no password is used when accessing the rsync server.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyContentToRemote(File sourcePath, String destinationDirectory,
            String destinationHostOrNull, String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Copies <var>sourcePath</var> on <var>sourceHost</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be readable.
     * @param sourceHost The host where the <var>sourcePath</var> resides
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or <code>null</code>, if the bulk transfer should be
     *            using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is used. May be <code>null</code> or the name of a
     *            non-existent file, in which case no password is used when accessing the rsync server.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyFromRemote(String sourcePath, String sourceHost, File destinationDirectory,
            String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Copies the content of <var>sourcePath</var> on <var>sourceHost</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. It needs to be an existing, readable directory. The content of the directory is copied rather than the
     *            directory itself.
     * @param sourceHost The host where the <var>sourcePath</var> resides
     * @param destinationDirectory The directory to use as a destination in the copy operation. It must be readable and writable. If
     *            <var>destinationDir/sourcePath</var> exists, it will be overwritten.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or <code>null</code>, if the bulk transfer should be
     *            using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is used. May be <code>null</code> or the name of a
     *            non-existent file, in which case no password is used when accessing the rsync server.
     * @param stdoutHandlerOrNull Handler of stdout lines. Can be <code>null</code>.
     * @param stderrHandlerOrNull Handler of stderr lines. Can be <code>null</code>.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyContentFromRemote(String sourcePath, String sourceHost, File destinationDirectory,
            String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull,
            ITextHandler stdoutHandlerOrNull, ITextHandler stderrHandlerOrNull);

    /**
     * Try to connect to the <var>host</var> via ssh and return whether the connection is OK.
     * 
     * @param host The host to check the connection to.
     * @param rsyncExecutableOnHostOrNull The rsync executable to use on the host, or <code>null</code>, if the first one in the path should be
     *            chosen.
     * @param millisToWaitForCompletion Time (in milli-seconds) to wait for the result. Specify {@link ConcurrencyUtilities#NO_TIMEOUT} if you want
     *            the connection to wait indefinitely.
     * @return <code>true</code> if the connection was successful and <code>false</code> otherwise.
     */
    boolean checkRsyncConnectionViaSsh(String host, String rsyncExecutableOnHostOrNull,
            long millisToWaitForCompletion);

    /**
     * Try to connect to the <var>host</var> via rsync server and return whether the connection is OK.
     * 
     * @param host The host to check the connection to.
     * @param rsyncModule The rsync module to use.
     * @param rsyncPassworFileOrNull The password file for the rsync connection or <code>null</code> if none should be used.
     * @param millisToWaitForCompletion Time (in milli-seconds) to wait for the result. Specify {@link ConcurrencyUtilities#NO_TIMEOUT} if you want
     *            the connection to wait indefinitely.
     * @return <code>true</code> if the connection was successful and <code>false</code> otherwise.
     */
    boolean checkRsyncConnectionViaRsyncServer(String host, String rsyncModule,
            String rsyncPassworFileOrNull, long millisToWaitForCompletion);
}
