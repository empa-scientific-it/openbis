/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.deletion;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/**
 * @author pkupczyk
 */
public class DeletionForceCheckBox extends Composite
{

    private Label label;

    private CheckBox checkBox;

    public DeletionForceCheckBox()
    {
        label = new Label();
        checkBox = new CheckBox();

        HorizontalPanel panel = new HorizontalPanel();
        panel.add(label);
        panel.add(checkBox);
        initWidget(panel);
    }

    public void setText(String text)
    {
        label.setText(text);

    }

    public void setTooltip(String tooltip)
    {
        label.setToolTip(tooltip);
        checkBox.setToolTip(tooltip);
    }

    public CheckBox getCheckBox()
    {
        return checkBox;
    }

    public boolean getValue()
    {
        Boolean value = checkBox.getValue();
        return value != null ? value.booleanValue() : false;
    }

    public void setValue(boolean value)
    {
        checkBox.setValue(value);
    }

}
