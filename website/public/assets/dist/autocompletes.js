// http://www.runningcoder.org/jquerytypeahead/documentation/

if ($("#attribute").length) {

    $.typeahead({
        input: "#attribute",
        minLength: 1,
        maxItem: 20,
        order: "asc",
        dynamic: true,
        delay: 300,
        source: {
            location: {
                display: "name",
                ajax: {
                    method: "GET",
                    url: "/autocomplete/attribute/" + "{{query}}"
                }
            }
        },
        callback: {
            onInit: function (node) {
                console.log('Typeahead Initiated on ' + node.selector);
            }
        }
    });
}

if ($("#thing").length) {

    $.typeahead({
        input: "#thing",
        minLength: 1,
        maxItem: 20,
        order: "asc",
        dynamic: true,
        delay: 300,
        source: {
            location: {
                display: "name",
                ajax: {
                    method: "GET",
                    url: "/autocomplete/thing/" + "{{query}}"
                }
            }
        },
        callback: {
            onInit: function (node) {
                console.log('Typeahead Initiated on ' + node.selector);
            }
        }
    });
}

