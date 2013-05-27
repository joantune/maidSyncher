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
            
            
            //let's add the custom callback for each row, if it exists
            if (binding.trClick) {
                $(element).find("tr").click(binding.trClick);
            }
            
            if (binding.rowFunction) {
                $(element).find("tr").each(binding.rowFunction);
            }
        }
    };
})(jQuery);

/* The ViewModel */
var dataTableExampleViewModel = {
            rowClickHandler: function(event) {
               console.log(event); 
            },
            rowColorize: function(index, row) {
                $(row).children("td").each(function (index, td) {
                    var tdContent = $(td).text();
                    console.log(tdContent);
                    if (tdContent != undefined) {
                        if (tdContent.indexOf('Success') != -1 ) {
                            $(row).addClass("success");
                            return false;
                        }
                        else if (tdContent.indexOf('Fail') != -1) {
                            $(row).addClass('error');
                            return false;
                        }
                    }
                });
            },
    tableData : ko.observableArray([ [ "20/10/2013", "Success" ], [ "20/12/2013", "Fail" ], [ "20/05/2012", "Success" ],
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
