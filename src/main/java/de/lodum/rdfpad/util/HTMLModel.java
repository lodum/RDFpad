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
package de.lodum.rdfpad.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class HTMLModel {

	private static String FOOTER = "<hr /><p align=\"right\"><small><a href=\"http://ifgi.uni-muenster.de/~kessler/rdfpad/\">RDFpad is free and open source software</a>. Contact: <a property=\"dc:creator\" "
			+ "href=\"http://ifgi.uni-muenster.de/~kessler/\">Carsten Ke�ler</small></p>\n"
			+ "</div>\n</div>\n</body>\n</html>";

	/**
	 * Shows the input form for the etherpad URL.
	 * 
	 * @param error
	 *            if set to true, the msg will be shown in red.
	 * @param msg
	 * @param baseRequest
	 * @param response
	 * @throws IOException
	 */
	public void showForm(boolean error, String msg, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		PrintWriter writer = response.getWriter();
		writer.println(getHeader(request));

		String padPath = (request.getRequestURI()).split(".htm")[0];

		if (error)
			writer.println("<p class=\"error\">Error while processing <a property=\"rdf:about\" href=\""
					+ "http:/"
					+ padPath
					+ "\">http:/"
					+ padPath
					+ "</a>:<br />" + msg + "</p>");
		else
			writer.println("<p>" + msg + "</p>");

		String padURL = "http:/" + (request.getRequestURI()).split(".htm")[0];
		if (padURL.equals("http://")) {
			padURL = "http://typewith.me/p/lodum";
			writer.println("<p>Try this one:</p>");
		}

		writer.println("<form action=\"http://"

				+ request.getServerName()
				+ ":"
				+ request.getServerPort()
				+ "\" method=\"POST\"><input type=\"text\" size=\"60\" name=\"pad\" value=\""
				+ padURL + "\" />"
				+ "<input type=\"submit\" value=\"Get RDF\" /></form>");

		writer.println(FOOTER);
	}

	/**
	 * Returns the base URL of the server RDFpad is running on.
	 * 
	 * @param request
	 * @return
	 */
	public String getRequestBaseURL(HttpServletRequest request) {
		// request.setAttribute(arg0, arg1)
		return ("http://" + request.getServerName() + ":" + request
				.getServerPort());
	}

	/**
	 * Generates an HTML view for the requested pad. Will forward to the
	 * showForm method if anything goes wrong (invalid etherpad URL, parsing
	 * error in the RDF code, ...).
	 * 
	 * @param baseRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void getHTML(Request baseRequest, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		PrintWriter writer = response.getWriter();
		String exportURL = getPadContentURL(new URL(
				("http:/" + request.getRequestURI()).split(".htm")[0]));

		Model model = ModelFactory.createDefaultModel();

		try {
			model.read(exportURL, "TTL");
		} catch (Exception e) {
			showForm(true, e.getLocalizedMessage(), baseRequest, request,
					response);
			return;
		}

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);

		String padPath = (request.getRequestURI()).split(".htm")[0];

		writer.println(getHeader(request)
				+ "\n<p>based on <a property=\"rdf:about\" href=\"" + "http:/"
				+ padPath + "\">http:/" + padPath + "</a></p>");

		// list all name spaces:
		writer.println("<hr /><h2>Namespaces</h2>");
		Map<String, String> ns = model.getNsPrefixMap();
		Iterator<String> keys = ns.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			writer.println("<a href=\"" + model.getNsPrefixURI(key) + "\">"
					+ key + "</a> ");
		}

		writer.println("</p><hr /><h2>Triples</h2>\n<table>");

		// get all resources that appear as subjects:
		for (ResIterator rit = model.listSubjects(); rit.hasNext();) {
			Resource subject = rit.next();

			boolean printedSubject = false;

			for (StmtIterator it = model.listStatements(subject,
					(Property) null, (RDFNode) null); it.hasNext();) {
				Statement stm = it.nextStatement();

				// print each subject only once:
				if (!printedSubject) {
					writer.println("<tr><td>" + nodeToHTML(subject) + "</td>");
					printedSubject = true;
				} else {
					// empty table cell:
					writer.println("<tr><td></td>");
				}

				writer.println("<td>" + nodeToHTML(stm.getPredicate())
						+ "</td>");

				writer.println("<td>" + nodeToHTML(stm.getObject())
						+ "</td></tr>");

			}
		}

		// model.write(response.getWriter(), "N-TRIPLE");
		writer.println("</table>");
		writer.println("<hr /><p>View as <a href=\""
				+ getRequestBaseURL(request) + padPath
				+ ".rdf\">RDF/XML</a> | <a href=\""
				+ getRequestBaseURL(request) + padPath
				+ ".nt\">N-Triples</a> | <a href=\""
				+ getRequestBaseURL(request) + padPath
				+ ".ttl\">Turtle</a> | <a href=\"" + getRequestBaseURL(request)
				+ padPath + ".n3\">N3</a> &ndash; "
				+ "View in <a href=\"http://inspector.sindice.com/inspect?url="
				+ exportURL + "\">Sindice Inspector</a>&ndash;"
				+ " Query with <a href=\"http://query.lodum.de?queryFrom="
				+ getRequestBaseURL(request) + padPath
				+ ".rdf\">SPARQLfly</a></p>"); // TODO
		writer.println(FOOTER);
	}

	/**
	 * Generates HTML code for an RDFNode; if the node is a URIResource, it will
	 * be rendered as a link; otherwise, as simple text.
	 * 
	 * @param node
	 * @return
	 */
	private String nodeToHTML(RDFNode node) {
		if (node.isURIResource()) {
			Resource r = node.asResource();
			String ns = r.getModel().getNsURIPrefix(r.getNameSpace());
			if (ns == null) {
				return ("<a href=\"" + node.toString() + "\">"
						+ node.toString() + "</a>");
			} else {
				return ("<a href=\"" + node.toString() + "\">" + ns + ":"
						+ r.getLocalName() + "</a>");
			}
		} else {
			return (node.toString());
		}
	}

	/**
	 * Retrieves the URL for plain text export from the given etherpad URL for
	 * the current state of the pad.
	 * 
	 * @param padURL
	 * @return
	 * @throws IOException
	 */
	public String getPadContentURL(URL padURL) throws IOException {

		String path = padURL.getPath();

		// The name of the pad is the last chunk of the path:
		String[] pathSegments = path.split("/");
		String padname = pathSegments[pathSegments.length - 1];

		// construct the export URI; looks like
		// http://ifgipedia.uni-muenster.de/ep/pad/export/foaf-carsten/latest?format=txt
		// String exportURL = padURL.getProtocol() + "://" + padURL.getHost()
		// + "/p/" + padname + "/export/txt";
		String exportURL = padURL.getProtocol() + "://" + padURL.getHost()
				+ "/ep/pad/export/" + padname + "/latest?format=txt";
		URL url = new URL(exportURL);
		URLConnection con = url.openConnection();

		con.connect();
		if (con.getContentLength() == -1) {
			// System.out.println("geht nicht");
			exportURL = padURL.getProtocol() + "://" + padURL.getHost() + "/p/"
					+ padname + "/export/txt";
		}

		return exportURL;
	}

	/**
	 * Provides the header for the HTML pages.
	 * 
	 * @param request
	 * @return
	 */
	private String getHeader(HttpServletRequest request) {
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
				+ "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n"
				+ "<html xml:lang=\"en\"\n"
				+ "      xmlns=\"http://www.w3.org/1999/xhtml\"\n"
				+ "      xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
				+ "      xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n"
				+ "      xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
				+ "      xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
				+ "      xmlns:dcterms=\"http://purl.org/dc/terms/\"\n"
				+ "      xmlns:dct=\"http://purl.org/dc/terms/\"\n"
				+ "      xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n"
				+ "      xmlns:wot=\"http://xmlns.com/wot/0.1/\"\n"
				+ "      xmlns:prv=\"http://purl.org/net/provenance/ns#\"\n"
				+ "      xmlns:opmv=\"http://purl.org/net/opmv/ns#\"\n"
				+ "      xmlns:tisc=\"http://observedchange.com/tisc/ns#\"\n"
				+ ">\n"
				+ "<head>\n"
				+ " <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
				+ "  <link rel=\"alternate\" type=\"application/rdf+xml\" href=\"tisc-20110426.owl\" />\n"
				+ "  <link rel=\"stylesheet\" type=\"text/css\" href=\""
				+ getRequestBaseURL(request)
				+ "/files/style.css\" />\n"
				+ "  <title>RDFpad</title>\n"
				+ "</head>\n"
				+ "<body>"
				+ "  <a href=\"http://lodum.de\"><img src=\""
				+ getRequestBaseURL(request)
				+ "/files/lodum.png\" align = \"right\"/></a>"
				+ "<h1 about=\"\" property=\"dcterms:title\" xml:lang=\"en\"><a href=\""
				+ getRequestBaseURL(request) + "\">RDFpad</a></h1>";

		return header;
	}
}
