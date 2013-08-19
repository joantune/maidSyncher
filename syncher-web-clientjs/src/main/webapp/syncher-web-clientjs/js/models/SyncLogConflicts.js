define([ 'backboneRelational', 'models/SyncLogConflictRelational' ], function(Backbone, SyncLogConflictRelational) {
    return Backbone.Collection.extend({
        parse : function(response) {
            return response.conflicts;
        },
        model : SyncLogConflictRelational
    });
});