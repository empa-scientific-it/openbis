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

package ch.systemsx.cisd.common.db;

/**
 * A sequencer handler encapsulates stuff related to database sequencer.
 * 
 * @author Franz-Josef Elmer
 */
public interface ISequencerHandler
{
    /**
     * Returns the SQL script for obtaining the next value from the specified sequencer.
     * <p>
     * This is DB specific.
     * </p>
     */
    public String getNextValueScript(final String sequencer);
}