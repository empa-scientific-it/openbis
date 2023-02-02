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

package ch.systemsx.cisd.common.collection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A {@link BlockingQueue} with methods that are able to retrieve, but not remove, the head of the queue, waiting if no elements are present.
 * 
 * @author Bernd Rinn
 */
public interface IExtendedBlockingQueue<E> extends BlockingQueue<E>
{

    /**
     * Retrieves, but does not remove, the head of this queue, waiting if no elements are present on this queue.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    public E peekWait() throws InterruptedException;

    /**
     * Retrieves, but does not remove, the head of this queue, waiting if necessary up to the specified wait time if no elements are present on this
     * queue.
     * 
     * @param timeout how long to wait before giving up, in units of <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    public E peekWait(long timeout, TimeUnit unit) throws InterruptedException;

}
