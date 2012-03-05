package au.org.ala.workforce

class ChartTagLib {

    static namespace = 'chart'

    def resources = {
        out << "<script type='text/javascript' src='/workforce/js/highcharts.js'></script>\n"
        out << "<script type='text/javascript' src='/workforce/js/modules/exporting.js'></script>"
    }

    def pie = { attrs ->

        def seriesData = new StringBuilder('[')
        def categories = attrs.data['categories']
        def name = attrs.data['series'][0][0]
        def values = attrs.data['series'][0][1]
        categories.eachWithIndex { it, i ->
            seriesData.append("['${it}',${values[i]}],")
        }
        seriesData.deleteCharAt(seriesData.length() - 1).append(']')

        def height = 400
        def width = 800
        if (attrs.size?.equalsIgnoreCase('small')) {
            height = 200
            width = 400
        }

        out << """
        <script type="text/javascript">
            \$(document).ready(function() {
                pieChart${attrs.index} = new Highcharts.Chart({
                    chart: {
                        renderTo: 'pieChart${attrs.index}',
                        type: 'pie',
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false
                    },
                    title: {
                        text: '${attrs.title}'
                    },
                    subtitle: {
                        text: '${attrs.subtitle}'
                    },
                    tooltip: {
                        formatter: function() {
                            return '<b>'+ this.point.name +'</b>: ' + this.y;
                        }
                    },
                    plotOptions: {
                        pie: {
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                formatter: function() {
                                    return '<b>'+ this.point.name +'</b>: '+ this.percentage.toPrecision(3) +' %';
                                }
                            }
                        },
                        series: {
                            animation: false
                        }
                    },
                    navigation: {
                        buttonOptions: {
                            enabled: false
                        }
                    },
                    credits: {
                        enabled: false
                    },
                    series: [{
                        type: 'pie',
                        name: '${name}',
                        data: ${seriesData.toString()}
                    }]
                });
            });
        </script>
        <div class='chart' id='pieChart${attrs.index}' style='width: ${width}px; height: ${height}px; margin: 50px 0px auto'></div>
"""
    }

    def column = { attrs ->

        def categories = new StringBuilder('[')
        def series = new StringBuilder('[')
        attrs.data['categories'].each {
            categories.append("'${it}',")
        }
        attrs.data['series'].each {
            series.append("{name: '${it[0]}', data: [")
            it[1].each { val ->
                series.append("${val},")
            }
            series.deleteCharAt(series.length() - 1).append(']},')
        }
        categories.deleteCharAt(categories.length() - 1).append(']')
        series.deleteCharAt(series.length() - 1).append(']')

        def xAxisLabels = new StringBuilder()
        if (attrs.xAxisLabelOrientation?.equalsIgnoreCase('vertical')) {
            xAxisLabels.append("rotation: -90, align: 'right'")
        } else {
            xAxisLabels.append("rotation: 0, align: 'center'")
        }

        def xAxisTitle = 'null'
        if (attrs.xAxisTitle) {
            xAxisTitle = "'${attrs.xAxisTitle}'"
        }
        
        def stacking = 'null'
        if (attrs.stacked?.equalsIgnoreCase('true')) {
            stacking = "'normal'"
        }

        def height = 400
        def width = 800
        if (attrs.size?.equalsIgnoreCase('small')) {
            height = 200
            width = 400
        } else if (attrs.size?.equalsIgnoreCase('medium')) {
            height = 300
            width = 600
        }

        def legend = 'enabled: false'
        if (attrs.legend?.equalsIgnoreCase('true')) {
            def align = 'left'
            def xOffset = 70
            if (attrs.legendAlignment?.equalsIgnoreCase('right')) {
                align = 'right'
                xOffset = 0
            }

            legend = """
                layout: 'vertical',
                backgroundColor: '#FFFFFF',
                align: '${align}',
                verticalAlign: 'top',
                x: ${xOffset},
                y: 50,
                floating: true,
                shadow: true
"""
        }

        out << """
<script type="text/javascript">
    var columnChart;
    \$(document).ready(function() {
        columnChart${attrs.index} = new Highcharts.Chart({
            chart: {
                renderTo: 'columnChart${attrs.index}',
                type: 'column'
            },
            title: {
                text: '${attrs.title}'
            },
            subtitle: {
                text: '${attrs.subtitle}'
            },
            legend: {
                ${legend}
            },
            tooltip: {
                formatter: function() {
                    return this.x + ': <b>' + this.y + '</b>';
                }
            },
            xAxis: {
                title: {
                    text: ${xAxisTitle}
                },
                categories: ${categories.toString()},
                labels: { ${xAxisLabels.toString()} }
            },
            yAxis: {
                min: 0,
                title: {
                    text: '${attrs.yAxisLabel}'
                }
            },
            plotOptions: {
                column: {
                    dataLabels: {
                        enabled: true
                    }
                },
                series: {
                    animation: false,
                    stacking: ${stacking}
                }
            },
            credits: {
                enabled: false
            },
            navigation: {
                buttonOptions: {
                    enabled: false
                }
            },
            series: ${series.toString()}
        });
    });
</script>
<div class='chart' id='columnChart${attrs.index}' style='width: ${width}px; height: ${height}px; margin: 50px 0px auto'></div>
"""
    }

    def bar = { attrs ->

        def categories = new StringBuilder('[')
        def series = new StringBuilder('[')
        attrs.data['categories'].each {
            categories.append("'${it}',")
        }
        attrs.data['series'].each {
            series.append("{name: '${it[0]}', data: [")
            it[1].each { val ->
                series.append("${val},")
            }
            series.deleteCharAt(series.length() - 1).append(']},')
        }
        categories.deleteCharAt(categories.length() - 1).append(']')
        series.deleteCharAt(series.length() - 1).append(']')

        def height = 400
        def width = 800
        if (attrs.size?.equalsIgnoreCase('small')) {
            height = 200
            width = 400
        }

        out << """
<script type="text/javascript">
    var barChart;
    \$(document).ready(function() {
        barChart${attrs.index} = new Highcharts.Chart({
            chart: {
                renderTo: 'barChart${attrs.index}',
                type: 'bar'
            },
            title: {
                text: '${attrs.title}'
            },
            subtitle: {
                text: '${attrs.subtitle}'
            },
            legend: {
                enabled: false
            },
            tooltip: {
                formatter: function() {
                    return this.x + ': <b>' + this.y + '</b>';
                }
            },
            xAxis: {
                categories: ${categories.toString()}
            },
            yAxis: {
                min: 0,
                title: {
                    text: '${attrs.yAxisLabel}'
                }
            },
            plotOptions: {
                bar: {
                    dataLabels: {
                        enabled: true
                    }
                },
                series: {
                    animation: false
                }
            },
            credits: {
                enabled: false
            },
            navigation: {
                buttonOptions: {
                    enabled: false
                }
            },
            series: ${series.toString()}
        });
    });
</script>
<div class='chart' id='barChart${attrs.index}' style='width: ${width}px; height: ${height}px; margin: 50px 0px auto'></div>
"""
    }

    def jsonCharts = { attrs ->
        def dir = attrs.survey.replace(' ', '-')
        def files = getJsonChartFiles(dir)
        if (files) {
            out << "<p>Display chart from file:&nbsp;&nbsp;&nbsp;" + select(name: 'jsonChartFiles', from: files, noSelection: ['':'Select a file']) + '</p>'

            out << """
                <script type="text/javascript">
                    \$('#jsonChartFiles').change(function() {
                        var file = '';
                        \$('#jsonChartFiles option:selected').each(function() {
                            file += \$(this).text();
                        });
                        window.location.href = '/workforce/report/charts?file=' + file + '&survey=${attrs.survey}';
                    });
                </script>
            """
        }
    }

    private List getJsonChartFiles(String dir) {
        return new File("/data/workforce/charts/${dir}").list({ d, f-> f ==~ /.*.json/ } as FilenameFilter)
    }

}
