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

  /*
  |   |   `--JAVADOC -> JAVADOC [60:5]
  |   |       |--NEWLINE -> \n [60:5]
  |   |       |--LEADING_ASTERISK ->    * [61:0]
  |   |       |--TEXT ->  Finds the position of the last leading asterisk in the string. If  [61:4]
  |   |       |--JAVADOC_INLINE_TAG -> JAVADOC_INLINE_TAG [61:71]
  |   |       |   |--JAVADOC_INLINE_TAG_START -> { [61:71]
  |   |       |   |--CODE_LITERAL -> @code [61:72]
  |   |       |   |--WS ->   [61:77]
  |   |       |   |--TEXT -> text [61:78]
  |   |       |   `--JAVADOC_INLINE_TAG_END -> } [61:83]
  |   |       |--TEXT ->  contains no [61:84]
    */
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
