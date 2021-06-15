package biz.eurosib.lkdkp;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Environment traverse fs-root and push files to ftp-server
 */
public class Main {
    public static void main(String... args) throws Exception {
        System.out.println("Hi, i'm ftp-pusher!");

        ///------- parsing input args -------

        Options options = new Options();

        Option fsRootOption = new Option("s", "source-root", true, "root directory of source fs");
        fsRootOption.setRequired(false);
        options.addOption(fsRootOption);

        Option ftpRoorOption = new Option("d", "destination-root", true, "root directory of destination ftp");
        ftpRoorOption.setRequired(false);
        options.addOption(ftpRoorOption);

        Option ftpHostOption = new Option("h", "host", true, "ftp-server address");
        ftpHostOption.setRequired(false);
        options.addOption(ftpHostOption);

        Option ftpUsernameOption = new Option("u", "user", true, "user login");
        ftpUsernameOption.setRequired(false);
        options.addOption(ftpUsernameOption);

        Option ftpPasswordOption = new Option("p", "password", true, "user's password");
        ftpPasswordOption.setRequired(false);
        options.addOption(ftpPasswordOption);

        Option timerOption = new Option("t", "timer", true, "repetition period in seconds");
        timerOption.setRequired(false);
        options.addOption(timerOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("ftp-pusher", options);

            System.exit(1);
            return;
        }

        String fsRoot = cmd.getOptionValue("source-root") != null ? cmd.getOptionValue("source-root") : "/Users/v-dubrovskaya/Documents/lkpkd"; //s
        String ftpRoot = cmd.getOptionValue("destination-root") != null ? cmd.getOptionValue("destination-root") : "/test/in/test"; //d
        String ftpHost = cmd.getOptionValue("host") != null ? cmd.getOptionValue("host") : "192.168.112.104"; //h
        String ftpUsername = cmd.getOptionValue("user") != null ? cmd.getOptionValue("user") : "ftp"; //u
        String ftpPassword = cmd.getOptionValue("password") != null ? cmd.getOptionValue("password") : "Ghbdtn32$"; //p
        int timer = cmd.getOptionValue("timer") != null ? Integer.parseInt(cmd.getOptionValue("timer")) : 0; //t
        boolean needTimer = timer != 0;


        /// --------- work with ftp -------

        FtpPusher pusher = new FtpPusher(ftpHost, ftpUsername, ftpPassword, ftpRoot);

        do {
            Files.walk(Paths.get(fsRoot))
                    .filter(Files::isRegularFile)
                    .forEach(f -> {
                        File file = f.toFile();
                        System.out.println("push :: " + f);

                        try {
                            pusher.push(file, fsRoot);
                            Files.delete(f);
                            System.out.println("delete ::" + f);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
            Thread.sleep(timer * 1000);

        } while (needTimer);

        System.out.println("Goodbye!");
    }

}
