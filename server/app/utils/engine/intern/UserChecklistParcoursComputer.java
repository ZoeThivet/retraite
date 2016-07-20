package utils.engine.intern;

import static utils.TextUtils.isLikeEmpty;

import java.util.Map;

import play.Logger;
import utils.RetraiteException;
import utils.RetraiteStringsUtils;
import utils.engine.data.UserChecklistGenerationData;
import utils.engine.utils.VariablesReplacer;

public class UserChecklistParcoursComputer {

	private final UserChecklistVarsProvider userChecklistVarsProvider;
	private final VariablesReplacer variablesReplacer;

	public UserChecklistParcoursComputer(final UserChecklistVarsProvider userChecklistVarsProvider, final VariablesReplacer variablesReplacer) {
		this.userChecklistVarsProvider = userChecklistVarsProvider;
		this.variablesReplacer = variablesReplacer;
	}

	public String compute(final String text, final UserChecklistGenerationData userChecklistGenerationData) {
		if (text == null) {
			return null;
		}
		if (isLikeEmpty(text)) {
			return null;
		}
		return replaceVars(replaceLinks(text), userChecklistGenerationData);
	}

	private String replaceVars(final String text, final UserChecklistGenerationData userChecklistGenerationData) {
		final Map<String, String> vars = userChecklistVarsProvider.provideVars(userChecklistGenerationData).getMapOfValues();
		try {
			return variablesReplacer.replaceVariables(text, vars);
		} catch (final RetraiteException e) {
			Logger.error(e, "Erreur lors du remplacement des variables");
			return text;
		}
	}

	private String replaceLinks(final String text) {
		return replaceLinks(text, 0);
	}

	private String replaceLinks(final String text, final int fromIndex) {
		final BeginIndex beginIndex = searchBeginIndex(text, fromIndex);
		if (beginIndex == BeginIndex.NONE) {
			return text;
		}
		final int endIndex = searchEndIndexForLink(beginIndex.type, text, beginIndex.index);
		final String beforeLink = text.substring(0, beginIndex.index);
		final String link = text.substring(beginIndex.type == BeginIndexType.SIMPLE ? beginIndex.index : beginIndex.index + 2, endIndex);
		final String buildedLink = buildLink(beginIndex.type, link);
		final String afterLink = text.substring(beginIndex.type == BeginIndexType.SIMPLE ? endIndex : endIndex + 2);
		final String newText = beforeLink + buildedLink + afterLink;
		return replaceLinks(newText, beforeLink.length() + buildedLink.length());
	}

	private int searchEndIndexForLink(final BeginIndexType type, final String text, final int beginIndex) {
		if (type == BeginIndexType.SIMPLE) {
			return searchEndIndexForSimpleLink(text, beginIndex);
		}
		return searchEndIndexForAdvancedLink(text, beginIndex);
	}

	private BeginIndex searchBeginIndex(final String text, final int fromIndex) {
		final int beginIndexHttp = text.indexOf("http://", fromIndex);
		final int beginIndexHttps = text.indexOf("https://", fromIndex);
		final int beginIndexBrackets = text.indexOf("[[", fromIndex);
		final int beginIndex = minIndex(minIndex(beginIndexHttp, beginIndexHttps), beginIndexBrackets);
		if (beginIndex != -1) {
			if (beginIndex == beginIndexBrackets) {
				return new BeginIndex(BeginIndexType.ADVANCED, beginIndex);
			}
			return new BeginIndex(BeginIndexType.SIMPLE, beginIndex);
		}
		return BeginIndex.NONE;
	}

	private int minIndex(final int index1, final int index2) {
		if (index1 == -1) {
			return index2;
		}
		if (index2 == -1) {
			return index1;
		}
		return Math.min(index1, index2);
	}

	private int searchEndIndexForSimpleLink(final String text, final int beginIndex) {
		final int endIndex = RetraiteStringsUtils.getMinIndex(
				text.length(),
				text.indexOf(" ", beginIndex),
				text.indexOf("<", beginIndex));
		if (endIndex == -1) {
			return -1;
		}
		return skipSpecialCharAfterLink(text, endIndex);
	}

	private int searchEndIndexForAdvancedLink(final String text, final int beginIndex) {
		return text.indexOf("]]", beginIndex);
	}

	private int skipSpecialCharAfterLink(final String text, final int endIndex) {
		final char lastCharInLink = text.charAt(endIndex - 1);
		if (lastCharInLink == '.') {
			return skipSpecialCharAfterLink(text, endIndex - 1);
		}
		return endIndex;
	}

	private String buildLink(final BeginIndexType type, final String link) {
		if (type == BeginIndexType.SIMPLE) {
			return buildLinkSimple(link);
		}
		final String linkTrimed = link.trim();
		final int index = linkTrimed.indexOf("http");
		final String textForLink = linkTrimed.substring(0, index).trim();
		final String url = linkTrimed.substring(index);
		return buildLink(url, textForLink);
	}

	private String buildLinkSimple(final String link) {
		final String textForLink = convertTextForLink(link);
		return buildLink(link, textForLink);
	}

	private String buildLink(final String url, final String textForLink) {
		return "<a href='" + url + "' target='_blank' title='Nouvelle fenêtre'>" + textForLink + "</a>";
	}

	private String convertTextForLink(final String link) {
		if (link.toLowerCase().startsWith("http://")) {
			return convertTextForLink(link.substring("http://".length()));
		}
		if (link.toLowerCase().startsWith("https://")) {
			return convertTextForLink(link.substring("https://".length()));
		}
		if (link.toLowerCase().startsWith("www.")) {
			return convertTextForLink(link.substring("www.".length()));
		}
		if (link.toLowerCase().endsWith(".html")) {
			return convertTextForLink(link.substring(0, link.length() - ".html".length()));
		}
		if (link.toLowerCase().endsWith(".htm")) {
			return convertTextForLink(link.substring(0, link.length() - ".htm".length()));
		}
		if (link.toLowerCase().endsWith(".pdf")) {
			return convertTextForLink(link.substring(0, link.length() - ".pdf".length()));
		}
		return link;
	}

	private static enum BeginIndexType {
		SIMPLE, ADVANCED
	}

	private static class BeginIndex {

		public static final BeginIndex NONE = new BeginIndex();

		private final BeginIndexType type;
		private final int index;

		public BeginIndex(final BeginIndexType type, final int index) {
			this.type = type;
			this.index = index;
		}

		private BeginIndex() {
			this(null, -1);
		}

	}

}
