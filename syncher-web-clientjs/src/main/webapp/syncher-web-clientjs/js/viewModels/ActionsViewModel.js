define(['knockout', 'knockback', 'AppUtils'], function (ko,kb, AppUtils) {
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
    this.actionOriginPopoverContent = ko.computed(function() {
        if (self.changedDescriptors() != null ) {
            
        var descriptorsStrings = AppUtils.StringArrayToString(self.changedDescriptors());
            return "<p><small><strong>Type:</strong> " + self.typeOfChangeEvent() + "</small></p>"
                    + "<p><small><strong>Changed descriptors:</strong> " + descriptorsStrings + "</small></p>"
                    + "<p><small><a href=\"" + self.urlOriginObject() + "\" target='_blank'>Link</a></small></p>";
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
