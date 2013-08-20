define([ 'knockback', 'knockout', 'libs/async', 'AppUtils', 'bennu-knockout', 'bootstrap'], function(kb, ko, async, AppUtils, bennuKo, $) {
    function NavbarViewModel(dateOfFetch) {
        self = this;
        
        self.dateLastFetch = dateOfFetch === undefined || dateOfFetch == null ? "NA" : dateOfFetch;
    }
    
    return NavbarViewModel;

});