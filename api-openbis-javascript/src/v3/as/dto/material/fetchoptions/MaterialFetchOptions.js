/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/AbstractEntityFetchOptions", "as/dto/material/fetchoptions/MaterialTypeFetchOptions", "as/dto/history/fetchoptions/HistoryEntryFetchOptions",
		"as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/tag/fetchoptions/TagFetchOptions", "as/dto/material/fetchoptions/MaterialSortOptions" ],
		function(require, stjs, AbstractEntityFetchOptions) {
			var MaterialFetchOptions = function() {
			    AbstractEntityFetchOptions.call(this);
			};
			stjs.extend(MaterialFetchOptions, AbstractEntityFetchOptions, [ AbstractEntityFetchOptions ], function(constructor, prototype) {
				prototype['@type'] = 'as.dto.material.fetchoptions.MaterialFetchOptions';
				constructor.serialVersionUID = 1;
				prototype.type = null;
				prototype.history = null;
				prototype.registrator = null;
				prototype.materialProperties = null;
				prototype.tags = null;
				prototype.sort = null;
				prototype.withType = function() {
					if (this.type == null) {
						var MaterialTypeFetchOptions = require("as/dto/material/fetchoptions/MaterialTypeFetchOptions");
						this.type = new MaterialTypeFetchOptions();
					}
					return this.type;
				};
				prototype.withTypeUsing = function(fetchOptions) {
					return this.type = fetchOptions;
				};
				prototype.hasType = function() {
					return this.type != null;
				};
				prototype.withHistory = function() {
					if (this.history == null) {
						var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
						this.history = new HistoryEntryFetchOptions();
					}
					return this.history;
				};
				prototype.withHistoryUsing = function(fetchOptions) {
					return this.history = fetchOptions;
				};
				prototype.hasHistory = function() {
					return this.history != null;
				};
				prototype.withRegistrator = function() {
					if (this.registrator == null) {
						var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
						this.registrator = new PersonFetchOptions();
					}
					return this.registrator;
				};
				prototype.withRegistratorUsing = function(fetchOptions) {
					return this.registrator = fetchOptions;
				};
				prototype.hasRegistrator = function() {
					return this.registrator != null;
				};
				prototype.withMaterialProperties = function() {
					if (this.materialProperties == null) {
						this.materialProperties = new MaterialFetchOptions();
					}
					return this.materialProperties;
				};
				prototype.withMaterialPropertiesUsing = function(fetchOptions) {
					return this.materialProperties = fetchOptions;
				};
				prototype.hasMaterialProperties = function() {
					return this.materialProperties != null;
				};
				prototype.withTags = function() {
					if (this.tags == null) {
						var TagFetchOptions = require("as/dto/tag/fetchoptions/TagFetchOptions");
						this.tags = new TagFetchOptions();
					}
					return this.tags;
				};
				prototype.withTagsUsing = function(fetchOptions) {
					return this.tags = fetchOptions;
				};
				prototype.hasTags = function() {
					return this.tags != null;
				};
				prototype.sortBy = function() {
					if (this.sort == null) {
						var MaterialSortOptions = require("as/dto/material/fetchoptions/MaterialSortOptions");
						this.sort = new MaterialSortOptions();
					}
					return this.sort;
				};
				prototype.getSortBy = function() {
					return this.sort;
				};
			}, {
				type : "MaterialTypeFetchOptions",
				history : "HistoryEntryFetchOptions",
				registrator : "PersonFetchOptions",
				materialProperties : "MaterialFetchOptions",
				tags : "TagFetchOptions",
				sort : "MaterialSortOptions"
			});
			return MaterialFetchOptions;
		})