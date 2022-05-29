package com.jspconverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiStatement;

public class JSPConverterUtil
{
	private static final String NEWLINE_PREFIX = "";
	private static final String NEWLINE_STR = "\n";
	private static final String DEFAULT_TAB_STR = "\t";
	private static final String MAIN_LOWERCASE = "main";
	private static final String DOT = ".";

	static String getJSPFileContentsOfJavaFile(PsiFile psiFile, Integer tabSize) throws JSPConverterException
	{
		validateJavaFileType(psiFile);
		String tabStr = DEFAULT_TAB_STR;
		if (tabSize != null)
		{
			tabStr = repeat(tabSize, " ");
		}
		PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
		StringBuilder sb = new StringBuilder();

		sb.append("<!-- Imports -->");
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
		appendMainMethodContents(mainMethod, sb, tabStr);
		int remainingMethodCount = javaMethods.length - 1;
		boolean hasOtherMethods = remainingMethodCount > 0;
		if (hasOtherMethods)
		{
			sb.append("<!-- Other methods -->");
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

		return sb.toString();
	}

	static String[] convertAnGetJavaImportsAsJSPImports(PsiJavaFile psiJavaFile)
	{
		PsiImportList psiImportList = psiJavaFile.getImportList();
		psiImportList.getAllImportStatements();
		return convertAnGetJavaImportsAsJSPImports(psiImportList);
	}

	private static String[] convertAnGetJavaImportsAsJSPImports(PsiImportList psiImportList)
	{
		System.out.println("Imports: ");
		List<String> jspImportsList = new ArrayList<>();
		for (PsiImportStatement psiImportStatement : psiImportList.getImportStatements())
		{
			jspImportsList.add("<%@page import=\"" + psiImportStatement.getImportReference().getText() + "\"%>");
		}
		return jspImportsList.toArray(new String[0]);
	}

	private static void appendMainMethodContents(PsiMethod mainMethod, StringBuilder sb, String tabStr)
	{
		sb.append("<!-- Main -->");
		sb.append(NEWLINE_PREFIX + NEWLINE_STR);

		sb.append("<%");
		sb.append(NEWLINE_PREFIX + NEWLINE_STR);
		for (PsiStatement psiStatement : mainMethod.getBody().getStatements())
		{
			sb.append(tabStr + psiStatement.getText());
			sb.append(NEWLINE_PREFIX + NEWLINE_STR);
		}
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
			throw new JSPConverterException(ErrorConstants.INVALID_FILE_TYPE, "Oops, you have selected a " + DOT + fileType.getDefaultExtension() + " file. Please ensure you have selected a " + DOT + JavaFileType.INSTANCE.getDefaultExtension() + " file.");
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
}
