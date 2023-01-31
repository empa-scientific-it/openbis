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

package ch.systemsx.cisd.etlserver.registrator.v1;

import java.util.Properties;

import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.etlserver.IStorageProcessorTransactional.UnstoreDataAction;

/**
 * Returns the undo store action as configured by a properties file, defaulting those that are not configured.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ConfiguredOnErrorActionDecision implements IDataSetOnErrorActionDecision
{

    public static final String INVALID_DATA_SET_KEY = "invalid-data-set";

    public static final String OPENBIS_REGISTRATION_FAILURE_KEY = "registration-error";

    public static final String REGISTRATION_SCRIPT_ERROR_KEY = "registration-script-error";

    public static final String VALIDATION_SCRIPT_ERROR_KEY = "validation-script-error";

    public static final String STORAGE_PROCESSOR_ERROR_KEY = "storage-processor-error";

    public static final String POST_REGISTRATION_ERROR_KEY = "post-registration-error";

    public static final String PRE_REGISTRATION_ERROR_KEY = "pre-registration-error";

    private final UnstoreDataAction invalidDataSetAction;

    private final UnstoreDataAction validationScriptError;

    private final UnstoreDataAction openbisRegistrationFailure;

    private final UnstoreDataAction registrationScriptError;

    private final UnstoreDataAction storageProcessorError;

    private final UnstoreDataAction postRegistrationError;

    private final UnstoreDataAction preRegistrationError;

    public ConfiguredOnErrorActionDecision(Properties properties)
    {
        invalidDataSetAction = getAction(ErrorType.INVALID_DATA_SET, properties);
        validationScriptError = getAction(ErrorType.VALIDATION_SCRIPT_ERROR, properties);
        openbisRegistrationFailure = getAction(ErrorType.OPENBIS_REGISTRATION_FAILURE, properties);
        registrationScriptError = getAction(ErrorType.REGISTRATION_SCRIPT_ERROR, properties);
        storageProcessorError = getAction(ErrorType.STORAGE_PROCESSOR_ERROR, properties);
        postRegistrationError = getAction(ErrorType.POST_REGISTRATION_ERROR, properties);
        preRegistrationError = getAction(ErrorType.PRE_REGISTRATION_ERROR, properties);
    }

    @Override
    public UnstoreDataAction computeUndoAction(ErrorType errorType, Throwable failureOrNull)
    {
        UnstoreDataAction action = null;
        switch (errorType)
        {
            case INVALID_DATA_SET:
                action = invalidDataSetAction;
                break;
            case OPENBIS_REGISTRATION_FAILURE:
                action = openbisRegistrationFailure;
                break;
            case REGISTRATION_SCRIPT_ERROR:
                action = registrationScriptError;
                break;
            case STORAGE_PROCESSOR_ERROR:
                action = storageProcessorError;
                break;
            case VALIDATION_SCRIPT_ERROR:
                action = validationScriptError;
                break;
            case POST_REGISTRATION_ERROR:
                action = postRegistrationError;
                break;
            case PRE_REGISTRATION_ERROR:
                action = preRegistrationError;
                break;
        }

        return action;
    }

    protected UnstoreDataAction getAction(ErrorType errorType, Properties properties)
    {
        String actionStringKey = null;
        switch (errorType)
        {
            case INVALID_DATA_SET:
                actionStringKey = INVALID_DATA_SET_KEY;
                break;
            case OPENBIS_REGISTRATION_FAILURE:
                actionStringKey = OPENBIS_REGISTRATION_FAILURE_KEY;
                break;
            case REGISTRATION_SCRIPT_ERROR:
                actionStringKey = REGISTRATION_SCRIPT_ERROR_KEY;
                break;
            case STORAGE_PROCESSOR_ERROR:
                actionStringKey = STORAGE_PROCESSOR_ERROR_KEY;
                break;
            case VALIDATION_SCRIPT_ERROR:
                actionStringKey = VALIDATION_SCRIPT_ERROR_KEY;
                break;
            case POST_REGISTRATION_ERROR:
                actionStringKey = POST_REGISTRATION_ERROR_KEY;
                break;
            case PRE_REGISTRATION_ERROR:
                actionStringKey = PRE_REGISTRATION_ERROR_KEY;
                break;
        }

        String actionString = PropertyUtils.getProperty(properties, actionStringKey);
        if (null == actionString)
        {
            return getDefaultAction(errorType);
        }

        UnstoreDataAction action;
        actionString = actionString.toUpperCase();
        try
        {
            action = UnstoreDataAction.valueOf(actionString);
        } catch (IllegalArgumentException ex)
        {
            action = getDefaultAction(errorType);
        }
        return action;
    }

    protected UnstoreDataAction getDefaultAction(ErrorType errorType)
    {
        UnstoreDataAction action = null;
        switch (errorType)
        {
            case INVALID_DATA_SET:
            case OPENBIS_REGISTRATION_FAILURE:
            case REGISTRATION_SCRIPT_ERROR:
            case STORAGE_PROCESSOR_ERROR:
            case VALIDATION_SCRIPT_ERROR:
            case POST_REGISTRATION_ERROR:
            case PRE_REGISTRATION_ERROR:
                action = UnstoreDataAction.LEAVE_UNTOUCHED;
                break;
        }

        return action;
    }
}
