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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * A utility class that is intended to be used as the MainClass in a Java ARchive.
 *
 * This class will write to the console, on the "standard" output stream, on the "standard" error output stream, or
 * both.
 *
 * The only information written to the "standard" output stream will be an IP address. All other information will be
 * written to "standard" error output stream.
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class Starter
{
    public static final void main( String[] args )
    {
        final Starter starter = new Starter();
        starter.parseArguments( args );
        starter.doExecution();
    }

    /**
     * Starts the resolving process, printing the result to the "standard" output stream, or any error to the
     * "standard" error output stream.
     */
    private void doExecution()
    {
        final InetAddress result = ResolverService.getInstance().resolve();
        if ( result == null )
        {
            System.err.println( "Unable to resolve public IP address." );
            System.exit( 1 );
        }

        System.out.println( result.getHostAddress() );
    }

    /**
     * Processes any arguments that were provided when invoking the JAR file.
     *
     * @param args Any arguments. Can be null, can be empty.
     */
    private void parseArguments( String[] args )
    {
        if ( args == null )
        {
            return;
        }

        for ( int i = 0; i < args.length; i++ )
        {
            if ( "verbose".equalsIgnoreCase( args[ i ] ) )
            {
                makeVerbose();
            }
        }
    }

    /**
     * Once invoked, will cause verbose messages describing the execution application be outputted to the "standard"
     * error output stream.
     */
    private void makeVerbose()
    {
        final ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel( Level.ALL );
        ResolverService.LOGGER.addHandler( handler );
        ResolverService.LOGGER.setLevel( Level.ALL );
    }
}
