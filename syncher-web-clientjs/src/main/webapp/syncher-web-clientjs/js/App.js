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
            //let's add the custom callback for each row
            console.log(element);
            
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
            
            $(element).find("tr").click(function(clickedEl) {
               window.console(clickedEl); 
            });
        }
    };
})(jQuery);

/* The ViewModel */
var dataTableExampleViewModel = {
    tableData : ko.observableArray([ [ "Existing", "Data" ], [ "In An", "observableArray" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ],
            [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Data" ], [ "Existing", "Janice" ] ]),
    add : function() {
        this.tableData.push([ (new Date()).getTime(), "Added" ]);
    },
    remove : function() {
        this.tableData.pop();
    }
};

/* Initialise the ViewModel */
$(function() {
    ko.applyBindings(dataTableExampleViewModel);
});
