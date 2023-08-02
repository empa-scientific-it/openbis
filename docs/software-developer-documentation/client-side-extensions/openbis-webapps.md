openBIS webapps
===============
  
## Introduction

Webapps are HTML5 apps that interact with openBIS. Webapps can be
distributed as core-plugins. To supply a webapp plugin, create a folder
called `webapps` in the `as`. Each subfolder of the `webapps` folder is
treated as a webapp plugin. A webapp plugin requires two things, a
`plugin.properties` file, as with all plugins, and a folder containing
the content of the webapp. This folder can have any name and needs to be
referenced in the `plugin.properties` file with the key
**webapp-folder**.

It is recommended to name the webapp folder `html` as done in the
examples below. This has the advantage that an existing subfolder named
`etc` will not be changed after an upgrade of the plugin. That is, the
content of the folder `html/etc` will be completely untouched by
upgrades. This feature allows to provide an initial configuration (say
in `html/etc/config.js`) with some default settings which can be
overridden by the customer.

The webapp is then served by the same web server (jetty) that serves
openBIS. The name of the webapp defines the URL used to access it. See
the example below. The file index.html is used as a welcome page if the
user does not specifically request a particular page.

```{warning}
An openBIS webapp is *not* a J2EE webapp. It has more in common with an app for mobile devices.
```
 

### Example

This is an example of a webapp. In a real webapp, the name of the webapp
can be any valid folder name. The same goes for the folder in the webapp
containing the the code. The name of the webapp folder is what is used
to define the URL. The name of the folder containing the code is neither
shown nor available to the user.

#### Directory Structure

-   \[module\]  
    -   \[version\]  
        -   `as`  
            -   `webapps`  
                -   example-webapp  
                    -   `plugin.properties`
                    -   html  
                        -   `index.html`
                -   fun-viewer  
                    -   `plugin.properties`
                    -   html  
                        -   code
                        -   `index.html`

#### plugin.properties

    # The properties file for an example webapps plugin
    # This file has no properties defined because none need to be defined.
    webapp-folder = html

#### URL

If openBIS is served at the URL <https://my.domain.com:8443/openbis>,
the above webapps will be available under the following URLs:

-   <https://my.domain.com:8443/openbis/webapp/example-webapp>
-   <https://my.domain.com:8443/openbis/webapp/fun-viewer>  
      

Server Configuration
--------------------

There are two things to consider in the server configuration. The
injection of webapps is done through Jetty, which is the web server we
use for openBIS. If you use the default provided jetty.xml
configuration, then you do not need to do anything extra; if, on the
other hand, you have a custom jetty.xml configuration, then you will
need to update your jetty.xml file to support webapps. 

 

### Jetty Configuration

If your openBIS server has a custom jetty.xml file, you will need to
modify the file to include support for injecting web apps. To do this,
you will need to replace
org.eclipse.jetty.deploy.providers.WebAppProvider by
ch.systemsx.cisd.openbis.generic.server.util.OpenbisWebAppProvider in
`addAppProvider` call to your jetty.xml.

**jetty.xml**

```xml
<Call name="addBean">
    <Arg>
    <New id="DeploymentManager" class="org.eclipse.jetty.deploy.DeploymentManager">
        <Set name="contexts">
        <Ref id="Contexts" />
        </Set>
        <Call name="addAppProvider">
        <Arg>
            <New class="ch.systemsx.cisd.openbis.generic.server.util.OpenbisWebAppProvider">
            <Set name="monitoredDir"><Property name="jetty.home" default="." />/webapps</Set>
            <Set name="scanInterval">0</Set>
            <Set name="extractWars">true</Set>
            </New>
        </Arg>
        </Call>
    </New>
    </Arg>
</Call>
```


Embedding webapps in the OpenBIS UI
-----------------------------------

#### Introduction

Webapps can be used as both standalone applications as well as can be
embedded in the OpenBIS web UI. Standalone webapps are built to
completely replace the original OpenBIS web interface with customer
adjusted layout and functionality. Users of the standalone webapps are
usually completely unaware of the default OpenBIS look and feel. The
webapp itself provides them with all the functionality they need: login
pages, web forms, searches, images, charts etc. The standalone webapp is
a right choice when you want to build a very specific and fully featured
web interface from scratch. If you want to use the default OpenBIS UI
but extend it with some custom functionality then embedding a webapp in
the OpenBIS UI is probably a way to go. To make a webapp visible as a
part of the default OpenBIS UI you have to define where the webapp
should be shown using "openbisui-contexts" property. Moreover some of
the contexts also require additional information describing when the
webapp should be shown. For instance, to embed a webapp in the
experiment details view that will be displayed for experiments with type
"MY\_EXPERIMENT\_TYPE" your plugin.properties file should look like:

**plugin.propeties**

    webapp-folder = html
    openbisui-contexts = experiment-details-view
    experiment-entity-types = MY_EXPERIMENT_TYPE

#### Configuring embedded webapps

A full list of supported properties is presented below.

|Property Key           |Description                                                                                                                                     |Allowed values                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|openbisui-contexts     |Place where the webapp is shown in the OpenBIS UI.                                                                                              |modules-menu <ul><li>webapp is an item in the modules top menu</li></ul> experiment-details-view <ul><li>webapp is a tab in the experiment details view</li><li>requires experiment-entity-types to be defined</li></ul> sample-details-view <ul><li>webapp is a tab in the sample details view</li><li>requires sample-entity-types to be defined</li></ul> data-set-details-view <ul><li>webapp is a tab in the data set details view </li><li>requires data-set-entity-types to be defined</li></ul> material-details-view <ul><li>webapp is a tab in the material details view</li><li>requires material-entity-types to be defined</li></ul> Accepts a comma separated list of values with regular expressions, e.g. "modules-menu, .*-details-view"|
|label                  |The label. It will be shown in the GUI.                                                                                                         |String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
|sorting                |Sorting of the webapp. Webapps are sorted by "sorting" and "folder name" ascending with nulls last (webapps without sorting are presented last).|Integer                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|experiment-entity-types|Types of experiments the webapp should be displayed for.                                                                                        |Accepts a comma separated list of values with regular expressions, e.g. "TYPE_A_1, TYPE_A_2, TYPE_B_.*"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|sample-entity-types    |Types of samples the webapp should be displayed for.                                                                                            |Accepts a comma separated list of values with regular expressions, e.g. "TYPE_A_1, TYPE_A_2, TYPE_B_.*"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|data-set-entity-types  |Types of data sets the webapp should be displayed for.                                                                                          |Accepts a comma separated list of values with regular expressions, e.g. "TYPE_A_1, TYPE_A_2, TYPE_B_.*"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|material-entity-types  |Types of materials the webapp should be displayed for.                                                                                          |Accepts a comma separated list of values with regular expressions, e.g. "TYPE_A_1, TYPE_A_2, TYPE_B_.*"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |


#### Creating embedded webapps

Embedded webapps similar to the standalone counterparts are HTML5
applications that interact with OpenBIS. Because embedded webapps are
shown inside the OpenBIS UI they have access to additional information
about the context they are displayed in. For instance, a webapp that is
displayed in the experiment-details-view context knows that it is
displayed for an experiment entity, with a given type, identifier and
permid. Having this information the webapp can adjust itself and display
only data related to the currently chosen entity. Apart from the entity
details, a webapp also receives a current sessionId that can be used for
calling OpenBIS JSON RPC services. This way embedded webapps can reuse a
current session that was created when a user logged in to the OpenBIS
rather than provide their own login pages for authentication. A sample
webapp that makes use of this context information is presented below:

**webapp.html**

```html
<html>
<head>
    <!-- include jquery library required by the openbis.js -->
    <script src="/openbis/resources/js/jquery.js"></script>
    <!-- include openbis library to gain access to the openbisWebAppContext and openbis objects -->
    <script src="/openbis/resources/js/openbis.js"></script>
</head>
<body>


<div id="log"></div>


<script>
    $(document).ready(function(){


        // create a context object to access the context information
        var c = new openbisWebAppContext();
        $("#log").append("SessionId: " + c.getSessionId() + "<br/>");
        $("#log").append("EntityKind: " + c.getEntityKind() + "<br/>");
        $("#log").append("EntityType: " + c.getEntityType() + "<br/>");
        $("#log").append("EntityIdentifier: " + c.getEntityIdentifier() + "<br/>");
        $("#log").append("EntityPermId: " + c.getEntityPermId() + "<br/>");


        // create an OpenBIS facade to call JSON RPC services
        var o = new openbis();


        // reuse the current sessionId that we received in the context for all the facade calls
        o.useSession(c.getSessionId());

        // call one of the OpenBIS facade methods
        o.listProjects(function(response){
            $("#log").append("<br/>Projects:<br/>"); 
            $.each(response.result, function(index, value){
                    $("#log").append(value.code + "<br/>");  
            });
        });
    });

</script>
</body>
</html>
```


#### Linking to subtabs of other entity detail views

A link from a webapp to an entity subtab looks like this:

    <a href="#" onclick="window.top.location.hash='#entity=[ENTITY_KIND]&permId=[PERM_ID]&ui-subtab=[SECTION];return false;">Link Text</a>

, for example

    <a href="#" onclick="window.top.location.hash='#entity=EXPERIMENT&permId=20140716095938913-1&ui-subtab=webapp-section_test-webapp;return false;">Experiment webapp</a>

ENTITY\_KIND = 'EXPERIMENT' / 'SAMPLE' / 'DATA\_SET' / 'MATERIAL'

PERM\_ID = Entity permid

SECTION = Subtab identifier.

Notes about subtab identifiers:

-   The valid subtab identifiers can be found from
    ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator.java
-   Managed property subtab identifiers are of format
    'managed\_property\_section\_\[MANAGED\_PROPERTY\_TYPE\_CODE\]'
-   Webapp subtab identifiers are of format
    'webapp-section\_\[WEBAPP\_CODE\]' (webapp code is a name of the
    webapp core-plugin folder, i.e.
    \[technology\]/\[version\]/as/webapps/\[WEBAPP\_CODE\])

Cross communication openBIS > DSS
------------------------------------

### Background

Sometimes is required for a web app started in openBIS to make a call to
the DSS. This happens often to upload files or navigate datasets between
others.

Making calls to different domains is forbidden by the web security
sandbox and a common client side issue.

To make the clients accept the responses without additional
configuration by the users, the server should set a special header
"Access-Control-Allow-Origin" on the response when accessing from a
different domain or port.

### Default Configuration

This is done automatically by the DSS for any requests coming from well
known openBIS web apps.

A well known openBIS web app is a web app running using the same URL
configured for openbis on the DSS service.properties.

**DSS service.properties**

    # The URL of the openBIS server
    server-url = https://sprint-openbis.ethz.ch:8446

Even if the web app is accessible from other URLs, not using the URL
configured on the DSS service.properties will lead to the DSS not
recognizing the app.

```{warning}
As a consequence the DSS will not set the necessary header and the client will reject the responses.
```

### Basic Configuration

This is required very often in enterprise environments where the
reachable openBIS URL is not necessarily the one configured on the DSS
service.properties.

Is possible to add additional URLS configuring the AS
service.properties.

**AS service.properties**

    trusted-cross-origin-domains= https://195.176.122.56:8446

The first time the DSS will need to check the valid URLs after a start
up will contact the AS to retrieve the additional trusted domain list.

### Advanced Configuration

A very typical approach is to run both the AS and DSS on the same port
using a reverse proxy like Apache or NGNIX. This way the web security
sandbox is respected. On this case the "Access-Control-Allow-Origin"
header is unnecessary and will also work out of the box.

```{warning}
Even with this configuration, sometimes happens that a web app call the DSS using an auto detected URL given by openBIS. This auto detected URL not necessarily respects your proxy configuration, giving a different port or hostname to the DSS.
```

On this case you will need to solve the problems with one of the methods
explained above or modify your web app.

Embedding openBIS Grids in Web Apps
-----------------------------------

Users of openBIS will have encountered the advanced and powerful table
views used in the application. These views allow for sorting and
filtering. It is possible to take advantage of these views in custom web
UIs.

### Requirements

It is possible to use openBIS table views in a web UI when the data for
the table comes from an aggregation service. The parameters to the
aggregation service are passed as URL query parameters, thus an
additional requirement is that all the aggregation service parameters
can be passed this way. A final requirement is that the web UI be
exposed as an embedded webapp (this is necessary because of the way
openBIS keeps track of the user of the system). If these requirements
are met, then it will be possible to embed an openBIS table view display
the aggregation service data in a web UI.

### Use

To embed a table view, add an iframe to the web UI. The URL of the
iframe should have the following form:

    {openbis url}?viewMode=GRID#action=AGGREGATION_SERVICE&serviceKey={aggregation service key}&dss={data store server code}[& gridSettingsId][& gridHeaderText][& service parameters]

Parameters:

|Parameter|Description|Required|
|--- |--- |--- |
|serviceKey|An aggregation service that will be used for generating the data for the grid.|true|
|dss|A code of a data store that will be used for generating the data for the grid.|true|
|gridSettingsId|An identifier of the grid that will be used for storing the grid settings (visibility of columns, sorting, filters etc.). If not specified then the serviceKey parameter is used.|false|
|gridHeaderText|A header of the grid. If not specified then the header is not shown.|false|

Example:

[http://localhost:8888/openbis-test/index.html?viewMode=GRID\#action=AGGREGATION\_SERVICE&serviceKey=sp-233&dss=standard&gridSettingsId=myTestGridSettingsId&gridHeaderText=myTestGridHeaderText&name=hello](http://localhost:8888/openbis-test/index.html?viewMode=GRID#action=AGGREGATION_SERVICE&serviceKey=sp-233&dss=standard&name=hello)

Full Example

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8" />
    <title>Embedded Grid Example</title>
</head>
<body>
<iframe src="http://localhost:8888/openbis-test/index.html?viewMode=GRID#action=AGGREGATION_SERVICE&serviceKey=sp-233&dss=standard&gridSettingsId=myTestGridSettingsId&gridHeaderText=myTestGridHeaderText&name=hello" width="100%" height="95%" style="border: none">
</body>
</html>
```


Image Viewer component
----------------------

Image viewer screenshot:

![image info](img/473.png)

Example usage of the image viewer component:

```html
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Image Viewer Example</title>

<link rel="stylesheet" href="/openbis/resources/lib/bootstrap/css/bootstrap.min.css">
<link rel="stylesheet" href="/openbis/resources/lib/bootstrap-slider/css/bootstrap-slider.min.css">
<link rel="stylesheet" href="/openbis/resources/components/imageviewer/css/image-viewer.css">

<script type="text/javascript" src="/openbis/resources/config.js"></script>
<script type="text/javascript" src="/openbis/resources/require.js"></script>

</head>
<body>
    <script>
         
        // ask for jquery library, openbis-screening facade and the image viewer component
        require([ "jquery", "openbis-screening", "components/imageviewer/ImageViewerWidget" ], function($, openbis, ImageViewerWidget) {

            $(document).ready(
                    function() {
                        var facade = new openbis();
                        facade.login("admin", "password", function(response) {
 
                            // create the image viewer component for the specific data sets
                            var widget = new ImageViewerWidget(facade, [ "20140513145946659-3284", "20140415140347875-53", "20140429125231346-56",
                                    "20140429125614418-59", "20140506132344798-146" ]);


                            // do the customization once the component is loaded
                            widget.addLoadListener(function() {
                                var view = widget.getDataSetChooserWidget().getView();


                                // example of how to customize a widget
                                view.getDataSetText = function(dataSetCode) {
                                    return "My data set: " + dataSetCode;
                                };


                                // example of how to add a change listener to a widget
                                widget.getDataSetChooserWidget().addChangeListener(function(event) {
                                    console.log("data set changed from: " + event.getOldValue() + " to: " + event.getNewValue());
                                });
                            });


                            // render the component and add it to the page
                            $("#container").append(widget.render());
                        });
                    });
        });
    </script>

    <div id="container" style="padding: 20px"></div>

</body>
</html>
```

