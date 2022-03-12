package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.jira.issue.fields.FieldManager;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GJIRAUtils {

    public static boolean preExistente = false;

    public static List<Issue> getIssuesOnlyByType(String issueType, JiraAuthenticationContext authenticationContext, SearchService searchService) throws SearchException {
        //Se obtiene el usuario actual para que se haga la búsqueda con los permisos que tenga
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Se crea una nueva cláusula de JQL
        JqlClauseBuilder jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
        //De acuerdo a la cláusula de JQL se hace un nuevo Query que se crea a partir de los métodos del ClauseBuilder
        Query query = jqlClauseBuilder.issueType(issueType).buildQuery();
        //Hacemos una lista no paginada
        PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
        //Finalmente se obtienen los resultados o se devuelve null
        SearchResults searchResults = searchService.search(user, query, pagerFilter);
        return searchResults != null ? searchResults.getResults() : null;
    }

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
        Query query = jqlClauseBuilder.project(proyecto).and().issueType(issueType).and().field("cf[10409]").in().functionCascaingOption(categoriaItem[0],categoriaItem[1]).and().status().notIn().strings("Resolved","Canceled","Closed").buildQuery();
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

    public static boolean crearIncidenteProductivoEnlazado(String projectKey, String mainIssueKey, String problemKey, String nuevoNombre,String prioridad, String momentoError, String severidad, String fabricaDesarrollo, String motivoEscalamiento, String epica, String nuevoResponsable,JiraAuthenticationContext authenticationContext, Map params, ProjectService projectService, ConstantsManager constantsManager, IssueService issueService, IssueLinkService issueLinkService, FieldManager fieldManager, CustomFieldManager customFieldManager, OptionsManager optionsManager, SearchService searchService) throws ServletException{

        //Traemos el usuario que se encuentra loggeado actualmente
        ApplicationUser user = authenticationContext.getLoggedInUser();
        //Obtenemos el proyecto al que se va a escalar
        Project project = projectService.getProjectByKey(user, projectKey).getProject();
        //Obtenemos el mainIssue que va a ser escalado
        Issue mainIssue = issueService.getIssue(user,mainIssueKey).getIssue();
        //Obtenemos el problem al que se va a relacionar
        Issue problem = !problemKey.equalsIgnoreCase("Service Request") ? issueService.getIssue(user, problemKey).getIssue(): null;
        IssueType incidenteIssueType = constantsManager.getAllIssueTypeObjects().stream().filter(
                issueType -> issueType.getName().equalsIgnoreCase("Incidente Productivo")).findFirst().orElse(null);
        //Valor del custom field Centro de desarrollo
        CustomField centroDesarrollo = customFieldManager.getCustomFieldObject("customfield_18009");
        Options opcionesDisponiblesCentroDesarrollo = !problemKey.equalsIgnoreCase("Service Request") ? optionsManager.getOptions(centroDesarrollo.getRelevantConfig(problem)) : optionsManager.getOptions(centroDesarrollo.getRelevantConfig(mainIssue));
        String opcionParaBuscar = !problemKey.equalsIgnoreCase("Service Request") ? problem.getCustomFieldValue(fieldManager.getCustomField("customfield_18009")).toString() : mainIssue.getCustomFieldValue(fieldManager.getCustomField("customfield_18009")).toString();
        //throw new ServletException(opcionParaBuscar);
        Option opcionParaPoner = opcionesDisponiblesCentroDesarrollo.stream().filter(opcion->opcion.getValue().equalsIgnoreCase(opcionParaBuscar)).findFirst().get();
        //Valor del custom field Aplicación
        Object categoriaItem = !problemKey.equalsIgnoreCase("Service Request") ? problem.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10409")) : mainIssue.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10409"));
        String categoriaItemString = categoriaItem.toString();
        categoriaItemString = categoriaItemString.split(",")[0].split("=")[1] +"-"+ removeLastChar(categoriaItemString.split(",")[1].split("=")[1]);
        String[] categoria_item = categoriaItemString.split("-");
        CustomField aplicacion = customFieldManager.getCustomFieldObject("customfield_10414");
        //obtenemos un issue incidente productivo cualquiera para tener la lista
        Issue incidenteCualquiera;
        try {
            incidenteCualquiera = getIssuesOnlyByType("Incidente Productivo", authenticationContext, searchService).stream().findFirst().get();
        }
        catch(SearchException ex){
            return false;
        }
        if(incidenteCualquiera == null){
            return false;
        }
        Options opcionesDisponiblesAplicacion = optionsManager.getOptions(aplicacion.getRelevantConfig(incidenteCualquiera));
        Option opcionParaAplicacion = opcionesDisponiblesAplicacion.stream().filter(opcion -> opcion.getValue().equalsIgnoreCase(categoria_item[1])).findFirst().get();
        try {
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
            issueInputParameters.setSummary(!problemKey.equalsIgnoreCase("Service Request") ? problem.getSummary() : nuevoNombre)
                    .setIssueTypeId(incidenteIssueType.getId())
                    .setReporterId(user.getName())
                    .setAssigneeId(nuevoResponsable)
                    .setPriorityId(prioridad)
                    .addCustomFieldValue("customfield_15104", "https://jira.segurosbolivar.com/browse/"+mainIssueKey)
                    .addCustomFieldValue("customfield_18009", opcionParaPoner.getOptionId().toString())
                    .addCustomFieldValue("customfield_10414", opcionParaAplicacion.getOptionId().toString())
                    .addCustomFieldValue("customfield_14300", momentoError)
                    .addCustomFieldValue("customfield_10432", severidad)
                    .addCustomFieldValue("customfield_15700", fabricaDesarrollo)
                    .addCustomFieldValue("customfield_14501", motivoEscalamiento)
                    .addCustomFieldValue("customfield_10102",epica)
                    .setDescription(!problemKey.equalsIgnoreCase("Service Request") ? problem.getDescription() : mainIssue.getDescription())
                    .setProjectId(project.getId());

            IssueService.CreateValidationResult result = issueService.validateCreate(user, issueInputParameters);

            if (result.getErrorCollection().hasAnyErrors()) {
                params.put("mensaje", result.getErrorCollection());
                return false;
            } else {
                IssueService.IssueResult nuevoIncidente = issueService.create(user, result);
                params.put("validation",result.isValid());
                params.put("nuevoIncidente",nuevoIncidente.getErrorCollection());
                GJIRAUtils.relacionarIssuesConRelacionado(mainIssue.getKey(), nuevoIncidente.getIssue().getKey(), issueLinkService, params, authenticationContext);
                IssueService.TransitionValidationResult statusUpdateResult = issueService.validateTransition(user,nuevoIncidente.getIssue().getId(),391,issueInputParameters);
                if(statusUpdateResult.getErrorCollection().hasAnyErrors()){
                    return false;
                }
                else{
                    issueService.transition(user,statusUpdateResult);
                    return true;
                }
            }
        }
        catch(Exception ex){
            params.put("mensaje", ex.toString());
            ex.printStackTrace();
            return false;
        }
    }

    public static Options getCustomFieldOptionsForIncidenteProductivo(String customFieldName,JiraAuthenticationContext authenticationContext, SearchService searchService, OptionsManager optionsManager, CustomFieldManager customFieldManager){
        Issue incidenteCualquiera;
        CustomField customField = customFieldManager.getCustomFieldObject(customFieldName);
        try {
            incidenteCualquiera = getIssuesOnlyByType("Incidente Productivo", authenticationContext, searchService).stream().findFirst().get();
        }
        catch(SearchException ex){
            return null;
        }
        Options opcionesDisponibles = optionsManager.getOptions(customField.getRelevantConfig(incidenteCualquiera));
        return opcionesDisponibles;
    }

    public static boolean updateIssueStatus(Issue issue, boolean isServiceRequest,String nuevoResponsable, JiraAuthenticationContext authenticationContext, IssueService issueService){
        //Traemos el usuario que se encuentra loggeado actualmente
        ApplicationUser user = authenticationContext.getLoggedInUser();
        IssueInputParameters inputParameters = issueService.newIssueInputParameters()
                .setAssigneeId(nuevoResponsable)
                .setStatusId("10258")
                .addCustomFieldValue("customfield_10439",GJIRAUtils.preExistente ? "11274" : "23801");
        IssueService.UpdateValidationResult result = issueService.validateUpdate(user, issue.getId(), inputParameters);
        if(result.getErrorCollection().hasAnyErrors()){
            return false;
        }
        else{
            issueService.update(user,result);
            IssueService.TransitionValidationResult statusResult = issueService.validateTransition(user,issue.getId(),!isServiceRequest ? 161 : 961,inputParameters);
            if(statusResult.getErrorCollection().hasAnyErrors()){
                return false;
            }else {
                issueService.transition(user, statusResult);
                return true;
            }
        }
    }

    public static IssueService.CreateValidationResult crearProblemaAsociado(JiraAuthenticationContext authenticationContext, IssueService issueService, Issue incident, FieldManager fieldManager, CustomFieldManager customFieldManager, OptionsManager optionsManager, ProjectManager projectManager){
        ApplicationUser user = authenticationContext.getLoggedInUser();
        IssueInputParameters inputParameters = issueService.newIssueInputParameters()
                .setIssueTypeId("10213")
                .setProjectId(projectManager.getProjectByCurrentKey("PPP").getId())
                .setSummary(incident.getSummary())
                .setDescription(incident.getDescription())
                .setReporterId(user.getName())
                .setAssigneeId(user.getName())
                .setPriorityId(incident.getPriority().getId())
                .addCustomFieldValue("customfield_18009", getOptionIdFromCustomField("customfield_18009",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager))
                .addCustomFieldValue("customfield_10337", getOptionIdFromCustomField("customfield_10337",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager))
                .addCustomFieldValue("customfield_10343", getOptionIdFromCustomField("customfield_10343",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager))
                .addCustomFieldValue("customfield_10439", getOptionIdFromCustomField("customfield_10439",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager))
                .addCustomFieldValue("customfield_10409", getOptionIdFromCustomField("customfield_10409",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager).split("-")[0])
                .addCustomFieldValue("customfield_10409:1", getOptionIdFromCustomField("customfield_10409",incident,authenticationContext,issueService,customFieldManager,optionsManager,fieldManager).split("-")[1]);
        IssueService.CreateValidationResult result =  issueService.validateCreate(user,inputParameters);
        return result;
    }

    public static String getOptionIdFromCustomField(String customfield,Issue issue,JiraAuthenticationContext authenticationContext, IssueService issueService, CustomFieldManager customFieldManager, OptionsManager optionsManager, FieldManager fieldManager){
        CustomField centroDesarrollo = customFieldManager.getCustomFieldObject(customfield);
        Options opcionesDisponiblesCentroDesarrollo = optionsManager.getOptions(centroDesarrollo.getRelevantConfig(issue));
        if(customfield.equalsIgnoreCase("customfield_10409")) {
            Object categoriaItem = issue.getCustomFieldValue(ComponentAccessor.getFieldManager().getCustomField("customfield_10409"));
            String categoriaItemString = categoriaItem.toString();
            categoriaItemString = categoriaItemString.split(",")[0].split("=")[1] +"-"+ removeLastChar(categoriaItemString.split(",")[1].split("=")[1]);
            String categoria = categoriaItemString.split("-")[0];
            String item = categoriaItemString.split("-")[1];
            Option Categoria = opcionesDisponiblesCentroDesarrollo.stream().filter(opcion -> opcion.getValue().equalsIgnoreCase(categoria)).findFirst().get();
            Option Item = Categoria.getChildOptions().stream().filter(opcion->opcion.getValue().equalsIgnoreCase(item)).findFirst().get();
            return String.valueOf(Categoria.getOptionId())+"-"+String.valueOf(Item.getOptionId());
        }else{
            Option opcionParaPoner = opcionesDisponiblesCentroDesarrollo.stream().filter(opcion->opcion.getValue().equalsIgnoreCase(issue.getCustomFieldValue(fieldManager.getCustomField(customfield)).toString())).findFirst().get();
            return String.valueOf(opcionParaPoner.getOptionId());
        }
    }
    public static Collection<ApplicationUser> traerIntegrantesDeGrupo(String nombreGrupo,GroupManager groupManager){
        return groupManager.getDirectUsersInGroup(groupManager.getGroup(nombreGrupo));
    }
    public static String removeLastChar(String s) {
        return (s == null || s.length() == 0)
                ? null
                : (s.substring(0, s.length() - 1));
    }
}
