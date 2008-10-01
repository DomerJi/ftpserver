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

package org.apache.ftpserver.commands.impl.listing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.ftpserver.command.impl.listing.NLSTFileFormater;
import org.apache.ftpserver.ftplet.FileObject;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class NLSTFileFormaterTest extends TestCase {

    private static final FileObject TEST_FILE = new MockFileObject();

    public NLSTFileFormater formater = new NLSTFileFormater();

    public static class MockFileObject implements FileObject {
        public InputStream createInputStream(long offset) throws IOException {
            return null;
        }

        public OutputStream createOutputStream(long offset) throws IOException {
            return null;
        }

        public boolean delete() {
            return false;
        }

        public boolean doesExist() {
            return false;
        }

        public String getFullName() {
            return "fullname";
        }

        public String getGroupName() {
            return "group";
        }

        public long getLastModified() {
            return 1;
        }

        public int getLinkCount() {
            return 1;
        }

        public String getOwnerName() {
            return "owner";
        }

        public String getShortName() {
            return "short";
        }

        public long getSize() {
            return 13;
        }

        public boolean hasDeletePermission() {
            return false;
        }

        public boolean hasReadPermission() {
            return true;
        }

        public boolean hasWritePermission() {
            return false;
        }

        public boolean isDirectory() {
            return false;
        }

        public boolean isFile() {
            return true;
        }

        public boolean isHidden() {
            return false;
        }

        public FileObject[] listFiles() {
            return null;
        }

        public boolean mkdir() {
            return false;
        }

        public boolean move(FileObject destination) {
            return false;
        }
    }

    public void testSingleFile() {
        assertEquals("short\r\n", formater.format(TEST_FILE));
    }

    public void testSingleDir() {
        FileObject dir = new MockFileObject() {
            public boolean isDirectory() {
                return true;
            }

            public boolean isFile() {
                return false;
            }

        };

        assertEquals("short\r\n", formater.format(dir));
    }

}