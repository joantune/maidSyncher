requirejs.config({
    paths: {
    	less: 'libs/less-1.3.3.min',
    	jquery: 'libs/jquery',
    	bootstrap: 'libs/bootstrap.min',
        // Backbone
        backbone: 'libs/backbone-min',
        underscore: 'libs/underscore',
        knockback: 'libs/knockback-core'
        backbone-relational: 'libs/backbone-relational'
        //moment: 'libs/moment.min'
        //async: 'libs/async'
        
    },
    shim: {
        "underscore": {
            deps: [],
            exports: "_"
        },
        "backbone": {
            deps: ["jquery", "underscore"],
            exports: "Backbone"
        },
    }
});

require(['knockout', 
         'bennu-knockout',
         'collections/TransactionalContextCollection',
         'viewModels/TransactionalContextViewModel',
         'viewModels/NewTxViewModel'], function(ko, bennuKo, TrasactionalContextCollection, TransactionalContextViewModel, NewTxViewModel) {

        var collection = new TrasactionalContextCollection();
        collection.fetch({
            success: function () {
                collection.sort();
                var viewModel = new TransactionalContextViewModel(collection);
                var modalViewModel = new NewTxViewModel(collection);
                bennuKo.loadPage('mainView', viewModel, 'LongTxManagement');
                bennuKo.loadPage('newTxModal', modalViewModel, 'NewTxModal');
            }
        });
});