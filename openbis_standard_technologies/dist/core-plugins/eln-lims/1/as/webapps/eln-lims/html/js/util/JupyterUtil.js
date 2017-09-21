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

var JupyterUtil = new function() {
	
	this.copyNotebook = function(datasetCode, notebookURL) {
		var jupyterNotebook = new JupyterCopyNotebookController(datasetCode, notebookURL);
		jupyterNotebook.init();
	}
	
	this.openJupyterNotebookFromTemplate = function(folder, fileName, template) {
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
		
		$.ajax({
            url : jupyterURL,
            type : 'POST',
            crossDomain: true,
            data : template,
            success : function(result) {
            	var win = window.open(jupyterNotebookURL + result.fileName, '_blank');
				win.focus(); 
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.createJupyterNotebookAndOpen = function(folder, fileName, dataSetIds, ownerEntity) {
		fileName = fileName + ".ipynb";
		var jupyterURL = profile.jupyterIntegrationServerEndpoint + "?token=" + mainController.serverFacade.openbisServer.getSession() + "&folder=" + folder + "&filename=" + fileName;
		var newJupyterNotebook = this.createJupyterNotebookContent(dataSetIds, ownerEntity, fileName);
		var jupyterNotebookURL = profile.jupyterEndpoint + "user/" + mainController.serverFacade.getUserId() + "/notebooks/" + folder + "/";
		
		$.ajax({
            url : jupyterURL,
            type : 'POST',
            crossDomain: true,
            data : JSON.stringify(newJupyterNotebook),
            success : function(result) {
            	var win = window.open(jupyterNotebookURL + result.fileName, '_blank');
				win.focus(); 
            },
            error : function(result) {
            	alert("error: " + JSON.stringify(result));
            }
		});
	}
	
	this.createJupyterNotebookContent = function(dataSetIds, ownerEntity, fileName) {
		var content = [];
		var mainTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "# Jupyter notebook autogenerated from openBIS-ELN"
				   ]
		}
		content.push(mainTitle);
		var connectTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "### Connect to openBIS"
				   ]
		}
		content.push(connectTitle);
		var initializeOpenbisConnection = {
			      "cell_type": "code",
			      "execution_count": null,
			      "metadata": {
			        "collapsed": false
			      },
			      "outputs": [],
			      "source": [
			        "from pybis import Openbis\n",
			        "o = Openbis(url='" + profile.jupyterOpenbisEndpoint + "', verify_certificates=False)"
			      ]
		};
		content.push(initializeOpenbisConnection);
		
		var datasetsInfoTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "### Datasets Information"
				   ]
		}
		content.push(datasetsInfoTitle);
		for(var cIdx = 0; cIdx < dataSetIds.length; cIdx++) {
			var datasetInfo = {
				      "cell_type": "code",
				      "execution_count": null,
				      "metadata": {
				        "collapsed": true
				      },
				      "outputs": [],
				      "source": [
				        "ds" + cIdx + " = o.get_dataset('" + dataSetIds[cIdx]+ "')\n",
				        "ds" + cIdx + ".attrs"
				      ]
			};
			content.push(datasetInfo);
		}
		
		var datasetsDownloadTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "### Datasets Download"
				   ]
		}
		content.push(datasetsDownloadTitle);
		for(var cIdx = 0; cIdx < dataSetIds.length; cIdx++) {
			var datasetDownload = {
				      "cell_type": "code",
				      "execution_count": null,
				      "metadata": {
				        "collapsed": true
				      },
				      "outputs": [],
				      "source": [
				        "ds" + cIdx + ".download(files=ds" + cIdx + ".file_list, destination='./', wait_until_finished=True)",
				      ]
			};
			content.push(datasetDownload);
		}
		
		var notebookProcessTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "### Process your data here"
				   ]
		}
		content.push(notebookProcessTitle);
		var notebookProcess = {
			      "cell_type": "code",
			      "execution_count": null,
			      "metadata": {
			        "collapsed": true
			      },
			      "outputs": [],
			      "source": []
		};
		content.push(notebookProcess);
		
		var saveTitle = {
				   "cell_type": "markdown",
				   "metadata": {},
				   "source": [
				    "### Create Result Dataset with current notebook and HTML version with the output (save the notebook first!)"
				   ]
		}
		content.push(saveTitle);
		var createHTML = [
					        "from nbconvert import HTMLExporter\n",
					        "import codecs\n",
					        "import nbformat\n",
					        "exporter = HTMLExporter()\n",
					        "output_notebook = nbformat.read('" + fileName + "', as_version=4)\n",
					        "output, resources = exporter.from_notebook_node(output_notebook)\n",
					        "codecs.open('" + fileName + ".html', 'w', encoding='utf-8').write(output)\n",
					        "\n"
		];
		
		
		var ownerSettings = ""
		switch(ownerEntity["@type"]) {
			case "as.dto.experiment.Experiment":
				ownerSettings = "experiment= o.get_experiment('"+ ownerEntity.identifier.identifier +"'),\n";
				break;
			case "as.dto.sample.Sample":
				ownerSettings = "sample= o.get_sample('"+ ownerEntity.identifier.identifier +"'),\n";
				break;
		}
		
		var parents = JSON.stringify(dataSetIds);
		
		var createDataset = [
		                     "ds_new = o.new_dataset(\n",
		                     "type='ANALYZED_DATA',\n",
		                     ownerSettings,
		                     "parents=" + parents + ",\n",
		                     "files = ['" + fileName + "', '" + fileName + ".html'],\n",
		                     "props={'name': 'Name your dataset!', 'notes': 'Write some notes or delete this property!'}\n",
		                     ")\n",
		                     "ds_new.save()"
		];
		
		var save = {
			      "cell_type": "code",
			      "execution_count": null,
			      "metadata": {
			        "collapsed": true
			      },
			      "outputs": [],
			      "source": createHTML.concat(createDataset)
		};
		content.push(save);
		
		return {
			  "cells": content,
					  "metadata": {
					    "kernelspec": {
					      "display_name": "Python 3",
					      "language": "python",
					      "name": "python3"
					    },
					    "language_info": {
					      "codemirror_mode": {
					        "name": "ipython",
					        "version": 3
					      },
					      "file_extension": ".py",
					      "mimetype": "text/x-python",
					      "name": "python",
					      "nbconvert_exporter": "python",
					      "pygments_lexer": "ipython3",
					      "version": "3.5.2"
					    }
					  },
					  "nbformat": 4,
					  "nbformat_minor": 2
		};
	}

}