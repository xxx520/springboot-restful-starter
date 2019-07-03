package com.neko.seed.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MultipartFormParser {
	private byte[] boundary;
	private byte[] newlineBoundary;
	
	private static final byte[] twoNewLines = "\r\n\r\n".getBytes();
	private static final byte[] newline = "\r\n".getBytes();
	private static final byte[] eos = "--".getBytes();
	
	private FileContainerFactory fileFactory;
	private FileContainerType fileType = FileContainerType.TEMPFILE;
	
	public MultipartFormParser(byte[] boundary) {
		setBoundary(boundary);
		fileFactory = new FileContainerFactory();
	}
	
	public MultipartFormParser(byte[] boundary, String tempDir) {
		setBoundary(boundary);
		fileFactory = new FileContainerFactory(tempDir);
	}
	
	public MultipartFormParser(String boundary) {
		setBoundary(boundary.getBytes());
		fileFactory = new FileContainerFactory();
	}
	
	public MultipartFormParser(String boundary, String tempDir) {
		setBoundary(boundary.getBytes());
		fileFactory = new FileContainerFactory(tempDir);
	}
	
	private void setBoundary(byte[] boundary) {
		this.boundary = new byte[boundary.length + 2];
		this.boundary[0] = '-';
		this.boundary[1] = '-';
		
		this.newlineBoundary = new byte[boundary.length + 4];
		this.newlineBoundary[0] = '\r';
		this.newlineBoundary[1] = '\n';
		this.newlineBoundary[2] = '-';
		this.newlineBoundary[3] = '-';
		
		for(int n = 0; n < boundary.length; n++) {
			this.boundary[n + 2] = boundary[n];
			this.newlineBoundary[n + 4] = boundary[n];
		}
	}
	
	public List<FormElement> parse(InputStream inputStream) throws Exception {
		PushbackInputStream pis = new PushbackInputStream(new BufferedInputStream(inputStream, 16 * 1024), 1024);
		List<FormElement> elements = new ArrayList<FormElement>();
		
		try {
//			readBlock(pis, new NullOutputStream(),new byte[][] {boundary}, new byte[0]);
			
			while(!isEnd(pis)) {
				Map<String, String> headers = getHeaders(pis);
				FormElement element;
				
				if(headers.containsKey("content-type")) {
					FileContainer fc = writeFile(pis);
					element = new FormElement(headers.get("name"), headers.get("filename"), headers.get("content-type"), fc);
				} else {
					String value = getValue(pis);
					element = new FormElement(headers.get("name"), value);
				}
				
				elements.add(element);
			}
		} catch(Exception ex) {
			throw new Exception(ex);
		}
		
		return elements;
	}
	
	private boolean isEnd(PushbackInputStream pis) throws Exception, IOException {
		byte[] eol = new byte[2];
		pis.read(eol);
		
		if(Arrays.equals(eol, eos))
			return true;
		
		if(!Arrays.equals(eol, newline))
			throw new Exception("Newline expected but not found");
		
		return false;
	}
	
	private void readBlock(PushbackInputStream pis, OutputStream os, byte[][] delimiters, byte[] dontEat) throws IOException, Exception {
		int byteCount = 0;
		
		while(true) {
			int i = pis.read();
			if(i == -1)
				throw new Exception("Unexpected end of stream");
			
			byte b = (byte)i;
			
			if(dontEat.length > 0 && b == dontEat[0]) {
				byte[] lookahead = new byte[dontEat.length];
				lookahead[0] = b;
				int count = pis.read(lookahead, 1, dontEat.length - 1);
				if(Arrays.equals(lookahead,  dontEat)) {
					pis.unread(lookahead);
					return;
				} else {
					pis.unread(lookahead, 1, count);
				}
			}
			
			for(int n = 0; n < delimiters.length; n++) {
				byte[] delimiter = delimiters[n];
				if(b == delimiter[0]) {
					byte[] lookahead = new byte[delimiter.length];
					lookahead[0] = b;
					int count = pis.read(lookahead, 1, delimiter.length - 1);
					if(Arrays.equals(lookahead, delimiter)) {
						return;
					} else {
						pis.unread(lookahead, 1, count);
					}
				}
			}
			os.write(b);
			byteCount++;
		}
	}
	
	private Map<String, String> getHeaders(PushbackInputStream pis) throws IOException, Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
		readBlock(pis, baos, new byte[][] {twoNewLines}, boundary);
		
		byte[] data = baos.toByteArray();
		baos.close();
		
		return processHeaders(new String(data, "UTF-8"));
	}
	
	private Map<String, String> processHeaders(String data) {
		Map<String, String> headers = new HashMap<String, String>();
		
		String[] lines = data.replace("\r", "").split("\n");
		for(String line : lines) {
			String[] header = line.trim().split(":");
			String key = header[0].trim().toLowerCase();
			if(header.length > 1) {
				String[] values = header[1].trim().split(";");
				headers.put(key,  values[0]);
				
				for(int i = 1; i < values.length; i++) {
					String[] parts = values[i].trim().split("=", 2);
					if(parts.length > 1) {
						if(parts[1].length() > 1) {
							if(parts[1].charAt(0) == '"' && parts[1].charAt(parts[1].length() - 1) == '"') {
								parts[1] = parts[1].substring(1, parts[1].length() - 1);
							}
						}
						headers.put(parts[0], parts[1]);
					} else {
						headers.put(parts[0], "");
					}
				}
			} else {
				headers.put(key, "");
			}
		}
		
		return headers;
	}
	
	private String getValue(PushbackInputStream pis) throws Exception, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(64 * 1024);
		readBlock(pis, baos, new byte[][] {newlineBoundary, boundary}, new byte[0]);
		
		byte[] data = baos.toByteArray();
		baos.close();
		
		return new String(data, "UTF-8");
	}
	
	private FileContainer writeFile(PushbackInputStream pis) throws IOException, Exception {
		FileContainer fc = fileFactory.getFileContainer(fileType);
		
		OutputStream os = fc.openOutputStream();
		readBlock(pis, os, new byte[][] {newlineBoundary, boundary}, new byte[0]);
		fc.closeOutputStream();
		
		return fc;
	}

	public FileContainerType getFileType() {
		return fileType;
	}

	public void setFileType(FileContainerType fileType) {
		this.fileType = fileType;
	}

}