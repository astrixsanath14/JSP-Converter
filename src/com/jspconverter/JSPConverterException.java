package com.jspconverter;

public class JSPConverterException extends Exception
{

	private Object data = null;
	private int errCode = -1;
	private String errString;

	public JSPConverterException()
	{
		super();
	}

	public JSPConverterException(String message)
	{
		super(message);
	}

	public JSPConverterException(int errCode, String message)
	{
		super(message);
		this.errCode = errCode;
	}

	public JSPConverterException(String errString, String message)
	{
		super(message);
		this.errString = errString;
	}

	public JSPConverterException(Throwable cause)
	{
		super(cause);
	}

	public JSPConverterException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public JSPConverterException(int errCode, Throwable cause)
	{
		super(cause);
		this.errCode = errCode;
	}

	public JSPConverterException(int errCode, String message, Throwable cause)
	{
		super(message, cause);
		this.errCode = errCode;
	}

	public JSPConverterException(String errString, String message, Throwable cause)
	{
		super(message, cause);
		this.errString = errString;
	}

	public JSPConverterException(int errorCode)
	{
		this.errCode = errorCode;
	}

	public int getErrorCode()
	{
		return errCode;
	}

	public void setData(Object obj)
	{
		this.data = obj;
	}

	public Object getData()
	{
		return data;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder(100);
		builder.append(super.toString());
		builder.append("Error Code: ");  //No I18N
		builder.append(getErrorCode());
		String message = getMessage();
		if (message != null)
		{
			builder.append("Error Message: ");  //No I18N
			builder.append(message);
		}
		return builder.toString();
	}

	@Override
	public void printStackTrace()
	{
		if (this.errCode == ErrorConstants.READ_ONLY_MODE)
		{
			return;
		}
		super.printStackTrace();
	}

	public String getErrString()
	{
		return errString;
	}

	public void setErrString(String errString)
	{
		this.errString = errString;
	}
}

