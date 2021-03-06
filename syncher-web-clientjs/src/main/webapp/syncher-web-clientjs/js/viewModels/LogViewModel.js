define([
        'knockout',
        'knockback',
        'viewModels/ConflictsViewModel',
        'viewModels/ActionsViewModel',
        'viewModels/WarningsViewModel',
        'AppUtils'], function(ko, kb, ConflictsViewModel, ActionsViewModel, WarningsViewModel, AppUtils) {
    var LogViewModel = function(model) {

        
        var self = this;
        
        self.AppUtils = AppUtils;

        this.model = model;
        
        this.SyncGHStartTime = kb.observable(model, 'SyncGHStartTime');
        this.SyncGHEndTime = kb.observable(model, 'SyncGHEndTime');

        this.NrGeneratedSyncActions = kb.observable(model, 'NrGeneratedSyncActions');
        this.NrGeneratedSyncActionsFromRemainingSyncEvents = kb.observable(model,
                'NrGeneratedSyncActionsFromRemainingSyncEvents');
        this.visible = ko.computed(function() {
            return false;
        });
        this.Status = kb.observable(model, 'Status');
        this.SyncStartTime = kb.observable(model, 'SyncStartTime');
        this.conflicts = kb.collectionObservable(model.get('conflicts'), {
            view_model : ConflictsViewModel
        });
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
                content : syncStartTimePrefixHtml + AppUtils.timeOnly(startTime) + smallHtmlTerminator + "<br/>"
                        + syncEndTimePrefixHtml + AppUtils.timeOnly(endTime) + smallHtmlTerminator,
            };

        };

        this.ghPopoverEnabler = function(jQueryElement) {
            if (self.SyncGHStartTime() != null && self.SyncGHEndTime() != null) {
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
            return self.Status() === AppUtils.FAILURE && self.SerializedStackTrace() && self.showStackTraceCommand();
        });

        this.statusStyle = ko.computed(function() {
            if (self.Status() === AppUtils.FAILURE && self.SerializedStackTrace()) {
                return "popoverStyle";
            }
            return "";
        });

        this.acPopoverEnabler = function(jQueryElement) {
            if (self.SyncACStartTime() != null && self.SyncACEndTime() != null) {
                var options = popoverOptionsGenerator(self.SyncACStartTime(), self.SyncACEndTime());
                options.placement = "top";
                jQueryElement.popover(options);
                return true;
            }
        };
        
        this.SyncACStartTime = kb.observable(model, 'SyncACStartTime');
        this.SyncACEndTime = kb.observable(model, 'SyncACEndTime');

        this.acSyncTimeDuration = ko.computed(function() {
            if (self.SyncACStartTime() != null && self.SyncACEndTime() != null) {
                return AppUtils.calculateDurationInSeconds(self.SyncACEndTime(), self.SyncACStartTime());

            }

        });

        this.ghSyncTimeDuration = ko.computed(function() {
            if (self.SyncGHStartTime() != null && self.SyncGHEndTime() != null) {
                return AppUtils.calculateDurationInSeconds(self.SyncGHEndTime(), self.SyncGHStartTime());

            }

        });
        
        this.is = function(what) {
            var status = self.Status();
            if (status === undefined || what === undefined || what == null)
                return false;
            else return status.toLowerCase() === what.toLowerCase();
        };
        
        
        this.isOngoing = ko.computed(function() {
            return self.is(AppUtils.ONGOING);
        })

        this.classCss = ko.computed((function() {
            if (self.warnings().length > 0) {
                return "warning";
            }
            var status = self.Status();

            if (status != undefined) {

                if (self.is(AppUtils.SUCCESS)) {
                    return "success";
                } else if (self.is(AppUtils.CONFLICT) || self.is(AppUtils.INTERRUPTED)) {
                    return "warning";
                } else if (self.is(AppUtils.FAILURE)) {
                    return "error";
                } else if (self.is(AppUtils.ONGOING)) {
                    return "info";
                }
            }
            return "";

        }));

        return this;
    };

    return LogViewModel;
});