<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.devlot.model.Vector" %>
<%@ page import="java.util.List" %>
<%@ page import="static ru.devlot.model.Factor.Feature" %>

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

</head>

<body>
    <div class="container">
        <div class="page-header" align="center">
            <h1>Рассчет предпроекта</h1>
        </div>
        <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
            <h4>Введите данные по кварталу</h4>
        <%
            List<Feature> features = (List<Feature>) request.getAttribute("features");
            Vector example = (Vector) request.getAttribute("example");

            for (Feature feature : features) {
                String name = feature.getName();
        %>
                <div class="form-group input-group">
                    <span class="input-group-addon"><%= name %></span>
                    <input id="<%= name %>" name="feature" type="number" class="form-control" placeholder="<%= example.get(name) %>">
                    <span class="input-group-addon"><%= feature.getDimension() %></span>
                </div>
        <%
            }
        %>
            <div class="form-group btn-group-justified">
                <a class="btn btn-primary" onclick="submit()">Расcчитать!</a>
                <a class="btn btn-link" href="/how">Как это работает?</a>
            </div>

            <div class="form-group" align="center">
                <script type="text/javascript"></script>
                <div class="pluso" data-background="#ebebeb" data-options="medium,square,line,horizontal,nocounter,theme=04" data-services="print,email,vkontakte,facebook,twitter" data-user="1725861257"></div>
            </div>
        </div>

        <div id="report" class="col-lg-8  col-md-8 col-sm-8 col-xs-12">
        </div>
    </div>

    <script>

        function submit() {
            var features = {};
            $('input').each(function() {
                features[$(this).attr('id')] = $(this).val();
            });

            for (var id in features) {
                var val = parseFloat(features[id]);
                if (isNaN(val)) {
                    alert(capitaliseFirstLetter(id) + ' должен(а) быть числом!');
                    return;
                }
                if (val < 0) {
                    alert(capitaliseFirstLetter(id) + ' должен(а) быть положительным числом!');
                    return;
                }
                if (val > 1e9) {
                    alert(capitaliseFirstLetter(id) + ' не должен(а) быть очень большим числом!');
                    return;
                }
            }

            console.log(JSON.stringify(features));
            $.ajax({
                url: '/submit',
                data: 'feature_json=' + JSON.stringify(features),
                success: function(report) {
                    $('#report').html(report);
                }
            });
        }

        (function() {
            if (window.pluso) {
                if (typeof window.pluso.start == "function") {
                    return;
                }
            }
            if (window.ifpluso == undefined) {
                window.ifpluso = 1;
                var d = document;
                var s = d.createElement('script');
                var g = 'getElementsByTagName';
                s.type = 'text/javascript';
                s.charset='UTF-8';
                s.async = true;
                s.src = ('https:' == window.location.protocol ? 'https' : 'http')  + '://share.pluso.ru/pluso-like.js';
                var h = d[g]('body')[0];
                h.appendChild(s);
            }
        })();

        function capitaliseFirstLetter(s) {
            return s.charAt(0).toUpperCase() + s.slice(1);
        }

    </script>
</body>
</html>