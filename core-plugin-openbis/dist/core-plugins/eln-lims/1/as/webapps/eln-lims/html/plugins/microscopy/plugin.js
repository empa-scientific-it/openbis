function MicroscopyTechnology() {
    this.init();
}

$.extend(MicroscopyTechnology.prototype, ELNLIMSPlugin.prototype, {
    init: function () {

    },
    forcedDisableRTF: [],
    forceMonospaceFont: [],
    sampleTypeDefinitionsExtension: {
        "MICROSCOPY_SAMPLE_TYPE": {
            "SHOW_ON_NAV": true,
            "SHOW": false,
            "SAMPLE_CHILDREN_DISABLED": true,
            "SAMPLE_PARENTS_DISABLED": true,
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_EXPERIMENT": {
            "SHOW": false,
            "SAMPLE_CHILDREN_DISABLED": false,
            "SAMPLE_PARENTS_DISABLED": false,
            "SAMPLE_PARENTS_ANY_TYPE_DISABLED": true,
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: true, MOVE: false, COPY: false, DELETE: false, PRINT: true, HIERARCHY_GRAPH: true, HIERARCHY_TABLE: true, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: true, EXPORT_METADATA: true },
            "SAMPLE_PARENTS_HINT": [{
                "LABEL": "Organization Units",
                "TYPE": "ORGANIZATION_UNIT",
                "ANNOTATION_PROPERTIES": []
            }]
        }
    },
    dataSetTypeDefinitionsExtension: {
        "MICROSCOPY_ACCESSORY_FILE": {
            "SHOW_ON_NAV": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_OVERVIEW": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_THUMBNAIL": {
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        },
        "MICROSCOPY_IMG_CONTAINER": {
            "SHOW": true,
            "DATASET_PARENTS_DISABLED": true,
            "TOOLBAR": { EDIT: true, FREEZE: false, MOVE: false, ARCHIVE: true, DELETE: false, HIERARCHY_TABLE: true, EXPORT_ALL: true, EXPORT_METADATA: true }
        }
    },
    sampleFormTop: function ($container, model) {
        if (model.sample && model.sample.sampleTypeCode == "MICROSCOPY_EXPERIMENT") {
            this.displayExperimentThumbnails($container, model, model.sample);
        }
        if (model.datasets && model.datasets.length > 0) {
            var imageViewerDataSets = [];
            for (var dsIdx = 0; dsIdx < model.datasets.length; dsIdx++) {
                if (profile.isImageViewerDataSetCode(model.datasets[dsIdx].dataSetTypeCode)) {
                    imageViewerDataSets.push(model.datasets[dsIdx].code);
                }
            }

            if (imageViewerDataSets.length > 0) {
                require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
                    var screningFacade = new openbis(null);
                    screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

                    // Create the image viewer component for the specific data sets
                    var widget = new ImageViewerWidget(screningFacade, imageViewerDataSets);

                    // Customize the widget
                    widget.addLoadListener(function () {

                        widget.getDataSetChooserWidget().then(function (chooser) {

                            var view = chooser.getView();

                            // Show the series name instead of the dataset code
                            view.getDataSetText = function (dataSetCode) {
                                var displayName = dataSetCode;

                                // Return the series name
                                for (var i = 0; i < model.datasets.length; i++) {
                                    if (model.datasets[i].code === dataSetCode &&
                                        model.datasets[i].properties[profile.propertyReplacingCode]) {
                                        displayName = model.datasets[i].properties[profile.propertyReplacingCode];
                                        break;
                                    }
                                }

                                // If not found, return the dataset code
                                return displayName;
                            };

                        });
                    });

                    // Render the component and add it to the page
                    $container.append($('<legend>').text('Microscopy Viewer'));
                    var $imageWidgetContainer = new $('<div>');
                    $imageWidgetContainer.css("margin", "20px");
                    $container.append($imageWidgetContainer);
                    $imageWidgetContainer.append(widget.render());
                });
            }
        }
    },
    sampleFormBottom: function ($container, model) {

    },
    dataSetFormTop: function ($container, model) {
        if (model.dataSetV3 && profile.isImageViewerDataSetCode(model.dataSetV3.type.code)) {
            require(["openbis-screening", "components/imageviewer/ImageViewerWidget"], function (openbis, ImageViewerWidget) {
                var screningFacade = new openbis(null);
                screningFacade._internal.sessionToken = mainController.openbisV1._internal.sessionToken;

                // Create the image viewer component for the specific data sets
                var widget = new ImageViewerWidget(screningFacade, [model.dataSetV3.permId.permId]);

                    // Customize the widget
                    widget.addLoadListener(function () {

                        widget.getDataSetChooserWidget().then(function (chooser) {

                            var view = chooser.getView();

                            // Show the series name instead of the dataset code
                            view.getDataSetText = function (dataSetCode) {
                                var displayName = dataSetCode;
                                // Return the series name
                                if (model.dataSet.code === dataSetCode &&
                                    model.dataSet.properties[profile.propertyReplacingCode]) {
                                    displayName = model.dataSet.properties[profile.propertyReplacingCode];
                                }
                                return displayName;
                            };

                        });
                    });

                // Render the component and add it to the page
                $container.append($('<legend>').text('Microscopy Viewer'));
                var $imageWidgetContainer = new $('<div>');
                $imageWidgetContainer.css("margin", "20px");
                $container.append($imageWidgetContainer);
                $imageWidgetContainer.append(widget.render());
            });
        }
    },
    dataSetFormBottom: function ($container, model) {

    },

    displayExperimentThumbnails: function ($container, model, microscopyExperimentSample) {
        if (microscopyExperimentSample.sampleTypeCode !== "MICROSCOPY_EXPERIMENT") {
            return;
        }

        // Clear the container
        $container.empty();

        if (microscopyExperimentSample.children.length === 0) {
            return;
        }

        // Add legend
        var legend = $("<legend>")
            .text("File previews");
        $container.append(legend);

        // Prepare a div to host the thumbnails
        var sampleView_div = $("<div>");
        $container.append(sampleView_div);

        var newThumbRow = $("<div />", { class: "row" });
        sampleView_div.append(newThumbRow);

        var thisObj = this;

        // Prepare the display
        microscopyExperimentSample.children.forEach(function (sample) {

            // Prepare the name to be shown
            var name = sample.code;
            if (sample.properties[profile.propertyReplacingCode]) {
                name = sample.properties[profile.propertyReplacingCode];
            }

            // Make sure it is not too long
            var displayName;
            var l = name.length;
            if (l > 30) {
                displayName = name.substring(0, 13) + "..." + name.substring(l - 13);
            } else {
                displayName = name;
            }

            // If the size property exists (this was added later), retrieve it and display it as well
            var datasetSize;
            if (sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES) {
                datasetSize = thisObj.formatSizeForDisplay(sample.properties.MICROSCOPY_SAMPLE_SIZE_IN_BYTES);
            } else {
                datasetSize = "";
            }

            // A column to be added to current row that will store all
            // elements related to current sample
            var newThumbCol = $("<div />",
                {
                    class: "col-sm-6 col-md-4 col-lg-3",
                    display: "inline",
                    "text-align": "center",
                    id: sample.code
                });

            // A div element to contain the thumbnail and its info
            var thumbnailView = $("<div />")
                .css("min-height", "320px")
                .css("text-align", "center")
                .css("padding-top", "10px")
                .css("padding-bottom", "10px")

            // Link to the dataset (sample) viewer.
            var link = FormUtil.getFormLink(displayName, "Sample", sample.permId, null);

            // Actual thumbnail. Initially we display a place holder. Later,
            // we will replace it asynchronously.
            var thumbnailImage = $("<img />",
                {
                    src: "./img/image_loading.gif",
                    class: "img-responsive",
                    display: "inline",
                    "text-align": "center",
                    id: "image_" + sample.code,
                    title: name
                });

            // Build the thumbnail viewer
            thumbnailView.append(thumbnailImage);
            thumbnailView.append($("<br />"));
            thumbnailView.append(link);
            if (datasetSize !== "") {
                thumbnailView.append($("<br />"));
                var spanSz = $("<span>").text(datasetSize);
                thumbnailView.append(spanSz);
            }

            // Add the thumbnail to the column and the row
            newThumbCol.append(thumbnailView);
            newThumbRow.append(newThumbCol);

            // Now retrieve the link to the thumbnail image asynchronously and update the <img>
            thisObj.displayThumbnailForSample(model, sample, "image_" + sample.code);

        });
    },

    formatSizeForDisplay: function (datasetSize) {

        // Output
        var formattedDatasetSize = "";

        // Cast datasetSize to float
        var datasetSizeF = parseFloat(datasetSize);

        var sMB = datasetSizeF / 1024.0 / 1024.0;
        if (sMB < 1024.0) {
            formattedDatasetSize = sMB.toFixed(2) + " MiB";
        } else {
            var sGB = sMB / 1024.0;
            formattedDatasetSize = sGB.toFixed(2) + " GiB";
        }

        return formattedDatasetSize;
    },

    displayThumbnailForSample: function (model, sample, img_id) {

        require([
            "as/dto/dataset/search/DataSetSearchCriteria",
            "as/dto/dataset/fetchoptions/DataSetFetchOptions",
            "dss/dto/datasetfile/search/DataSetFileSearchCriteria",
            "dss/dto/datasetfile/fetchoptions/DataSetFileFetchOptions",
        ],

            function (
                DataSetSearchCriteria,
                DataSetFetchOptions,
                DataSetFileSearchCriteria,
                DataSetFileFetchOptions) {


                var dataSetCriteria = new DataSetSearchCriteria();
                dataSetCriteria.withType().withCode().thatEquals("MICROSCOPY_IMG_CONTAINER");
                dataSetCriteria.withSample().withPermId().thatEquals(sample.permId);

                var dataSetFetchOptions = new DataSetFetchOptions();
                dataSetFetchOptions.withChildren();
                dataSetFetchOptions.withProperties();
                dataSetFetchOptions.withComponents();
                dataSetFetchOptions.withComponents().withType();

                // Query the server
                mainController.openbisV3.searchDataSets(dataSetCriteria, dataSetFetchOptions).done(function (result) {
                    if (result.getTotalCount() == 0) {
                        
                        // Thumbnail
                        let imD = $("#" + img_id);
                        
                        // Make sure to reset the display attribute
                        imD.css("display", "inline");
                        
                        // Thumbnail not found!
                        imD.attr("src", "./img/image_unavailable.png");
                        imD.attr("title", "Could not find a thumbnail for this dataset!");
                        
                        return null;
                    }


                    // Declare the dataSet that we will use to retrieve the thumbnail
                    let dataSet = null;

                    // All MICROSCOPY_IMG_CONTAINER datasets (i.e. a file series) contain a MICROSCOPY_IMG_OVERVIEW
                    // and a MICROSCOPY_IMG dataset; one of the series will also contain a MICROSCOPY_IMG_THUMBNAIL,
                    // which is what we are looking for here.
                    // Even though the MICROSCOPY_IMG_THUMBNAIL is always created for series 0, we cannot guarantee
                    // here that series zero will be returned as the first. We quickly scan through the returned
                    // results for the MICROSCOPY_IMG_CONTAINER that has three contained datasets.
                    // From there we can then quickly retrieve the MICROSCOPY_IMG_THUMBNAIL.
                    external_loop:
                    for (let i = 0; i < result.objects.length; i++) {
                        let currentDataSet = result.objects[i];
                        if (null == currentDataSet.components) {
                            continue;
                        }
                        for (let j = 0; j < currentDataSet.components.length; j++) {
                            if (currentDataSet.components[j].type.code === "MICROSCOPY_IMG_THUMBNAIL") {
                                dataSet = currentDataSet.components[j];
                                break external_loop;
                            }
                        }
                    }


                    // Check that we indeed found the dataSet of type MICROSCOPY_IMG_THUMBNAIL
                    if (dataSet == null) {

                        // Thumbnail
                        let imD = $("#" + img_id);

                        // Make sure to reset the display attribute
                        imD.css("display", "inline");

                        // Thumbnail not found!
                        imD.attr("src", "./img/image_unavailable.png");
                        imD.attr("title", "Could not find a thumbnail for this dataset!");

                        return;
                    }

                    // Now retrieve the thumbnail and add display it

                    // Get the file
                    let criteria = new DataSetFileSearchCriteria();
                    let dataSetCriteria = criteria.withDataSet().withOrOperator();
                    dataSetCriteria.withPermId().thatEquals(dataSet.permId.permId);

                    let fetchOptions = new DataSetFileFetchOptions();

                    var dataSetContainer = result.getObjects()[0];
                    
                    // Query the server
                    mainController.openbisV3.getDataStoreFacade().searchFiles(criteria, fetchOptions).done(function (result) {

                        // Thumbnail
                        let imD = $("#" + img_id);

                        // Make sure to reset the display attribute
                        imD.css("display", "inline");

                        if (result.getTotalCount() === 0) {

                            // Thumbnail not found!
                            imD.attr("src", "./img/image_unavailable.png");
                            imD.attr("title", "Could not find a thumbnail for this dataset!");

                            return;
                        }

                        // Extract the files
                        let datasetFiles = result.getObjects();

                        // Find the thumbnail
                        datasetFiles.forEach(function (f) {

                            if (!f.isDirectory()) {

                                if (f.getPath() == "thumbnail.png") {
                                    // Build the download URL
                                    let url = f.getDataStore().getDownloadUrl() + "/datastore_server/" +
                                        f.permId.dataSetId.permId + "/" + f.getPath() + "?sessionID=" +
                                        mainController.serverFacade.openbisServer.getSession();

                                    // Replace the image
                                    let eUrl = encodeURI(url);
                                    eUrl = eUrl.replace('+', '%2B');
                                    imD.attr("src", eUrl);
                                }
                            }
                        });
                    });
                });
            });
    }
});

profile.plugins.push( new MicroscopyTechnology());