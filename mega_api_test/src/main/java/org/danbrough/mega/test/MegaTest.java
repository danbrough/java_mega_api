package org.danbrough.mega.test;

import java.io.File;
import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import org.danbrough.mega.AccountDetails;
import org.danbrough.mega.AccountDetails.UserSession;
import org.danbrough.mega.Callback;
import org.danbrough.mega.ExecutorThreadPool;
import org.danbrough.mega.MegaClient;
import org.danbrough.mega.ThreadPool;

public class MegaTest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaTest.class.getSimpleName());

  private static final String APP_KEY = "DQcSQK7S";

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
    client = new MegaClient(APP_KEY, threadPool);
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

  public void quit() {
    log.info("quit()");
    client.stop();
    threadPool.stop();
  }

  public void whoami() {
    log.info("whoami();");

    client.getAccountDetails(true, true, true, true, true, true,
        new Callback<AccountDetails>() {
          @Override
          public void onResult(AccountDetails result) {
            for (UserSession session : result.getSessions()) {
              System.out.println(session.toString());
            }
          }
        });

    // void MegaClient::getaccountdetails(AccountDetails* ad, int storage,
    // int transfer, int pro, int transactions, int purchases, int sessions) {
    // reqs[r].add(new CommandGetUserQuota(this, ad, storage, transfer, pro));
    // if (transactions)
    // reqs[r].add(new CommandGetUserTransactions(this, ad));
    // if (purchases)
    // reqs[r].add(new CommandGetUserPurchases(this, ad));
    // if (sessions)
    // reqs[r].add(new CommandGetUserSessions(this, ad));
    // }

  }

  public void run(String[] args) throws IOException {

    log.trace("run():trace");
    log.debug("run():debug");
    log.info("run():info");
    log.warn("run():warn");
    log.error("run():error");

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
          if (cmd.equals(""))
            continue;
          log.debug("cmd [{}]", cmd);

          boolean addToHistory = true;

          if (cmd.equals("login")) {
            client.login(words[1], words[2]);
          } else if (cmd.equals("quit")) {
            quit();
            return;
          } else if (cmd.equals("?") || cmd.equals("h") || cmd.equals("help")) {
            addToHistory = false;
            printHelp();
          } else if (cmd.equals("mount")) {
            log.error("mount not implemented");
          } else if (cmd.equals("ls")) {
            log.error("ls not implemented");
          } else if (cmd.equals("cd")) {
            log.error("cd not implemented");
          } else if (cmd.equals("get")) {
            log.error("get not implemented");
          } else if (cmd.equals("put")) {
            log.error("put not implemented");
          } else if (cmd.equals("mkdir")) {
            log.error("mkdir not implemented");
          } else if (cmd.equals("rm")) {
            log.error("rm not implemented");
          } else if (cmd.equals("mv")) {
            log.error("mv not implemented");
          } else if (cmd.equals("cp")) {
            log.error("cp not implemented");
          } else if (cmd.equals("pwd")) {
            log.error("pwd not implemented");
          } else if (cmd.equals("lcd")) {
            log.error("lcd not implemented");
          } else if (cmd.equals("share")) {
            log.error("share not implemented");
          } else if (cmd.equals("export")) {
            log.error("export not implemented");
          } else if (cmd.equals("import")) {
            log.error("import not implemented");
          } else if (cmd.equals("whoami")) {
            whoami();
          } else if (cmd.equals("passwd")) {
            log.error("passwd not implemented");
          } else {

            console.println("Invalid command: " + cmd);
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
