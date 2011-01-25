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

import java.util.Set;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property.ManagedPropertyGridGeneratedCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.managed_property.ManagedPropertyGridGeneratedCallback.IOnGridComponentGeneratedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.lang.StringEscapeUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableModelReference;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IManagedPropertyGridInformationProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ManagedTableWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedOutputWidgetDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.ManagedOutputWidgetType;

/**
 * {@link TabContent} handled by managed property script.
 * 
 * @author Piotr Buczek
 */
public class ManagedPropertySection extends DisposableTabContent
{
    private static final IDisposableComponent DUMMY_CONTENT = new IDisposableComponent()
        {

            public void update(Set<DatabaseModificationKind> observedModifications)
            {
            }

            public DatabaseModificationKind[] getRelevantModifications()
            {
                return DatabaseModificationKind.EMPTY_ARRAY;
            }

            public Component getComponent()
            {
                return new ContentPanel();
            }

            public void dispose()
            {
            }

        };

    private static String ID_PREFIX = "managed_property_section_";

    private final String gridIdSuffix;

    private final IDelegatedAction refreshAction;

    private final IManagedProperty managedProperty;

    private final IEntityInformationHolder entity;

    public ManagedPropertySection(final String header, IViewContext<?> viewContext,
            IEntityInformationHolder entity, IManagedProperty managedProperty,
            IDelegatedAction refreshAction)
    {
        super(header, viewContext, entity);
        this.entity = entity;
        this.managedProperty = managedProperty;
        this.gridIdSuffix = Format.hyphenize(header);
        this.refreshAction = refreshAction;
        setIds(new IDisplayTypeIDGenerator()
            {

                public String createID(String suffix)
                {
                    return createID() + suffix;
                }

                public String createID()
                {
                    return ID_PREFIX + gridIdSuffix;
                }
            });
    }

    @Override
    protected IDisposableComponent createDisposableContent()
    {
        try
        {
            final ManagedTableWidgetDescription tableDescription = getTableDescription();
            final IManagedPropertyGridInformationProvider gridInfo =
                    new IManagedPropertyGridInformationProvider()
                        {
                            public String getKey()
                            {
                                return gridIdSuffix;
                            }
                        };
            // refresh reloads the table and replaces tab component
            final IOnGridComponentGeneratedAction gridGeneratedAction =
                    new IOnGridComponentGeneratedAction()
                        {

                            public void execute(IDisposableComponent gridComponent)
                            {
                                replaceContent(gridComponent);
                            }

                        };

            AsyncCallback<TableModelReference> callback =
                    ManagedPropertyGridGeneratedCallback.create(viewContext.getCommonViewContext(),
                            entity, managedProperty, gridInfo, gridGeneratedAction, refreshAction);
            viewContext.getCommonService().createReportForManagedProperty(tableDescription,
                    callback);
            return null;
        } catch (UserFailureException ex)
        {
            final String basicMsg = ex.getMessage();
            final String detailedMsg = ex.getDetails();
            if (detailedMsg != null)
            {
                GWTUtils.createErrorMessageWithDetailsDialog(viewContext, basicMsg, detailedMsg)
                        .show();
            } else
            {
                MessageBox.alert("Error", basicMsg, null);
            }
            return DUMMY_CONTENT;
        }
    }

    private ManagedTableWidgetDescription getTableDescription() throws UserFailureException
    {
        final IManagedUiDescription uiDescription = managedProperty.getUiDescription();
        if (uiDescription == null)
        {
            throwFailToCreateContentException("uiDescription was not set in IManagedProperty object");
            return null; // make eclipse happy
        } else
        {
            final String value = StringEscapeUtils.unescapeHtml(managedProperty.getValue());
            // if there is a script error than value will contain error message
            if (value.startsWith(BasicConstant.ERROR_PROPERTY_PREFIX)
                    && (value.equals(BasicConstant.MANAGED_PROPERTY_PLACEHOLDER_VALUE) == false))
            {
                final String errorMsg =
                        value.substring(BasicConstant.ERROR_PROPERTY_PREFIX.length());
                throwFailToCreateContentException(errorMsg);
            }

            final IManagedOutputWidgetDescription outputWidget =
                    uiDescription.getOutputWidgetDescription();
            if (outputWidget == null)
            {
                throwFailToCreateContentException("Output widget was not set in IManagedUiDescription object");
            } else if (outputWidget.getManagedWidgetType() != ManagedOutputWidgetType.TABLE)
            {
                throwFailToCreateContentException("IManagedOutputWidgetDescription is not of type ManagedOutputWidgetType.TABLE");
            } else if ((outputWidget instanceof ManagedTableWidgetDescription) == false)
            {
                throwFailToCreateContentException("IManagedOutputWidgetDescription should be a subclass of ManagedTableWidgetDescription");
            }
            return (ManagedTableWidgetDescription) uiDescription.getOutputWidgetDescription();
        }

    }

    private void throwFailToCreateContentException(String detailedErrorMsg)
            throws UserFailureException
    {
        throw new UserFailureException("Failed to create content for " + getHeading() + ".",
                detailedErrorMsg);
    }

}
