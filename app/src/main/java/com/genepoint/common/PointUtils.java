package com.genepoint.common;

import com.genepoint.common.LogDebug;
import com.navinfo.nimapapi.geometry.GeoPoint;

/**��γ����ī�������껻��
 * @author jsj
 *
 */
public class PointUtils {
	private double x;//����
	private double y;//γ��
	public PointUtils(double x,double y) {
		this.x=x;
		this.y=y;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * ��γ��תī����
	 */
	  public static GeoPoint LonLat2Mercator(PointUtils c)
      {
          double x = c.getX() * 20037508.34 / 180;
          double y = Math.log(Math.tan((90 + c.getY()) * Math.PI / 360)) / (Math.PI / 180);
          y = y * 20037508.34 / 180;
          GeoPoint geoPoint=new GeoPoint();
          geoPoint.setX(x);
          geoPoint.setY(y);
          return geoPoint;
      }
	  
	  /**
	   * ī����ת��γ��
	   */
      public static PointUtils Mercator2LonLat(GeoPoint c)
      {
          double x = c.getX() / 20037508.34 * 180;
          double y = c.getY() / 20037508.34 * 180;
          y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
         return new PointUtils(x, y);
      }
      /**
     * @param geoPoint jpgͼ���ϽǶ�Ӧ��ī��������
     * @param width  1�����ش����ī���п����
     * @param height 1�����ش����ī���и�
     */
    public static PointUtils pix2LonLat(GeoPoint geoPoint ,double width,double height){
    	//�����1�����ص�ī��������
    	  GeoPoint pixgeoPoint=new GeoPoint();
    	  pixgeoPoint.setX(geoPoint.getX()+width);
    	  pixgeoPoint.setY(geoPoint.getY()+height);
    	  //ת��Ϊ��γ������
    	  PointUtils point=Mercator2LonLat(geoPoint);
    	  PointUtils pixlonLat=Mercator2LonLat(pixgeoPoint);
    	  double widthPerPixel=pixlonLat.getX()-point.getX();
    	  double heightPerPixel=pixlonLat.getY()-point.getY();
    	  
    	  LogDebug.w("PL", "���صľ�γ�ȿ��Ϊ��"+widthPerPixel+"________"+heightPerPixel);
    	  return new PointUtils(widthPerPixel,heightPerPixel);
      }


}
