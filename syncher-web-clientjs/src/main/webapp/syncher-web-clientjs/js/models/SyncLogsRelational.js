define([ 'backboneRelational', 'models/SyncLogRelational' ], function(Backbone, SyncLogRelational) {
    return Backbone.Collection.extend({
        url : '../api/syncher-web-restserver/synclogs',
        model : SyncLogRelational,
        parse : function(response) {
            return response.synclogs;
        }
    });
});