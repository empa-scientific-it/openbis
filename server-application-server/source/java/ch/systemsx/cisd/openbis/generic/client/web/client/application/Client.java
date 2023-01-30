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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.CommonViewContext.ClientStaticState;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.LoginController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AttachmentDownloadLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.BrowserLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.CustomImportLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.GlobalSearchLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.HomeLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MetaprojectBrowserLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MetaprojectLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.OpenViewAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkEditingLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ProjectLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.SampleRegistrationLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.SearchLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action.LoginAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.DefaultClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ModuleInitializationController;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.BasicLoginCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.aggregation.AggregationServiceLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WindowUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.ViewMode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * The {@link EntryPoint} implementation.
 * 
 * @author Franz-Josef Elmer
 * @author Izabela Adamczyk
 */
public class Client implements EntryPoint, ValueChangeHandler<String>
{
    /** name of the URL parameter which decides if logging is switched on or off */
    private static final String LOGGING_PARAM = "log";

    /** value of the URL parameter {@link #LOGGING_PARAM} which switches logging on */
    private static final String LOGGING_ON = "true";

    /** name of the URL parameter which decides if debugging is switched on or off */
    private static final String DEBUGGING_PARAM = "debug";

    /** value of the URL parameter {@link #DEBUGGING_PARAM} which switches debugging on */
    private static final String DEBUGGING_ON = "true";

    private IViewContext<ICommonClientServiceAsync> viewContext;

    private final List<Controller> controllers = new ArrayList<Controller>();

    public final IViewContext<ICommonClientServiceAsync> tryToGetViewContext()
    {
        return viewContext;
    }

    private final IViewContext<ICommonClientServiceAsync> createViewContext()
    {
        final ICommonClientServiceAsync service = GWT.create(ICommonClientService.class);
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.COMMON_SERVER_NAME);
        final IGenericImageBundle imageBundle =
                GWT.<IGenericImageBundle> create(IGenericImageBundle.class);
        final IPageController pageController = new IPageController()
            {
                //
                // IPageController
                //
                @Override
                public final void reload(final boolean logout)
                {
                    if (logout)
                    {
                        // refresh the entire page
                        Window.Location.reload();
                    } else
                    {
                        onModuleLoad();
                    }
                }

            };

        CommonViewContext commonContext =
                new CommonViewContext(service, imageBundle, pageController, isLoggingEnabled(),
                        isDebuggingEnabled(), getPageTitle());
        commonContext.setClientPluginFactoryProvider(createPluginFactoryProvider(commonContext));
        initializeLocatorHandlerRegistry(commonContext.getLocatorResolverRegistry(), commonContext);
        return commonContext;
    }

    private String getPageTitle()
    {
        return Window.getTitle();
    }

    private String tryGetViewMode()
    {
        return Window.Location.getParameter(BasicConstant.VIEW_MODE_KEY);
    }

    private boolean isLoggingEnabled()
    {
        return GWTUtils.getParamString() != null
                && GWTUtils.getParamString().contains(LOGGING_PARAM + "=" + LOGGING_ON);
    }

    private boolean isDebuggingEnabled()
    {
        return GWTUtils.getParamString() != null
                && GWTUtils.getParamString().contains(DEBUGGING_PARAM + "=" + DEBUGGING_ON);
    }

    /**
     * Creates the provider for client plugin factories. Can be overridden in subclasses.
     */
    protected IClientPluginFactoryProvider createPluginFactoryProvider(
            IViewContext<ICommonClientServiceAsync> commonContext)
    {
        return new DefaultClientPluginFactoryProvider(commonContext);
    }

    private final void initializeControllers(Controller openUrlController)
    {
        removeControllers();
        addController(new LoginController(viewContext));
        addController(new AppController((CommonViewContext) viewContext));
        addController(openUrlController);
    }

    public void removeControllers()
    {
        final Dispatcher dispatcher = Dispatcher.get();
        for (Controller controller : controllers)
        {
            dispatcher.removeController(controller);
        }
        controllers.clear();
        ModuleInitializationController.cleanup();
    }

    private void addController(Controller controller)
    {
        final Dispatcher dispatcher = Dispatcher.get();
        dispatcher.addController(controller);
        controllers.add(controller);
    }

    @Override
    public final void onModuleLoad()
    {
        onModuleLoad(WindowUtils.createOpenUrlController());
    }

    // @Private - exposed for tests
    public final void onModuleLoad(Controller openUrlController)
    {
        if (viewContext == null)
        {
            viewContext = createViewContext();
            initializeControllers(openUrlController);
        }
        History.addValueChangeHandler(this); // both modes

        final IClientServiceAsync service = getServiceForRetrievingApplicationInfo(viewContext);
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {

                //
                // AbstractAsyncCallback
                //

                @Override
                public final void process(final ApplicationInfo info)
                {
                    ViewMode viewMode = findViewMode(info);
                    ClientStaticState.setViewMode(viewMode);
                    GenericViewModel model = viewContext.getModel();
                    model.setApplicationInfo(info);
                    model.setViewMode(viewMode);
                    boolean anonymous = isAnonymousLogin(info);
                    model.setAnonymousAllowed(anonymous);
                    // the callback sets the SessionContext and redirects to the login page or the
                    // initial page and may additionaly open an initial tab
                    OpenViewAction action = UrlParamsHelper.createNavigateToCurrentUrlAction(viewContext);
                    SessionContextCallback sessionContextCallback =
                            new SessionContextCallback((CommonViewContext) viewContext,
                                    action);
                    String sessionId = action.getViewLocator().getSessionId();
                    service.tryToGetCurrentSessionContext(anonymous, sessionId, sessionContextCallback);
                }

                private ViewMode findViewMode(ApplicationInfo info)
                {
                    // if view mode is specified in the URL it should override the default one
                    final ViewMode userViewModeOrNull = tryGetUrlViewMode();
                    final ViewMode viewMode =
                            userViewModeOrNull != null ? userViewModeOrNull : info
                                    .getWebClientConfiguration().getDefaultViewMode();
                    viewContext.log("viewMode = " + viewMode);
                    return viewMode;
                }

                private boolean isAnonymousLogin(ApplicationInfo info)
                {
                    String anonymousOrNull =
                            Window.Location.getParameter(BasicConstant.ANONYMOUS_KEY);
                    if (anonymousOrNull != null)
                    {
                        return anonymousOrNull.equalsIgnoreCase("yes")
                                || anonymousOrNull.equalsIgnoreCase("true");
                    }
                    return info.getWebClientConfiguration().isDefaultAnonymousLogin();
                }

                private ViewMode tryGetUrlViewMode()
                {
                    final String userViewMode = tryGetViewMode();
                    if (userViewMode != null)
                    {
                        try
                        {
                            return ViewMode.valueOf(userViewMode.toUpperCase());
                        } catch (IllegalArgumentException e)
                        {
                            // ignore mode in URL if it is incorrect (use default mode)
                        }
                    }
                    return null;
                }
            });
    }

    protected IClientServiceAsync getServiceForRetrievingApplicationInfo(
            IViewContext<ICommonClientServiceAsync> context)
    {
        return context.getService();
    }

    /**
     * A version of onModuleLoad specifically for tests. Ignore the state in the session and go directly to the login page.
     */
    public final void onModuleLoadTest()
    {
        Controller openUrlController = WindowUtils.createOpenUrlController();
        if (viewContext == null)
        {
            viewContext = createViewContext();
            initializeControllers(openUrlController);
        }

        final IClientServiceAsync service = viewContext.getService();
        service.getApplicationInfo(new AbstractAsyncCallback<ApplicationInfo>(viewContext)
            {

                //
                // AbstractAsyncCallback
                //

                @Override
                public final void process(final ApplicationInfo info)
                {
                    viewContext.getModel().setApplicationInfo(info);
                    final Dispatcher dispatcher = Dispatcher.get();
                    dispatcher.dispatch(AppEvents.LOGIN);
                }
            });
    }

    /**
     * Callback class which handles return value {@link ICommonClientService#tryToGetCurrentSessionContext()}.
     * 
     * @author Franz-Josef Elmer
     */
    private static final class SessionContextCallback extends AbstractAsyncCallback<SessionContext>
    {
        private final IDelegatedAction afterInitAction;

        /**
         * @param afterInitAction action executed after application init (if user is logged in)
         */
        SessionContextCallback(final CommonViewContext viewContext,
                final IDelegatedAction afterInitAction)
        {
            super(viewContext);
            this.afterInitAction = afterInitAction;

        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final SessionContext sessionContext)
        {
            final Dispatcher dispatcher = Dispatcher.get();
            if (sessionContext == null)
            {
                if (viewContext.getModel().isAnonymousAllowed())
                {
                    viewContext.getService().tryToLoginAnonymously(
                            new BasicLoginCallback(viewContext.getCommonViewContext(), null)
                                {
                                    @Override
                                    protected void handleMissingSession()
                                    {
                                        new LoginAction(viewContext).execute();
                                    }
                                });
                } else
                {
                    dispatcher.dispatch(AppEvents.LOGIN);
                }
            } else
            {
                viewContext.getModel().setSessionContext(sessionContext);
                // NOTE: Display settings manager needs to be reinitialized after login.
                // Otherwise if two users used the same browser one after another without server
                // restart than display settings of the user that logged in first would be used
                // also for the second user.
                viewContext.initDisplaySettingsManager();

                dispatcher.dispatch(AppEvents.INIT);

                if (viewContext.isSimpleOrEmbeddedMode() == false)
                {
                    GWTUtils.setConfirmExitMessage();
                }

                final String lastHistoryOrNull = tryGetLastHistoryToken();
                if (lastHistoryOrNull != null)
                {
                    ViewLocator lastHistoryLocator = new ViewLocator(lastHistoryOrNull);
                    viewContext.getLocatorResolverRegistry().locatorExists(lastHistoryLocator,
                            new AsyncCallback<Void>()
                                {

                                    @Override
                                    public void onSuccess(Void result)
                                    {
                                        History.newItem(lastHistoryOrNull);
                                    }

                                    @SuppressWarnings("deprecation")
                                    @Override
                                    public void onFailure(Throwable reason)
                                    {
                                        // Do not try to open the last history location
                                        // when it no longer exists. Clear it instead.
                                        viewContext.getModel().getSessionContext()
                                                .getDisplaySettings()
                                                .setLastHistoryTokenOrNull(null);
                                        viewContext.getDisplaySettingsManager().storeSettings();
                                    }
                                });
                } else
                {
                    afterInitAction.execute();
                }
            }
        }

        @SuppressWarnings("deprecation")
        private String tryGetLastHistoryToken()
        {
            if (viewContext.isSimpleOrEmbeddedMode() == false
                    && StringUtils.isBlank(History.getToken()))
            {
                DisplaySettings displaySettings =
                        viewContext.getModel().getSessionContext().getDisplaySettings();
                if (displaySettings.isIgnoreLastHistoryToken())
                {
                    return null;
                } else
                {
                    return displaySettings.getLastHistoryTokenOrNull();
                }
            }
            return null;
        }
    }

    /**
     * Register any handlers for locators specified in the openBIS URL.
     * 
     * @param handlerRegistry The handler registry to initialize
     * @param context The ViewContext which may be needed by resolvers. Can't use the viewContext instance variable since it may not have been
     *            initialized yet.
     */
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry,
            IViewContext<ICommonClientServiceAsync> context)
    {
        // It is important that MaterialLocatorResolver is registered before PermlinkLocatorResolver
        // as it handles the same action in a more specific way.
        handlerRegistry.registerHandler(new MaterialLocatorResolver(context));
        handlerRegistry.registerHandler(new ProjectLocatorResolver(context));
        handlerRegistry.registerHandler(new MetaprojectLocatorResolver(context));
        handlerRegistry.registerHandler(new AttachmentDownloadLocatorResolver(context));
        handlerRegistry.registerHandler(new PermlinkEditingLocatorResolver(context));
        handlerRegistry.registerHandler(new PermlinkLocatorResolver(context));

        handlerRegistry.registerHandler(new SearchLocatorResolver(context));
        handlerRegistry.registerHandler(new MetaprojectBrowserLocatorResolver(context));
        handlerRegistry.registerHandler(new BrowserLocatorResolver(context));
        handlerRegistry.registerHandler(new GlobalSearchLocatorResolver(context));
        handlerRegistry.registerHandler(new HomeLocatorResolver(context));
        handlerRegistry.registerHandler(new SampleRegistrationLocatorResolver(context));
        handlerRegistry.registerHandler(new AggregationServiceLocatorResolver(context));
        handlerRegistry.registerHandler(new CustomImportLocatorResolver(context));
    }

    @Override
    public void onValueChange(ValueChangeEvent<String> event)
    {
        UrlParamsHelper.createNavigateToCurrentUrlAction(viewContext).execute();
    }

}
