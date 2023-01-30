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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * The {@link CodeField} extension "auto generate" option.
 * 
 * @author Izabela Adamczyk
 */
public class CodeFieldWithGenerator extends CodeField
{
    private final IViewContext<?> viewContext;

    private final String codePrefix;

    private final boolean autoGenerateCode;

    private final EntityKind entityKind;

    public CodeFieldWithGenerator(final IViewContext<?> viewContext, final String label,
            String codePrefix, EntityKind entityKind, boolean autoGenerateCode)
    {
        super(viewContext, label);
        this.viewContext = viewContext;
        this.codePrefix = codePrefix;
        this.entityKind = entityKind;
        this.autoGenerateCode = autoGenerateCode;
        setTriggerStyle("x-form-trigger-generate");
        setHideTrigger(false);
        GWTUtils.setToolTip(this, "Click the button to automatically generate the code");
    }

    @Override
    protected void onRender(Element target, int index)
    {
        super.onRender(target, index);
        if (autoGenerateCode)
        {
            generateCode();
        }
    }

    @Override
    public void setHideTrigger(boolean hideTrigger)
    {
        super.setHideTrigger(hideTrigger);
        if (hideTrigger == true)
        {
            GWTUtils.setToolTip(this, "");
        }
    }

    @Override
    public void reset()
    {
        if (autoGenerateCode)
        {
            generateCode();
        } else
        {
            super.reset();
        }
    }

    @Override
    protected void onTriggerClick(ComponentEvent ce)
    {
        super.onTriggerClick(ce);
        generateCode();
    }

    /**
     * Automatically generates the code.
     */
    public void generateCode()
    {
        viewContext.getCommonService().generateCode(codePrefix, entityKind,
                new GenerateCodeCallback(viewContext));
    }

    private final class GenerateCodeCallback extends AbstractAsyncCallback<String>
    {

        GenerateCodeCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        @Override
        protected final void process(final String result)
        {
            setValue(result);
        }

    }

}
