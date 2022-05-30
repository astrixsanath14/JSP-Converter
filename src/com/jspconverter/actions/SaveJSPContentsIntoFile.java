package com.jspconverter.actions;

import java.io.FileWriter;
import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileTypeDescriptor;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.local.LocalFileSystemImpl;
import com.intellij.psi.PsiFile;
import com.jspconverter.ErrorConstants;
import com.jspconverter.JSPConverterAPI;
import com.jspconverter.JSPConverterException;
import com.jspconverter.JSPConverterUtil;

public class SaveJSPContentsIntoFile extends AnAction
{

	@Override public void actionPerformed(@NotNull AnActionEvent anActionEvent)
	{
		PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
		Editor editor = anActionEvent.getDataContext().getData(CommonDataKeys.HOST_EDITOR);
		Integer tabSize = editor.getSettings().getTabSize(anActionEvent.getProject());
		VirtualFile sourceVirtualFile = anActionEvent.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
		Project project = anActionEvent.getProject();
		try
		{
			//PsiFile psiFile = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_FILE);
			//ProblemsCollector p = new ProblemsListener();
			//int problemsCount = p.getFileProblemCount(sourceVirtualFile);

			System.out.println("Going to get JSP contents: " + psiFile.getName());
			String jspContents = JSPConverterAPI.getJSPFileContentsOfJavaFile(psiFile, tabSize);
			System.out.println("JSP contents: " + jspContents);

			String sourceFileName = sourceVirtualFile.getNameWithoutExtension();
			String sourceFileNameWithJSPExtension = sourceFileName + JSPConverterUtil.DOT_JSP;

			JSPFileChooserDescriptor jspFileChooserDescriptor = new JSPFileChooserDescriptor("JSP Destination", "Where do you want to save the JSP?");
			jspFileChooserDescriptor.withFileFilter(virtualFile -> virtualFile.isDirectory() || virtualFile.getName().endsWith(JSPConverterUtil.JSP));
			jspFileChooserDescriptor.setForcedToUseIdeaFileChooser(true);

			ApplicationManager.getApplication().invokeLater(() -> {
				VirtualFile destinationVF = FileChooser.chooseFile(jspFileChooserDescriptor, psiFile.getProject(), sourceVirtualFile);
				if (destinationVF == null)
				{
					return;
				}
				String destinationFilePath = destinationVF.getPath();
				if (destinationVF.isDirectory())
				{
					destinationFilePath += "/" + Messages.showInputDialog("JSP filename", "Enter the JSP file name", null, sourceFileNameWithJSPExtension, new NonEmptyInputValidator());
					if (!JSPConverterUtil.isValidJSPFilePath(destinationFilePath))
					{
						//throw new JSPConverterException(ErrorConstants.INVALID_DATA, "Invalid JSP file path: " + destinationFilePath);
						JSPConverterExceptionHandler.handleException(new JSPConverterException(ErrorConstants.INVALID_DATA, "Invalid JSP file path: " + destinationFilePath));
					}
				}
				else
				{
					if (!destinationVF.getExtension().equals(JSPConverterUtil.JSP))
					{
						destinationFilePath = destinationFilePath.replace(destinationVF.getExtension(), JSPConverterUtil.DOT_JSP);
					}
				}

				try
				{
					FileWriter destinationJSPWriter = new FileWriter(destinationFilePath);
					destinationJSPWriter.write(jspContents);
					destinationJSPWriter.close();
				}
				catch (IOException e)
				{
					//throw new JSPConverterException(ErrorConstants.INVALID_DATA, e.getMessage());
					JSPConverterExceptionHandler.handleException(new JSPConverterException(ErrorConstants.INVALID_DATA, e.getMessage()));
				}
				Notifications.Bus.notify(new Notification(Notifications.SYSTEM_MESSAGES_GROUP_ID, "JSP Converter", "Successfully saved JSP Contents at " + destinationFilePath, NotificationType.INFORMATION));
				LocalFileSystem localFileSystem = new LocalFileSystemImpl();
				VirtualFile destinationFile = localFileSystem.refreshAndFindFileByPath(destinationFilePath);

				int myOrientation = 1;
				FileEditorManagerEx fileEditorManagerEx = FileEditorManagerImpl.getInstanceEx(project);

				fileEditorManagerEx.openTextEditor(new OpenFileDescriptor(project, destinationFile), true);
				fileEditorManagerEx.createSplitter(myOrientation, fileEditorManagerEx.getCurrentWindow());
				for (VirtualFile file : fileEditorManagerEx.getOpenFiles())
				{
					if (file.getName().equals(destinationFile.getName()))
					{
						fileEditorManagerEx.getPrevWindow(fileEditorManagerEx.getCurrentWindow()).closeFile(file);
						break;
					}
				}
			});

		}
		catch (JSPConverterException exception)
		{
			JSPConverterExceptionHandler.handleException(exception);
		}
	}
}
