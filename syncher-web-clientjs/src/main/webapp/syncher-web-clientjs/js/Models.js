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

var RemainingSyncEvents = Backbone.Collection.extend({
    parse : function(response) {
        return response.remainingevents;
    },
    url : '../api/syncher-web-restserver/synclogs/remainingevents'

});