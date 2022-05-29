package com.jspconverter.actions;

import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.execution.ParametersListUtil;
import com.jspconverter.JSPConverterAPI;
import com.jspconverter.JSPConverterException;

public class OpenJSPContentUsingPSIInEditor extends AnAction
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

			JTextField jTextField = new JTextField(new HTMLDocument(), jspContents, 10000);
			Messages.showTextAreaDialog(jTextField, "Converted JSP file contents", null, DEFAULT_LINE_PARSER, ParametersListUtil.DEFAULT_LINE_JOINER);
		}
		catch (JSPConverterException exception)
		{
			JSPConverterExceptionHandler.handleException(exception);
		}
	}

	public static final Function<String, List<String>> DEFAULT_LINE_PARSER = (text) -> Arrays.asList(text.split("\n"));
}
