/**** efw4.X Copyright 2019 efwGrp ****/
package efw.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.TagSupport;

import efw.framework;

/**
 * ElFinderタグを処理するクラス。
 * &lt;efw:ElFinder home="myHomeFolder" readonly="false" protected="true" selection="file1" height="400" width="800"/&gt;<br>
 * &lt;efw:ElFinder home="c:/myHomeFolder" isAbs="true" /&gt;<br>
 * &lt;efw:ElFinder home="myHomeFolder" appurl="subAppUrl" /&gt;<br>
 * @author Chang Kejun
 *
 */
public final class ElFinder extends TagSupport implements DynamicAttributes {
	
	private String id="elFinder";
	private String home="";
	private boolean isAbs=false;
	private String selection="";
	private boolean readonly=false;
	private String height="400";
	private String width="auto";
	private boolean _protected=false;
	private String appurl="";
	/**
	 * ElFinderのIDを取得する。
	 * @return ElFinderのID。
	 */
	public String getId() {
		return id;
	}
	/**
	 * ElFinderのIDを設定する。
	 * @param id ElFinderのID。
	 */
	public void setId(String id) {
		this.id = Util.translateAttr(pageContext,id);
	}
	/**
	 * ホームフォルダを取得する。
	 * @return ホームフォルダ。
	 */
	public String getHome() {
		return home;
	}
	/**
	 * ホームフォルダを設定する。
	 * @param home ホームフォルダ。
	 */
	public void setHome(String home) {
		this.home = Util.translateAttr(pageContext,home);
	}
	/**
	 * 絶対パスフラグを取得する。
	 * @return 絶対パスフラグ。
	 */
	public String getIsAbs() {
		return ""+isAbs;
	}
	/**
	 * 絶対パスフラグを設定する。
	 * @param isAbs 絶対パスフラグ。
	 */
	public void setIsAbs(String isAbs) {
		this.isAbs = "true".equals(Util.translateAttr(pageContext,isAbs));
	}
	/**
	 * 選択項目を取得する。
	 * @return 選択項目。
	 */
	public String getSelection() {
		return selection;
	}
	/**
	 * 選択項目を設定する。
	 * @param selection 選択項目。
	 */
	public void setSelection(String selection) {
		this.selection = Util.translateAttr(pageContext,selection);
	}
	/**
	 * 読取り専用フラグを取得する。
	 * @return 読取り専用フラグ。
	 */
	public String getReadonly() {
		return ""+readonly;
	}
	/**
	 * 読取り専用フラグを設定する。
	 * @param readonly 読取り専用フラグ。
	 */
	public void setReadonly(String readonly) {
		if ("true".equalsIgnoreCase(Util.translateAttr(pageContext,readonly))) {
			this.readonly=true;
		}else {
			this.readonly=false;
		}
	}
	/**
	 * 高さを取得する。
	 * @return 高さ。
	 */
	public String getHeight() {
		return height;
	}
	/**
	 * 高さを設定する。
	 * @param height 高さ。
	 */
	public void setHeight(String height) {
		this.height = Util.translateAttr(pageContext,height);
	}
	/**
	 * 幅を取得する。
	 * @return 幅。
	 */
	public String getWidth() {
		return width;
	}
	/**
	 * 幅を設定する。
	 * @param width 幅。
	 */
	public void setWidth(String width) {
		this.width = Util.translateAttr(pageContext,width);
	}
	/**
	 * 保護モードフラグを取得する。
	 * @return 保護モードフラグ。
	 */
	public String isProtected() {
		return ""+_protected;
	}
	/**
	 * 保護モードフラグを設定する。
	 * @param _protected 保護モードフラグ。
	 */
	public void setProtected(String _protected) {
		if ("true".equalsIgnoreCase(Util.translateAttr(pageContext,_protected))) {
			this._protected=true;
		}else {
			this._protected=false;
		}
	}
	/**
	 * サブアプリURLを取得する。
	 * @return サブアプリURL。
	 */
	public String getAppurl() {
		return appurl;
	}
	/**
	 * サブアプリURLを設定する。
	 * @param appurl サブアプリURL。
	 */
	public void setAppurl(String appurl) {
		this.appurl = Util.translateAttr(pageContext,appurl);
	}

	private HashMap<String, String> attrs=new HashMap<String, String>();
	
	private static void _init(String id,String home,boolean isAbs,boolean readonly,boolean _protected,HttpServletRequest req) {
		req.getSession().setAttribute("EFW_ELFINDER_PROTECTED_"+id, _protected?"true":"false");
		req.getSession().setAttribute("EFW_ELFINDER_HOME_"+id, home);
		req.getSession().setAttribute("EFW_ELFINDER_ISABS_"+id, (isAbs?"true":"false"));
		req.getSession().setAttribute("EFW_ELFINDER_READONLY_"+id,(readonly?"true":"false"));
	}

	/**
	 * タグを実行する。
	 * @return SKIP_BODY。
	 */
	@Override
	public int doStartTag(){
		JspWriter out;
		String lang=(String) pageContext.getAttribute(Client.EFW_I18N_LANG,PageContext.REQUEST_SCOPE);
		if ("".equals(lang)||lang==null)lang="en";
		try {
			String v=framework.version;
			boolean asMain=false;
			//メイン部品と実行する場合
			if ("".equals(appurl)) {
				asMain=true;
			}else {
				//サブ部品実行、サブpartにelfinderのappurl属性を設定する想定。
				asMain=false;
			}
			out = pageContext.getOut();
			out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"elfinder/css/elfinder.min.css?v="+v+"\">");
			out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"elfinder/css/theme.css?v="+v+"\">");
			//out.print("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"elfinder/js/elfinder4efw.full.js?v="+v+"\"></script>");
			out.print("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"elfinder/js/elfinder4efw.min.js?v="+v+"\"></script>");
			out.print("<script type=\"text/javascript\" charset=\"UTF-8\" src=\"elfinder/js/elfinder.messages.jsp?lang="+lang+"&v="+v+"\"></script>");
			out.print("<script type=\"text/javascript\" charset=\"UTF-8\">");
			out.print("var "+id+";$(function(){"+id+"=$(\"#"+id+"\")"
					+ ".elfinder({"
					+"\"url\":"+(asMain?"efw.baseurl+\"":"\""+appurl)+"/efwServlet\","
					+"\"urlUpload\":"+(asMain?"efw.baseurl+\"":"\""+appurl)+"/uploadServlet\","
					+"\"soundPath\":"+(asMain?"efw.baseurl+\"":"\""+appurl)+"/elfinder/sounds\","
					+ "\"appurl\":"+(asMain?"efw.baseurl":"\""+appurl+"\"")+","
					+"\"requestType\":\"POST\","
					+"\"lang\":\""+lang+"\","
					+"\"height\":\""+height+"\","
					+"\"width\":\""+width+"\","
					+ "\"customData\":{"
					+ "\"home\":\""+jsEncode(home)+"\","
					+ "\"isAbs\":"+isAbs+","
					+ "\"selection\":\""+jsEncode(selection)+"\","
					+ "\"readonly\":"+readonly+","
					+ "\"id\":\""+id+"\","
					+ "}"
					+ "}).elfinder(\"instance\");});");
			out.print("</script>");
			String temp="";
			for(Map.Entry<String, String> e : attrs.entrySet()) {
				temp+=e.getKey()+"=\""+e.getValue()+"\" ";
			}
			out.print("<div "+"id=\""+id+"\" "+temp+"></div>");
			
			ElFinder._init(id,home,isAbs,readonly, _protected,(HttpServletRequest)this.pageContext.getRequest());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		//初期値を再設定する。
		id="elFinder";
		home="";
		isAbs=false;
		selection="";
		readonly=false;
		height="400";
		width="auto";
		_protected=false;
		appurl="";
		attrs=new HashMap<String, String>();
		return SKIP_BODY;
	}

	/**
	 * 動的パラメータを取得する。
	 * @param uri 名称空間。
	 * @param name 属性名。
	 * @param value 属性値。
	 */
	@Override
	public void setDynamicAttribute(String uri, String name, Object value)
			throws JspException {
		attrs.put(name, Util.translateAttr(pageContext,(String)value));
	}
	
	/**
	 * javascript escape機能を実装する
	 * @param v
	 * @return
	 */
	private String jsEncode(String v) {
		if (v==null) {
			return v;
		}else {
			return v.replaceAll("[\\\\]", "\\\\\\\\").replaceAll("[']","\\\\'").replaceAll("[\"]","\\\\\"");
		}
	}
}
