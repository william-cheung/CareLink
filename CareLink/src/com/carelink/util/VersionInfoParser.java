package com.carelink.util;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.carelink.model.VersionInfo;
import com.google.gson.Gson;

public class VersionInfoParser {
	private static String elemName = "";

	public static VersionInfo getVersionInfo(InputStream inputStream) {
		try {
			if (inputStream == null || inputStream.available() == 0) {
				return null;
			}
			final VersionInfo versionInfo = new VersionInfo();
			Xml.parse(inputStream, Encoding.UTF_8, new ContentHandler() {
				public void startPrefixMapping(String prefix, String uri)
						throws SAXException {
				}
				public void startElement(String uri, String localName, String qName,
						Attributes atts) throws SAXException {
					Log.d("VersionInfoParser", localName + " " + qName + " " );
					elemName = qName;
				}
				public void startDocument() throws SAXException {
				}
				public void skippedEntity(String name) throws SAXException {
				}
				public void setDocumentLocator(Locator locator) {
				}
				public void processingInstruction(String target, String data)
						throws SAXException {
				}
				public void ignorableWhitespace(char[] ch, int start, int length)
						throws SAXException {
				}
				public void endPrefixMapping(String prefix) throws SAXException {
				}
				public void endElement(String uri, String localName, String qName)
						throws SAXException {
					elemName = "";
				}
				public void endDocument() throws SAXException {
				}
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					if (!elemName.equals("")) {
						if (elemName.equals("version_name")) {
							versionInfo.setVersionName(new String(ch, start, length));
						} else if (elemName.equals("description")) {
							versionInfo.setDescription(new String(ch, start, length));
						} else if (elemName.equals("apk_url")) {
							versionInfo.setApkUrl(new String(ch, start, length));
						}
					}
				}
			});

			Log.d("VersionInfoParser", new Gson().toJson(versionInfo));
			Log.d("VersionInfoParser", "" + versionInfo.getVersionName());
			return versionInfo;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		return null;
	}
}
