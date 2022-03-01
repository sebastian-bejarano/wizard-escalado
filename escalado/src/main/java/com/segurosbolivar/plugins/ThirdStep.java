package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.query.Query;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThirdStep extends HttpServlet {

    private final String CAMPO_CATEGORIA_ITEM = "customfield_10409";
    private final Long CAMPO_CATEGORIA_ITEM_ID = 10409l;
    @ComponentImport
    private final JiraAuthenticationContext authenticationContext;

    @ComponentImport
    private final SearchService searchService;

    @ComponentImport
    private final TemplateRenderer templateRenderer;

    @Inject
    public ThirdStep(JiraAuthenticationContext authenticationContext, SearchService searchService, TemplateRenderer templateRenderer){
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //Verificamos el valor del parámetro proyecto de la petición anterior
        String proyectoAEscalar = req.getParameter("proyecto");
        //Obtenemos un objecto de tipo Project para visualizarlo en campo de información no editable
        Project proyecto = ComponentAccessor.getProjectManager().getProjectByCurrentKey(proyectoAEscalar);
        //Mapa de parámetros a renderizar
        Map<String,Object> params = new HashMap<String,Object>();
        final String mainIssueKey = req.getParameter("issueKey");
        //Obtenemos todos los issues tipo Problem para el proyecto especificado que tengan el campo Categoría Item igual que el Incident
        //Lo primero es obtener el campo categoría Item del issue actual
        final Issue actualIssue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(mainIssueKey);
        Object categoriaItem = actualIssue.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField(CAMPO_CATEGORIA_ITEM));
        String categoriaItemString = categoriaItem.toString();
        categoriaItemString = categoriaItemString.split(",")[0].split("=")[1] +"-"+ removeLastChar(categoriaItemString.split(",")[1].split("=")[1]);
        String[] categoria_item = categoriaItemString.split("-");
        //Fin de obtención del campo categoría Item
        try {
            List<Issue> problemsConEseCategoriaItem = getIssuesByTypeAndCategoriaItem("MDSB","Problem",categoria_item);
            params.put("problems",problemsConEseCategoriaItem);
            params.put("issueKey",mainIssueKey);
            params.put("proyectoAEscalar",proyecto);
        }
        catch(SearchException e){
            e.printStackTrace();
        }

        templateRenderer.render("templates/thirdStep.vm", params,resp.getWriter());
    }

    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }

    private List<Issue> getIssues(String proyecto) throws SearchException{
        ApplicationUser user = this.authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(proyecto).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = this.searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

    private List<Issue> getIssuesByType(String proyecto, String issueType) throws SearchException{
        ApplicationUser user = this.authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(proyecto).and().issueType(issueType).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();

        SearchResults searchResults = this.searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

    private List<Issue> getIssuesByTypeAndCategoriaItem(String proyecto, String issueType, String[] categoriaItem) throws SearchException{
        ApplicationUser user = this.authenticationContext.getLoggedInUser();
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        Query query = jqlClauseBuilder.project(proyecto).and().issueType(issueType).and().field("cf[10409]").in().functionCascaingOption(categoriaItem[0],categoriaItem[1]).buildQuery();
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        SearchResults searchResults = this.searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }
}
