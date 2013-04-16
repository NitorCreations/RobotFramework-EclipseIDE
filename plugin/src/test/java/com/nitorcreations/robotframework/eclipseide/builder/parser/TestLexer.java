/**
 * Copyright 2013 Nitor Creations Oy
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
package com.nitorcreations.robotframework.eclipseide.builder.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

/**
 * {@link Lexer} does not assign values to {@link RobotLine#type}, so we do not test it here.
 */
public class TestLexer {
    @Test
    public void empty() throws Exception {
        Lexer lexer = new Lexer("");
        List<RobotLine> lines = lexer.lex();
        assertEquals(1, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
    }

    @Test
    public void space() throws Exception {
        Lexer lexer = new Lexer(" ");
        List<RobotLine> lines = lexer.lex();
        assertEquals(1, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
    }

    @Test
    public void letter() throws Exception {
        Lexer lexer = new Lexer("x");
        List<RobotLine> lines = lexer.lex();
        assertEquals(1, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(1));
    }

    @Test
    public void linefeed() throws Exception {
        Lexer lexer = new Lexer("\n");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(1));
        assertThat(line2.arguments.size(), is(0));
    }

    @Test
    public void letter_linefeed() throws Exception {
        Lexer lexer = new Lexer("x\n");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(1));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(2));
        assertThat(line2.arguments.size(), is(0));
    }

    @Test
    public void letter_linefeed_letter() throws Exception {
        Lexer lexer = new Lexer("x\nx");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(1));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(2));
        assertThat(line2.arguments.size(), is(1));
    }

    @Test
    public void linefeed_letter() throws Exception {
        Lexer lexer = new Lexer("\nx");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(1));
        assertThat(line2.arguments.size(), is(1));
    }

    @Test
    public void letter_space_linefeed() throws Exception {
        Lexer lexer = new Lexer("x \n");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(1));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(3));
        assertThat(line2.arguments.size(), is(0));
    }

    @Test
    public void twospaces_letter_linefeed() throws Exception {
        Lexer lexer = new Lexer("  x\n");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(2));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(4));
        assertThat(line2.arguments.size(), is(0));
    }

    @Test
    public void carriagereturn_linefeed() throws Exception {
        Lexer lexer = new Lexer("\r\n");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(2));
        assertThat(line2.arguments.size(), is(0));
    }

    @Test
    public void carriagereturn_linefeed_carriagereturn() throws Exception {
        Lexer lexer = new Lexer("\r\n\r");
        List<RobotLine> lines = lexer.lex();
        assertEquals(2, lines.size());
        RobotLine line1 = lines.get(0);
        assertThat(line1.lineNo, is(0));
        assertThat(line1.lineCharPos, is(0));
        assertThat(line1.arguments.size(), is(0));
        RobotLine line2 = lines.get(1);
        assertThat(line2.lineNo, is(1));
        assertThat(line2.lineCharPos, is(2));
        assertThat(line2.arguments.size(), is(0));
    }

}
