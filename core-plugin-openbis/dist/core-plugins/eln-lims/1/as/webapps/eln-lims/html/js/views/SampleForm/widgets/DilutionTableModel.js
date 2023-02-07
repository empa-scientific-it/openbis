/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function DilutionTableModel(sample, isEnabled) {
	this.sample = sample;
	this.isEnabled = isEnabled;
	this.predefinedMass = [ 139,141,142,143,144
	                        ,145,146,147,148,149
	                        ,150,151,152,153,154
	                        ,155,156,158,159,160
	                        ,161,162,163,164,165
	                        ,166,167,168,169,170
	                        ,171,172,173,174,175
	                        ,176];
	this.allProteins = null;
	this.widgetTableId = "dilution-widget-table";
	this.totalVolume = null;
	
	// NOTE: When adding new columns please remember updating this list correctly
	this.idxColIdx = 0; //This column should always be the 0
	this.metColIdx = 1; //This column should always be the 1
	this.antColIdx = 2; //This column should always be the 2
	this.conColIdx = 3; //This column should always be the 3
	this.cloColIdx = 4; //This column should always be the 4
	this.tbnColIdx = 5;
	this.reaColIdx = 6;
	this.supColIdx = 7;
	this.dilColIdx = 8;
	this.volColIdx = 9; //This column should always be the last
	// END NOTE
}