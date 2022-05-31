package com.jspconverter.actions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Function;
import com.jspconverter.ErrorConstants;
import com.jspconverter.JSPConverterAPI;
import com.jspconverter.JSPConverterException;
import com.jspconverter.JSPConverterUtil;

public class OpenJSPContentUsingPSIInEditor extends AnAction
{
	@Override public void actionPerformed(@NotNull AnActionEvent anActionEvent)
	{
		PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
		Editor editor = anActionEvent.getDataContext().getData(CommonDataKeys.HOST_EDITOR);
		Integer tabSize = editor.getSettings().getTabSize(anActionEvent.getProject());
		Project project = anActionEvent.getProject();
		try
		{
			//PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
			//ProblemsCollector p = new ProblemsListener();
			//int problemsCount = p.getFileProblemCount(virtualFile);

			System.out.println("Going to get JSP contents: " + psiFile.getName());
			String jspContents = JSPConverterAPI.getJSPFileContentsOfJavaFile(psiFile, tabSize);
			System.out.println("JSP contents: " + jspContents);

			//JTextField jTextField = new JTextField(new HTMLDocument(), jspContents, 10000);
			//Messages.showTextAreaDialog(jTextField, "Converted JSP file contents", null, DEFAULT_LINE_PARSER, ParametersListUtil.DEFAULT_LINE_JOINER);

			try
			{
				String prefix = "_" + System.currentTimeMillis();
				LightVirtualFile lightVirtualFile = new LightVirtualFile("JSP_Converter_Temporary_Editor" + prefix + JSPConverterUtil.DOT_JSP);
				lightVirtualFile.setBinaryContent(jspContents.getBytes());

				int myOrientation = 1;
				FileEditorManagerEx fileEditorManagerEx = FileEditorManagerImpl.getInstanceEx(project);
				//fileEditorManagerEx.openTextEditor(new OpenFileDescriptor(project, lightVirtualFile), true);
				fileEditorManagerEx.openFile(lightVirtualFile, true, true);
				fileEditorManagerEx.createSplitter(myOrientation, fileEditorManagerEx.getCurrentWindow());
				for (VirtualFile file : fileEditorManagerEx.getOpenFiles())
				{
					if (file.getName().equals(lightVirtualFile.getName()))
					{
						fileEditorManagerEx.getPrevWindow(fileEditorManagerEx.getCurrentWindow()).closeFile(file);
						break;
					}
				}
			}
			catch (IOException e)
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, e.getMessage());
			}
		}
		catch (JSPConverterException exception)
		{
			JSPConverterExceptionHandler.handleException(exception);
		}
	}

	public static final Function<String, List<String>> DEFAULT_LINE_PARSER = (text) -> Arrays.asList(text.split("\n"));
}
