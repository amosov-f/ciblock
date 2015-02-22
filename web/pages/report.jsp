<%@ page import="ru.spbu.astro.ciblock.commons.Factor.Class" %>
<%@ page import="ru.spbu.astro.ciblock.commons.CityBlockInfo" %>
<%@ page import="ru.spbu.astro.ciblock.commons.Value" %>
<%@ page import="java.util.List" %>
<%@ page import="static ru.spbu.astro.ciblock.commons.Factor.Feature" %>
<%@ page import="static ru.spbu.astro.ciblock.commons.Factor.Answer" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<body>
<%
    final Feature[] features = (Feature[]) request.getAttribute("features");
    final Answer[] answers = (Answer[]) request.getAttribute("answers");
    final Map<String, Double> featureValues = (Map<String, Double>) request.getAttribute("feature values");
    final Map<String, Value> values = (Map<String, Value>) request.getAttribute("values");
    final CityBlockInfo[] nearestNeighbours = (CityBlockInfo[]) request.getAttribute("nearest neighbours");
%>

    <div class="panel panel-default">
        <div class="panel-heading"><h1 class="panel-title">Рекомендованный предпроект</h1></div>
        <div class="panel-body">
            <div class="col-lg-7">
                <blockquote>
            <%
                for (final Feature feature : features) {
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
                    for (final CityBlockInfo neighbour : nearestNeighbours) {
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
                    for (final Answer answer : answers) {
                        final Value value = values.get(answer.getName());
                %>
                    <p>
                        <%= answer.getName() %>:
                    <%
                        if (answer instanceof Class) {
                            final int index = (int) value.getValue();
                    %>
                            <%= ((Class) answer).getClasses().get(index) %>
                    <%
                        } else {
                    %>
                            <%= (int) Math.max(value.getValue(), 0) %>
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
