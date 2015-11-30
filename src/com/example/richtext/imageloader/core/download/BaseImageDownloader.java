package com.example.richtext.imageloader.core.download;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.example.richtext.imageloader.core.DisplayImageOptions;
import com.example.richtext.imageloader.core.assist.ContentLengthInputStream;
import com.example.richtext.imageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 提供通过URI获取图片的输入流(图片来源：网络、文件系统、其他app资源)
 * {@link URLConnection}用于检索图像流从网络。
 * @author renhui
 */
public class BaseImageDownloader implements ImageDownloader {
	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
	public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds
	protected static final int BUFFER_SIZE = 32 * 1024; // 32 Kb
	
	protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";  // 允许URI出现的字符

	protected static final int MAX_REDIRECT_COUNT = 5;  // 最大重定向的次数

	protected static final String CONTENT_CONTACTS_URI_PREFIX = "content://com.android.contacts/";

	private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. " + "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

	protected final Context context;
	protected final int connectTimeout;
	protected final int readTimeout;

	public BaseImageDownloader(Context context) {
		this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
	}

	public BaseImageDownloader(Context context, int connectTimeout, int readTimeout) {
		this.context = context.getApplicationContext();
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	@Override
	public InputStream getStream(String imageUri, Object extra) throws IOException {
		switch (Scheme.ofUri(imageUri)) {
			case HTTP:
			case HTTPS:
				return getStreamFromNetwork(imageUri, extra);
			case FILE:
				return getStreamFromFile(imageUri, extra);
			case CONTENT:
				return getStreamFromContent(imageUri, extra);
			case ASSETS:
				return getStreamFromAssets(imageUri, extra);
			case DRAWABLE:
				return getStreamFromDrawable(imageUri, extra);
			case UNKNOWN:
			default:
				return getStreamFromOtherSource(imageUri, extra);
		}
	}

	/**
	 * 通过URI获取图片的输入流(网络).
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 * @throws IOException 如果获取图片输入流的时候发生一些I/O错误
	 */
	protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
		// 创建图片的URI链接
		HttpURLConnection conn = createConnection(imageUri, extra);

		int redirectCount = 0;
		
		// 检测到需要重定向3xx 重定向
		while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
			// 获取conn的头部信息的Location内容
			conn = createConnection(conn.getHeaderField("Location"), extra);
			redirectCount++;
		}

		InputStream imageStream;
		try {
			imageStream = conn.getInputStream();
		} catch (IOException e) {
			// 读取conn里面所有的数据来让链接能够重用
			IoUtils.readAndCloseStream(conn.getErrorStream());
			throw e;
		}
		
		// 返回值不是200
		if (!shouldBeProcessed(conn)) {
			IoUtils.closeSilently(imageStream);
			throw new IOException("Image request failed with response code " + conn.getResponseCode());
		}

		return new ContentLengthInputStream(new BufferedInputStream(imageStream, BUFFER_SIZE), conn.getContentLength());
	}

	/**
	 * @param 已经打开的网络链接(响应代码是可用的)
	 * @return <b>true</b> - 如果数据来自连接是正确的，应该读和处理；
	 *         <b>false</b> - 如果响应包含不相关的数据，不应该被处理
	 * @throws IOException
	 */
	protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
		return conn.getResponseCode() == 200;
	}

	/**
	 * 创建输入的URL的Http链接{@linkplain HttpURLConnection HTTP connection}
	 *
	 * @param url   要链接的URL
	 * @param extra 辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 要链接的URL的{@linkplain HttpURLConnection Connection}. 链接没执行，所以它仍然可以配置其他参数
	 * @throws IOException 如果网络请求发生错误或者输入流发生错误
	 */
	protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
		// 将URI的String处理为符合URI格式的String,主要是对非ASCII码的字符进行编码
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}

	/**
	 * 通过URI获取图片的输入流(本地文件系统或者SD卡).
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 * @throws IOException 如果获取图片输入流的时候发生一些I/O错误
	 */
	protected InputStream getStreamFromFile(String imageUri, Object extra) throws IOException {
		// 去掉URI的Scheme
		String filePath = Scheme.FILE.crop(imageUri);
		if (isVideoFileUri(imageUri)) {
			return getVideoThumbnailStream(filePath);
		} else {
			BufferedInputStream imageStream = new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
			return new ContentLengthInputStream(imageStream, (int) new File(filePath).length());
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private InputStream getVideoThumbnailStream(String filePath) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
		}
		return null;
	}

	/**
	 * 通过URI获取图片的输入流(图片可以通过 {@link ContentResolver}访问).
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 * @throws FileNotFoundException  如果提供URI不能被打开
	 */
	protected InputStream getStreamFromContent(String imageUri, Object extra) throws FileNotFoundException {
		ContentResolver res = context.getContentResolver();

		Uri uri = Uri.parse(imageUri);
		if (isVideoContentUri(uri)) { // video thumbnail
			Long origId = Long.valueOf(uri.getLastPathSegment());
			Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(res, origId, MediaStore.Images.Thumbnails.MINI_KIND, null);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
		} else if (imageUri.startsWith(CONTENT_CONTACTS_URI_PREFIX)) { // contacts photo
			return getContactPhotoStream(uri);
		}

		return res.openInputStream(uri);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected InputStream getContactPhotoStream(Uri uri) {
		ContentResolver res = context.getContentResolver();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return ContactsContract.Contacts.openContactPhotoInputStream(res, uri, true);
		} else {
			return ContactsContract.Contacts.openContactPhotoInputStream(res, uri);
		}
	}

	/**
	 * 通过URI获取图片的输入流(图片在应用的assets).
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 * @throws IOException  如果文件读取是发生一些I/O错误
	 */
	protected InputStream getStreamFromAssets(String imageUri, Object extra) throws IOException {
		String filePath = Scheme.ASSETS.crop(imageUri);
		return context.getAssets().open(filePath);
	}

	/**
	 * 通过URI获取图片的输入流(图片在应用的drawable里面).
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的输入流{@link InputStream}
	 */
	protected InputStream getStreamFromDrawable(String imageUri, Object extra) {
		String drawableIdString = Scheme.DRAWABLE.crop(imageUri);
		int drawableId = Integer.parseInt(drawableIdString);
		return context.getResources().openRawResource(drawableId);
	}

	/**
	 * 要获取输入流的图片的URI是其他渠道的,而且图片的scheme不被支持.
	 * 此类URI的资源如果需要的话可以有继承此类的方法通过重写来实现
	 * This method is called only if image URI has unsupported scheme. Throws {@link UnsupportedOperationException} by
	 * default.
	 *
	 * @param imageUri 图片的URI
	 * @param extra    辅助对象,通过{@link DisplayImageOptions.Builder#extraForDownloader(Object)
	 *                 DisplayImageOptions.extraForDownloader(Object)}; 传递过来,可以为空
	 * @return 图片的{@link InputStream}
	 * @throws IOException      如果发生了一些I/O异常
	 * @throws UnsupportedOperationException 如果图片的URI存在不被支持的方案(协议)
	 */
	protected InputStream getStreamFromOtherSource(String imageUri, Object extra) throws IOException {
		throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME, imageUri));
	}

	private boolean isVideoContentUri(Uri uri) {
		String mimeType = context.getContentResolver().getType(uri);
		return mimeType != null && mimeType.startsWith("video/");
	}

	private boolean isVideoFileUri(String uri) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		return mimeType != null && mimeType.startsWith("video/");
	}
}
