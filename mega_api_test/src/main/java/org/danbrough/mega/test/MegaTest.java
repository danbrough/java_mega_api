package org.danbrough.mega.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import jline.console.ConsoleReader;
import jline.console.history.FileHistory;

import org.danbrough.mega.AccountDetails;
import org.danbrough.mega.AccountDetails.UserSession;
import org.danbrough.mega.Callback;
import org.danbrough.mega.ExecutorThreadPool;
import org.danbrough.mega.GSONUtil;
import org.danbrough.mega.MegaClient;
import org.danbrough.mega.Node;
import org.danbrough.mega.ThreadPool;

public class MegaTest {
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
      .getLogger(MegaTest.class.getSimpleName());

  static final String MEGA_API_KEY = "MEGA_API_KEY";

  String appKey;
  ConsoleReader console;
  MegaClient client;
  ThreadPool threadPool;
  FileHistory history;
  File sessionFile = new File(System.getProperty("user.home"),
      ".megatest_session");

  public MegaTest(String args[]) throws IOException {
    super();
    console = new ConsoleReader();
    console.setPrompt(">> ");

    appKey = System.getProperty(MEGA_API_KEY);
    if (appKey == null)
      appKey = System.getenv(MEGA_API_KEY);
    if (appKey == null) {
      throw new RuntimeException(
          MEGA_API_KEY
              + " not definied as either a system property or in the environment. Visit https://mega.co.nz/#sdk to create your application key");
    }

    File historyFile = new File(System.getProperty("user.home"),
        ".megatest_history");
    log.warn("writing command history to {}", historyFile);
    history = new FileHistory(historyFile);
    console.setHistory(history);

    threadPool = new ExecutorThreadPool();

    if (sessionFile.exists()) {
      try {
        client = GSONUtil.getGSON().fromJson(new FileReader(sessionFile),
            MegaClient.class);
      } catch (Exception ex) {
        log.error("Failed to restore session: " + ex.getMessage(), ex);
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          saveSession();
        } catch (Throwable t) {
        }
      }
    });

    if (client == null)
      client = new MegaClient();
    client.setAppKey(appKey);
    client.setThreadPool(threadPool);
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

  public void saveSession() {
    log.error("test:saveSession()");
    FileWriter output;
    try {

      log.error("getting json ..");

      String json = GSONUtil.getGSON().toJson(client);
      log.error("client: {}", json);
      output = new FileWriter(sessionFile);
      output.write(json);
      output.close();
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    }

  }

  public void cmd_login(String email, String password) throws IOException {
    client.login(email, password, new Callback<Void>() {
      @Override
      public void onResult(Void result) {
        log.warn("SCSN: {}", client.getScsn());
        saveSession();
      }
    });
  }

  public void cmd_quit() {
    log.info("cmd_quit()");
    client.stop();
    threadPool.stop();
    System.exit(0);
  }

  public void cmd_whoami() {
    log.info("cmd_whoami();");

    client.getAccountDetails(true, true, true, true, true, true,
        new Callback<AccountDetails>() {
          @Override
          public void onResult(AccountDetails result) {
            for (UserSession session : result.getSessions()) {
              try {
                console.println(session.toString());
              } catch (IOException e) {
                log.error(e.getMessage(), e);
              }
            }
          }
        });
  }

  public void cmd_ls(String args[]) throws IOException {
    boolean recursive = false;
    String path = null;

    if (args.length == 3) {
      if (args[1].equals("-R")) {
        recursive = true;
        path = args[2];
      } else {
        console.println("usage ls (-R) path");
        return;
      }
    } else if (args.length == 2) {
      path = args[1];
    } else {
      console.println("usage ls (-R) path");
      return;
    }

    log.debug("path:" + path + " recursive: " + recursive);

    client.setCurrentFolder(client.getRootNode());
    for (Node node : client.getChildren()) {
      log.debug("child: {}", node);
    }
  }

  public void run() throws IOException {

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
            cmd_login(words[1], words[2]);
          } else if (cmd.equals("quit")) {
            cmd_quit();
            return;
          } else if (cmd.equals("?") || cmd.equals("h") || cmd.equals("help")) {
            addToHistory = false;
            printHelp();
          } else if (cmd.equals("mount")) {
            log.error("mount not implemented");
          } else if (cmd.equals("ls")) {
            cmd_ls(words);
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
            cmd_whoami();
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
    new MegaTest(args).run();
  }
}
