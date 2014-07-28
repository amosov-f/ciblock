<%@ page import="ru.devlot.model.Factor.Class" %>
<%@ page import="ru.devlot.model.Info" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="static ru.devlot.model.Factor.Feature" %>
<%@ page import="static ru.devlot.model.Factor.Answer" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<body>
<%
    List<Feature> features = (List<Feature>) request.getAttribute("features");
    List<Answer> answers = (List<Answer>) request.getAttribute("answers");
    Map<String, Double> values = (Map<String, Double>) request.getAttribute("values");
%>

    <div class="panel panel-default">
        <div class="panel-heading"><h1 class="panel-title">Рекомендованный предпроект</h1></div>
        <div class="panel-body">
            <div class="col-lg-7">
                <blockquote>
            <%
                for (Feature feature : features) {
            %>
                    <p>
                        <%= feature.getName() %>:
                        <%= values.get(feature.getName()).intValue() %> <%= feature.getDimension() %>
                    </p>
            <%
                }
            %>
                </blockquote>
            </div>
            <div class="col-lg-5">
                <h5>Похожие проекты</h5>
                <ol>
                <%
                    for (Info info : (List<Info>) request.getAttribute("nearestNeighbours")) {
                %>
                        <li><a href="<%= info.getRef() %>" target="_blank"><%= info.getId() %></a></li>
                <%
                    }
                %>
                </ol>
            </div>
            <div class="col-lg-12">
                <blockquote style="border-color: limegreen">
                    <%
                        for (Answer answer : answers) {
                    %>
                    <p>
                        <%= answer.getName() %>:
                        <%
                            if (answer instanceof Class) {
                                int index = values.get(answer.getName()).intValue();
                        %>
                        <%= ((Class) answer).getClasses().get(index) %>
                        <%
                        } else {
                        %>
                        <%= values.get(answer.getName()).intValue() %>
                        <%
                            }
                            if (answer.getDimension() != null) {
                        %>
                        <%= answer.getDimension() %>
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

    </div>

</body>
