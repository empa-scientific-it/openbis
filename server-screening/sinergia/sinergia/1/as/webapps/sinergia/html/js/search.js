//
// Utility search functions
//
this.getInitializedSamples = function(result) {
	
	//
	// Fill Map that uses as key the sample @id and value the sample object 
	//
	var samplesById = {};
	
	function storeSamplesById(originalSample)
	{
		var stack = [originalSample];
		
		var referredSample = null;
		while (referredSample = stack.pop()) {
			if (isNaN(referredSample)) {
				samplesById[referredSample["@id"]] = referredSample;
				if (referredSample.parents) {
					for(var i = 0, len = referredSample.parents.length; i < len; ++i) {
						stack.push(referredSample.parents[i]);
					}
				}
				if (referredSample.children) {
					for(var i = 0, len = referredSample.children.length; i < len; ++i) {
						stack.push(referredSample.children[i]);
					}
				}
			}					
		}
	}
	
	for(var i = 0; i < result.length; i++) {
		var sampleOrId = result[i];
		storeSamplesById(sampleOrId);
	}
	
	//
	// Fix Result List
	//
	var finalSamples = [];
	
	for(var i = 0; i < result.length; i++) {
		var sampleOrId = result[i];
		if (isNaN(sampleOrId))
		{
			sampleOrId = samplesById[sampleOrId["@id"]];
		} else
		{
			sampleOrId = samplesById[sampleOrId]; 
		}
		
		//Fill Parents - Only 1 Level without recursion
		if(sampleOrId.parents) {
			for(var j = 0; j < sampleOrId.parents.length; j++) {
				var parentOrId = sampleOrId.parents[j];
				if(!isNaN(parentOrId)) { //If is an Id get the reference
					sampleOrId.parents[j] = samplesById[parentOrId];
				}
			}
		}
		
		//Fill Children - Only 1 Level without recursion
		if(sampleOrId.children) {
			for(var j = 0; j < sampleOrId.children.length; j++) {
				var childOrId = sampleOrId.children[j];
				if(!isNaN(childOrId)) { //If is an Id get the reference
					sampleOrId.children[j] = samplesById[childOrId];
				}
			}
		}
		
		finalSamples.push(sampleOrId);
	}

	return finalSamples;
}

this.searchSorter = function(searchResults) {
	
		var getChars = function(code) {
			var theChars = code.replace(/[0-9]/g, '')
			return theChars;
		}
	
		var getNums = function(code) {
			var thenum = code.replace( /^\D+/g, '');
			if(thenum.length > 0) {
				return parseInt(thenum);
			} else {
				return 0;
			}
		}
	
		var customSort = function(sampleA, sampleB){
			var aCode = getChars(sampleA.code);
			var bCode = getChars(sampleB.code);
		
			var returnValue = null;
			if(aCode < bCode) {
				returnValue = -1;
			} else if(aCode > bCode) {
				returnValue = 1;
			} else {
				var aNum = getNums(sampleA.code);
				var bNum = getNums(sampleB.code);
				returnValue = aNum - bNum;
			}
			return +1 * returnValue;
		}
	
		var sortedResults = searchResults.sort(customSort);
	
		return sortedResults;
}

function searchSamplesWithType(sampleType, callbackFunction)
{	
	var matchClauses = [ {"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",			
				attribute : "TYPE",
				desiredValue : sampleType
			}
	]
		
	var sampleCriteria = {
			matchClauses : matchClauses,
			operator : "MATCH_ALL_CLAUSES"
	};
	
	var localReference = this;
	openbis.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS", "CONTAINED"], function(data) {
		callbackFunction(
			localReference.searchSorter(localReference.getInitializedSamples(data.result))
		);
	});
}


function searchSamplesWithTypeAndCode(sampleType, sampleCode, callbackFunction)
{    
	var matchClauses = [ {"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "TYPE",
				desiredValue : sampleType
			}
	]
	
	matchClauses.push(
				  {
				  "@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "CODE",
				desiredValue : sampleCode 
			}        
	);
	
	
	var sampleCriteria = 
	{
		matchClauses : matchClauses,
		operator : "MATCH_ALL_CLAUSES"
	};
	
	var localReference = this;
	openbis.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
		callbackFunction(localReference.getInitializedSamples(data.result));
	});
}

function searchSamplesWithTypeAndCodeAndGeneProperty(sampleType, sampleCode, sampleGene, callbackFunction)
{    
	var matchClauses = [ 
			{
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "TYPE",
				desiredValue : sampleType
			},
			{
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "CODE",
				desiredValue : sampleCode 
			},
			{
				"@type":"PropertyMatchClause",
				fieldType : "PROPERTY",            
				propertyCode : "GENE",
				desiredValue : sampleGene 
			} 
	]
	
	var sampleCriteria = 
	{
		matchClauses : matchClauses,
		operator : "MATCH_ALL_CLAUSES"
	};
	
	var localReference = this;
	openbis.searchForSamplesWithFetchOptions(sampleCriteria, ["PROPERTIES", "ANCESTORS", "DESCENDANTS"], function(data) {
		callbackFunction(localReference.getInitializedSamples(data.result));
	});
}

function searchMaterialWithTypeAndCode(materialType, materialCode, callbackFunction)
{    
	var matchClauses = [ 
			{
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "TYPE",
				desiredValue : materialType
			},
			{
				"@type":"AttributeMatchClause",
				fieldType : "ATTRIBUTE",            
				attribute : "CODE",
				desiredValue : materialCode 
			}
	]
	
	var materialCriteria = 
	{
		matchClauses : matchClauses,
		operator : "MATCH_ALL_CLAUSES"
	};
	
	var localReference = this;
	openbis.searchForMaterials(materialCriteria, function(data) {
		callbackFunction(data.result);
	});
}