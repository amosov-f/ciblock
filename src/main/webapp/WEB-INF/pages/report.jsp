<%@ page import="ru.devlot.model.Factor" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<body>
<%
    Map<Integer, Factor> features = (Map<Integer, Factor>) request.getAttribute("features");
    Map<Integer, Factor> answers = (Map<Integer, Factor>) request.getAttribute("answers");
    Map<Integer, Double> values = (Map<Integer, Double>) request.getAttribute("values");
%>

    <div class="panel panel-default">
        <div class="panel-heading"><h1 class="panel-title">Рекомендованный предпроект</h1></div>
        <div class="panel-body">
            <blockquote>
        <%
            for (int i : features.keySet()) {
        %>
                <p>
                    <%= features.get(i).name %>:
                    <%= values.get(i).intValue() %> <%= features.get(i).dimension %>
                </p>
        <%
            }
        %>
            </blockquote>
            <blockquote style="border-color: limegreen">
        <%
            for (int i : answers.keySet()) {
        %>
                <p>
                    <%= answers.get(i).name %>:
                    <%= values.get(i).intValue() %> <%= answers.get(i).dimension %>
                </p>
        <%
            }
        %>
            </blockquote>
        </div>

    </div>
</body>
