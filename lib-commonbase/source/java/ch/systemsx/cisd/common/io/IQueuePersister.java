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

package ch.systemsx.cisd.common.io;

/**
 * A role that can persist a {@link java.util.Queue}. It is assumed that the persister gets a reference to the queue in the constructor.
 * 
 * @author Bernd Rinn
 */
public interface IQueuePersister<E>
{

    /**
     * Checks if the persister is in good working order.
     * 
     * @throws IllegalStateException If the persister has been closed.
     */
    public void check() throws IllegalStateException;

    /**
     * Close the persister. The persister must not be used after this method has been called.
     */
    public void close();

    /**
     * Synchronize the persistence store, ensuring that everything is written.
     */
    public void sync();

    /**
     * Persist the current form of the queue.
     * <p>
     * This method needs to be thread-safe if the queue is supposed to be thread-safe.
     */
    public void persist();

    /**
     * Add <var>elem</var> to the tail of the queue.
     * <p>
     * This method needs to be thread-safe if the queue is supposed to be thread-safe.
     */
    public void addToTail(E elem);

    /**
     * Remove <var>elem</var> from the head of the queue.
     * <p>
     * This method needs to be thread-safe if the queue is supposed to be thread-safe.
     */
    public void removeFromHead(E elem);

}
