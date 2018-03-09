package com.yw.app;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.yw.bean.Lover;
import com.yw.bean.Matchmaker;
import com.yw.bean.Message;
import com.yw.bean.TextMeaasge;
import com.yw.bean.Trouble;
import com.yw.bean.UserInfo;
import com.yw.utils.CheckUtil;
import com.yw.utils.DatetimeUtil;
import com.yw.utils.MessageUtil;
import com.yw.utils.StringCheckUtil;

/**
 * Created by Administrator on 2017/6/20.
 */
@RestController
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.yw"})
public class Application implements EmbeddedServletContainerCustomizer {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public void homeGet(String signature, String timestamp, String nonce,
			String echostr, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PrintWriter out = response.getWriter();
		if (CheckUtil.checkSignature(signature, timestamp, nonce)) {
			out.print(echostr); // 校验通过，原样返回echostr参数内容
		}
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public void homePost(HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("utf-8");
        response.setContentType("text/xml;charset=utf-8");
        PrintWriter out = response.getWriter();
        try {
            Map<String, String> map = MessageUtil.xmlToMap(request);
            String toUserName = map.get("ToUserName");
            String fromUserName = map.get("FromUserName");
            String msgType = map.get("MsgType");
            String content = map.get("Content");
            String message = null;
            
            boolean userRegister=true;
            if(!userInfoCheck(fromUserName)){
            	userRegister=false;
            }
            
            UserInfo userInfo=new UserInfo();
            
            if ("text".equals(msgType)) {
            	content=content.trim();
            	int lastStringCount=jdbcTemplate.queryForObject("select count(user_name) from laststring where user_name='"+fromUserName+"'",Integer.class );
            	if(lastStringCount==0){
            		// 记录信息流
                	jdbcTemplate.execute("insert into laststring values('"+fromUserName+"','开始',now())");
            	}
            	String lastString=jdbcTemplate.queryForObject("select last_string from laststring where user_name='"+fromUserName+"'",String.class );
            	if(userRegister){
            		userInfo=getUserInfo(fromUserName);
            	}
            	
            	if(userInfo.getIswarn()==2){
            		message = warnInfo(userInfo.getWarn(), toUserName, fromUserName);
            	}else if((userInfo.getWarn()>2&&userInfo.getIswarn()==0)||(userInfo.getWarn()>5&&userInfo.getIswarn()==1)){
            		message = warnInfo(userInfo.getWarn(), toUserName, fromUserName);
            		jdbcTemplate.update("UPDATE userinfo SET iswarn=iswarn+1 where user_name='"+fromUserName+"'");
            		if(userInfo.getWarn()>5&&userInfo.getIswarn()>=1){
            			jdbcTemplate.execute("insert into blacklist(user_name) values('"+fromUserName+"')");
            		}
            	}else if("退出".equals(content)){
            		// 记录信息流
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
                    message = exit(toUserName, fromUserName);
            		System.out.println(message);   
            	}else if("帮助".equals(content)){
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            		message = help(toUserName, fromUserName);
            		System.out.println(message);
            	}else if("给公众号留言".equals(content)){
            		jdbcTemplate.execute("update laststring set last_string='给公众号留言',time_occur=now() where user_name='"+fromUserName+"'");
            		message = gzhMessageAsk(toUserName, fromUserName);
            		System.out.println(message);
            	}else if("给公众号留言".equals(lastString)){
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            		message = gzhMessage(content ,toUserName, fromUserName);
            		System.out.println(message);
            	}else if("举报".equals(content)){
            		// 记录信息流
            		jdbcTemplate.execute("update laststring set last_string='举报',time_occur=now() where user_name='"+fromUserName+"'");
                    message = report(toUserName, fromUserName);
            		System.out.println(message);
            	}else if("举报".equals(lastString)){
            		try {
            			String warnUserName=jdbcTemplate.queryForObject("select user_name from userinfo where another_name='"+content+"'", String.class);
            			message = reportSuccessful(warnUserName, toUserName, fromUserName);
					} catch (Exception e) {
						message = reportFailed(toUserName, fromUserName);
					}
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            	}else if("资料卡".equals(content)){
            		if(!userRegister){
            			// 记录信息流
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
            			// 记录信息流
                		jdbcTemplate.execute("update laststring set last_string='资料卡确认',time_occur=now() where user_name='"+fromUserName+"'");
            			message = userInfoChangeAskFirst(toUserName, fromUserName);
                        System.out.println(message);
            		}  
            	}else if("资料卡".equals(lastString)){
            		//检查content格式
            		if(content.replaceAll("[\\s]", "").matches("^[^,，\\s]{1,}[,，][^,，\\s]{1,}[,，][男|女][,，]\\d{1,}[,，][\u4E00-\u9FA5]{2,}$")){
            			// 记录信息流
            			jdbcTemplate.execute("update laststring set last_string='资料卡确认:"+content+"',time_occur=now() where user_name='"+fromUserName+"'");
            			message = userInfoConfirmAsk(content, toUserName, fromUserName);
            			System.out.println(message);   
            		}else{
            			message =  userInfoPatternFailed(toUserName, fromUserName);
            			System.out.println(message);
            		}
            	}else if(lastString.startsWith("资料卡确认")){
            		if(content.contains("确认")){
            			// 记录信息流
            			message = userInfoConfirmYes(userRegister,toUserName, fromUserName);
            			System.out.println(message);   
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            		}else if(content.contains("修改")){
            			jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
            			message = userInfoChange(toUserName, fromUserName);
            			System.out.println(message);   
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = errorInput(toUserName, fromUserName);
            			System.out.println(message);
            		}
            	}else if("解忧".equals(content)){
            		if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
            			// 解忧选择
                		jdbcTemplate.execute("update laststring set last_string='解忧选择',time_occur=now() where user_name='"+fromUserName+"'");
                        message = allayChoose(toUserName, fromUserName);
                        System.out.println(message);
            		}
            	}else if("解忧选择".equals(lastString)){
            		if("烦恼".equals(content)){
            			int annoyanceUnAllay=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM trouble WHERE user_name='"+fromUserName+"' AND counts<2", Integer.class);
            			if(annoyanceUnAllay>=2){
            				message = annoyanceRefuse(toUserName, fromUserName);
                            System.out.println(message);
            			}else{
                    		jdbcTemplate.execute("update laststring set last_string='烦恼',time_occur=now() where user_name='"+fromUserName+"'");
                            message = annoyance(toUserName, fromUserName);
                            System.out.println(message); 
            			}
            		}else if("解答".equals(content)){
                        message = allay(lastString, toUserName, fromUserName);
                        System.out.println(message);
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = errorInput(toUserName, fromUserName);
            			System.out.println(message);
            		}
            	}else if(lastString.startsWith("放弃烦恼")){
            		if("1".equals(content)||"2".equals(content)){
            			message = annoyanceGiveUp(content, toUserName, fromUserName);
            			System.out.println(message);
            		}else{
            			message = errorInput(toUserName, fromUserName);
            			System.out.println(message);
            		}
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            	}else if("烦恼".equals(lastString)){
            		jdbcTemplate.execute("update laststring set last_string='烦恼:" + content +"',time_occur=now() where user_name='"+fromUserName+"'");
                    message = annoyanceAsk(content, toUserName, fromUserName);
                    System.out.println(message);
            	}else if(lastString.startsWith("烦恼:")){
            		if("确认".equals(content)){
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = annoyanceYes(lastString,toUserName, fromUserName);
            			System.out.println(message);
            		}else if("修改".equals(content)){
            			jdbcTemplate.execute("update laststring set last_string='烦恼',time_occur=now() where user_name='"+fromUserName+"'");
            			message = annoyanceNo(toUserName, fromUserName);
            			System.out.println(message);
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = errorInput(toUserName, fromUserName);
            			System.out.println(message);
            		}
            	}else if(lastString.startsWith("解答:")){
        			if("回复".equals(content)){
        				jdbcTemplate.execute("update laststring set last_string='解忧回复:"+lastString.substring(lastString.indexOf(":")+1)+"',time_occur=now() where user_name='"+fromUserName+"'");
            			message = allayYes(toUserName, fromUserName);
            			System.out.println(message);
        			}else if("下一条".equals(content)){
        				message = allay(lastString, toUserName, fromUserName);
            			System.out.println(message);
        			}else{
        				jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = errorInput(toUserName, fromUserName);
            			System.out.println(message);
        			}
        		}else if(lastString.startsWith("解忧回复:")){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = allayInput(content, lastString, toUserName, fromUserName);
        			System.out.println(message);
        		}else if("情话".equals(content)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = loving(toUserName, fromUserName);
        			System.out.println(message);
        		}else if("发布情话".equals(content)){
        			if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='发布情话',time_occur=now() where user_name='"+fromUserName+"'");
            			message = publishLovingAsk(toUserName, fromUserName);
            			System.out.println(message);
            		}
        		}else if("发布情话".equals(lastString)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = publishLoving(content, toUserName, fromUserName);
        			System.out.println(message);
        		}else if("寄信".equals(content)){
        			if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
	        			message = letter(toUserName, fromUserName);
	        			System.out.println(message);
            		}
        		}else if("写信".equals(content)){
        			if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
                		jdbcTemplate.execute("update laststring set last_string='写信',time_occur=now() where user_name='"+fromUserName+"'");
	        			message = writeLettersAsk(toUserName, fromUserName);
	        			System.out.println(message);
            		}
        		}else if("写信".equals(lastString)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = writeLetters(content,toUserName, fromUserName);
        			System.out.println(message);
        		}else if("日记本".equals(content)){
        			if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            			message = note(toUserName, fromUserName);
            			System.out.println(message);
            		}
        		}else if("显示微信".equals(content)){
        			if(!userRegister){
            			// 检验资料卡
                		jdbcTemplate.execute("update laststring set last_string='资料卡',time_occur=now() where user_name='"+fromUserName+"'");
                        message = userInfoAsk(toUserName, fromUserName);
                        System.out.println(message); 
            		}else{
            			jdbcTemplate.execute("update laststring set last_string='显示微信',time_occur=now() where user_name='"+fromUserName+"'");
            			message = displayWechatAsk(toUserName, fromUserName);
            			System.out.println(message);
            		}
        		}else if("显示微信".equals(lastString)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = displayWechat(content, toUserName, fromUserName);
        			System.out.println(message);
        		}else if("信铺".equals(content)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = letterShop( toUserName, fromUserName);
        			System.out.println(message);
        		}else if("投稿".equals(content)){
        			jdbcTemplate.execute("update laststring set last_string='投稿',time_occur=now() where user_name='"+fromUserName+"'");
        			message = contributeAsk( toUserName, fromUserName);
        			System.out.println(message);
        		}else if("投稿".equals(lastString)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = contribute(content, toUserName, fromUserName);
        			System.out.println(message);
        		}else if("人工".equals(content)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = customer(toUserName, fromUserName);
        			System.out.println(message);
        		}else if("群聊".equals(content)){
        			jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        			message = talk(toUserName, fromUserName);
        			System.out.println(message);
        		}else{
            		//被动回复
            		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            		message = elseReply(toUserName, fromUserName);
                    System.out.println(message);
            	}
            }
            
            //关注
            if ("event".equals(msgType)&&"subscribe".equals(map.get("Event"))) {
            	jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            	// 用户关注公众号触发回复
        		if(userRegister){
        			jdbcTemplate.update("update userinfo set isdelete=0 where user_name='" +fromUserName+ "'");
        		}
                message = subscribe(toUserName, fromUserName);
                System.out.println(message);            
            }
            
            //取消关注
            if ("event".equals(msgType)&&"unsubscribe".equals(map.get("Event"))){
            	jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            	if(userRegister){
            		jdbcTemplate.update("update userinfo set isdelete=1,deletetime=now() where user_name='" +fromUserName+ "'");
            	}
            	message = unSubscribe(toUserName, fromUserName);
                System.out.println(message);
            }
            
            if (!"text".equals(msgType)&&!"event".equals(msgType)){
            	jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
            	message = otherType(toUserName, fromUserName);
                System.out.println(message);
            }
            
            
            out.print(message);// 将回应发送给微信服务器
        } catch (DocumentException e) {
            e.printStackTrace();
        }finally{
            out.close();
        }
	}

	public String talk(String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("点击进入-><a href=\"https://jq.qq.com/?_wv=1027&k=521Sz7I\">信村表情包尬聊群</a>");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String customer(String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("点击进入-><a href=\"https://wpa.qq.com/msgrd?v=3&uin=1207400615&site=qq&menu=yes\">人工服务</a>");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String otherType(String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("暂时不接收“非文本”消息的处理QAQ，已返回最上层！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String contribute(String content, String toUserName, String fromUserName){
		int count=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM contribute WHERE user_name='"+fromUserName+"' AND DATE_FORMAT(time_occur,'%Y-%m-%d')=CURDATE()", Integer.class);
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		if(count>=2){
			text.setContent("为防止恶意投稿，单天最多投稿两次，见谅，已返回最上层!");
		}else{
			jdbcTemplate.execute("INSERT INTO contribute(user_name,details,time_occur) VALUES('"+fromUserName+"','"+content+"',NOW());");
			text.setContent("投稿成功，诚挚感谢，已返回最上层！");
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String contributeAsk(String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("公众号近期接收情感或者信件类的文章投稿，用于推送，请输入您要投稿的内容，防止恶意投稿，一天最多投稿两次，我们会进行排版处理。\n\n"
        		+ "温馨提示：任何情况下回复[退出]，可直接返回最上层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String letterShop(String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("点击进入-><a href=\"https://jxrwzj.kuaizhan.com/\">信铺</a>");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String displayWechat(String content, String toUserName, String fromUserName){
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		List<Matchmaker> matchmakerList=jdbcTemplate.query("SELECT a.id FROM matchmaker a "
				+ "LEFT JOIN userinfo b ON a.to_username=b.user_name WHERE a.user_name='"+fromUserName+"' AND b.another_name='"+content.trim()+"' AND interaction>=5 and agree=0", new RowMapper<Matchmaker>(){
			@Override
			public Matchmaker mapRow(ResultSet rs, int arg1)
					throws SQLException {
				Matchmaker matchmaker=new Matchmaker();
				matchmaker.setId(rs.getLong("id"));
				return matchmaker;
			}
		});
		if(matchmakerList.size()==0){
			text.setContent("您输入的别名不存在或者不在[日记本]满足条件的列表里，已返回最上层！");
		}else{
			jdbcTemplate.update("update matchmaker set agree=1 where id="+matchmakerList.get(0).getId());
			text.setContent("向用户["+content.trim()+"]暴露成功，如果对方也向你暴露了微信号，将可以在[日记本]中查看到对方的微信号，已返回最上层！");
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String displayWechatAsk(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入你要暴露微的对方用户的别名，一次只能输入一个别名！\n\n"
        		+ "温馨提示：输入的别名必须存在于[日记本]达到条件的记录里，任何情况下回复[退出]，直接返回最上层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String note(String toUserName,String fromUserName){
		StringBuilder sb=new StringBuilder("近期交互中已经连续交互天数达到五天的好友：\n\n");
		List<Matchmaker> matchmakerList=jdbcTemplate.query("SELECT a.id,b.another_name,b.sex,b.age,b.province FROM matchmaker a "
				+ "LEFT JOIN userinfo b ON a.to_username=b.user_name WHERE a.user_name='"+fromUserName+"' AND interaction>=5 and agree=0 and displaycounts<=3", new RowMapper<Matchmaker>(){
			@Override
			public Matchmaker mapRow(ResultSet rs, int arg1)
					throws SQLException {
				Matchmaker matchmaker=new Matchmaker();
				matchmaker.setId(rs.getLong("id"));
				matchmaker.setAnotherName(rs.getString("another_name"));
				matchmaker.setSex(rs.getString("sex"));
				matchmaker.setAge(rs.getInt("age"));
				matchmaker.setProvince(rs.getString("province"));
				return matchmaker;
			}
		});
		if(matchmakerList.size()==0){
			sb.append("[暂未有好友达到要求]\n\n");
		}else{
			for(Matchmaker matchmaker:matchmakerList){
				sb.append("别名："+matchmaker.getAnotherName()+";\n"
						+ "性别："+matchmaker.getSex()+";\n"
						+ "年龄："+matchmaker.getAge()+";\n"
						+ "省份："+matchmaker.getProvince()+";\n\n");
				jdbcTemplate.update("update matchmaker set displaycounts=displaycounts+1 where id="+matchmaker.getId());
			}
			sb.append("回复[显示微信]，向好友暴露自己资料卡中填写的微信账号；\n\n");
		}
		sb.append("近期互相同意暴露微信账号的用户：\n\n");
		List<Matchmaker> matchmakerAgreeList=jdbcTemplate.query("SELECT a.id,c.another_name,c.sex,c.age,c.province,c.wechat_name FROM matchmaker a "
				+ "LEFT JOIN matchmaker b ON a.to_username=b.user_name LEFT JOIN userinfo c ON a.to_username=c.user_name "
				+ "WHERE a.user_name='"+fromUserName+"' AND a.agree=1 AND b.agree=1 AND a.counts<=3", new RowMapper<Matchmaker>(){
			@Override
			public Matchmaker mapRow(ResultSet rs, int arg1)
					throws SQLException {
				Matchmaker matchmaker=new Matchmaker();
				matchmaker.setId(rs.getLong("id"));
				matchmaker.setAnotherName(rs.getString("another_name"));
				matchmaker.setSex(rs.getString("sex"));
				matchmaker.setAge(rs.getInt("age"));
				matchmaker.setProvince(rs.getString("province"));
				matchmaker.setWechatName(rs.getString("wechat_name"));
				return matchmaker;
			}
		});
		if(matchmakerAgreeList.size()==0){
			sb.append("[暂未有互相同意暴露微信账号的用户]\n\n");
		}else{
			for(Matchmaker matchmaker:matchmakerAgreeList){
				sb.append("别名："+matchmaker.getAnotherName()+";\n"
						+ "性别："+matchmaker.getSex()+";\n"
						+ "年龄："+matchmaker.getAge()+";\n"
						+ "省份："+matchmaker.getProvince()+";\n"
						+ "微信号："+matchmaker.getWechatName()+";\n\n");
				jdbcTemplate.update("update matchmaker set counts=counts+1 where id="+matchmaker.getId());
			}
		}
		sb.append("温馨提示：系统会清除一周内不再交互的记录。");
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent(sb.toString());
        return MessageUtil.textMessageToXML(text);
	}
	
	public String writeLetters(String content,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		if(!content.matches("^[^,，\\s]{1,}[:：][\\s\\S]{1,}$")){
			text.setContent("格式不正确，已返回最上层！");
		}else{
			content=content.replaceFirst("：", ":");
			String anotherName=content.substring(0,content.indexOf(":"));
			String details=content.substring(content.indexOf(":")+1);
			List<UserInfo> userInfoList=jdbcTemplate.query("select user_name from userinfo where another_name='"+anotherName+"'",new RowMapper<UserInfo>(){
				@Override
				public UserInfo mapRow(ResultSet rs, int arg1)
						throws SQLException {
					UserInfo userInfo=new UserInfo();
					userInfo.setUserName(rs.getString("user_name"));
					return userInfo;
				}
			});
			if(userInfoList.size()>0){
				int letterCountFrom=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM message WHERE  from_user_name='"+fromUserName+"' AND to_user_name='"+userInfoList.get(0).getUserName()
						+"' AND  DATE_FORMAT(time_occur,'%Y-%m-%d')=CURDATE()", Integer.class);
				if(letterCountFrom<2){
					jdbcTemplate.execute("INSERT INTO message(from_user_name,to_user_name,details,time_occur)"
							+ " VALUES('"+fromUserName+"','"+userInfoList.get(0).getUserName()+"','"+details+"',NOW())");
					int letterCountTo=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM message WHERE  from_user_name='"+userInfoList.get(0).getUserName()+"' AND to_user_name='"+fromUserName
							+"' AND  DATE_FORMAT(time_occur,'%Y-%m-%d')=CURDATE()", Integer.class);
					if((letterCountFrom+letterCountTo)==0){
						int matchMakerCount=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM matchmaker WHERE user_name='"+fromUserName+
								"' AND to_username='"+userInfoList.get(0).getUserName()+"'", Integer.class);
						if(matchMakerCount==0){
							jdbcTemplate.execute("INSERT INTO matchmaker(user_name,to_username,interaction,time_occur) "
									+ " VALUES('"+fromUserName+"','"+userInfoList.get(0).getUserName()+"',1,now()),('"+userInfoList.get(0).getUserName()+"','"+fromUserName+"',1,now());");
						}else{
							jdbcTemplate.update("UPDATE matchmaker SET interaction=interaction+1,displaycounts=0 ,time_occur=now() WHERE (user_name='"+fromUserName+"' AND to_username='"+userInfoList.get(0).getUserName()
									+"') OR (user_name='"+userInfoList.get(0).getUserName()+"' AND to_username='"+fromUserName+"')");
						}
					}
					text.setContent("信件投递成功，最早将在次日送达对方邮箱，已返回最上层！");
				}else{
					text.setContent("单天最多只能给同一个用户写两封信件，已返回最上层！");
				}
			}else{
				text.setContent("别名不存在或对方已经更改，投递失败，已返回最上层！");
			}
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String writeLettersAsk(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入用户别名和信件内容，格式(别名:xxxxxx)！\n\n"
        		+ "举例：\n"
        		+ "run:我这里的天气变暖了，你那里呢？\n\n"
        		+ "温馨提示：今日投递的信件会在次日送达，单天最多只能给同一个用户写两封信件，任何情况下回复[退出]，直接返回最外层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String letter(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		List<Message> messageList=jdbcTemplate.query("SELECT a.id,b.another_name,b.sex,a.details,a.time_occur FROM message a LEFT JOIN userinfo b "
				+ "ON a.from_user_name=b.user_name WHERE a.to_counts<=2 and time_occur<CURDATE() and a.to_user_name='"+fromUserName+"'", new RowMapper<Message>(){
					@Override
					public Message mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Message message=new Message();
						message.setId(rs.getLong("id"));
						message.setAnotherName(rs.getString("another_name"));
						message.setSex(rs.getString("sex"));
						message.setDetails(rs.getString("details"));;
						message.setTimeOccur(rs.getDate("time_occur"));
						return message;
					}
				});
		StringBuffer sb=new StringBuffer("邮箱里新增的信件：\n\n");
		if(messageList.size()>0){
			for(Message message:messageList){
				sb.append(message.getDetails()+"\n"
						+ "——"+message.getAnotherName()+"，"+message.getSex()+"，"+ DatetimeUtil.getDateByPattern(message.getTimeOccur(), "yyyy-MM-dd")+"\n\n");
				jdbcTemplate.update("update message set to_counts=to_counts+1 where id="+message.getId());
			}
		}else{	
			sb.append("[暂未收取到信件！]\n\n");
		}
		
		List<Message> messageWriteList=jdbcTemplate.query("SELECT a.id,b.another_name,b.sex,a.details,a.time_occur,to_counts FROM message a LEFT JOIN userinfo b "
				+ "ON a.to_user_name=b.user_name WHERE a.from_counts<=2 and a.from_user_name='"+fromUserName+"'", new RowMapper<Message>(){
					@Override
					public Message mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Message message=new Message();
						message.setId(rs.getLong("id"));
						message.setAnotherName(rs.getString("another_name"));
						message.setSex(rs.getString("sex"));
						message.setDetails(rs.getString("details"));;
						message.setTimeOccur(rs.getDate("time_occur"));
						message.setCounts(rs.getInt("to_counts"));
						return message;
					}
				});
		sb.append("近期投递的信件：\n\n");
		if(messageWriteList.size()>0){
			for(Message message:messageWriteList){
				sb.append("写给:"+message.getAnotherName()+"，"+message.getSex()+"\n"
						+ "内容:"+message.getDetails()+"\n");
				jdbcTemplate.update("update message set from_counts=from_counts+1 where id="+message.getId()+ " AND time_occur<CURDATE() and to_counts>0");
				if(message.getCounts()==0){
					sb.append("状态:未查看\n\n");
				}else{
					sb.append("状态:已查看\n\n");
				}
			}
		}else{
			sb.append("[近期未投递任何信件！]\n\n");
		}
		
		sb.append("回复[写信]，给其他用户写一封信；\n"
				+ "回复[退出]，返回最上层；\n\n"
				+ "温馨提示：多在[解忧]、[情话]中活跃，或者主动给别的用户寄信，可能让你收取到更多信件。");
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		text.setContent(sb.toString());
        return MessageUtil.textMessageToXML(text);
	}
	
	public String publishLoving(String content, String toUserName,String fromUserName){
		jdbcTemplate.execute("INSERT INTO lover(user_name,details,time_occur) VALUES('"+fromUserName+"','"+content+"',now())");
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("发布成功！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String publishLovingAsk(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入您要发布的情话，您的有趣的漂流瓶子可能会得到其他用户的私信赞赏！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String loving(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		List<Lover> loverList=jdbcTemplate.query("SELECT t1.* FROM (SELECT b.id,c.another_name,c.sex,b.details FROM lover b LEFT JOIN userinfo c ON b.user_name=c.user_name) t1 "
				+ " JOIN (SELECT ROUND(RAND() * ((SELECT MAX(id) FROM lover)-(SELECT MIN(id) FROM lover))+(SELECT MIN(id) FROM lover)) AS id) AS t2 "
				+ " WHERE t1.id >= t2.id " 
				+ " ORDER BY t1.id LIMIT 1", new RowMapper<Lover>(){
					@Override
					public Lover mapRow(ResultSet rs, int arg1)
							throws SQLException {
						Lover lover=new Lover();
						lover.setId(rs.getLong("id"));
						lover.setAnotherName(rs.getString("another_name"));
						lover.setSex(rs.getString("sex"));
						lover.setDetails(rs.getString("details"));;
						return lover;
					}
				});
		StringBuffer sb=new StringBuffer("您捡到了一个这样的情话瓶子：\n\n");
		if(loverList.size()>0){
			sb.append(loverList.get(0).getDetails()+"\n");
			sb.append(" ——"+loverList.get(0).getAnotherName() +"，" +loverList.get(0).getSex()+"\n\n");
			jdbcTemplate.update("update lover set counts=counts+1 where id="+loverList.get(0).getId());
		}else{	
			sb.append("[大海上尚未有足够的情话漂流瓶，快去发布一条新的情话吧！]\n\n");
		}
		sb.append("回复[情话]，再次获取；\n"
				+ "回复[发布情话]，发布一条新的情话；\n"
				+ "回复[退出]，返回最上层；\n\n"
				+ "温馨提示：随机获取一条最近两天海上的漂流瓶子，昨天之前的瓶子会被海浪冲走，反复获取到相同的情话是因为海面上的漂流瓶不足。");
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		text.setContent(sb.toString());
        return MessageUtil.textMessageToXML(text);
	}
	public String story(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("开发测试中，暂未开放！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String allayInput(String content,String lastString,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		try {
			Connection conn=jdbcTemplate.getDataSource().getConnection();
			if(conn.getAutoCommit()){
				conn.setAutoCommit(false);
			}
			jdbcTemplate.update("update trouble set counts=counts+1 where id="+Integer.parseInt(lastString.substring(lastString.indexOf(":")+1)));
			jdbcTemplate.execute("INSERT INTO troublesolve (tid,user_name,details,time_occur) VALUES("+Integer.parseInt(lastString.substring(lastString.indexOf(":")+1))
					+",'"+fromUserName+"','"+content+"',now())");
			conn.commit();
			text.setContent("回复成功，已为您返回最上层！");
		} catch (SQLException e) {
			text.setContent("回复失败，系统出现异常，已为您返回最上层！");
		}
		return MessageUtil.textMessageToXML(text);
	}
	
	public String allayYes(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入您的回复内容！\n\n"
        		+ "回复[退出]，返回最上层！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String annoyanceGiveUp(String content,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		
		String lastString=jdbcTemplate.queryForObject("select last_string from laststring where user_name='"+fromUserName +"'", String.class);
		lastString=lastString.substring(lastString.indexOf(":")+1);
		String[] idStringArray=lastString.split(":");
		for(String idString:idStringArray){
			if(idString.startsWith(content+".")){
				String id=idString.substring(idString.indexOf(".")+1);
				try {					
					jdbcTemplate.execute("delete from trouble where id="+Integer.parseInt(id));
			        text.setContent("删除烦恼成功，已返回最上层!");
				} catch (Exception e) {
					text.setContent("删除烦恼失败，已返回最上层!");
				}
			}
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String annoyanceNo(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请重新输入您的烦恼！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String annoyanceYes(String lastString,String toUserName,String fromUserName){
		String annoyance=lastString.substring(lastString.indexOf(":")+1);
		try {
			jdbcTemplate.execute("insert into trouble(user_name,details,time_occur) values('"+fromUserName+"','"+annoyance+"',now())");
			TextMeaasge text = new TextMeaasge();
	        text.setFromUserName(toUserName);         // 发送和回复是反向的
	        text.setToUserName(fromUserName);
	        text.setMsgType("text");
	        text.setCreateTime(new Date().getTime());
	        text.setContent("烦恼录入成功，已返回最上层！");
	        return MessageUtil.textMessageToXML(text);
		} catch (Exception e) {
			e.printStackTrace();
			TextMeaasge text = new TextMeaasge();
	        text.setFromUserName(toUserName);         // 发送和回复是反向的
	        text.setToUserName(fromUserName);
	        text.setMsgType("text");
	        text.setCreateTime(new Date().getTime());
	        text.setContent("烦恼录入失败，已返回最上层！");
	        return MessageUtil.textMessageToXML(text);
		}
	}
	
	public String annoyanceAsk(String content,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());        	
        text.setContent("您输入的烦恼是:\n\n"
        		+ content
        		+"\n\n回复[确认],确认您的输入;\n"
        		+ "回复[修改],重新输入;\n\n"
        		+ "温馨提示：任何情况下回复[退出]，返回最上层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String allay(String lastString,String toUserName,String fromUserName){
		int srcId=0;
		if(lastString.startsWith("解答:")){
			srcId=Integer.parseInt(lastString.substring(lastString.indexOf(":")+1));
		}
		List<Trouble> troubleList=jdbcTemplate.query("SELECT a.id,c.another_name,c.sex,a.details FROM trouble a LEFT JOIN troublesolve b ON a.id=b.tid "
				+ "LEFT JOIN userinfo c ON a.user_name= c.user_name "
				+ "where a.id>"+srcId+" AND a.user_name!='"+fromUserName+"' AND (b.user_name!='"+fromUserName+"' or b.user_name is null) AND a.counts<2 ORDER BY a.time_occur limit 1", new RowMapper<Trouble>(){
					@Override
				public Trouble mapRow(ResultSet rs, int arg1)
						throws SQLException {
					Trouble trouble=new Trouble();
					trouble.setId(rs.getLong("id"));
					trouble.setAnotherName(rs.getString("another_name"));
					trouble.setSex(rs.getString("sex"));
					trouble.setDetails(rs.getString("details"));
					return trouble;
				}
		});
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime()); 
        if(troubleList.size()==0){
        	text.setContent("解忧杂货店里已经没有更多的烦恼，请等候其他用户发布，已返回最上层！");
    		jdbcTemplate.execute("update laststring set last_string='开始',time_occur=now() where user_name='"+fromUserName+"'");
        }else{        	
        	text.setContent("您捡到了一条这样的烦恼漂流瓶子:\n\n"
        			+ troubleList.get(0).getDetails() + "\n"
        			+ "        ——" +troubleList.get(0).getAnotherName() +"，" +troubleList.get(0).getSex()+ "\n\n"
        					+ "回复[回复]，对当前烦恼进行回复\n"
        					+ "回复[下一条]，继续浏览下一条\n"
        					+ "回复[退出]，返回最上层");
        	jdbcTemplate.execute("update laststring set last_string='解答:"+troubleList.get(0).getId()+"',time_occur=now() where user_name='"+fromUserName+"'");
        }
        return MessageUtil.textMessageToXML(text);
	}
	
	public String annoyance(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());        	
        text.setContent("请输入您的烦恼，您的烦恼将会随机漂流到别的用户的聊天窗口里，当烦恼被至少两个用户回复之后，解忧完成;");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String annoyanceRefuse(String toUserName,String fromUserName){
		List<Trouble> troubleList=jdbcTemplate.query("SELECT id,details FROM trouble WHERE user_name='"+fromUserName+"' AND counts<2", new RowMapper<Trouble>(){
			@Override
			public Trouble mapRow(ResultSet rs, int arg1)
					throws SQLException {
				Trouble trouble=new Trouble();
				trouble.setId(rs.getLong("id"));
				trouble.setDetails(rs.getString("details"));;
				return trouble;
			}
		});
		StringBuffer troubleSB=new StringBuffer("您已经发布的待完成的烦恼:\n");
		StringBuffer lastStringSB=new StringBuffer("放弃烦恼");
		for(int i=0 ; i<troubleList.size();i++){
			troubleSB.append((i+1)+"."+troubleList.get(i).getDetails()+"\n");
			lastStringSB.append(":"+(i+1)+"."+troubleList.get(i).getId());
		}
		
		jdbcTemplate.execute("update laststring set last_string='"+lastStringSB.toString()+"',time_occur=now() where user_name='"+fromUserName+"'");                                                                                                                                                                                                                                                                    
    	
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());        	
        text.setContent("您已经有大于两个的烦恼处于漂流待回复状态，请等候烦恼被其他用户回复之后，再重新发布您的烦恼;\n\n"
        		+ troubleSB.toString()
        		+ "\n回复相应编号，放弃发布某个烦恼，立即发布新的烦恼，例如回复数字1放弃烦恼1，一次只能回复一个数字;\n"
        		+ "回复[退出]，返回最上层;");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String allayChoose(String toUserName,String fromUserName){
		List<Trouble> troubleList=jdbcTemplate.query("SELECT b.id,b.details,a.details AS allayDetails,c.another_name,c.sex FROM troublesolve a  left JOIN "
				+ "trouble b on a.tid=b.id left join userinfo c on a.user_name=c.user_name "
				+ "where b.user_name='"+fromUserName+"' and b.counts<=6 order by a.time_occur desc", new RowMapper<Trouble>(){
			@Override
			public Trouble mapRow(ResultSet rs, int arg1)
					throws SQLException {
				Trouble trouble=new Trouble();
				trouble.setId(rs.getLong("id"));
				trouble.setDetails(rs.getString("details"));
				trouble.setAllayDetails(rs.getString("allayDetails"));
				trouble.setAnotherName(rs.getString("another_name"));
				trouble.setSex(rs.getString("sex"));
				return trouble;
			}
		});
		
		StringBuffer troubleListSB=new StringBuffer("您近期被其他用户回复的烦恼:\n\n");
		
		if(troubleList.size()==0){
			troubleListSB.append("[尚未收到回复]");
		}else{			
			for(int i=0;i<troubleList.size();i++){
				troubleListSB.append("烦恼:"+troubleList.get(i).getDetails()+"\n");
				troubleListSB.append("回复:"+troubleList.get(i).getAllayDetails()+"\n");
				troubleListSB.append("来自:"+troubleList.get(i).getAnotherName()+"，"+troubleList.get(i).getSex());
				jdbcTemplate.update("update trouble set counts=counts+1 where counts>=2 and id="+troubleList.get(i).getId());
				if(i!=troubleList.size()-1){
					troubleListSB.append("\n\n");
				}
			}
		}
		
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());        	
        text.setContent("[假如生活欺骗了你，我带你去欺骗生活]\n\n"
        		+ "回复[烦恼]，向别人倾述生活中的琐碎;\n"
        		+ "回复[解答]，浏览、替别人解决生活中的忧愁;\n\n"
        		+ "提示：文明用语避免举报，您的好的回复可能会得到烦恼发布者的[寄信]留言。\n\n"
        		+ troubleListSB.toString());
        
        return MessageUtil.textMessageToXML(text);
	}
	
	public String elseReply(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());        	
        text.setContent("1.僻静的街道旁有一家杂货店，只要写下烦恼投进卷帘门的投信口，第二天就会在店后的牛奶箱里得到答案;\n\n"
        		+"回复[解忧]，进入解忧树洞;\n\n"
        		+"2.如果全世界都对你恶语相加，我愿意对你说上一世情话;\n\n"
        		+"回复[情话]，进入情话漂流;\n\n"
        		+"3.我想写一些琐碎给你，写今晚的月亮和昨天牛肉热干面的味道;\n\n"
        		+"回复[寄信]，给好友留言写信；\n\n"
        		+"4.一些旧信件，在老地方，讲过的故事，像有些人，看一眼就够了;\n\n"
        		+"回复[日记本]，查看和好友的交互记录；\n\n"
        		+"其它：尝试回复[资料卡]、[信铺]、[举报]、[帮助]、[投稿]、[人工]、[给公众号留言]、[群聊]获得其他信息;\n\n"
        		+"温馨提示：资料卡是交互的基础，任何情况下回复退出直接返回最上层，文明用语，避免举报。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public UserInfo getUserInfo(String fromUserName){
		UserInfo userInfo=jdbcTemplate.queryForObject("select another_name,warn,iswarn,isdelete from userinfo where user_name='"+fromUserName+"'",new RowMapper<UserInfo>(){
			@Override
			public UserInfo mapRow(ResultSet rs, int arg1)
					throws SQLException {
				UserInfo userInfo=new UserInfo();
				userInfo.setAnotherName(rs.getString("another_name"));
				userInfo.setWarn(rs.getInt("warn"));
				userInfo.setIswarn(rs.getInt("iswarn"));
				userInfo.setIsdelete(rs.getInt("isdelete"));
				return userInfo;
			}
			
		});
		return userInfo;
	}
	
	public String warnInfo(int warnCount,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        if(warnCount>5){        	
        	text.setContent("您已被多名其他用户举报超过5次，无法使用公众号的社交功能，如有疑议请联系管理员QQ:3315146372！");
        }else if(warnCount>2){        	
        	text.setContent("您已被多名其他用户举报超过3次，望您在公众号使用中文明交流，举报超过5次将被拉入黑名单且无法使用公众号的其他社交功能！");
        }
        return MessageUtil.textMessageToXML(text);
	}
	
	public String gzhMessage(String content,String toUserName,String fromUserName){
		int count=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM gzhmessage WHERE user_name='"+fromUserName+"' AND DATE_FORMAT(time_occur,'%Y-%m-%d')=CURDATE()", Integer.class);
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		if(count>0){
			text.setContent("您当天已经给公众号留言过，此次留言失败，已返回最上层!");
		}else{
			jdbcTemplate.execute("INSERT INTO gzhmessage(user_name,details,time_occur) VALUES('"+fromUserName+"','"+content+"',NOW());");
			text.setContent("留言成功，已返回最上层！");
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String gzhMessageAsk(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("非常感谢您给我们留言，请输入您的留言内容，为了防止恶意刷留言，单个用户一天只能给公众号留言一条！\n\n"
        		+ "回复[退出]，返回最上层!");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String userInfoChangeAskFirst(String toUserName,String fromUserName){
		UserInfo userInfo=jdbcTemplate.queryForObject("select another_name,wechat_name,sex,age,province from userinfo where user_name='" + fromUserName + "'", new RowMapper<UserInfo>(){
			@Override
			public UserInfo mapRow(ResultSet rs, int arg1)
					throws SQLException {
				UserInfo userInfo=new UserInfo();
				userInfo.setAnotherName(rs.getString("another_name"));
				userInfo.setWechatName(rs.getString("wechat_name"));
				userInfo.setSex(rs.getString("sex"));
				userInfo.setAge(rs.getInt("age"));
				userInfo.setProvince(rs.getString("province"));
				return userInfo;
			}
			
		});
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("您录入的资料卡：\n\n"
        		+"别名:" + userInfo.getAnotherName() + "\n"
        		+"微信号:" + userInfo.getWechatName() + "\n"
        		+"性别:" + userInfo.getSex() + "\n"
        		+"年龄:" + userInfo.getAge() + "\n"
        		+"省份:" + userInfo.getProvince() + "\n\n"
        		+"回复[修改]，重新填写资料信息；\n"
        		+"回复[退出]，返回最上层；\n\n"
        		+"温馨提示：每月有一次修改资料卡的机会，别名修改之后不会通知到原先知晓你别名的用户，可能会造成对方跟你的交互困难，所以请谨慎修改别名！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String errorInput(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("输入出错，已返回！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String userInfoChange(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请重新输入您的资料卡，用逗号分隔(格式：别名，微信号，性别，年龄，省份)！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String userInfoConfirmYes(boolean userRegister,String toUserName,String fromUserName){
		String lastString=jdbcTemplate.queryForObject("select last_string from laststring where user_name='"+fromUserName +"'", String.class);
		String[] userinfo=lastString.substring(lastString.indexOf(":")+1).replaceAll("[,，]",",").split(",");
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		int nameCount=jdbcTemplate.queryForObject("select count(id) from userinfo where another_name='"+userinfo[0]+"' and user_name!='"+fromUserName+"'", Integer.class);
		if(nameCount>0){
			text.setContent("公众号别名唯一，已跟别的用户别名冲突，已返回最上层！");
		}else if(userinfo[0].length()>10){
			text.setContent("请输入10字以内的别名长度，已返回最上层！");
		}else if(userinfo[1].length()>20||StringCheckUtil.isChinese(userinfo[1])){
			text.setContent("请输入20个非汉字以内字符的微信账号，已返回最上层！");
		}else{
			try {
				if(!userRegister){
					jdbcTemplate.execute("INSERT INTO userinfo(user_name,another_name,wechat_name,sex,age,province,registertime,isdelete) "
							+"VALUES('" + fromUserName + "','" +userinfo[0]+ "','"+userinfo[1]+"','" +userinfo[2] + "',"+Integer.parseInt(userinfo[3])
							+",'" + userinfo[4] + "', now() ,0);");
					text.setContent("资料卡录入成功，已返回最上层！");
				}else{
					int edit=jdbcTemplate.queryForObject("select edit from userinfo where user_name='"+fromUserName+"'", Integer.class);
					if(edit==1){
						jdbcTemplate.execute("update userinfo set another_name='"+userinfo[0]+"',wechat_name='"+userinfo[1]+"',sex='"+userinfo[2]
								+"',age="+Integer.parseInt(userinfo[3])+",province='"+userinfo[4]+"',edit=0 where user_name='"+fromUserName+"'");
						text.setContent("资料卡录入成功，已返回最上层！");
					}else if(edit==0){
						text.setContent("您当月不能再修改资料卡信息，请等待下个月修改,已返回最上层！");
					}
				}
			} catch (Exception e) {
				text.setContent("资料卡录入失败，可能是微信号重名，已返回最上层！");
			}
		}
		return MessageUtil.textMessageToXML(text);
	}
	
	public String userInfoPatternFailed(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("您输入的资料卡格式有误，请重新输入！\n\n"
        		+ "温馨提示：任何情况下回复[退出]，可直接返回最上层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String userInfoConfirmAsk(String content,String toUserName,String fromUserName){
		content=content.replaceAll("[,，]",",");
		String[] userinfo=content.split(",");
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请确认您填写的资料卡：\n\n"
        		+"别名:" + userinfo[0] + "\n"
        		+"微信号:" + userinfo[1] + "\n"
        		+"性别:" + userinfo[2] + "\n"
        		+"年龄:" + userinfo[3] + "\n"
        		+"省份:" + userinfo[4] + "\n\n"
        		+"回复[修改]，重新填写资料信息；\n"
        		+"回复[确认]，开始录入个人资料；\n\n"
        		+"温馨提示：任何情况下回复[退出]，返回最上层。");
        return MessageUtil.textMessageToXML(text);
	}
	
	public boolean userInfoCheck(String fromUserName){
		int count=jdbcTemplate.queryForObject("SELECT COUNT(id) FROM userinfo WHERE user_name='" + fromUserName + "'", Integer.class);
		if(count>0){
			return true;
		}else{
			return false;
		}
	}
	
	public String userInfoAsk(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("您还未填写资料卡，填写资料卡是开始交互的基础，请填写基本信息，逗号分隔(格式：别名，微信号，性别，年龄，省份);\n\n"
        		+"资料卡信息说明:\n"
        		+"别名(10个字符以内，公众号唯一，用户与用户之间交互留言，将使用别名)；\n"
        		+"微信号(20个字符以内，用户之间，交互次数和交互天数满足一定条件之后，程序将会征求用户是否同意彼此之间暴露微信账号)；\n\n"
        		+"格式举例:\n"
        		+"三三两两,oklee_1998,女,19,浙江");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String reportSuccessful(String warnUserName,String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
		text.setFromUserName(toUserName);         // 发送和回复是反向的
		text.setToUserName(fromUserName);
		text.setMsgType("text");
		text.setCreateTime(new Date().getTime());
		
		int warnCount=jdbcTemplate.queryForObject("select count(*) from warn where user_name='"+fromUserName+"' and warn_username='"
				+warnUserName+"' and DATE_FORMAT(time_occur,'%Y-%m-%d 00:00:00')=CURDATE()", Integer.class);
		if(warnCount>0){
			text.setContent("您当天已经举报过此用户，已返回最上层!");
		}else{
			jdbcTemplate.execute("INSERT INTO warn(user_name,warn_username,time_occur) VALUES('"+fromUserName+"','"+warnUserName+"',now())");
			text.setContent("举报成功，已返回最上层!");
		}
        return MessageUtil.textMessageToXML(text);
	}
	
	public String reportFailed(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入您要举报的用户的别名不存在或已被对方修改，已返回最上层!");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String report(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("请输入您要举报的用户的别名，请勿恶意举报用户，恶意举报用户同样会被记录次数!");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String exit(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("退出成功，已返回最上层!");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String help(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("公众号社交功能简介:\n\n"
        		+ "设计思路,在解忧、情话的基础上让用户产生首次的联系，首次联系可以知晓对方的别名，然后别名可以用来寄信，寄信交互五天以上，就可以触发系统询问是否暴露微信账号给对方；\n\n"
        		+ "[资料卡]，关注公众号之后，会提示录入[资料卡]，资料卡包括了别名、微信号、性别、年龄、省份，这里提到的别名比较重要，所有的用户交互中都会使用到别名作为标识，"
        		+ "每月有一次修改资料卡的机会，轻易更改别名可能会导致交互失败，"
        		+ "填写资料卡是其它社交功能例如[解忧]、[情话]、[寄信]的基础；\n\n"
        		+ "[解忧]，发布烦恼，每次最多可以发布两个进行中的烦恼，烦恼将会按时间发布顺序漂流到别的用户的聊天窗口里；"
        		+ "当一个烦恼被两个以上的用户回复之后，这个烦恼将会进入完成状态，"
        		+ "好的回复可能会得到烦恼主用户的[寄信](类似于私信)回复；\n\n"
        		+ "[情话]，情话漂流，您可以选择获取一条情话，或者发布情话，您发布的比较好的情话可能会得到别的用户的青睐留言；\n\n"
        		+ "[寄信]，通过知晓对方的别名，可以通过寄信主动给对方写信留言，信件将会在第二天到达对方的邮箱里；\n\n"
        		+ "[日记本]，日记本记录了您最近和其他用户的交互记录，当交互记录达到一定条件之后，系统会征求双方是否同意暴露[资料卡]信息；\n\n"
        		+ "[信铺]，如果你想给好友写一封实体的信件，又碍于自己字体不好没有信封邮票等等，不凡来铺子里看看；\n\n"
        		+ "[举报]，对于用户发布的恶意的言论，您可以通过举报输入别名的方式进行举报，当用户被举报次数达到一定次数时会触发警告，情节特别严重的我们会直接禁用掉所有社交功能拉入黑名单；");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String subscribe(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("像夏日里一个停电的午后\n"
        		+"无聊的呆在家翻出一本书\n"
        		+"泡上一壶茶水\n"
        		+"打开很久没用的收音机\n"
        		+"漫无目的地调频\n"
        		+"忽然听到一首歌\n"
        		+"慢慢把声音调的尽可能清晰\n"
        		+"虽然不懂里面的人在唱着什么\n"
        		+"也记不起歌的名字\n"
        		+"可是合着外面的蝉鸣\n"
        		+"整颗心都沉浸去了\n\n"
        		+"回复[hello]，开始你的轻社交之旅");
        return MessageUtil.textMessageToXML(text);
	}
	
	public String unSubscribe(String toUserName,String fromUserName){
		TextMeaasge text = new TextMeaasge();
        text.setFromUserName(toUserName);         // 发送和回复是反向的
        text.setToUserName(fromUserName);
        text.setMsgType("text");
        text.setCreateTime(new Date().getTime());
        text.setContent("取消关注成功！");
        return MessageUtil.textMessageToXML(text);
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		container.setPort(80);
	}
}
