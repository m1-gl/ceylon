/********************************************************************************
 * Copyright (c) 2011-2017 Red Hat Inc. and/or its affiliates and others
 *
 * This program and the accompanying materials are made available under the 
 * terms of the Apache License, Version 2.0 which is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * SPDX-License-Identifier: Apache-2.0 
 ********************************************************************************/
package org.eclipse.ceylon.common.tools.help;

import org.eclipse.ceylon.common.tool.WordWrap;
import org.tautua.markdownpapers.ast.CharRef;
import org.tautua.markdownpapers.ast.Code;
import org.tautua.markdownpapers.ast.CodeSpan;
import org.tautua.markdownpapers.ast.CodeText;
import org.tautua.markdownpapers.ast.Document;
import org.tautua.markdownpapers.ast.Emphasis;
import org.tautua.markdownpapers.ast.Header;
import org.tautua.markdownpapers.ast.Image;
import org.tautua.markdownpapers.ast.InlineUrl;
import org.tautua.markdownpapers.ast.Item;
import org.tautua.markdownpapers.ast.Line;
import org.tautua.markdownpapers.ast.LineBreak;
import org.tautua.markdownpapers.ast.Link;
import org.tautua.markdownpapers.ast.List;
import org.tautua.markdownpapers.ast.Node;
import org.tautua.markdownpapers.ast.Paragraph;
import org.tautua.markdownpapers.ast.Quote;
import org.tautua.markdownpapers.ast.Resource;
import org.tautua.markdownpapers.ast.Ruler;
import org.tautua.markdownpapers.ast.Tag;
import org.tautua.markdownpapers.ast.Text;

public class PlaintextMarkdownVisitor extends AbstractMarkdownVisitor {

    private WordWrap out;
    
    private int headerLevel = -1;

    private boolean inCode;

    private boolean inEmphasis;
    
    public PlaintextMarkdownVisitor(WordWrap out) {
        this.out = out;
    }
    
    @Override
    public void visit(Header node) {
        final Node parent = node.jjtGetParent();
        for (int ii = 0; ii < parent.jjtGetNumChildren(); ii++) {
            if (parent.jjtGetChild(ii) == node) {
                if (ii == 0) {
                    continue;
                }
            }
        }
        switch (node.getLevel()) {
        case 1:
        case 2:
            out.setIndent(0);
            break;
        case 3:
            out.setIndent(3);
            break;
        case 4:
            out.setIndent(5);
            break;
        case 5:
            out.setIndent(6);
            break;
        case 6:
        default:
            out.setIndent(7);
            break;
        }
        if (!(node.jjtGetParent() instanceof Document && Markdown.getIndexInParent(node) == 0)) {
            out.newline();
        }
        switch (node.getLevel()) {
        case 1:
            int col = out.getColumn();
            this.headerLevel = node.getLevel();
            node.childrenAccept(this);
            this.headerLevel = -1;
            int num = out.getColumn() - col;
            out.newline();
            for (int ii = 0; ii < num; ii++) {
                out.append("=");
            }
            out.newline();
            out.setIndent(8);
            break;
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        default:
            this.headerLevel = node.getLevel();
            node.childrenAccept(this);
            this.headerLevel = -1;
            out.newline();
            out.setIndent(8);
            break;        }
        out.newline();
    }
    
    @Override
    public void visit(Item node) {
        int rest = out.getIndentRestLines();
        if (((List)node.jjtGetParent()).isOrdered()) {
            for (int ii = 0; ii < node.jjtGetParent().jjtGetNumChildren(); ii++) {
                if (node.jjtGetParent().jjtGetChild(ii) == node) {
                    out.append((ii+1)+". ");
                    out.setIndent(rest + 3);
                    break;
                }
            }
        } else {
            out.append("* ");
            out.setIndent(rest + 2);
        }
        node.childrenAccept(this);
        out.setIndent(rest);
        out.newline().newline();
    }

    @Override
    public void visit(Paragraph node) {
        node.childrenAccept(this);
        if (!(node.jjtGetParent() instanceof Item)) {
            out.newline().newline();
        }
    }

    @Override
    public void visit(Quote node) {
        out.setPrefix("> ");
        node.childrenAccept(this);
        out.setPrefix(null);
    }

    @Override
    public void visit(CodeText node) {
        out.append(node.getValue());
        out.newline();
    }
    
    @Override
    public void visit(Code node) {
        this.inCode = true;
        node.childrenAccept(this);
        this.inCode = false;
    }
    
    @Override
    public void visit(Ruler node) {
        out.newline();
        out.column(out.getWidth()/2-2);
        out.append("* * *").newline().newline();
    }
    
    @Override
    public void visit(Tag node) {
        out.append("<").append(node.getName()).append(">");
        node.childrenAccept(this);
        out.append("</").append(node.getName()).append(">");
    }
    
    @Override
    public void visit(CharRef node) {
        out.append(node.getValue());
    }

    @Override
    public void visit(CodeSpan node) {
        String text = node.getText();
        if (uppercaseText()) {
            text = text.toUpperCase();
        }
        out.append("'").append(text).append("'");
    }

    @Override
    public void visit(Emphasis node) {
        boolean prevInEmphasis = inEmphasis;
        switch (node.getType()) {
        case ITALIC_AND_BOLD:
        case BOLD:
            inEmphasis = true;
            break;
        case ITALIC:
        default:    
            break;
        }
        node.childrenAccept(this);
        inEmphasis = prevInEmphasis;
    }

    @Override
    public void visit(Image node) {
        // TODO Issue a warning? Or should we try to copy the image into the
        // target directory?
        node.childrenAccept(this);
    }

    @Override
    public void visit(Line node) {
        node.childrenAccept(this);
        if (!node.isEmpty() && !node.isEnding()) {
            if (inCode 
                    && Markdown.getNext(node) != null) {
                out.append("");
            } else if (!inCode) {
                out.append(" ");
            }
        }
        if (node.isEmpty() && inCode) {
            out.newline();
        }
    }

    @Override
    public void visit(LineBreak node) {
        out.append(" ");
        node.childrenAccept(this);
    }

    @Override
    public void visit(Link node) {
        Resource resource = node.getResource();
        if (resource == null) {
            Node doc = node.jjtGetParent();
            while (!(doc instanceof Document)) {
                doc = doc.jjtGetParent();
            }
            resource = ((Document)doc).findResource(node.getReference());
        }
        if (resource != null) {
            out.append(node.getText()).append(" (").append(resource.getLocation()).append(")");
        } else {
            out.append(node.getText());
        }
    }

    @Override
    public void visit(InlineUrl node) {
        out.append(node.getUrl());
    }

    @Override
    public void visit(final Text node) {
        if (node.isWhitespace()) {
            if (headerLevel >= 1
                    && node.jjtGetParent().jjtGetChild(0) == node) {
                // filter whitespace at the start of headers
                return;
            }
        }
        String text = node.getValue();
        if (uppercaseText()) {
            text = text.toUpperCase();
        }
        out.append(text);
    }

    private boolean uppercaseText() {
        return headerLevel == 1 || headerLevel == 2 || inEmphasis;
    }

}
