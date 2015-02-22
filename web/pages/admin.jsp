<%@ page import="java.util.List" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Админка</title>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    
    <script src="http://code.highcharts.com/highcharts.js"></script>
    <script src="http://code.highcharts.com/highcharts-3d.js"></script>
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
        final double[] squares = (double[]) request.getAttribute("squares");
        final double[] altitudes = (double[]) request.getAttribute("altitudes");
        final double[] perimeters = (double[]) request.getAttribute("perimeters");

        for (int i = 0; i < squares.length; ++i) {
    %>
            points.push([<%= squares[i] %>, <%= altitudes[i] %>,  <%= perimeters[i] %>]);
    <%
        }
    %>
        console.log(points);

        var chart = new Highcharts.Chart({
            chart: {
                renderTo: 'container',
                margin: 100,
                type: 'scatter',
                height: 650,
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
                text: 'Загруженные кварталы'
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
