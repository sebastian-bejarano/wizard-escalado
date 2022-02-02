package com.segurosbolivar.plugins;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SecondStep extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PluginServlet.class);

    @ComponentImport
    private final UserManager userManager;

    @ComponentImport
    private final LoginUriProvider loginUriProvider;

    @ComponentImport
    private final TemplateRenderer templateRenderer;

    @ComponentImport
    private final PageBuilderService pageBuilderService;

    @Inject
    public SecondStep(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, PageBuilderService pageBuilderService){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
        this.pageBuilderService = pageBuilderService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        //Se obtiene información sobre el usuario que realiza la petición
        UserProfile user = userManager.getRemoteUser(req);
        String userName = user.getUsername();

        if(userName == null){
            redirectToLogin(req,res);
            return;
        }
        //FIN VERIFICACIÓN USUARIO LOGGEADO
        //Mapa de parámetros a renderizar
        Map<String,Object> params = new HashMap<String,Object>();
        //Colocamos el tipo de respuesta que va
        res.setContentType("text/html; charset=utf-8");
        //Verificamos el valor del parámetro origen
        String actionType = req.getParameter("origen");
        //Miramos los posibles valores del parámetro origen que viene del formulario
        switch (actionType) {
            case "Babysitting":
                templateRenderer.render("templates/secondStepBaby.vm", params,res.getWriter());
                break;
            case "Preexistente":
                templateRenderer.render("templates/secondStepPre.vm", params,res.getWriter());
                break;
            default:
                res.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
        res.getWriter().write("HELLO THERE!");
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