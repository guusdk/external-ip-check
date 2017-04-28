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

import nl.goodbytes.network.utility.eip.spi.Resolver;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A instance that will resolve the external IP address of the host on which the application is executed.
 *
 * This implementation makes requests to public (web) services that respond with the IP address of the originating
 * entity.
 *
 * The result of a successful execution can be cached - unsuccessful results are not. When a cached result is available,
 * but expired, then the cached result will only be returned after all web services failed to generate an updated value.
 *
 * This implementation prefers webservices that produce faster responses with less failures over others.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class ResolverService
{
    final static Logger LOGGER = Logger.getLogger( ResolverService.class.getName() );

    private static ResolverService instance;
    private ServiceLoader<Resolver> loader;
    private long lastSuccess;
    private InetAddress cache;
    private Set<Resolver> failedResolvers = new HashSet<>();

    /**
     * Instantiates a new service, by loading all service providers.
     */
    private ResolverService()
    {
        loader = ServiceLoader.load( Resolver.class );
    }

    /**
     * Returns the singleton instance of this implementation, lazily creating one if needed.
     *
     * @return An instance (never null)
     */
    public static synchronized ResolverService getInstance()
    {
        if ( instance == null )
        {
            instance = new ResolverService();
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
    public InetAddress resolve()
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
    public InetAddress resolve( long duration, TimeUnit timeUnit )
    {
        final long cacheExpiry = timeUnit.toMillis( duration );
        synchronized ( this )
        {
            final boolean cacheExpired = lastSuccess + cacheExpiry < System.currentTimeMillis();
            if ( cache != null && !cacheExpired )
            {
                LOGGER.finest( "Returning from cache: " + cache );
                return cache;
            }
        }

        final List<Resolver> resolvers = getRandomizedResolvers();

        for ( final Resolver resolver : resolvers )
        {
            try
            {
                LOGGER.finest( "Resolver '" + resolver.getClass().getName() + "' is about to be invoked." );

                final InetAddress result = resolver.resolveAddress();
                if ( result != null )
                {
                    LOGGER.finest( "Resolver '" + resolver.getClass().getName() + "' successfully resolved: " + result );
                    synchronized ( this )
                    {
                        cache = result;
                        lastSuccess = System.currentTimeMillis();
                    }
                    return result;
                }
            }
            catch ( IOException | ParseException e )
            {
                LOGGER.log( Level.WARNING, "Resolver '" + resolver.getClass().getName() + "' failed.", e );
                synchronized ( this )
                {
                    failedResolvers.add( resolver );
                }
            }
        }

        return cache;
    }

    // TODO factor in response time.
    // TODO un-fail failed resolvers over time.
    private List<Resolver> getRandomizedResolvers()
    {
        final List<Resolver> resolvers = new ArrayList<>();
        for ( final Resolver resolver : loader )
        {
            resolvers.add( resolver );
        }

        // Spread the load.
        Collections.shuffle( resolvers );

        synchronized ( this )
        {
            // Prefer resolvers that have not failed yet.
            resolvers.removeAll( failedResolvers );
            resolvers.addAll( failedResolvers );
        }
        return resolvers;
    }
}
