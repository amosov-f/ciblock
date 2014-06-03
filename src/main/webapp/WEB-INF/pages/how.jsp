<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="ru.devlot.model.Vector" %>
<%@ page import="java.util.List" %>
<%@ page import="static ru.devlot.model.Factor.Feature" %>

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>How it works</title>

    <link href="../../resources/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <link href="../../resources/utility/css/styles.css" rel="stylesheet">

</head>

<body>

    <div class="blog-masthead">
        <div class="container">
            <nav class="blog-nav">
                <a class="blog-nav-item" href="/">Главная</a>
                <a class="blog-nav-item active" href="/how">Как это работает?</a>
            </nav>
        </div>
    </div>

    <div class="container">

        <div class="blog-header">
            <h1 class="blog-title">Как это работает?</h1>
            <p class="lead blog-description">Все очень просто</p>
        </div>


        <p>Вы вводите данные предполагаемого квартала.</p>
        <img src="/resources/images/dir.PNG" class="img-responsive img-rounded" />
        <hr>
        <p>У нас есть база отобранных участков, на которых обучается система. База постоянно пополняется и точность возрастает.</p>

        <div class="col-lg-12 col-md-12 col-xs-12 col-sm-12">
            <img src="/resources/images/exc.PNG" class="img-responsive img-rounded col-lg-6 col-md-6 col-xs-6 col-sm-12"  />
            <img src="/resources/images/rgis.PNG" class="img-responsive img-rounded col-lg-6 col-md-6 col-xs-6 col-sm-12" />
        </div>
        <div class="row" style="margin: 0px;" >
            <hr>
        </div>
        <p>Затем математическими методами мы расчитываем показатели, которые можно получить из участка.</p>
        <img src="/resources/images/prdprkt.PNG" class="img-responsive img-rounded" />

    </div>


    <div class="blog-footer">
        <a>Ciblock</a>
        <p>2014</p>
    </div>

</body>
</html>
