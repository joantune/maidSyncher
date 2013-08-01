define([ 'backboneRelational', 'backbone' ], function(BackboneRelational, Backbone) {
    return Backbone.Collection.extend({
        parse : function(response) {
            return response.remainingevents;
        },
        url : '../api/syncher-web-restserver/synclogs/remainingevents'

    });
});