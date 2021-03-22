package parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ParsingHtml {

    private static List<Character> listCharacter = new ArrayList<>();
    private static List<Character> listBuffer = new ArrayList<>();

    public static void start(String url, String tagStart, String tagEnd) throws IOException {
        downloadHtmlCode(url);
        getTextFromHtmlTag(tagStart, tagEnd);
        deleteSymbolsInText();
        printResults();
    }

    @MicroBenchmark(profile = "testing", parameter = TypeBenchmark.SPEED)
    protected static void downloadHtmlCode(String addressUrl) throws IOException {

        InputStream is = null;
        try (BufferedReader bufferedReader =
                     new BufferedReader(new InputStreamReader(is = (new URL(addressUrl)).openStream()))) {
            bufferedReader.lines()
                    .filter(str -> !str.isEmpty())
                    .forEach(str -> {
                        for (char s : str.toCharArray())
                            listCharacter.add(s);
                    });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        is.close();
    }

    @MicroBenchmark(profile = "testing", parameter = TypeBenchmark.SPEED)
    protected static void getTextFromHtmlTag(String tagStart, String tagEnd) {

        char[] htmlTabStart = tagStart.toCharArray();
        char[] htmlTabEnd = tagEnd.toCharArray();
        boolean flagRecord = false;

        //Iterate the entire list, search for a given tab and write words between tabs
        for (int i = 0; i < listCharacter.size() - htmlTabEnd.length - 1; i++) {

            // The checking of entry point
            if (listCharacter.get(i) == htmlTabStart[0] && !flagRecord) {
                int countTagSymbolsStart = 0;
                for (int j = 1; j < htmlTabStart.length; j++) {
                    if (listCharacter.get(i + j) == htmlTabStart[j]) {
                        countTagSymbolsStart++;
                    } else break;
                }
                if (countTagSymbolsStart == htmlTabStart.length - 1) {
                    flagRecord = true;
                    countTagSymbolsStart = 0;
                }
            }

            // The Record of symbols from List<Character> to new List<Character> listBuffer
            if (flagRecord) {
                listBuffer.add(listCharacter.get(i + htmlTabStart.length));
            }

            // Closing the process of write
            if (listCharacter.get(i + htmlTabStart.length) == htmlTabEnd[0] && flagRecord) {
                int countTagSymbolsEnd = 0;
                for (int j = 1; j < htmlTabEnd.length; j++) {
                    if (listCharacter.get(i + htmlTabStart.length + j) == htmlTabEnd[j]) {
                        countTagSymbolsEnd++;
                    } else break;
                }
                if (countTagSymbolsEnd == htmlTabEnd.length - 1) {
                    flagRecord = false;
                    countTagSymbolsEnd = 0;
                    listBuffer.remove(listBuffer.size() - 1);
                }
            }
        }
    }

    @MicroBenchmark(profile = "testing", parameter = TypeBenchmark.SPEED)
    protected static void deleteSymbolsInText() {
        listCharacter.clear();
        boolean flagWrite = true;
        for (int i = 0; i < listBuffer.size() - 1; i++) {
            if (listBuffer.get(i) == '<' && flagWrite) flagWrite = false;
            if (flagWrite) listCharacter.add(listBuffer.get(i));
            if (listBuffer.get(i) == '>' && !flagWrite) flagWrite = true;
        }
        listBuffer.clear();
    }

    @RunMethod(profile = "testing")
    private static void printResults() {
        listCharacter.forEach(System.out::print);
    }
}
