requirejs.config({
    paths : {
        less : 'libs/less-1.3.3.min',
        jquery : 'libs/jquery',
        bootstrap : 'libs/bootstrap.min',
        // Backbone
        backbone : 'libs/backbone-min',
        underscore : 'libs/underscore',
        knockback : 'libs/knockback-core',
        backboneRelational : 'libs/backbone-relational',
        jqueryBlockUi : 'libs/jquery.blockUI'

    // moment: 'libs/moment.min'
    // async: 'libs/async'

    },
    shim : {
        "bootstrap" : {
            deps : ["jquery"],
            exports : "$"
        },
        "underscore" : {
            deps : [],
            exports : "_"
        },
//        "knockback" : {
//          deps: ["backboneRelational", "knockout"],
//          exports: "kb"
//        },
        "backboneRelational" : {
            deps : [ "backbone"],
            exports : "Backbone"
        },
        "backbone" : {
            deps : [ "jquery", "underscore" ],
            exports : "Backbone"
        },
        "jqueryBlockUi" : {
            deps : [ "jquery" ],
            exports : "$.blockUI"
        }
    }
});

require([ 'knockout', 'bennu-knockout', 'viewModels/SyncLogsViewModel', 'models/RemainingSyncEvents',
        'models/SyncLogsRelational','viewModels/LogViewModel', 'viewModels/NavbarViewModel', 'AppUtils', 'SyncLogsInitializer' ],
        function(ko, bennuKo, SyncLogsViewModel, RemainingSyncEvents, SyncLogsRelational, LogViewModel, NavbarViewModel, AppUtils, SyncLogsInitializer) {
    // --
    ko.bindingHandlers.bootstrapPopover = {
            init: function(element, valueAccessor, allBindingsAccessor, viewModel) {
                var options = valueAccessor();
                var defaultOptions = {};
                options = $.extend(true, {}, defaultOptions, options);
                $(element).popover(options);
            }
    };
    
    var idCounter = 0;
    ko.bindingHandlers.pager = {
            init: function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
                var contents = $(element).html();

               
                if (ko.utils.unwrapObservable(valueAccessor()).customPagerId) {
                    var id = ko.utils.unwrapObservable(valueAccessor()).customPagerId;
                }
                else {
                    var id = 'pager' + idCounter++;
                }

                var pagerModel = viewModel[id + '$_pager'] = {};

                var itemsPerPage = ko.utils.unwrapObservable(valueAccessor()).itemsPerPage || 10;
                var currentPageObs = pages = ko.observable(1);
                pagerModel['currentPage'] = currentPageObs;
                pagerModel['pagedArray'] = ko.computed(function() {
                    var collection = ko.utils.unwrapObservable(valueAccessor()).array;
                    var firstItemIndex = itemsPerPage * (currentPageObs() -1);
                    var slicedCollection = collection.slice(firstItemIndex, firstItemIndex + itemsPerPage);
                    if (ko.utils.unwrapObservable(valueAccessor()).viewModel) {
                        var _ViewModelConstructor = require(ko.utils.unwrapObservable(valueAccessor()).viewModel);
                        var slicedArrayObs = ko.observableArray();
                        slicedCollection.forEach(function(item) {
                            slicedArrayObs.push(new _ViewModelConstructor(item));
                        } );
                        return slicedArrayObs();
                    }
                    else {
                        return slicedCollection;
                    }
                });
                var numPages = ko.computed(function() {
                    return Math.ceil(ko.utils.unwrapObservable(valueAccessor()).array().length / itemsPerPage);
                });
                pagerModel['next'] = function() {
                    if(currentPageObs() < numPages()) {
                        currentPageObs(currentPageObs() + 1);
                    }
                }
                pagerModel['previous'] = function() {
                    if(currentPageObs() > 1) {
                        currentPageObs(currentPageObs() - 1);
                    }
                }
                pagerModel['numPages'] = numPages;

                $(element).html(' <!-- ko foreach: ' + id+ '$_pager.pagedArray -->' + contents + '<!-- /ko -->');

                $(element).append('<div data-bind="with: ' + id +'$_pager">\
                    <div class="pagination pagination-small">\
                        <ul>\
                            <li data-bind="css: { disabled: currentPage() == 1 }"><a href="#" data-bind="click: previous">&laquo;</a></li>\
                            <li data-bind="css: { disabled: currentPage() == numPages() || numPages() == 0 }"><a href="#" data-bind="click: next">&raquo;</a></li>\
                        </ul>\
                    </div></div>');
            }
        };
    //--
    var navbarViewModel = new NavbarViewModel();
    bennuKo.loadPage('navbar', navbarViewModel, 'navbar');
    SyncLogsInitializer(navbarViewModel,'syncLogs');
    

            //		 
            // var collection = new TrasactionalContextCollection();
            // collection.fetch({
            // success: function () {
            // collection.sort();
            // var syncLogsViewModel = new SyncLogsViewModel();
            // var viewModel = new TransactionalContextViewModel(collection);
            // var modalViewModel = new NewTxViewModel(collection);
            // bennuKo.loadPage('syncLogs', viewModel, 'LongTxManagement');
            // bennuKo.loadPage('newTxModal', modalViewModel, 'NewTxModal');
            // }
            // });
        });