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
  static File sessionFile = new File(System.getProperty("user.home"),
      ".megatest_session");
  File currentDir = new File(".").getCanonicalFile();

  public MegaTest() throws IOException {
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

    if (sessionFile.exists()) {
      try {
        client = GSONUtil.getGSON().fromJson(new FileReader(sessionFile),
            MegaClient.class);
      } catch (Exception ex) {
        log.error(ex.getMessage(), ex);
      }
    }

    threadPool = new ExecutorThreadPool();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        quit();
      }
    });

    if (client == null)
      client = new MegaClient();
    client.setAppKey(appKey);
    client.setThreadPool(threadPool);
  }

  public void printHelp() throws IOException {
    console.println("login email [password]/folderurl\n" + "logout\n"
        + "mount\n" + "ls [-R] [path]\n" + "cd [path]\n" + "get remotefile\n"
        + "put localfile [path/email]\n" + "mkdir path\n"
        + "rm path (instant completion)\n"
        + "mv path path (instant completion)\n" + "cp path path/email\n"
        + "pwd\n" + "lcd [localpath]\n" + "share [path [email [access]]]\n"
        + "export path [del]\n" + "import exportedfilelink#key\n" + "whoami\n"
        + "passwd\n" + "debug\n" + "quit");
  }

  /**
   * Stops the client, thread pool and flushes the console
   */
  public synchronized void quit() {
    if (client != null) {
      saveSession();
      client.stop();
      client = null;
    }

    if (threadPool != null) {
      threadPool.stop();
      threadPool = null;
    }

    if (console != null) {
      try {
        console.flush();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
      console = null;
    }
  }

  public void saveSession() {
    FileWriter output;
    try {
      output = new FileWriter(sessionFile);
      GSONUtil.getGSON().toJson(client, output);
      output.close();
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    }

    log.trace("session saved to {}", sessionFile);
  }

  public void cmd_login(String email, String password) throws IOException {
    client.login(email, password, new Callback<Void>() {
      @Override
      public void onResult(Void result) {
        saveSession();
      }
    });
  }

  public void cmd_logout() throws IOException {
    client.stop();
    client = null;
    client = new MegaClient();
    client.setAppKey(appKey);
    client.setThreadPool(threadPool);
    client.start();
  }

  public void cmd_lcd(String path) throws IOException {
    File newDir = null;
    if (path.startsWith("" + File.separatorChar)) {
      newDir = new File(path);
    } else {
      newDir = new File(currentDir, path);
    }
    if (newDir.exists() && newDir.isDirectory()) {
      currentDir = newDir.getCanonicalFile();
    } else {
      console.println("invalid path: " + newDir.getAbsolutePath());
    }
    console.println(currentDir.getCanonicalPath());
  }

  public void cmd_lls() throws IOException {
    for (File file : currentDir.listFiles()) {
      console.println((file.isDirectory() ? "dir:  \t" : "file: \t")
          + file.getName() + "\t" + file.length());
    }
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

    threadPool.start();
    client.start();

    String line;
    try {
      while ((line = console.readLine()) != null) {

        String words[] = line.split("\\s+");
        if (words.length == 0)
          continue;

        String cmd = words[0];

        boolean addToHistory = true;
        try {
          if (cmd.equals("login")) {
            cmd_login(words[1], words[2]);
          } else if (cmd.equals("test")) {
            cmd_test();
          } else if (cmd.equals("logout")) {
            cmd_logout();
          } else if (cmd.equals("quit")) {
            break;
          } else if (cmd.equals("?") || cmd.equals("h") || cmd.equals("help")
              || cmd.equals("")) {
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
            console.println(currentDir.getAbsolutePath());
          } else if (cmd.equals("lcd")) {
            cmd_lcd(words[1]);
          } else if (cmd.equals("lls")) {
            cmd_lls();
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
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

    } finally {
      quit();
    }
  }

  public void cmd_test() throws IOException {
    log.info("cmd_test();");
    client.getAccountDetails(true, true, false, false, false, false,
        new Callback<AccountDetails>() {
          @Override
          public void onResult(AccountDetails result) {
            try {
              console.println(GSONUtil.getGSON().toJson(result));
            } catch (IOException e) {
              log.error(e.getMessage(), e);
            }

          }
        });
  }

  public static void main(String[] args) throws IOException {
    new MegaTest().run();
  }
}
