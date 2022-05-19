package pro.taskana.checkstyle;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

@StatelessCheck
public class JavadocTagEndsWithNoPoint extends AbstractJavadocCheck {

  @Override
  public int[] getDefaultJavadocTokens() {
    return new int[] {
      JavadocTokenTypes.JAVADOC_TAG,
    };
  }

  @Override
  public int[] getRequiredJavadocTokens() {
    return getAcceptableJavadocTokens();
  }

  @Override
  public void visitJavadocToken(DetailNode detailNode) {
    DetailNode textNode = JavadocUtil.findFirstToken(detailNode, JavadocTokenTypes.DESCRIPTION);
    if (textNode != null) {
      textNode = JavadocUtil.findFirstToken(textNode, JavadocTokenTypes.TEXT);
      if (textNode != null) {
        String text = textNode.getText();
        // log(detailNode.getLineNumber(), detailNode.getColumnNumber(), text);
        if (text != null
            && text.length() > 0
            && (text.charAt(text.length() - 1) == '.'
                || (text.charAt(0) <= 'Z') && (text.charAt(0) >= 'A'))) {
          log(detailNode.getLineNumber(), detailNode.getColumnNumber(), "No point!");
        }
      }
    }
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
