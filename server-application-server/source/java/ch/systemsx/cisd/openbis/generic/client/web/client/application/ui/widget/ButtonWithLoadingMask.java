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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.ui.Image;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * A nice button which has a loading mask.
 * 
 * @author Christian Ribeaud
 */
public abstract class ButtonWithLoadingMask extends LayoutContainer
{
    private static final String LOADING_IMAGE_URL = "images/loading.gif";

    private final Button button;

    private final Image loadingImage;

    public ButtonWithLoadingMask(final String buttonLabel, final String buttonIdOrNull)
    {
        this(buttonLabel, buttonIdOrNull, Direction.RIGHT);
    }

    /**
     * @param loadingMaskDirection on which direction you would like the loading mask picture. Only {@link Direction#LEFT} or {@link Direction#RIGHT}
     *            are supported.
     */
    public ButtonWithLoadingMask(final String buttonLabel, final String buttonIdOrNull,
            final Direction loadingMaskDirection)
    {
        super(createLayout());
        assertDirection(loadingMaskDirection);
        button = createButton(buttonLabel, buttonIdOrNull, loadingMaskDirection);
        loadingImage = createLoadingImage();
        if (loadingMaskDirection == Direction.RIGHT)
        {
            add(button);
            add(loadingImage);
        } else
        {
            add(loadingImage);
            add(button);
        }
        layout();
    }

    private final void assertDirection(final Direction direction)
    {
        if (direction == Direction.LEFT || direction == Direction.RIGHT)
        {
            return;
        }
        throw new IllegalArgumentException("Only LEFT or RIGHT are supported.");
    }

    private final static TableRowLayout createLayout()
    {
        final TableRowLayout tableRowLayout = new TableRowLayout();
        tableRowLayout.setBorder(0);
        tableRowLayout.setCellPadding(0);
        tableRowLayout.setCellSpacing(0);
        return tableRowLayout;
    }

    private final Button createButton(final String buttonLabel, final String buttonIdOrNull,
            final Direction direction)
    {
        assert buttonLabel != null : "Unspecified button label.";
        final Button but = new Button(buttonLabel);
        if (StringUtils.isBlank(buttonIdOrNull) == false)
        {
            but.setId(buttonIdOrNull);
        }
        but.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                //
                // SelectionListener
                //

                @Override
                public final void componentSelected(final ButtonEvent ce)
                {
                    doButtonClick();
                }

            });
        final String margin = "3px";
        final String attributeName;
        if (direction == Direction.LEFT)
        {
            attributeName = "marginLeft";
        } else
        {
            attributeName = "marginRight";
        }
        but.setStyleAttribute(attributeName, margin);
        return but;
    }

    private final static Image createLoadingImage()
    {
        final Image image = new Image(LOADING_IMAGE_URL);
        image.setSize("19px", "19px");
        image.setVisible(false);
        return image;
    }

    /**
     * Triggers some action when the button gets clicked.
     */
    public abstract void doButtonClick();

    /**
     * Returns the {@link Button}.
     */
    public final Button getButton()
    {
        return button;
    }

    //
    // LayoutContainer
    //

    @Override
    public final void setEnabled(final boolean enabled)
    {
        button.setEnabled(enabled);
        loadingImage.setVisible(enabled == false);
    }
}
