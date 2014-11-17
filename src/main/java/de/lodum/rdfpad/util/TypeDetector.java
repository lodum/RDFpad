package de.lodum.rdfpad.util;

/*
 * Copyright 2008-2010 Digital Enterprise Research Institute (DERI)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

/**
 * Implementation of {@link org.deri.any23.mime.MIMETypeDetector} based on <a
 * href="http://lucene.apache.org/tika/">Apache Tika</a>.
 *
 * @author Michele Mostarda (michele.mostarda@gmail.com)
 * @author Davide Palmisano (dpalmisano@gmail.com)
 */
public class TypeDetector {

	// TODO: centralize mimetype strings somewhere.

	public static final String N3 = "N3";

	public static final String NQUADS = "NQUAD";

	public static final String TURTLE = "TURTLE";

	public static final String TRIX = "TRIGX";

	/**
	 * N3 patterns.
	 */
	private static final Pattern[] N3_PATTERNS = {
			Pattern.compile("^\\S+\\s*<\\S+>\\s*<\\S+>\\s*\\."), // * URI URI .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*_:\\S+\\s*\\."), // * URI BNODE
																	// .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*\".*\"(@\\S+)?\\s*\\."), // *
																			// URI
																			// LLITERAL
																			// .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*\".*\"(\\^\\^\\S+)?\\s*\\.") // *
																				// URI
																				// TLITERAL
																				// .
	};

	/**
	 * N-Quads patterns.
	 */
	private static final Pattern[] NQUADS_PATTERNS = {
			Pattern.compile("^\\S+\\s*<\\S+>\\s*<\\S+>\\s*\\<\\S+>\\s*\\."), // *
																				// URI
																				// URI
																				// URI
																				// .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*_:\\S+\\s*\\<\\S+>\\s*\\."), // *
																				// URI
																				// BNODE
																				// URI
																				// .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*\".*\"(@\\S+)?\\s*\\<\\S+>\\s*\\."), // *
																						// URI
																						// LLITERAL
																						// URI
																						// .
			Pattern.compile("^\\S+\\s*<\\S+>\\s*\".*\"(\\^\\^\\S+)?\\s*\\<\\S+>\\s*\\.") // *
																							// URI
																							// TLITERAL
																							// URI
																							// .
	};

	/**
	 * TRIX patterns.
	 */
	private static final Pattern[] TRIX_PATTERNS = {

	Pattern.compile(".*<\\S+>\\s{0,}\\{"),
			Pattern.compile(".*_?:\\S+\\s{0,}\\{")

	};

	/**
	 * Checks if the stream contains the <i>N3</i> triple patterns.
	 *
	 * @param is
	 *            input stream to be verified.
	 * @return <code>true</code> if <i>N3</i> patterns are detected,
	 *         <code>false</code> otherwise.
	 * @throws IOException
	 */
	public static boolean checkN3Format(InputStream is) throws IOException {
		return findPattern(N3_PATTERNS, '.', is);
	}

	/**
	 * Checks if the stream contains the <i>NQuads</i> patterns.
	 *
	 * @param is
	 *            input stream to be verified.
	 * @return <code>true</code> if <i>N3</i> patterns are detected,
	 *         <code>false</code> otherwise.
	 * @throws IOException
	 */
	public static boolean checkNQuadsFormat(InputStream is) throws IOException {
		return findPattern(NQUADS_PATTERNS, '.', is);
	}

	public static boolean checkTrixFormat(InputStream is) throws IOException {
		return findPattern(TRIX_PATTERNS, '{', is);
	}

	public static void main(String[] args) {

		System.out.println(new TypeDetector().guessType(new File(
				"test_data/test.trix")));
		System.out.println(new TypeDetector().guessType(new File(
				"test_data/test.n3")));

	}

	/**
	 * Tries to apply one of the given patterns on a sample of the input stream.
	 *
	 * @param patterns
	 *            the patterns to apply.
	 * @param delimiterChar
	 *            the delimiter of the sample.
	 * @param is
	 *            the input stream to sample.
	 * @return <code>true</code> if a pattern has been applied,
	 *         <code>false</code> otherwise.
	 * @throws IOException
	 */
	private static boolean findPattern(Pattern[] patterns, char delimiterChar,
			InputStream is) throws IOException {
		String sample = extractDataSample(is, delimiterChar);
		// sample= sample.replaceAll("\\s+", " ");;
		for (Pattern pattern : patterns) {
			if (pattern.matcher(sample).find()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Extracts a sample data from the input stream, from the current mark to
	 * the first <i>breakChar</i> char.
	 *
	 * @param is
	 *            the input stream to sample.
	 * @param breakChar
	 *            the char to break to sample.
	 * @return the sample string.
	 * @throws IOException
	 *             if an error occurs during sampling.
	 */
	private static String extractDataSample(InputStream is, char breakChar)
			throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		final int MAX_SIZE = 1024 * 2;
		int c;
		boolean insideBlock = false;
		int read = 0;
		br.mark(MAX_SIZE);
		try {
			while ((c = br.read()) != -1) {
				read++;
				if (read > MAX_SIZE) {
					break;
				}
				if ('<' == c) {
					insideBlock = true;
				} else if ('>' == c) {
					insideBlock = false;
				} else if ('"' == c) {
					insideBlock = !insideBlock;
				}
				sb.append((char) c);
				if (!insideBlock && breakChar == c) {
					break;
				}
			}
		} finally {
			is.reset();
			br.reset();
		}
		return sb.toString();
	}

	/**
	 * Estimates the <code>MIME</code> type of the content of input file. The
	 * <i>input</i> stream must be resettable.
	 *
	 * @param fileName
	 *            name of the data source.
	 * @param input
	 *            <code>null</code> or a <b>resettable</i> input stream
	 *            containing data.
	 * @param mimeTypeFromMetadata
	 *            mimetype declared in metadata.
	 * @return the supposed mime type or <code>null</code> if nothing
	 *         appropriate found.
	 * @throws IllegalArgumentException
	 *             if <i>input</i> is not <code>null</code> and is not
	 *             resettable.
	 */
	public String guessType(File file) {

		InputStream input = null;
		try {
			input = getInputStream(file);
			purify(input);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String format = TURTLE;
		try {
			if (checkTrixFormat(input)) {
				format = TRIX;
			} else if (checkN3Format(input)) {
				format = N3;
			} else if (checkNQuadsFormat(input)) {
				format = NQUADS;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return format;
	}

	private static InputStream getInputStream(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		while (fis.read(buffer) != -1) {
			bos.write(buffer);
		}
		fis.close();
		InputStream bais;
		bais = new ByteArrayInputStream(bos.toByteArray());
		return bais;
	}

	public void purify(InputStream inputStream) throws IOException {
		if (!inputStream.markSupported())
			throw new IllegalArgumentException(
					"Provided InputStream does not support marks");

		// mark the current position
		inputStream.mark(Integer.MAX_VALUE);
		int byteRead = inputStream.read();
		char charRead = (char) byteRead;
		while (isBlank(charRead) && (byteRead != -1)) {
			// if here means that the previos character must be removed, so
			// mark.
			inputStream.mark(Integer.MAX_VALUE);
			byteRead = inputStream.read();
			charRead = (char) byteRead;
		}
		// if exit go back to the last valid mark.
		inputStream.reset();
	}

	private boolean isBlank(char c) {
		return c == '\t' || c == '\n' || c == ' ' || c == '\r' || c == '\b'
				|| c == '\f';
	}

}
