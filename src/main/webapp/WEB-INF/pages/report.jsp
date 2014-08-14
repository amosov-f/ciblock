<%@ page import="ru.devlot.model.Factor.Class" %>
<%@ page import="ru.devlot.model.Info" %>
<%@ page import="ru.devlot.model.Value" %>
<%@ page import="java.util.List" %>
<%@ page import="static ru.devlot.model.Factor.Feature" %>
<%@ page import="static ru.devlot.model.Factor.Answer" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<body>
<%
    List<Feature> features = (List<Feature>) request.getAttribute("features");
    List<Answer> answers = (List<Answer>) request.getAttribute("answers");
    Map<String, Double> featureValues = (Map<String, Double>) request.getAttribute("feature values");
    Map<String, Value> values = (Map<String, Value>) request.getAttribute("values");
    List<Info> nearestNeighbours = (List<Info>) request.getAttribute("nearest neighbours");
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
                        <%= featureValues.get(feature.getName()).intValue() %> <%= feature.getDimension() %>
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
                    for (Info neighbour : nearestNeighbours) {
                %>
                        <li><a href="<%= neighbour.getRef() %>" target="_blank"><%= neighbour.getId() %></a></li>
                <%
                    }
                %>
                </ol>
            </div>
            <div class="col-lg-12">
                <blockquote style="border-color: limegreen">
                <%
                    for (Answer answer : answers) {
                        Value value = values.get(answer.getName());
                %>
                    <p>
                        <%= answer.getName() %>:
                    <%
                        if (answer instanceof Class) {
                            int index = value.getValue().intValue();
                    %>
                            <%= ((Class) answer).getClasses().get(index) %>
                    <%
                        } else {
                    %>
                            <%= value.getValue().intValue() %>
                    <%
                        }
                        if (answer.getDimension() != null) {
                    %>
                            <%= answer.getDimension() %>
                    <%
                        }
                    %>
                        <span style="float: right">
                            <abbr title="Число кварталов, по которым делается предсказание"><%= value.getNumInstances() %></abbr>,
                            <abbr title="Качество предсказания"><%= Math.max((int) (100 * value.getQuality()), 0) %>%</abbr>
                        </span>
                    </p>
                <%
                    }
                %>
                </blockquote>
            </div>
        </div>

    </div>

</body>
