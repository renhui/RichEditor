package com.example.richtext.utils.imageloader.download;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.richtext.imageloader.core.download.ImageDownloader.Scheme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Images.Thumbnails;
import android.webkit.MimeTypeMap;

/**
 * 图片下载类
 * 获取到图片资源的InputStream
 * @author renhui
 */
public class ImageDownloader {
	public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;  // 默认缓冲区大小 --- 32KB
	private static final int MAX_REDIRECT_COUNT = 5;  // 最大重定向的次数
	private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";  // 允许URI出现的字符
	
	private final Context context;
	
	public ImageDownloader(Context context) {
		this.context = context.getApplicationContext();
	}
	
	public InputStream getStream(String imageUri) throws IOException {
		switch (Scheme.ofUri(imageUri)) {
		case HTTP:
		case HTTPS:
			return getStreamFromNetwork(imageUri);
		case FILE:
			return getStreamFromFile(imageUri);
		case ASSETS:
			return getStreamFromAssets(imageUri);
		case DRAWABLE:
			return getStreamFromDrawable(imageUri);
		default:
			break;
		}
		return null;
	}
	
	/**
	 * 通过URI获取图片的输入流(网络)
	 * @param imageUri	图片的URI
	 * @return	图片的输入流{@link InputStream}
	 * @throws IOException	如果获取图片输入流的时候发生一些I/O错误
	 */
	protected InputStream getStreamFromNetwork(String imageUri) throws IOException {
		/*创建图片的URI链接*/
		HttpURLConnection conn = createConnection(imageUri);
		int redirectCount = 0;
		
		/*允许图片的链接请求时重定向,但是重定向次数不能大于5*/
		while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
			conn = createConnection(conn.getHeaderField("Location"));
			redirectCount ++;
		}
		
		InputStream imageStream;
		try {
			imageStream = conn.getInputStream();
		} catch (IOException e) {
			readAndCloseStream(conn.getErrorStream());
			throw e;
		}
		
		// 再次判断conn的状态吗是否是200,如果不是200,则证明此次网络请求出现的错误
		if(conn.getResponseCode() != 200) {
			closeSilently(imageStream);
			throw new IOException("Image request failed with response code " + conn.getResponseCode());
		}
		
		return new BufferedInputStream(imageStream);
	}
	
	/**
	 * 通过URI获取图片的输入流(本地文件系统或者SD卡).
	 * @param imageUri	图片的URI
	 * @return 	图片的输入流{@link InputStream}
	 * @throws IOException	如果获取图片输入流的时候发生一些I/O错误
	 */
	protected InputStream getStreamFromFile(String imageUri) throws IOException {
		// 去掉路径的Scheme
		String filePath = Scheme.FILE.crop(imageUri);
		if (isVideoFileUri(imageUri)) {
			return getVideoThumbnailStream(filePath);
		} else {
			return new BufferedInputStream(new FileInputStream(filePath), DEFAULT_BUFFER_SIZE);
		}
	}
	
	/**
	 * 通过URI获取图片的输入流(图片在应用的assets).
	 * @param imageUri 图片的URI
	 * @return	图片的输入流{@link InputStream}
	 * @throws IOException 如果文件读取是发生一些I/O错误
	 */
	protected InputStream getStreamFromAssets(String imageUri) throws IOException {
		String filePath = Scheme.ASSETS.crop(imageUri);
		return context.getAssets().open(filePath);
	}
	
	/**
	 * 通过URI获取图片的输入流(图片在应用的drawable里面).
	 * 
	 * @param imageUri 图片的URI
	 * @return 图片的输入流{@link InputStream}
	 */
	protected InputStream getStreamFromDrawable(String imageUri) {
		String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
		int drawableId = Integer.parseInt(drawableIdString);
		return context.getResources().openRawResource(drawableId);
	}
	
	/**
	 * 创建图片的URL的链接
	 * @param url  要链接的图片的URL  
	 * @return  要链接的URL的{@linkplain HttpURLConnection Connection}. 链接没执行，所以它仍然可以配置其他参数
	 * @throws IOException 如果网络请求发生错误或者输入流发生错误
	 */
	protected HttpURLConnection createConnection(String url) throws IOException {
		/* 将URI的String处理为符合URI格式的String,主要是对非ASCII码的字符进行编码*/
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection(); // 打开URL的链接
		return conn;
	}
	
	private InputStream getVideoThumbnailStream(String filePath) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.FULL_SCREEN_KIND);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
		}
		return null;
	}
	
	/** 判断uri是否是video类型*/
	private boolean isVideoFileUri(String uri) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		return mimeType != null && mimeType.startsWith("video/");
	}
	
	/**
	 * 从流中读取所有的数据然后静默关闭输入流
	 * @param is  输入流
	 */
	public static void readAndCloseStream(InputStream is) {
		final byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
		try {
			while (is.read(bytes, 0, DEFAULT_BUFFER_SIZE) != -1);
		} catch (IOException ignored) {
		} finally {
			closeSilently(is);
		}
	}
	
	/***
	 * 静默关闭流
	 * @param closeable
	 */
	public static void closeSilently(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception ignored) {
			}
		}
	}
}
