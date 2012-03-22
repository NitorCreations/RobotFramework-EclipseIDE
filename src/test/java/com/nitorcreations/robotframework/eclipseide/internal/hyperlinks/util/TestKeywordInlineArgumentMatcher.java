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

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.nitorcreations.robotframework.eclipseide.internal.hyperlinks.util.KeywordInlineArgumentMatcher.KeywordMatchResult;

@RunWith(Enclosed.class)
public class TestKeywordInlineArgumentMatcher {

    public static class Wildcard_matches {
        @Test
        public void t1() throws Exception {
            m("K*A*", "KLAA");
        }

        @Test
        public void t2() throws Exception {
            m("K*L*", "KLAA");
        }

        @Test
        public void t3() throws Exception {
            m("Hello *", "* world");
        }

        @Test
        public void t4() throws Exception {
            m("Log in as * with password *", "Log in as Kalle with password omglol");
        }

        @Test
        public void t5() throws Exception {
            m("Use *", "Use whatever you like");
        }

        @Test
        public void t6() throws Exception {
            m("*", "*");
        }

        @Test
        public void t7() throws Exception {
            m("*a*", "*b*");
        }

        @Test
        public void t9() throws Exception {
            m("*a*", "b*b");
        }

        @Test
        public void t10() throws Exception {
            m("*a*", "*a*");
        }

        private void m(String a, String b) {
            Assert.assertEquals("\"" + a + "\" should wildcard-match \"" + b + "\"", KeywordMatchResult.WILDCARD, match(a, b));
            Assert.assertEquals("\"" + b + "\" should wildcard-match \"" + a + "\"", KeywordMatchResult.WILDCARD, match(b, a));
        }

    }

    public static class Different {

        @Test
        public void t8() throws Exception {
            m("a", "b");
        }

        private void m(String a, String b) {
            Assert.assertEquals("\"" + a + "\" should not match \"" + b + "\"", KeywordMatchResult.DIFFERENT, match(a, b));
            Assert.assertEquals("\"" + b + "\" should not match \"" + a + "\"", KeywordMatchResult.DIFFERENT, match(b, a));
        }
    }

    public static class Exact {
        @Test
        public void t11() throws Exception {
            m("AAA", "aaa");
        }

        private void m(String a, String b) {
            Assert.assertEquals("\"" + a + "\" should match exactly \"" + b + "\"", KeywordMatchResult.EXACT, match(a, b));
            Assert.assertEquals("\"" + b + "\" should match exactly \"" + a + "\"", KeywordMatchResult.EXACT, match(b, a));
        }

    }

    static KeywordMatchResult match(final String a, final String b) {
        return KeywordInlineArgumentMatcher.match(a, b);
    }

}
