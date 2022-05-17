package pro.taskana.checkstyle;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import java.util.Set;

@StatelessCheck
public class JavadocAfterClosingParenthesisCheck extends AbstractJavadocCheck {

  public static final Set<Character> ACCEPTABLE_CHARS = Set.of(' ', ',', '.', ';', '\n');

  @Override
  public int[] getDefaultJavadocTokens() {
    return new int[] {
      JavadocTokenTypes.JAVADOC_INLINE_TAG,
    };
  }

  @Override
  public int[] getRequiredJavadocTokens() {
    return getAcceptableJavadocTokens();
  }

  @Override
  public void visitJavadocToken(DetailNode detailNode) {
    DetailNode textNode = JavadocUtil.getNextSibling(detailNode);
    if (textNode != null && textNode.getType() != JavadocTokenTypes.EOF) {
      if (!ACCEPTABLE_CHARS.contains(textNode.getText().charAt(0))) {
        log(textNode.getLineNumber(), textNode.getColumnNumber(), "Custom Error");
      }
    }
    /*
    if (detailNode.getType() == JavadocTokenTypes.JAVADOC) {
      textNode = JavadocUtil.getFirstChild(detailNode);
    } else {
      textNode = JavadocUtil.getNextSibling(detailNode);
    }

    if (textNode != null && textNode.getType() != JavadocTokenTypes.EOF) {
      final String text = textNode.getText();
      if (!correctCharsAfterParenthesis(text)) {
        log(textNode.getLineNumber(), textNode.getColumnNumber(), "Custom Error");
      }
    }
    if (textNode != null) {
      log(1, 1, textNode.getText());
    }
    ;

     */
  }

  private static boolean correctCharsAfterParenthesis(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '}') { // && !ACCEPTABLE_CHARS.contains(text.charAt(i + 1))) {
        return false;
      }
    }

    return true;
  }
}
