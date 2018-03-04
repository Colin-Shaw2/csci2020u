//Colin Shaw 100628526

import java.io.*;
import java.util.*;

public class SpamDetector {
//  private Map<String,Integer> trainHamFreq;
  //private Map<String,Integer> trainSpamFreq;

  public static int filenum =0;
  public static int hamFileNum =0;
  public static int spamFileNum =0;
  public static Map<String,Integer> trainHamFreq = new TreeMap<>();
  public static Map<String,Integer> trainSpamFreq = new TreeMap<>();


  public static Map<String, Integer> processFile(File file) throws IOException {
    Map<String,Integer> wordCounts = new TreeMap<>();
    if (file.isDirectory()) {
      System.out.println("Processing " + file.getAbsolutePath() + "...");
      // process all the files in that directory
      File[] contents = file.listFiles();
      for (File current: contents) {
        wordCounts.putAll(processFile(current));
      }
    } else if (file.exists()) {
      filenum++;
      // count the words in this file
      ArrayList<String> counted = new ArrayList<String>();
      Scanner scanner = new Scanner(file);
      scanner.useDelimiter("\\s");//"[\s\.;:\?\!,]");//" \t\n.;,!?-/\\");
      while (scanner.hasNext()) {
        String word = scanner.next().toLowerCase();
        if (isWord(word) && !counted.contains(word)) {
          wordCounts = countWord(word, wordCounts);
          counted.add(word);
        }
      }
    }
    return wordCounts;
  }

  private static boolean isWord(String word) {
    String pattern = "^[a-zA-Z]+$";
    if (word.matches(pattern)) {
      return true;
    } else {
      return false;
    }

    // also fine:
    //return word.matches(pattern);
  }

  private static Map<String, Integer> countWord(String word, Map<String, Integer> wordCounts) {
    if (wordCounts.containsKey(word)) {
      int oldCount = wordCounts.get(word);
      wordCounts.put(word, oldCount+1);
    } else {
      wordCounts.put(word, 1);
    }
    return wordCounts;
  }

//  private boolean isCounted(String word, ArrayList<String> counted){
  //  return counted.contains(word);
  //}

  public static void outputWordCounts(int minCount, File outFile, Map<String, Integer> wordCounts)
    throws IOException {
    System.out.println("Saving word counts to " + outFile.getAbsolutePath());
    System.out.println("# of words: " + wordCounts.keySet().size());
    System.out.println("# of files: " + filenum);

    outFile.createNewFile();
    if (outFile.canWrite()) {
      PrintWriter fileOut = new PrintWriter(outFile);

      Set<String> keys = wordCounts.keySet();
      Iterator<String> keyIterator = keys.iterator();

      while (keyIterator.hasNext()) {
        String key = keyIterator.next();
        int count = wordCounts.get(key);

        if (count >= minCount) {
          fileOut.println(key + ": " + count);
        }
      }

      fileOut.close();
    } else {
      System.err.println("Error:  Cannot write to file: " + outFile.getAbsolutePath());
    }

    if (outFile.exists()) {
      System.err.println("File already exists: Overwriting  " + outFile.getAbsolutePath());
    }
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: java SpamDetector <dir/ham> <dir/spam>");
      //System.exit(0);
    }

    SpamDetector SpamDetector = new SpamDetector();
  //  File hamDir = new File(args[0]);
    //File spamDir = new File(args[1]);
    File hamDir = new File("data/train/ham");
    File spamDir = new File("data/train/spam");
    File outFileHam = new File("hamWords.txt");
    File outFileSpam = new File("spamWords.txt");


    //Map<String,Integer> trainHamFeq;
    try {
      trainHamFreq = processFile(hamDir);
      System.out.println(trainHamFreq);
      //outputWordCounts(1, outFileHam, trainHamFreq);
      hamFileNum = filenum;
      filenum=0;


/*

      Map<String, Float> probOfSpamGivenWord = new TreeMap<>();

      Set<String> keys = trainHamFreq.keySet();
      Iterator<String> keyIterator = keys.iterator();

          System.out.println(keyIterator.hasNext());
      while (keyIterator.hasNext()) {
        String key = keyIterator.next();
        int count = trainSpamFreq.get(key);

        float probHam = count/hamFileNum;
        System.out.println(probHam);
        //probOfSpamGivenWord.put(key, prob);

          //fileOut.println(key + ": " + count);

      }



*/



    } catch (FileNotFoundException e) {
      System.err.println("Invalid input dir: " + hamDir.getAbsolutePath());
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }



    try {
      trainSpamFreq = processFile(spamDir);
      //outputWordCounts(1, outFileSpam, trainSpamFreq);
      spamFileNum = filenum;
      filenum=0;
    } catch (FileNotFoundException e) {
      System.err.println("Invalid input dir: " + spamDir.getAbsolutePath());
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
