<%@ page import="ru.devlot.model.factor.Class" %>
<%@ page import="ru.devlot.model.factor.Factor" %>
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
                    <%= features.get(i).getName() %>:
                    <%= values.get(i).intValue() %> <%= features.get(i).getDimension() %>
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
                    <%= answers.get(i).getName() %>:
                <%
                    if (answers.get(i) instanceof Class)  {
                %>
                        <%= ((Class) answers.get(i)).getClasses().get(values.get(i).intValue()) %>
                <%
                    } else {
                %>
                        <%= values.get(i).intValue() %>
                <%
                    }
                    if (answers.get(i).getDimension() != null) {
                %>
                        <%= answers.get(i).getDimension() %>
                <%
                    }
                %>
                </p>
        <%
            }
        %>
            </blockquote>
        </div>

    </div>
</body>
