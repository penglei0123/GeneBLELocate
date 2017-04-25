package com.genepoint.datapack;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * surfaceview
 * 
 */
public class DragZoomImageSurfaceView extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {
	private String TAG = "DragZoomImageSurfaceView";
	private boolean DEBUG = false;

	// ����״̬
	private static final int MODE_NONE = 0x00;
	private static final int MODE_DRAG = 0x01;
	private static final int MODE_ZOOM = 0x02;
	private int mMode = MODE_NONE;

	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private Canvas mCanvas;//画布
	private long lastClikeTime;// 记录上一次点击屏幕的时间，以判断双击事件
	private Bitmap mapBitmap = null;//地图
	private Bitmap posBitmap = null;//定位图标
	private Bitmap backgroundBitmap = null;//背景图
	private Paint paint;//画笔

	private float curRate = 1.0f;
	private float preRate = 1.0f;
	
	private final float MAX_RATE=2;
	private final float MIN_RATE=0.3f;

	private PointF startP = new PointF();
	private PointF endP = new PointF();
	private PointF mapCenterP = new PointF();//surfaceview的中心点
	private PointF CenterP = new PointF();//地图图片的中心点（w/2,h/2）
	public PointF markPoint=new PointF();//标记点在地图的坐标
	
	private PointF touchPoint=new PointF();
	
	private float oldDist = 1.0f;

	private PointF myPosition = new PointF(-1.0f, -1.0f);

	public DragZoomImageSurfaceView(Context context) {
		super(context);
		init();
	}

	public DragZoomImageSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragZoomImageSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	public PointF getMarkpoint(){
		return markPoint;
		
	}

	/**
	 * 初始化SurfaceView
	 */
	private void init() {
		surfaceView = this;
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceView.setOnTouchListener(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		float centerX = surfaceView.getWidth() / 2.0f;
		float centerY = surfaceView.getHeight() / 2.0f;
		mapCenterP.set(centerX, centerY);
		drawSurfaceView();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (mapBitmap != null) {
			mapBitmap.recycle();
		}
		if (backgroundBitmap != null) {
			backgroundBitmap.recycle();
		}
		if (posBitmap != null) {
			posBitmap.recycle();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:// 单指开始触摸
			startP.set(event.getX(), event.getY());
			endP.set(event.getX(), event.getY());
			mMode = MODE_DRAG;
			touchPoint.set(event.getX(),
					event.getY());			
			break;
		case MotionEvent.ACTION_UP:// 单指抬起
			preRate = curRate;
			mMode = MODE_NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:  // 双指触摸
			oldDist = spacing(event);
			if (oldDist > 5.0f) {
				mMode = MODE_ZOOM;
			}
			break;
		case MotionEvent.ACTION_POINTER_UP: // 双指抬起
			mMode = MODE_NONE;
			break;
		case MotionEvent.ACTION_MOVE: // 在移动(手指或者其他)
			if (mMode == MODE_DRAG) {
				endP.set(event.getX(), event.getY());
				mapCenterP.set(mapCenterP.x + (endP.x - startP.x), mapCenterP.y + (endP.y - startP.y));
//				move.set(move.x+(endP.x - startP.x),move.y+(endP.y - startP.y));
				startP.set(event.getX(), event.getY());
			} else if (mMode == MODE_ZOOM) {
				float newDist = spacing(event);
				if (newDist > 5.0f) {
					curRate = preRate * (newDist / oldDist);
					if(curRate>=MAX_RATE)
						curRate=MAX_RATE;
					if(curRate<=MIN_RATE)
						curRate=MIN_RATE;
				}
			}
			drawSurfaceView();
			break;
		}
		return true;
	}

	/**
	 * 加载地图
	 * 
	 * @param imgPathStr
	 *            
	 */
	public void loadMap(String imgPathStr) {
		mapBitmap = BitmapFactory.decodeFile(imgPathStr);
		CenterP.set((float)mapBitmap.getWidth()/2,(float)mapBitmap.getHeight()/2);
//		mapwh.set((float)mapBitmap.getWidth()/2,(float)mapBitmap.getHeight()/2);
		drawSurfaceView();
	}

	/**
	 *
	 * 
	 * @param resourceId
	 *           
	 */
	public void loadMap(int resourceId) {
		mapBitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);		
		drawSurfaceView();
	}

	/**
	 * 加载定位图标
	 * 
	 * @param resourceId
	 */
	public void loadPositionLogo(int resourceId) {
		posBitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);
		drawSurfaceView();
	}

	/**
	 * 加载背景图
	 */
	public void loadBackGround(int resourceId) {
		backgroundBitmap = BitmapFactory.decodeResource(this.getResources(), resourceId);
		drawSurfaceView();
	}

	/**
	 * ����ǰλ�ã��ƶ���SurfaceView���м�
	 */
	public void movePositionToCenter() {
		if (mapBitmap == null)
			return;
		endP.x = (mapBitmap.getWidth() / 2 - myPosition.x) * curRate;
		endP.y = (mapBitmap.getHeight() / 2 - myPosition.y) * curRate;
		mapCenterP.set(mapCenterP.x * curRate + (endP.x - startP.x), mapCenterP.y * curRate + (endP.y - startP.y));
		startP.set(endP.x, endP.y);
		drawSurfaceView();
	}

	/**
	 * ���Surface�ϵ�����ͼ��
	 */
	public void clearAllMap() {
		if (mapBitmap != null) {
			mapBitmap.recycle();
		}
		mapBitmap = null;
		clearLayer();
		myPosition = new PointF(-1.0f, -1.0f);
		drawSurfaceView();
	}
	
	/**
	 * ���Surface�ϵ�ͼ�㣬�����ͼ
	 */
	public void clearLayer() {
		if (posBitmap != null) {
			posBitmap.recycle();
		}
		posBitmap = null;

		drawSurfaceView();
	}

	/**
	 * �ڵ�ͼ�������ϻ���λ�õ�
	 */
	public void drawPosition(double pos_x, double pos_y) {
		myPosition.set((float) pos_x, (float) pos_y);
		drawSurfaceView();
	}

	/**
	 * ��ʼ����SurfaceView
	 */
	public void drawSurfaceView() {
		
		//logVariable();
//		new Thread(new Runnable() {			
//			@Override
//			public void run() {
				try {
					mCanvas = surfaceHolder.lockCanvas();
					if (mCanvas != null) {			
						// 
						if (backgroundBitmap != null) {
							mCanvas.drawBitmap(backgroundBitmap,
									new Rect(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight()),
									new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight()), paint);
						} else {
							mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
						}
						Matrix matrix = new Matrix();
						// 
						if (mapBitmap != null) {
							matrix.setScale(curRate, curRate, mapBitmap.getWidth() / 2.0f, mapBitmap.getHeight() / 2.0f);

							matrix.postTranslate(mapCenterP.x + (endP.x - startP.x) - mapBitmap.getWidth() / 2.0f,
									mapCenterP.y + (endP.y - startP.y) - mapBitmap.getHeight() / 2.0f);							
							
							markPoint.set((CenterP.x-((mapCenterP.x + (endP.x - startP.x)-surfaceView.getWidth() / 2.0f)/curRate)),
									(CenterP.y-((mapCenterP.y + (endP.y - startP.y)-surfaceView.getHeight() / 2.0f))/curRate));
							
							mCanvas.drawBitmap(mapBitmap, matrix, paint);
						}
						// 画定位图标
						matrix.setScale(1.0f, 1.0f);
						if (posBitmap != null) {
							if (myPosition.x != -1.0f || myPosition.y != -1.0f) {
								float tmpx = 0.0f, tmpy = 0.0f;
								tmpx = mapCenterP.x + (endP.x - startP.x);
								tmpy = mapCenterP.y + (endP.y - startP.y);
								tmpx -= ((double) posBitmap.getWidth()) / 2.0f;
								tmpy -= ((double) posBitmap.getHeight()) / 2.0f;
								tmpx = tmpx - mapBitmap.getWidth() * curRate / 2.0f + myPosition.x * curRate;
								tmpy = tmpy - mapBitmap.getHeight() * curRate / 2.0f + myPosition.y * curRate;

								matrix.postTranslate(tmpx, tmpy);
								mCanvas.drawBitmap(posBitmap, matrix, paint);
								
							}
						}
//   					点击处在地图上的坐标	
//						 Paint p = new Paint(); // 创建一个画笔对象 
//					     p.setColor(Color.BLACK); // 设置画笔的颜色为白色 
//						mCanvas.drawCircle(touchPoint.x,
//								touchPoint.y, 10, p);
//						System.out.println("触摸点图像x坐标"+((touchPoint.x-(move.x+(surfaceView.getWidth()/2.0f)+endP.x - startP.x))+(mapwh.x*curRate)
//								)/curRate);
//						System.out.println("触摸点图像y坐标"+((touchPoint.y-(move.y+(surfaceView.getHeight()/2.0f)+endP.x - startP.x))+(mapwh.y*curRate)
//								)/curRate);
//						
						surfaceHolder.unlockCanvasAndPost(mCanvas);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
				}
				
	//		}
	//	}).start();
	
	}

	/**
	 * ���������ֵ
	 */
	private void logVariable() {
		if (!DEBUG)
			return;
		StringBuilder sb = new StringBuilder();
		sb.append("mapCenterP:" + getString(mapCenterP.x) + "," + getString(mapCenterP.y) + " ");
		sb.append("startP:" + getString(startP.x) + "," + getString(startP.y) + " ");
		sb.append("endP:" + getString(endP.x) + "," + getString(endP.y) + " ");
		sb.append("rate:" + getString(curRate) + " ");
		sb.append("myPosition:" + getString(myPosition.x) + "," + getString(myPosition.y) + " ");
		if (mapBitmap != null) {
			sb.append("mapBitmap.size:" + mapBitmap.getWidth() + "," + mapBitmap.getHeight() + " ");
		}
		if (posBitmap != null) {
			sb.append("posBitmap.size:" + posBitmap.getWidth() + "," + posBitmap.getHeight() + " ");
		}
		Log.w(TAG, sb.toString());
	}

	private String getString(double num) {
		return String.format("%.2f", num);
	}

	/**
	 * 计算距离
	 */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

}
