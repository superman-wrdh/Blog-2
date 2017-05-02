package org.fczm.blog.service.util;

import org.directwebremoting.WebContextFactory;
import org.fczm.blog.component.ConfigComponent;
import org.fczm.blog.dao.AttachmentDao;
import org.fczm.blog.dao.BlogDao;
import org.fczm.blog.dao.CommentDao;
import org.fczm.blog.dao.IllustrationDao;
import org.fczm.blog.dao.TypeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.sf.json.JSONObject;

public class ManagerTemplate {

	@Autowired
	protected BlogDao blogDao;

	@Autowired
	protected CommentDao commentDao;

	@Autowired
	protected TypeDao typeDao;

	@Autowired
	protected IllustrationDao illustrationDao;

	@Autowired
	protected AttachmentDao attachmentDao;

	@Autowired
	protected ConfigComponent configComponent;
	
	private WebApplicationContext context = null;

	public void setContext(WebApplicationContext context) {
		this.context = context;
	}

	public WebApplicationContext getContext() {
		if(context == null) {
			context = WebApplicationContextUtils.getWebApplicationContext(WebContextFactory.get().getServletContext());
		}
		return context;
	}
	
	public JSONObject getPageSizeConfig() {
		ConfigManager manager = (ConfigManager)getContext().getBean("configManager");
		return manager.getConfig().getJSONObject("pageSize");
	}

}
