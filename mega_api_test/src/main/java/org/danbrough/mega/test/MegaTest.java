package org.danbrough.mega.test;

import java.io.File;
import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import org.danbrough.mega.ExecutorThreadPool;
import org.danbrough.mega.MegaClient;
import org.danbrough.mega.ThreadPool;

public class MegaTest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaTest.class.getSimpleName());

  private static final String APP_NAME = "DQcSQK7S";

  ConsoleReader console;
  MegaClient client;
  ThreadPool threadPool;
  FileHistory history;

  public MegaTest() throws IOException {
    super();
    console = new ConsoleReader();
    console.setPrompt(">> ");

    File historyFile = new File(System.getProperty("user.home"),
        ".megatest_history");
    log.warn("writing command history to {}", historyFile);
    history = new FileHistory(historyFile);
    console.setHistory(history);

    threadPool = new ExecutorThreadPool();
    client = new MegaClient(APP_NAME, threadPool);
  }

  public void printHelp() throws IOException {
    console.println("login email [password]/folderurl\n" + "mount\n"
        + "ls [-R] [path]\n" + "cd [path]\n" + "get remotefile\n"
        + "put localfile [path/email]\n" + "mkdir path\n"
        + "rm path (instant completion)\n"
        + "mv path path (instant completion)\n" + "cp path path/email\n"
        + "pwd\n" + "lcd [localpath]\n" + "share [path [email [access]]]\n"
        + "export path [del]\n" + "import exportedfilelink#key\n" + "whoami\n"
        + "passwd\n" + "debug\n" + "quit");
  }

  public void run(String[] args) throws IOException {

    log.trace("run():trace");
    log.debug("run():debug");
    log.info("run():info");
    log.warn("run():warn");
    log.error("run():error");

    log.error("INSTANCE: "
        + org.apache.http.message.BasicLineFormatter.INSTANCE);

    threadPool.start();
    client.start();
    try {
      String line;
      while ((line = console.readLine()) != null) {
        String words[] = line.split("\\s+");
        if (words.length == 0)
          continue;

        try {
          String cmd = words[0];
          log.debug("cmd [{}]", cmd);

          boolean addToHistory = true;

          if (cmd.equals("login")) {
            client.login(words[1], words[2]);
          } else if (cmd.equals("quit")) {
            return;
          } else if (cmd.equals("?") || cmd.equals("h") || cmd.equals("help")) {
            addToHistory = false;
            printHelp();
          } else {
            console.println("Invalid command");
            addToHistory = false;
          }

          if (addToHistory) {
            history.add(line);
            history.flush();
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } finally {

      threadPool.stop();
      client.stop();
    }
  }

  public static void main(String[] args) throws IOException {
    new MegaTest().run(args);
  }
}
