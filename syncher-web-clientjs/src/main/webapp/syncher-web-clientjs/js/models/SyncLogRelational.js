define([
        'backboneRelational',
        'models/SyncLogActionRelational',
        'models/SyncLogWarningRelational',
        'models/SyncLogConflictRelational',
        'models/SyncLogActions',
        'models/SyncLogWarnings',
        'models/SyncLogConflicts' ], function(
        Backbone, SyncLogActionRelational, SyncLogWarningRelational, SyncLogConflictRelational, SyncLogActions,
        SyncLogWarnings, SyncLogConflicts) {
    return Backbone.RelationalModel.extend({
        relations : [ {
            type : Backbone.HasMany,
            key : 'actions',
            relatedModel : SyncLogActionRelational,
            reverseRelation : {
                key : 'syncLog'
            },
            collectionOptions : function(relationalSyncLog) {
                return {
                    url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/actions'
                };
            },
            collectionType : SyncLogActions
        }

        , {
            type : Backbone.HasMany,
            key : 'warnings',
            relatedModel : SyncLogWarningRelational,
            reverseRelation : {
                key : 'syncLog'
            },
            collectionOptions : function(relationalSyncLog) {
                return {
                    url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/warnings'
                };
            },
            collectionType : SyncLogWarnings
        }, {
            type : Backbone.HasMany,
            key : 'conflicts',
            relatedModel : SyncLogConflictRelational,
            reverseRelation : {
                key : 'syncLog'
            },
            collectionOptions : function(relationalSyncLog) {
                return {
                    url : '../api/syncher-web-restserver/synclogs/' + relationalSyncLog.get('id') + '/conflicts'
                };
            },
            collectionType : SyncLogConflicts

        } ]
    });
});