define([ 'backboneRelational', 'models/SyncLogActionRelational' ], function(Backbone, SyncLogActionRelational) {
    return Backbone.Collection.extend({
        parse : function(response) {
            return response.actions;
        },
        model : SyncLogActionRelational
    });
});
