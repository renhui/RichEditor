package com.imageloader.core;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import com.imageloader.core.assist.ImageScaleType;
import com.imageloader.core.display.BitmapDisplayer;
import com.imageloader.core.display.SimpleBitmapDisplayer;
import com.imageloader.core.download.ImageDownloader;
import com.imageloader.core.listener.ImageLoadingListener;
import com.imageloader.core.process.BitmapProcessor;

/**
 * 图片显示的选项：
 * 1、在加载图片的时候是否需要展示预置的图片
 * 2、要加载的图片的URI为空的是否是否需要展示预置的图片
 * 3、如果图片加载失败的时候是否需要展示预置的图片
 * 4、加载图片之前是否需要重置ImageView
 * 5、是否需要将图片缓存到内存中
 * 6、是否需要将图片缓存在磁盘中
 * 7、图片加载拉伸的类型
 * 8、解码类型 （包括 Bitmap解码的配置方式）
 * 9、延迟加载图片
 * 10、加载图片的时候是否需要考虑 图片的 EXIF参数
 * 11、是否需要给 {@link ImageDownloader #getStream(String, Object)}传辅助对象
 * 12、在缓存图片到内存之前进行预处理
 * 13、在已经缓存图片到内存后是否对位图进行再处理
 * 14、要展示的位图如何解码
 * 
 * 
 * 下面是使用的实例代码：
 * You can create instance:
 * <li>with {@link Builder}:<br />
 * <b>i.e.</b> :
 * <code>new {@link DisplayImageOptions}.{@link Builder#Builder() Builder()}.{@link Builder#cacheInMemory() cacheInMemory()}.
 * {@link Builder#showImageOnLoading(int) showImageOnLoading()}.{@link Builder#build() build()}</code><br />
 * </li>
 * <li>or by static method: {@link #createSimple()}</li> <br />
 * @author renhui
 */
public final class DisplayImageOptions {

	private final int imageResOnLoading;
	private final int imageResForEmptyUri;
	private final int imageResOnFail;
	private final Drawable imageOnLoading;
	private final Drawable imageForEmptyUri;
	private final Drawable imageOnFail;
	private final boolean resetViewBeforeLoading;
	private final boolean cacheInMemory;
	private final boolean cacheOnDisk;
	private final ImageScaleType imageScaleType;
	private final Options decodingOptions;
	private final int delayBeforeLoading;
	private final boolean considerExifParams;
	private final Object extraForDownloader;
	private final BitmapProcessor preProcessor;
	private final BitmapProcessor postProcessor;
	private final BitmapDisplayer displayer;
	private final Handler handler;
	private final boolean isSyncLoading;

	private DisplayImageOptions(Builder builder) {
		imageResOnLoading = builder.imageResOnLoading;
		imageResForEmptyUri = builder.imageResForEmptyUri;
		imageResOnFail = builder.imageResOnFail;
		imageOnLoading = builder.imageOnLoading;
		imageForEmptyUri = builder.imageForEmptyUri;
		imageOnFail = builder.imageOnFail;
		resetViewBeforeLoading = builder.resetViewBeforeLoading;
		cacheInMemory = builder.cacheInMemory;
		cacheOnDisk = builder.cacheOnDisk;
		imageScaleType = builder.imageScaleType;
		decodingOptions = builder.decodingOptions;
		delayBeforeLoading = builder.delayBeforeLoading;
		considerExifParams = builder.considerExifParams;
		extraForDownloader = builder.extraForDownloader;
		preProcessor = builder.preProcessor;
		postProcessor = builder.postProcessor;
		displayer = builder.displayer;
		handler = builder.handler;
		isSyncLoading = builder.isSyncLoading;
	}

	public boolean shouldShowImageOnLoading() {
		return imageOnLoading != null || imageResOnLoading != 0;
	}

	public boolean shouldShowImageForEmptyUri() {
		return imageForEmptyUri != null || imageResForEmptyUri != 0;
	}

	public boolean shouldShowImageOnFail() {
		return imageOnFail != null || imageResOnFail != 0;
	}

	public boolean shouldPreProcess() {
		return preProcessor != null;
	}

	public boolean shouldPostProcess() {
		return postProcessor != null;
	}

	public boolean shouldDelayBeforeLoading() {
		return delayBeforeLoading > 0;
	}

	public Drawable getImageOnLoading(Resources res) {
		return imageResOnLoading != 0 ? res.getDrawable(imageResOnLoading) : imageOnLoading;
	}

	public Drawable getImageForEmptyUri(Resources res) {
		return imageResForEmptyUri != 0 ? res.getDrawable(imageResForEmptyUri) : imageForEmptyUri;
	}

	public Drawable getImageOnFail(Resources res) {
		return imageResOnFail != 0 ? res.getDrawable(imageResOnFail) : imageOnFail;
	}

	public boolean isResetViewBeforeLoading() {
		return resetViewBeforeLoading;
	}

	public boolean isCacheInMemory() {
		return cacheInMemory;
	}

	public boolean isCacheOnDisk() {
		return cacheOnDisk;
	}

	public ImageScaleType getImageScaleType() {
		return imageScaleType;
	}

	public Options getDecodingOptions() {
		return decodingOptions;
	}

	public int getDelayBeforeLoading() {
		return delayBeforeLoading;
	}

	public boolean isConsiderExifParams() {
		return considerExifParams;
	}

	public Object getExtraForDownloader() {
		return extraForDownloader;
	}

	public BitmapProcessor getPreProcessor() {
		return preProcessor;
	}

	public BitmapProcessor getPostProcessor() {
		return postProcessor;
	}

	public BitmapDisplayer getDisplayer() {
		return displayer;
	}

	public Handler getHandler() {
		return handler;
	}

	boolean isSyncLoading() {
		return isSyncLoading;
	}

	/**
	 * Builder for {@link DisplayImageOptions}
	 *
	 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
	 */
	public static class Builder {
		private int imageResOnLoading = 0;
		private int imageResForEmptyUri = 0;
		private int imageResOnFail = 0;
		private Drawable imageOnLoading = null;
		private Drawable imageForEmptyUri = null;
		private Drawable imageOnFail = null;
		private boolean resetViewBeforeLoading = false;
		private boolean cacheInMemory = false;
		private boolean cacheOnDisk = false;
		private ImageScaleType imageScaleType = ImageScaleType.IN_SAMPLE_POWER_OF_2;
		private Options decodingOptions = new Options();
		private int delayBeforeLoading = 0;
		private boolean considerExifParams = false;
		private Object extraForDownloader = null;
		private BitmapProcessor preProcessor = null;
		private BitmapProcessor postProcessor = null;
		private BitmapDisplayer displayer = DefaultConfigurationFactory.createBitmapDisplayer();
		private Handler handler = null;
		private boolean isSyncLoading = false;

		public Builder() {
			decodingOptions.inPurgeable = true;
			decodingOptions.inInputShareable = true;
		}

		/**
		 * 预置的图片会在加载图片的时候在ImageAware进行展示
		 * 此方法当前已经弃用了
		 * @param imageRes Stub image resource
		 * @deprecated Use {@link #showImageOnLoading(int)} instead
		 */
		@Deprecated
		public Builder showStubImage(int imageRes) {
			imageResOnLoading = imageRes;
			return this;
		}

		/**
		 * 预置的图片会在加载图片的时候在ImageAware进行展示
		 * @param imageRes Image resource
		 */
		public Builder showImageOnLoading(int imageRes) {
			imageResOnLoading = imageRes;
			return this;
		}

		/**
		 * 预置的图片会在加载图片的时候在ImageAware进行展示
		 * 如果 {@link DisplayImageOptions.Builder#showImageOnLoading(int)} 也被调用设置了参数
		 * ,则此方法设定的参数会被忽略。
		 */
		public Builder showImageOnLoading(Drawable drawable) {
			imageOnLoading = drawable;
			return this;
		}

		/**
		 * 要加载的图片的uri为空的时候需要在ImageAware里面展示的图片
		 * 此方法逻辑：
		 * 如果检测到传递过来的图片的uri为空,则把此方法设定的图片的uri传过去,展示此图片
		 * @param imageRes Image resource
		 */
		public Builder showImageForEmptyUri(int imageRes) {
			imageResForEmptyUri = imageRes;
			return this;
		}

		/**
		 * 要加载的图片的uri为空的时候需要在ImageAware里面展示的图片
		 * 此方法逻辑：
		 * 如果检测到传递过来的图片的uri为空,则把此方法设定的图片的uri传过去,展示此图片
		 * 如果{@link DisplayImageOptions.Builder#showImageForEmptyUri(int)} 也被调用设置了参数
		 * ,则此方法设定的参数会被忽略。
		 */
		public Builder showImageForEmptyUri(Drawable drawable) {
			imageForEmptyUri = drawable;
			return this;
		}

		/**
		 * 
		 * 图片加载失败后需要在{@link com.imageloader.core.imageaware.ImageAware} 里面展示的图片
		 * @param imageRes Image resource
		 */
		public Builder showImageOnFail(int imageRes) {
			imageResOnFail = imageRes;
			return this;
		}

		/**
		 * 图片加载失败后需要在{@link com.imageloader.core.imageaware.ImageAware} 里面展示的图片
		 * 如果{@link DisplayImageOptions.Builder#showImageOnFail(int)} 也被调用设置了参数
		 * ,则此方法设定的参数会被忽略。
		 */
		public Builder showImageOnFail(Drawable drawable) {
			imageOnFail = drawable;
			return this;
		}

		/**
		 * {@link com.imageloader.core.imageaware.ImageAware
		 * image aware view} will be reset (set <b>null</b>) before image loading start
		 *
		 * @deprecated Use {@link #resetViewBeforeLoading(boolean) resetViewBeforeLoading(true)} instead
		 */
		public Builder resetViewBeforeLoading() {
			resetViewBeforeLoading = true;
			return this;
		}

		/**
		 * Sets whether {@link com.imageloader.core.imageaware.ImageAware
		 * image aware view} will be reset (set <b>null</b>) before image loading start
		 */
		public Builder resetViewBeforeLoading(boolean resetViewBeforeLoading) {
			this.resetViewBeforeLoading = resetViewBeforeLoading;
			return this;
		}

		/**
		 * Loaded image will be cached in memory
		 *
		 * @deprecated Use {@link #cacheInMemory(boolean) cacheInMemory(true)} instead
		 */
		@Deprecated
		public Builder cacheInMemory() {
			cacheInMemory = true;
			return this;
		}

		/** Sets whether loaded image will be cached in memory */
		public Builder cacheInMemory(boolean cacheInMemory) {
			this.cacheInMemory = cacheInMemory;
			return this;
		}

		/**
		 * Loaded image will be cached on disk
		 *
		 * @deprecated Use {@link #cacheOnDisk(boolean) cacheOnDisk(true)} instead
		 */
		@Deprecated
		public Builder cacheOnDisc() {
			return cacheOnDisk(true);
		}

		/**
		 * Sets whether loaded image will be cached on disk
		 *
		 * @deprecated Use {@link #cacheOnDisk(boolean)} instead
		 */
		@Deprecated
		public Builder cacheOnDisc(boolean cacheOnDisk) {
			return cacheOnDisk(cacheOnDisk);
		}

		/** Sets whether loaded image will be cached on disk */
		public Builder cacheOnDisk(boolean cacheOnDisk) {
			this.cacheOnDisk = cacheOnDisk;
			return this;
		}

		/**
		 * Sets {@linkplain ImageScaleType scale type} for decoding image. This parameter is used while define scale
		 * size for decoding image to Bitmap. Default value - {@link ImageScaleType#IN_SAMPLE_POWER_OF_2}
		 */
		public Builder imageScaleType(ImageScaleType imageScaleType) {
			this.imageScaleType = imageScaleType;
			return this;
		}

		/** Sets {@link Bitmap.Config bitmap config} for image decoding. Default value - {@link Bitmap.Config#ARGB_8888} */
		public Builder bitmapConfig(Bitmap.Config bitmapConfig) {
			if (bitmapConfig == null) throw new IllegalArgumentException("bitmapConfig can't be null");
			decodingOptions.inPreferredConfig = bitmapConfig;
			return this;
		}

		/**
		 * Sets options for image decoding.<br />
		 * <b>NOTE:</b> {@link Options#inSampleSize} of incoming options will <b>NOT</b> be considered. Library
		 * calculate the most appropriate sample size itself according yo {@link #imageScaleType(ImageScaleType)}
		 * options.<br />
		 * <b>NOTE:</b> This option overlaps {@link #bitmapConfig(android.graphics.Bitmap.Config) bitmapConfig()}
		 * option.
		 */
		public Builder decodingOptions(Options decodingOptions) {
			if (decodingOptions == null) throw new IllegalArgumentException("decodingOptions can't be null");
			this.decodingOptions = decodingOptions;
			return this;
		}

		/** Sets delay time before starting loading task. Default - no delay. */
		public Builder delayBeforeLoading(int delayInMillis) {
			this.delayBeforeLoading = delayInMillis;
			return this;
		}

		/** Sets auxiliary object which will be passed to {@link ImageDownloader#getStream(String, Object)} */
		public Builder extraForDownloader(Object extra) {
			this.extraForDownloader = extra;
			return this;
		}

		/** Sets whether ImageLoader will consider EXIF parameters of JPEG image (rotate, flip) */
		public Builder considerExifParams(boolean considerExifParams) {
			this.considerExifParams = considerExifParams;
			return this;
		}

		/**
		 * Sets bitmap processor which will be process bitmaps before they will be cached in memory. So memory cache
		 * will contain bitmap processed by incoming preProcessor.<br />
		 * Image will be pre-processed even if caching in memory is disabled.
		 */
		public Builder preProcessor(BitmapProcessor preProcessor) {
			this.preProcessor = preProcessor;
			return this;
		}

		/**
		 * Sets bitmap processor which will be process bitmaps before they will be displayed in
		 * {@link com.imageloader.core.imageaware.ImageAware image aware view} but
		 * after they'll have been saved in memory cache.
		 */
		public Builder postProcessor(BitmapProcessor postProcessor) {
			this.postProcessor = postProcessor;
			return this;
		}

		/**
		 * Sets custom {@link BitmapDisplayer displayer} for image loading task. Default value -
		 * {@link DefaultConfigurationFactory#createBitmapDisplayer()}
		 */
		public Builder displayer(BitmapDisplayer displayer) {
			if (displayer == null) throw new IllegalArgumentException("displayer can't be null");
			this.displayer = displayer;
			return this;
		}

		Builder syncLoading(boolean isSyncLoading) {
			this.isSyncLoading = isSyncLoading;
			return this;
		}

		/**
		 * Sets custom {@linkplain Handler handler} for displaying images and firing {@linkplain ImageLoadingListener
		 * listener} events.
		 */
		public Builder handler(Handler handler) {
			this.handler = handler;
			return this;
		}

		/** Sets all options equal to incoming options */
		public Builder cloneFrom(DisplayImageOptions options) {
			imageResOnLoading = options.imageResOnLoading;
			imageResForEmptyUri = options.imageResForEmptyUri;
			imageResOnFail = options.imageResOnFail;
			imageOnLoading = options.imageOnLoading;
			imageForEmptyUri = options.imageForEmptyUri;
			imageOnFail = options.imageOnFail;
			resetViewBeforeLoading = options.resetViewBeforeLoading;
			cacheInMemory = options.cacheInMemory;
			cacheOnDisk = options.cacheOnDisk;
			imageScaleType = options.imageScaleType;
			decodingOptions = options.decodingOptions;
			delayBeforeLoading = options.delayBeforeLoading;
			considerExifParams = options.considerExifParams;
			extraForDownloader = options.extraForDownloader;
			preProcessor = options.preProcessor;
			postProcessor = options.postProcessor;
			displayer = options.displayer;
			handler = options.handler;
			isSyncLoading = options.isSyncLoading;
			return this;
		}

		/** Builds configured {@link DisplayImageOptions} object */
		public DisplayImageOptions build() {
			return new DisplayImageOptions(this);
		}
	}

	/**
	 * Creates options appropriate for single displaying:
	 * <ul>
	 * <li>View will <b>not</b> be reset before loading</li>
	 * <li>Loaded image will <b>not</b> be cached in memory</li>
	 * <li>Loaded image will <b>not</b> be cached on disk</li>
	 * <li>{@link ImageScaleType#IN_SAMPLE_POWER_OF_2} decoding type will be used</li>
	 * <li>{@link Bitmap.Config#ARGB_8888} bitmap config will be used for image decoding</li>
	 * <li>{@link SimpleBitmapDisplayer} will be used for image displaying</li>
	 * </ul>
	 * <p/>
	 * These option are appropriate for simple single-use image (from drawables or from Internet) displaying.
	 */
	public static DisplayImageOptions createSimple() {
		return new Builder().build();
	}
}
