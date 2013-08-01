define([ 'backboneRelational', 'models/SyncLogWarningRelational' ], function(Backbone, SyncLogWarningRelational) {
    return Backbone.Collection.extend({
        parse : function(response) {
            return response.warnings;
        },
        model : SyncLogWarningRelational
    });
});
