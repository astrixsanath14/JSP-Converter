package com.jspconverter.actions;

import java.awt.datatransfer.StringSelection;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.psi.PsiFile;
import com.jspconverter.JSPConverterAPI;
import com.jspconverter.JSPConverterException;

public class CopyJSPContentUsingPSIToClipBoard extends AnAction
{
	@Override public void actionPerformed(@NotNull AnActionEvent anActionEvent)
	{
		PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
		Editor editor = anActionEvent.getDataContext().getData(CommonDataKeys.HOST_EDITOR);
		Integer tabSize = editor.getSettings().getTabSize(anActionEvent.getProject());
		try
		{
			//PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
			//ProblemsCollector p = new ProblemsListener();
			//int problemsCount = p.getFileProblemCount(virtualFile);

			System.out.println("Going to get JSP contents: " + psiFile.getName());
			String jspContents = JSPConverterAPI.getJSPFileContentsOfJavaFile(psiFile, tabSize);
			System.out.println("JSP contents: " + jspContents);

			CopyPasteManager.getInstance().setContents(new StringSelection(jspContents));
			//Messages.showInfoMessage("Successfully copied to ClipBoard", "JSP Converter");
			Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "JSP Converter", "Successfully copied JSP Contents to ClipBoard", NotificationType.INFORMATION));
		}
		catch (JSPConverterException exception)
		{
			JSPConverterExceptionHandler.handleException(exception);
		}
	}
}
