var DropboxMonitorUtil = new function() {
    this.iconizeStatus = function(status) {
        var icon;
        if (status === "0" || status === "succeded") {
          icon = $("<i>");
          icon.addClass("fa fa-smile-o fa-lg one");
        } else if (status === "1" || status === "failed") {
          icon = $("<i>");
          icon.addClass("fa fa-frown-o fa-lg zero");
        } else if (status === "process") {
            icon = $("<i>");
            icon.addClass("fa fa-spinner");
        } else {
          icon = "";
        }
        return icon;
    };


}