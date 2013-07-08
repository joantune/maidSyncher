var syncLogs = new SyncLogsRelational();

var spinner = null;
var displayLoadingScreen = function(turnOn) {
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
        if (spinner == null) {
            spinner = new Spinner(opts);
        }
        spinner.spin(target);
    } else {
        spinner.stop();
        $.unblockUI({})

    }
};

syncLogs.fetch({
    success : function() {
        var remainingEvents = new RemainingSyncEvents();
        remainingEvents.fetch();

        function SyncLogsViewModel(model) {
            self = this;

            // self.syncLogsAll = kb.observable(model, 'synclogs');
            self.hasItems = function(item) {
                return item.actions().length > 0 || item.warnings().length > 0 || item.conflicts().length > 0;
            };

            self.syncLogsAll = kb.collectionObservable(syncLogs, {
                models_only : true
            });
            self.currentPageObs = ko.observable(1);
            // self.filter_fn = ko.computed(function() {
            // return function(model, huh) {
            // return true;
            // }
            // });
            // self.syncLogs = kb.collectionObservable(syncLogs, {
            // view_model : LogViewModel
            // filters: self.filter_fn
            // });

//            self.syncLogs = ko.computed(function() {
//                var itemsPerPage = 10;
//                var firstItemIndex = itemsPerPage * (self.currentPageObs() - 1);
//                if (self.syncLogsAll() == null) {
//                    return ko.observableArray([]);
//                } else {
//                    var slicedCollection = self.syncLogsAll().slice(firstItemIndex,
//                            firstItemIndex + itemsPerPage);
//                    var slicedArray = ko.observableArray();
//                    slicedCollection.forEach(function(item) {
//                        slicedArray.push(new LogViewModel(item));
//                    });
//                    return slicedArray();
//                }
//            });

            self.hideSyncs = ko.observable(false);

            self.remainingSyncEvents = kb.collectionObservable(remainingEvents);
            self.selectedSyncLog = ko.observable(null);
            self.goToLogDetails = function(currentSyncLog) {
                displayLoadingScreen(true);
                async.parallel({
                    actions : function(callback) {
                        currentSyncLog.model.get('actions').fetch({
                            success : function(argument) {
                                callback(null, argument)
                            },
                            error : function(argument) {
                                callback(argument, null)
                            }
                        });
                    },
                    warnings : function(callback) {
                        currentSyncLog.model.get('warnings').fetch({
                            success : function(argument) {
                                callback(null, argument)
                            },
                            error : function(argument) {
                                callback(argument, null)
                            }
                        });
                    }
                }, function(err, results) {
                    displayLoadingScreen(false);
                    if (err === undefined || err === null) {
                        syncLogsViewModel.selectedSyncLog(currentSyncLog);
                        currentSyncLog.ghPopoverEnabler($('#ghSyncTime'));
                        currentSyncLog.acPopoverEnabler($('#acSyncTime'));
                        $('#details').modal('show');
                    } else {
                        alert('there was an error communicating with the server');
                    }
                });

            };
        }
        ;

        var syncLogsViewModel = new SyncLogsViewModel();
        $(function() {
            ko.applyBindings(syncLogsViewModel, $('#content')[0]);
            //            
            // syncLogsViewModel['pager$_pager']['currentPage'].subscribe(function(){
            // alert('blah');
            //
            // });
            // $('#ghSyncTime').on('shown');
            $('#details').on('shown', function() {
                // $('#ghSyncTime').popover();
            });
        });
    }
});

var successUtil = function(toReturnOnSuccess, toReturnOnFailure, toReturnOnNotSet, success) {
    if (success === null) {
        return toReturnOnNotSet;
    } else if (success)
        return toReturnOnSuccess;
    else
        return toReturnOnFailure;
};

var calculateDurationInSeconds = function(endTime, startTime) {
    var end = moment(endTime);
    var start = moment(startTime);
    return end.diff(start, 'seconds', true);
}

var ActionsViewModel = function(model) {

    var self = this;

    this.success = kb.observable(model, 'success');
    this.rowClass = ko.computed(function() {
        return successUtil("success", "error", "", self.success());
    });

    this.changedDescriptors = kb.observable(model, 'changedDescriptors');

    this.actionOriginPopoverContent = ko.computed(function() {
        if (self.changedDescriptors() != null) {
            var descriptorsStrings = "";
            self.changedDescriptors().forEach(function(value) {
                descriptorsStrings += value + ", ";
            })
            // to trim the last ", "
            descriptorsStrings = descriptorsStrings.substring(0, descriptorsStrings.length - 2);

            return "<p><small><strong>Changed descriptors:</strong> " + descriptorsStrings + "</small></p>"
                    + "<p><small><a href=\"" + self.urlOriginObject() + "\" target='_blank'>Link</a></small></p>";
        } else
            return "";
    });

    this.syncEndTime = kb.observable(model, 'syncEndTime');
    this.syncStartTime = kb.observable(model, 'syncStartTime');

    this.typeOriginObject = kb.observable(model, 'typeOriginObject');
    this.urlOriginObject = kb.observable(model, 'urlOriginObject');

    this.duration = ko.computed(function() {
        if (self.syncEndTime() != null && self.syncStartTime() != null) {
            return calculateDurationInSeconds(self.syncEndTime(), self.syncStartTime());
        }
        return null;
    });

    this.trimmedTypeOriginObject = ko.computed(function() {
        var toReturn = self.typeOriginObject();
        if (toReturn) {
            firstChar = self.typeOriginObject().lastIndexOf('.') + 1;
            if (firstChar > 0) {
                toReturn = toReturn.substring(firstChar);
            }
        }
        return toReturn;
    });

    this.successIcon = ko.computed(function() {
        var successIcon = '<i class="icon-ok"></i>';
        var failureIcon = '<i class="icon-remove"></i>';
        return successUtil(successIcon, failureIcon, "", self.success());
    });
};

var WarningsViewModel = function(model) {

    var self = this;

    this.description = kb.observable(model, 'description');
    this.id = kb.observable(model, 'id');

};

var momentify = function(dateString) {
    return moment(dateString)
}

var friendlyDateTime = function(dateTimeString, styleIt) {
    var amoment = moment(dateTimeString);
    if (styleIt) {
        return amoment.format("[<strong>]DD-MM-YYYY[</strong>] @ HH:mm:ss")
    } else {
        return amoment.format("DD-MM-YYYY @ HH:mm:ss")
    }
}
var friendlyNamedDateTime = function(dateTimeString) {
    var amoment = moment(dateTimeString);
    return amoment.format("MMMM Do YYYY, HH:mm:ss")
}

var timeOnly = function(amoment) {
    return moment(amoment).format("HH:mm:ss");
    // return amoment.format("HH:mm:ss")
}

var humanFriendlyFromNow = function(dateTimeString) {
    var amoment = moment(dateTimeString);
    return amoment.fromNow();
}

var SUCCESS = "Success";
var CONFLICT = "Conflict";
var FAILURE = "Failure";
var ONGOING = "Ongoing";

var LogViewModel = function(model) {

    var self = this;

    this.model = model;

    this.NrGeneratedSyncActions = kb.observable(model, 'NrGeneratedSyncActions');
    this.NrGeneratedSyncActionsFromRemainingSyncEvents = kb.observable(model,
            'NrGeneratedSyncActionsFromRemainingSyncEvents');
    this.visible = ko.computed(function() {
        return false;
    });
    this.Status = kb.observable(model, 'Status');
    this.SyncStartTime = kb.observable(model, 'SyncStartTime');
    this.conflicts = kb.collectionObservable(model.get('conflicts'));
    this.actions = kb.collectionObservable(model.get('actions'), {
        view_model : ActionsViewModel
    });
    this.warnings = kb.collectionObservable(model.get('warnings'), {
        view_model : WarningsViewModel
    });

    var popoverOptionsGenerator = function(startTime, endTime) {
        var syncStartTimePrefixHtml = "<small><strong>Sync start time: </strong>";
        var syncEndTimePrefixHtml = "<small><strong>Sync end time: </strong>";
        var smallHtmlTerminator = "</small>";
        return {
            html : true,
            trigger : "hover",
            content : syncStartTimePrefixHtml + timeOnly(startTime) + smallHtmlTerminator + "<br/>"
                    + syncEndTimePrefixHtml + timeOnly(endTime) + smallHtmlTerminator,
        };

    };

    this.ghPopoverEnabler = function(jQueryElement) {
        if (self.SyncGHStartTime && self.SyncGHEndTime) {
            var options = popoverOptionsGenerator(self.SyncGHStartTime(), self.SyncGHEndTime());
            jQueryElement.popover(options);
            return true;
        }
    };

    this.SerializedStackTrace = kb.observable(model, 'SerializedStackTrace');

    this.toggleStackTraceCommand = function() {
        if (self.showStackTraceCommand())
            self.showStackTraceCommand(false);
        else
            self.showStackTraceCommand(true);
    };

    this.showStackTraceCommand = ko.observable(false);

    this.shouldShowStackTrace = ko.computed(function() {
        return self.Status() === FAILURE && self.SerializedStackTrace() && self.showStackTraceCommand();
    });

    this.statusStyle = ko.computed(function() {
        if (self.Status() === FAILURE && self.SerializedStackTrace()) {
            return "popoverStyle";
        }
        return "";
    });

    this.acPopoverEnabler = function(jQueryElement) {
        if (self.SyncACStartTime && self.SyncACEndTime) {
            var options = popoverOptionsGenerator(self.SyncACStartTime(), self.SyncACEndTime());
            options.placement = "top";
            jQueryElement.popover(options);
            return true;
        }
    };

    this.acSyncTimeDuration = ko.computed(function() {
        if (self.SyncACStartTime && self.SyncACEndTime) {
            return calculateDurationInSeconds(self.SyncACEndTime(), self.SyncACStartTime());

        }

    });

    this.ghSyncTimeDuration = ko.computed(function() {
        if (self.SyncGHStartTime && self.SyncGHEndTime) {
            return calculateDurationInSeconds(self.SyncGHEndTime(), self.SyncGHStartTime());

        }

    });

    this.classCss = ko.computed((function() {
        if (self.warnings().length > 0) {
            return "warning";
        }
        var status = self.Status();

        if (status != undefined) {

            if (status.toLowerCase() === SUCCESS.toLowerCase()) {
                return "success";
            } else if (status.toLowerCase() === CONFLICT.toLowerCase()) {
                return "warning";
            } else if (status.toLowerCase() === FAILURE.toLowerCase()) {
                return "error";
            } else if (status.toLowerCase() === ONGOING.toLowerCase()) {
                return "info";
            }
        }
        return "";

    }));

    return this;
};