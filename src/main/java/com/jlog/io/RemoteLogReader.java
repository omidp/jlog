/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jlog.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.ChannelSftp.LsEntrySelector;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.extern.slf4j.Slf4j;

/**
 * SSH connector using JSCh library
 * 
 * JSch allows you to connect to an sshd server and use port forwarding, X11
 * forwarding, file transfer, etc., and you can integrate its functionality into
 * your own Java programs. 
 * 
 *
 */
@Slf4j
public class RemoteLogReader implements LogReader, AutoCloseable
{

    // Default logger
    private static final Logger LOG = LoggerFactory.getLogger(RemoteLogReader.class);

    // Constants
    private static final String STRICT_HOSTKEY_CHECKIN_KEY = "StrictHostKeyChecking";
    private static final String STRICT_HOSTKEY_CHECKIN_VALUE = "no";
    // private static final String CHANNEL_TYPE = "shell";
    private static final String CHANNEL_TYPE_SFTP = "sftp";
//    private static final String CHANNEL_TYPE_EXEC = "exec";

    // SSH server ip
    private String ip;
    // SSH server port
    private int port;
    // User login
    private String login;
    // User password
    private String password;
    // Connection timeout
    private int timeout;

    private Session session;
    private PrintStream ps;
    private InputStream input;
    private OutputStream ops;
    private ChannelSftp channel;
//    private ChannelExec channelExec;

    /**
     * Basic constructor
     * 
     * @param ip
     *            the ssh server IP
     * @param port
     *            the ssh server port
     * @param login
     *            , the ssh user login
     * @param password
     *            , the ssh user password
     * @param timeout
     *            , the connection timeout
     */
    public RemoteLogReader(String ip, int port, String login, String password, int timeout)
    {
        this.ip = ip;
        this.port = port;
        this.login = login;
        this.password = password;
        this.timeout = timeout;
    }
    
    

    /**
     * Open a connection
     * 
     * @throws JSchException
     *             if a error due to the ssh server connection...
     * @throws IOException
     * 
     */
    public void open() 
    {

        // Prepare session
        try
        {
            final JSch jsch = new JSch();
            session = jsch.getSession(login, ip, port);
            session.setPassword(password);
            session.setTimeout(timeout);
            session.setConfig(STRICT_HOSTKEY_CHECKIN_KEY, STRICT_HOSTKEY_CHECKIN_VALUE);

            // Start a connection
            LOG.debug("-- Try to connect to the server " + ip + ":" + port + " with user " + login);
            session.connect();
            LOG.debug("-- Connexion OK");

            LOG.debug("-- Open SSH channel");
            channel = (ChannelSftp) session.openChannel(CHANNEL_TYPE_SFTP);
            input = channel.getInputStream();

//            channelExec = (ChannelExec) session.openChannel(CHANNEL_TYPE_EXEC);
//            channelExec.connect();
            channel.connect();
            LOG.debug("-- Open SSH channel OK"); 
        }
        catch (JSchException | IOException e)
        {
            throw new LogReaderException(e);
        }
        
    }

    public void copyFiles(String srcDirectory, String remoteDirectory) throws SftpException
    {
        File srcDir = new File(srcDirectory);
        // TODO: more validation and checking
        Collection<File> listFiles = FileUtils.listFiles(srcDir, new String[] { "log" }, true);
        for (Iterator iterator = listFiles.iterator(); iterator.hasNext();)
        {
            File file = (File) iterator.next();
            LOG.info("Copying file {}", file.getName());
            channel.put(file.getAbsolutePath(), remoteDirectory + file.getName());
        }
    }

    public void readLogFiles(String remoteDirectory, JLogFileReader lr, JLogFileReadComplete rc) 
    {
        LOG.info("Reading files from {}", remoteDirectory);        
        List<String> remoteFileNames = new ArrayList<>();
        try
        {
            ls(remoteDirectory, remoteFileNames);
            for (String fullPath : remoteFileNames)
            {
                try(InputStream is = channel.get(fullPath))
                {
                    JLogFile.readLines(is, lr);
                }
                rc.afterRead(fullPath);
            }
            
        }
        catch (IOException | SftpException e)
        {
            throw new LogReaderException(e);
        }
    }
    
    
    private void ls(String remoteDirectory, List<String> remoteLogFilesPath) 
    {        
        List<String> dirs = new ArrayList<>();
        try
        {
            channel.ls(remoteDirectory, new LsEntrySelector() {
                
                @Override
                public int select(LsEntry entry)
                {
                    if(".".equals(entry.getFilename()) || "..".equals(entry.getFilename()))
                        return LsEntrySelector.CONTINUE;    
                    if(entry.getAttrs().isDir())
                    {
                        String rd = remoteDirectory;
                        if(rd.endsWith("/") == false)
                            rd += "/";
                        dirs.add(rd + entry.getFilename());
                    }
                    else
                    {
                        String filename = entry.getFilename();
                        boolean extension = FilenameUtils.isExtension(filename, "log");
                        if (extension)
                        {
                            String rd = remoteDirectory;
                            if(rd.endsWith("/") == false)
                                rd += "/";
                            remoteLogFilesPath.add(rd + filename);
//                    return LsEntrySelector.BREAK;
                        }
                    }
                    return LsEntrySelector.CONTINUE;
                    
                }
            });
            dirs.forEach(d->ls(d, remoteLogFilesPath));
        }
        catch (SftpException e)
        {
            throw new LogReaderException(e);
        }
        
    }

    public void close() throws Exception
    {
        // Close channel
        if(channel != null)
            channel.disconnect();
        // Close session
        if(session != null)
            session.disconnect();
    }



    public void setIp(String ip)
    {
        this.ip = ip;
    }



    public void setPort(int port)
    {
        this.port = port;
    }



    public void setLogin(String login)
    {
        this.login = login;
    }



    public void setPassword(String password)
    {
        this.password = password;
    }
    
    
    
    
}