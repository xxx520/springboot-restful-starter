package com.neko.seed.base.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;

import com.neko.seed.IoCallback;
import com.neko.seed.util.MultipartFormParser;

@WebServlet(name="fileUploadServlet",urlPatterns="/swift_upload")
public class FileUploadServlet extends HttpServlet{
		
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");  
        response.setCharacterEncoding("utf-8");  
         
        //检测是不是存在上传文件  
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);  
          
        if(isMultipart){  
            DiskFileItemFactory factory = new DiskFileItemFactory();  
            
            //指定在内存中缓存数据大小,单位为byte,这里设为1Mb  
            factory.setSizeThreshold(1024*1024);  
           
            //设置一旦文件大小超过getSizeThreshold()的值时数据存放在硬盘的目录   
            factory.setRepository(new File("f:\\repository.zip"));  
            
            
            // Create a new file upload handler  
            ServletFileUpload upload = new ServletFileUpload(factory);  
           
            // 指定单个上传文件的最大尺寸,单位:字节，这里设为50Mb    
            upload.setFileSizeMax(50 * 1024 * 1024);    
            
            //指定一次上传多个文件的总尺寸,单位:字节，这里设为50Mb  
            upload.setSizeMax(50 * 1024 * 1024);     
            upload.setHeaderEncoding("UTF-8");
              
            List<FileItem> items = null;  
              
            try {  
//            	printInputstream(request.getInputStream());
//                // 解析request请求  （大文件比较耗时间//TODO）
//            	String contenttype = request.getHeader( "Content-Type");
//            	String[] ss = contenttype.split( ";");
//            	String bound=ss[1].split( "=")[1];
//            	new MultipartFormParser(bound.getBytes()).parse(request.getInputStream());
//                items = upload.parseRequest(request);
            	/**
            	 * {
						// write inputstream 2 outputstream
                		@Override
						public void excute(InputStream in, OutputStream out) {
                			try {
                				File file=new File( "f://temp.file");
                				if(!file.exists()){
                					file.createNewFile();
                				}
								FileOutputStream fileOutputStream = new FileOutputStream(file);
                				Streams.copy(in, fileOutputStream, true);
							} catch ( IOException e) {
								e.printStackTrace();
							}
							response.getBufferSize();
						}
					}
            	 */
                items = upload.parseRequest_New(request,new IoCallback() ,null);
            } catch (FileUploadException e) {  
                e.printStackTrace();  
            } catch (Exception e) {
				e.printStackTrace();
			}  
            
            if(items!=null){  
                //解析表单项目  
                Iterator<FileItem> iter = items.iterator();  
                while (iter.hasNext()) {  
                    FileItem item = iter.next(); 
                    
                    //如果是普通表单属性  
                    if (item.isFormField()) {  
                        //相当于input的name属性   <input type="text" name="content">  
                        String name = item.getFieldName();
                        
                        //input的value属性  
                        String value = item.getString();
                        
                        System.out.println("属性:" + name + " 属性值:" + value);  
                    }  
                    //如果是上传文件  
                    else {  
                        //属性名  
                        String fieldName = item.getFieldName();  
                        
                        //上传文件路径  
                        String fileName = item.getName();  
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);// 获得上传文件的文件名  
                        
//                        try {  
//                            item.write(new File(uploadPath, fileName));  
//                        } catch (Exception e) {  
//                            e.printStackTrace();  
//                        }  
                    } 
                }  
            }  
        }  
        response.addHeader("token", "hello");
		
//		Streams.copy(inputStream, outputStream, true);
		response.getWriter().print( "hello");
	}

	private void printInputstream(InputStream inputStream) {
		BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
		String line=null;
		try {
			while ((line=reader.readLine())!=null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
