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
package com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util;

public class KeywordInlineArgumentMatcher {

    public enum KeywordMatchResult {
        DIFFERENT, WILDCARD, EXACT
    }

    // private static String indent = "";

    public static KeywordMatchResult match(String a, String b) {
        a = a.replaceAll("\\$\\{[^}]+\\}", Character.toString(WILDCARD));
        b = b.replaceAll("\\$\\{[^}]+\\}", Character.toString(WILDCARD));
        // System.out.println(indent + "\"" + a.replace(WILDCARD, '*') +
        // "\" vs \"" + b.replace(WILDCARD, '*') + "\"");
        if (a.equalsIgnoreCase(b)) {
            return a.indexOf(WILDCARD) == -1 ? KeywordMatchResult.EXACT : KeywordMatchResult.WILDCARD;
        }
        // String old = indent;
        // indent += " |  ";
        boolean res = match3(a, 0, a.length() - 1, b, 0, b.length() - 1);
        // indent = old;
        // System.out.println(indent + " '-> " + res);
        return res ? KeywordMatchResult.WILDCARD : KeywordMatchResult.DIFFERENT;
    }

    private static boolean match(final String a, final int as, final int ae, final String b, final int bs, final int be) {
        // System.out.println(indent + "\"" + a.substring(as, ae +
        // 1).replace(WILDCARD, '*') + "\" vs \"" + b.substring(bs, be +
        // 1).replace(WILDCARD, '*') + "\"");
        // String old = indent;
        // indent += " |  ";
        boolean res = match3(a, as, ae, b, bs, be);
        // indent = old;
        // System.out.println(indent + " '-> " + res);
        return res;
    }

    private static final char WILDCARD = '\0';

    private static boolean match3(final String a, final int as, final int ae, final String b, final int bs, final int be) {
        if (as > ae || bs > be) {
            return (as > ae) == (bs > be);
        }
        if ((as == ae && a.charAt(as) == WILDCARD) || (bs == be && b.charAt(bs) == WILDCARD)) {
            return true;
        }
        if (a.charAt(as) == WILDCARD && b.charAt(be) == WILDCARD) {
            return true;
        }
        if (a.charAt(ae) == WILDCARD && b.charAt(bs) == WILDCARD) {
            return true;
        }
        if (a.charAt(as) == b.charAt(bs)) {
            if (a.charAt(as) == WILDCARD) {
                return match(a, as + 1, ae, b, bs, be) || match(a, as, ae, b, bs + 1, be);
            }
            return match(a, as + 1, ae, b, bs + 1, be);
        }
        if (a.charAt(ae) == b.charAt(be)) {
            if (a.charAt(ae) == WILDCARD) {
                return match(a, as, ae - 1, b, bs, be) || match(a, as, ae, b, bs, be - 1);
            }
            return match(a, as, ae - 1, b, bs, be - 1);
        }
        if (b.charAt(bs) == WILDCARD) {
            return match(b, bs, be, a, as, ae);
        }
        if (a.charAt(as) == WILDCARD) {
            for (int bp = bs; bp <= be; ++bp) {
                if (match(a, as + 1, ae, b, bp, be)) {
                    return true;
                }
            }
        }
        return false;
    }

}
