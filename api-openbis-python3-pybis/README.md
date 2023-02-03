# Welcome to pyBIS!

pyBIS is a Python module for interacting with openBIS, designed to be used in Jupyter. It offers a sort of IDE for openBIS, supporting TAB completition and input checks, making the life of a researcher hopefully easier.

[PyBIS Documentation](src/python/README.md)


# Requirements and organization

### Dependencies and Requirements
- pyBIS relies the openBIS API v3; openBIS version 16.05.2 or newer 
- pyBIS uses Python 3.3 and pandas


### Installation

- locate the `jupyter-api` folder found in `pybis/src/coreplugins`
- copy this folder to `openbis/servers/core-plugins` in your openBIS installation
- register the plugin by editing `openbis/servers/core-plugins/core-plugins.properties` :
- `enabled-modules = jupyter-api` (separate multiple plugins with comma)
- restart your DSS to activate the plugin
