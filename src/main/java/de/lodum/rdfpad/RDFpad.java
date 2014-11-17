// Copyright 2011 Carsten Ke�ler, carsten.kessler@uni-muenster.de	
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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.lodum.rdfpad.util.AcceptHeader;
import de.lodum.rdfpad.util.HTMLModel;

public class RDFpad extends AbstractHandler {

	Logger logger;

	private AcceptHeader accept;
	private HTMLModel html;

	/**
	 * Entry point for the server, handles all incoming requests.
	 */
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		// drop any requests that are looking for static files;
		// they will be handled by the ResourceHandler in RDFpadServer:
		if (request.getRequestURI().startsWith("/files"))
			return;

		this.logger = LoggerFactory.getLogger(RDFpad.class);

		// forward an input from the form to the permanent URL for the contents
		// of the corresponding pad:
		if (request.getMethod().equals("POST")) {
			try {
				response.sendRedirect(html.getRequestBaseURL(request)
						+ (request.getParameter("pad")).split("http:/")[1]);
			} catch (Exception e) {
				html.showForm(true, e.getLocalizedMessage(), baseRequest,
						request, response);
			}

			return;
		}

		accept = new AcceptHeader(request);
		html = new HTMLModel();

		// check which pad to convert
		String pad = "http:/" + request.getRequestURI();
		if (pad.equals("http://")) {
			html.showForm(false,
					"RDFpad is a proxy for turtle contents from etherpads, "
							+ "serving it in various RDF serializations.",
					baseRequest, request, response);
		} else if (pad.endsWith("favicon.ico")) {
			response.sendError(404);
		} else if (pad.endsWith(".html") || pad.endsWith(".htm")) {
			html.getHTML(baseRequest, request, response);
		} else if ((pad.split("/"))[pad.split("/").length - 1].contains(".")) {

			getRDF(baseRequest, request, response);
		} else {
			negotiateContent(baseRequest, request, response);
		}

	}

	/**
	 * Forwards to a URI with a file extension that is comliant with the MIME
	 * type preferred by the client.
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 */
	private void negotiateContent(Request baseRequest,
			HttpServletRequest request, HttpServletResponse response) {

		String fwd = html.getRequestBaseURL(request) + request.getRequestURI();

		if (accept.getPrefMIME().equalsIgnoreCase("text/turtle"))
			response.setHeader("Location", fwd + ".ttl");
		else if (accept.getPrefMIME().equalsIgnoreCase("application/rdf+xml"))
			response.setHeader("Location", fwd + ".rdf");
		else if (accept.getPrefMIME().equalsIgnoreCase("text/plain"))
			response.setHeader("Location", fwd + ".nt");
		else if (accept.getPrefMIME().equalsIgnoreCase("text/n3"))
			response.setHeader("Location", fwd + ".n3");
		else
			response.setHeader("Location", fwd + ".html");

		response.setStatus(HttpServletResponse.SC_SEE_OTHER);
		baseRequest.setHandled(true);

	}

	/**
	 * Transforms the padContents to the request serialization using Jena.
	 * 
	 * @throws IOException
	 * @throws MalformedURLException
	 * 
	 */
	private void getRDF(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws MalformedURLException,
			IOException {
		String requestURI = request.getRequestURI();
		int lastDot = requestURI.lastIndexOf(".");
		String exportURL = html.getPadContentURL(new URL(("http:/" + requestURI
				.substring(0, lastDot))));
		response.setCharacterEncoding("UTF-8");
		PrintWriter writer = response.getWriter();

		// we try to build a Jena model and then serialize it:

		Model model = ModelFactory.createDefaultModel();
		try {
			// BufferedReader in = new BufferedReader( new InputStreamReader(new
			// URL(exportURL).openStream(),"UTF-8"));
			// model.read(exportURL, "TTL");
			model.read(exportURL, exportURL, "TTL");
		} catch (Exception e) {
			html.showForm(true, e.getLocalizedMessage(), baseRequest, request,
					response);
			return;
		}
		// PrintWriter out = new PrintWriter(new
		// OutputStreamWriter(respne.getOutputStream(), "UTF8"));

		if (requestURI.endsWith(".ttl")) {
			response.setContentType("text/turtle; charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			model.write(writer, "TURTLE");
		} else if (requestURI.endsWith(".rdf")) {
			response.setContentType("application/rdf+xml; charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			model.write(writer, "RDF/XML-ABBREV");

		} else if (requestURI.endsWith(".nt")) { //
			response.setContentType("text/plain; charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			model.write(writer, "N-TRIPLE");

		} else if (requestURI.endsWith(".n3")) {
			response.setContentType("text/n3; charset=UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			baseRequest.setHandled(true);
			model.write(writer, "N3");

		} else {
			logger.error(accept.getPrefMIME());
			response.sendError(404);
			baseRequest.setHandled(true);
		}

	}

}
