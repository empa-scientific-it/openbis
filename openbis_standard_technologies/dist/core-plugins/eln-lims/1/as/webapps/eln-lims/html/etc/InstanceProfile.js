alert("Instance Profile");
function InstanceProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(InstanceProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		StandardProfile.prototype.init.call(this, serverFacade);

// BEGIN ANSIBLE MANAGED BLOCK
//		this.jupyterIntegrationServerEndpoint = "https://jupyterhub-demo.labnotebook.ch:80";
//		this.jupyterEndpoint = "https://jupyterhub-demo.labnotebook.ch/";
// END ANSIBLE MANAGED BLOCK
		this.hideSectionsByDefault = true;
        this.minBarcodeLength = 15;
}
})	
