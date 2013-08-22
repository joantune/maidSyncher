define([
        'knockback',
        'knockout',
        'libs/async',
        'AppUtils',
        'bennu-knockout',
        'bootstrap',
        'SyncLogsInitializer',
        'ActiveCollabInitializer' ], function(
        kb, ko, async, AppUtils, bennuKo, $, SyncLogsInitializer, ActiveCollabInitializer) {
    function NavbarViewModel(dateOfFetch) {
        var self = this;

        self.selectedId = ko.observable('syncLogs');

        self.showTimestamp = ko.observable(false);

        self.apps = ko.observableArray([ {
            templateName : 'syncLogs',
            name : "Sync logs",
            init : SyncLogsInitializer
        }, {
            templateName : 'activeCollab',
            name : "ActiveCollab",
            init : ActiveCollabInitializer
        }, {
            templateName : 'gitHub',
            name : "GitHub",
            init : null
        } ]);

        self.dateLastFetch = dateOfFetch === undefined || dateOfFetch == null ? ko.observable("NA") : ko
                .observable(dateOfFetch);

        self.goToApp = function(element) {
            if (element.init == null || element.init === undefined) {
                return;
            } else
                element.init(self, element.templateName);
        }
    }

    return NavbarViewModel;

});