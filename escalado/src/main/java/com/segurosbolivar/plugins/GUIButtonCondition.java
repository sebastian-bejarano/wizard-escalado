package com.segurosbolivar.plugins;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginParseException;

import javax.inject.Named;
import java.util.Map;

@Named
public class GUIButtonCondition extends AbstractWebCondition {
    private String issueTypeName;

    @Override
    public void init(Map<String, String> params) throws PluginParseException{
        super.init(params);

        this.issueTypeName = params.get("issueType");
    }

    @Override
    public boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper){
        final Issue issue = (Issue) jiraHelper.getContextParams().get("issue");
        if(issue == null){
            return false;
        }

        final String issueTypeName = issue.getIssueType().getName();
        return issueTypeName.equalsIgnoreCase(this.issueTypeName);
    }
}
