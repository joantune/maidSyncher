define([ 'backboneRelational', 'models/SyncLogConflictRelational' ], function(Backbone, SyncLogConflictRelational) {
    Backbone.Collection.extend({
        parse : function(response) {
            return response.conflicts;
        },
        model : SyncLogConflictRelational
    });
});