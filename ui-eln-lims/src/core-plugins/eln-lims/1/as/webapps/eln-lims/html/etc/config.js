var loadJSResorce = function(pathToResource, onLoad) {
    var head = document.getElementsByTagName('head')[0];
    var script= document.createElement('script');
    script.type= 'text/javascript';
    var src = pathToResource;
    script.src= src;
    script.onreadystatechange= function () {
        if (this.readyState === 'complete') onLoad();
    };
    script.onload = onLoad;

    head.appendChild(script);
};

var setFavicons = function(img) {
    var head = document.getElementsByTagName('head')[0];

    if(_.isString(img)){
        var iconLink= document.createElement('link');
        iconLink.setAttribute('rel', 'icon');
        iconLink.setAttribute('href', img);
        head.appendChild(iconLink);
    }else if(_.isObject(img)){
        Object.keys(img).forEach(function(linkKey){
            var linkDefinition = img[linkKey]
            var linkElement = document.createElement('link');
            if(_.isObject(linkDefinition)){
                Object.keys(linkDefinition).forEach(function(attributeName){
                    linkElement.setAttribute(attributeName, linkDefinition[attributeName]);
                })
                head.appendChild(linkElement);
            }
        })
    }
};

var setHelp = function(url) {
    var $container = $("#help");
    $container.append("Click ");
    $container.append($("<a>", {href: url, text: "here", target : "_blank"}));
    $container.append(" for help.");
};

var onLoadInstanceProfileResorceFunc = function() {
	profile = new InstanceProfile();
	//
	// Updating title and logo
	//
	$("#mainLogo").attr("src", profile.mainLogo);
	$("#mainLogoTitle").append(profile.mainLogoTitle);
	if(profile.mainLogoTitle.length < 10) {
		$("#mainLogoTitle").css("font-weight", "bold");
	}
	$("login-form-div").attr("visibility", "visible");
};

//<PROFILE_PLACEHOLDER>
loadJSResorce("./etc/InstanceProfile.js", onLoadInstanceProfileResorceFunc);
setFavicons({
    appleTouchIcon : { href : "./img/apple-touch-icon.png", rel: "apple-touch-icon", sizes: "180x180" },
    favicon32 : { href : "./img/favicon-32x32.png", rel: "icon", type: "image/png", sizes: "32x32" },
    favicon16 : { href : "./img/favicon-16x16.png", rel: "icon", type: "image/png", sizes: "16x16" },
    manifest : { href : "./site.webmanifest", rel: "manifest" }
});
//</PROFILE_PLACEHOLDER>

var PLUGINS_CONFIGURATION = {
    extraPlugins : ["life-sciences", "flow", "microscopy"]
};

var options = {
    showResearchCollectionExportBuilder: false
};