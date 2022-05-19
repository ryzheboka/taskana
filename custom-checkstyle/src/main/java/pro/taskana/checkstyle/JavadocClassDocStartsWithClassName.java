package pro.taskana.checkstyle;

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.DetailNode;
import com.puppycrawl.tools.checkstyle.api.JavadocTokenTypes;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.AbstractJavadocCheck;
import com.puppycrawl.tools.checkstyle.utils.JavadocUtil;

@FileStatefulCheck
public class JavadocClassDocStartsWithClassName extends AbstractJavadocCheck {

  @Override
  public int[] getDefaultJavadocTokens() {
    return new int[] {
      JavadocTokenTypes.JAVADOC,
    };
  }

  /*
   * From the documentation: "The javadoc tokens that this check must be registered for."
   */
  @Override
  public int[] getRequiredJavadocTokens() {
    return getAcceptableJavadocTokens();
  }

  @Override
  public void visitJavadocToken(DetailNode detailNode) {
    /* You can print the tree using AstTreeStringPrinter.printJavaAndJavadocTree(File file))
    The tree with the block comment above the class definition looks e. g. like this:

      `--CLASS_DEF -> CLASS_DEF [10:0]
    |--MODIFIERS -> MODIFIERS [10:0]
    |   |--ANNOTATION -> ANNOTATION [10:0]
    |   |   |--BLOCK_COMMENT_BEGIN -> /* [9:0]
    |   |   |   |--COMMENT_CONTENT -> * The JavadocMissingWhitespaceAfterAsteriksCheck .  [9:2]
    |   |   |   |   `--JAVADOC -> JAVADOC [9:3]
    |   |   |   |       |--TEXT ->  The JavadocMissingWhitespaceAfterAsteriksCheck .  [9:3]
    |   |   |   |       `--EOF -> <EOF> [9:53]
    |   |   |   `--BLOCK_COMMENT_END -> */
    /* [9:52]
    |   |   |--AT -> @ [10:0]
    |   |   `--IDENT -> StatelessCheck [10:1]
    |   `--LITERAL_PUBLIC -> public [11:0]
    |--LITERAL_CLASS -> class [11:7]
    |--IDENT -> JavadocMissingWhitespaceAfterAsteriksCheck [11:13]
    |--EXTENDS_CLAUSE -> extends [11:56]
    |   `--IDENT -> AbstractJavadocCheck [11:64]
    `--OBJBLOCK -> OBJBLOCK [11:85]

    or like this:
    `--CLASS_DEF -> CLASS_DEF [11:0]
    |--MODIFIERS -> MODIFIERS [11:0]
    |--BLOCK_COMMENT_BEGIN -> /* [9:0]
    |   |--COMMENT_CONTENT -> * The JavadocMissingWhitespaceAfterAsteriksCheck .  [9:2]
    |   |   `--JAVADOC -> JAVADOC [9:3]
    |   |       |--TEXT ->  The JavadocMissingWhitespaceAfterAsteriksCheck .  [9:3]
    |   |       `--EOF -> <EOF> [9:53]
    |   `--BLOCK_COMMENT_END -> */
    /* [9:52]
    |--LITERAL_CLASS -> class [11:0]
    |--IDENT -> JavadocMissingWhitespaceAfterAsteriksCheck [11:6]
    |--EXTENDS_CLAUSE -> extends [11:49]
    |   `--IDENT -> AbstractJavadocCheck [11:57]
    `--OBJBLOCK -> OBJBLOCK [11:78]

    case 1: BLOCK_COMMENT_BEGIN is child of ANNOTATION
            Check, that ANNOTATION is child of MODIFIERS is child of CLASS_DEF
            Use second neighbour of MODIFIERS as class name
    case 2: BLOCK_COMMENT_BEGIN is child of CLASS_DEF
            Use second neighbour of BLOCK_COMMENT_BEGIN as class name


     */
    // set detailNode to TEXT Node, e. g. |--TEXT ->  The JavadocMissingWhitespaceAfterAsteriksCheck
    // .
    detailNode = JavadocUtil.getFirstChild(detailNode);
    if (detailNode != null) {
      DetailAST classTree = getBlockCommentAst();
      DetailAST parent = classTree.getParent();
      DetailAST className = null;
      if (parent != null && parent.getType() == TokenTypes.CLASS_DEF) {
        className = classTree.getNextSibling().getNextSibling();
      }
      if (parent != null && parent.getType() == TokenTypes.ANNOTATION) {
        className = parent.getParent().getNextSibling().getNextSibling();
      }
      if (className != null && !detailNode.getText().startsWith(" The " + className.getText())) {
        log(
            detailNode.getLineNumber(),
            detailNode.getColumnNumber(),
            "The Javadoc comment above a class definition must start with \"The \" + class name.");
      }
    }
  }
}
