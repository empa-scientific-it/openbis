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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.wizard;

/**
 * Basic interface to the data model behind a wizard.
 *
 * @author Franz-Josef Elmer
 */
public interface IWizardDataModel
{
    /**
     * Returns the workflow model.
     */
    public WizardWorkflowModel getWorkflow();

    /**
     * Determines at workflow branches the next state based on the specified current state.
     */
    public IWizardState determineNextState(IWizardState currentState);

    /**
     * Finishes the wizard, submit/commit data and return a success message for the user.
     */
    public String finish();
}
