package com.segurosbolivar.plugins;

import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;

import java.util.ArrayList;
import java.util.HashMap;

public class GJIRAUtils {
    static boolean relacionarIssuesConRelacionado(String issue1Key, String issue2Key, IssueLinkService issueLinkService, HashMap params, JiraAuthenticationContext authenticationContext){
        //Obtenemos el issue que vamos a escalar (Que debería ser en este momento tipo incident)
        Issue issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issue1Key);
        //Obtenemos el issue tipo problem con el cuál se va a enlazar
        Issue problem = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issue2Key);
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
}
