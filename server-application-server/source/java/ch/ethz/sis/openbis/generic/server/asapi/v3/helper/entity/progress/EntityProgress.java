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

import ch.ethz.sis.openbis.generic.server.asapi.v3.context.Progress;
import ch.ethz.sis.openbis.generic.server.asapi.v3.context.ProgressDetails;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentityHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IPermIdHolder;

/**
 * @author pkupczyk
 */
public class EntityProgress extends Progress
{

    private static final long serialVersionUID = 1L;

    private IIdHolder entity;

    public EntityProgress(String label, IIdHolder entity, int numItemsProcessed, int totalItemsToProcess)
    {
        super(label, numItemsProcessed, totalItemsToProcess);
        this.entity = entity;
    }

    @Override
    protected void updateDetails(ProgressDetails details)
    {
        if (entity != null)
        {
            final ProgressDetails entityDetails = new ProgressDetails();
            entityDetails.set("class", entity.getClass().getSimpleName());

            // the entity might be in an inconsistent/incomplete state therefore we allow failures when retrieving the data

            setWithFailureAllowed(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        entityDetails.set("id", entity.getId());
                    }
                });

            setWithFailureAllowed(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        if (entity instanceof IPermIdHolder)
                        {
                            entityDetails.set("permId", ((IPermIdHolder) entity).getPermId());
                        }
                    }
                });

            setWithFailureAllowed(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        if (entity instanceof IIdentityHolder)
                        {
                            entityDetails.set("identifier", ((IIdentityHolder) entity).getIdentifier());
                        }
                    }
                });

            details.set("entity", entityDetails);
        }
    }

    private void setWithFailureAllowed(IDelegatedAction setAction)
    {
        try
        {
            setAction.execute();
        } catch (Exception e)
        {

        } catch (AssertionError e)
        {

        }
    }

}
