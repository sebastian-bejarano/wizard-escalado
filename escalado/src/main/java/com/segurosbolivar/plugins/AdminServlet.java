package com.segurosbolivar.plugins;

//Import de Input Output Exception
import java.io.IOException;

//Imports del manejo de Servlet y peticiones
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//Imports del manejo de login para administrador
import java.net.URI;

//Imports para el mapeo de propiedades
import java.util.HashMap;
import java.util.Map;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;

//Import del template renderer para velocity
import com.atlassian.templaterenderer.TemplateRenderer;

//Import para guardar las configuraciones del plugin
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

//Imports de dependencias para hacer inyección e importar componentes de JIRA
import javax.inject.Inject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

public class AdminServlet extends HttpServlet{

    private static final String PLUGIN_STORAGE_KEY = "com.segurosbolivar.plugins";
    //Se importa el user manager de JIRA para mirar el loggeo
    @ComponentImport
    private final UserManager userManager;
    //Se importa el loginUriProvider para hacer la redirección al login en caso de que no esté loggeado
    @ComponentImport
    private final LoginUriProvider loginUriProvider;
    //Se importa el template renderer para renderizar el template de velocity
    @ComponentImport
    private final TemplateRenderer renderer;
    //Se importa el PluginSettingsFactory para guardar las propiedades del plugin y valores importantes
    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    //Constructor para hacer la inyección de dependencias hacia el plugin
    @Inject
    public AdminServlet(UserManager userManager, LoginUriProvider loginUriProvider, TemplateRenderer renderer, PluginSettingsFactory pluginSettingsFactory){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.renderer = renderer;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    //Método para controlar la petición get al servlet
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{

        //Inicio de control de inicio de sesión de administrador
        String username = userManager.getRemoteUsername(request);
        if(username == null || !userManager.isSystemAdmin(username)){
            redirectToLogin(request,response);
            return;
        }
        //Fin de control de inicio de sesión de administrador
        //Inicio del mapeo de propiedades del plugin
        Map<String, Object> context = new HashMap<String, Object>();

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        if(pluginSettings.get(PLUGIN_STORAGE_KEY+".regla1") == null){
            String noRule1 = "";
            pluginSettings.put(PLUGIN_STORAGE_KEY+".regla1", noRule1);
        }
        if(pluginSettings.get(PLUGIN_STORAGE_KEY+".regla2") == null){
            String noRule2 = "";
            pluginSettings.put(PLUGIN_STORAGE_KEY+".regla2", noRule2);
        }
        if(pluginSettings.get(PLUGIN_STORAGE_KEY+".regla3") == null){
            String noRule3 = "";
            pluginSettings.put(PLUGIN_STORAGE_KEY+".regla3", noRule3);
        }
        if(pluginSettings.get(PLUGIN_STORAGE_KEY+".regla4") == null){
            String noRule4 = "";
            pluginSettings.put(PLUGIN_STORAGE_KEY+".regla4", noRule4);
        }
        if(pluginSettings.get(PLUGIN_STORAGE_KEY+".regla5") == null){
            String noRule5 = "";
            pluginSettings.put(PLUGIN_STORAGE_KEY+".regla5", noRule5);
        }

        context.put("regla1",pluginSettings.get(PLUGIN_STORAGE_KEY+".regla1"));
        context.put("regla2",pluginSettings.get(PLUGIN_STORAGE_KEY+".regla2"));
        context.put("regla3",pluginSettings.get(PLUGIN_STORAGE_KEY+".regla3"));
        context.put("regla4",pluginSettings.get(PLUGIN_STORAGE_KEY+".regla4"));
        context.put("regla5",pluginSettings.get(PLUGIN_STORAGE_KEY+".regla5"));
        //Fin del mapeo de propiedades del plugin
        //Inicio de renderización del formulario
        response.setContentType("text/html; charset=utf-8");
        renderer.render("templates/admin.vm", context,response.getWriter());
        //Fin de renderización del formulario
    }
    //Método para manejo del post del formulario de las propiedades o reglas
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();

        for(int i = 1; i <= 5; i++){
            if(request.getParameter("regla"+String.valueOf(i)) != null){
                pluginSettings.put(PLUGIN_STORAGE_KEY+".regla"+String.valueOf(i),new String("checked"));
            }
            else{
                pluginSettings.put(PLUGIN_STORAGE_KEY+".regla"+String.valueOf(i),new String(""));
            }
        }
        response.sendRedirect("admin");
    }

    //Método para hacer la redirección al login.
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException{
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    //Método para obtener el URI de la petición que se está haciendo.
    private URI getUri(HttpServletRequest request){
        StringBuffer builder = request.getRequestURL();
        if(request.getQueryString() != null){
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}