/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager.IDisplaySettingsUpdater;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.IProfilingTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.ProfilingTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.displaysettings.IDisplaySettingsUpdate;

/**
 * The <i>generic</i> {@link IViewContext} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonViewContext implements IViewContext<ICommonClientServiceAsync>
{

    /**
     * Holds static state of client that should be accessible from everywhere on the client.
     * 
     * @author Piotr Buczek
     */
    public final static class ClientStaticState
    {
        private static ViewMode viewMode;

        private static String pageTitleSuffix;

        public static void init(final String aPageTitleSuffix)
        {
            pageTitleSuffix = aPageTitleSuffix;
        }

        /**
         * @deprecated use {@link IViewContext#isSimpleOrEmbeddedMode()} instead where possible
         */
        @Deprecated
        public static boolean isSimpleMode()
        {
            return viewMode != ViewMode.NORMAL;
        }

        public static void setViewMode(ViewMode viewModeParam)
        {
            viewMode = viewModeParam;
        }

        public static String getPageTitleSuffix()
        {
            return pageTitleSuffix;
        }
    }

    private static final String TECHNOLOGY_NAME = "common";

    private final ICommonClientServiceAsync service;

    private final IGenericImageBundle imageBundle;

    private final GenericViewModel viewModel;

    private final IPageController pageController;

    private DisplaySettingsManager displaySettingsManager;

    private IClientPluginFactoryProvider clientPluginFactoryProvider;

    private final CompositeMessageProvider messageProvider;

    private final ViewLocatorResolverRegistry locatorHandlerRegistry;

    private final IProfilingTable profilingTable;

    private final boolean isDebuggingEnabled;

    CommonViewContext(final ICommonClientServiceAsync service,
            final IGenericImageBundle imageBundle, final IPageController pageController,
            boolean isLoggingEnabled, boolean isDebuggingEnabled, String basicPageTitle)
    {
        this.service = service;
        this.imageBundle = imageBundle;
        this.pageController = pageController;
        this.isDebuggingEnabled = isDebuggingEnabled;
        this.profilingTable = ProfilingTable.create(isLoggingEnabled);
        messageProvider = new CompositeMessageProvider();
        messageProvider.add(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME));
        viewModel = new GenericViewModel();
        locatorHandlerRegistry = new ViewLocatorResolverRegistry();
        ClientStaticState.init(basicPageTitle);
    }

    final void setClientPluginFactoryProvider(
            IClientPluginFactoryProvider clientPluginFactoryProvider)
    {
        this.clientPluginFactoryProvider = clientPluginFactoryProvider;
    }

    //
    // IViewContext
    //

    @Override
    public final ICommonClientServiceAsync getService()
    {
        return service;
    }

    @Override
    public String getTechnology()
    {
        return TECHNOLOGY_NAME;
    }

    @Override
    public String getPropertyOrNull(String key)
    {
        return AbstractPluginViewContext.getPropertyOrNull(this, key);
    }

    @Override
    public final GenericViewModel getModel()
    {
        return viewModel;
    }

    @Override
    public void initDisplaySettingsManager()
    {
        final DisplaySettings loggedUserDisplaySettings =
                viewModel.getSessionContext().getDisplaySettings();
        displaySettingsManager =
                createDisplaySettingsManager(loggedUserDisplaySettings, viewModel
                        .getApplicationInfo().getWebClientConfiguration());
    }

    @Override
    public DisplaySettingsManager getDisplaySettingsManager()
    {
        assert displaySettingsManager != null : "displaySettingsManager not initialized";
        return displaySettingsManager;
    }

    private DisplaySettingsManager createDisplaySettingsManager(
            final DisplaySettings displaySettings, WebClientConfiguration webClientConfigurationDTO)
    {
        IDisplaySettingsUpdater settingsUpdater = new IDisplaySettingsUpdater()
            {
                @Override
                public void execute()
                {
                    service.saveDisplaySettings(displaySettings, new VoidAsyncCallback<Void>(
                            CommonViewContext.this));
                }

                @Override
                public void execute(IDisplaySettingsUpdate update)
                {
                    service.updateDisplaySettings(update, new VoidAsyncCallback<Void>(
                            CommonViewContext.this));
                }
            };
        return new DisplaySettingsManager(displaySettings, settingsUpdater, this);
    }

    @Override
    public final IGenericImageBundle getImageBundle()
    {
        return imageBundle;
    }

    @Override
    public final IPageController getPageController()
    {
        return pageController;
    }

    @Override
    public final IClientPluginFactoryProvider getClientPluginFactoryProvider()
    {
        return clientPluginFactoryProvider;
    }

    @Override
    public final IViewContext<ICommonClientServiceAsync> getCommonViewContext()
    {
        return this;
    }

    /** @see IMessageProvider#containsKey(String) */
    @Override
    public boolean containsKey(String key)
    {
        return messageProvider.containsKey(key);
    }

    /** @see IMessageProvider#getMessage(String, Object...) */
    @Override
    public String getMessage(String key, Object... parameters)
    {
        return messageProvider.getMessage(key, parameters);
    }

    /** @see IMessageProvider#getName() */
    @Override
    public String getName()
    {
        return messageProvider.getName();
    }

    @Override
    public ICommonClientServiceAsync getCommonService()
    {
        return getService();
    }

    @Override
    public void addMessageSource(String messageSource)
    {
        messageProvider.add(new DictonaryBasedMessageProvider(messageSource));
    }

    @Override
    public ViewLocatorResolverRegistry getLocatorResolverRegistry()
    {
        return locatorHandlerRegistry;
    }

    // ----- delegation to profilingTable

    @Override
    public final void clearLog()
    {
        profilingTable.clearLog();
    }

    @Override
    public final List<String> getLoggedEvents()
    {
        return profilingTable.getLoggedEvents();
    }

    @Override
    public final int log(String description)
    {
        return profilingTable.log(description);
    }

    @Override
    public void log(int taskId, String description)
    {
        profilingTable.log(taskId, description);
    }

    @Override
    public final void logStop(int taskId)
    {
        profilingTable.logStop(taskId);
    }

    @Override
    public boolean isLoggingEnabled()
    {
        return profilingTable.isLoggingEnabled();
    }

    @Override
    public boolean isDebuggingEnabled()
    {
        return isDebuggingEnabled;
    }

    @Override
    public boolean isSimpleOrEmbeddedMode()
    {
        return ClientStaticState.isSimpleMode();
    }

}
