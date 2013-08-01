
define(['knockback'], function (kb) {
var WarningsViewModel = function(model) {

    var self = this;

    this.description = kb.observable(model, 'description');
    this.id = kb.observable(model, 'id');

};
});
