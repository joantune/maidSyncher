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
        'jquery' ], function(kb, ko, async, AppUtils, bennuKo, $, SyncLogsInitializer, ActiveCollabInitializer, _, $) {
    function SettingsViewModel() {
        
        self = this;
        
        self.syncherRunning = window.syncherRunning;
        self.repositoriesToIgnore = window.repositoriesToIgnore;
        self.schedule = window.schedule;
        
        self.groups = window.groups;
        
        self.ghRepositories = ko.observableArray();
        
        self.syncherRunningAux = ko.observable(self.syncherRunning());
        
        self.saveSettings = function() {
            debugger;
            $.ajax({
                type: "POST",
                url: '../api/syncher-web-restserver/configuration/ghRepositoriesToIgnore',
                data: JSON.stringify(self.repositoriesToIgnore()),
                dataType: 'json',
                contentType: 'application/json',
                success : function(output, status, response) {
                    $.ajax({
                       type: "POST",
                       url: '../api/syncher-web-restserver/configuration/syncherTask',
                       data: 'enable='+self.syncherRunning(),
                        success: function(outputSyncherTask, statusSyncherTask, responseSyncherTask) {
                            AppUtils.applyConfigurationData(outputSyncherTask, window);
                            $('#settingsModal').modal('hide');
                        }
                    });
                    
                }
            })
        }
        
        self.fetchData = function(successCallback, errorCallback) {
            //let's fetch the main configuration data
            AppUtils.fetchConfiguration(function() {
                //and then get our stuff
            $.ajax({
                type : "GET",
                url : '../api/syncher-web-restserver/configuration/ghRepositories',
                dataType : 'json',
                success : function(ghRepositories, status, response) {
                    self.ghRepositories.removeAll();
                    AppUtils.pushEachInto(ghRepositories, self.ghRepositories);

                    if (successCallback) {
                        successCallback(ghRepositories, status, response);
                    }

                },
                error : function() {
                    if (errorCallback) {
                        errorCallback(ghRepositories, status, response);
                    }
                }
            });
            }, undefined, window)
            
        };
        

    }
    
    return SettingsViewModel;

});