// Copyright 2011 Carsten Ke§ler, carsten.kessler@uni-muenster.de	
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This basically means: do with the code whatever your want.

package de.lodum.rdfpad;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

public class RDFpadServer {
	/**
	 * Starts the engines...
	 * 
	 * @param args
	 *            - the port number to start the server on
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(new Integer(args[0]));
		
		
		//connector.setPort(new Integer(args[0]));
        server.addConnector(connector);
 
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "" });
 
        resource_handler.setResourceBase(".");
 
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { new RDFpad(), resource_handler });
        server.setHandler(handlers);
 
        server.start();
        server.join();

	}
}
