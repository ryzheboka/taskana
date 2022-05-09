package pro.taskana.checkstyle;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.*;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

@StatelessCheck
public class MissingWhitespaceAfterAsteriks extends AbstractJavadocCheck {

  /** A key is pointing to the warning message text in "messages.properties" file. */
  public static final String MSG_KEY = "javadoc.missing.whitespace";

  @Override
  public int[] getDefaultJavadocTokens() {
    return new int[] {
      JavadocTokenTypes.JAVADOC, JavadocTokenTypes.LEADING_ASTERISK,
    };
  }

  @Override
  public int[] getRequiredJavadocTokens() {
    return getAcceptableJavadocTokens();
  }

  @Override
  public void visitJavadocToken(DetailNode detailNode) {
    final DetailNode textNode;

    if (detailNode.getType() == JavadocTokenTypes.JAVADOC) {
      textNode = JavadocUtil.getFirstChild(detailNode);
    } else {
      textNode = JavadocUtil.getNextSibling(detailNode);
    }

    if (textNode != null && textNode.getType() != JavadocTokenTypes.EOF) {
      final String text = textNode.getText();
      final int lastAsteriskPosition = getLastLeadingAsteriskPosition(text);

      if (!isLast(lastAsteriskPosition, text)
          && !Character.isWhitespace(text.charAt(lastAsteriskPosition + 1))) {
        log(textNode.getLineNumber(), textNode.getColumnNumber(), MSG_KEY);
      }
    }
  }

  /**
   * Checks if the character position is the last one of the string.
   *
   * @param position the position of the character
   * @param text String literal.
   * @return true if the character position is the last one of the string.
   */
  private static boolean isLast(int position, String text) {
    return position == text.length() - 1;
  }

  /**
   * Finds the position of the last leading asterisk in the string. If {@code text} contains no
   * leading asterisk, -1 will be returned.
   *
   * @param text String literal.
   * @return the index of the last leading asterisk.
   */
  private static int getLastLeadingAsteriskPosition(String text) {
    int index = -1;

    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) != '*') {
        break;
      }
      index++;
    }

    return index;
  }
}
