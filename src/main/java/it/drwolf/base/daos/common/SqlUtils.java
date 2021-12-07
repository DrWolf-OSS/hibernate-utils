package it.drwolf.base.daos.common;

public class SqlUtils {

	public static int calculateFirst(int page, int size) {
		int first = 0;
		if (page * size > size) {
			first = (page * size) - size;
		}
		return first;
	}

	public static String likeAfter(String pValue) {
		return pValue + "%";
	}

	public static String likeAll(String pValue) {
		return "%" + pValue + "%";
	}

	public static String likeAllAndTrim(String pValue) {
		return (pValue != null ? "%" + pValue.trim() + "%" : null);
	}

	public static String likeBefore(String pValue) {
		return "%" + pValue;
	}

}
