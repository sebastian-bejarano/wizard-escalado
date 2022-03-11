package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
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

    @ComponentImport
    private final ProjectService projectService;

    @ComponentImport
    private final ConstantsManager constantsManager;

    @ComponentImport
    private final IssueService issueService;

    @ComponentImport
    private final SearchService searchService;

    //Constructor con inyección de dependencias
    @Inject
    public FifthStep(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, IssueLinkService issueLinkService, ProjectService projectService, ConstantsManager constantsManager, IssueService issueService, SearchService searchService){
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkService = issueLinkService;
        this.projectService = projectService;
        this.constantsManager = constantsManager;
        this.issueService = issueService;
        this.searchService = searchService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Creamos un mapa de Hash para inyectar parámetros que serán enviados a velocity
        Map<String,Object> params = new HashMap<String,Object>();
        //Obtenemos los parámetros que vienen en el request
        String proyectoAEscalar = req.getParameter("project");
        String issueAEscalar = req.getParameter("issue");
        String problemAEnlazar = req.getParameter("problem");
        String prioridad = req.getParameter("prioridad");
        String momentoError = req.getParameter("momentoError");
        String severidad = req.getParameter("severidad");
        String fabricaDesarrollo = req.getParameter("fabricaDesarrollo");
        String motivoEscalamiento = req.getParameter("motivoEscalamiento");
        String nuevoResponsable = req.getParameter("personaAEscalar");
        String epica = req.getParameter("epica");
        String issueKey = issueAEscalar.split("/")[0].trim();
        String projectKey = proyectoAEscalar.split("/")[0].trim();
        if(!problemAEnlazar.equalsIgnoreCase("Service Request - No lleva problem asociado")) {
            String problemKey = problemAEnlazar.split("/")[0].trim();
            if (GJIRAUtils.relacionarIssuesConRelacionado(issueKey, problemKey, this.issueLinkService, params, this.authenticationContext)) {
                if (GJIRAUtils.crearIncidenteProductivoEnlazado(projectKey, issueKey, problemKey, prioridad, momentoError, severidad, fabricaDesarrollo, motivoEscalamiento, epica, nuevoResponsable, this.authenticationContext, params, projectService, constantsManager, issueService, issueLinkService, ComponentAccessor.getFieldManager(), ComponentAccessor.getCustomFieldManager(), ComponentAccessor.getOptionsManager(), searchService)) {
                    if (GJIRAUtils.updateIssueStatus(ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey), false, nuevoResponsable, authenticationContext, issueService)) {
                        params.put("mensaje", "Incident escalado con éxito");
                    } else {
                        params.put("mensaje", "Falló la actualización de estado");
                    }
                }
            }
        }else{
            if (GJIRAUtils.crearIncidenteProductivoEnlazado(projectKey, issueKey, "Service Request", prioridad, momentoError, severidad, fabricaDesarrollo, motivoEscalamiento, epica, nuevoResponsable, this.authenticationContext, params, projectService, constantsManager, issueService, issueLinkService, ComponentAccessor.getFieldManager(), ComponentAccessor.getCustomFieldManager(), ComponentAccessor.getOptionsManager(), searchService)) {
                if (GJIRAUtils.updateIssueStatus(ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey), true, nuevoResponsable, authenticationContext, issueService)) {
                    params.put("mensaje", "Service Request escalado con éxito");
                } else {
                    params.put("mensaje", "Falló la actualización de estado");
                }
            }
        }
        templateRenderer.render("templates/fifthStep.vm", params, resp.getWriter());
    }
}
