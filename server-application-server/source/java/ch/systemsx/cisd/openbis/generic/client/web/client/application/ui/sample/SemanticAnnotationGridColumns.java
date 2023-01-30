/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ACCESSION_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ONTOLOGY_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.DESCRIPTOR_ONTOLOGY_VERSION;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.INHERITED;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PERM_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ACCESSION_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ONTOLOGY_ID;
import static ch.systemsx.cisd.openbis.generic.client.web.client.dto.SemanticAnnotationGridColumnIDs.PREDICATE_ONTOLOGY_VERSION;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.MultilineStringCellRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;

public class SemanticAnnotationGridColumns
{

    public void setRenderers(ColumnDefsAndConfigs<?> schema)
    {
        schema.setGridCellRendererFor(PERM_ID, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(PREDICATE_ONTOLOGY_ID, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(PREDICATE_ONTOLOGY_VERSION, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(PREDICATE_ACCESSION_ID, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(DESCRIPTOR_ONTOLOGY_ID, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(DESCRIPTOR_ONTOLOGY_VERSION, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
        schema.setGridCellRendererFor(DESCRIPTOR_ACCESSION_ID, new SemanticAnnotationCellDecorator(new MultilineStringCellRenderer()));
    }

    private class SemanticAnnotationCellDecorator implements GridCellRenderer<BaseEntityModel<?>>
    {

        private GridCellRenderer<BaseEntityModel<?>> renderer;

        public SemanticAnnotationCellDecorator(GridCellRenderer<BaseEntityModel<?>> renderer)
        {
            this.renderer = renderer;
        }

        @Override
        public Object render(BaseEntityModel<?> model, String property, ColumnData config, int rowIndex, int colIndex,
                ListStore<BaseEntityModel<?>> store, Grid<BaseEntityModel<?>> grid)
        {
            Object rendered = renderer.render(model, property, config, rowIndex, colIndex, store, grid);

            if (rendered instanceof String)
            {
                String inherited = model.get(INHERITED);

                if (inherited != null && inherited.equals("yes"))
                {
                    return "<div class=\"semanticAnnotationInherited\">" + rendered + "</div>";
                }
            }

            return rendered;
        }

    }

}
