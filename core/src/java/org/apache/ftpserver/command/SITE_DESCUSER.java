/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */  

package org.apache.ftpserver.command;

import java.io.IOException;

import org.apache.ftpserver.FtpResponseImpl;
import org.apache.ftpserver.FtpSessionImpl;
import org.apache.ftpserver.FtpWriter;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.listener.Connection;
import org.apache.ftpserver.usermanager.TransferRateRequest;
import org.apache.ftpserver.usermanager.WriteRequest;

/**
 * This SITE command returns the specified user information.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class SITE_DESCUSER extends AbstractCommand {

    /**
     * Execute command.
     */
    public void execute(Connection connection, 
                        FtpRequest request,
                        FtpSessionImpl session, 
                        FtpWriter out) throws IOException, FtpException {
    
        // reset state variables
        session.resetState();
        
        // only administrator can execute this
        UserManager userManager = connection.getServerContext().getUserManager(); 
        boolean isAdmin = userManager.isAdmin(session.getUser().getName());
        if(!isAdmin) {
            out.send(530, "SITE", null);
            return;
        }
        
        // get the user name
        String argument = request.getArgument();
        int spIndex = argument.indexOf(' ');
        if(spIndex == -1) {
            out.send(503, "SITE.DESCUSER", null);
            return;
        }
        String userName = argument.substring(spIndex + 1);
        
        // check the user existance
        FtpServerContext serverContext = connection.getServerContext();
        UserManager usrManager = serverContext.getUserManager();
        User user = null;
        try {
            if(usrManager.doesExist(userName)) {
                user = usrManager.getUserByName(userName);
            }
        }
        catch(FtpException ex) {
            log.debug("Exception trying to get user from user manager", ex);
            user = null;
        }
        if(user == null) {
            out.send(501, "SITE.DESCUSER", userName);
            return;
        }
        
        // send the user information
        StringBuffer sb = new StringBuffer(128);
        sb.append("\n");
        sb.append("uid             : ").append(user.getName()).append("\n");
        sb.append("userpassword    : ********\n");
        sb.append("homedirectory   : ").append(user.getHomeDirectory()).append("\n");
        sb.append("writepermission : ").append(user.authorize(new WriteRequest())).append("\n");
        sb.append("enableflag      : ").append(user.getEnabled()).append("\n");
        sb.append("idletime        : ").append(user.getMaxIdleTime()).append("\n");
        
        TransferRateRequest transferRateRequest = new TransferRateRequest();
        transferRateRequest = (TransferRateRequest) session.getUser().authorize(transferRateRequest);
        
        if(transferRateRequest != null) {
            sb.append("uploadrate      : ").append(transferRateRequest.getMaxUploadRate()).append("\n");
            sb.append("downloadrate    : ").append(transferRateRequest.getMaxDownloadRate()).append("\n");
        } else {
            sb.append("uploadrate      : 0\n");
            sb.append("downloadrate    : 0\n");
        }
        sb.append('\n');
        out.write(new FtpResponseImpl(200, sb.toString()));
    }

}