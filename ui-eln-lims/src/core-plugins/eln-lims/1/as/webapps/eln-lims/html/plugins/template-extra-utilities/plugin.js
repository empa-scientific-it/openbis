function ExtraUtilitiesTemplateTechnology() {
    this.init();
}

$.extend(ExtraUtilitiesTemplateTechnology.prototype, ELNLIMSPlugin.prototype, {

	init: function() {

	},
    getExtraUtilities : function() {
        var _this = this;
        return [{
                icon : "glyphicon glyphicon-info-sign", // Glyphicon example icon. For more check https://www.w3schools.com/bootstrap/bootstrap_ref_comp_glyphs.asp
                uniqueViewName : "TEMPLATE_EXTRA_UTILITIES_CUSTOM_VIEW_HTML_TEMPLATE",
                label : "Custom View Template HTML",
                paintView : function($header, $content) {
                    $header.append($("<h1>").append("Custom View Template HTML"));
                    $content.load("./plugins/template-extra-utilities/www/template.html");
                }
            }, {
                icon : "fa fa-info-circle", // Font awesome example icon. For more check https://fontawesome.com/v4.7/icons/
                uniqueViewName : "TEMPLATE_EXTRA_UTILITIES_CUSTOM_VIEW_JS_TEMPLATE",
                label : "Custom View Template JS",
                paintView : function($header, $content) {
                    $header.append($("<h1>").append("Custom View Template JS"));
                    $content.append($("<p>").append("This is a custom view build with JS."));
                }
            }, {
                icon : null, // No icon example
                uniqueViewName : "TEMPLATE_EXTRA_UTILITIES_EXTERNAL_LINK_TEMPLATE",
                label : "Custom Link Template",
                paintView : function($header, $content) {
                    $header.append($("<h1>").append("Custom Link Template"));
                    var src = "https://not-openbis-domain.com/login" + "?token=" + mainController.serverFacade.openbisServer.getSession();
                    var win = window.open(src, '_blank');
                    win.focus();
                }
            }
        ];
    }
});

profile.plugins.push(new ExtraUtilitiesTemplateTechnology());