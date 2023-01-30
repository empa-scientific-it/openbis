package ch.systemsx.cisd.openbis.uitest.page;

import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.widget.Button;
import ch.systemsx.cisd.openbis.uitest.widget.Checkbox;

public class UserSettingsDialog
{

    @Locate("openbis_change-user-settings-dialog-group-field-legacyUI-field")
    private Checkbox legacyUi;

    @Locate("openbis_dialog-save-button")
    private Button save;

    public void setLegacyUi()
    {
        legacyUi.set(true);
    }

    public void save()
    {
        save.click();
    }
}
