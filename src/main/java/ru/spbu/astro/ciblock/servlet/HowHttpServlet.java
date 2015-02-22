package ru.spbu.astro.ciblock.servlet;

import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: amosov-f
 * Date: 22.02.15
 * Time: 1:55
 */
@WebServlet("/how")
public final class HowHttpServlet extends HttpServlet {
    @Override
    protected void doGet(@NotNull final HttpServletRequest req, 
                         @NotNull final HttpServletResponse resp) throws ServletException, IOException 
    {
        req.getRequestDispatcher("pages/how.jsp").forward(req, resp);
    }
}
