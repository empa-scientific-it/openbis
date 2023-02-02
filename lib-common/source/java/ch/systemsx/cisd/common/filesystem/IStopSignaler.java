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

/**
 * A role that allows to signal that a worker was stopped.
 *
 * @author Bernd Rinn
 */
public interface IStopSignaler
{

    /**
     * Returns <code>true</code>, if the signaler has been stopped and <code>false</code> otherwise.
     * <p>
     * Not throwing a StopException, but signaling back that the worker was stopped by means of this method implies that it was prepared to handle the
     * StopException and clean up after itself.
     */
    boolean isStopped();

}
