<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <chart:resources/>
        <title>${title}</title>
    </head>
    <body>
        <div class="nav" id="breadcrumb">
            <wf:isABRSAdmin>
                <span class="navButton"><a class="home" href="${createLink(uri: '/admin')}">Home</a></span>
                <span class="navButton"><a href="${createLink(uri: '/admin/set/' + setId)}">${survey}</a></span>
                <span class="navButton">Charts ${year}</span>
            </wf:isABRSAdmin>
            <wf:isNotABRSAdmin>
                <span class="navButton"><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></span>
            </wf:isNotABRSAdmin>
        </div>
        <div class="body">
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <h1>Question ${qId}</h1>
            <a style="cursor: pointer; color: #666; font-weight: bold;" onclick="saveCharts();">Save Charts</a>
            <div style='clear: both;'>
                <g:set var="index" value="0"/>
                <g:each in="${chartData}" var="item">
                    <g:if test="${item['chart'] == 'pie'}">
                        <chart:pie title="${item['title']}" subtitle="${item['subtitle']}" data="${item['data']}" size="${item['size'] as String}" index="${index}"/>
                    </g:if>
                    <g:elseif test="${item['chart'] == 'column'}">
                        <chart:column title="${item['title']}" subtitle="${item['subtitle']}" data="${item['data']}" yAxisLabel="${item['yAxisLabel']}" xAxisLabelOrientation="${item['xAxisLabelOrientation'] as String}" size="${item['size'] as String}" legend="${item['legend'] as String}" legendAlignment="${item['legendAlignment'] as String}" xAxisTitle="${item['xAxisTitle'] as String}" stacked="${item['stacked'] as String}" index="${index}"/>
                    </g:elseif>
                    <g:elseif test="${item['chart'] == 'bar'}">
                        <chart:bar title="${item['title']}" subtitle="${item['subtitle']}" data="${item['data']}" yAxisLabel="${item['yAxisLabel'] as String}" size="${item['size'] as String}" index="${index}"/>
                    </g:elseif>
                    <g:set var="index" value="${(index as int) + 1}"/>
                </g:each>
            </div>
        </div>
        <script type="text/javascript">
            function saveCharts() {
                var charts = [];
                $('.chart').each(function(index) {
                    var chartId = this.id;
                    var svg = window[chartId].getSVG();
                    charts[charts.length] = svg;
                });
                var form = Highcharts.createElement('form', { method: 'post', action: '${createLink(uri: '/report/save')}' }, { display: 'none' }, document.body);
                Highcharts.createElement('input', {  type: 'hidden', name: 'year', value: '${year}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'survey', value: '${survey}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'qnum', value: '${qId}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'qsetId', value: '${setId}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'userId', value: '${userId}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'json', value: '${json}' }, null, form);
                Highcharts.createElement('input', {  type: 'hidden', name: 'fromFile', value: '${fromFile}' }, null, form);
                var i;
                for (i = 0; i < charts.length; i += 1) {
                    Highcharts.createElement('input', { type: 'hidden', name: 'chart'+i, value: charts[i] }, null, form);
                }
                form.submit();
            }
        </script>
    </body>
</html>
