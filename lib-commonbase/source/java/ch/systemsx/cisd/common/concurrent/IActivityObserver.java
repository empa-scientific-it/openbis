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
package ch.systemsx.cisd.common.concurrent;

import java.util.Observer;

/**
 * A simple role that can observe activity. It is a simple version of {@link Observer} that can only observes the fact that the {@link #update()}
 * method has been called.
 * 
 * @author Bernd Rinn
 */
public interface IActivityObserver
{
    /**
     * Called when activity occurred.
     */
    public void update();
}