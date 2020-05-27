package org.qualitified.crm.operation;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import java.io.*;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.*;

@Operation(id=UploadFile.ID ,category= Constants.CAT_DOCUMENT, label="UploadFile", description="Upload file on FTP server")

public class UploadFile {
    public static final String ID = "Qualitified.UploadFile";

    @Param(name = "server")
    protected  String server;
    @Param(name = "port")
    protected  int port ;
    @Param(name = "user")
    protected  String user;
    @Param(name="privateKeyPath",required = false)
    protected String privateKeyPath;
    @Param(name = "password")
    protected  String password;
    @Param(name="connexionType")
    protected String connexionType;
    @Param(name="depositDirectory")
    protected String depositDirectory;
    @OperationMethod()
    public Blob run(Blob blob) throws IOException, OperationException,JSchException {
        if(connexionType=="sftp") {
            Session session =null;

            try {
                JSch jsch = new JSch();
                jsch.addIdentity(privateKeyPath,password);
                //session = jsch.getSession(server);
                session = jsch.getSession(user, server, port);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                System.out.println("Session connected "+session.isConnected());

                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;
                File file = File.createTempFile("FVAMOT", ".csv");
                InputStream inputStream = blob.getStream();
                String remoteFile = depositDirectory+blob.getFilename();
                sftpChannel.put(inputStream, remoteFile);
                sftpChannel.exit();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(connexionType=="ftp")
        {
            FTPClient ftpClient = new FTPClient();

            try {

                ftpClient.connect(server, port);
                ftpClient.login(user, password);
                ftpClient.enterLocalPassiveMode();

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                System.out.println("cnx done");
                File file = File.createTempFile("export", ".csv");
                InputStream inputStream = blob.getStream();
                String remoteFile = depositDirectory+blob.getFilename();

                System.out.println("Start uploading  file");
                OutputStream outputStream = ftpClient.storeFileStream(remoteFile);
                byte[] bytesIn = new byte[4096];
                int read = 0;

                while ((read = inputStream.read(bytesIn)) != -1) {
                    outputStream.write(bytesIn, 0, read);
                }
                inputStream.close();
                outputStream.close();

                boolean completed = ftpClient.completePendingCommand();
                if (completed) {
                    System.out.println("The file is uploaded successfully.");
                }

            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                try {
                    if (ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return blob;
    }

}