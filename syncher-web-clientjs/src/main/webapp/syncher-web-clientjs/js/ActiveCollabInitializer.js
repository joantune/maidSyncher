define([
        'knockback',
        'knockout',
        'libs/async',
        'AppUtils',
        'bennu-knockout',
        'bootstrap',
        'bennu-knockout'
        ], function(
        kb, ko, async, AppUtils, bennuKo, Bootstrap,  bennuKo) {

    function ActiveCollabInitializer(navbarViewModel, templateName) {
        var self = this;

        if (!self.activeCollabViewModel) {
            self.activeCollabViewModel = {};
        }
        bennuKo.loadPage('mainContent', self.activeCollabViewModel, 'activeCollab');
        navbarViewModel.selectedId(templateName);
        navbarViewModel.showTimestamp(false);

    };

    return ActiveCollabInitializer;

});