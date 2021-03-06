/*
 * Copyright (c) 2006-2017 DMDirc Developers
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.dmdirc.addons.calc;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a tree of {@link Token}s.
 */
public class TreeToken {

    /** The children of this node. */
    private final List<TreeToken> children = new ArrayList<>();
    /** The token at the root of the tree. */
    private final Token token;
    /** Whether or not this tree has been processed. */
    private boolean processed = false;

    public TreeToken(final Token token) {
        this.token = token;
    }

    public List<TreeToken> getChildren() {
        return children;
    }

    public Token getToken() {
        return token;
    }

    public boolean isProcessed() {
        return processed;
    }

    /**
     * Adds the specified child to this tree.
     *
     * @param token The child to be added
     */
    public void addChild(final TreeToken token) {
        children.add(token);
    }

    /**
     * Sets the processed flag of this tree to true.
     */
    public void setProcessed() {
        processed = true;
    }

    /**
     * Evaluates this tree to return a number.
     *
     * @return A numerical evaluation of this tree.
     */
    public Number evaluate() {
        return token.getType().evaluate(this);
    }

    @Override
    public String toString() {
        return "TreeToken{" + "children=" + children
                + ", token=" + token
                + ", processed=" + processed + '}';
    }

}
