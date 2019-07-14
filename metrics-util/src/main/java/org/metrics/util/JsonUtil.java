package org.metrics.util;


public class JsonUtil {
	private JsonUtil() {
	}

	/**
	 * Based on https://stackoverflow.com/a/49564514/510017
	 *
	 * @param unformattedJsonString
	 *            JSON string that has not been formatted at all.
	 * @return A best-effort at pretty printed JSON, even for malformed JSON.
	 */
	public static String prettyPrint(String unformattedJsonString) {
		StringBuilder sb = new StringBuilder();
		int indentLevel = 0;
		boolean inQuote = false;
		for (char charFromUnformattedJson : unformattedJsonString.toCharArray()) {
			switch (charFromUnformattedJson) {
			case '"':
				// switch the quoting status
				inQuote = !inQuote;
				sb.append(charFromUnformattedJson);
				break;
			case ' ':
				// For space: ignore the space if it is not being quoted.
				if (inQuote) {
					sb.append(charFromUnformattedJson);
				}
				break;
			case '{':
			case '[':
				// Starting a new block: increase the indent level
				sb.append(charFromUnformattedJson);
				indentLevel++;
				appendIndentedNewLine(indentLevel, sb);
				break;
			case '}':
			case ']':
				// Ending a new block; decrese the indent level
				indentLevel--;
				appendIndentedNewLine(indentLevel, sb);
				sb.append(charFromUnformattedJson);
				break;
			case ',':
				// Ending a json item; create a new line after
				sb.append(charFromUnformattedJson);
				if (!inQuote) {
					appendIndentedNewLine(indentLevel, sb);
				}
				break;
			default:
				sb.append(charFromUnformattedJson);
			}
		}
		return sb.toString();
	}

	/**
	 * Print a new line with indention at the beginning of the new line.
	 *
	 * @param indentLevel
	 * @param stringBuilder
	 */
	private static void appendIndentedNewLine(int indentLevel,
			StringBuilder stringBuilder) {
		stringBuilder.append("\n");
		for (int i = 0; i < indentLevel; i++) {
			// Assuming indention using 2 spaces
			stringBuilder.append("  ");
		}
	}
}
