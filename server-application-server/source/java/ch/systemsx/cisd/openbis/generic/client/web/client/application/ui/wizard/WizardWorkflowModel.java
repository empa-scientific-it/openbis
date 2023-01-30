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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.gwt.core.client.JavaScriptException;

/**
 * Model of wizard workflow.
 *
 * @author Franz-Josef Elmer
 */
public class WizardWorkflowModel
{
    private static final List<IWizardState> EMPTY_LIST = Arrays.<IWizardState> asList();

    private final List<IWizardStateChangeListener> changeListeners =
            new ArrayList<IWizardStateChangeListener>();

    private final Map<IWizardState, List<IWizardState>> transitionsToNext =
            new HashMap<IWizardState, List<IWizardState>>();

    private final Map<IWizardState, List<IWizardState>> transitionsToPrevious =
            new HashMap<IWizardState, List<IWizardState>>();

    private final Stack<IWizardState> visitedStates = new Stack<IWizardState>();

    private final IWizardDataModel dataModel;

    private IWizardState initialState;

    /**
     * Creates an instance for the specified data model. The data model is needed to make decision at workflow branches.
     */
    public WizardWorkflowModel(IWizardDataModel dataModel)
    {
        this.dataModel = dataModel;
    }

    void addStateChangeListener(IWizardStateChangeListener stateChangeListener)
    {
        changeListeners.add(stateChangeListener);
    }

    /**
     * Adds a transition from specified state to specified next state. For one state several transations can be added. In this branching case
     * {@link IWizardDataModel#determineNextState(IWizardState)} will be invoked during execution of the workflow in order to determine which
     * transition to follow.
     */
    public void addTransition(IWizardState state, IWizardState nextState)
    {
        List<IWizardState> nextStates = transitionsToNext.get(state);
        if (nextStates == null)
        {
            nextStates = new ArrayList<IWizardState>();
            transitionsToNext.put(state, nextStates);
        }
        nextStates.add(nextState);
        List<IWizardState> previousStates = transitionsToPrevious.get(nextState);
        if (previousStates == null)
        {
            previousStates = new ArrayList<IWizardState>();
            transitionsToPrevious.put(nextState, previousStates);
        }
        previousStates.add(state);
        if (initialState == null)
        {
            initialState = state;
        }
    }

    void nextState()
    {
        List<IWizardState> nextStates = getNextStates();
        if (nextStates.size() == 1)
        {
            setNextState(nextStates.get(0));
        } else if (nextStates.size() > 1)
        {
            IWizardState nextState = dataModel.determineNextState(tryGetCurrentState());
            if (nextStates.contains(nextState) == false)
            {
                throw new JavaScriptException("Error", "Next state " + nextState + " is not from "
                        + nextStates);
            }
            setNextState(nextState);
        } else
        {
            fireStateChangeEvent(tryGetCurrentState(), null);
        }
    }

    private void setNextState(IWizardState nextState)
    {
        IWizardState oldState = tryGetCurrentState();
        visitedStates.push(nextState);
        fireStateChangeEvent(oldState, nextState);
    }

    private void fireStateChangeEvent(IWizardState oldState, IWizardState nextState)
    {
        for (IWizardStateChangeListener listener : changeListeners)
        {
            listener.stateChanged(oldState, nextState);
        }
    }

    boolean hasNextState(IWizardState state)
    {
        return transitionsToNext.get(state) != null;
    }

    private List<IWizardState> getNextStates()
    {
        IWizardState currentState = tryGetCurrentState();
        if (currentState == null)
        {
            return initialState == null ? EMPTY_LIST : Arrays.asList(initialState);
        }
        List<IWizardState> nextStates = transitionsToNext.get(currentState);
        return nextStates == null ? EMPTY_LIST : nextStates;
    }

    void previousState()
    {
        if (visitedStates.isEmpty() == false)
        {
            IWizardState oldState = visitedStates.pop();
            fireStateChangeEvent(oldState, tryGetCurrentState());
        }
    }

    boolean hasPreviousState(IWizardState state)
    {
        return transitionsToPrevious.get(state) != null;
    }

    IWizardState tryGetCurrentState()
    {
        return visitedStates.isEmpty() ? null : visitedStates.peek();
    }
}
