define([ 'knockback', 'knockout', 'libs/async', 'AppUtils', 'bennu-knockout', 'bootstrap'], function(kb, ko, async, AppUtils, bennuKo, $) {
    function SyncLogsViewModel(model, remainingEvents) {
        self = this;
        
        
        self.AppUtils = AppUtils;

        // self.syncLogsAll = kb.observable(model, 'synclogs');
        self.hasItems = function(item) {
            return item.actions().length > 0 || item.warnings().length > 0 || item.conflicts().length > 0;
        };

        self.syncLogsAll = kb.collectionObservable(model, {
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

        // self.syncLogs = ko.computed(function() {
        // var itemsPerPage = 10;
        // var firstItemIndex = itemsPerPage * (self.currentPageObs() - 1);
        // if (self.syncLogsAll() == null) {
        // return ko.observableArray([]);
        // } else {
        // var slicedCollection = self.syncLogsAll().slice(firstItemIndex,
        // firstItemIndex + itemsPerPage);
        // var slicedArray = ko.observableArray();
        // slicedCollection.forEach(function(item) {
        // slicedArray.push(new LogViewModel(item));
        // });
        // return slicedArray();
        // }
        // });

        self.hideSyncs = ko.observable(false);

        self.remainingSyncEvents = kb.collectionObservable(remainingEvents);
        self.selectedSyncLog = ko.observable(null);
        self.goToLogDetails = function(currentSyncLog) {
            AppUtils.displayLoadingScreen(true);
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
                },
                conflicts : function(callback) {
                    currentSyncLog.model.get('conflicts').fetch({
                        success : function(argument) {
                            callback(null, argument)
                        },
                        error : function(argument) {
                            callback(argument, null)
                        }
                    });
                }
            }, function(err, results) {
                AppUtils.displayLoadingScreen(false);
                if (err === undefined || err === null) {
                    self.selectedSyncLog(currentSyncLog);
                    bennuKo.loadPage('syncLogDetails', self, 'syncLogDetails');
                    currentSyncLog.ghPopoverEnabler($('#ghSyncTime'));
                    currentSyncLog.acPopoverEnabler($('#acSyncTime'));
                    $('#details').modal('show');
                } else {
                    alert('there was an error communicating with the server');
                }
            });

        };
    };
    return SyncLogsViewModel;
});