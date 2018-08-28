$(function () {
    $('[data-toggle="tooltip"]').tooltip()
})

function highFive(id) {
    $("#five_"+id).submit(function(e){
        e.preventDefault(e);
    });

    $.post('high_five', $("#five_"+id).serialize());
    $("#five_"+id).replaceWith("<i class=\"material-icons\">pan_tool</i> High Fived");
}

function lowFive(id) {
    $("#five_"+id).submit(function(e){
        e.preventDefault(e);
    });

    $.post('low_five', $("#five_"+id).serialize());
    $("#five_"+id).replaceWith("<i class=\"material-icons icon-upside-down\">pan_tool</i> Low Fived");
}

function block(id) {
    $("#block_"+id).submit(function(e){
        e.preventDefault(e);
    });

    $.post('block', $("#block_"+id).serialize());
    $("#post_"+id).hide();
}

// Template

$(function () {
    function getRight() {
        if (!$('[data-toggle="popover"]').length) return 0
        return ($(window).width() - ($('[data-toggle="popover"]').offset().left + $('[data-toggle="popover"]').outerWidth()))
    }

    $(window).on('resize', function () {
        var instance = $('[data-toggle="popover"]').data('bs.popover')
        if (instance) {
            instance.config.viewport.padding = getRight()
        }
    })

    $('[data-toggle="popover"]').popover({
        template: '<div class="popover" role="tooltip"><div class="arrow"></div><div class="popover-body popover-content px-0"></div></div>',
        title: '',
        html: true,
        trigger: 'manual',
        placement:'bottom',
        viewport: {
            selector: 'body',
            padding: getRight()
        },
        content: function () {
            var $nav = $('#js-popoverContent').clone()
            return '<ul class="nav nav-pills nav-stacked flex-column" style="width: 120px">' + $nav.html() + '</ul>'
        }
    })

    $('[data-toggle="popover"]').on('click', function (e) {
        e.stopPropagation()

        if ($($('[data-toggle="popover"]').data('bs.popover').getTipElement()).hasClass('in')) {
            $('[data-toggle="popover"]').popover('hide')
            $(document).off('click.app.popover')

        } else {
            $('[data-toggle="popover"]').popover('show')

            setTimeout(function () {
                $(document).one('click.app.popover', function () {
                    $('[data-toggle="popover"]').popover('hide')
                })
            }, 1)
        }
    })

})

// File Uploads

$(function() {

    // We can attach the `fileselect` event to all file inputs on the page
    $(document).on('change', ':file', function() {
        var input = $(this),
            numFiles = input.get(0).files ? input.get(0).files.length : 1,
            label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
        input.trigger('fileselect', [numFiles, label]);
    });

    // We can watch for our custom `fileselect` event like this
    $(document).ready( function() {
        $(':file').on('fileselect', function(event, numFiles, label) {

            var name = $("#image_name"),
                log = numFiles > 1 ? numFiles + ' files selected' : label;

            if( name.length ) {
                name.text(log + ' will be included in post.');
            } else {
                if( log ) alert(log);
            }

        });
    });

});