/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.editors.sql;

import org.eclipse.jface.action.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;
import org.jkiss.dbeaver.core.CoreCommands;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverActivator;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.ui.ActionUtils;
import org.jkiss.dbeaver.ui.editors.sql.handlers.CopyUnformattedTextAction;

import java.util.ResourceBundle;

/**
 * SQL Editor contributor
 */
public class SQLEditorContributor extends BasicTextEditorActionContributor
{
    static final String ACTION_CONTENT_ASSIST_PROPOSAL = "ContentAssistProposal"; //$NON-NLS-1$
    static final String ACTION_CONTENT_ASSIST_TIP = "ContentAssistTip"; //$NON-NLS-1$
    static final String ACTION_CONTENT_FORMAT_PROPOSAL = "ContentFormatProposal"; //$NON-NLS-1$

    private SQLEditorBase activeEditorPart;

    private RetargetTextEditorAction contentAssistProposal;
    private RetargetTextEditorAction contentAssistTip;
    private RetargetTextEditorAction contentFormatProposal;
    private CopyUnformattedTextAction copyUnformattedTextAction;

    public SQLEditorContributor()
    {
        super();

        createActions();
    }

    static String getActionResourcePrefix(String actionId)
    {
        return "actions_" + actionId + "_";//$NON-NLS-1$
    }

    protected boolean isNestedEditor()
    {
        return false;
    }

    private void createActions()
    {
        // Init custom actions
        ResourceBundle bundle = DBeaverActivator.getCoreResourceBundle();
        contentAssistProposal = new RetargetTextEditorAction(bundle, getActionResourcePrefix(ACTION_CONTENT_ASSIST_PROPOSAL));
        contentAssistProposal.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
        contentFormatProposal = new RetargetTextEditorAction(bundle, getActionResourcePrefix(ACTION_CONTENT_FORMAT_PROPOSAL));
        contentFormatProposal.setActionDefinitionId(CoreCommands.CMD_CONTENT_FORMAT);
        contentAssistTip = new RetargetTextEditorAction(bundle, getActionResourcePrefix(ACTION_CONTENT_ASSIST_TIP));
        contentAssistTip.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
        copyUnformattedTextAction = new CopyUnformattedTextAction();
    }

    @Override
    public void dispose()
    {
        setActiveEditor(null);

        super.dispose();
    }

    @Override
    public void setActiveEditor(IEditorPart targetEditor)
    {
        super.setActiveEditor(targetEditor);

        if (activeEditorPart == targetEditor) {
            return;
        }
        if (targetEditor instanceof SQLEditorBase) {
        	activeEditorPart = (SQLEditorBase)targetEditor;
        } else {
        	activeEditorPart = null;
        }

        if (activeEditorPart != null) {
            // Update editor actions
            contentAssistProposal.setAction(getAction(activeEditorPart, ACTION_CONTENT_ASSIST_PROPOSAL)); //$NON-NLS-1$
            contentAssistTip.setAction(getAction(activeEditorPart, ACTION_CONTENT_ASSIST_TIP)); //$NON-NLS-1$
            contentFormatProposal.setAction(getAction(activeEditorPart, ACTION_CONTENT_FORMAT_PROPOSAL)); //$NON-NLS-1$
            copyUnformattedTextAction.setEditor(activeEditorPart);
        }
    }

    @Override
    public void init(IActionBars bars)
    {
        super.init(bars);
    }

    @Override
    public void contributeToMenu(IMenuManager manager)
    {
        if (!isNestedEditor()) {
            super.contributeToMenu(manager);
        }

        if (!isNestedEditor()) {
            IWorkbenchWindow window = DBeaverUI.getActiveWorkbenchWindow();
            IMenuManager editMenu = manager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
            if (editMenu != null) {
                editMenu.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS, ActionUtils.makeCommandContribution(window, ITextEditorActionDefinitionIds.BLOCK_SELECTION_MODE));
                editMenu.add(contentAssistProposal);
                editMenu.add(contentAssistTip);
                MenuManager formatMenu = new MenuManager(CoreMessages.actions_menu_edit_ContentFormat);
                editMenu.add(formatMenu);
                formatMenu.add(contentFormatProposal);
                formatMenu.add(copyUnformattedTextAction);
            }
            IMenuManager navMenu = manager.findMenuUsingPath(IWorkbenchActionConstants.M_NAVIGATE);
            if (navMenu != null) {
                navMenu.add(new Separator());
                navMenu.add(ActionUtils.makeCommandContribution(window, CoreCommands.CMD_SQL_QUERY_NEXT));
                navMenu.add(ActionUtils.makeCommandContribution(window, CoreCommands.CMD_SQL_QUERY_PREV));
            }
        }
    }

    @Override
    public void contributeToCoolBar(ICoolBarManager manager)
    {
        if (!isNestedEditor()) {
            super.contributeToCoolBar(manager);
        }
    }

    @Override
    public void contributeToToolBar(IToolBarManager manager)
    {
        if (!isNestedEditor()) {
            super.contributeToToolBar(manager);
            if (manager.find(CoreCommands.CMD_EXECUTE_STATEMENT) == null) {
                manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_EXECUTE_STATEMENT));
                manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_EXECUTE_STATEMENT_NEW));
                manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_EXECUTE_SCRIPT));
                manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_EXECUTE_SCRIPT_NEW));
                //manager.add(new Separator());
                manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_EXPLAIN_PLAN));
                //manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_ANALYSE_STATEMENT));
                //manager.add(ActionUtils.makeCommandContribution(getPage().getWorkbenchWindow(), CoreCommands.CMD_VALIDATE_STATEMENT));
            }
        }
    }

    @Override
    public void contributeToStatusLine(IStatusLineManager statusLineManager)
    {
        if (!isNestedEditor()) {
            super.contributeToStatusLine(statusLineManager);
        }
    }

}
