package com.AAA.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.AAA.photowall.ImageLoader;
import com.AAA.photowall.Images;
import com.AAA.photowall.R;


/**
//                               _oo0oo_
//                             o8888888o
//                             88"   .   "88
//                             (|    -_-    |)
//                             0\    =    /0
//                          ___ /   '---'   \ ___
//                        .'  \\\|            |//  '.
//                       /  \\\|||     :      |||//  \\
//                        / _ |||||    -:-     |||||- \\
//                       | |  \\\\     -     /// |   |
//                       |  \_|  ''\   ---  /''   |_/ |
//                        \   .-\__   '-'   __/-.   /
//                     ___ '. .'   /  --.--  \    '. .' ___
//                  .""  '<  '.___\_ <|>_/___.' >'  "".
//                     | |  : '-  \'.;'\  _  /';.'/ - ' :  | |
//                   \  \ '_.    \_ __\  /__ _/    .-' /  /
//          ====='-.____'.___ \_____/___.-'____.-'=====
//                                   '=---='
//
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//
//                  ���汣��                 ����BUG       �����޸�

*/

public class MyScrollView extends ScrollView implements OnTouchListener {

	// ÿ��Ҫ����ͼƬ������
	public static final int PAGE_SIZE = 15;
	private int page;

	private int columnWidth;
	/**
	 * ��һ�еĸ߶�
	 */
	private int firstColumnHeight;
	/**
	 * �ڶ��еĸ߶�
	 */
	private int secondColumnHeight;
	/**
	 * �����еĸ߶�
	 */
	private int thirdColumnHeight;

	/**
	 * �Ƿ��һ�μ���
	 */
	private boolean loadOnce;
	/**
	 * ��ͼƬ���й���Ĺ�����
	 */
	private ImageLoader imageLoader;
	/**
	 * ��һ�в���
	 */
	private LinearLayout firstColumn;
	/**
	 * �ڶ��в���
	 */
	private LinearLayout secondColumn;
	/**
	 * �����в���
	 */
	private LinearLayout thirdColumn;
	/**
	 * ��¼�����������ػ��ߵȴ����ص�����
	 */
	private static Set<LoadImageTask> taskCollection;
	/**
	 * MyScrollView�µ�ֱ���Ӳ���
	 */
	private static View scrollLayout;

	/**
	 * MyScrollView�ĸ߶�
	 */
	private static int scrollViewHeight;

	/**
	 * ��¼��ֱ����Ĺ�������
	 */
	private static int lastScrollY = -1;

	/**
	 * ��¼���н����ϵ�ͼƬ ����ʱ���Զ�ͼƬ�����ͷ�
	 */
	private List<ImageView> imageList = new ArrayList<ImageView>();

	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			MyScrollView myScrollView = (MyScrollView) msg.obj;
			int scrollY = myScrollView.getScrollY();
			// �����ǰ����λ�ú��ϴ���ͬ ��ʾ�Ѿ�ֹͣ����
			if (scrollY == lastScrollY) {
				// ���»����ײ�����û���������ص�����ʱ����ʼ������һ��ͼƬ
				if (scrollViewHeight + scrollY >= scrollLayout.getHeight()
						&& taskCollection.isEmpty()) {
					myScrollView.loadMoreImages();
				}
				myScrollView.checkVisibility();
			} else {
				lastScrollY = scrollY;
				Message message = new Message();
				message.obj = myScrollView;
				// 5����ٴζԹ���λ�ý����ж�
				handler.sendMessageDelayed(message, 5);
			}
		}

	};

	/**
	 * MyScrollView�������
	 * 
	 * @param context
	 * @param attrs
	 */
	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		imageLoader = ImageLoader.getInstance();
		taskCollection = new HashSet<LoadImageTask>();
		setOnTouchListener(this);
	}

	/*
	 * ����һЩ�ؼ��ĳ�ʼ������,��ȡMyScrollView�ĸ߶�,�Լ���һ�еĿ��ֵ.�������￪ʼ���ص�һҳͼƬ
	 */
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed && !loadOnce) {
			scrollViewHeight = getHeight();
			scrollLayout = getChildAt(0);
			firstColumn = (LinearLayout) findViewById(R.id.first_column);
			secondColumn = (LinearLayout) findViewById(R.id.second_column);
			thirdColumn = (LinearLayout) findViewById(R.id.third_column);
			columnWidth = firstColumn.getWidth();
			loadOnce = true;
			loadMoreImages();
		}
	}

	/**
	 * ������һ��ͼƬ ÿ��ͼƬ���Ὺ��һ���첽�߳�ȥ����
	 */
	public void loadMoreImages() {
		if (hasSDCard()) {
			int startIndex = page * PAGE_SIZE;
			int endIndex = page * PAGE_SIZE + PAGE_SIZE;
			if (startIndex < Images.imageUrls.length) {
				Toast.makeText(getContext(), "���ڼ���...", Toast.LENGTH_SHORT)
						.show();
				if (endIndex > Images.imageUrls.length) {
					endIndex = Images.imageUrls.length;
				}
				for (int i = startIndex; i < endIndex; i++) {
					LoadImageTask loadImageTask = new LoadImageTask();
					taskCollection.add(loadImageTask);
					loadImageTask.execute(Images.imageUrls[i]);
				}
				page++;
			} else {
				Toast.makeText(getContext(), "û�и���ͼƬ", Toast.LENGTH_SHORT)
						.show();
			}
		} else {
			Toast.makeText(getContext(), "û�з���SD��", Toast.LENGTH_SHORT).show();
		}
	}

	public void checkVisibility() {
		for (int i = 0; i < imageList.size(); i++) {
			ImageView imageView = imageList.get(i);
			int borderTop = (Integer) imageView.getTag(R.string.border_top);
			int borderBottom = (Integer) imageView
					.getTag(R.string.border_bottom);
			if (borderBottom > getScrollY()
					&& borderTop < getScrollY() + scrollViewHeight) {
				String imageUrl = (String) imageView.getTag(R.string.image_url);
				Bitmap bitmap = imageLoader.getBitmapFromMemoryCache(imageUrl);
				if (bitmap != null) {
					imageView.setImageBitmap(bitmap);
				} else {
					LoadImageTask task = new LoadImageTask(imageView);
					task.execute(imageUrl);
				}
			} else {
				imageView.setImageResource(R.drawable.empty_photo);
			}
		}
	}

	private boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			Message message = new Message();
			message.obj = this;
			handler.sendMessageDelayed(message, 5);
		}
		return false;
	}

	/**
	 * �첽����ͼƬ����
	 * 
	 * @author Stone
	 * 
	 */
	class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

		private String mImageUrl;
		/**
		 * ���ظ�ʹ�õ�imageview
		 */
		private ImageView mImageView;

		public LoadImageTask() {
		}

		/**
		 * ���ظ�ʹ�õ�imageview����
		 * 
		 * @param mImageView
		 */
		public LoadImageTask(ImageView mImageView) {
			this.mImageView = mImageView;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			mImageUrl = params[0];
			Bitmap imageBitmap = imageLoader
					.getBitmapFromMemoryCache(mImageUrl);
			if (imageBitmap == null) {
				imageBitmap = loadImage(mImageUrl);
			}
			return imageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				double ratio = bitmap.getWidth() / (columnWidth * 1.0);
				int scaleHeight = (int) (bitmap.getHeight() / ratio);
				addImage(bitmap, columnWidth, scaleHeight);
			}
			taskCollection.remove(this);
		}

		private Bitmap loadImage(String imageUrl) {
			File imageFile = new File(getImagePath(imageUrl));
			Log.e("loadImage", imageFile.exists() + " , "
					+ getImagePath(imageUrl));
			if (!imageFile.exists()) {
				downloadImage(imageUrl);
			}
			if (imageUrl != null) {
				Bitmap bitmap = imageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
					return bitmap;
				}
			}
			return null;
		}

		private void addImage(Bitmap bitmap, int imageWidth, int imageHeight) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					imageWidth, imageHeight);
			if (mImageView != null) {
				mImageView.setImageBitmap(bitmap);
			} else {
				ImageView imageView = new ImageView(getContext());
				imageView.setLayoutParams(params);
				imageView.setImageBitmap(bitmap);
				imageView.setScaleType(ScaleType.FIT_XY);
				imageView.setPadding(5, 5, 5, 5);
				imageView.setTag(R.string.image_url, mImageUrl);
				findColumnToAdd(imageView, imageHeight).addView(imageView);
				imageList.add(imageView);
			}
		}

		private LinearLayout findColumnToAdd(ImageView imageView,
				int imageHeight) {
			Log.e("AAA", "firstColumnHeight: "+firstColumnHeight+" ,secondColumnHeight"+secondColumnHeight+" ,thirdColumnHeight"+thirdColumnHeight);
			if (firstColumnHeight <= secondColumnHeight) {
				if (firstColumnHeight <= thirdColumnHeight) {
					imageView.setTag(R.string.border_top, firstColumnHeight);
					firstColumnHeight += imageHeight;
					imageView.setTag(R.string.border_bottom, firstColumnHeight);
					return firstColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			} else {
				if (secondColumnHeight <= thirdColumnHeight) {
					imageView.setTag(R.string.border_top, secondColumnHeight);
					secondColumnHeight += imageHeight;
					imageView
							.setTag(R.string.border_bottom, secondColumnHeight);
					return secondColumn;
				}
				imageView.setTag(R.string.border_top, thirdColumnHeight);
				thirdColumnHeight += imageHeight;
				imageView.setTag(R.string.border_bottom, thirdColumnHeight);
				return thirdColumn;
			}
		}

		/**
		 * ��ͼƬ���ص�SD����������
		 * 
		 * @param imageUrl
		 *            ͼƬ��url��ַ
		 */
		private void downloadImage(String imageUrl) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Log.d("TAG", "monted sdcard");
			} else {
				Log.d("TAG", "has no sdcard");
			}
			HttpURLConnection con = null;
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			BufferedInputStream bis = null;
			File imageFile = null;
			try {
				URL url = new URL(imageUrl);
				con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(5 * 1000);
				con.setReadTimeout(15 * 1000);
				con.setDoInput(true);
				con.setDoOutput(true);
				bis = new BufferedInputStream(con.getInputStream());
				imageFile = new File(getImagePath(imageUrl));
				fos = new FileOutputStream(imageFile);
				bos = new BufferedOutputStream(fos);
				byte[] b = new byte[1024];
				int length;
				while ((length = bis.read(b)) != -1) {
					bos.write(b, 0, length);
					bos.flush();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (bis != null) {
						bis.close();
					}
					if (bos != null) {
						bos.close();
					}
					if (con != null) {
						con.disconnect();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (imageFile != null) {
				Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(
						imageFile.getPath(), columnWidth);
				if (bitmap != null) {
					imageLoader.addBitmapToMemoryCache(imageUrl, bitmap);
				}
			}
		}

		/**
		 * ��ȡͼƬ���ش洢·��
		 * 
		 * @param imageUrl
		 *            ͼƬurl
		 * @return ͼƬ���ش洢·��
		 */
		private String getImagePath(String imageUrl) {
			int lastSlashIndex = imageUrl.lastIndexOf("/");
			String name = imageUrl.substring(lastSlashIndex + 1);
			String imageDir = Environment.getExternalStorageDirectory()
					.getPath() + "/PhotoWall/";
			File file = new File(imageDir);
			if (!file.exists()) {
				file.mkdir();
			}
			String imagePath = imageDir + name;
			return imagePath;
		}
	}
}
