var IdentifierUtil = new function() {
	this.isProjectSamplesEnabled = false;
    this.createContinuousSampleCodes = true;
    		
	//
	// Identifier Building
	//
	
	this.getForcedSpaceIdentifier = function(spaceCode) { // Only used by ELN
		return ('/' + spaceCode);
	}
	
	this.getMaterialIdentifier = function(materialTypeCode, materialCode) {
		return ('/' + materialTypeCode + '/' + materialCode);
	}
	
	this.getProjectIdentifier = function(spaceCode, projectCode) {
		return ('/' + spaceCode + '/' + projectCode);
	}
	
	this.getExperimentIdentifier = function(spaceCode, projectCode, experimentCode) {
		return ('/' + spaceCode + '/' + projectCode + '/' + experimentCode);
	}
	
	this.getSampleIdentifier = function(spaceCode, projectCodeOrNull, sampleCode) {
		return ('/' + spaceCode + '/' + ((projectCodeOrNull && this.isProjectSamplesEnabled)?projectCodeOrNull + '/':'') + sampleCode);
	}
	
	//
	// All Identifier Parsing
	//
	
	this.getSpaceCodeFromIdentifier = function(identifier) {
		var identifierParts = identifier.split('/');
		var spaceCode;
		if(identifierParts.length > 2) { //If has less parts, is a shared sample
			spaceCode = identifierParts[1];
		}
		return spaceCode;
	};
	
	this.getCodeFromIdentifier = function(identifier) {
		var identifierParts = identifier.split('/');
		return identifierParts[identifierParts.length - 1];
	}
	
	//
	// Sample Identifier Parsing
	//
	
	this.getProjectCodeFromSampleIdentifier = function(sampleIdentifier) {
		var projectCode;
		var sampleIdentifierParts = sampleIdentifier.split('/');
		if(sampleIdentifierParts.length === 4) {
			projectCode = sampleIdentifierParts[2];
		}
		return projectCode;
	}
	
	this.getContainerSampleIdentifierFromContainedSampleIdentifier = function(sampleIdentifier) {
		var containerSampleIdentifier;
		var containerIdentifierEnd = sampleIdentifier.lastIndexOf(':');
		if(containerIdentifierEnd !== -1) {
			containerSampleIdentifier = sampleIdentifier.substring(0, containerIdentifierEnd);
		}
		return containerSampleIdentifier;
	}
	
	//
	// Experiment Identifier Parsing
	//
	
	this.getProjectIdentifierFromExperimentIdentifier = function(experimentIdentifier) {
		var spaceCode = this.getSpaceCodeFromIdentifier(experimentIdentifier);
		var projectCode = this.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
		return this.getProjectIdentifier(spaceCode, projectCode);
	}
	
	this.getProjectCodeFromExperimentIdentifier = function(experimentIdentifier) {
		return experimentIdentifier.split('/')[2];
	};
	
	
	this.createDummySampleIdentifierFromExperimentIdentifier = function(experimentIdentifier) {
		var spaceCode = this.getSpaceCodeFromIdentifier(experimentIdentifier);
		var projectCode = this.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
		return "/" + spaceCode + "/" + projectCode + "/__DUMMY_FOR_RIGHTS_CALCULATION__";
	}
}