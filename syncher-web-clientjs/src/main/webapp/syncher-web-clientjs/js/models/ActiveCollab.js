
define(['backboneRelational'], function(Backbone) {
	return Backbone.Model.extend({
	    urlRoot: "../api/syncher-web-restserver/activeCollab",
	    id: "instance"
	});
});