package com.AAA.photowall;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;


/**
 * @author Stone
 * 
 */
@SuppressLint("NewApi")
public class ImageLoader {

	// 图片缓存核心技术类,用于缓存所有下好的图片,程序内存达到设定值时会将最近最少使用的图片移除
	private static LruCache<String, Bitmap> mMemoryCache;
	// ImageLoader 实例
	private static ImageLoader mImageLoader;

	private ImageLoader() {
		// 获取应用程序最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// 设置图片缓存为最大内存的8分之1
		int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
	}

	/**
	 * 获取imageLoader实例
	 * 
	 * @return mImageLoader
	 */
	public static ImageLoader getInstance() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader();
		}
		return mImageLoader;
	}

	/**
	 * 将一张图片存储到LruCache中。
	 * 
	 * @param key
	 *            LruCache的键，这里传入图片的URL地址。
	 * @param bitmap
	 *            LruCache的值，这里传入从网络上下载的Bitmap对象。
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从缓存中取出图片
	 * 
	 * @param Lrucache的键
	 *            传入url的值
	 * @return 缓存中的bitmap
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 计算图片压缩比率 当图片宽度大于标准宽度时
	 * 
	 * @param options
	 * @param reqWidth
	 *            标准宽度
	 * @return 比率(图片的压缩比率)
	 */
	public static int caculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// 源图片宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			// 计算出实际宽度和目标宽度的比率
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth) {
		// 第一次解析将inJustDecodeBounds设为true 来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// 计算inSampleSize的值
		options.inSampleSize = caculateInSampleSize(options, reqWidth);
		// 使用获取到的inSampleSize的值再次获取图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

}
