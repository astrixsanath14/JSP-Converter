package com.jspconverter.actions;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.jspconverter.JSPConverterUtil;

public class JSPFileChooserDescriptor extends FileChooserDescriptor
{

	public JSPFileChooserDescriptor(@NotNull String title, @NotNull String description)
	{
		super(true, true, false, false, false, false);
		assert title != null : "title is null";
		assert description != null : "description is null";

		this.setTitle(title);
		this.setDescription(description);
	}

	public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles)
	{
		if (file == null)
		{
			return false;
		}
		if (file.isDirectory())
		{
			return true;
		}
		if (file.getExtension() != null && file.getExtension().equals(JSPConverterUtil.JSP))
		{
			return true;
		}
		return false;
	}

	@Override public boolean isFileSelectable(VirtualFile file)
	{
		return isFileVisible(file, super.isShowHiddenFiles());
	}
}
