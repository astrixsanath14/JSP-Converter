package com.jspconverter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
	private static final Logger LOGGER = Logger.getLogger(StringUtil.class.getName());

	public StringUtil()
	{
	}

	public static String getNonNullValue(String argToCheck, String defaultValue)
	{
		return argToCheck != null && !argToCheck.trim().equals("") && !argToCheck.trim().equalsIgnoreCase("null") ? argToCheck : defaultValue;
	}

	public static String escapeSpecialChars(String input)
	{
		return escapeSpecialChars(input, "");
	}

	public static String escapeSpecialChars(String input, String replace)
	{
		return input != null ? input.replaceAll("[\\\\\\?\\/\\*\\>\\<\\s\":|]", replace) : input;
	}

	public static String escapeDollarAndSlash(String input)
	{
		return input == null || input.indexOf(36) == -1 && input.indexOf("\\") == -1 ? input : input.replaceAll("[\\\\]", "\\\\\\\\").replaceAll("[\\$]", "\\\\\\$");
	}

	public static String escapeForHTML(String input)
	{
		if (input == null)
		{
			return input;
		}
		else
		{
			input = input.replaceAll("'", "&apos;");
			return input.replaceAll("\\\"", "&quot;");
		}
	}

	public static String escapeForDoubleQuoteHTML(String input)
	{
		return input == null ? input : input.replaceAll("\\\"", "&quot;");
	}

	public static String escapeDoubleQuotes(String input)
	{
		return input != null && input.indexOf("\"") != -1 ? input.replaceAll("\\\"", "\\\\\"") : input;
	}

	public static String escapeDoubleQuotesForCSV(String input)
	{
		if (input != null)
		{
			if (input.indexOf("\\") != -1)
			{
				input = input.replaceAll("[\\\\]", "\\\\\\\\");
			}

			if (input.indexOf("\"") != -1)
			{
				input = input.replaceAll("\"", "\"\"");
			}
		}

		return input;
	}

	public static String getI18NEscapedString(String input)
	{
		return input == null ? input : input.replaceAll("'", "\\\\'");
	}

	public static String convertStringWithSpaceAddedBeforeUpperCase(String input)
	{
		StringBuffer result = new StringBuffer();
		boolean previousCharIsUpperCase = false;

		for (int i = 0; i < input.length(); ++i)
		{
			char c = input.charAt(i);
			if (Character.isUpperCase(c))
			{
				if (!previousCharIsUpperCase)
				{
					result.append(' ');
					previousCharIsUpperCase = true;
				}
			}
			else
			{
				boolean charBeforePreviousCharIsAlsoUpperCase = false;
				if (i >= 2 && Character.isUpperCase(input.charAt(i - 2)))
				{
					charBeforePreviousCharIsAlsoUpperCase = true;
				}

				if (previousCharIsUpperCase && charBeforePreviousCharIsAlsoUpperCase)
				{
					result.insert(result.length() - 1, ' ');
				}

				previousCharIsUpperCase = false;
			}

			result.append(c);
		}

		return result.toString().trim();
	}

	public static String replaceAllSpecialCharWithUnderScore(String input)
	{
		String pattern = "[^a-zA-Z0-9_.]{1,}";
		return input.replaceAll(pattern, "_");
	}

	public static String replaceSpecialCharsForXml(String str)
	{
		return str != null ? str.replaceAll("&", "&amp;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;") : null;
	}

	public static boolean isNotEmpty(String str)
	{
		return !isEmpty(str);
	}

	public static boolean isEmpty(String str)
	{
		return str == null || str.trim().equalsIgnoreCase("null") || str.trim().equalsIgnoreCase("undefined") || str.trim().equalsIgnoreCase("");
	}

	public static boolean isEmptyString(String str)
	{
		if (str == null)
		{
			return false;
		}
		else
		{
			int strLength = str.length();

			for (int i = 0; i < strLength; ++i)
			{
				if (!Character.isWhitespace(str.charAt(i)))
				{
					return false;
				}
			}

			return true;
		}
	}

	public static Object returnNULLOnEmpty(String str)
	{
		return isEmpty(str) ? null : str;
	}

	public static String removeAllCarriageReturns(String str)
	{
		return removeAllCarriageReturns(str.getBytes());
	}

	public static String removeAllCarriageReturns(byte[] srcByteArr)
	{
		byte[] tempByteArr = new byte[srcByteArr.length];
		int j = 0;

		for (int i = 0; i < srcByteArr.length; ++i)
		{
			if ((char) srcByteArr[i] == '\r' || (char) srcByteArr[i] == '\n')
			{
				srcByteArr[i] = 32;
			}

			tempByteArr[j] = srcByteArr[i];
			++j;
		}

		byte[] destByteArr = new byte[j];

		for (int i = 0; i < j; ++i)
		{
			destByteArr[i] = tempByteArr[i];
		}

		return new String(destByteArr);
	}

	public static String getFirstMatch(String src, String regex) throws Exception
	{
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(src);
		boolean matchfind = mat.find();
		String matched = null;
		if (matchfind)
		{
			matched = mat.group(1);
			return matched;
		}
		else
		{
			return src;
		}
	}

	public static String getCommaSeparatedFromArray(Object[] array) throws Exception
	{
		return getCommaSeparatedFromArray(array, true);
	}

	public static String getCommaSeparatedFromArray(Object[] array, boolean appendSingleQuote) throws Exception
	{
		return getCommaSeparatedFromArray(array, appendSingleQuote, true);
	}

	public static String getCommaSeparatedFromArray(Object[] array, boolean appendSingleQuote, boolean appendBrackets) throws Exception
	{
		return getCommaSeparatedFromArray(array, appendSingleQuote, appendBrackets, ",");
	}

	public static String getCommaSeparatedFromArray(Object[] array, boolean appendSingleQuote, boolean appendBrackets, String delimiter) throws Exception
	{
		return getCommaSeparatedFromObject(array, appendSingleQuote, appendBrackets, delimiter);
	}

	private static String getCommaSeparatedFromObject(Object obj, boolean appendSingleQuote, boolean appendBrackets, String delimiter) throws Exception
	{
		StringBuilder str = new StringBuilder();
		int size = 0;
		boolean isList = false;
		List list = null;
		Object[] array = null;
		if (obj instanceof List)
		{
			list = (List) obj;
			isList = true;
			size = list.size();
		}
		else
		{
			array = (Object[]) ((Object[]) obj);
			if (array != null)
			{
				size = array.length;
			}
		}

		if (obj != null)
		{
			if (appendBrackets)
			{
				str.append("(");
			}

			for (int i = 0; i < size; ++i)
			{
				Object val = null;
				if (isList)
				{
					val = String.valueOf(list.get(i));
				}
				else
				{
					val = array[i];
				}

				if (appendSingleQuote)
				{
					str.append("'").append(val).append("'");
				}
				else
				{
					str.append(val);
				}

				if (i != size - 1)
				{
					str.append(delimiter);
				}
			}

			if (appendBrackets)
			{
				str.append(")");
			}
		}

		String commaSeparatedId = str.toString();
		return commaSeparatedId;
	}

	public static String getCommaSeparatedFromList(List list) throws Exception
	{
		return getCommaSeparatedFromList(list, true);
	}

	public static String getCommaSeparatedFromList(List list, boolean appendSingleQuote) throws Exception
	{
		return getCommaSeparatedFromList(list, appendSingleQuote, true);
	}

	public static String getCommaSeparatedFromList(List list, boolean appendSingleQuote, boolean appendBrackets) throws Exception
	{
		return getCommaSeparatedFromList(list, appendSingleQuote, appendBrackets, ",");
	}

	public static String getCommaSeparatedFromList(List list, boolean appendSingleQuote, boolean appendBrackets, String delimiter) throws Exception
	{
		return getCommaSeparatedFromObject(list, appendSingleQuote, appendBrackets, delimiter);
	}

	public static void removeLastChar(StringBuilder strBuilder, String str)
	{
		int len = strBuilder.length() - str.length();
		int index = strBuilder.indexOf(str, len);
		if (index != -1)
		{
			strBuilder.deleteCharAt(index);
		}
	}

	public static String[] splitString(String str, String delim)
	{
		return isEmpty(str) ? null : splitString(str, delim, false);
	}

	public static String[] splitString(String str, String delim, boolean returnDelim)
	{
		StringTokenizer stzr = new StringTokenizer(str, delim, returnDelim);
		int count = stzr.countTokens();
		String[] stringArray = new String[count];

		for (int i = 0; i < count; ++i)
		{
			stringArray[i] = stzr.nextToken();
		}

		return stringArray;
	}

	public static String processImportString(String str, int length)
	{
		str = str.trim();
		if (str.length() > length)
		{
			str = str.substring(length - 1);
		}

		if (str.length() < 1)
		{
			str = null;
		}

		return str;
	}

	public static byte[] processBytesForImport(byte[] b) throws Exception
	{
		byte[] newarr = new byte[b.length];
		int j = 0;
		byte[] arr$ = b;
		int len$ = b.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			byte element = arr$[i$];
			if ((char) element != '\\')
			{
				newarr[j] = element;
				++j;
			}
		}

		return trimByteArray(newarr, j);
	}

	public static byte[] trimByteArray(byte[] b, int len) throws Exception
	{
		if (len > b.length)
		{
			throw new Exception("length argument greater than byte array length!");
		}
		else
		{
			byte[] newarr = new byte[len];

			for (int i = 0; i < len; ++i)
			{
				newarr[i] = b[i];
			}

			return newarr;
		}
	}

	public static boolean isEmptyORExceedsLength(String value, int length)
	{
		return value.length() == 0 || value.length() > length;
	}

	public static int[] getTagIndexes(String content, String tagId, String tagName)
	{
		String pattern = ".*?(<\\s*" + tagName + "\\s*id\\s*=['\"]?\\s*" + tagId + "['\"]?\\s*.*?>.*?<\\s*/" + tagName + "\\s*>)(.*)";
		int val = 34;
		Pattern p = Pattern.compile(pattern, val);
		Matcher mat = p.matcher(content);
		boolean find = mat.find();
		int[] toRet = new int[]{-1, -1};
		if (!find)
		{
			return toRet;
		}
		else
		{
			toRet[0] = mat.start(1);
			toRet[1] = mat.start(2);
			return toRet;
		}
	}

	public static boolean isNotEmpty(String[] strArray)
	{
		if (strArray == null)
		{
			return false;
		}
		else
		{
			String str = "";
			String[] arr$ = strArray;
			int len$ = strArray.length;

			for (int i$ = 0; i$ < len$; ++i$)
			{
				String element = arr$[i$];
				if (element != null && !element.trim().equalsIgnoreCase("null") && !element.trim().equalsIgnoreCase("undefined") && !element.trim().equalsIgnoreCase(""))
				{
					return true;
				}
			}

			return false;
		}
	}

	public static List getAsList(String response, String delimiter, boolean addNullForEmpty, boolean returnDelimiter)
	{
		if (response == null)
		{
			return null;
		}
		else
		{
			StringTokenizer st = new StringTokenizer(response, delimiter, returnDelimiter);
			List list = new ArrayList();

			String t;
			for (String prvTkn = ""; st.hasMoreTokens(); prvTkn = t)
			{
				t = st.nextToken();
				if (addNullForEmpty && prvTkn.equals(t))
				{
					list.add((Object) null);
				}
				else if (!delimiter.equals(t))
				{
					list.add(t);
				}
			}

			return list;
		}
	}

	public static Long[] convertStringArrayToLongArray(String[] strArr) throws Exception
	{
		return convertStringArrayToLongArray(strArr, false);
	}

	public static Long[] convertStringArrayToLongArray(String[] strArr, boolean isDiscardNullVals) throws Exception
	{
		if (strArr == null)
		{
			return null;
		}
		else
		{
			Long[] longArr = new Long[strArr.length];

			for (int i = 0; i < strArr.length; ++i)
			{
				if (isEmpty(strArr[i]) && !isDiscardNullVals)
				{
					longArr[i] = null;
				}
				else
				{
					longArr[i] = Long.parseLong(strArr[i]);
				}
			}

			return longArr;
		}
	}

	public static Long[] convertStringToLongArray(String str, String delim) throws Exception
	{
		if (isEmpty(str))
		{
			return null;
		}
		else
		{
			String[] strArr = splitString(str, delim);
			return convertStringArrayToLongArray(strArr);
		}
	}

	public static List<String> convertStringToArrayList(String string) throws Exception
	{
		if (isEmpty(string))
		{
			return new ArrayList();
		}
		else
		{
			String formattedEntityIDStr = string.substring(1, string.lastIndexOf("]"));
			if (isEmptyString(formattedEntityIDStr))
			{
				return new ArrayList();
			}
			else
			{
				String[] entityIDStr = splitString(formattedEntityIDStr, ", ");
				return new ArrayList(Arrays.asList(entityIDStr));
			}
		}
	}

	public static List<Long> convertStringArrayToList(String[] strArr) throws Exception
	{
		if (strArr != null && strArr.length > 1)
		{
			List delIDslist = new ArrayList(strArr.length);
			Long valID = null;
			String[] arr$ = strArr;
			int len$ = strArr.length;

			for (int i$ = 0; i$ < len$; ++i$)
			{
				String element = arr$[i$];
				if (!isEmpty(element))
				{
					valID = Long.parseLong(element);
					delIDslist.add(valID);
				}
			}

			return delIDslist;
		}
		else
		{
			return null;
		}
	}

	public static String getCommaSepFromList(List list) throws Exception
	{
		Iterator it = list.iterator();
		if (!it.hasNext())
		{
			return "";
		}
		else
		{
			StringBuilder builder = new StringBuilder((String) it.next());

			while (it.hasNext())
			{
				builder.append(',').append(it.next());
			}

			return builder.toString();
		}
	}

	public static String replaceAll(String inputStr, String oldValue, String newValue, boolean escape)
	{
		if (escape)
		{
			String oldPattern = Pattern.quote(oldValue);
			String newPattern = Matcher.quoteReplacement(newValue);
			return inputStr.replaceAll(oldPattern, newPattern);
		}
		else
		{
			return inputStr.replaceAll(oldValue, newValue);
		}
	}

	public static String replaceAll(String inputStr, String oldValue, String newValue)
	{
		return replaceAll(inputStr, oldValue, newValue, true);
	}

	public static String replaceAll(Matcher inputStr, String newValue, boolean escape)
	{
		if (escape)
		{
			String newPattern = Matcher.quoteReplacement(newValue);
			return inputStr.replaceAll(newPattern);
		}
		else
		{
			return inputStr.replaceAll(newValue);
		}
	}

	public static String replaceAll(Matcher inputStr, String newValue)
	{
		return replaceAll(inputStr, newValue, true);
	}

	public static String removeEmptyLines(String inputStr)
	{
		return isEmpty(inputStr) ? inputStr : inputStr.replaceAll("(?m)^[ \t]*\r?\n", "");
	}

	public static List getAsList(String mailIDs)
	{
		return mailIDs == null ? null : getAsList(mailIDs, "", false, false);
	}

	public static Properties getAsProperties(String response) throws Exception
	{
		Properties prop = new Properties();

		try
		{
			String[] responseArr = splitString(response, "&");

			for (int i = 0; i < responseArr.length; ++i)
			{
				String[] eachResParam = splitString(responseArr[i], "=");
				prop.put(eachResParam[0], eachResParam[1]);
			}
		}
		catch (Exception var5)
		{
			LOGGER.log(Level.INFO, "Error occurred when converting response to Properties : ", var5);
		}

		return prop;
	}

	public static int[] constructIntArray(String str)
	{
		StringTokenizer token = new StringTokenizer(str, ",");
		int length = token.countTokens();
		int[] flt = new int[length];

		for (int i = 0; i < length; ++i)
		{
			String s = token.nextToken();
			flt[i] = Integer.parseInt(s);
		}

		return flt;
	}

	public static float[] constructFloatArray(String str)
	{
		StringTokenizer token = new StringTokenizer(str, ",");
		int length = token.countTokens();
		float[] flt = new float[length];

		for (int i = 0; i < length; ++i)
		{
			String s = token.nextToken();
			flt[i] = Float.parseFloat(s);
		}

		return flt;
	}

	public static String[] constructArray(String str)
	{
		return splitString(str, ",");
	}
}
