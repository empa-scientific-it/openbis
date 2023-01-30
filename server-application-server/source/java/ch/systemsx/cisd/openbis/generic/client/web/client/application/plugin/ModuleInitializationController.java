/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * This class controls initialization process of all modules and notifies the {@link IModuleInitializationObserver}s when all initialization is
 * finished.
 * 
 * @author Piotr Buczek
 */
public class ModuleInitializationController
{
    private static ModuleInitializationController INSTANCE;

    /**
     * Cleans up the controller instance which is kept as a singleton on the client. Should be called after logout, otherwise module initialization
     * might not be performed on the server after server restart and modules will stop working (see LMS-2154) .
     */
    public static void cleanup()
    {
        INSTANCE = null;
    }

    public static ModuleInitializationController createAndInitialize(List<IModule> modules)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new ModuleInitializationController(modules);
            for (IModule module : modules)
            {
                module.initialize(new ModuleInitializationCallback(INSTANCE, module));
            }
        }
        return INSTANCE;
    }

    public void addObserver(IModuleInitializationObserver observer)
    {
        if (remainingModulesCounter != 0)
        {
            observers.add(observer);
        } else
        {
            observer.notify(successfullyInitializedModules);
        }
    }

    private int remainingModulesCounter;

    private final List<IModule> successfullyInitializedModules = new ArrayList<IModule>();

    private final List<IModule> uninitializedModules = new ArrayList<IModule>();

    private final List<IModuleInitializationObserver> observers;

    private ModuleInitializationController(List<IModule> allModules)
    {
        this.observers = new ArrayList<IModuleInitializationObserver>();
        successfullyInitializedModules.addAll(allModules);
        remainingModulesCounter = allModules.size();
    }

    private void onInitializationFailure(Throwable caught, IModule module)
    {
        successfullyInitializedModules.remove(module);
        uninitializedModules.add(module);
        onModuleInitializationComplete();
    }

    private void onInitializationSuccess(IModule module)
    {
        onModuleInitializationComplete();
    }

    private void onModuleInitializationComplete()
    {
        remainingModulesCounter--;
        if (remainingModulesCounter == 0)
        {
            for (IModuleInitializationObserver observer : observers)
            {
                observer.notify(successfullyInitializedModules);
            }
            showErrorMessageIfNecessary();
        }
    }

    private void showErrorMessageIfNecessary()
    {
        if (uninitializedModules.size() == 0)
        {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Initialization of these utilities failed: ");
        for (IModule module : uninitializedModules)
        {
            sb.append(module.getName() + ", ");
        }
        sb.setLength(sb.length() - 2);

        GWTUtils.alert("Error", sb.toString());
    }

    private static class ModuleInitializationCallback implements AsyncCallback<Void>
    {

        private final ModuleInitializationController controller;

        private final IModule module;

        public ModuleInitializationCallback(ModuleInitializationController controller,
                IModule module)
        {
            this.controller = controller;
            this.module = module;
        }

        @Override
        public void onFailure(Throwable caught)
        {
            controller.onInitializationFailure(caught, module);
        }

        @Override
        public void onSuccess(Void result)
        {
            controller.onInitializationSuccess(module);
        }

    }

}
