/**
 * Copyright 2012 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.editors.util;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

import com.nitorcreations.robotframework.eclipseide.structure.ParsedString;

public class TokenQueue {
    private final List<PendingToken> pendingTokens = new LinkedList<PendingToken>();
    private int nextTokenStart = 0;
    private int curTokenOff, curTokenLen;

    public void reset() {
        nextTokenStart = 0;
        assert pendingTokens.isEmpty();
        pendingTokens.clear();
        curTokenOff = curTokenLen = 0;
    }

    public IToken take() {
        PendingToken removed = pendingTokens.remove(0);
        curTokenOff += curTokenLen;
        curTokenLen = removed.len;
        assert removed.token != null;
        return removed.token;
    }

    public void addEof() {
        addToken(0, Token.EOF);
    }

    public boolean hasPending() {
        return !pendingTokens.isEmpty();
    }

    public void add(ParsedString arg, IToken token) {
        add(arg.getArgCharPos(), arg.getArgEndCharPos(), token);
    }

    public void add(int off, int eoff, IToken token) {
        if (off > nextTokenStart) {
            addToken(off - nextTokenStart, Token.UNDEFINED);
        }
        addToken(eoff - off, token);
        nextTokenStart = eoff;
    }

    private void addToken(int len, IToken token) {
        pendingTokens.add(new PendingToken(token, len));
    }

    public int getLastTakenTokenOffset() {
        return curTokenOff;
    }

    public int getLastTakenTokenLength() {
        return curTokenLen;
    }

}