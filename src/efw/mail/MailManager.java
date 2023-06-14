/**** efw4.X Copyright 2019 efwGrp ****/
package efw.mail;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import efw.XMLFileIsNotExistsException;
import efw.XMLFileIsNotLegalException;
import efw.XMLTagIdIsDuplicateException;
import efw.XMLTagIdIsNotExistsException;
import efw.XMLTagIsNotLegalException;
import efw.efwException;
import efw.framework;
import efw.properties.PropertiesManager;

/**
 * MailテンプレートXMLを管理するクラス。
 * @author Chang Kejun
 *
 */
public final class MailManager {
	/**
	 * ネーミング操作の開始コンテキストの名称。
	 * 「java:comp/env」に固定。
	 */
	private static final String JAVA_INITCONTEXT_NAME="java:comp/env";
    /**
     * javaメールセッション。
     */
    private static Session mailSession;
    /**
     * サーブレットから設定情報を受け取る。
     * @throws efwException　MailテンプレートXMLファイルの読み取りエラー。
     */
	public static void init() throws NamingException{
        if(framework.getMailResourceName().indexOf("java:")>-1){//if the mail resouce begins from [java:], it is full jndi name.
        	mailSession = (Session) new InitialContext().lookup(framework.getMailResourceName());
        }else{//or it begins by [java:comp/env/]
        	mailSession = (Session) new InitialContext().lookup(JAVA_INITCONTEXT_NAME+"/"+framework.getMailResourceName());
        }
	}
	public static void initFromBatch(String mailFolder) throws efwException{
    	String username=PropertiesManager.EFW_MAIL_USERNAME;
    	String password=PropertiesManager.EFW_MAIL_PASSWORD;
		String usernameValue=PropertiesManager.getProperty(username, "");
		String passwordValue=PropertiesManager.getProperty(password, "");
		mailSession=Session.getInstance(PropertiesManager.getProp(),new MailAuthenticator(usernameValue,passwordValue));
	}
	/**
	 * メールを送信
	 * @param mailId
	 * @param params
	 * @throws efwException 
	 */
	public static void send(String groupId,String mailId,Map<String,String> params) throws efwException,AddressException,MessagingException{
		Mail mail=get(groupId,mailId);
		MimeMessage message = new MimeMessage(mailSession);
		
		String to=mail.getTo(params);
		if (to!=null&&!"".equals(to)){
			String[] ary=to.split(";");
			for(int i=0;i<ary.length;i++){
				if(!"".equals(ary[i]))message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(ary[i]));
			}
		}
		String cc=mail.getCc(params);
		if (cc!=null&&!"".equals(cc)){
			String[] ary=cc.split(";");
			for(int i=0;i<ary.length;i++){
				if(!"".equals(ary[i]))message.addRecipient(MimeMessage.RecipientType.CC, new InternetAddress(ary[i]));
			}
		}
		String bcc=mail.getBcc(params);
		if (bcc!=null&&!"".equals(bcc)){
			String[] ary=bcc.split(";");
			for(int i=0;i<ary.length;i++){
				if(!"".equals(ary[i]))message.addRecipient(MimeMessage.RecipientType.BCC, new InternetAddress(ary[i]));
			}
		}
		String subject=mail.getSubject(params);
		if (subject!=null&&!"".equals(subject)){
			message.setSubject(subject,framework.SYSTEM_CHAR_SET);
		}
		String body=mail.getBody(params);
		if (body!=null&&!"".equals(body)){
			message.setContent(body,"text/plain;charset=UTF-8");//text/html;charset=UTF-8
		}
		String from=mail.getFrom(params);
		if (from!=null&&!"".equals(from)){
			message.setFrom(new InternetAddress(from));
		}else{
			message.setFrom();
		}
		String mdn=mail.getMdn(params);
		if (mdn!=null&&!"".equals(mdn)){
			message.addHeader("Disposition-Notification-To",mdn);
		}
		
		Transport.send(message);
	}
	/**
	 * ひとつのMailオブジェクトを取得する。 
	 * デバッグモードの場合、最終更新日時により再ロードするか否か判断する。
	 * 通常モードの場合、予めロード済みデータから、Sqlオブジェクトを探す。
	 * @param groupId MailテンプレートXMLファイルのファイル名（拡張子を除く）。
	 * @param mailId　mailタグに定義するid。
	 * @return　Mailオブジェクト。
	 * @throws efwException　Mail外部化XMLファイルの定義エラーか、存在しないエラーか。
	 */
	private static Mail get(String groupId,String mailId) throws efwException{
		//get group
		MailHashMap group;
		if ((group=groups.get(groupId))==null){
			synchronized(groups){
				if ((group=groups.get(groupId))==null){
					load(groupId);
					group=groups.get(groupId);
				}
			}
		}else if (framework.getIsDebug()){
			if (checkModifyTime(groupId)){
				synchronized(groups){
					if (checkModifyTime(groupId)){
						groups.remove(groupId);
						load(groupId);
						group=groups.get(groupId);
					}
				}
			}
		}
		//if group is not exists, it is wrong group id
		if (group==null){
			throw new XMLFileIsNotExistsException("mail",groupId);
		}else{
			//get mail
			Mail mail=group.get(mailId);
			//if mail is not exists, it is wrong mail id
			if(mail==null){
				throw new XMLTagIdIsNotExistsException("mail",groupId,mailId);
			}else{
				return mail;
			}
		}
	}
	/**
	 * 予めロード済みデータのMailオブジェクトの最終更新日時は、実ファイルと同じか否かをチェックする。
	 * @param groupId MailテンプレートXMLファイルのファイル名（拡張子を除く）。
	 * @return 最終更新日時が変更なしの場合 true　。
	 */
	private static boolean checkModifyTime(String groupId){
		MailHashMap group=groups.get(groupId);
		if (group==null){
			return true;//xml file is not in memory,so it is need to reload
		}else{
			Date fileLastModifytime = new Date(new File(framework.getMailFolder()+"/"+groupId+".xml").lastModified());
			if(!fileLastModifytime.equals(group.getLastModifytime())){
				return true;//xml file is modified, so it is need to reload
			}else{
				return false;//xml file is not modified
			}
		}
	}
	/**
	 * MailテンプレートXMLファイルのファイル名によりロードする。
	 * @param groupId MailテンプレートXMLファイルのファイル名（拡張子を除く）。
	 */
	///////////////////////////////////////////////////////////////////////////
	private static void load(String groupId) throws efwException{
		String filename=framework.getMailFolder()+"/"+groupId+".xml";
		File fl=new File(filename);
		if (!fl.exists()) return;//ファイルが存在しない場合、なにもしない。
		
		Date lastModifytime=new Date(fl.lastModified());
		MailHashMap group=new MailHashMap();
		group.setLastModifytime(lastModifytime);
		//add a new map by file name in aryData 
		groups.put(groupId,group);
		//read xml to get Mails 
		NodeList mails;
		try {
			mails = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(fl)
								.getDocumentElement()
								.getElementsByTagName("mail");
		} catch (SAXException e) {
			throw new XMLFileIsNotLegalException("mail",groupId,e.getMessage());
		} catch (IOException e) {
			throw new XMLFileIsNotLegalException("mail",groupId,e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new XMLFileIsNotLegalException("mail",groupId,e.getMessage());
		}
		//get sql from element
		for(int i=0;i<mails.getLength();i++){
			Node node = mails.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE){
				Element element= (Element)node;
				String mailId=element.getAttribute("id");
				if (group.get(mailId)==null){
					try {
						group.put(mailId, new Mail(element));
					}catch(Exception e) {
						throw new XMLTagIsNotLegalException("mail",groupId,mailId,e.getMessage());
					}
				}else{
					throw new XMLTagIdIsDuplicateException("mail",groupId,mailId);
				}
			}
		}
	}
	/**
	 * ロードするMailテンプレートXMLファイルを格納するオブジェクト。
	 */
	private static final HashMap<String,MailHashMap> groups=new HashMap<String,MailHashMap>();

}
