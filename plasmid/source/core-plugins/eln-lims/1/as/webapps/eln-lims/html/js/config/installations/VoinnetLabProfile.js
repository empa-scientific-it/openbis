
function VoinnetLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(VoinnetLabProfile.prototype, StandardProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
	
		
}
});
