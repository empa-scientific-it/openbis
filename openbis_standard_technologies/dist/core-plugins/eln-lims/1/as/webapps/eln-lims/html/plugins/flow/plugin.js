function FlowCytometryTechnology() {
    this.init();
}

$.extend(FlowCytometryTechnology.prototype, ELNLIMSPlugin.prototype, {
    init: function () {

        // Store a reference to the "retrieve FCS events" service
        this.retrieveFCSEventsService = null;

        // Data cache
        this.dataCache = {};
    },
    forcedDisableRTF: [],
    forceMonospaceFont: [],
    sampleTypeDefinitionsExtension: {
        "CYTOFLEX_S_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "FACS_ARIA_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "INFLUX_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "LSR_FORTESSA_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "MOFLO_XDP_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "S3E_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "SONY_SH800S_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "SONY_MA900_EXPERIMENT": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        },
        "CYTOFLEX_S_PLATE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "CYTOFLEX_S_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "CYTOFLEX_S_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "CYTOFLEX_S_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "CYTOFLEX_S_WELL": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true,
            "SHOW_ON_NAV_FOR_PARENT_TYPES": ["CYTOFLEX_S_PLATE"]
        },
        "FACS_ARIA_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "FACS_ARIA_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "FACS_ARIA_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "INFLUX_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "INFLUX_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "INFLUX_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "LSR_FORTESSA_PLATE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "LSR_FORTESSA_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "LSR_FORTESSA_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "LSR_FORTESSA_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "LSR_FORTESSA_WELL": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true,
            "SHOW_ON_NAV_FOR_PARENT_TYPES": ["LSR_FORTESSA_PLATE"]
        },
        "MOFLO_XDP_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "MOFLO_XDP_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "MOFLO_XDP_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "S3E_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "S3E_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "S3E_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "SONY_SH800S_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "SONY_SH800S_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "SONY_SH800S_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "SONY_MA900_SPECIMEN": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        },
        "SONY_MA900_TUBE": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": true
        },
        "SONY_MA900_TUBESET": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SHOW_ON_NAV": false
        }
    },
    dataSetTypeDefinitionsExtension: {
        "CYTOFLEX_S_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "FACS_ARIA_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "INFLUX_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "LSR_FORTESSA_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MOFLO_XDP_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "S3E_ARIA_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "SONY_SH800S_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "SONY_MA900_FCSFILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        }
    },
    sampleFormTop: function ($container, model) {

    },
    sampleFormBottom: function ($container, model) {

    },
    dataSetFormTop: function ($container, model) {

        // Render the paremeter options
        this.renderParameterSelectionWidget($container, model);

        // Add a div for reporting status
        $container.append($('<div>')
            .css("margin-bottom", "5px")
            .attr("id", "status_div"));

        // Append the div where the data will be plotted
        $container.append($('<div>')
            .css("width", "580px")
            .attr("id", "plot_canvas_div"));

    },
    dataSetFormBottom: function ($container, model) {

    },

    // Additional functionality
    renderParameterSelectionWidget: function ($container, model) {

        // Check that we ave the correct dataset type
        if (!model.dataSetV3) {
            return;
        }

        if (!model.dataSetV3.type.code.endsWith("_FCSFILE")) {
            return;
        }

        // Clear the container
        $container.empty();

        //
        // Retrieve the parameter info
        //
        var parameterInfo = this.retrieveParameterInfo(model);

        // Add legend
        var legend = $("<legend>")
            .text("Data viewer");
        $container.append(legend);

        // Create a div for all plotting options
        var plot_params_div = $('<div>')
            .css("text-align", "left")
            .css("margin", "5px 0 15px 0")
            .attr("id", "plot_params_div");

        //
        // Lay out the widget
        //

        // Create a form for the plot parameters
        var form = $("<form>")
            .attr("id", "parameter_form");
        plot_params_div.append(form);

        // Create divs to spatially organize the groups of parameters
        var xAxisDiv = $("<div>")
            .css("display", "inline-block")
            .css("text-align", "right")
            .attr("id", "xAxisDiv");
        var yAxisDiv = $("<div>")
            .css("display", "inline-block")
            .css("text-align", "right")
            .attr("id", "yAxisDiv");
        var eventsDiv = $("<div>")
            .css("display", "inline-block")
            .css("text-align", "right")
            .attr("id", "eventsDiv");
        var plotDiv = $("<div>")
            .css("display", "inline-block")
            .css("vertical-align", "top")
            .css("padding-left", "10px")
            .attr("id", "plotDiv");

        // Add them to the form
        form.append(xAxisDiv);
        form.append(yAxisDiv);
        form.append(eventsDiv);
        form.append(plotDiv);

        // X axis parameters
        xAxisDiv.append($("<label>")
            .attr("for", "parameter_form_select_X_axis")
            .html("X axis"));
        var selectXAxis = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_X_axis");
        xAxisDiv.append(selectXAxis);

        // Y axis parameters
        yAxisDiv.append($("<label>")
            .attr("for", "parameter_form_select_Y_axis")
            .html("Y axis"));
        var selectYAxis = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_Y_axis");
        yAxisDiv.append(selectYAxis);

        // Add all options
        for (var i = 0; i < parameterInfo.numParameters; i++) {
            var name = parameterInfo["names"][i];
            var compositeName = parameterInfo["compositeNames"][i];
            selectXAxis.append($("<option>")
                .attr("value", name)
                .text(compositeName));
            selectYAxis.append($("<option>")
                .attr("value", name)
                .text(compositeName));
        }

        // // Pre-select some parameters
        selectXAxis.val(parameterInfo["names"][0]);
        selectYAxis.val(parameterInfo["names"][1]);

        // Add a selector with the number of events to plot
        eventsDiv.append($("<label>")
            .attr("for", "parameter_form_select_num_events")
            .html("Events to plot"));
        var selectNumEvents = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_num_events");
        eventsDiv.append(selectNumEvents);

        // Add the options
        var possibleOptions = [500, 1000, 2500, 5000, 10000, 20000, 50000, 100000];
        var numEventsInFile = parseInt(parameterInfo.numEvents);
        for (i = 0; i < possibleOptions.length; i++) {
            if (possibleOptions[i] < numEventsInFile) {
                selectNumEvents.append($("<option>")
                    .attr("value", possibleOptions[i])
                    .text(possibleOptions[i].toString()));
            }
        }
        selectNumEvents.append($("<option>")
            .attr("value", parameterInfo.numEvents)
            .text(parseInt(parameterInfo.numEvents)));

        // Pre-select something reasonable
        if (parameterInfo.numEvents > possibleOptions[4]) {
            selectNumEvents.val(possibleOptions[4]);
        } else {
            selectNumEvents.val(parameterInfo.numEvents);
        }

        // Add "Plot" button
        var thisObj = this;
        var plotButton = $("<input>")
            .attr("type", "button")
            .attr("value", "Plot")
            .click(function () {

                // Get the selected parameters and their display scaling
                var paramX = selectXAxis.find(":selected").val();
                var paramY = selectYAxis.find(":selected").val();
                var displayX = selectScaleX.find(":selected").val();
                var displayY = selectScaleY.find(":selected").val();

                // How many events to plot?
                var numEventsToPlot = selectNumEvents.val();

                // Sampling method
                var samplingMethod = selectSamplingMethod.find(":selected").val();

                // Call the retrieving and plotting method
                thisObj.callServerSidePluginGenerateFCSPlot(
                    model,
                    paramX,
                    paramY,
                    displayX,
                    displayY,
                    numEventsToPlot,
                    parameterInfo.numEvents,
                    samplingMethod);
            });
        plotDiv.append(plotButton);

        // Add a selector with the scaling for axis X
        var xAxisScalingDiv = xAxisDiv.append($("<div>")
            .css("display", "block")
            .attr("id", "xAxisScalingDiv"));
        xAxisScalingDiv.append($("<label>")
            .attr("for", "parameter_form_select_scaleX")
            .html("Scale for X axis"));
        var selectScaleX = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_scaleX");
        xAxisScalingDiv.append(selectScaleX);

        // Add the options
        possibleOptions = ["Linear", "Hyperlog"];
        for (i = 0; i < possibleOptions.length; i++) {
            selectScaleX.append($("<option>")
                .attr("name", possibleOptions[i])
                .attr("value", possibleOptions[i])
                .text(possibleOptions[i]));
        }

        // Pre-select "Linear"
        $("parameter_form_select_scaleX").val(0);

        // Add a selector with the scaling for axis Y
        var yAxisScalingDiv = yAxisDiv.append($("<div>")
            .css("display", "block")
            .attr("id", "yAxisScalingDiv"));
        yAxisScalingDiv.append($("<label>")
            .attr("for", "parameter_form_select_scaleY")
            .html("Scale for Y axis"));
        var selectScaleY = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_scaleY");
        yAxisScalingDiv.append(selectScaleY);

        // Add the options
        possibleOptions = ["Linear", "Hyperlog"];
        for (i = 0; i < possibleOptions.length; i++) {
            selectScaleY.append($("<option>")
                .attr("name", possibleOptions[i])
                .attr("value", possibleOptions[i])
                .text(possibleOptions[i]));
        }

        // Pre-select "Linear"
        $("parameter_form_select_scaleY").val(0);

        // Add a selector with the sampling method
        var eventSamplingDiv = eventsDiv.append($("<div>")
            .css("display", "block")
            .attr("id", "eventSamplingDiv"));
        eventSamplingDiv.append($("<label>")
            .attr("for", "parameter_form_select_sampling_method")
            .html("Sampling"));
        var selectSamplingMethod = $("<select>")
            .css("margin", "0 3px 0 3px")
            .attr("id", "parameter_form_select_sampling_method");
        eventSamplingDiv.append(selectSamplingMethod);

        // Add the options
        possibleOptions = ["Regular", "First rows"];
        for (i = 0; i < possibleOptions.length; i++) {
            selectSamplingMethod.append($("<option>")
                .attr("name", "" + (i + 1))
                .attr("value", (i + 1))
                .text(possibleOptions[i]));
        }

        // Pre-select "Linear"
        $("parameter_form_select_sampling_method").val(0);

        //
        // End of widget
        //

        // Append the created div to the container
        $container.append(plot_params_div);

    },

    retrieveParameterInfo: function (model) {

        // Retrieve parameter information
        var key = model.dataSetV3.type.code.substring(
            0, model.dataSetV3.type.code.indexOf("_FCSFILE")) +
            "_FCSFILE_PARAMETERS";

        var parametersXML = $.parseXML(model.dataSetV3.properties[key]);
        var parameters = parametersXML.childNodes[0];

        var numParameters = parameters.getAttribute("numParameters");
        var numEvents = parameters.getAttribute("numEvents");

        var names = [];
        var compositeNames = [];
        var display = [];

        // Parameter numbering starts at 1
        var parametersToDisplay = 0;
        for (var i = 1; i <= numParameters; i++) {

            // If the parameter contains the PnCHANNELTYPE attribute (BD Influx Cell Sorter),
            // we only add it if the channel type is 6.
            var channelType = parameters.getAttribute("P" + i + "CHANNELTYPE");
            if (channelType != null && channelType !== 6) {
                continue;
            }

            // Store the parameter name
            var name = parameters.getAttribute("P" + i + "N");
            names.push(name);

            // Store the composite name
            var pStr = parameters.getAttribute("P" + i + "S");
            var composite = name;
            if (pStr !== "") {
                composite = name + " (" + pStr + ")";
            }
            compositeNames.push(composite);

            // Store the display scale
            var displ = parameters.getAttribute("P" + i + "DISPLAY");
            display.push(displ);

            // Update the count of parameters to display
            parametersToDisplay++;
        }

        // Store the parameter info
        parameterInfo = {
            "numParameters": parametersToDisplay,
            "numEvents": numEvents,
            "names": names,
            "compositeNames": compositeNames,
            "display": display
        };

        // Return it
        return parameterInfo;
    },

    callServerSidePluginGenerateFCSPlot: function (model, paramX, paramY, displayX, displayY, numEventsToPlot, totalNumEvents, samplingMethod) {

        // Check whether the data for the plot is already cached
        var key = model.dataSetV3.code + "_" + paramX + "_" + paramY + "_" + numEventsToPlot.toString() +
            "_" + displayX + "_" + displayY + "_" + samplingMethod.toString();

        if (model.dataSetV3.code in this.dataCache &&
            key in this.dataCache[model.dataSetV3.code]) {

            // Plot the cached data
            this.plotFCSData(
                this.dataCache[model.dataSetV3.code][key],
                paramX,
                paramY,
                displayX,
                displayY);

            // Return immediately
            return;
        }

        // Inform the user that we are about to process the request
        this.displayStatus("Please wait while processing your request. This might take a while...",
            "info");

        var thisObj = this;
        require(["openbis",
                "as/dto/service/search/AggregationServiceSearchCriteria",
                "as/dto/service/fetchoptions/AggregationServiceFetchOptions",
                "as/dto/service/execute/AggregationServiceExecutionOptions"],
            function (openbis,
                      AggregationServiceSearchCriteria,
                      AggregationServiceFetchOptions,
                      AggregationServiceExecutionOptions) {

                // Parameters for the aggregation service
                var options = new AggregationServiceExecutionOptions();
                options.withParameter("code", model.dataSetV3.code);
                options.withParameter("paramX", paramX);
                options.withParameter("paramY", paramY);
                options.withParameter("displayX", displayX);
                options.withParameter("displayY", displayY);
                options.withParameter("numEvents", totalNumEvents);
                options.withParameter("maxNumEvents", numEventsToPlot);
                options.withParameter("samplingMethod", samplingMethod);
                options.withParameter("nodeKey", model.dataSetV3.code);

                // Call service
                if (null === thisObj.retrieveFCSEventsService) {
                    var criteria = new AggregationServiceSearchCriteria();
                    criteria.withName().thatEquals("retrieve_fcs_events");
                    var fetchOptions = new AggregationServiceFetchOptions();
                    mainController.openbisV3.searchAggregationServices(criteria, fetchOptions).then(function (result) {

                        // Check that we got our service
                        if (undefined === result.objects) {
                            console.log("Could not retrieve the server-side aggregation service!");
                            return;
                        }
                        thisObj.retrieveFCSEventsService = result.getObjects()[0];

                        // Now call the service
                        mainController.openbisV3.executeAggregationService(
                            thisObj.retrieveFCSEventsService.getPermId(),
                            options).then(function (result) {
                            thisObj.processResultsFromRetrieveFCSEventsServerSidePlugin(result);
                        });
                    });
                } else {
                    // Call the service
                    mainController.openbisV3.executeAggregationService(
                        thisObj.retrieveFCSEventsService.getPermId(),
                        options).then(function (result) {
                        thisObj.processResultsFromRetrieveFCSEventsServerSidePlugin(result);
                    });
                }
            });
    },

    plotFCSData: function (data, xLabel, yLabel, xDisplay, yDisplay) {

        // Make sure to have a proper array
        var parsed_data = JSON.parse(data);

        // Prepend data names to be compatible with C3.js
        parsed_data[0].unshift("x_values");
        parsed_data[1].unshift("y_values");

        // Plot the data
        c3.generate({
            bindto: '#plot_canvas_div',
            title: {
                text: yLabel + " vs. " + xLabel
            },
            data: {
                xs: {
                    y_values: "x_values"
                },
                columns: [
                    parsed_data[0],
                    parsed_data[1],
                ],
                names: {
                    y_values: yLabel
                },
                type: 'scatter'
            },
            axis: {
                x: {
                    label: xLabel,
                    tick: {
                        fit: false
                    }
                },
                y: {
                    label: yLabel,
                    tick: {
                        fit: false
                    }
                }
            },
            legend: {
                show: false
            },
            tooltip: {
                format: {
                    title: function (d) {
                        const format = d3.format(',');
                        return xLabel + " | " + format(d);
                    },
                    value: function (value, ratio, id) {
                        const format = d3.format(',');
                        return format(value);
                    }
                }
            },
            zoom: {
                enabled: true,
                rescale: true
            },
        });
    },

    processResultsFromRetrieveFCSEventsServerSidePlugin: function (table) {

        // Did we get the expected result?
        if (!table.rows || table.rows.length !== 1) {
            DATAVIEWER.displayStatus(
                "There was an error retrieving the data to plot!",
                "danger");
            return;
        }

        // Get the row of results
        var row = table.rows[0];

        // Retrieve the uid
        var r_UID = row[0].value;

        // Is the process completed?
        var r_Completed = row[1].value;

        var thisObj = this;
        if (r_Completed === 0) {

            require(["as/dto/service/execute/AggregationServiceExecutionOptions"],
                function (AggregationServiceExecutionOptions) {

                    // Call the plug-in
                    setTimeout(function () {

                        // Now call the service again:
                        // we only need the UID of the job
                        var options = new AggregationServiceExecutionOptions();
                        options.withParameter("uid", r_UID);

                        mainController.openbisV3.executeAggregationService(
                            thisObj.retrieveFCSEventsService.getPermId(),
                            options).then(function (result) {
                            thisObj.processResultsFromRetrieveFCSEventsServerSidePlugin(result);
                        })
                    }, 2000);
                });

            // Return here
            return;

        }

        // We completed the call and we can process the result

        // Returned parameters
        var r_Success = row[2].value;
        var r_ErrorMessage = row[3].value;
        var r_Data = row[4].value;
        var r_Code = row[5].value;
        var r_ParamX = row[6].value;
        var r_ParamY = row[7].value;
        var r_DisplayX = row[8].value;
        var r_DisplayY = row[9].value;
        var r_NumEvents = row[10].value;   // Currently not used
        var r_MaxNumEvents = row[11].value;
        var r_SamplingMethod = row[12].value;
        var r_NodeKey = row[13].value;

        var level;
        if (r_Success === 1) {

            // Error message and level
            status = r_ErrorMessage;
            level = "success";

            // Plot the data
            thisObj.plotFCSData(r_Data, r_ParamX, r_ParamY, r_DisplayX, r_DisplayY);

            // Cache the plotted data
            var dataKey = r_Code + "_" + r_ParamX + "_" + r_ParamY + "_" + r_MaxNumEvents.toString() +
                "_" + r_DisplayX + "_" + r_DisplayY + "_" + r_SamplingMethod.toString();
            thisObj.cacheFCSData(r_NodeKey, dataKey, r_Data);

        } else {
            status = "Sorry, there was an error: \"" + r_ErrorMessage + "\".";
            level = "danger";
        }

        // We only display errors
        if (r_Success === 0) {
            thisObj.displayStatus(status, level);
        } else {
            thisObj.hideStatus();
        }

        return table;

    },

    cacheFCSData: function (nodeKey, dataKey, fcsData) {

        // Cache the data
        if (! (nodeKey in this.dataCache)) {
            this.dataCache[nodeKey] = {};
        }
        this.dataCache[nodeKey][dataKey] = fcsData;
    },

    displayStatus: function(status, level) {
        switch (level) {
            case "info":
                color = "black";
                break;
            case "success":
                color = "cyan";
                break;
            case "danger":
                color = "red";
                break;
            default:
                color = "black";
                break;
        }
        var status_div = $("#status_div");
        status_div
            .css("color", color)
            .text(status);
        status_div.show();
    },

    hideStatus: function() {
        $("#status_div").hide();
    }

});

profile.plugins.push( new FlowCytometryTechnology());