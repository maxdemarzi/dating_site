if ($(".typeahead").length) {

    $.typeahead({
        input: ".typeahead",
        minLength: 1,
        maxItem: 20,
        order: "asc",
        dynamic: true,
        delay: 300,
        source: {
            location: {
                display: "full_name",
                ajax: {
                    method: "GET",
                    url: "/autocomplete/city/" + "{{query}}"
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

// http://www.runningcoder.org/jquerytypeahead/documentation/
