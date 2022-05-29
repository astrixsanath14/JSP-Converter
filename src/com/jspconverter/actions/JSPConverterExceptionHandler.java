package com.jspconverter.actions;

import com.intellij.openapi.ui.Messages;
import com.jspconverter.ErrorConstants;
import com.jspconverter.JSPConverterException;

public class JSPConverterExceptionHandler extends Throwable
{
	static void handleException(JSPConverterException exception)
	{
		if (exception instanceof JSPConverterException)
		{
			JSPConverterException jspConverterException = (JSPConverterException) exception;
			if (jspConverterException.getErrorCode() == ErrorConstants.INVALID_FILE_TYPE)
			{
				Messages.showErrorDialog(jspConverterException.getMessage(), "JSPConverter: Invalid file type");
			}
			else
			{
				Messages.showErrorDialog(jspConverterException.getMessage(), "JSPConverter: Error");
			}
		}
	}
}
