package com.jspconverter;

import com.intellij.psi.PsiFile;

public class JSPConverterAPI
{
	public static String getJSPFileContentsOfJavaFile(PsiFile psiFile, Integer tabSize) throws JSPConverterException
	{
		return JSPConverterUtil.getJSPFileContentsOfJavaFile(psiFile, tabSize);
	}
}
