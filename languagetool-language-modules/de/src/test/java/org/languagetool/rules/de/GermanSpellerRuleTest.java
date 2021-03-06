/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AustrianGerman;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.language.SwissGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.hunspell.HunspellRule;

import java.io.IOException;
import java.util.*;

import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class GermanSpellerRuleTest {

  private static final GermanyGerman GERMAN_DE = new GermanyGerman();
  private static final SwissGerman GERMAN_CH = new SwissGerman();

  @Test
  public void testSortSuggestion() throws Exception {
    final GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(rule.sortSuggestionByQuality("fehler", Arrays.asList("Fehler", "fehl er", "fehle r")).toString(),
            is("[Fehler, fehl er]"));
    assertThat(rule.sortSuggestionByQuality("mülleimer", Arrays.asList("Mülheimer", "-mülheimer", "Melkeimer", "Mühlheimer", "Mülleimer")).toString(),
            is("[Mülleimer, Mülheimer, -mülheimer, Melkeimer, Mühlheimer]"));
  }

  @Test
  public void testGetAdditionalTopSuggestions() throws Exception {
    final GermanSpellerRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    final JLanguageTool langTool = new JLanguageTool(GERMAN_DE);
    RuleMatch[] matches1 = rule.match(langTool.getAnalyzedSentence("konservierungsstoffe"));
    assertThat(matches1[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffe]"));
    RuleMatch[] matches2 = rule.match(langTool.getAnalyzedSentence("konservierungsstoffstatistik"));
    assertThat(matches2[0].getSuggestedReplacements().toString(), is("[Konservierungsstoffstatistik]"));
    RuleMatch[] matches3 = rule.match(langTool.getAnalyzedSentence("konservierungsstoffsasdsasda"));
    assertThat(matches3[0].getSuggestedReplacements().size(), is(0));
    assertFirstSuggestion("denkte", "dachte", rule, langTool);
    assertFirstSuggestion("schwimmte", "schwamm", rule, langTool);
    assertFirstSuggestion("gehte", "ging", rule, langTool);
    assertFirstSuggestion("greifte", "griff", rule, langTool);
  }

  @Test
  public void testAddIgnoreWords() throws Exception {
    MyGermanSpellerRule rule = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    Set<String> set = new HashSet<>();
    rule.addIgnoreWords("Fußelmappse", set);
    assertTrue(set.contains("Fußelmappse"));
    rule.addIgnoreWords("Fußelmappse/N", set);
    assertTrue(set.contains("Fußelmappse"));
    assertTrue(set.contains("Fußelmappsen"));
    rule.addIgnoreWords("Toggeltröt/NS", set);
    assertTrue(set.contains("Toggeltröt"));
    assertTrue(set.contains("Toggeltröts"));
    assertTrue(set.contains("Toggeltrötn"));
    rule.addIgnoreWords("Toggeltröt/NS", set);
    MyGermanSpellerRule ruleCH = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    ruleCH.addIgnoreWords("Fußelmappse/N", set);
    assertTrue(set.contains("Fusselmappse"));
    assertTrue(set.contains("Fusselmappsen"));
  }
  
  private void assertFirstSuggestion(String input, String expected, GermanSpellerRule rule, JLanguageTool langTool) throws IOException {
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence(input));
    assertThat(matches[0].getSuggestedReplacements().get(0), is(expected));
  }

  @Test
  public void testDashAndHyphen() throws Exception {
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    final JLanguageTool langTool = new JLanguageTool(GERMAN_DE);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ist doch - gut")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Ist doch -- gut")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Stil- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung gut")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Stil-, Text- und Grammatikprüfung")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Stil-, Text- oder Grammatikprüfung")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Miet- und Zinseinkünfte")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Haupt- und Nebensatz")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Miet und Zinseinkünfte")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Stil- und Grammatik gut")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Flasch- und Grammatikprüfung gut")).length);
    //assertEquals(1, rule.match(langTool.getAnalyzedSentence("Haupt- und Neben")).length);  // hunspell accepts this :-(
  }

  @Test
  public void testGetSuggestionsFromSpellingTxt() throws Exception {
    MyGermanSpellerRule ruleGermany = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertThat(ruleGermany.getSuggestions("Ligafußboll").toString(), is("[Ligafußball, Ligafußballs]"));  // from spelling.txt
    MyGermanSpellerRule ruleSwiss = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    assertThat(ruleSwiss.getSuggestions("Ligafußboll").toString(), is("[Ligafussball, Ligafussballs]"));
    assertThat(ruleSwiss.getSuggestions("konfliktbereid").toString(), is("[konfliktbereit, konfliktbereite]"));
    assertThat(ruleSwiss.getSuggestions("konfliktbereitel").toString(),
               is("[konfliktbereite, konfliktbereitem, konfliktbereiten, konfliktbereiter, konfliktbereites, konfliktbereit]"));
  }

  @Test
  public void testIgnoreWord() throws Exception {
    MyGermanSpellerRule ruleGermany = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertTrue(ruleGermany.doIgnoreWord("einPseudoWortFürLanguageToolTests"));  // from ignore.txt
    assertTrue(ruleGermany.doIgnoreWord("Ligafußball"));       // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchen"));   // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("Wichtelmännchens"));  // from spelling.txt with suffix
    assertTrue(ruleGermany.doIgnoreWord("vorgehängt"));        // from spelling.txt
    assertTrue(ruleGermany.doIgnoreWord("vorgehängten"));      // from spelling.txt with suffix
    MyGermanSpellerRule ruleSwiss = new MyGermanSpellerRule(TestTools.getMessages("de"), GERMAN_CH);
    assertTrue(ruleSwiss.doIgnoreWord("einPseudoWortFürLanguageToolTests"));
    assertFalse(ruleSwiss.doIgnoreWord("Ligafußball"));        // 'ß' never accepted for Swiss
  }

  private static class MyGermanSpellerRule extends GermanSpellerRule {
    MyGermanSpellerRule(ResourceBundle messages, German language) throws IOException {
      super(messages, language);
      init();
    }
    boolean doIgnoreWord(String word) throws IOException {
      return super.ignoreWord(Collections.singletonList(word), 0);
    }
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithGermanyGerman() throws Exception {
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    final JLanguageTool langTool = new JLanguageTool(GERMAN_DE);
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
    // TODO: this is a false alarm:
    //assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Mozart'sche Sonate.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithAustrianGerman() throws Exception {
    final AustrianGerman language = new AustrianGerman();
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), language);
    final JLanguageTool langTool = new JLanguageTool(language);
    commonGermanAsserts(rule, langTool);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // umlauts
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);
  }

  // note: copied from HunspellRuleTest
  @Test
  public void testRuleWithSwissGerman() throws Exception {
    final SwissGerman language = new SwissGerman();
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), language);
    final JLanguageTool langTool = new JLanguageTool(language);
    commonGermanAsserts(rule, langTool);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der äußere Übeltäter.")).length);  // ß not allowed in Swiss
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der äussere Übeltäter.")).length);  // ss is used instead of ß
  }
  
  // note: copied from HunspellRuleTest
  private void commonGermanAsserts(HunspellRule rule, JLanguageTool langTool) throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch")).length);  // compound
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Versuch")).length);  // compound
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Der Arbeitnehmer")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Die Verhaltensänderung")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentest-Dftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Dftgedgs-Waschmaschinentest")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestdftgedgs")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Waschmaschinentestversuch orkt")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Der Arbeitsnehmer")).length);  // wrong interfix
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Die Verhaltenänderung")).length);  // missing interfix
    assertEquals(2, rule.match(langTool.getAnalyzedSentence("Der asdegfue orkt")).length);
  }
  
  @Test
  public void testGetSuggestions() throws Exception {
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);

    assertCorrection(rule, "Hauk", "Haus", "Haut");
    assertCorrection(rule, "Hauk", "Haus", "Haut");
    assertCorrection(rule, "Eisnbahn", "Einbahn", "Eisbahn", "Eisenbahn"); 
    assertCorrection(rule, "Rechtschreipreform", "Rechtschreibreform");
    assertCorrection(rule, "Theatrekasse", "Theaterkasse");
    assertCorrection(rule, "Traprennen", "Trabrennen");
    assertCorrection(rule, "Autuverkehr", "Autoverkehr");
    assertCorrection(rule, "Rechtschreibprüfun", "Rechtschreibprüfung");
    assertCorrection(rule, "Rechtschreib-Prüfun", "Rechtschreib-Prüfung");
    
    //TODO: requires morfologik-speller change (suggestions for known words):
    //assertCorrection(rule, "Arbeitamt", "Arbeitsamt");

    assertCorrection(rule, "Autoverkehrr", "Autoverkehr");

    assertCorrection(rule, "hasslich", "hässlich", "fasslich");
    assertCorrection(rule, "Struße", "Strauße", "Straße", "Sträuße");
    
    assertCorrection(rule, "gewohnlich", "gewöhnlich");
    assertCorrection(rule, "gawöhnlich", "gewöhnlich");
    assertCorrection(rule, "gwöhnlich", "gewöhnlich");
    assertCorrection(rule, "geewöhnlich", "gewöhnlich");
    assertCorrection(rule, "gewönlich", "gewöhnlich");
    
    assertCorrection(rule, "außergewöhnkich", "außergewöhnlich");
    assertCorrection(rule, "agressiv", "aggressiv");
    assertCorrection(rule, "agressivster", "aggressivster");
    assertCorrection(rule, "agressiver", "aggressiver");
    assertCorrection(rule, "agressive", "aggressive");
    
    assertCorrection(rule, "Algorythmus", "Algorithmus");
    assertCorrection(rule, "Algorhythmus", "Algorithmus");
    
    assertCorrection(rule, "Amalgan", "Amalgam");
    assertCorrection(rule, "Amaturenbrett", "Armaturenbrett");
    assertCorrection(rule, "Aquise", "Akquise");
    assertCorrection(rule, "Artzt", "Arzt");
    
    assertCorrection(rule, "aufgrunddessen", "aufgrund dessen");
    
    assertCorrection(rule, "barfuss", "barfuß");
    assertCorrection(rule, "Batallion", "Bataillon");
    
    // use to work with jwordsplitter 3.4: too many other suggestions with Levenshtein=2
    //assertCorrection(rule, "Handselvertreter", "Handelsvertreter");
    //assertCorrection(rule, "Handselvertretertreffen", "Handelsvertretertreffen");
    
    assertCorrection(rule, "aul", "auf");
    assertCorrection(rule, "Icj", "Ich");   // only "ich" (lowercase) is in the lexicon
    //assertCorrection(rule, "Ihj", "Ich");   // only "ich" (lowercase) is in the lexicon - does not work because of the limit

    // three part compounds:
    assertCorrection(rule, "Handelsvertretertrffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvartretertreffen", "Handelsvertretertreffen");
    assertCorrection(rule, "Handelsvertretertriffen", "Handelsvertretertreffen");
      
    // this won't work as jwordsplitter splits into Handelsvertrter + Treffen but
    // the Hunspell dict doesn't contain "Handelsvertreter", thus it's a known limitation
    // because jwordsplitter doesn't use the same dictionary as Hunspell:
    // assertCorrection(rule, "Handelsvertrtertreffen", "Handelsvertretertreffen");

    // TODO: compounds with errors in more than one part
    // totally wrong jwordsplitter split: Hands + elvertretertreffn:
    //assertCorrection(rule, "Handselvertretertreffn", "Handelsvertretertreffen");
  }

  @Test
  public void testGetSuggestionOrder() throws Exception {
    final HunspellRule rule = new GermanSpellerRule(TestTools.getMessages("de"), GERMAN_DE);
    assertCorrectionsByOrder(rule, "heisst", "heißt");  // "heißt" should be first
    assertCorrectionsByOrder(rule, "heissen", "heißen");
    assertCorrectionsByOrder(rule, "müßte", "müsste");
    assertCorrectionsByOrder(rule, "schmohren", "schmoren");
    assertCorrectionsByOrder(rule, "Fänomen", "Phänomen");
    assertCorrectionsByOrder(rule, "homofob", "homophob");
    assertCorrectionsByOrder(rule, "ueber", "über");
    assertCorrectionsByOrder(rule, "uebel", "übel");
    assertCorrectionsByOrder(rule, "Aerger", "Ärger");
    assertCorrectionsByOrder(rule, "Walt", "Wald");
    assertCorrectionsByOrder(rule, "Rythmus", "Rhythmus");
    assertCorrectionsByOrder(rule, "Rytmus", "Rhythmus", "Remus");
  }
  
  private void assertCorrection(HunspellRule rule, String input, String... expectedTerms) throws IOException {
    final List<String> suggestions = rule.getSuggestions(input);
    for (String expectedTerm : expectedTerms) {
      assertTrue("Not found: '" + expectedTerm + "' in: " + suggestions, suggestions.contains(expectedTerm));
    }
  }
  
  private void assertCorrectionsByOrder(HunspellRule rule, String input, String... expectedTerms) throws IOException {
    final List<String> suggestions = rule.getSuggestions(input);
    int i = 0;
    for (String expectedTerm : expectedTerms) {
      assertTrue("Not found at position " + i + ": '" + expectedTerm + "' in: " + suggestions, suggestions.get(i).equals(expectedTerm));
      i++;
    }
  }
  
}
