package com.neko.seed.util;

public class FileContainerFactory {

	private String tempDir;
	
	public FileContainerFactory() {
		this.tempDir = "/tmp";
	}

	public FileContainerFactory(String tempDir) {
		this.tempDir = tempDir;
	}
	
	public FileContainer getFileContainer(FileContainerType type) throws Exception {
		if(type == FileContainerType.TEMPFILE)
			return new TemporaryFileContainer(tempDir);
		else if(type == FileContainerType.MEMORY)
			return new MemoryFileContainer();
		
		throw new Exception(type.toString());
	}

}
