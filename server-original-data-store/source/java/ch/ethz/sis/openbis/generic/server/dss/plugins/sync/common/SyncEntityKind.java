/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common;

/**
 * Contrary to the Entity Kind in openbis core, this one also includes Projects which are "Entities" in OpenbisSync
 * 
 * @author Ganime Betul Akin
 */
public enum SyncEntityKind
{
    SPACE("SP"), PROJECT("P"), EXPERIMENT("E"), SAMPLE("S"), DATA_SET("D"), MATERIAL("M"), FILE("F");

    private final String abbreviation;

    private SyncEntityKind(String abbreviation)
    {
        this.abbreviation = abbreviation;

    }

    public final String getAbbreviation()
    {
        return abbreviation;
    }
}
