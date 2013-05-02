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
package de.lodum.rdfpad.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

public class AcceptHeader {

	private String preferredMIMEType;

	/**
	 * Creates an AcceptHeader object from an HttpServletRequest to enable
	 * queries for the client's preferred content type. Takes "q" values in the
	 * accept headers into account and specifies the client's preferred content
	 * type as "text/html", if no accept headers are sent. See <a
	 * href="http://www.w3.org/Protocols/HTTP/HTRQ_Headers.html#z3"
	 * >http://www.w3.org/Protocols/HTTP/HTRQ_Headers.html#z3</a> for examples.
	 * 
	 * @param request
	 */
	public AcceptHeader(HttpServletRequest request) {

		// first, try different capitalizations of "accept":
		Enumeration<String> accepts = request.getHeaders("Accept");

		if (!accepts.hasMoreElements())
			accepts = request.getHeaders("accept");

		if (!accepts.hasMoreElements())
			accepts = request.getHeaders("ACCEPT");

		// seems like no header has been sent; we'll assume text/html
		if (!accepts.hasMoreElements() || request.getHeader("accept")!=null) {
			//check if its given 
			preferredMIMEType =request.getParameter("accept");
			if(preferredMIMEType==null || preferredMIMEType==""){
				preferredMIMEType = "text/html";
			}
		} else {
			// so we got our headers; iterate and find the most preferred one:

			String currentlyPreferred = "";
			double largestQ = 0.0;

			while (accepts.hasMoreElements()) {
				String accept = accepts.nextElement();
				// split this accept header into the different elements:
				String[] entries = accept.split(",");
				for (int i = 0; i < entries.length; i++) {
					String entry = entries[i];
					String[] entryParts = entry.split(";");
					String thisType = entryParts[0];
					double thisQ = 0.0;
					// if this is the first listed MIME type, make it the
					// preferred one for now:

					for (int j = 1; j < entryParts.length; j++) {
						String thisPart = entryParts[j];
						if (thisPart.startsWith("q=")) {
							thisQ = new Double(thisPart.split("=")[1])
							.doubleValue();
							j = entryParts.length; // stop here
						}
					}

					// if q is still 0.0 (i.e., it has not been set in the
					// entry), set it to 1.0:
					if (thisQ == 0.0)
						thisQ = 1.0;

					// done disentangling stuff: 
					if(thisQ > largestQ){
						currentlyPreferred = thisType;
						largestQ = thisQ;
					}


				}

			}
			this.preferredMIMEType = currentlyPreferred;

		}


	}

	/**
	 * Returns the MIME type preferred by the client.
	 * 
	 * @return
	 */
	public String getPrefMIME() {
		return preferredMIMEType;
	}

}
