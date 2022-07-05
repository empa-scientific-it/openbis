function InstanceProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(InstanceProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);

// BEGIN ANSIBLE MANAGED BLOCK (jupyter)
//		this.jupyterIntegrationServerEndpoint = "https://jupyterhub-demo.labnotebook.ch:80";
//		this.jupyterEndpoint = "https://jupyterhub-demo.labnotebook.ch/";
// END ANSIBLE MANAGED BLOCK (jupyter)
// BEGIN ANSIBLE MANAGED BLOCK (sso)
//		this.singleSignOnUrlTemplate = "https://openbis-demo.ethz.ch/Shibboleth.sso/Login?target=https://openbis-demo.ethz.ch/shibboleth";
//		this.singleSignOnLinkLabel = "Login with SWITCHaai";
// END ANSIBLE MANAGED BLOCK (sso)
		this.hideSectionsByDefault = true;
        this.minBarcodeLength = 15;
        this.docuBaseUrl = "https://unlimited.ethz.ch/display/openBISDoc2010";
        this.showUserManagementConfig = false;
}
})	
