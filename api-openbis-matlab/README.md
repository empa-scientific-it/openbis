# MATLAB toolbox for openBIS

This repository provides a set of MATLAB functions that allow seemless integration
between [MATLAB](https://mathworks.com/products/matlab.html) and
the [openBIS](https://wiki-bsse.ethz.ch/display/bis/Home) data management system. The toolbox is
written as a wrapper around
the [pyBIS](https://sissource.ethz.ch/sispub/openbis/blob/master/pybis/src/python/README.md) Python
API for openBIS. As MATLAB can call Python libraries directly (
see https://ch.mathworks.com/help/matlab/call-python-libraries.html), all the functionality of pyBIS
is directly available from within MATLAB. The main goal of the toolbox is to provide a more
MATLABish way of accessing pyBIS fucntions.

To get started, checkout the installation instructions [here](https://sissource.ethz.ch/sispub/openbis/-/blob/master/docs/api-openbis-matlab/home.md). A usage example is provided in the script [openbis_example.mlx](https://sissource.ethz.ch/sispub/openbis/-/blob/master/api-openbis-matlab/openbis_example.mlx) which can be run within the MATLAB Live Editor. Documentation is provided in the file [OpenBis.m](https://sissource.ethz.ch/sispub/openbis/-/blob/master/api-openbis-matlab/OpenBis.m). In the MATLAB Desktop UI, you can get the documentation by typing `doc OpenBis`.

This repository is maintained by the [Scientific IT Services](https://sis.id.ethz.ch/)
of [ETH Zurich](www.ethz.ch). 

