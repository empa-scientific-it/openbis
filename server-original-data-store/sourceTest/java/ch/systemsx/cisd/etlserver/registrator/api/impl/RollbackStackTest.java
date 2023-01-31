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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.api.impl.RollbackStack.IRollbackStackDelegate;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.AbstractTestWithRollbackStack;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class RollbackStackTest extends AbstractTestWithRollbackStack
{
    @Test
    public void testRollback()
    {
        // Given
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD1"));
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD2"));
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD3"));
        assertEquals("CMD1 executed\n"
                + "CMD2 executed\n"
                + "CMD3 executed", getLogs());
        List<Integer> recordedSizes = new ArrayList<>();

        // When
        createRollBackStack().rollbackAll(new IRollbackStackDelegate()
            {
                @Override
                public void willContinueRollbackAll(RollbackStack stack)
                {
                    recordedSizes.add(getIndex());
                }
            });

        // Then
        assertEquals("CMD1 executed\n"
                + "CMD2 executed\n"
                + "CMD3 executed\n"
                + "CMD3 rolled back\n"
                + "CMD2 rolled back\n"
                + "CMD1 rolled back", getLogs());
        assertEquals("[-99, 1, 0]", recordedSizes.toString());
    }

    @Test
    public void testLargeRollback()
    {
        // Given
        int n = 100;
        StringBuilder builder = new StringBuilder();
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < n; i++)
        {
            String name = "CMD" + i;
            rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, name));
            builder.append(name).append(" executed\n");
            stack.push(name);
        }
        while (stack.empty() == false)
        {
            builder.append(stack.pop() + " rolled back\n");
        }

        // When
        createRollBackStack().rollbackAll();

        // Then
        assertEquals(builder.toString().trim(), getLogs());
    }

    @Test
    public void testCantRollbackIfLocked()
    {
        // Given
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD1"));
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD2"));
        rollbackStack.setLockedState(true);

        // When
        try
        {
            rollbackStack.rollbackAll();
            fail("The rollbackAll should fail when the rollback stack is in locked state");
        } catch (Exception ex)
        {
            // Then
            assertEquals("Rollback stack is in the locked state. Triggering rollback forbidden.", ex.getMessage());
            assertEquals("RollbackStack " + commandsFile + " with 2 commands to roll back", rollbackStack.toString());

            rollbackStack.setLockedState(false);

            rollbackStack.rollbackAll();
            assertEquals("CMD1 executed\n"
                    + "CMD2 executed\n"
                    + "CMD2 rolled back\n"
                    + "CMD1 rolled back", getLogs());
        }
    }

    @Test
    public void testResume()
    {
        // Given
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD1"));
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD2"));
        rollbackStack.pushAndExecuteCommand(new MockCommand(logFile, "CMD3"));
        try
        {
            createRollBackStack().rollbackAll(new IRollbackStackDelegate()
                {
                    @Override
                    public void willContinueRollbackAll(RollbackStack stack)
                    {
                        if (getIndex() == 1)
                        {
                            throw new RuntimeException("interrupted");
                        }
                    }
                });
            fail("Exception expected");
        } catch (RuntimeException e)
        {
            assertEquals("interrupted", e.getMessage());
        }
        assertEquals("CMD1 executed\n"
                + "CMD2 executed\n"
                + "CMD3 executed\n"
                + "CMD3 rolled back", getLogs());
        clearLogs();

        // When
        createRollBackStack().rollbackAll();

        // Then
        assertEquals("CMD2 rolled back\n"
                + "CMD1 rolled back", getLogs());
    }

    private String getLogs()
    {
        return FileUtilities.loadToString(logFile).trim();
    }

    private void clearLogs()
    {
        logFile.delete();
    }

    private RollbackStack createRollBackStack()
    {
        return new RollbackStack(commandsFile);
    }

    private int getIndex()
    {
        File indexFile = new File(commandsFile.getParentFile(), commandsFile.getName() + ".index");
        int index = indexFile.exists() ? Integer.parseInt(FileUtilities.loadExactToString(indexFile)) : -99;
        return index;
    }

    private static class MockCommand extends AbstractTransactionalCommand
    {
        private static final long serialVersionUID = 1L;

        private File logFile;

        private String name;

        MockCommand(File logFile, String name)
        {
            this.logFile = logFile;
            this.name = name;
        }

        @Override
        public void execute()
        {
            log(name + " executed");
        }

        @Override
        public void rollback()
        {
            log(name + " rolled back");
        }

        private void log(String message)
        {
            FileUtilities.appendToFile(logFile, message, true);
        }
    }

}
