/*
 * Copyright (c) 2017 Guus der Kinderen. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.goodbytes.network.utility.eip.spi;

import nl.goodbytes.network.utility.eip.ParseException;

import java.io.IOException;
import java.net.InetAddress;

/**
 * A service provider interface for a service that uses an external entity to report back the IP address of the host
 * that is executing this application.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public interface Resolver
{
    /**
     * Returns the IP address as reported by the external entity.
     *
     * @return The IP address (never null).
     * @throws IOException    When communication with the external entity fails.
     * @throws ParseException When the response of the external entity cannot be parsed as an IP address.
     */
    InetAddress resolveAddress() throws IOException, ParseException;

    /**
     * Returns a count of successful responses from the external entity since the instance was created.
     *
     * @return An execution count, zero or positive.
     */
    long getSuccessfulExecutionCount();

    /**
     * Returns the average execution duration, in milliseconds. This value is to be framed to the most recent
     * executions, and should include only executions that were successful.
     *
     * @return an (average) duration in milliseconds. Zero when no successful executions have occurred.
     */
    long getAverageDuration();
}
