//Colin Shaw 100628526
package sample;

import javafx.collections.ObservableList;
import java.io.*;
import java.util.*;

public class SpamDetector {
//  private Map<String,Integer> trainHamFreq;
  //private Map<String,Integer> trainSpamFreq;

  private static int filenum =0;
  private static int hamFileNum =0;
  private static int spamFileNum =0;
  private static Map<String,Integer> trainHamFreq = new TreeMap<>();
  private static Map<String,Integer> trainSpamFreq = new TreeMap<>();
  private static Map<String,Float> probOfSpamGivenWord = new TreeMap<>();
  private static ArrayList<TestFile> testedFiles = new ArrayList<>();

  private static Map<String, Integer> processFile(File file) throws IOException {
    Map<String,Integer> wordCounts = new TreeMap<>();
    if (file.isDirectory()) {
      System.out.println(wordCounts);
      System.out.println("Processing " + file.getAbsolutePath() + "...");
      // process all the files in that directory
      File[] contents = file.listFiles();
      for (File current: contents) {
        Map<String,Integer> temp;
        temp = processFile(current);

        Set<String> keys = temp.keySet();
        Iterator<String> keyIterator = keys.iterator();

        while (keyIterator.hasNext()) {
          String key = keyIterator.next();
          int count = temp.get(key);

            if (wordCounts.containsKey(key)) {
              int oldCount = wordCounts.get(key);
              wordCounts.put(key, oldCount+count);
            } else {
              wordCounts.put(key, 1);
            }


        }

      }
    } else if (file.exists()) {
      filenum++;
      // count the words in this file
      ArrayList<String> counted = new ArrayList<>();
      Scanner scanner = new Scanner(file);
      scanner.useDelimiter("\\s");//"[\s\.;:\?\!,]");//" \t\n.;,!?-/\\");
      while (scanner.hasNext()) {
        String word = scanner.next().toLowerCase();
        if (isWord(word) && !counted.contains(word)) {
          wordCounts = countWord(word, wordCounts);
          counted.add(word);
        }
      }
      counted.clear();
    }
    return wordCounts;
  }

  private static boolean isWord(String word) {
    String pattern = "^[a-zA-Z]+$";
    return word.matches(pattern);

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


  private static void isSpamProbability(File file) throws IOException {
    if (file.isDirectory()) {
      System.out.println("Testing " + file.getAbsolutePath() + "...");
      // process all the files in that directory
      File[] contents = file.listFiles();
      for (File current: contents) {
        isSpamProbability(current);
      }
    } else if (file.exists()) {
      Scanner scanner = new Scanner(file);
      double probSum=0;
      scanner.useDelimiter("\\s");//"[\s\.;:\?\!,]");//" \t\n.;,!?-/\\");
      while (scanner.hasNext()) {
        String word = scanner.next().toLowerCase();
        if (isWord(word)) {
          if(null!=probOfSpamGivenWord.get(word)){
            probSum += (Math.log(1-probOfSpamGivenWord.get(word))-Math.log(probOfSpamGivenWord.get(word)));
          }
        }
      }
      double probEst = 1/(1+Math.pow(Math.E,probSum));
      String[] actualTypeArr = file.getParent().split("/");
      String actualType = actualTypeArr[actualTypeArr.length-1];
      testedFiles.add(new TestFile(file.getName(),probEst, actualType));
    }
  }

    public static ArrayList<TestFile> getTestedFiles() {
        return testedFiles;
    }

  public static ArrayList<TestFile> runAll(String[] args){
    if (args.length < 2) {
      System.err.println("Usage: java SpamDetector <dir/ham> <dir/spam>");
      //System.exit(0);
    }

  //  File hamDir = new File(args[0]);
    //File spamDir = new File(args[1]);
    File hamDir = new File("src/sample/data/train/ham");
    File spamDir = new File("src/sample/data/train/spam");
    File outFileHam = new File("hamWords.txt");
    File outFileSpam = new File("spamWords.txt");



    //Map<String,Integer> trainHamFeq;
    try {
        System.out.println("hamddir is: " + hamDir.getPath());
      trainHamFreq = processFile(hamDir);
      //outputWordCounts(1, outFileHam, trainHamFreq);
      hamFileNum = filenum;
      filenum=0;

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



    //We are only going to look through the spam map because if the word is
    //only in the ham map the result is always 0
    System.out.println("trainSpamFreq = " + trainSpamFreq);

    Set<String> keys = trainSpamFreq.keySet();
    Iterator<String> keyIterator = keys.iterator();

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      int spamCount = trainSpamFreq.get(key);

      float probSpam = (float)spamCount/(float)spamFileNum;
      int hamCount;
      if (null == trainHamFreq.get(key)){
        hamCount = 0;
      }
      else{
        hamCount = trainHamFreq.get(key);
      }

      float probHam = (float)hamCount/(float)hamFileNum;

      if(probHam+probSpam!=0){
        probOfSpamGivenWord.put(key, (probSpam/(probSpam+probHam)));
      }
      else{
        probOfSpamGivenWord.put(key, (float)0);
      }
      System.out.println((probSpam/(probSpam+probHam)));
    }

    try{
    isSpamProbability(new File("src/sample/data/test/ham"));
    isSpamProbability(new File("src/sample/data/test/spam"));
  } catch (IOException e) {
    e.printStackTrace();
  }

    return testedFiles;
  }
  public static void main(String[] args) {
    runAll(args);
  }
}
