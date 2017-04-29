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

package nl.goodbytes.network.utility.eip;

import java.net.InetAddress;
import java.util.concurrent.*;

/**
 * A instance that will resolve the external IP address of the host on which the application is executed.
 *
 * This implementation makes requests to public (web) services that respond with the IP address of the originating
 * entity.
 *
 * The result of a successful execution can be cached - unsuccessful results are not. When a cached result is available,
 * but expired, then the cached result will only be returned after all web services failed to generate an updated value.
 *
 * This implementation prefers web services that produce faster responses with less failures over others.
 *
 * This is a asynchronous implementation: the thread that invokes the various methods used to resolve the external IP
 * address will not block. Instead, a {@link Future} instance is returned, that can be used to obtain the result later.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class NonBlockingResolverService
{
    private final static ExecutorService executor;

    static
    {
        // Ensure that the executor will not cause a JVM to remain running, by making all of its threads daemon.
        executor = Executors.newCachedThreadPool( new ThreadFactory()
        {
            public Thread newThread( Runnable runnable )
            {
                final Thread thread = Executors.defaultThreadFactory().newThread( runnable );
                thread.setDaemon( true );
                return thread;
            }
        } );
    }

    private static NonBlockingResolverService instance;

    /**
     * Returns the singleton instance of this implementation, lazily creating one if needed.
     *
     * @return An instance (never null)
     */
    public static synchronized NonBlockingResolverService getInstance()
    {
        if ( instance == null )
        {
            instance = new NonBlockingResolverService();
        }
        return instance;
    }

    /**
     * Returns the IP address of the host on which this application is executed, as resolved by one of the service
     * providers, or null if none of the providers were able to resolve the IP address.
     *
     * Only when a cached response is older than 24 hours is a new execution preferred over the cached response.
     *
     * @return A resolved IP address, or null when all of the service providers failed.
     */
    public Future<InetAddress> resolve()
    {
        return resolve( 1, TimeUnit.DAYS );
    }

    /**
     * Returns the IP address of the host on which this application is executed, as resolved by one of the service
     * providers, or null if none of the providers were able to resolve the IP address.
     *
     * Only when a cached response is older than the duration specified in the arguments, then a new execution preferred
     * over the cached response.
     *
     * @param duration The maximum preferred age of a cached entry. Can be zero or negative for forced cache refresh.
     * @param timeUnit The unit in which duration is expressed (cannot be null).
     * @return A resolved IP address, or null when all of the service providers failed.
     */
    public Future<InetAddress> resolve( final long duration, final TimeUnit timeUnit )
    {
        final Callable<InetAddress> callable = new Callable<InetAddress>()
        {
            @Override
            public InetAddress call() throws Exception
            {
                return ResolverService.getInstance().resolve( duration, timeUnit );
            }
        };

        return executor.submit( callable );
    }
}
