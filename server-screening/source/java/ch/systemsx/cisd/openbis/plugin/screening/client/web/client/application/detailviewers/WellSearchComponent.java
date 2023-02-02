package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.common.shared.basic.string.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.LinkRenderer.IURLProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.MultilineItemsField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCodesCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.MaterialSearchCriteria;

/**
 * Allows to specify search criteria for materials contained in wells. Used in experiment section panel or as a standalone module.
 * 
 * @author Tomasz Pylak
 */
public class WellSearchComponent extends TabContent
{
    public static final String ID_SUFFIX = "ExperimentPlateLocationsSection";

    private static final int TEXT_AREA_WIDTH = 500;

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final ExperimentSearchCriteria experimentSearchCriteria;

    private final MultilineItemsField materialListField;

    private final CheckBoxField exactMatchOnly;

    private final CheckBoxField showCombinedResults;

    private List<MaterialType> materialTypesOrNull;

    public static String getTabTitle(IViewContext<IScreeningClientServiceAsync> viewContext)
    {
        return viewContext.getMessage(Dict.EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION);
    }

    public WellSearchComponent(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            String materialListOrNull, Boolean isExactMatchOrNull, Boolean showCombinedResultsOrNull)
    {
        this(screeningViewContext, ExperimentSearchCriteria.createAllExperiments());

        if (!StringUtils.isBlank(materialListOrNull))
        {
            materialListField.setValue(materialListOrNull);
        }
        if (isExactMatchOrNull != null)
        {
            exactMatchOnly.setValue(isExactMatchOrNull);
        }
        if (showCombinedResultsOrNull != null)
        {
            showCombinedResults.setValue(showCombinedResultsOrNull);
        }

        setContentVisible(true);
    }

    public static WellSearchComponent create(
            IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        assert experiment != null : "experiment is null";
        return new WellSearchComponent(screeningViewContext,
                getExperimentSearchCriteria(experiment));
    }

    private static ExperimentSearchCriteria getExperimentSearchCriteria(
            IEntityInformationHolderWithIdentifier experimentOrNull)
    {
        if (experimentOrNull == null)
        {
            return ExperimentSearchCriteria.createAllExperiments();
        }
        return ExperimentSearchCriteria.createExperiment(experimentOrNull);
    }

    public WellSearchComponent(IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        super(getTabTitle(screeningViewContext), screeningViewContext,
                tryGetExperimentId(experimentSearchCriteria));
        this.screeningViewContext = screeningViewContext;
        this.experimentSearchCriteria = experimentSearchCriteria;
        this.materialListField = createMaterialListArea();
        this.exactMatchOnly = createCheckBox(Dict.EXACT_MATCH_ONLY, true, screeningViewContext);
        this.showCombinedResults =
                createCheckBox(Dict.WELLS_SEARCH_SHOW_COMBINED_RESULTS,
                        ScreeningLinkExtractor.WELL_SEARCH_SHOW_COMBINED_RESULTS_DEFAULT,
                        screeningViewContext);

        setIds(DisplayTypeIDGenerator.EXPERIMENT_PLATE_LOCATIONS_SECTION);
        screeningViewContext.getCommonService().listMaterialTypes(
                new AbstractAsyncCallback<List<MaterialType>>(screeningViewContext)
                    {
                        @Override
                        protected void process(List<MaterialType> result)
                        {
                            materialTypesOrNull = result;
                        }
                    });
    }

    private static TechId tryGetExperimentId(ExperimentSearchCriteria experimentSearchCriteria)
    {
        return experimentSearchCriteria.tryGetExperiment() == null ? null
                : experimentSearchCriteria.tryGetExperiment().getExperimentId();
    }

    private static CheckBoxField createCheckBox(String labelDictKey, Boolean value,
            IMessageProvider messageProvider)
    {
        String label = messageProvider.getMessage(labelDictKey);
        CheckBoxField checkbox = new CheckBoxField(label, false);
        checkbox.setBoxLabel(label);
        checkbox.setValue(value);
        return checkbox;
    }

    private MultilineItemsField createMaterialListArea()
    {
        MultilineItemsField area = new MultilineItemsField("", true, 10);
        area.setWidth(TEXT_AREA_WIDTH);
        area.setEmptyText(screeningViewContext
                .getMessage(Dict.PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS));
        area.setLabelSeparator("");

        return area;
    }

    @Override
    protected void showContent()
    {
        LayoutContainer container = new LayoutContainer(new RowLayout());

        CheckBoxGroup checkBoxGroup = new CheckBoxGroup();
        checkBoxGroup.add(exactMatchOnly);
        checkBoxGroup.add(showCombinedResults);
        container.add(checkBoxGroup);

        container.add(materialListField);
        container.add(createSearchLink());

        container.setScrollMode(Scroll.AUTO);
        add(container, new MarginData(10));

    }

    private Widget createSearchLink()
    {
        Button searchButton =
                new Button(screeningViewContext.getMessage(ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.SEARCH_BUTTON));
        searchButton.setWidth(TEXT_AREA_WIDTH);
        IDelegatedAction normalModeAction = new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    showPlateMaterialReviewer();
                }
            };
        IURLProvider urlProvider = new IURLProvider()
            {
                @Override
                public String tryGetURL()
                {
                    MaterialSearchCodesCriteria materialCriteria = tryGetMaterialSearchCriteria();
                    if (materialCriteria == null)
                    {
                        return null;
                    }
                    return ScreeningLinkExtractor.createWellsSearchLink(experimentSearchCriteria,
                            materialCriteria, showCombinedResults.getValue());
                }
            };
        return LinkRenderer.createButtonLink(searchButton, normalModeAction, urlProvider);
    }

    private void showPlateMaterialReviewer()
    {
        MaterialSearchCodesCriteria materialCriteria = tryGetMaterialSearchCriteria();
        if (materialCriteria == null)
        {
            return;
        }
        WellSearchGrid.openTab(screeningViewContext, experimentSearchCriteria,
                MaterialSearchCriteria.create(materialCriteria),
                AnalysisProcedureCriteria.createNoProcedures(), showCombinedResults.getValue(),
                null);
    }

    private MaterialSearchCodesCriteria tryGetMaterialSearchCriteria()
    {
        String[] materialItemList = materialListField.tryGetModifiedItemList();
        if (materialItemList == null || materialItemList.length == 0)
        {
            return null;
        }
        // TODO 2010-07-23, Tomasz Pylak: allow user to choose the types
        // Now we search using all types available.
        if (materialTypesOrNull == null)
        {
            return null;
        }
        String[] materialTypeCodes = Code.extractCodesToArray(materialTypesOrNull);
        return new MaterialSearchCodesCriteria(materialItemList, materialTypeCodes,
                exactMatchOnly.getValue());
    }
}