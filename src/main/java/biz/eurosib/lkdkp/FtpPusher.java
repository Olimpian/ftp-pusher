package biz.eurosib.lkdkp;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Get file from FsExplorer
 * Create all necessary directories
 * Push file
 * Send to FsExplorer to delete this file from local FS
 */
public class FtpPusher {
    private FTPClient ftp;
    private String root;

    public FtpPusher(String host, String username, String password, String root) throws Exception {
        ftp = new FTPClient();
        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

        int replyCode;
        ftp.connect(host);
        replyCode = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            ftp.disconnect();
            throw new Exception("Exception in connecting to FTP Server");
        }

        ftp.login(username, password);
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();

        this.root = root;
        addRoot(root);
    }

    private void addRoot(String root) throws IOException {
        Path path = Paths.get(root);
        if(path.getParent() == null) {
            System.out.println("addRoot :: " + root);
            ftp.makeDirectory(root);
        } else {
            addRoot(path.getParent().toString());
            System.out.println("addRoot :: " + root);
            ftp.makeDirectory(root);
        }
    }

    private void addDir(String dir) throws IOException {
        Path path = Paths.get(dir);
        if(path.getParent() == null) {
            System.out.println("addDir :: " + root + dir);
            ftp.makeDirectory(root + dir);
        } else {
            addDir(path.getParent().toString());
            System.out.println("addDir :: " + root + dir);
            ftp.makeDirectory(root + dir);
        }
    }

    public boolean push(File file, String fsRoot) throws IOException {
        String hostDir = file.getParent().replaceFirst(fsRoot, "");
        addDir(hostDir);
        try(InputStream input = new FileInputStream(file)) {
            System.out.println("storeFile :: " + root + hostDir + "/" + file.getName());
            ftp.storeFile(root + hostDir + "/" + file.getName(), input);
        }
        return true;
    }


    public void disconnect() {
        if (this.ftp.isConnected()) {
            try {
                this.ftp.logout();
                this.ftp.disconnect();
            } catch (IOException ex) {
                // do nothing as file is already saved to server
            }
        }
    }
}
