package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.Issue;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FourthStep extends HttpServlet {

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
    public FourthStep(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, IssueLinkService issueLinkService){
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkService = issueLinkService;
    }

    //Handler del método POST en el servlet
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Creamos un mapa de Hash para inyectar parámetros que serán enviados a velocity
        Map<String,Object> params = new HashMap<String,Object>();
        //Obtenemos los parámetros que vienen en el request
        String proyectoAEscalar = req.getParameter("proyecto");
        String issueAEscalar = req.getParameter("issueKey");
        String problemAEnlazar = req.getParameter("problem");
        //El key de proyecto y el nombre de proyecto están separados por un /
        String keyProyecto = proyectoAEscalar.split("/")[0].trim();
        //Obtenemos el issue que vamos a escalar (Que debería ser en este momento tipo incident)
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueAEscalar);
        //Obtenemos el issue tipo problem con el cuál se va a enlazar
        Issue problem = ComponentAccessor.getIssueManager().getIssueByCurrentKey(problemAEnlazar);
        //Código del issueLink "Relacionado"
        //Obtenemos el objeto IssueLink
        IssueLink link = ComponentAccessor.getIssueLinkManager().getIssueLink(274416l);
        //Colocamos los parámetros en el mapa hash para que sean enviados al template de velocity
        params.put("issueLink", link);
        params.put("issue",issue);
        params.put("problem",problem);
        params.put("project",proyectoAEscalar);
        //Usuario actual de la aplicación
        ApplicationUser currentUser = this.authenticationContext.getLoggedInUser();
        //Verificamos que se pueda agregar el link
        //El método necesita una lista de Keys de Issues con los que se va a mirar si sí se puede hacer el link
        ArrayList<String> keysToAdd = new ArrayList<String>();
        keysToAdd.add(problem.getKey());
        //Finalmente acá se mira el resultado, en orden los parámetros del método son
        //Usuario que intenta hacer el link, issue desde el cual se va a hacer el link, ID del link, la dirección de link ya que tiene outward e inward, y pues
        //finalmente los keys con los cuales se va a enlazar y un parámetro que dice que ignore los links del sistema, ya que "Relacionado" fue creado por GJIRA
        IssueLinkService.AddIssueLinkValidationResult result = issueLinkService.validateAddIssueLinks(currentUser,issue,link.getLinkTypeId(), Direction.IN,keysToAdd,true);
        if(result.getErrorCollection().hasAnyErrors()){
            //Mostramos los errores en caso de que haya
            params.put("errors",result.getErrorCollection());
        }
        else{
            //En caso contrario hacemos el link del issue
            issueLinkService.addIssueLinks(currentUser,result);
        }

        templateRenderer.render("templates/fourthStep.vm", params,resp.getWriter());
    }
}