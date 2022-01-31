package com.segurosbolivar.plugins;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class PluginServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(PluginServlet.class);

    @ComponentImport
    private final UserManager userManager;

    @ComponentImport
    private final LoginUriProvider loginUriProvider;

    @Inject
    public PluginServlet(UserManager userManager, LoginUriProvider loginUriProvider){
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        //Se obtiene información sobre el usuario que realiza la petición
        UserProfile user = userManager.getRemoteUser(req);

        boolean isUserAdmin = userManager.isSystemAdmin(user.getUserKey());

        String userName = user.getUsername();

        if(userName == null || !isUserAdmin){
            redirectToLogin(req,res);
            return;
        }
        res.setContentType("text/html");
        res.getWriter().write("<html><body>Estamos contrlando acceso de usuario administrador!!</body></html>");
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse res) throws IOException{
        res.sendRedirect(loginUriProvider.getLoginUri(getUri(req)).toASCIIString());
    }

    private URI getUri(HttpServletRequest req){
        StringBuffer builder = req.getRequestURL();
        if(req.getQueryString() != null){
            builder.append("?");
            builder.append(req.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
