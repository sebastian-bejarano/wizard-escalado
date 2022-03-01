package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FifthStep extends HttpServlet {

    //Se utiliza para obrener el usuario que está loggeado actualmente
    @ComponentImport
    private final JiraAuthenticationContext authenticationContext;

    //Se utiliza para crear el link entre las incidencias
    @ComponentImport
    private final IssueLinkService issueLinkService;

    //Se utiliza para renderizar el template de velocity
    @ComponentImport
    private final TemplateRenderer templateRenderer;

    //Constructor con inyección de dependencias
    @Inject
    public FifthStep(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, IssueLinkService issueLinkService){
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkService = issueLinkService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Creamos un mapa de Hash para inyectar parámetros que serán enviados a velocity
        Map<String,Object> params = new HashMap<String,Object>();
        //Obtenemos los parámetros que vienen en el request
        String proyectoAEscalar = req.getParameter("proyecto");
        String issueAEscalar = req.getParameter("issueKey");
        String problemAEnlazar = req.getParameter("problem");
        templateRenderer.render("templates/fifthStep.vm", params,resp.getWriter());
        GJIRAUtils
    }
}
