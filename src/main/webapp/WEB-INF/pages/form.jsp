<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.devlot.model.Factor" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="static ru.devlot.model.Factor.Feature" %>
<%@ page import="ru.devlot.model.Vector" %>

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
                <div class="input-group">
                    <span class="input-group-addon"><%= name %></span>
                    <input id="<%= name %>" name="feature" type="number" class="form-control" placeholder="<%= example.get(name) %>">
                    <span class="input-group-addon"><%= feature.getDimension() %></span>
                </div>
                <br>
        <%
            }
        %>
            <div class="form-group">
                <button class="btn btn-primary col-lg-5" onclick="submit()">Расcчитать!</button>
                <a href="/how"><button class="btn btn-link col-lg-7">Как это работает?</button></a>
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

            console.log(JSON.stringify(features));
            $.ajax({
                url: '/submit',
                data: 'feature_json=' + JSON.stringify(features),
                success: function(report) {
                    $('#report').html(report);
                }
            });
        }

    </script>
</body>
</html>