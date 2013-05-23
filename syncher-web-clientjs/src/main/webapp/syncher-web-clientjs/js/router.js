define([
    'jquery',
    'underscore',
    'mustache',
    'backbone',
    'marionette',
    'app'
], function($, _, Mustache, Backbone, Marionette, App) {

    var Router = Backbone.Marionette.AppRouter.extend({

        initialize: function() {
           
        },
        
        appRoutes: {
    		"" : "showHome"
    	},
    	
    	controller: {

    		showHome : function() {
    			require(['views/Home'], function(HomeView) {
    				App.page.show(new HomeView());
    			});
    		}
    	}
    });
    
    Bankai.setRouter(Router);
    
});
