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

package ch.systemsx.cisd.etlserver.registrator.api.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.IRollbackStack;
import ch.systemsx.cisd.etlserver.registrator.ITransactionalCommand;

/**
 * A package internal class to manage the rollback stack.
 * <p>
 * Since there is no persistent stack class, the rollback stack is implemented with two queues, optimized for pushing onto the stack (poping from the
 * stack is slower).
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class RollbackStack implements IRollbackStack
{
    /**
     * Delegate methods for the rollback stack, giving clients of the stack control over its behavior.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IRollbackStackDelegate
    {

        /**
         * Informs clients that the stack will rollback another item. Implementations may throw exceptions or block the thread until the stack can
         * continue.
         */
        void willContinueRollbackAll(RollbackStack stack);
    }

    private final File commandsFile;

    private final File commandOffsetsFile;

    private final File indexFile;

    private final File lockedMarkerFile;

    private final Logger operationLog;

    private long currentOffset;

    private DataOutputStream commandsOutputStream;

    private DataOutputStream indicesOutputStream;

    /**
     * Constructor for a rollback stack that uses queue1File and queue2File for the persistent queues.
     * 
     * @param commandsFile File for a persistent queue
     */
    public RollbackStack(File commandsFile)
    {
        this(commandsFile, null);
    }

    public RollbackStack(File commandsFile, Logger operationLog)
    {
        this.commandsFile = commandsFile;

        if (operationLog == null)
        {
            this.operationLog = LogFactory.getLogger(LogCategory.OPERATION, RollbackStack.class);
        } else
        {
            this.operationLog = operationLog;
        }

        this.commandOffsetsFile = new File(commandsFile.getParentFile(), commandsFile.getName() + ".offsets");
        this.indexFile = new File(commandsFile.getParentFile(), commandsFile.getName() + ".index");
        this.lockedMarkerFile = new File(commandsFile.getParentFile(), commandsFile.getName() + ".LOCKED");
        this.operationLog.info("Rollback stack: " + commandsFile);
    }

    /**
     * The size of the rollback stack
     */
    public int getSize()
    {
        return (int) (commandOffsetsFile.length() / Long.BYTES);
    }

    /**
     * Push the command onto the stack and execute it.
     */
    @Override
    public void pushAndExecuteCommand(ITransactionalCommand cmd)
    {
        try
        {
            byte[] serializedCommand = SerializationUtils.serialize(cmd);
            if (indicesOutputStream == null)
            {
                indicesOutputStream = new DataOutputStream(new FileOutputStream(commandOffsetsFile, true));
                currentOffset = 0;
            }
            currentOffset += serializedCommand.length;
            indicesOutputStream.writeLong(currentOffset);
            indicesOutputStream.flush();
            if (commandsOutputStream == null)
            {
                commandsOutputStream = new DataOutputStream(new FileOutputStream(commandsFile, true));
            }
            commandsOutputStream.write(serializedCommand);
            cmd.execute();
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        }
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
     */
    public void rollbackAll()
    {
        rollbackAll(new IRollbackStackDelegate()
            {
                @Override
                public void willContinueRollbackAll(RollbackStack stack)
                {
                    // Don't do anything
                }
            });
    }

    /**
     * Rollback any commands that have been executed. Rollback is done in the reverse order of execution.
     */
    public void rollbackAll(IRollbackStackDelegate delegate)
    {
        if (isLockedState())
        {
            throw new IllegalStateException(
                    "Rollback stack is in the locked state. Triggering rollback forbidden.");
        }
        RandomAccessFile rafile = null;
        try
        {
            long[] offsets = loadOffsets();
            int initialIndex = loadIndex(offsets);
            if (initialIndex >= 0)
            {
                operationLog.info("Rolling back " + (initialIndex + 1) + " of " + offsets.length + " commands");
                rafile = new RandomAccessFile(commandsFile, "r");
                int numberOfRolledBackCommands = 0;
                for (int index = initialIndex; index >= 0; index--)
                {
                    delegate.willContinueRollbackAll(this);
                    long offset2 = offsets[index];
                    long offset1 = index == 0 ? 0 : offsets[index - 1];
                    rafile.seek(offset1);
                    byte[] bytes = new byte[(int) (offset2 - offset1)];
                    rafile.read(bytes);
                    ITransactionalCommand cmd = (ITransactionalCommand) SerializationUtils.deserialize(bytes);
                    try
                    {
                        cmd.rollback();
                    } catch (Throwable t)
                    {
                        operationLog.error("Encountered error rolling back command " + cmd.toString(), t);
                    }
                    numberOfRolledBackCommands++;
                    saveIndex(index - 1);
                    if (numberOfRolledBackCommands % 100 == 0)
                    {
                        operationLog.info(numberOfRolledBackCommands + " commands rolled back");
                    }
                }
            }
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            if (rafile != null)
            {
                try
                {
                    rafile.close();
                } catch (IOException e)
                {
                    // silently ignored
                }
            }
        }
    }

    private long[] loadOffsets() throws Exception
    {
        if (commandOffsetsFile.exists() == false)
        {
            return new long[0];
        }
        DataInputStream stream = null;
        try
        {
            long[] offsets = new long[getSize()];
            stream = new DataInputStream(new FileInputStream(commandOffsetsFile));
            for (int i = 0; i < offsets.length; i++)
            {
                offsets[i] = stream.readLong();
            }
            return offsets;
        } finally
        {
            try
            {
                stream.close();
            } catch (IOException e)
            {
                // silently ignored
            }
        }
    }

    private int loadIndex(long[] offsets)
    {
        if (indexFile.exists() == false)
        {
            return offsets.length - 1;
        }
        return Integer.parseInt(FileUtilities.loadExactToString(indexFile));
    }

    private void saveIndex(int index)
    {
        FileUtilities.writeToFile(indexFile, Integer.toString(index));
    }

    /**
     * Throws away the persistent stack.
     */
    public void discard()
    {
        if (isLockedState())
        {
            throw new IllegalStateException(
                    "Discarding of locked rollback stack is illegal. Set locked to false first.");
        }
        commandOffsetsFile.delete();
        commandsFile.delete();
        indexFile.delete();
    }

    @Override
    public String toString()
    {
        return "RollbackStack " + commandsFile + " with " + getSize() + " commands to roll back";
    }

    /**
     * Internal getter for clients that need to serialize the rollback stack. Use this method with caution since it exposes implementation details.
     */
    public File getCommandsFile()
    {
        return commandsFile;
    }

    @Override
    public void setLockedState(boolean lockedState)
    {
        if (!lockedState && isLockedState())
        {
            deleteLockedMarkerFile();
        } else if (lockedState && false == isLockedState())
        {
            createLockedMarkerFile();
        }
    }

    @Override
    public boolean isLockedState()
    {
        return lockedMarkerFile.exists();
    }

    private void deleteLockedMarkerFile()
    {
        lockedMarkerFile.delete();
    }

    private void createLockedMarkerFile()
    {
        try
        {
            lockedMarkerFile.createNewFile();
        } catch (IOException ex)
        {
            operationLog.fatal("Failed to create rollback stack lock marker file "
                    + lockedMarkerFile.getAbsolutePath());
        }
    }

}
