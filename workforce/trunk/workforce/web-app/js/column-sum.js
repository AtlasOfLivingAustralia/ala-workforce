$(document).ready(function() {
    var summableFields = $('input.summable');
    summableFields.change(function(event) {
        updateSum(event.target);
    });
});

function updateSum(target) {
    var colNum = target.getAttribute('colnum');
    var sum = 0;
    var intraQuestionSum = $('input.intratotal');
    if (intraQuestionSum.size() == 0) {
        $('input[colnum="' + colNum + '"]').each(function() {
            var num = parseFloat($(this).attr('value').replace(',', ''));
            if (!isNaN(num)) {
              sum = sum + num
            }
        });
        $('input[totalnum="' + colNum + '"]').attr('value',sum);
        $('span[totalnum="' + colNum + '"]').html(sum);
    } else {
        var qprefix = target.getAttribute('id');
        qprefix = qprefix.substring(0, qprefix.lastIndexOf("_"));
        $('input[colnum="' + colNum + '"][id^="' + qprefix + '"]').each(function() {
            var num = parseFloat($(this).attr('value').replace(',', ''));
            if (!isNaN(num)) {
              sum = sum + num
            }
        });
        $('input[totalnum="' + colNum + '"][id^="' + qprefix + '"]').attr('value',sum);
        $('span[totalnum="' + colNum + '"][id^="s' + qprefix + '"]').html(sum);
    }
}
