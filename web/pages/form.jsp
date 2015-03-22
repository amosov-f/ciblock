<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.spbu.astro.ciblock.commons.Vector" %>
<%@ page import="java.util.List" %>
<%@ page import="static ru.spbu.astro.ciblock.commons.Factor.Feature" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="description" content>
    <meta name="author" content>

    <title>City Block Designer</title>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
    
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min.js"></script>

    <link href="styles/styles.css" rel="stylesheet">
</head>

<body>

    <div class="blog-masthead">
        <div class="container">
            <nav class="blog-nav">
                <a class="blog-nav-item active" href="form">Главная</a>
                <a class="blog-nav-item" href="how">Как это работает?</a>
            </nav>
        </div>
    </div>

    <div class="container">
        <div class="page-header" align="center">
            <h1>Рассчет предпроекта</h1>
        </div>
        <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
            <h4>Введите данные по участку</h4>
        <%
            final Feature[] features = (Feature[]) request.getAttribute("features");
            final Vector example = (Vector) request.getAttribute("example");

            for (final Feature feature : features) {
                final String name = feature.getName();
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
            </div>

            <div class="form-group" align="center">
                <script type="text/javascript"></script>
                <div class="pluso" data-background="#ebebeb" data-options="medium,square,line,horizontal,nocounter,theme=04" data-services="print,email,vkontakte,facebook,twitter" data-user="1725861257"></div>
            </div>
        </div>

        <div id="report" class="col-lg-8  col-md-8 col-sm-8 col-xs-12">
            <p align="center">
                Тут будет рекомендованный предпроект
            </p>
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

            $.ajax({
                url: '/submit',
                data: $.param(features),
                type: 'GET',
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