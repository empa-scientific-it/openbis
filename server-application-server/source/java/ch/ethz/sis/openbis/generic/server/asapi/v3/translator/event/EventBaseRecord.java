/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.event;

import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.ObjectBaseRecord;

import java.util.Date;

/**
 * @author pkupczyk
 */
public class EventBaseRecord extends ObjectBaseRecord
{

    public String eventType;

    public String entityType;

    public String entitySpace;

    public String entitySpaceId;

    public String entityProject;

    public String entityProjectId;

    public String entityRegistrator;

    public Date entityRegistrationDate;

    public String identifier;

    public String description;

    public String reason;

    public String content;

    public Date registrationDate;

}
