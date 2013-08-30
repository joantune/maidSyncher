define([
        'knockback',
        'knockout',
        'libs/async',
        'AppUtils',
        'bennu-knockout',
        'bootstrap',
        'SyncLogsInitializer',
        'ActiveCollabInitializer',
        'underscore',
        'jquery',
        'viewModels/SettingsViewModel',
        'bootstrapSwitch'], function(kb, ko, async, AppUtils, bennuKo, $, SyncLogsInitializer, ActiveCollabInitializer, _, $,SettingsViewModel, jQuery) {
    function NavbarViewModel(dateOfFetch) {
        var self = this;

        self.selectedId = ko.observable('syncLogs');

        // making some of the shared variables global.
        if (!window.groups) {
            window.groups = ko.observableArray();
        }
        self.groups = window.groups;

        if (!window.repositoriesToIgnore) {
            window.repositoriesToIgnore = ko.observableArray();
        }
        self.repositoriesToIgnore = window.repositoriesToIgnore;

        if (!window.schedule) {
            window.schedule = ko.observable();
        }
        self.schedule = window.schedule;
        if (!window.syncherRunning) {
            window.syncherRunning = ko.observable();
        }
        self.syncherRunning = window.syncherRunning;

        if (!window.username) {
            window.username = ko.observable();
        }
        self.username = window.username;

        if (!window.casEnabled) {
            window.casEnabled = ko.observable();
        }
        self.casEnabled = window.casEnabled;
        // end of global variables;

        self.loggedIn = ko.computed(function() {
            if (self.username() && self.username() !== null)
                return true;
            return false;

        });

        self.showSettings = function() {
            if (!window.settingsViewModel) {
                window.settingsViewModel = new SettingsViewModel();
            }
            window.settingsViewModel.fetchData(function() {
                // after fetching its data, let's show the
                // modal with the settings
                bennuKo.loadPage('settings', window.settingsViewModel, 'settings');
                jQuery('.make-switch').bootstrapSwitch();
                $('#settingsModal').on('switch-change', function (e, data) {
                    var $el = $(data.el)
                      , value = data.value;
                    window.syncherRunning(value);
                });
                $('#settingsModal').modal('show');

            });

        };

        self.isManager = ko.computed(function() {
            if (self.groups()) {
                var found = _.find(self.groups(), function(group) {
                    if (group.expression) {
                        return "#managers" === group.expression.toLowerCase();
                    }
                    return false;
                });
                if (found)
                    return true;
            }
            return false;
        });

        self.fetchData = function(successCallback, errorCallback) {
            AppUtils.fetchConfiguration(successCallback, errorCallback, self);
        }

        self.login = function(formElement) {
            var user = $(formElement).children("[name='username']").val();
            var pass = $(formElement).children("[name='password']").val();
            $.post("../api/bennu-core/profile/login", {
                username : user,
                password : pass
            }, function(data, textStatus, jqxhr) {
                self.username(user);
                self.fetchData();
            });
        }

        self.logout = function() {
            var logoutUrl = "../api/bennu-core/profile/logout";
            if (self.casEnabled) {
                // logoutUrl = hostJson.logoutUrl;
                // TODO
            }
            $.get(logoutUrl, null, function(data, textStatus, jqxhr) {
                self.username(undefined);
                self.fetchData();
            });

        }

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