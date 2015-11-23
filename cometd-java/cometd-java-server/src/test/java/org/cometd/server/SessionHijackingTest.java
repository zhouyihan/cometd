/*
 * Copyright (c) 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cometd.server;

import java.net.HttpCookie;

import org.cometd.bayeux.Message;
import org.cometd.common.JSONContext;
import org.cometd.common.JettyJSONContextClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.Assert;
import org.junit.Test;

public class SessionHijackingTest extends AbstractBayeuxClientServerTest
{
    public SessionHijackingTest(String serverTransport)
    {
        super(serverTransport);
    }

    @Test
    public void testSessionHijacking() throws Exception
    {
        startServer(null);

        String cookieName = "BAYEUX_BROWSER";

        Request handshake1 = newBayeuxRequest("[{" +
                "\"channel\": \"/meta/handshake\"," +
                "\"version\": \"1.0\"," +
                "\"supportedConnectionTypes\": [\"long-polling\"]" +
                "}]");
        ContentResponse response1 = handshake1.send();
        Assert.assertEquals(200, response1.getStatus());

        String clientId1 = extractClientId(response1);
        String cookie1 = extractCookie(cookieName);
        // Reset cookies to control what cookies this test sends.
        httpClient.getCookieStore().removeAll();

        Request handshake2 = newBayeuxRequest("[{" +
                "\"channel\": \"/meta/handshake\"," +
                "\"version\": \"1.0\"," +
                "\"supportedConnectionTypes\": [\"long-polling\"]" +
                "}]");
        ContentResponse response2 = handshake2.send();
        Assert.assertEquals(200, response2.getStatus());

        String clientId2 = extractClientId(response2);
        String cookie2 = extractCookie(cookieName);
        // Reset cookies to control what cookies this test sends.
        httpClient.getCookieStore().removeAll();

        // Client1 tries to impersonate Client2.
        Request publish1 = newBayeuxRequest("[{" +
                "\"channel\": \"/session_mismatch\"," +
                "\"data\": \"publish_data\"," +
                "\"clientId\": \"" + clientId2 + "\"" +
                "}]");
        publish1.cookie(new HttpCookie(cookieName, cookie1));
        response1 = publish1.send();
        Assert.assertEquals(200, response1.getStatus());

        // Message should fail.
        JSONContext.Client parser = new JettyJSONContextClient();
        Message.Mutable[] messages = parser.parse(response1.getContentAsString());
        Assert.assertEquals(1, messages.length);
        Message message = messages[0];
        Assert.assertFalse(message.isSuccessful());
        Assert.assertTrue(((String)message.get(Message.ERROR_FIELD)).startsWith("402"));
    }
}