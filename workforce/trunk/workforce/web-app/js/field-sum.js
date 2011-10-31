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
    sum = sum.toFixed(2)
    $('input.sumTotal').attr('value',sum);
    $('span.calculated').html(sum);
}
