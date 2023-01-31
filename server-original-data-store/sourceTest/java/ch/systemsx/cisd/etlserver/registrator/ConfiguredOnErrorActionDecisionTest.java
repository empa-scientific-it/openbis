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

package ch.systemsx.cisd.etlserver.registrator;

import java.util.Properties;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;
import ch.systemsx.cisd.etlserver.registrator.v1.ConfiguredOnErrorActionDecision;
import ch.systemsx.cisd.etlserver.registrator.v1.IDataSetOnErrorActionDecision.ErrorType;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ConfiguredOnErrorActionDecisionTest extends AssertJUnit
{

    @Test
    public void testCompletePropertiesFile()
    {
        Properties props = new Properties();
        props.put(ConfiguredOnErrorActionDecision.INVALID_DATA_SET_KEY, "DELETE");
        props.put(ConfiguredOnErrorActionDecision.OPENBIS_REGISTRATION_FAILURE_KEY, "delete");
        props.put(ConfiguredOnErrorActionDecision.REGISTRATION_SCRIPT_ERROR_KEY, "dElete");
        props.put(ConfiguredOnErrorActionDecision.STORAGE_PROCESSOR_ERROR_KEY, "delete");
        props.put(ConfiguredOnErrorActionDecision.VALIDATION_SCRIPT_ERROR_KEY, "delete");
        props.put(ConfiguredOnErrorActionDecision.POST_REGISTRATION_ERROR_KEY, "delete");
        ConfiguredOnErrorActionDecision decision = new ConfiguredOnErrorActionDecision(props);
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.INVALID_DATA_SET, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.OPENBIS_REGISTRATION_FAILURE, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.REGISTRATION_SCRIPT_ERROR, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.STORAGE_PROCESSOR_ERROR, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.VALIDATION_SCRIPT_ERROR, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.POST_REGISTRATION_ERROR, null));

    }

    @Test
    public void testIncompletePropertiesFile()
    {
        Properties props = new Properties();
        props.put(ConfiguredOnErrorActionDecision.OPENBIS_REGISTRATION_FAILURE_KEY, "move_to_error");
        props.put(ConfiguredOnErrorActionDecision.STORAGE_PROCESSOR_ERROR_KEY, "delete");
        props.put(ConfiguredOnErrorActionDecision.VALIDATION_SCRIPT_ERROR_KEY, "some junk");
        ConfiguredOnErrorActionDecision decision = new ConfiguredOnErrorActionDecision(props);
        assertEquals(UnstoreDataAction.LEAVE_UNTOUCHED,
                decision.computeUndoAction(ErrorType.INVALID_DATA_SET, null));
        assertEquals(UnstoreDataAction.MOVE_TO_ERROR,
                decision.computeUndoAction(ErrorType.OPENBIS_REGISTRATION_FAILURE, null));
        assertEquals(UnstoreDataAction.LEAVE_UNTOUCHED,
                decision.computeUndoAction(ErrorType.REGISTRATION_SCRIPT_ERROR, null));
        assertEquals(UnstoreDataAction.DELETE,
                decision.computeUndoAction(ErrorType.STORAGE_PROCESSOR_ERROR, null));
        assertEquals(UnstoreDataAction.LEAVE_UNTOUCHED,
                decision.computeUndoAction(ErrorType.VALIDATION_SCRIPT_ERROR, null));

    }
}
