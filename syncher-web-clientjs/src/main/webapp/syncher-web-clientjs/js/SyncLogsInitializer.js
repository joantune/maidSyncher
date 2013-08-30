define([ 'knockback', 'knockout', 'libs/async', 'AppUtils', 'bennu-knockout', 'bootstrap', 'models/SyncLogsRelational', 'bennu-knockout', 'models/RemainingSyncEvents', 'viewModels/SyncLogsViewModel'], 
        function(kb, ko, async, AppUtils, bennuKo, Bootstrap, SyncLogsRelational, bennuKo, RemainingSyncEvents, SyncLogsViewModel) {
    
    function SyncLogsInitializer(navbarViewModel,  templateName) {
        var self = this;
        
        if (!self.syncLogs) {
            self.syncLogs = new SyncLogsRelational();
        }
        
        syncLogs.fetch({
            success: function() {
                var remainingEvents = new RemainingSyncEvents();
                remainingEvents.fetch();
                
                if (self.syncLogsViewModel) {
                 delete self.syncLogsViewModel;
                }
                self.syncLogsViewModel = new SyncLogsViewModel(syncLogs, remainingEvents, navbarViewModel, SyncLogsInitializer, templateName);
                var currentTime = AppUtils.friendlyDateTimeNow(true);
                bennuKo.loadPage('mainContent', self.syncLogsViewModel, 'syncLogs');
                navbarViewModel.selectedId(templateName);
                navbarViewModel.dateLastFetch(currentTime);
                navbarViewModel.showTimestamp(true);
                
                
            }
        });
        
    };
    
    return SyncLogsInitializer;

});