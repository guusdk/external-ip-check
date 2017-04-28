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

package nl.goodbytes.network.utility.eip.impl;

import nl.goodbytes.network.utility.eip.ParseException;
import nl.goodbytes.network.utility.eip.spi.Resolver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * An abstract service provider that uses a webservice referenced to by a URL.
 *
 * This implementation takes responsibility for making the request to the webservice and does bookkeeping to track
 * average duration and number of requests. Subclasses are responsible for parsing the webservice response.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public abstract class URLResolver implements Resolver
{
    private long successfulExecutions = 0;
    private Queue<Long> mostRecentExecutionDurations = new ArrayDeque<>( 10 );

    /**
     * The URL of the web service that is to be invoked.
     *
     * @return A web service address. Cannot be null.
     */
    abstract URL getServiceAddress();

    /**
     * Parses a webservice response into an IP address.
     *
     * @param content The webservice response (can be null).
     * @return An IP address (cannot be null).
     * @throws ParseException When the provided content cannot be parsed.
     */
    abstract InetAddress parse( String content ) throws ParseException;

    @Override
    public InetAddress resolveAddress() throws IOException, ParseException
    {
        final long start = System.currentTimeMillis();

        String content;
        try ( final InputStreamReader reader = new InputStreamReader( getServiceAddress().openStream() ) )
        {
            char[] buffer = new char[ 39 ]; // IPv6 hex representation length.
            final StringBuilder sb = new StringBuilder( buffer.length );

            int length;
            while ( ( length = reader.read( buffer ) ) != -1 )
            {
                sb.append( buffer, 0, length );
            }

            content = sb.toString();
        }

        final InetAddress result = parse( content );

        final long duration = System.currentTimeMillis() - start;

        synchronized ( this )
        {
            mostRecentExecutionDurations.add( duration );
            while ( mostRecentExecutionDurations.size() > 10 )
            {
                mostRecentExecutionDurations.remove();
            }
            successfulExecutions++;
        }

        return result;
    }

    public synchronized long getSuccessfulExecutionCount()
    {
        return successfulExecutions;
    }

    public synchronized long getAverageDuration()
    {
        long total = 0;
        for ( final Long mostRecentExecutionDuration : mostRecentExecutionDurations )
        {
            total += mostRecentExecutionDuration;
        }
        return total;
    }
}
