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

package org.apache.ftpserver.util;

import junit.framework.TestCase;

import org.apache.ftpserver.ftplet.FtpException;

/**
*
* @author The Apache MINA Project (dev@mina.apache.org)
* @version $Rev$, $Date$
*
*/
public class EncryptUtilsTest extends TestCase {

    public void testEncryptMd5() throws FtpException {
        assertEquals("21232F297A57A5A743894A0E4A801FC3", EncryptUtils
                .encryptMD5("admin"));
    }

    public void testEncryptSha() throws FtpException {
        assertEquals("D033E22AE348AEB5660FC2140AEC35850C4DA997", EncryptUtils
                .encryptSHA("admin"));
    }
}