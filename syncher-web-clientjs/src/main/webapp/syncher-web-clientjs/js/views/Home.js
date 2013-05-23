define([
    'backbone',
    'marionette',
    'text!templates/Home.html'
], function(Backbone, Marionette, tpl) {
	return Backbone.Marionette.ItemView.extend({
		template: tpl
	});
});
