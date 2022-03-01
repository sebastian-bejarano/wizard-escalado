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

    @ComponentImport
    private final JiraAuthenticationContext authenticationContext;

    @ComponentImport
    private final IssueLinkService issueLinkService;

    @ComponentImport
    private final TemplateRenderer templateRenderer;

    @Inject
    public FourthStep(TemplateRenderer templateRenderer, JiraAuthenticationContext authenticationContext, IssueLinkService issueLinkService){
        this.templateRenderer = templateRenderer;
        this.authenticationContext = authenticationContext;
        this.issueLinkService = issueLinkService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String,Object> params = new HashMap<String,Object>();
        String proyectoAEscalar = req.getParameter("proyecto");
        String issueAEscalar = req.getParameter("issueKey");
        String problemAEnlazar = req.getParameter("problem");

        String keyProyecto = proyectoAEscalar.split("/")[0].trim();
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueAEscalar);
        Issue problem = ComponentAccessor.getIssueManager().getIssueByCurrentKey(problemAEnlazar);
        //Código del issueLink "Hija"
        IssueLink link = ComponentAccessor.getIssueLinkManager().getIssueLink(274416l);
        params.put("issueLink", link);
        params.put("issue",issue);
        params.put("problem",problem);

        //Usuario actual de la aplicación
        ApplicationUser currentUser = this.authenticationContext.getLoggedInUser();
        //Verificamos que se pueda agregar el link
        ArrayList<String> keysToAdd = new ArrayList<String>();
        keysToAdd.add(problem.getKey());
        IssueLinkService.AddIssueLinkValidationResult result = issueLinkService.validateAddIssueLinks(currentUser,issue,link.getLinkTypeId(), Direction.IN,keysToAdd,true);
        if(result.getErrorCollection().hasAnyErrors()){
            params.put("errors",result.getErrorCollection());
        }
        else{
            issueLinkService.addIssueLinks(currentUser,result);
        }

        templateRenderer.render("templates/fourthStep.vm", params,resp.getWriter());
    }
}