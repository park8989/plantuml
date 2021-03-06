/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2017, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 * Modified by: Nicolas Jouanin
 * 
 *
 */
package net.sourceforge.plantuml.preproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sourceforge.plantuml.FileSystem;
import net.sourceforge.plantuml.Log;

public class FileWithSuffix {

	private final File file;
	private final String suffix;
	private final String entry;

	public Reader getReader(String charset) throws IOException {
		if (entry == null) {
			if (charset == null) {
				Log.info("Using default charset");
				return new FileReader(file);
			}
			Log.info("Using charset " + charset);
			return new InputStreamReader(new FileInputStream(file), charset);
		}
		final InputStream is = getDataFromZip(file, entry);
		if (is == null) {
			return null;
		}
		if (charset == null) {
			Log.info("Using default charset");
			return new InputStreamReader(is);
		}
		Log.info("Using charset " + charset);
		return new InputStreamReader(is, charset);
	}

	private InputStream getDataFromZip(File f, String name) throws IOException {
		final ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			final String fileName = ze.getName();
			if (ze.isDirectory()) {
			} else if (fileName.equals(name)) {
				return zis;
			}
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
		return null;
	}

	public boolean fileOk() {
		if (file.exists() == false || file.isDirectory()) {
			return false;
		}
		return true;
	}

	public FileWithSuffix(File file, String suffix) {
		this.file = file;
		this.suffix = suffix;
		this.entry = null;
	}

	public FileWithSuffix(String fileName, String suffix) throws IOException {
		final int idx = fileName.indexOf('~');
		this.suffix = suffix;
		if (idx == -1) {
			this.file = FileSystem.getInstance().getFile(fileName);
			this.entry = null;
		} else {
			this.file = FileSystem.getInstance().getFile(fileName.substring(0, idx));
			this.entry = fileName.substring(idx + 1);
		}
	}

	@Override
	public int hashCode() {
		return file.hashCode() + (suffix == null ? 0 : suffix.hashCode() * 43) + (entry == null ? 0 : entry.hashCode());
	}

	@Override
	public boolean equals(Object arg) {
		final FileWithSuffix other = (FileWithSuffix) arg;
		return this.file.equals(other.file) && equals(suffix, other.suffix) && same(entry, other.entry);
	}

	private static boolean same(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		}
		if (s1 != null && s2 != null) {
			return s1.equals(s2);
		}
		return false;
	}

	private static boolean equals(String s1, String s2) {
		if (s1 == null && s2 == null) {
			return true;
		}
		if (s1 != null && s2 != null) {
			return s1.equals(s2);
		}
		assert (s1 == null && s2 != null) || (s1 != null && s2 == null);
		return false;
	}

	public static Set<File> convert(Set<FileWithSuffix> all) {
		final Set<File> result = new HashSet<File>();
		for (FileWithSuffix f : all) {
			result.add(f.file);
		}
		return result;
	}

	public final File getFile() {
		return file;
	}

	public File getParentFile() {
		return file.getParentFile();
	}

	public String getDescription() {
		if (entry == null) {
			return file.getAbsolutePath();
		}
		return file.getAbsolutePath() + "~" + entry;
	}

	public final String getSuffix() {
		return suffix;
	}

}
