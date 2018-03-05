//Colin Shaw 100628526

//import TestFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SpamDetector {
//  private Map<String,Integer> trainHamFreq;
  //private Map<String,Integer> trainSpamFreq;

  private static int filenum =0;
  private static float hamFileNum =0;
  private static float spamFileNum =0;
  private static Map<String,Integer> trainHamFreq = new TreeMap<>();
  private static Map<String,Integer> trainSpamFreq = new TreeMap<>();
  private static Map<String,Float> probOfSpamGivenWord = new TreeMap<>();
  private static ArrayList<TestFile> testedFiles = new ArrayList<>();

  private static Map<String, Integer> processFile(File file) throws IOException {
    Map<String,Integer> wordCounts = new TreeMap<>();
    if (file.isDirectory()) {
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
          if(null == probOfSpamGivenWord.get(word)){//we never saw this word
          }
          else if(0 == probOfSpamGivenWord.get(word)){//only in ham files
            System.err.println("Error prob was 0");
          }
          else if(null!=probOfSpamGivenWord.get(word)){
            probSum += ((Math.log(1-probOfSpamGivenWord.get(word)))-(Math.log(probOfSpamGivenWord.get(word))));
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

    File hamDir = new File("data/train/ham2");
    File spamDir = new File("data/train/spam");



    try {
      trainHamFreq = processFile(hamDir);
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
      spamFileNum = filenum;
      filenum=0;

    } catch (FileNotFoundException e) {
      System.err.println("Invalid input dir: " + spamDir.getAbsolutePath());
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }



    //first we look through spam map

    Set<String> keys = trainSpamFreq.keySet();
    Iterator<String> keyIterator = keys.iterator();

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      int spamCount = trainSpamFreq.get(key);

      float probSpam = (float)(spamCount)/(float)(spamFileNum);
            if(probSpam==0){System.err.println("UHOH");}
      int hamCount;
      if (null == trainHamFreq.get(key)){
        hamCount = 1;
      }
      else{
        hamCount = trainHamFreq.get(key);
      }

      float probHam = (float)hamCount/(float)hamFileNum;



      if(probHam+probSpam!=0){
        probOfSpamGivenWord.put(key, (probSpam/(probSpam+probHam)));
      }
      else{//we never saw this word
        System.err.println("ERROR SOMEING WENT WRONG");
        probOfSpamGivenWord.put(key, (float)0.5);
      }
    }

    //this goes through all words only found in ham files
    keys = trainHamFreq.keySet();
    keyIterator = keys.iterator();

    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      float hamCount = trainHamFreq.get(key);

      if(null == trainSpamFreq.get(key)){
        probOfSpamGivenWord.put(key, (1/(float)spamFileNum)/((1/(float)spamFileNum)+(hamCount/(float)hamFileNum)));
      }
    }

    try{
    isSpamProbability(new File("data/test/ham"));
    isSpamProbability(new File("data/test/spam"));
  } catch (IOException e) {
    e.printStackTrace();
  }

    return testedFiles;
  }

  public static double getAccuracy(){
    double correct=0;
    for(int i=0;i<testedFiles.size();i++){
      if(((testedFiles.get(i).getActualClass().compareTo("spam")!=0)&&testedFiles.get(i).getSpamProbability()>0.5) ||
      ((testedFiles.get(i).getActualClass().compareTo("ham")!=0)&&testedFiles.get(i).getSpamProbability()<=0.5)){
        correct++;
      }
    }
    return correct/(testedFiles.size());
  }

  public static double getPrecision(){
    double truePos=0;
    double falsePos=0;
    for(int i=0;i<testedFiles.size();i++){
      if((testedFiles.get(i).getActualClass().compareTo("spam")!=0)&&testedFiles.get(i).getSpamProbability()>0.5){
        truePos++;
      }else if((testedFiles.get(i).getActualClass().compareTo("ham")!=0)&&testedFiles.get(i).getSpamProbability()>0.5){
        falsePos++;
      }
    }
    return truePos/(truePos+falsePos);
  }

  public static void main(String[] args) {
    runAll(args);
  }
}
