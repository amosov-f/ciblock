<%@ page import="java.util.List" %>
<%--
  Created by IntelliJ IDEA.
  User: fedor
  Date: 08.05.14
  Time: 23:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>

    <script type="text/javascript" src="/resources/jquery/jquery.min.js"></script>

    <script src="/resources/highcharts/js/highcharts.js"></script>
    <script src="/resources/highcharts/js/highcharts-3d.js"></script>
    <script src="/resources/highcharts/js/modules/exporting.js"></script>
</head>
<body>
    <div id="container" style="height: 400px"></div>

    <script>
        $(function () {

            // Give the points a 3D feel by adding a radial gradient
            Highcharts.getOptions().colors = $.map(Highcharts.getOptions().colors, function (color) {
                return {
                    radialGradient: {
                        cx: 0.4,
                        cy: 0.3,
                        r: 0.5
                    },
                    stops: [
                        [0, color],
                        [1, Highcharts.Color(color).brighten(-0.2).get('rgb')]
                    ]
                };
            });
        });

        var points = [];
    <%
        List<Double> squares = (List<Double>) request.getAttribute("squares");
        List<Double> altitudes = (List<Double>) request.getAttribute("altitudes");
        List<Double> perimeters = (List<Double>) request.getAttribute("perimeters");

        for (int i = 0; i < squares.size(); ++i) {
    %>
            points.push([<%= squares.get(i) %>, <%= altitudes.get(i) %>,  <%= perimeters.get(i) %>]);
    <%
        }
    %>

        var chart = new Highcharts.Chart({
            chart: {
                renderTo: 'container',
                margin: 100,
                type: 'scatter',
                options3d: {
                    enabled: true,
                    alpha: 10,
                    beta: 30,
                    depth: 250,
                    viewDistance: 5,

                    frame: {
                        bottom: { size: 1, color: 'rgba(0,0,0,0.02)' },
                        back: { size: 1, color: 'rgba(0,0,0,0.04)' },
                        side: { size: 1, color: 'rgba(0,0,0,0.06)' }
                    }
                }
            },
            title: {
                text: 'Загруженные проекты'
            },
            subtitle: {
                text: 'По которым делается рекомендация'
            },
            plotOptions: {
                scatter: {
                    width: 10,
                    height: 10,
                    depth: 10
                }
            },
            xAxis: {
                min: 0,
                title: {
                    enabled: true,
                    text: 'Площадь'
                }
            },
            yAxis: {
                min: 0,
                title: {
                    enabled: true,
                    text: 'Высота'
                }
            },
            zAxis: {
                min: 0,
                max: 5000,
                title: {
                    enabled: true,
                    text: 'Периметр'
                }
            },
            legend: {
                enabled: false
            },
            series: [{
                name: 'Reading',
                colorByPoint: true,
                data: points
            }]
        });


        // Add mouse events for rotation
        $(chart.container).bind('mousedown.hc touchstart.hc', function (e) {
            e = chart.pointer.normalize(e);

            var posX = e.pageX,
                    posY = e.pageY,
                    alpha = chart.options.chart.options3d.alpha,
                    beta = chart.options.chart.options3d.beta,
                    newAlpha,
                    newBeta,
                    sensitivity = 5; // lower is more sensitive

            $(document).bind({
                'mousemove.hc touchdrag.hc': function (e) {
                    // Run beta
                    newBeta = beta + (posX - e.pageX) / sensitivity;
                    newBeta = Math.min(100, Math.max(-100, newBeta));
                    chart.options.chart.options3d.beta = newBeta;

                    // Run alpha
                    newAlpha = alpha + (e.pageY - posY) / sensitivity;
                    newAlpha = Math.min(100, Math.max(-100, newAlpha));
                    chart.options.chart.options3d.alpha = newAlpha;

                    chart.redraw(false);
                },
                'mouseup touchend': function () {
                    $(document).unbind('.hc');
                }
            });
        });
    </script>
</body>
</html>
