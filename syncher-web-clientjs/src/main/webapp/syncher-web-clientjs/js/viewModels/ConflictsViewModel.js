define(['knockout', 'models/TransactionalContextModel'], function (ko, TransactionalContextModel) {

	var ConflictsViewModel = function(model) {
    var self = this;
    self.eventOneTypeOfChangeEvent = kb.observable(model, 'eventOneTypeOfChangeEvent');
    self.eventTwoTypeOfChangeEvent = kb.observable(model, 'eventTwoTypeOfChangeEvent');
    self.eventOneOriginator = kb.observable(model, 'eventOneOriginator');
    self.eventTwoOriginator = kb.observable(model, 'eventTwoOriginator');
    self.winnerObject = kb.observable(model, 'winnerObject');
    self.eventTwoChangedDescriptors = kb.observable(model, 'eventTwoChangedDescriptors');
    self.eventOneChangedDescriptors = kb.observable(model, 'eventOneChangedDescriptors');

    self.eventOneTypeOfChangeEventAbbr = ko.computed(function() {
        return TypeOfChangeEventAbbreviator(self.eventOneTypeOfChangeEvent);
    });
    self.eventTwoTypeOfChangeEventAbbr = ko.computed(function() {
        return TypeOfChangeEventAbbreviator(self.eventTwoTypeOfChangeEvent);
    });

    self.eventOneShortName = ko.computed(function() {
        if (self.eventOneOriginator() != null)
            return ClassNameTrimmer(self.eventOneOriginator().className) + " " + self.eventOneTypeOfChangeEventAbbr();
    });
    self.eventTwoShortName = ko.computed(function() {
        if (self.eventTwoOriginator() != null)
            return ClassNameTrimmer(self.eventTwoOriginator().className) + " " + self.eventTwoTypeOfChangeEventAbbr();
    });

    self.eventStatusRep = function(id) {
        if (id && self.winnerObject() != null) {
            if (id === self.winnerObject().id) {
                // return "<i class='icon-ok-circle'></i>";
                return 'icon-ok-circle';
            } else {
                // return "<i class='icon-ban-circle'></i>";
                return 'icon-ban-circle';
            }
        }
        return "";
    };

    self.popoverDescriptionContent = ko.computed(function() {
        var eventOneOUrl = (self.eventOneOriginator && self.eventOneOriginator() != null) ? self.eventOneOriginator().url : "#";
        var eventTwoOUrl = (self.eventTwoOriginator && self.eventTwoOriginator() != null) ? self.eventTwoOriginator().url : "#";
//        return "<h5>Event One</h5> "
//        + "<p><small><strong>Type:</strong> " + self.eventOneTypeOfChangeEvent()
//        + "</small></p>" + "<p><small><strong>Changed descriptors:</strong> " + StringArrayToString(self.eventOneChangedDescriptors())
//        + "</small></p>" + "<p><small><a href=\"" + eventOneOUrl+ "\" target='_blank'>Link</a></small></p>"
//        +"<h5>Event Two</h5> "
//        + "<p><small><strong>Type:</strong> " + self.eventTwoTypeOfChangeEvent()
//        + "</small></p>" + "<p><small><strong>Changed descriptors:</strong> " + StringArrayToString(self.eventTwoChangedDescriptors())
//        + "</small></p>" + "<p><small><a href=\"" + eventTwoOUrl + "\" target='_blank'>Link</a></small></p>";

        return "<table class='table table-condensed'>" +
        		"<thead>" +
        		    "<tr>" +
        		        "<th></th>" +
        		        "<th>Event One</th>" +
        		        "<th>Event Two</th>" +
        		    "</tr>" +
        		"</thead>" +
        		"<tbody>" +
        		    "<tr>" +
        		        "<td><strong>Type:</strong></td>" +
        		        "<td>"+ self.eventOneShortName() + "</td>" +
        		        "<td>"+ self.eventTwoShortName() + "</td>" +
        		    "</tr>" +
        		    "<tr>" +
        		        "<td><strong>Changed Dsc:</strong></td>" +
        		        "<td>" +StringArrayToString(self.eventOneChangedDescriptors())+ "</td>" +
        		        "<td>" + StringArrayToString(self.eventTwoChangedDescriptors())+ "</td>" +
        		    "</tr>" +
        		    "<tr>" +
        		        "<td><strong>Links:</strong></td>" +
        		        "<td><a href=\"" + eventOneOUrl+ "\" target='_blank'>link</a></td>" +
        		        "<td><a href=\"" + eventTwoOUrl+ "\" target='_blank'>link</a></td>" +
        		    "</tr>" +
        		"</tbody>" +
        		"</table> ";
    });
};

return ConflictsViewModel;

});