define(['jquery','jqueryBlockUi'], function($) {
var AppUtils = {
        
self : this,
spinner : null,

displayLoadingScreen : function(turnOn) {
    if (turnOn) {
        var opts = {
            lines : 13, // The number of lines to draw
            length : 7, // The length of each line
            width : 4, // The line thickness
            radius : 10, // The radius of the inner circle
            rotate : 0, // The rotation offset
            color : '#000', // #rgb or #rrggbb
            speed : 1, // Rounds per second
            trail : 60, // Afterglow percentage
            shadow : false, // Whether to render a shadow
            hwaccel : false, // Whether to use hardware acceleration
            className : 'spinner', // The CSS class to assign to the spinner
            zIndex : 2e9, // The z-index (defaults to 2000000000)
        // top: 'auto', // Top position relative to parent in px
        // left: 'auto' // Left position relative to parent in px
        };
        $.blockUI({
            message : $('#pleaseWait'),
            // TODO comment the timeout
            // timeout: 2000
            fadeIn : 1000,
            css : {
                top : '20%'
            }
        });
        var target = document.getElementById('pleaseWait');
        if (self.spinner == null) {
            self.spinner = new Spinner(opts);
        }
        self.spinner.spin(target);
    } else {
        self.spinner.stop();
        $.unblockUI({})

    }
},

successUtil : function(toReturnOnSuccess, toReturnOnFailure, toReturnOnNotSet, success) {
    if (success === null) {
        return toReturnOnNotSet;
    } else if (success)
        return toReturnOnSuccess;
    else
        return toReturnOnFailure;
},

calculateDurationInSeconds : function(endTime, startTime) {
    var end = moment(endTime);
    var start = moment(startTime);
    return end.diff(start, 'seconds', true);
},

TypeOfChangeEventAbbreviator : function(koObsWithType) {
    if (koObsWithType !== undefined && koObsWithType() != null)
        return "(" + koObsWithType().charAt(0) + ")";
    else
        return "";
},

ClassNameTrimmer : function(className) {
    if (className) {
        firstChar = className.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            className = className.substring(firstChar);
        }
    }
    return className;
},

StringArrayToString : function(stringArray) {
    var descriptorsStrings = "";
    if (stringArray && stringArray != null) {
        stringArray.forEach(function(value) {
            descriptorsStrings += value + ", ";
        });
        // to trim the last ", "
        descriptorsStrings = descriptorsStrings.substring(0, descriptorsStrings.length - 2);
    }
    return descriptorsStrings;
},




momentify : function(dateString) {
    return moment(dateString)
},

friendlyDateTime : function(dateTimeString, styleIt) {
    var amoment = moment(dateTimeString);
    if (styleIt) {
        return amoment.format("[<strong>]DD-MM-YYYY[</strong>] @ HH:mm:ss")
    } else {
        return amoment.format("DD-MM-YYYY @ HH:mm:ss")
    }
},
friendlyDateTimeNow : function(styleIt) {
    var amoment = moment();
    if (styleIt) {
        return amoment.format("[<strong>]DD-MM-YYYY[</strong>] @ HH:mm:ss")
    } else {
        return amoment.format("DD-MM-YYYY @ HH:mm:ss")
    }
},
friendlyNamedDateTime : function(dateTimeString) {
    var amoment = moment(dateTimeString);
    return amoment.format("MMMM Do YYYY, HH:mm:ss")
},

timeOnly : function(amoment) {
    return moment(amoment).format("HH:mm:ss");
    // return amoment.format("HH:mm:ss")
},

humanFriendlyFromNow : function(dateTimeString) {
    var amoment = moment(dateTimeString);
    return amoment.fromNow();
},

SUCCESS : "Success",
CONFLICT : "Conflict",
FAILURE : "Failure",
ONGOING : "Ongoing"
};
return AppUtils;
});
