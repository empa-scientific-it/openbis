/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.helper.entity.progress;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.create.ICreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.IUpdate;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.ProgressDetails;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
public class CheckAccessProgress extends EntityProgress
{

    private static final long serialVersionUID = 1L;

    private ICreation creation;

    private IUpdate update;

    public CheckAccessProgress(IIdHolder entity, ICreation creation, int numItemsProcessed, int totalItemsToProcess)
    {
        this(entity, numItemsProcessed, totalItemsToProcess);
        this.creation = creation;
    }

    public CheckAccessProgress(IIdHolder entity, IUpdate update, int numItemsProcessed, int totalItemsToProcess)
    {
        this(entity, numItemsProcessed, totalItemsToProcess);
        this.update = update;
    }

    private CheckAccessProgress(IIdHolder entity, int numItemsProcessed, int totalItemsToProcess)
    {
        super("checking access", entity, numItemsProcessed, totalItemsToProcess);
    }

    @Override
    protected void updateDetails(ProgressDetails details)
    {
        super.updateDetails(details);
        details.set("creation", creation);
        details.set("update", update);
    }

}
