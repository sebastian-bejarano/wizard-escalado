package com.segurosbolivar.plugins;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;

import javax.inject.Named;
import java.util.Map;

/*Para trabajar con condiciones vamos a crear una clase que herede del AbstractWebCondition*/
@Named
public class GUIButtonCondition extends AbstractWebCondition {
    private String issueTypeName;

    //Inicializamos el mapa de parámetros de la clase padre
    @Override
    public void init(Map<String, String> params) throws PluginParseException{
        super.init(params);

        this.issueTypeName = params.get("issueType");
    }

    //Ahora con base en el JiraHelper vamos a ver qué tiene el issue en el que está parado ese botón
    //Con el método shouldDisplay es que vamos a verificar que tenga que mostrarse o no
    //Tiene como parámetros el usuario de la aplicación y la inyección del jiraHelper
    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper){
        //JiraHelper para obtener el issue
        final Issue issue = (Issue) jiraHelper.getContextParams().get("issue");
        if(issue == null){
            return false;
        }
        //Recordemos que el issueType es un campo compuesto por lo que debemos sacar el nombre de ahí
        final String issueTypeName = issue.getIssueType().getName();
        //Finalmente volvemos el booleano del issuetype con el que queremos comparar, el cual es traído del XML
        return issueTypeName.equalsIgnoreCase(this.issueTypeName);
    }
}
