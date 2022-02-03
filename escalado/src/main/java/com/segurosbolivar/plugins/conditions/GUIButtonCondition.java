package com.segurosbolivar.plugins.conditions;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/*Para trabajar con condiciones vamos a crear una clase que herede del AbstractWebCondition*/

public class GUIButtonCondition extends AbstractWebCondition {

    @ComponentImport
    private final PermissionManager permissionManager;

    private String issueTypeName1;
    private String issueTypeName2;
    private String permission;

    @Inject
    public GUIButtonCondition(PermissionManager permissionManager){
        this.permissionManager = permissionManager;
    }

    //Inicializamos el mapa de parámetros de la clase padre
    @Override
    public void init(Map<String, String> params) throws PluginParseException{
        super.init(params);

        this.issueTypeName1 = params.get("issueType1");
        this.issueTypeName2 = params.get("issueType2");
        this.permission = params.get("permission");
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
        //Verificamos que tenga permisos de administrador
        final boolean hasAdminPermissions = permissionManager.hasPermission(Permissions.ADMINISTER,applicationUser);
        //Finalmente volvemos el booleano del issuetype con el que queremos comparar, el cual es traído del XML
        return (issueTypeName.equalsIgnoreCase(this.issueTypeName1)||issueTypeName.equalsIgnoreCase(this.issueTypeName2)) && hasAdminPermissions;
    }
}
