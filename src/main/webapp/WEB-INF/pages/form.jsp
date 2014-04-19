<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.devlot.model.Factor" %>
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

</head>

<body>
    <div class="container">
        <div class="page-header" align="center">
            <h1>Рассчет предпроекта</h1>
        </div>
        <div class="col-lg-4 col-md-4 col-sm-4 col-xs-12">
        <%
            Map<Integer, Factor> features = (Map<Integer, Factor>) request.getAttribute("features");

            for (int i : features.keySet()) {
        %>
                <div class="input-group">
                    <span class="input-group-addon"><%= features.get(i).name %></span>
                    <input id="<%= i %>" name="feature" type="number" class="form-control">
                    <span class="input-group-addon"><%= features.get(i).dimension %></span>
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

    <script type="text/javascript" src="/resources/jquery/jquery.min.js"></script>
    <script src="/resources/bootstrap/js/bootstrap.min.js"></script>

</body>
</html>