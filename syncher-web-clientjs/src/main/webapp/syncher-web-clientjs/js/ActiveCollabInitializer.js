define([
        'knockback',
        'knockout',
        'libs/async',
        'AppUtils',
        'bennu-knockout',
        'bootstrap',
        'bennu-knockout',
        'models/ActiveCollab' ], function(kb, ko, async, AppUtils, bennuKo, Bootstrap, bennuKo, ActiveCollab) {

    function ActiveCollabInitializer(navbarViewModel, templateName) {
        var self = this;

        if (!self.activeCollabSingletonModel) {
            self.activeCollabSingletonModel = new ActiveCollab();
        }
        self.activeCollabSingletonModel.fetch({
            success : function() {
                if (!self.activeCollabViewModel) {
                    self.activeCollabViewModel = {};
                }
                var model = self.activeCollabSingletonModel;
                self.activeCollabViewModel.instanceUrl = kb.observable(model, 'url');
                bennuKo.loadPage('mainContent', self.activeCollabViewModel, 'activeCollab');
                navbarViewModel.selectedId(templateName);
                navbarViewModel.showTimestamp(false);

            },
            error : function() {
                return;
            }
        });

    }
    ;

    return ActiveCollabInitializer;

});