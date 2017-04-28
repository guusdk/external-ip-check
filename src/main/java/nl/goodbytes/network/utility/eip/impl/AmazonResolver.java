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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * A Service Provider that utilizes the web service as provided at http://icanhazip.com
 *
 * @author Guus der Kinderen, guus@goodbytes.nl
 */
public class AmazonResolver extends URLResolver
{
    @Override
    public URL getServiceAddress()
    {
        try
        {
            return new URL( "http://checkip.amazonaws.com" );
        }
        catch ( MalformedURLException e )
        {
            throw new Error( "This implementation is broken. It fails to parse a value that is hardcoded.", e );
        }
    }

    @Override
    public InetAddress parse( String content ) throws ParseException
    {
        try
        {
            // Amazon's service returns nothing more than a text-based IP address.
            return InetAddress.getByName( content );
        }
        catch ( UnknownHostException e )
        {
            throw new ParseException( "Unable to parse content.", e );
        }
    }
}
