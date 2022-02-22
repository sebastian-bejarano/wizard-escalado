package com.segurosbolivar.plugins.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

/*Para trabajar con condiciones vamos a crear una clase que herede del AbstractWebCondition*/

public class GUIButtonCondition extends AbstractWebCondition {

    @ComponentImport
    private final GroupManager groupManager;

    private String grupo;
    private String issueType1;
    private String issueType2;

    @Inject
    public GUIButtonCondition(GroupManager groupManager){
        this.groupManager = groupManager;
    }

    //Inicializamos el mapa de parámetros de la clase padre
    @Override
    public void init(Map<String, String> params) throws PluginParseException{
        super.init(params);

        this.issueType1 = params.get("issueType1");
        this.issueType2 = params.get("issueType2");
        this.grupo = params.get("grupo");
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
        //Verificamos que el usuario se encuentre en el grupo
        final Collection<String> gruposDelUsuario = groupManager.getGroupNamesForUser(applicationUser);

        //Finalmente volvemos el booleano del issuetype con el que queremos comparar, el cual es traído del XML
        return (issueTypeName.equalsIgnoreCase(this.issueType2)||issueTypeName.equalsIgnoreCase(this.issueType1)) && gruposDelUsuario.contains(grupo);
    }
}
