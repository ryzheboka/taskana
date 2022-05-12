package pro.taskana.checkstyle;

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;
import java.util.List;

@FileStatefulCheck
public class JavadocClassDocStartsWithClassName extends AbstractJavadocCheck {

  public static List<Integer> CLASS_DEF_CONTAINS =
      List.of(
          TokenTypes.BLOCK_COMMENT_BEGIN,
          TokenTypes.MODIFIERS,
          TokenTypes.LITERAL_PUBLIC,
          TokenTypes.ANNOTATION,
          TokenTypes.ANNOTATIONS,
          TokenTypes.AT,
          TokenTypes.LITERAL_PRIVATE,
          TokenTypes.LITERAL_PROTECTED,
          TokenTypes.LITERAL_STATIC,
          TokenTypes.FINAL,
          TokenTypes.LITERAL_CLASS);

  @Override
  public int[] getDefaultJavadocTokens() {
    return new int[] {
      JavadocTokenTypes.JAVADOC,
    };
  }

  @Override
  public int[] getRequiredJavadocTokens() {
    return getAcceptableJavadocTokens();
  }

  @Override
  public void visitJavadocToken(DetailNode detailNode) {
    detailNode = JavadocUtil.getFirstChild(detailNode);
    if (detailNode != null) {
      DetailAST classTree = getBlockCommentAst();
      while (classTree != null
          && classTree.getType() != TokenTypes.CLASS_DEF
          && CLASS_DEF_CONTAINS.contains(classTree.getType())) {
        classTree = classTree.getParent();
      }
      // log(detailNode.getLineNumber(), detailNode.getColumnNumber(), classTree.getText());
      if (classTree != null && classTree.getType() == TokenTypes.CLASS_DEF) {
        DetailAST name = classTree.getFirstChild().getNextSibling().getNextSibling();
        if (!detailNode.getText().startsWith(" The " + name.getText())) {
          log(
              detailNode.getLineNumber(),
              detailNode.getColumnNumber(),
              "JavaDoc for a class should start with its name "
                  + name.getText()
                  + detailNode.getText());
        }
      } /* if (classTree != null && classTree.getNextSibling() != null) {
              log(detailNode.getLineNumber(), detailNode.getColumnNumber(), classTree.getText());
              log(
                  detailNode.getLineNumber(),
                  detailNode.getColumnNumber(),
                  classTree.getNextSibling().getText());
              if (classTree.getNextSibling().getNextSibling() != null) {
                log(
                    detailNode.getLineNumber(),
                    detailNode.getColumnNumber(),
                    classTree.getNextSibling().getNextSibling().getText());
                if (classTree.getNextSibling().getNextSibling().getNextSibling() != null)
                  log(
                      detailNode.getLineNumber(),
                      detailNode.getColumnNumber(),
                      classTree.getNextSibling().getNextSibling().getNextSibling().getText());
              }
            }
          }
        }

        /* while (classTree != null && classTree.getType() != TokenTypes.CLASS_DEF) {
          classTree = classTree.getNextSibling();
        }

        /* if (classTree != null) {
            DetailAST classSubtree = classTree.getFirstChild();
            while (classSubtree != null && classSubtree.getType() != TokenTypes.IDENT) {
              classSubtree = classSubtree.getNextSibling();
            }
            detailNode = JavadocUtil.getFirstChild(detailNode);
            if (detailNode != null && detailNode.getType() != JavadocTokenTypes.EOF) {
              if (BlockCommentPosition.isOnClass(classTree) && classSubtree != null) {
                String text = detailNode.getText();

                if (!text.startsWith("The " + classSubtree.getText())) {
                  log(detailNode.getLineNumber(), detailNode.getColumnNumber(),
                   classSubtree.getText());
                }
                // log(detailNode.getLineNumber(), detailNode.getColumnNumber(),
                classTree.getText());
              }
              log(detailNode.getLineNumber(), detailNode.getColumnNumber(), classTree.getText());
            }
          }
        }

           */
    }
  }
}
