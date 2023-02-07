function BBBHubTechnology() {
    this.init();
}

$.extend(BBBHubTechnology.prototype, ELNLIMSPlugin.prototype, {
    // This code is copy of openBis plugin. It was taken from here:
    // https://sissource.ethz.ch/sis/bbb-hub/tree/master/openbis/core-plugins/bbb-hub/1/as/webapps/snakemake/html/js
    // Now it is a ELN plugin.

	init: function() {
	    //
	    profile.MainMenuNodeNames.Lab_Notebook = "DataSets";
	    profile.defaultStartView = {
	        page : "EXTRA_PLUGIN_UTILITY",
	        args : "BBB_HUB_HELP"
	    };
	    //

        // Setting help page and resize the login form
        $("#login-form-div").attr("style", "width:500px !important; height:500px !important");
        setHelp("./plugins/bbb-hub/www/help.html");

        loadJSResorce("./plugins/bbb-hub/UiComponents.js");
        loadJSResorce("./plugins/bbb-hub/BBBServerFacade.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-table.js");
        loadJSResorce("./plugins/bbb-hub/snakemake-trigger.js");
	},
    experimentTypeDefinitionsExtension : {
        "BBB": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, DELETE: false, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
    },
    sampleTypeDefinitionsExtension : {
        "FASTQ": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: false, HIERARCHY_GRAPH: false, HIERARCHY_TABLE: false, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
        "FOLDER": {
            "TOOLBAR": { CREATE: false, FREEZE: false, EDIT: false, MOVE: false, COPY: false, DELETE: false, PRINT: false, HIERARCHY_GRAPH: false, HIERARCHY_TABLE: false, UPLOAD_DATASET: false, UPLOAD_DATASET_HELPER: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
    },
    dataSetTypeDefinitionsExtension : {
        "FASTQ": {
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: false, DELETE: false, HIERARCHY_TABLE: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
        "METADATA": {
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: false, DELETE: false, HIERARCHY_TABLE: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
        "RESULTS": {
            "TOOLBAR": { EDIT: false, FREEZE: false, MOVE: false, ARCHIVE: false, DELETE: false, HIERARCHY_TABLE: false, EXPORT_ALL: false, EXPORT_METADATA: true }
        },
    },
	experimentFormTop : function($container, model) {
	    BBBServerFacade.getExperiment($container, model);
    },
    experimentFormBottom : function($container, model) {

    },

    getExtraUtilities : function() {
        var _this = this;
        return [{
            icon : "fa fa-table",
            uniqueViewName : "BBB_VIEW_NAME_TEST",
            label : "Index Page",
            paintView : function($header, $content) {
                    $header.append($("<h1>").append("Public Index Page"));
                    BBBServerFacade.getExperiments($content);
                }
            },{
                icon : "glyphicon glyphicon-info-sign",
                uniqueViewName : "OPENBIS_DOCUMENTATION",
                label : "openBIS Documentation",
                paintView : function($header, $content) {
                   $header.append($("<h1>").append("openBIS Documentation"));
                   _this.paintContent($content);
                }
            },{
                icon : "glyphicon glyphicon-info-sign",
                uniqueViewName : "BBB_HUB_HELP",
                label : "BBB-Hub Documentation",
                paintView : function($header, $content) {
                    $header.append($("<h1>").append("Welcome to BBB-Hub"));
                    $content.load("./plugins/bbb-hub/www/help-upload.html");
                }
            }
        ];
    },

    paintContent : function($content) {
        var src = "https://openbis.ch/index.php/docs/user-documentation-19-06-4/";
        $content.append($("<iframe src=" + src + " width='99%' height='700px'></iframe>"));
    },

    setNewNameById : function(elementId, newName, timeout) {
        var DEFAULT_TIMEOUT_STEP = 100;

        return new Promise(function executor(resolve, reject) {
            if($("#" + elementId).length <= 0) {
                setTimeout(executor.bind(null, resolve, reject), DEFAULT_TIMEOUT_STEP);
            } else {
                $("#" + elementId).html(newName);
                resolve();
            }
        });
    }
});

profile.plugins.push(new BBBHubTechnology());