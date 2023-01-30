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

package ch.systemsx.cisd.openbis.generic.client.web.clientnonjs;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AbstractViewLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.AttachmentDownloadLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.MaterialLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.OpenViewAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.PermlinkLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ProjectLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.SearchLocatorResolver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;

/**
 * A test of the ViewLocatorHandlerRegistry functionality from the Java side. The JavaScript side is tested by
 * {@link ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistryTest} .
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class ViewLocatorResolverRegistryTest extends AssertJUnit
{

    private static class DummyViewLocatorHandler extends AbstractViewLocatorResolver
    {
        boolean wasCalled = false;

        public DummyViewLocatorHandler(String handledAction)
        {
            super(handledAction);
        }

        @Override
        public void resolve(ViewLocator locator)
        {
            wasCalled = true;
        }
    }

    private ViewLocatorResolverRegistry registry;

    private Mockery context;

    private IViewContext<ICommonClientServiceAsync> viewContext;

    private ICommonClientServiceAsync commonService;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        registry = new ViewLocatorResolverRegistry();

        context = new Mockery();
        viewContext = context.mock(IViewContext.class);
        commonService = context.mock(ICommonClientServiceAsync.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // The following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testBasicHandlerLookup()
    {
        // Simple test of the handler registry that doesn't require mocks
        DummyViewLocatorHandler dummyHandler1 = new DummyViewLocatorHandler("ACTION1");
        DummyViewLocatorHandler dummyHandler2 = new DummyViewLocatorHandler("ACTION2");

        registry.registerHandler(dummyHandler1);
        registry.registerHandler(dummyHandler2);

        ViewLocator locator = new ViewLocator("ACTION=ACTION2");

        registry.handleLocator(locator);

        assertFalse(dummyHandler1.wasCalled);
        assertTrue(dummyHandler2.wasCalled);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolvePermlinkLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).getEntityInformationHolder(with(EntityKind.SAMPLE),
                            with("20100104150239401-871"),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator = new ViewLocator("entity=SAMPLE&permId=20100104150239401-871");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @Test
    public void testResolveIncompleteLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        // State used by the two sub-tests
        ViewLocator locator;
        OpenViewAction action;

        // No permId supplied
        locator = new ViewLocator("entity=SAMPLE");
        action = new OpenViewAction(registry, locator);

        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with no permId should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        // No entity supplied
        locator = new ViewLocator("permId=20100104150239401-871");
        action = new OpenViewAction(registry, locator);

        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with no entity should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testResolveLocatorWithInvalidEntity()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        // State used by the two sub-tests
        ViewLocator locator;
        OpenViewAction action;

        // The entity is invalid
        locator = new ViewLocator("entity=JUNK&permId=20100104150239401-871");
        action = new OpenViewAction(registry, locator);

        try
        {
            action.openInitialTabUnderExceptionHandlerForTest();
            fail("A URL with invalid entity should result in an exception.");
        } catch (UserFailureException expected)
        {
            // Do nothing -- this should happen
        }

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveMaterialLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        final MaterialIdentifier identifier = new MaterialIdentifier("CODE", "TYPE");
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).getMaterialInformationHolder(with(identifier),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator =
                new ViewLocator("entity=MATERIAL&code=" + identifier.getCode() + "&type="
                        + identifier.getTypeCode());
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveProjectLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        final BasicProjectIdentifier identifier =
                new BasicProjectIdentifier("SPACE_CODE", "PROJECT_CODE");
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getService();
                    will(returnValue(commonService));

                    one(commonService).getProjectInfo(with(identifier),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator =
                new ViewLocator("entity=PROJECT&space=" + identifier.getSpaceCode() + "&code="
                        + identifier.getProjectCode());
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveDownloadAttachmentLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        final String SAMPLE_PERM_ID = "20100104150239401-871";
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getService();
                    will(returnValue(commonService));

                    one(commonService).getEntityInformationHolder(with(EntityKind.SAMPLE),
                            with(SAMPLE_PERM_ID),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator =
                new ViewLocator("action=DOWNLOAD_ATTACHMENT&entity=SAMPLE&permId=" + SAMPLE_PERM_ID
                        + "&file=fileName.txt&version=1");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveSearchLocator()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).listSamples(
                            with(Expectations.any(ListSampleDisplayCriteria.class)),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator = new ViewLocator("action=SEARCH&entity=SAMPLE&code=CL1");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testResolveSearchWithoutCode()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));

                    one(commonService).listSamples(
                            with(Expectations.any(ListSampleDisplayCriteria.class)),
                            with(Expectations.any(AbstractAsyncCallback.class)));
                }

            });

        ViewLocator locator = new ViewLocator("action=SEARCH&entity=SAMPLE");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        context.assertIsSatisfied();
    }

    @Test
    public void testOpenInitialSearchTabWithDataset()
    {
        initializeLocatorHandlerRegistry(registry);
        context.checking(new Expectations()
            {
                {
                    allowing(viewContext).getCommonService();
                    will(returnValue(commonService));
                }

            });

        ViewLocator locator =
                new ViewLocator("action=SEARCH&entity=DATA_SET&data_set_type=HCS_IMAGE");
        OpenViewAction action = new OpenViewAction(registry, locator);
        action.execute();

        // Can't test this any further because the request to open the search disappears into
        // GWT/GXT land.

        context.assertIsSatisfied();
    }

    // Helper method modeled after the implementation in Client
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry)
    {
        handlerRegistry.registerHandler(new MaterialLocatorResolver(viewContext));
        handlerRegistry.registerHandler(new ProjectLocatorResolver(viewContext));
        handlerRegistry.registerHandler(new AttachmentDownloadLocatorResolver(viewContext));
        handlerRegistry.registerHandler(new PermlinkLocatorResolver(viewContext));
        handlerRegistry.registerHandler(new SearchLocatorResolver(viewContext));
    }
}
