//Colin Shaw 100628526

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class SpamDetector {

//i was having some issues origonally so i just made most of my variables global
  private static int filenum =0;
  private static float hamFileNum =0;
  private static float spamFileNum =0;
  private static Map<String,Integer> trainHamFreq = new TreeMap<>();
  private static Map<String,Integer> trainSpamFreq = new TreeMap<>();
  private static Map<String,Float> probOfSpamGivenWord = new TreeMap<>();
  private static ArrayList<TestFile> testedFiles = new ArrayList<>();


  //this function will take a file or directory and check how many times each
  //word appears (counting only one per file)
  private static Map<String, Integer> processFile(File file) throws IOException {
    //this stores our map
    Map<String,Integer> wordCounts = new TreeMap<>();
    if (file.isDirectory()) {
      System.out.println("Processing " + file.getAbsolutePath() + "...");
      // process all the files in that directory
      File[] contents = file.listFiles();
      for (File current: contents) {
        Map<String,Integer> temp;
        //if it was a directory continually call until you get files
        temp = processFile(current);


        //this code makes sure the returned list adds to the current one and
        //doesn't overwirte the numbers
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
      //count how many files ther are
      filenum++;
      //counted keeps track of which words we already saw in this file
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
      //clear counted since we are done the file
      counted.clear();
    }
    return wordCounts;
  }

  private static boolean isWord(String word) {//check if this is a word
    String pattern = "^[a-zA-Z]+$";
    return word.matches(pattern);

  }

  private static Map<String, Integer> countWord(String word, Map<String, Integer> wordCounts) {
    //adds the word to our Map
    if (wordCounts.containsKey(word)) {
      int oldCount = wordCounts.get(word);
      wordCounts.put(word, oldCount+1);
    } else {
      wordCounts.put(word, 1);
    }
    return wordCounts;
  }



  //takes a file or folder and calculates the spamProbability
  //this is stored in TestFiles arrays list
  private static void spamProbability(File file) throws IOException {
    if (file.isDirectory()) {
      System.out.println("Testing " + file.getAbsolutePath() + "...");
      // process all the files in that directory
      File[] contents = file.listFiles();
      for (File current: contents) {
        //if it was directory sarch through the files
        spamProbability(current);
      }
    } else if (file.exists()) {
      Scanner scanner = new Scanner(file);
      double probSum=0;
      scanner.useDelimiter("\\s");//"[\s\.;:\?\!,]");//" \t\n.;,!?-/\\");
      while (scanner.hasNext()) {
        String word = scanner.next().toLowerCase();
        if (isWord(word)) {
          //we never saw this word in training so ignore it
          if(null == probOfSpamGivenWord.get(word)){}
            //this is just an error check
          else if(0 == probOfSpamGivenWord.get(word)){
            System.err.println("Error prob was 0");
          }
          //Probability formula to make small values less likely
          else if(null!=probOfSpamGivenWord.get(word)){
            probSum += ((Math.log(1-probOfSpamGivenWord.get(word)))-(Math.log(probOfSpamGivenWord.get(word))));
          }
        }
      }
      //last step of Probability equation
      double probEst = 1/(1+Math.pow(Math.E,probSum));

      //this get the parent folders name
      String[] actualTypeArr = file.getParent().split("/");
      String actualType = actualTypeArr[actualTypeArr.length-1];

      //create a new TestFile and add it to the ArrayList
      testedFiles.add(new TestFile(file.getName(),probEst, actualType));
    }
  }

  public static ArrayList<TestFile> getTestedFiles() {//main uses this
      return testedFiles;
  }

  //this is basically the main function
  //itellij didn't like me calling main
  public static ArrayList<TestFile> runAll(String[] args){
    //directory path is passed via args
    File hamDir = new File(args[0]+"/train/ham");
    File spamDir = new File(args[0]+"/train/spam");



    try {
      //get word count from ham training
      trainHamFreq = processFile(hamDir);
      //store number of ham files and reset counter
      hamFileNum = filenum;
      filenum=0;

    } catch (FileNotFoundException e) {
      System.err.println("Invalid input dir: " + hamDir.getAbsolutePath());
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }



    try {
      //get word count from spam training
      trainSpamFreq = processFile(spamDir);
      //store number of spam files and reset counter
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

      // should never be zero
      float probSpam = (float)(spamCount)/(float)(spamFileNum);

      if(probSpam==0){System.err.println("spam prob was zero");}

      float hamCount;
      if (null == trainHamFreq.get(key)){
        //we dont want any zero
        hamCount = 1;
      }
      else{
        hamCount = trainHamFreq.get(key);
      }

      float probHam = hamCount/(float)hamFileNum;


      //check for zeroes then put the calculated prob into the map
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

      //this is almost zero but not quite so we avoid divide by 0 errors
      if(null == trainSpamFreq.get(key)){
        probOfSpamGivenWord.put(key, hamCount/hamFileNum);
      }
    }

    try{
    //check the Probability we found against data
    spamProbability(new File(args[0]+"/test/ham"));
    spamProbability(new File(args[0]+"/test/spam"));

  } catch (IOException e) {
      e.printStackTrace();
  }

    return testedFiles;
  }

  //get what percent of geusses were right
  public static double getAccuracy(){
    double correct=0;
    for(int i=0;i<testedFiles.size();i++){
      if(((testedFiles.get(i).getActualClass().compareTo("spam")==0)&&testedFiles.get(i).getSpamProbability()>=0.5) ||
      ((testedFiles.get(i).getActualClass().compareTo("ham")==0)&&testedFiles.get(i).getSpamProbability()<=0.5)){
        correct++;
      }
    }
    return correct/(testedFiles.size());
  }

  //find how percise the program was
  public static double getPrecision(){
    double truePos=0;
    double falsePos=0;
    for(int i=0;i<testedFiles.size();i++){
      if((testedFiles.get(i).getActualClass().compareTo("spam")==0)&&testedFiles.get(i).getSpamProbability()>0.5){
        truePos++;
      }else if((testedFiles.get(i).getActualClass().compareTo("ham")==0)&&testedFiles.get(i).getSpamProbability()>0.5){
        falsePos++;
      }
    }
    return truePos/(truePos+falsePos);
  }

  public static void main(String[] args) {
    //does all computations 
    runAll(args);
  }
}
