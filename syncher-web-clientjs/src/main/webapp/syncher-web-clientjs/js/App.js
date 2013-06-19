var selfThing = this;
/* The dataTable binding */
(function($) {
    ko.bindingHandlers.dataTable = {
        init : function(element, valueAccessor) {
            var binding = ko.utils.unwrapObservable(valueAccessor());

            // If the binding is an object with an options field,
            // initialise the dataTable with those options.
            if (binding.options) {
                $(element).dataTable(binding.options);
            }

        },
        update : function(element, valueAccessor) {
            var binding = ko.utils.unwrapObservable(valueAccessor());

            // If the binding isn't an object, turn it into one.
            if (!binding.data) {
                binding = {
                    data : valueAccessor()
                }
            }

            // Clear table
            $(element).dataTable().fnClearTable();

            // Rebuild table from data source specified in binding
            $(element).dataTable().fnAddData(binding.data());

            // let's add the custom callback for each row, if it exists
            if (binding.trClick) {
                $(element).find("tr").click(binding.trClick);
            }

            if (binding.rowFunction) {
                $(element).find("tr").each(binding.rowFunction);
            }
        }
    };
})(jQuery);
// var SyncLogDetail = Backbone.RelationalModel.extend({});
//
// var exampleLogSyncOne = new SyncLogDetail({
// id : '1',
// success : 'true',
// nrTotalActions : '1',
// completedActions : [{
// type : 'GITHUB',
// url : 'http://test.com',
// objectId : '12',
// typeOfEvent : 'CREATE',
// description : 'TODO'
// }],
// errorDetail : 'Exception thrown by asdasdasd'
// })
//
// var SyncLog = Backbone.RelationalModel.extend({
// relations : [ {
// type : Backbone.HasOne,
// key : 'syncLogDetails',
// relatedModel : 'SyncLogDetail',
// reverseRelation : {
// key : 'syncLog'
// }
// } ]
// });
//
// var syncLog = new SyncLog({
// dateStart : '20/05/2013 17:00',
// dateFinish : '20/05/2013 17:02',
// success : 'true',
// syncLogDetails : '1'
// })

var SyncLog = Backbone.Model.extend({});

var SyncLogActionRelational = Backbone.RelationalModel.extend({})
var SyncLogWarningRelational = Backbone.RelationalModel.extend({})
var SyncLogConflictRelational = Backbone.RelationalModel.extend({})

var SyncLogActions = Backbone.Collection.extend({
    parse : function(response) {
        return response.actions;
    },
    model : SyncLogActionRelational
});

var SyncLogWarnings = Backbone.Collection.extend({
    parse : function(response) {
        return response.warnings;
    },
    model : SyncLogWarningRelational
});

var SyncLogConflicts = Backbone.Collection.extend({
    parse : function(response) {
        return response.conflicts;
    },
    model : SyncLogConflictRelational
});

var SyncLogRelational = Backbone.RelationalModel.extend({
    relations : [ {
        type : Backbone.HasMany,
        key : 'actions',
        relatedModel : 'SyncLogActionRelational',
        reverseRelation : {
            key : 'syncLog'
        },
        collectionOptions : function(relationalSyncLog) {
            return {
                url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/actions'
            };
        },
        collectionType : 'SyncLogActions'
    }

    , {
        type : Backbone.HasMany,
        key : 'warnings',
        relatedModel : 'SyncLogWarningRelational',
        reverseRelation : {
            key : 'syncLog'
        },
        collectionOptions : function(relationalSyncLog) {
            return {
                url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/warnings'
            };
        },
        collectionType : 'SyncLogWarnings'
    }, {
        type : Backbone.HasMany,
        key : 'conflicts',
        relatedModel : 'SyncLogConflictRelational',
        reverseRelation : {
            key : 'syncLog'
        },
        collectionOptions : function(relationalSyncLog) {
            return {
                url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/conflicts'
            };
        },
        collectionType : 'SyncLogConflicts'

    } ]
});

var SyncLogsRelational = Backbone.Collection.extend({
    url : '../api/syncher-web-restserver/synclogs',
    model : SyncLogRelational,
    parse : function(response) {
        return response.synclogs;
    }

});

var syncLogs = new SyncLogsRelational();

var RemainingSyncEvents = Backbone.Collection.extend({
    parse : function(response) {
        return response.remainingevents;
    },
    url : '../api/syncher-web-restserver/synclogs/remainingevents'

});

syncLogs.fetch();

var LogViewModel = kb.ViewModel.extend({
    constructor : function(model) {
        var self = this;
        kb.ViewModel.prototype.constructor.call(this, model, { /*
                                                                 * internals:
                                                                 * ['actions']
                                                                 */});
        this.classCss = ko.computed((function() {
            if (self.warnings().length > 0) {
                return "warning";
            }
            var status = self.Status();

            if (status != undefined) {

                if (status.toLowerCase() === "Success".toLowerCase()) {
                    return "success";
                } else if (status.toLowerCase() === "Conflict".toLowerCase()) {
                    return "error";
                } else if (status.toLowerCase() === "Conflict".toLowerCase()) {
                    return "warning";
                }
            }
            return "";

        }));

        this.goToLogDetails = function(currentSyncLog) {
            router.navigate('details/' + currentSyncLog.id(), {
                trigger : true
            });
        };

        return this;
    }
});

var LogDetailsViewModel = kb.ViewModel.extend({
    constructor : function(model) {
        kb.ViewModel.prototype.constructor.call(this, model, {});
    }
});

// var CustomViewModel = kb.ViewModel.extend({
// this.classCss: ko.computed((function() { return "success";}))
// });

var remainingEvents = new RemainingSyncEvents();
remainingEvents.fetch();
var syncLogsViewModel = {
    syncLogs : kb.collectionObservable(syncLogs, {
        view_model : LogViewModel
    }),
    remainingSyncEvents : kb.collectionObservable(remainingEvents)
};

// tha router:
window.RouterBackboneJS = Backbone.Router.extend({

    constructor : function() {
        var _this = this;
        Backbone.Router.prototype.constructor.apply(this, arguments);

        var loadPage = function(el) {
            if (_this.active_el) {
                ko.removeNode(this.active_el);
            }
            $('#content').append(_this.active_el = el);
            $(el).addClass('active');
        };

        var appendContent = function(el, elementIdToAppendIn) {
            if (_this.appended_el) {
                ko.removeNode(this.appended_el);
            }
            $('#' + elementIdToAppendIn).append(_this.appended_el = el);
        };

        var loadTemplate = function(templateId, callback) {

            // preventing reloading of templates
            if (_this.loadedTemplates) {
                if (_this.loadedTemplates.indexOf(templateId) !== -1) {
                    callback();
                    return;
                }
            } else {
                _this.loadedTemplates = new Array();
                _this.loadedTemplates.push(templateId);

            }
            var el = $('#' + templateId);
            $.get(el.attr("src"), function(response) {
                el.text(response);
                console.log("Loaded template with ID: " + templateId);
                _this.loadedTemplates.push(templateId);
                callback();
                return;
            });
        }

        this.route('', null, function() {
            var templateId = 'home';

            loadTemplate(templateId, (function() {
                loadPage(kb.renderTemplate(templateId, syncLogsViewModel))
            }));
        });
        this.route('details/:syncLogId', null, function(syncLogId) {
            var templateId = 'details';

            loadTemplate(templateId, (function() {
                appendContent(kb.renderTemplate(templateId, new LogDetailsViewModel(syncLogs.get(syncLogId))),
                        'content')
            }));
        });
        // this.route('things', null, function() {
        // loadPage(kb.renderTemplate('things_page', new
        // ThingsPageViewModel())); });
        // this.route('things/:id', null, function(id) {
        // loadPage(kb.renderTemplate('thing_page', new
        // ThingCellViewModel(things_collection.get(id)))); });
    }
});

var router = new RouterBackboneJS();

/*
 * The ViewModel var dataTableExampleViewModel = { rowClickHandler :
 * function(event) { //ko.applyBindings(modalDetailsViewModel,
 * $('#syncLogDetailsModal')[0]); $('#syncLogDetailsModal').modal('show');
 * console.log(event); }, rowColorize : function(index, row) {
 * $(row).children("td").each(function(index, td) { var tdContent =
 * $(td).text(); console.log(tdContent); if (tdContent != undefined) { if
 * (tdContent.indexOf('Success') != -1) { $(row).addClass("success"); return
 * false; } else if (tdContent.indexOf('Fail') != -1) {
 * $(row).addClass('error'); return false; } } }); },
 * 
 * modalDetailsViewModel : kb.viewModel(syncLog),
 * 
 * tableData : ko.observableArray([ [ "20/10/2013", "Success" ], [ "20/12/2013",
 * "Fail" ], [ "20/05/2012", "Success" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing",
 * "Data" ], [ "Existing", "Janice" ] ]), add : function() {
 * this.tableData.push([ (new Date()).getTime(), "Added" ]); }, remove :
 * function() { this.tableData.pop(); } };
 */
/* Initialise the ViewModel */
$(function() {
    // ko.applyBindings(dataTableExampleViewModel);

    console.log(Backbone.history.start({
        root : '/xpto/syncher-web-clientjs/'
    }));
    // ko.applyBindings(syncLogsViewModel, $('#syncLogs')[0]);
});
