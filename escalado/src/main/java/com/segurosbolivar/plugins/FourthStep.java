package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkCreator;
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
import java.util.*;

public class FourthStep extends HttpServlet {

    private final String MOMENTO_DEL_ERROR = "customfield_14300";
    private final String SEVERIDAD = "customfield_10432";
    private final String FABRICA_DESARROLLO = "customfield_15700";
    private final String MOTIVO_ESCALAMIENTO = "customfield_14501";

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
    private final SearchService searchService;

    @ComponentImport
    private final IssueService issueService;

    //Constructor con inyección de dependencias
    @Inject
    public FourthStep(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, IssueLinkService issueLinkService, SearchService searchService, IssueService issueService){
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkService = issueLinkService;
        this.searchService = searchService;
        this.issueService = issueService;
    }

    //Handler del método POST en el servlet
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
        //Creamos un mapa de Hash para inyectar parámetros que serán enviados a velocity
        Map<String,Object> params = new HashMap<String,Object>();
        //Obtenemos los parámetros que vienen en el request
        String proyectoAEscalar = req.getParameter("proyecto");
        String issueAEscalar = req.getParameter("issueKey");
        String problemAEnlazar = req.getParameter("problem");
        String crearProblema = (req.getParameter("crearProblemaNuevo") != null && !req.getParameter("crearProblemaNuevo").isEmpty() ? req.getParameter("crearProblemaNuevo") : "No");
        String cambiarNombreProblema = (req.getParameter("cambiarNombreProblema") != null && !req.getParameter("cambiarNombreProblema").isEmpty() ? req.getParameter("cambiarNombreProblema") : "No");
        String nombreNuevo = req.getParameter("nombreNuevo");
        //Obtenemos el issue que vamos a escalar (Que debería ser en este momento tipo incident)
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueAEscalar);
        //Obtenemos el issue tipo problem con el cuál se va a enlazar
        Issue problem;
        if(crearProblema.equals("Si")){
            IssueService.CreateValidationResult resultadoCreacion = GJIRAUtils.crearProblemaAsociado(authenticationContext,issueService,issue,ComponentAccessor.getFieldManager(),ComponentAccessor.getCustomFieldManager(),ComponentAccessor.getOptionsManager(),ComponentAccessor.getProjectManager());
            if(!resultadoCreacion.getErrorCollection().hasAnyErrors()){
                IssueService.IssueResult nuevoProblema = issueService.create(authenticationContext.getLoggedInUser(),resultadoCreacion);
                problem = nuevoProblema.getIssue();
                params.put("problem", problem);

            }
            else{
                throw new ServletException(resultadoCreacion.getErrorCollection().toString());
            }
        }
        else{
            if(!problemAEnlazar.equalsIgnoreCase("Service Request")) {
                problem = ComponentAccessor.getIssueManager().getIssueByCurrentKey(problemAEnlazar);
                if(cambiarNombreProblema.equalsIgnoreCase("No")) {
                    params.put("problem", problem);
                }else{
                    params.put("nombreNuevo", "");
                    IssueInputParameters inputParameters = issueService.newIssueInputParameters()
                            .setSummary(nombreNuevo);

                    IssueService.UpdateValidationResult result = issueService.validateUpdate(authenticationContext.getLoggedInUser(),problem.getId(),inputParameters);
                    if(result.getErrorCollection().hasAnyErrors()){
                        throw new ServletException("No se pudo cambiar el nombre del problem");
                    }
                    else{
                        problem = issueService.update(authenticationContext.getLoggedInUser(), result).getIssue();
                        params.put("problem", problem);
                    }
                }
            }
            else{
                params.put("isServiceRequest","true");
                params.put("problem", "Service Request");
                params.put("nombreNuevo", " - "+nombreNuevo);
            }
        }
        //Código del issueLink "Relacionado"
        //Obtenemos el objeto IssueLink
        IssueLink link = ComponentAccessor.getIssueLinkManager().getIssueLink(274416l);
        //Colocamos los parámetros en el mapa hash para que sean enviados al template de velocity
        params.put("issue",issue);
        params.put("project",proyectoAEscalar);
        Options momentoErrorOpciones = GJIRAUtils.getCustomFieldOptionsForIncidenteProductivo(MOMENTO_DEL_ERROR,authenticationContext,searchService,ComponentAccessor.getOptionsManager(),ComponentAccessor.getCustomFieldManager());
        Options severidadOpciones = GJIRAUtils.getCustomFieldOptionsForIncidenteProductivo(SEVERIDAD,authenticationContext,searchService,ComponentAccessor.getOptionsManager(),ComponentAccessor.getCustomFieldManager());
        Options fabricaDesarrolloOpciones = GJIRAUtils.getCustomFieldOptionsForIncidenteProductivo(FABRICA_DESARROLLO,authenticationContext,searchService,ComponentAccessor.getOptionsManager(),ComponentAccessor.getCustomFieldManager());
        Options motivoEscalamientoOpciones = GJIRAUtils.getCustomFieldOptionsForIncidenteProductivo(MOTIVO_ESCALAMIENTO,authenticationContext,searchService,ComponentAccessor.getOptionsManager(),ComponentAccessor.getCustomFieldManager());
        List<Issue> epicas = null;
        try {
             epicas = GJIRAUtils.getIssuesOnlyByType("Epic", authenticationContext, searchService);
        }
        catch(Exception ex){
            params.put("mensaje",ex.toString());
        }
        params.put("epicas",epicas);
        params.put("momentoErrorOpciones",momentoErrorOpciones);
        params.put("severidadOpciones",severidadOpciones);
        params.put("fabricaDesarrolloOpciones",fabricaDesarrolloOpciones);
        params.put("motivoEscalamientoOpciones",motivoEscalamientoOpciones);
        params.put("personaAEscalarOpciones",GJIRAUtils.traerIntegrantesDeGrupo("Lideres Estabilizacion",ComponentAccessor.getGroupManager()));
        templateRenderer.render("templates/fourthStep.vm", params,resp.getWriter());
    }
}