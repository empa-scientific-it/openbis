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

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask.IScannedStore;

/**
 * A helper class for {@link DirectoryScanningTimerTask} which performs operations before and after treating the matching paths.
 * 
 * @author Christian Ribeaud
 * @see DirectoryScanningTimerTask
 */
public interface IDirectoryScanningHandler
{

    /**
     * The instruction flag of whether to process an item or not.
     */
    public enum HandleInstructionFlag
    {
        PROCESS, IGNORE, ERROR
    }

    /**
     * The instruction of whether to process an item or not, possibly including a message on why not to process it.
     * 
     * @author Bernd Rinn
     */
    public static class HandleInstruction
    {
        public final static HandleInstruction PROCESS =
                new HandleInstruction(HandleInstructionFlag.PROCESS, null);

        public final static HandleInstruction IGNORE =
                new HandleInstruction(HandleInstructionFlag.IGNORE, null);

        private final HandleInstructionFlag flag;

        private final String messageOrNull;

        public static HandleInstruction createError()
        {
            return new HandleInstruction(HandleInstructionFlag.ERROR, null);
        }

        public static HandleInstruction createError(String message)
        {
            assert message != null;

            return new HandleInstruction(HandleInstructionFlag.ERROR, message);
        }

        public static HandleInstruction createError(String messageTemplate, Object... args)
        {
            assert messageTemplate != null;

            return new HandleInstruction(HandleInstructionFlag.ERROR, String.format(
                    messageTemplate, args));
        }

        private HandleInstruction(HandleInstructionFlag flag, String messageOrNull)
        {
            assert flag != null;

            this.flag = flag;
            this.messageOrNull = messageOrNull;
        }

        public final HandleInstructionFlag getFlag()
        {
            return flag;
        }

        public final String tryGetMessage()
        {
            return messageOrNull;
        }
    }

    /**
     * Run once before the handler is used.
     */
    public void init(IScannedStore scannedStore);

    /**
     * Is performed just before handling all the items contained in the store.
     */
    public void beforeHandle(IScannedStore scannedStore);

    /**
     * Whether given <code>storeItem</code> found in given <var>scannedStore</var> should be processed or not, and whether not processing it
     * constitues an error or not.
     */
    public HandleInstruction mayHandle(IScannedStore scannedStore, StoreItem storeItem);

    /**
     * Finishes and closes the handling of given <var>storeItem</var>.
     * 
     * @returns A status of handling the <var>storeItem</var>.
     */
    public Status finishItemHandle(IScannedStore scannedStore, StoreItem storeItem);
}
