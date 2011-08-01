$(document).ready(function() {
    var summableFields = $('input.summable');
    summableFields.change(function(event) {
        updateSum(event.target);
    });
});

function updateSum(target) {
    var sum = 0;
    $('input.summable').each(function() {
        var num = parseFloat($(this).attr('value'));
        if (!isNaN(num)) {
          sum = sum + num
        }
    });
    $('input.sumTotal').attr('value',sum);
    $('span.calculated').html(sum);
}
