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

package org.apache.ftpserver;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ftpserver.filesystem.NativeFileSystemManager;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.Component;
import org.apache.ftpserver.ftplet.DefaultFtpletContainer;
import org.apache.ftpserver.ftplet.FileSystemManager;
import org.apache.ftpserver.ftplet.FtpStatistics;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletContainer;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.interfaces.CommandFactory;
import org.apache.ftpserver.interfaces.FtpServerContext;
import org.apache.ftpserver.interfaces.MessageResource;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.mina.MinaListener;
import org.apache.ftpserver.message.MessageResourceImpl;
import org.apache.ftpserver.usermanager.BaseUser;
import org.apache.ftpserver.usermanager.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.PropertiesUserManager;
import org.apache.ftpserver.usermanager.TransferRatePermission;
import org.apache.ftpserver.usermanager.WritePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FTP server configuration implementation. It holds all the components used.
 */
public class DefaultFtpServerContext implements FtpServerContext {

	private final Logger LOG = LoggerFactory
			.getLogger(DefaultFtpServerContext.class);

	private MessageResource messageResource;
	private UserManager userManager;
	private FileSystemManager fileSystemManager;
	private FtpletContainer ftpletContainer;
	private FtpStatistics statistics;
	private CommandFactory commandFactory;
	private ConnectionConfig connectionConfig = new DefaultConnectionConfig();

	private Map<String, Listener> listeners = new HashMap<String, Listener>();

	private static final Authority[] ADMIN_AUTHORITIES = new Authority[] { new WritePermission() };

	private static final Authority[] ANON_AUTHORITIES = new Authority[] {
			new ConcurrentLoginPermission(20, 2),
			new TransferRatePermission(4800, 4800) };

	/**
	 * Constructor - set the root configuration.
	 */
	public DefaultFtpServerContext() throws Exception {
		this(true);
	}

	public DefaultFtpServerContext(boolean createDefaultUsers) throws Exception {

		try {
			createListeners();

			// create all the components
			messageResource = new MessageResourceImpl();
			((MessageResourceImpl) messageResource)
					.configure();

			userManager = new PropertiesUserManager();
			((PropertiesUserManager) userManager).configure();

			fileSystemManager = new NativeFileSystemManager();

			statistics = new FtpStatisticsImpl();

			commandFactory = new DefaultCommandFactory();

			// create user if necessary
			// TODO turn into a setter
			if (createDefaultUsers) {
				createDefaultUsers();
			}

			ftpletContainer = new DefaultFtpletContainer();
		} catch (Exception ex) {
			dispose();
			throw ex;
		}
	}

	private void createListeners() throws Exception {
		listeners.put("default", new MinaListener());
	}

	/**
	 * Create default users.
	 */
	private void createDefaultUsers() throws Exception {
		UserManager userManager = getUserManager();

		// create admin user
		String adminName = userManager.getAdminName();
		if (!userManager.doesExist(adminName)) {
			LOG.info("Creating user : " + adminName);
			BaseUser adminUser = new BaseUser();
			adminUser.setName(adminName);
			adminUser.setPassword(adminName);
			adminUser.setEnabled(true);

			adminUser.setAuthorities(ADMIN_AUTHORITIES);

			adminUser.setHomeDirectory("./res/home");
			adminUser.setMaxIdleTime(0);
			userManager.save(adminUser);
		}

		// create anonymous user
		if (!userManager.doesExist("anonymous")) {
			LOG.info("Creating user : anonymous");
			BaseUser anonUser = new BaseUser();
			anonUser.setName("anonymous");
			anonUser.setPassword("");

			anonUser.setAuthorities(ANON_AUTHORITIES);

			anonUser.setEnabled(true);

			anonUser.setHomeDirectory("./res/home");
			anonUser.setMaxIdleTime(300);
			userManager.save(anonUser);
		}
	}

	/**
	 * Get user manager.
	 */
	public UserManager getUserManager() {
		return userManager;
	}

	/**
	 * Get file system manager.
	 */
	public FileSystemManager getFileSystemManager() {
		return fileSystemManager;
	}

	/**
	 * Get message resource.
	 */
	public MessageResource getMessageResource() {
		return messageResource;
	}

	/**
	 * Get ftp statistics.
	 */
	public FtpStatistics getFtpStatistics() {
		return statistics;
	}

	public void setFtpStatistics(FtpStatistics statistics) {
		this.statistics = statistics;
	}

	/**
	 * Get ftplet handler.
	 */
	public FtpletContainer getFtpletContainer() {
		return ftpletContainer;
	}

	/**
	 * Get the command factory.
	 */
	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	/**
	 * Get Ftplet.
	 */
	public Ftplet getFtplet(String name) {
		return ftpletContainer.getFtplet(name);
	}

	/**
	 * Close all the components.
	 */
	public void dispose() {

		Iterator<Listener> listenerIter = listeners.values().iterator();
		while (listenerIter.hasNext()) {
			Listener listener = listenerIter.next();
			listener.stop();
		}

		if (ftpletContainer != null && ftpletContainer instanceof Component) {
			((Component) ftpletContainer).dispose();
		}

		if (userManager != null && userManager instanceof Component) {
			((Component) userManager).dispose();
		}

		if (fileSystemManager != null && fileSystemManager instanceof Component) {
			((Component) fileSystemManager).dispose();
		}

		if (statistics != null && statistics instanceof Component) {
			((Component) statistics).dispose();
		}

		if (messageResource != null && messageResource instanceof Component) {
			((Component) messageResource).dispose();
		}
	}

	public Listener getListener(String name) {
		return listeners.get(name);
	}

	public void setListener(String name, Listener listener) {
		listeners.put(name, listener);
	}

	public Listener[] getAllListeners() {
		Collection<Listener> listenerList = listeners.values();

		Listener[] listenerArray = new Listener[0];

		return listenerList.toArray(listenerArray);
	}

	public Map<String, Listener> getListeners() {
		return listeners;
	}

	public void setListeners(Map<String, Listener> listeners) {
		this.listeners = listeners;
	}

	public void addListener(String name, Listener listener) {
		listeners.put(name, listener);
	}

	public Listener removeListener(String name) {
		return listeners.remove(name);
	}

	public void setCommandFactory(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	public void setFileSystemManager(FileSystemManager fileSystemManager) {
		this.fileSystemManager = fileSystemManager;
	}

	public void setFtpletContainer(FtpletContainer ftpletContainer) {
		this.ftpletContainer = ftpletContainer;
	}

	public void setMessageResource(MessageResource messageResource) {
		this.messageResource = messageResource;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public ConnectionConfig getConnectionConfig() {
		return connectionConfig;
	}

	public void setConnectionConfig(ConnectionConfig connectionConfig) {
		this.connectionConfig = connectionConfig;
	}
}
