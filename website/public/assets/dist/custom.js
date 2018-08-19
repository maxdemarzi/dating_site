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