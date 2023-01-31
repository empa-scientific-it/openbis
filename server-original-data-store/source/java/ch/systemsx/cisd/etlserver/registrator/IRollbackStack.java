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

package ch.systemsx.cisd.etlserver.registrator;

/**
 * This interface publishes a small part of the API of an RollbackStack, that is needed to (de)serialize storage processor transactions.
 * 
 * @author Kaloyan Enimanev
 */
public interface IRollbackStack
{

    /**
     * Push the command onto the stack and execute it.
     */
    void pushAndExecuteCommand(ITransactionalCommand cmd);

    /**
     * Sets the locked state of this rollback stack. Changing this state to true results in creating or deleting the marker file. If already in a
     * desired state - does nothing.
     */
    public void setLockedState(boolean lockedState);

    /**
     * Returns whether this rollback stack is in locked state (i.e. it cannot execute any rollback actions)
     */
    public boolean isLockedState();
}