package com.segurosbolivar.plugins;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
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

    @ComponentImport
    private final TemplateRenderer templateRenderer;

    @ComponentImport
    private final PageBuilderService pageBuilderService;

    @Inject
    public PluginServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer templateRenderer, PageBuilderService pageBuilderService){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.templateRenderer = templateRenderer;
        this.pageBuilderService = pageBuilderService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Enviamos el recurso de CSS
        pageBuilderService.assembler().resources().requireWebResource("com.segurosbolivar.plugins.escalado:escalado-resources");
        //Hacemos el mapa entre parámetros y objetos
        Map<String,Object> params = new HashMap<String,Object>();
        //Se obtiene información sobre el usuario que realiza la petición
        UserProfile user = userManager.getRemoteUser(req);
        String issueKey = req.getParameter("issueKey");
        params.put("issueKey", issueKey);
        String userName = user.getUsername();
        Issue issueAEscalar = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey);
        boolean noFaltaAlguno = true;
        boolean centroDesarrollo=false,vicepresidencia=false,gerencia=false,ubi_1=false,ubi_2=false,categoria=false,item=false,grupoAsignacion=false;
        try {
            centroDesarrollo = ComponentAccessor.getFieldManager().getCustomField("customfield_18009").hasValue(issueAEscalar);
            vicepresidencia = ComponentAccessor.getFieldManager().getCustomField("customfield_10403").hasValue(issueAEscalar);
            gerencia = issueAEscalar.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10403")).toString().split(",").length > 1 ? true : false;
            ubi_1 = ComponentAccessor.getFieldManager().getCustomField("customfield_10707").hasValue(issueAEscalar);
            ubi_2 = issueAEscalar.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10707")).toString().split(",").length > 1 ? true : false;
            categoria = ComponentAccessor.getFieldManager().getCustomField("customfield_10409").hasValue(issueAEscalar);
            item = issueAEscalar.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10409")).toString().split(",").length > 1 ? true : false;
            grupoAsignacion = ComponentAccessor.getFieldManager().getCustomField("customfield_10439").hasValue(issueAEscalar);
        }
        catch(Exception ex){
            noFaltaAlguno = false;
        }
        if(userName == null){
            redirectToLogin(req,res);
            return;
        }
        if(centroDesarrollo && vicepresidencia && categoria && item && grupoAsignacion && gerencia && ubi_1 && ubi_2 && noFaltaAlguno){
            res.setContentType("text/html; charset=utf-8");
            templateRenderer.render("templates/inicio.vm", params,res.getWriter());
        }
        else{
            res.getWriter().write("Antes de escalar, asegúrese que todos los campos relevantes estén llenos (Centro de desarrollo, Vicepresidencia/Gerencia, Ubicación (Lugar y centro), Categoría / Ítem configuración, Grupo Asignación).");
        }
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
