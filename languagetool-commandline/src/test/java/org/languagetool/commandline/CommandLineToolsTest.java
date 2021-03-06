/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.commandline;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.rules.WordRepeatRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.ResourceBundle;

public class CommandLineToolsTest extends TestCase {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(err));
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  public void testCheck() throws IOException, ParserConfigurationException, SAXException {
    final JLanguageTool tool = new JLanguageTool(TestTools.getDemoLanguage());

    int matches = CommandLineTools.checkText("Foo.", tool);
    String output = new String(this.out.toByteArray());
    assertEquals(0, output.indexOf("Time:"));
    assertEquals(0, matches);

    tool.disableRule("test_unification_with_negation");
    tool.addRule(new WordRepeatRule(getMessages("en"), TestTools.getDemoLanguage()));
    matches = CommandLineTools.checkText("To jest problem problem.", tool);
    output = new String(this.out.toByteArray());
    assertTrue(output.contains("Rule ID: WORD_REPEAT_RULE"));
    assertEquals(1, matches);
  }

  private static ResourceBundle getMessages(String language) {
    final ResourceBundle messages = ResourceBundle.getBundle(
            JLanguageTool.MESSAGE_BUNDLE, new Locale(language));
    return messages;
  }

}
