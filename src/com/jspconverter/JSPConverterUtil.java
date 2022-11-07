package com.jspconverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jspconverter.util.StringUtil;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiImportStaticStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;

public class JSPConverterUtil
{
	private static final String NEWLINE_PREFIX = "";
	private static final String COMMENT_PREFIX = "<%--";
	private static final String COMMENT_SUFFIX = "--%>";
	private static final String NEWLINE_STR = "\n";
	private static final String DEFAULT_TAB_STR = "\t";
	private static final String MAIN_LOWERCASE = "main";
	private static final String DOT = ".";
	public final static String JSP = "jsp";
	public final static String DOT_JSP = DOT + "jsp";

	static String getJSPFileContentsOfJavaFile(PsiFile psiFile, Integer tabSize) throws JSPConverterException
	{
		validateJavaFileType(psiFile);
		try
		{
			String tabStr = DEFAULT_TAB_STR;
			if (tabSize != null)
			{
				tabStr = repeat(tabSize, " ");
			}
			PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
			StringBuilder sb = new StringBuilder();

			sb.append(COMMENT_PREFIX + " Imports " + COMMENT_SUFFIX);
			sb.append(NEWLINE_PREFIX + NEWLINE_STR);

			for (String jspImport : convertAnGetJavaImportsAsJSPImports(psiJavaFile))
			{
				sb.append(jspImport);
				sb.append(NEWLINE_PREFIX + NEWLINE_STR);
			}
			sb.append(NEWLINE_PREFIX + NEWLINE_STR);

			int classCount = psiJavaFile.getClasses().length;
			if (classCount == 0)
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, "No class found. Please ensure there is 1 class.");
			}
			if (classCount > 1)
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, "More than 1 class found. Please ensure there is 1 class.");
			}
			PsiClass javaClass = psiJavaFile.getClasses()[0];

			int nestedClassCount = javaClass.getInnerClasses().length;
			if (nestedClassCount != 0)
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, "Inner classes aren't supported.");
			}
			int methodCount = javaClass.getMethods().length;
			if (methodCount == 0)
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, "No methods found. Please ensure there is at least 1 method.");
			}

			PsiMethod[] javaMethods = javaClass.getMethods();
			List<PsiMethod> mainMethods = Arrays.stream(javaMethods).filter(javaMethod -> MAIN_LOWERCASE.equalsIgnoreCase(javaMethod.getName())).collect(Collectors.toList());

			boolean isSingleMainMethodPresent = mainMethods.size() == 1;
			int index = 0;
			PsiMethod mainMethod;
			if (isSingleMainMethodPresent)
			{
				mainMethod = mainMethods.get(0);
			}
			else
			{
				mainMethod = javaMethods[index++];
			}
			boolean useStatements = false;
			appendMainMethodContents(mainMethod, sb, tabStr, useStatements);
			int remainingMethodCount = javaMethods.length - 1;
			boolean hasOtherMethods = remainingMethodCount > 0;
			if (hasOtherMethods)
			{
				sb.append(COMMENT_PREFIX + " Other methods " + COMMENT_SUFFIX);
				sb.append(NEWLINE_PREFIX + NEWLINE_STR);

				sb.append("<%!");
				sb.append(NEWLINE_PREFIX + NEWLINE_STR);

				while (index < javaMethods.length)
				{
					PsiMethod javaMethod = javaMethods[index++];
					boolean canSkipMethod = isSingleMainMethodPresent && javaMethod.getName().equalsIgnoreCase(MAIN_LOWERCASE);
					if (canSkipMethod)
					{
						continue;
					}
					appendMethodContents(javaMethod, sb, tabStr);
					sb.append(NEWLINE_PREFIX + NEWLINE_STR);
					sb.append(NEWLINE_PREFIX + NEWLINE_STR);
				}

				sb.append(NEWLINE_PREFIX + "%>");
			}
			printDashedLine();
			System.out.println("ClassName :: " + javaClass.getName());
			printDashedLine();

			String jspContent = sb.toString();
			return jspContent.replace("System.out.", "out.");
		}
		catch (Exception ex)
		{
			throw new JSPConverterException(ErrorConstants.RUN_TIME_ERROR, "Oops, an unexpected error has occurred! Please check if there is no compilation errors in the JAVA file!");
		}
	}

	static String[] convertAnGetJavaImportsAsJSPImports(PsiJavaFile psiJavaFile) throws JSPConverterException
	{
		PsiImportList psiImportList = psiJavaFile.getImportList();
		return convertAnGetJavaImportsAsJSPImports(psiImportList);
	}

	private static String[] convertAnGetJavaImportsAsJSPImports(PsiImportList psiImportList) throws JSPConverterException
	{
		System.out.println("Imports: ");
		List<String> jspImportsList = new ArrayList<>();
		for (PsiImportStatementBase psiImportStatement : psiImportList.getAllImportStatements())
		{
			if (psiImportStatement instanceof PsiImportStatement)
			{
				jspImportsList.add("<%@page import=\"" + psiImportStatement.getImportReference().getText() + "\"%>");
			}
			else if (psiImportStatement instanceof PsiImportStaticStatement)
			{
				jspImportsList.add("<%@page import=\"static " + psiImportStatement.getImportReference().getText() + "\"%>");
			}
			else
			{
				throw new JSPConverterException(ErrorConstants.INVALID_DATA, "Unsupported import reference found: " + psiImportStatement.getText());
			}
		}
		return jspImportsList.toArray(new String[0]);
	}

	private static void appendMainMethodContents(PsiMethod mainMethod, StringBuilder sb, String tabStr, boolean useStatements)
	{
		sb.append(COMMENT_PREFIX + " Main " + COMMENT_SUFFIX);
		sb.append(NEWLINE_PREFIX + NEWLINE_STR);

		sb.append("<%");

		if (useStatements)
		{
			sb.append(NEWLINE_PREFIX + NEWLINE_STR);
			for (PsiStatement psiStatement : mainMethod.getBody().getStatements())
			{
				sb.append(tabStr + psiStatement.getText());
			}
		}
		else
		{
			PsiElement[] children = mainMethod.getBody().getChildren();
			for (int childNo = 1; childNo < children.length - 1; childNo++)
			{
				boolean isLastLine = childNo == children.length - 2;
				String lineText = children[childNo].getText();
				if (isLastLine)
				{
					String regex = "\\s+$";

					lineText = lineText.replaceAll(regex, "");
				}
				sb.append(NEWLINE_PREFIX + lineText);
			}
		}

		sb.append(NEWLINE_PREFIX + NEWLINE_STR);
		sb.append(NEWLINE_PREFIX + "%>");
		sb.append(NEWLINE_PREFIX + NEWLINE_STR);

		sb.append(NEWLINE_PREFIX + NEWLINE_STR);
	}

	private static void appendMethodContents(PsiMethod javaMethod, StringBuilder sb, String tabStr)
	{
		sb.append(tabStr);
		sb.append(javaMethod.getText());
	}

	static void validateJavaFileType(PsiFile psiFile) throws JSPConverterException
	{
		FileType fileType = psiFile.getFileType();
		if (!fileType.equals(JavaFileType.INSTANCE))
		{
			String fileExtension = fileType.getDefaultExtension();
			if (!StringUtil.isEmpty(fileExtension))
			{
				throw new JSPConverterException(ErrorConstants.INVALID_FILE_TYPE, "Oops, you have selected a " + DOT + fileExtension + " file. Please ensure you have selected a " + DOT + JavaFileType.INSTANCE.getDefaultExtension() + " file.");
			}
			throw new JSPConverterException(ErrorConstants.INVALID_FILE_TYPE, "Oops, you have selected a file with unsupported extension. Please ensure you have selected a " + DOT + JavaFileType.INSTANCE.getDefaultExtension() + " file.");
		}
	}

	static void printDashedLine()
	{
		String dashedLine = repeat(500, "-");
		System.out.println(dashedLine);
	}

	public static String repeat(int count, String with)
	{
		return new String(new char[count]).replace("\0", with);
	}

	public static boolean isValidJSPFilePath(String jspFilePathWithExtension)
	{
		if (!jspFilePathWithExtension.endsWith(DOT_JSP))
		{
			return false;
		}
		return isValidFilePath(jspFilePathWithExtension);
	}

	public static boolean isValidFilePath(String path)
	{
		File file = new File(path);
		try
		{
			return file.getCanonicalPath().equalsIgnoreCase(path);
		}
		catch (IOException e)
		{
			return false;
		}
	}
}
