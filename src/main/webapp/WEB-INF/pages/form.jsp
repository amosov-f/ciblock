<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.devlot.model.factor.Factor" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="description" content>
    <meta name="author" content>


    <title>Lot Developer</title>

    <link href="/resources/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <script type="text/javascript" src="/resources/jquery/jquery.min.js"></script>
    <script src="/resources/bootstrap/js/bootstrap.min.js"></script>

    <script src="/resources/highcharts/js/highcharts.js"></script>
    <script src="/resources/highcharts/js/highcharts-3d.js"></script>
    <script src="/resources/highcharts/js/modules/exporting.js"></script>

</head>

<body>
    <div class="container">
        <div class="page-header" align="center">
            <h1>Рассчет предпроекта</h1>
        </div>
        <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
            <h4>Введите данные по кварталу</h4>
        <%
            Map<Integer, Factor> features = (Map<Integer, Factor>) request.getAttribute("features");

            for (int i : features.keySet()) {
        %>
                <div class="input-group">
                    <span class="input-group-addon"><%= features.get(i).getName() %></span>
                    <input id="<%= i %>" name="feature" type="number" class="form-control">
                    <span class="input-group-addon"><%= features.get(i).getDimension() %></span>
                </div>
                <br>
        <%
            }
        %>
            <div class="form-group">
                <button class="btn btn-primary" onclick="submit()">Расcчитать!</button>
            </div>
        </div>
        <div id="report" class="col-lg-8  col-md-8 col-sm-8 col-xs-12">
        </div>
    </div>

    <div id="container" style="height: 400px"></div>

    <script>

        function submit() {
            var features = {};
            $('input').each(function() {
                features[$(this).attr('id')] = $(this).val();
            });

            console.log(JSON.stringify(features));
            $.ajax({
                url: '/submit',
                data: 'feature_json=' + JSON.stringify(features),
                success: function(report) {
                    $('#report').html(report);
                }
            });
        }


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

            // Set up the chart
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
                    max: 4000,
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

        });
    </script>





</body>
</html>