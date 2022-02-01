package com.segurosbolivar.plugins;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PluginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PluginServlet.class);

    @ComponentImport
    private final UserManager userManager;

    @ComponentImport
    private final LoginUriProvider loginUriProvider;

    @ComponentImport TemplateRenderer templateRenderer;

    @Inject
    public PluginServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Hacemos el mapa entre parámetros y objetos
        Map<String,Object> params = new HashMap<String,Object>();
        //Se obtiene información sobre el usuario que realiza la petición
        UserProfile user = userManager.getRemoteUser(req);

        boolean isUserAdmin = userManager.isSystemAdmin(user.getUserKey());

        String userName = user.getUsername();

        if(userName == null || !isUserAdmin){
            redirectToLogin(req,res);
            return;
        }
        res.setContentType("text/html; charset=utf-8");
        templateRenderer.render("templates/inicio.vm", params,res.getWriter());

    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }
    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
