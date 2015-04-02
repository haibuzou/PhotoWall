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

	// ͼƬ������ļ�����,���ڻ��������ºõ�ͼƬ,�����ڴ�ﵽ�趨ֵʱ�Ὣ�������ʹ�õ�ͼƬ�Ƴ�
	private static LruCache<String, Bitmap> mMemoryCache;
	// ImageLoader ʵ��
	private static ImageLoader mImageLoader;

	private ImageLoader() {
		// ��ȡӦ�ó����������ڴ�
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		// ����ͼƬ����Ϊ����ڴ��8��֮1
		int cacheSize = maxMemory / 8;
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};
	}

	/**
	 * ��ȡimageLoaderʵ��
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
	 * ��һ��ͼƬ�洢��LruCache�С�
	 * 
	 * @param key
	 *            LruCache�ļ������ﴫ��ͼƬ��URL��ַ��
	 * @param bitmap
	 *            LruCache��ֵ�����ﴫ������������ص�Bitmap����
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * �ӻ�����ȡ��ͼƬ
	 * 
	 * @param Lrucache�ļ�
	 *            ����url��ֵ
	 * @return �����е�bitmap
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * ����ͼƬѹ������ ��ͼƬ��ȴ��ڱ�׼���ʱ
	 * 
	 * @param options
	 * @param reqWidth
	 *            ��׼���
	 * @return ����(ͼƬ��ѹ������)
	 */
	public static int caculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// ԴͼƬ���
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			// �����ʵ�ʿ�Ⱥ�Ŀ���ȵı���
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth) {
		// ��һ�ν�����inJustDecodeBounds��Ϊtrue ����ȡͼƬ��С
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		// ����inSampleSize��ֵ
		options.inSampleSize = caculateInSampleSize(options, reqWidth);
		// ʹ�û�ȡ����inSampleSize��ֵ�ٴλ�ȡͼƬ
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

}
