package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.jira.issue.fields.FieldManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GJIRAUtils {

    public static List<Issue> getIssues(String proyecto, JiraAuthenticationContext authenticationContext, SearchService searchService) throws SearchException {
        //Se obtiene el usuario actual para que se haga la búsqueda con los permisos que tenga
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Se crea una nueva cláusula de JQL
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        //De acuerdo a la cláusula de JQL se hace un nuevo Query que se crea a partir de los métodos del ClauseBuilder
        Query query = jqlClauseBuilder.project(proyecto).buildQuery();
        //Hacemos una lista no paginada
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        //Finalmente se obtienen los resultados o se devuelve null
        SearchResults searchResults = searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

    public static List<Issue> getIssuesByType(String proyecto, String issueType, JiraAuthenticationContext authenticationContext, SearchService searchService) throws SearchException{
        //Se obtiene el usuario actual para que se haga la búsqueda con los permisos que tenga
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Se crea una nueva cláusula de JQL
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        //De acuerdo a la cláusula de JQL se hace un nuevo Query que se crea a partir de los métodos del ClauseBuilder, teniendo en cuenta issueType y Proyecto
        Query query = jqlClauseBuilder.project(proyecto).and().issueType(issueType).buildQuery();
        //Hacemos una lista no paginada
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        //Finalmente se obtienen los resultados o se devuelve null
        SearchResults searchResults = searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

    public static List<Issue> getIssuesByTypeAndCategoriaItem(String proyecto, String issueType, String[] categoriaItem, JiraAuthenticationContext authenticationContext, SearchService searchService) throws SearchException{
        //Se obtiene el usuario actual para que se haga la búsqueda con los permisos que tenga
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Se crea una nueva cláusula de JQL
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        //De acuerdo a la cláusula de JQL se hace un nuevo Query que se crea a partir de los métodos del ClauseBuilder, teniendo encuenta issueType, proyecto y Categoría / Item configuración
        Query query = jqlClauseBuilder.project(proyecto).and().issueType(issueType).and().field("cf[10409]").in().functionCascaingOption(categoriaItem[0],categoriaItem[1]).buildQuery();
        //Hacemos una lista no paginada
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        //Finalmente se obtienen los resultados o se devuelve null
        SearchResults searchResults = searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

    public static boolean relacionarIssuesConRelacionado(String issueKey, String problemKey, IssueLinkService issueLinkService, Map params, JiraAuthenticationContext authenticationContext){
        //Obtenemos el issue que vamos a escalar (Que debería ser en este momento tipo incident)
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey);
        //Obtenemos el issue tipo problem con el cuál se va a enlazar
        Issue problem = ComponentAccessor.getIssueManager().getIssueByCurrentKey(problemKey);
        //Código del issueLink "Relacionado"
        //Obtenemos el objeto IssueLink
        IssueLink link = ComponentAccessor.getIssueLinkManager().getIssueLink(274416l);

        //Usuario actual de la aplicación
        ApplicationUser currentUser = authenticationContext.getLoggedInUser();
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
            return false;
        }
        else{
            //En caso contrario hacemos el link del issue
            issueLinkService.addIssueLinks(currentUser,result);
            return true;
        }
    }

    public static boolean crearIncidenteProductivoEnlazado(String projectKey, String mainIssueKey,String problemKey, JiraAuthenticationContext authenticationContext, Map params, ProjectService projectService, ConstantsManager constantsManager, IssueService issueService, IssueLinkService issueLinkService, FieldManager fieldManager){
        //Traemos el usuario que se encuentra loggeado actualmente
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Obtenemos el proyecto al que se va a escalar
        Project project = projectService.getProjectByKey(user, projectKey).getProject();
        //Obtenemos el mainIssue que va a ser escalado
        Issue mainIssue = issueService.getIssue(user,mainIssueKey).getIssue();
        //Obtenemos el problem al que se va a relacionar
        Issue problem = issueService.getIssue(user,problemKey).getIssue();
        IssueType incidenteIssueType = constantsManager.getAllIssueTypeObjects().stream().filter(
                issueType -> issueType.getName().equalsIgnoreCase("Incidente Productivo")).findFirst().orElse(null);
        try {
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
            issueInputParameters.setSummary(problem.getSummary())
                    .setIssueTypeId(incidenteIssueType.getId())
                    .setReporterId(user.getName())
                    .addCustomFieldValue("customfield_15104", "https://jira.segurosbolivar.com/browse/"+mainIssueKey)
                    .addCustomFieldValue("customfield_18009", problem.getCustomFieldValue(fieldManager.getCustomField("customfield_18009")).toString())
                    .setDescription(problem.getDescription())
                    .setProjectId(project.getId());

            IssueService.CreateValidationResult result = issueService.validateCreate(user, issueInputParameters);

            if (result.getErrorCollection().hasAnyErrors()) {
                params.put("mensaje", result.getErrorCollection());
                return false;
            } else {
                Issue nuevoIncidente = issueService.create(user, result).getIssue();
                GJIRAUtils.relacionarIssuesConRelacionado(mainIssue.getKey(), nuevoIncidente.getKey(), issueLinkService, params, authenticationContext);
                return true;
            }
        }
        catch(Exception ex){
            params.put("mensaje", ex.toString());
            return false;
        }
    }
}
