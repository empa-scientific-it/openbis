define([], function() {
	"use strict";
	var DetailedInfoDialogLogic = function(communicationFacade) {
		this.communicationFacade = communicationFacade;
	};

	DetailedInfoDialogLogic.prototype.getDetailDropboxInfo = function(getInfoAbout,
		callback, logN) {
        var parameters = {
            dropboxName: getInfoAbout,
            logN: logN
        };
        this.communicationFacade.getDetailDropboxInfo(parameters, callback);
    };

	return DetailedInfoDialogLogic;

});
