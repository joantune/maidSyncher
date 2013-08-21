define(['knockout', 'knockback', 'AppUtils', 'underscore'], function (ko,kb, AppUtils, _) {
var ActionsViewModel = function(model) {

    var self = this;

    this.success = kb.observable(model, 'success');
    this.rowClass = ko.computed(function() {
        return AppUtils.successUtil("success", "error", "", self.success());
    });

    this.typeOfChangeEvent = kb.observable(model, 'typeOfChangeEvent');

    this.changedDescriptors = kb.observable(model, 'changedDescriptors');

    this.shortTypeOfChangeEvent = ko.computed(function() {
        return AppUtils.TypeOfChangeEventAbbreviator(self.typeOfChangeEvent);
    });
    
    this.model = model;
    
    this.changedObjects = kb.observable(model, 'changedObjects');
    
    this.changedObjectsToList = function(changedObject, index, changedObjects) {
        if (changedObject == null)
            return "";
        var simpleClassName = AppUtils.ClassNameTrimmer(changedObject.className);
        this.listContent += '<li><a href="' + changedObject.urlObject + '" target="_blank">' + simpleClassName + "</a></li>\n";
    }
    
    this.actionOriginPopoverContent = ko.computed(function() {
        if (self.changedDescriptors() != null ) {
            
        	var descriptorsStrings = AppUtils.StringArrayToString(self.changedDescriptors());
        	var popoverContent = "<p><small><strong>Type:</strong> " + self.typeOfChangeEvent() + "</small></p>"
                    + "<p><small><strong>Changed descriptors:</strong> " + descriptorsStrings + "</small></p>"
                    + "<p><small>Origin object: </small><small><a href=\"" + self.urlOriginObject() + "\" target='_blank'>Link</a></small></p>"
                    + "<p>Changed objects:</p>";
        	if (self.changedObjects() != null && self.changedObjects().length > 0) {
        	    var listObject = {listContent : ""};
        	    _.each(self.changedObjects(),self.changedObjectsToList, listObject);
        	    popoverContent += "<ul>" + listObject.listContent + "</ul>";
        	    
        	}
        	else popoverContent += "<p><small>- no information -</small></p>";
        	
        	return popoverContent;
        }
        else return "";
    });

    this.syncEndTime = kb.observable(model, 'syncEndTime');
    this.syncStartTime = kb.observable(model, 'syncStartTime');

    this.typeOriginObject = kb.observable(model, 'typeOriginObject');
    this.urlOriginObject = kb.observable(model, 'urlOriginObject');

    this.duration = ko.computed(function() {
        if (self.syncEndTime() != null && self.syncStartTime() != null) {
            return AppUtils.calculateDurationInSeconds(self.syncEndTime(), self.syncStartTime());
        }
        return null;
    });

    this.trimmedTypeOriginObject = ko.computed(function() {
        return AppUtils.ClassNameTrimmer(self.typeOriginObject());
    });

    this.successIcon = ko.computed(function() {
        var successIcon = '<i class="icon-ok"></i>';
        var failureIcon = '<i class="icon-remove"></i>';
        return AppUtils.successUtil(successIcon, failureIcon, "", self.success());
    });
};

return ActionsViewModel;
});
