
function StandardProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(StandardProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.imageViewerDataSetCodes = ["MICROSCOPY_IMG_CONTAINER"];
		
		this.hideCodes = true;
		
		this.storagesConfiguration = { "isEnabled" : false };
		
		this.sampleFormOnSubmit = function(sample, action) {
			if(sample.sampleTypeCode === "ORDER") {
				var orderStatus = sample.properties["$ORDERING.ORDER_STATUS"];
				var samplesToDelete = null;
				var changesToDo = null;
				if((orderStatus === "ORDERED" || orderStatus === "DELIVERED" || orderStatus === "PAID") && !sample.properties["$ORDER.ORDER_STATE"]) {
					//Update parents to hold all info
    	    				var searchSamples = { entityKind : "SAMPLE", logicalOperator : "OR", rules : {} };
					for(var pIdx = 0; pIdx < sample.parents.length; pIdx++) {
						searchSamples.rules["UUIDv4_" + pIdx] = { type : "Attribute", name : "PERM_ID", value : sample.parents[pIdx].permId };
					}
					
					mainController.serverFacade.searchForSamplesAdvanced(searchSamples, { only : true, withProperties : true, withAncestors : true, withAncestorsProperties : true }, function(result) {
						sample.parents = mainController.serverFacade.getV3SamplesAsV1(result.objects);
						// We force V1 annotations on the ordering system to be backwards compatible, just in case is openBIS 20.X
						for(var pIdx = 0; pIdx < sample.parents.length; pIdx++) {
						    var pIdxAnnotationState = FormUtil.getAnnotationsFromSample(result.objects[pIdx], null);
						    sample.parents[pIdx].properties["$ANNOTATIONS_STATE"] = FormUtil.getXMLFromAnnotations(pIdxAnnotationState);
						}
						//Set property
						sample.properties["$ORDER.ORDER_STATE"] = window.btoa(unescape(encodeURIComponent(JSON.stringify(sample))));
						
						//Update order state on the requests
						changesToDo = [];
						var requests = sample.parents;
						if(requests) {
							for(var rIdx = 0; rIdx < requests.length; rIdx++) {
								changesToDo.push({ "permId" : requests[rIdx].permId, "identifier" : requests[rIdx].identifier, "properties" : {"$ORDERING.ORDER_STATUS" : orderStatus } });
							}
						}
						
						action(sample, null, samplesToDelete, changesToDo);
					});
				} else {
					action(sample, null, samplesToDelete, changesToDo);
				}
				
			} else if(sample.sampleTypeCode === "REQUEST") {
				mainController.currentView._newProductsController.createAndAddToForm(sample, action);
			} else if(action) {
			    profile.onSampleSave(sample, [], action);
			}
		}
		
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);
			if(this.getPropertyTypeFromSampleType(sampleType, "FREEFORM_TABLE_STATE")) {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			} else if(sampleTypeCode === "ORDER") {
				var isExisting = mainController.currentView._sampleFormModel.mode === FormMode.VIEW;
				var isFromState = false;
				if(isExisting) {					
					var showOrderSummary = function(order) {			
						var requests = order.parents;
						var providerByPermId = {};
						var productsByProviderPermId = {};
						var quantityByProductPermId = {};
						var absoluteTotalByCurrency = {};
						
						//
						// Fills data structures
						//
						for(var rIdx = 0; rIdx < requests.length; rIdx++) {
							var request = requests[rIdx];
							var requestProductsAnnotations = FormUtil.getAnnotationsFromSampleV1(request);
							var requestProducts = request.parents;
							for(var pIdx = 0; pIdx < requestProducts.length; pIdx++) {
								var requestProduct = requestProducts[pIdx];
								var requestProductAnnotations = requestProductsAnnotations[requestProduct.permId];
								
								if(requestProduct.parents.length === 0) {
									Util.showUserError("Product " + requestProduct.code + " does not have a provider, FIX IT!.");
									return;
								}
								var provider = requestProduct.parents[0];
								var providerProducts = productsByProviderPermId[provider.permId];
								if(!providerProducts) {
									providerProducts = [];
									productsByProviderPermId[provider.permId] = providerProducts;
									providerByPermId[provider.permId] = provider;
								}
								var quantity = quantityByProductPermId[requestProduct.permId];
								if(!quantity) {
									quantity = 0;
								}
								if (requestProductAnnotations) {
                                    quantity += parseInt(requestProductAnnotations["ANNOTATION.REQUEST.QUANTITY_OF_ITEMS"]);
								}
								if(!quantity) {
									Util.showUserError("Product " + requestProduct.code + " from request " +  request.code + " does not have a quantity, FIX IT!.");
									return;
								}
								var currencyCode = requestProduct.properties["$PRODUCT.CURRENCY"];
								if(!currencyCode) {
									currencyCode = "N/A";
								}
								
								var absoluteTotalForCurrency = absoluteTotalByCurrency[currencyCode];
								if(!absoluteTotalForCurrency) {
									absoluteTotalForCurrency = 0;
								}
								
								if(requestProduct.properties["$PRODUCT.PRICE_PER_UNIT"]) {
									absoluteTotalForCurrency += parseFloat(requestProduct.properties["$PRODUCT.PRICE_PER_UNIT"]) * quantity;
								}
								
								absoluteTotalByCurrency[currencyCode] = absoluteTotalForCurrency;
								
								quantityByProductPermId[requestProduct.permId] = quantity;
								providerProducts.push(requestProduct);
							}
						}
						
						//
						// Button that prints an order report
						//
						var printOrder = FormUtil.getButtonWithIcon("glyphicon-print",function() {
							//Create an order page for each provider
							var orderPages = [];
							for(var providerPermId in productsByProviderPermId) {
								var provider = providerByPermId[providerPermId];
								var preferredSupplierLanguage = provider.properties["$SUPPLIER.COMPANY_LANGUAGE"];
								
								var languageLabels = profile.orderLanguage[preferredSupplierLanguage];
								if(!languageLabels) {
									languageLabels = profile.orderLanguage["ENGLISH"];
								}
								
								var providerProducts = productsByProviderPermId[providerPermId];
								
								var registrationDate = null;
								if(order.registrationDetails && order.registrationDetails.modificationDate) {
									registrationDate = order.registrationDetails.modificationDate;
								} else if(mainController.currentView._sampleFormModel.sample.registrationDetails && 
										mainController.currentView._sampleFormModel.sample.registrationDetails.modificationDate) {
									registrationDate = mainController.currentView._sampleFormModel.sample.registrationDetails.modificationDate;
								}
								
								var page = languageLabels["ORDER_FORM"];
									page += "\n";
									page += "\n";
									page += languageLabels["ORDER_INFORMATION"];
									page += "\n";
									page += "- " + languageLabels["ORDER_DATE"] + ": " + Util.getFormatedDate(new Date(registrationDate));
									page += "\n";
									page += "- " + languageLabels["ORDER_STATUS"] + ": " + order.properties["$ORDERING.ORDER_STATUS"];
									page += "\n";
									page += "- " + languageLabels["ORDER_CODE"] + ": " + order.code;
									page += "\n";
									page += "\n";
									page += "\n";
									page += languageLabels["COSTUMER_INFORMATION"];
									page += "\n";
									if(order.properties["$ORDER.SHIP_TO"]) {
										page += "- " + languageLabels["SHIP_TO"] + ": " + order.properties["$ORDER.SHIP_TO"];
										page += "\n";
									}
									if(order.properties["$ORDER.BILL_TO"]) {
										page += "- " + languageLabels["BILL_TO"] + ": " + order.properties["$ORDER.BILL_TO"];
										page += "\n";
									}
									if(order.properties["$ORDER.SHIP_ADDRESS"]) {
										page += "- " + languageLabels["SHIP_ADDRESS"] + ": " + order.properties["$ORDER.SHIP_ADDRESS"];
										page += "\n";
									}
									if(order.properties["$ORDER.CONTACT_PHONE"]) {
										page += "- " + languageLabels["PHONE"] + ": " + order.properties["$ORDER.CONTACT_PHONE"];
										page += "\n";
									}
									if(order.properties["$ORDER.CONTACT_FAX"]) {
										page += "- " + languageLabels["FAX"] + ": " + order.properties["$ORDER.CONTACT_FAX"];
										page += "\n";
									}
									page += "\n";
									page += "\n";
									page += languageLabels["SUPPLIER_INFORMATION"];
									page += "\n";
									if(provider.properties[profile.propertyReplacingCode]) {
										page += "- " + languageLabels["SUPPLIER"] + ": " + provider.properties[profile.propertyReplacingCode];
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.COMPANY_ADDRESS_LINE_1"]) {
										page += "- " + languageLabels["SUPPLIER_ADDRESS_LINE_1"] + ": " + provider.properties["$SUPPLIER.COMPANY_ADDRESS_LINE_1"]
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.COMPANY_ADDRESS_LINE_2"]) {
										page += "  " + languageLabels["SUPPLIER_ADDRESS_LINE_2"] + "  " + provider.properties["$SUPPLIER.COMPANY_ADDRESS_LINE_2"]
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.COMPANY_PHONE"]) {
										page += "- " + languageLabels["SUPPLIER_PHONE"] + ": " + provider.properties["$SUPPLIER.COMPANY_PHONE"];
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.COMPANY_FAX"]) {
										page += "- " + languageLabels["SUPPLIER_FAX"] + ": " + provider.properties["$SUPPLIER.COMPANY_FAX"];
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.COMPANY_EMAIL"]) {
										page += "- " + languageLabels["SUPPLIER_EMAIL"] + ": " + provider.properties["$SUPPLIER.COMPANY_EMAIL"];
										page += "\n";
									}
									if(provider.properties["$SUPPLIER.CUSTOMER_NUMBER"]) {
										page += "- " + languageLabels["CUSTOMER_NUMBER"] + ": " + provider.properties["$SUPPLIER.CUSTOMER_NUMBER"];
										page += "\n";
									}
									page += "\n";
									page += "\n";
									page += languageLabels["REQUESTED_PRODUCTS_LABEL"];
									page += "\n";
									page += languageLabels["PRODUCTS_COLUMN_NAMES_LABEL"];
									page += "\n";
									var providerTotalByCurrency = {};
									for(var pIdx = 0; pIdx < providerProducts.length; pIdx++) {
										var product = providerProducts[pIdx];
										var quantity = quantityByProductPermId[product.permId];
										var unitPriceAsString = product.properties["$PRODUCT.PRICE_PER_UNIT"];
										var unitPrice = "N/A";
										if(unitPriceAsString) {
											unitPrice = parseFloat(unitPriceAsString);
										}
										var currencyCode = product.properties["$PRODUCT.CURRENCY"];
										if(!currencyCode) {
											currencyCode = "N/A";
										}
										page += quantity + "\t\t" + product.properties[profile.propertyReplacingCode] + "\t\t" + product.properties["$PRODUCT.CATALOG_NUM"] + "\t\t" + unitPrice + " " + currencyCode;
										page += "\n";
										
										if(unitPriceAsString) {
											var totalForCurrency = providerTotalByCurrency[product.properties["$PRODUCT.CURRENCY"]];
											if(!totalForCurrency) {
												totalForCurrency = 0;
											}
											totalForCurrency += unitPrice * quantity;
											
											providerTotalByCurrency[product.properties["$PRODUCT.CURRENCY"]] = totalForCurrency;
										} else {
											providerTotalByCurrency[product.properties["$PRODUCT.CURRENCY"]] = "N/A";
										}
									}
									page += "\n";
									page += "\n";
									page += "\n";
									
									var showTotals = false;
									for(var currency in providerTotalByCurrency) {
										if(providerTotalByCurrency[currency] > 0) {
											showTotals = true;
										}
									}
									if(showTotals) {
										page += languageLabels["PRICE_TOTALS_LABEL"] + ":";
										page += "\n";
										for(var currency in providerTotalByCurrency) {
											page += providerTotalByCurrency[currency] + " " + currency;
											page += "\n";
										}
										page += "\n";
										page += "\n";
										page += "\n";
									}
									page += languageLabels["ADDITIONAL_INFO_LABEL"];
									page += "\n";
									var additionalInfo = order.properties["$ORDER.ADDITIONAL_INFORMATION"];
									if(!additionalInfo) {
										additionalInfo = "N/A";
									}
									page += additionalInfo;
								orderPages.push(page);
							}
							
							//Print Pages
							for(var pageIdx = 0; pageIdx < orderPages.length; pageIdx++) {
								Util.downloadTextFile(orderPages[pageIdx], "order_" + sample.code + "_p" + pageIdx + ".txt");
							}
							
						}, "Print Order", null, "print-order-id");
						
						//
						// Order Summary Grid
						//
						var columns = [ {
							label : 'Catalog Num',
							property : 'catalogNum',
							isExportable: true,
							sortable : true,
							render : function(data) {
								return FormUtil.getFormLink(data.catalogNum, "Sample", data.permId, null, 'catalogNum-' + data.rowId);
							}
						},{
							label : 'Supplier',
							property : 'supplier',
							isExportable: true,
							sortable : true,
                            render : function(data) {
                                var $component = $('<div>', {'id' : 'supplier-' + data.rowId});
                                $component.append(data.supplier);
                                return $component;
                            }
						}, {
							label : 'Name',
							property : 'name',
							isExportable: true,
							sortable : true,
                            render : function(data) {
                                var $component = $('<div>', {'id' : 'name-' + data.rowId});
                                $component.append(data.name);
                                return $component;
                            }
						}, {
							label : 'Quantity',
							property : 'quantity',
							isExportable: true,
							sortable : true,
							render : function(data) {
                                var $component = $('<div>', {'id' : 'quantity-' + data.rowId});
                                $component.append(data.quantity);
                                return $component;
                            }
						}, {
							label : 'Unit Price',
							property : 'unitPrice',
							isExportable: true,
							sortable : true,
							render : function(data) {
                                var $component = $('<div>', {'id' : 'unitPrice-' + data.rowId});
                                $component.append(data.unitPrice);
                                return $component;
                            }
						}, {
							label : 'Total Product Cost',
							property : 'totalProductCost',
							isExportable: true,
							sortable : true,
							render : function(data) {
                                var $component = $('<div>', {'id' : 'totalProductCost-' + data.rowId});
                                $component.append(data.totalProductCost);
                                return $component;
                            }
						}, {
							label : 'Currency',
							property : 'currency',
							isExportable: true,
							sortable : true,
							render : function(data) {
                                var $component = $('<div>', {'id' : 'currency-' + data.rowId});
                                $component.append(data.currency);
                                return $component;
                            }
						}];
						
						var getDataRows = function(callback) {
							var rows = [];
							var index = 0;
							for(var providerPermId in productsByProviderPermId) {
								var provider = providerByPermId[providerPermId];
								var providerProducts = productsByProviderPermId[providerPermId];
								for(var pIdx = 0; pIdx < providerProducts.length; pIdx++) {
									var product = providerProducts[pIdx];
									var quantity = quantityByProductPermId[product.permId];
									var unitPriceAsString = product.properties["$PRODUCT.PRICE_PER_UNIT"];
									var unitPrice = "N/A";
									if(unitPriceAsString) {
										unitPrice = parseFloat(unitPriceAsString);
									}
									var rowData = {};
									rowData.permId = product.permId;
									rowData.supplier = provider.properties[profile.propertyReplacingCode];
									rowData.name = product.properties[profile.propertyReplacingCode];
									rowData.catalogNum =  product.properties["$PRODUCT.CATALOG_NUM"];
									rowData.quantity = quantity;
									rowData.unitPrice = unitPrice;
									rowData.rowId = index;
									if(unitPrice !== "N/A") {
										rowData.totalProductCost = rowData.quantity * rowData.unitPrice;
									} else {
										rowData.totalProductCost = "N/A";
									}
									var currencyCode = product.properties["$PRODUCT.CURRENCY"];
									if(!currencyCode) {
										currencyCode = "N/A";
									}
									rowData.currency = currencyCode;
									rows.push(rowData);
									++index;
								}
								
							}
							callback(rows);
						};
						
						var orderSummaryContainer = $("<div>");
						var repTitle = "Order Summary";
						if(isFromState) {
							repTitle += " (as saved when ordered)"
						}
						
						var orderSummary = new DataGridController(repTitle, columns, [], null, getDataRows, null, false, "ORDER_SUMMARY", false, 30);
						orderSummary.init(orderSummaryContainer);
						
						var totalsByCurrencyContainer = $("<div>").append($("<br>")).append($("<legend>").append("Total:"));
						for(var currency in absoluteTotalByCurrency) {
							totalsByCurrencyContainer.append(absoluteTotalByCurrency[currency] + " " + currency).append($("<br>"));
						}
						$("#" + containerId).append(orderSummaryContainer).append(totalsByCurrencyContainer).append($("<br>")).append(printOrder);
					}
					
					//
					// Data Structures to Help the reports functionality
					//
					var orderSample = mainController.currentView._sampleFormModel.sample;
					if(orderSample.properties["$ORDER.ORDER_STATE"]) {
						isFromState = true;
						var order = JSON.parse(decodeURIComponent(escape(window.atob(orderSample.properties["$ORDER.ORDER_STATE"]))));
						showOrderSummary(order);
					} else {
						var searchSamples = { entityKind : "SAMPLE", logicalOperator : "OR", rules : { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : orderSample.permId } } };
						mainController.serverFacade.searchForSamplesAdvanced(searchSamples, { only : true, withProperties : true, withAncestors : true, withAncestorsProperties : true }, function(result) {
							var order = mainController.serverFacade.getV3SamplesAsV1(result.objects)[0];
							var orderV3 = result.objects[0]

                            // We force V1 annotations on the ordering system to be backwards compatible, just in case is openBIS 20.X
                            for(var pIdx = 0; pIdx < order.parents.length; pIdx++) {
                                var pIdxAnnotationState = FormUtil.getAnnotationsFromSample(orderV3.parents[pIdx], null);
                                order.parents[pIdx].properties["$ANNOTATIONS_STATE"] = FormUtil.getXMLFromAnnotations(pIdxAnnotationState);
                            }

							showOrderSummary(order);
						});
					}
				}
			} else if(sampleTypeCode === "REQUEST") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				if(isEnabled) {
					var $newProductsController = new NewProductsController();
						$newProductsController.init($("#" + containerId), sample.spaceCode);
						mainController.currentView._newProductsController = $newProductsController;
				}
			}
		}
		

		this.dataSetTypeForFileNameMap = [
				// { fileNameExtension : "fasta", dataSetType : "SEQ_FILE" },
		];

}
});