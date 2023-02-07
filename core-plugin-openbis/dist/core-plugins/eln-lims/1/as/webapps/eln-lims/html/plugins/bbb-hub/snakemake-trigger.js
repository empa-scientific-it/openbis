var SnakemakeTrigger = new function() {

    var dataSetPaths;
    var $formContainerAll;
    var $model;

    this.paintTriggerContainers = function($container, model, experiment) {
        $model = model;
        BBBServerFacade.userIsAdmin(function(isAdmin) {
            UiComponents.stopLoading();
            if (isAdmin) {
                SnakemakeTrigger.paintFormContainers($container);
                SnakemakeTrigger.paintProcessAll();
                files = SnakemakeTrigger.getFiles(experiment);
            }
        });
    }

    this.paintFormContainers = function($container) {
        var $fieldsetAll = UiComponents.getFieldset("Process all comparisons");
        $formContainerAll = $('<div>').addClass('form-group');
        $fieldsetAll.append($formContainerAll);
        $container.append($fieldsetAll);
    }

    this.paintProcessAll = function() {
        var $buttonAll = UiComponents.getButton("Start processing", SnakemakeTrigger.startProcessingAll);
        $formContainerAll.append($buttonAll);
    }

    this.getFiles = function(experiment) {
        dataSetPaths = [];
        for (var i = 0; i < experiment.dataSets.length; i++) {
            var dataSet = experiment.dataSets[i];
            if (dataSet.type.code == "FASTQ" || dataSet.type.code == "METADATA") {
                var path = dataSet.physicalData.shareId + "/" + dataSet.physicalData.location;
                dataSetPaths.push(path);
            }
        }
    }

    this.startProcessingAll = function() {
        UiComponents.startLoading();
        var jsonrpc = {
            "method": "start_processing_all",
            "params": {
                "data_set_paths": dataSetPaths
            }
        };
        BBBServerFacade.callCustomASService(jsonrpc, $model, function(result) {
            UiComponents.stopLoading();
            if (result.success) {
              alert("Processing started.");
            } else {
              alert("Error: " + result.data);
            }
        });
    }
}